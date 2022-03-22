# -*- coding: utf-8 -*-

# Copyright 2021 Tianmian Tech. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Copyright 2019 The FATE Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import uuid
from collections import Iterable
from functools import partial
from operator import is_not

import numpy as np

from common.python.p_session.base_impl.db_runtime import DBRuntime
from common.python.storage import StoreType
from common.python.utils import cloudpickle as f_pickle
from common.python.utils.core_utils import deserialize
from common.python.utils.store_type import DBTypes


class _TaskInfo:
    def __init__(self, task_id, function_id, function_bytes, is_in_place_computing):
        self._task_id = task_id
        self._function_id = function_id
        self._function_bytes = function_bytes
        self._is_in_place_computing = is_in_place_computing


class _Operand:
    def __init__(self, _type, namespace, name, partition, partitions):
        self._type = _type
        self._namespace = namespace
        self._name = name
        self._partition = partition
        self._partitions = partitions


def __get_function(info: _TaskInfo):
    return f_pickle.loads(info._function_bytes)


class _UnaryProcess:
    def __init__(self, task_info: _TaskInfo, operand: _Operand):
        self._info = task_info
        self._operand = operand
        # self._process_conf = process_conf


class _BinaryProcess:
    def __init__(self, task_info: _TaskInfo, left: _Operand, right: _Operand):
        self._info = task_info
        self._left = left
        self._right = right


def _serialize_and_hash_func(func):
    pickled_function = f_pickle.dumps(func)
    func_id = str(uuid.uuid1())
    return func_id, pickled_function


def _submit_to_pool(base_source, func, _do_func):
    func_id, pickled_function = _serialize_and_hash_func(func)
    _task_info = _TaskInfo(DBRuntime.get_instance().session_id, func_id, pickled_function,
                           base_source.get_in_place_computing())
    results = []
    if _support_partition_storage():
        for p in range(base_source._partitions):
            _op = _Operand(base_source._type, base_source._namespace, base_source._name, p, base_source._partitions)
            # _p_conf = _ProcessConf.get_default()
            _p = _UnaryProcess(_task_info, _op)
            results.append(DBRuntime.get_instance().pool.submit(_do_func, _p))
    else:
        _op = _Operand(base_source._type, base_source._namespace, base_source._name, None, base_source._partitions)
        # _p_conf = _ProcessConf.get_default()
        _p = _UnaryProcess(_task_info, _op)
        results.append(DBRuntime.get_instance().pool.submit(_do_func, _p))
    return results


def __create_output_operand(src_op, task_info):
    name = task_info._function_id
    from common.python import RuntimeInstance
    from common.python.common.consts import NAMESPACE
    tname = f"{RuntimeInstance.SESSION._session_id}_{name}"
    return _Operand(StoreType.PROCESS.value, NAMESPACE.PROCESS, tname, src_op._partition, src_op._partitions)


def __get_is_in_place_computing(info: _TaskInfo):
    return info._is_in_place_computing


def _support_partition_storage():
    db_type = DBRuntime.get_instance().get_storage_session().get_db_type()
    return db_type in [DBTypes.LMDB, DBTypes.LOCAL_FS]


def _get_table_from_operand(op: _Operand):
    return DBRuntime.get_instance().table(op._name, op._namespace, op._partitions,
                                          persistent=False, persistent_engine=StoreType.PROCESS)


def do_map(p: _UnaryProcess):
    _mapper = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info)
    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    def mapper_result():
        for k, v in src_table.collect(partition=op._partition):
            yield _mapper(k, v)

    dst_table.put_all(mapper_result())
    return rtn


def do_map_value(p: _UnaryProcess):
    _mapper = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info)
    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    def mapper_result():
        for k, v in src_table.collect(partition=op._partition):
            yield k, _mapper(v)

    dst_table.put_all(mapper_result())
    return rtn


def do_apply_partitions(p: _UnaryProcess):
    _func = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info)
    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    v = _func(src_table.collect(partition=op._partition))
    dst_table.put(op._partition or 0, v)
    return rtn


def do_map_partitions(p: _UnaryProcess):
    _func = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info)
    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    v = _func(src_table.collect(partition=op._partition))
    dst_table.put(op._partition or 0, v)
    return rtn


def do_map_partitions2(p: _UnaryProcess):
    _func = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info)
    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    v = _func(src_table.collect(partition=op._partition))
    if isinstance(v, Iterable):
        dst_table.put_all(v)
    else:
        dst_table.put(op._partition or 0, v)
    return rtn


def do_reduce(p: _UnaryProcess):
    _reducer = __get_function(p._info)
    op = p._operand
    src_table = _get_table_from_operand(op)

    value = None
    for k, v in src_table.collect(partition=op._partition):
        if value is None:
            value = v
        else:
            value = _reducer(value, v)

    return value


def do_join(p: _BinaryProcess):
    _joiner = __get_function(p._info)
    is_in_place_computing = __get_is_in_place_computing(p._info)
    left_op = p._left
    right_op = p._right
    rtn = __create_output_operand(left_op, p._info)

    left_table = _get_table_from_operand(left_op)
    right_table = _get_table_from_operand(right_op)
    dst_table = _get_table_from_operand(rtn)

    if left_table.count() >= right_table.count():
        left_is_large = True
        large_table = left_table
        small_table = right_table
    else:
        left_is_large = False
        large_table = right_table
        small_table = left_table

    small_kv_dict = dict(small_table.collect(partition=left_op._partition))
    result = []
    for k, v in large_table.collect(partition=left_op._partition):
        other_v = small_kv_dict.get(k)
        if other_v is not None:
            if left_is_large:
                new_v = _joiner(v, other_v)
            else:
                new_v = _joiner(other_v, v)
            result.append((k, new_v))

    dst_table.put_all(result)
    del small_kv_dict
    del result
    return rtn


def do_glom(p: _UnaryProcess):
    op = p._operand
    rtn = __create_output_operand(op, p._info)

    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    v_list = []
    key = None
    for k, v in src_table.collect(partition=op._partition):
        v_list.append((k, v))
        key = k

    dst_table.put(key, v_list)
    return rtn


def do_sample(p: _UnaryProcess):
    op = p._operand
    rtn = __create_output_operand(op, p._info)

    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    fraction, seed = deserialize(p._info._function_bytes)
    random_state = np.random.RandomState(seed)

    result = []
    for k, v in src_table.collect(partition=op._partition):
        if random_state.rand() < fraction:
            result.append((k, v))

    dst_table.put_all(result)

    return rtn


def do_subtract_by_key(p: _BinaryProcess):
    is_in_place_computing = __get_is_in_place_computing(p._info)
    left_op = p._left
    right_op = p._right
    rtn = __create_output_operand(left_op, p._info)

    left_table = _get_table_from_operand(left_op)
    right_table = _get_table_from_operand(right_op)
    dst_table = _get_table_from_operand(rtn)

    right_key_set = set()
    for k, v in right_table.collect(partition=right_op._partition):
        right_key_set.add(k)

    result = []
    for k, v in left_table.collect(partition=left_op._partition):
        if k not in right_key_set:
            result.append((k, v))

    dst_table.put_all(result)
    return rtn


def do_filter(p: _UnaryProcess):
    _func = __get_function(p._info)
    is_in_place_computing = __get_is_in_place_computing(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info)

    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    def result_generator():
        for k, v in src_table.collect(partition=op._partition):
            if _func(k, v):
                yield k, v

    dst_table.put_all(result_generator())
    return rtn


def do_union(p: _BinaryProcess):
    _func = __get_function(p._info)
    is_in_place_computing = __get_is_in_place_computing(p._info)
    left_op = p._left
    right_op = p._right
    rtn = __create_output_operand(left_op, p._info)

    left_table = _get_table_from_operand(left_op)
    right_table = _get_table_from_operand(right_op)
    dst_table = _get_table_from_operand(rtn)

    def result_generator():

        left_key_set = set()
        conflict_result = {}
        for k, v in left_table.collect(partition=left_op._partition):
            left_key_set.add(k)

        for k, v in right_table.collect(partition=right_op._partition):
            if k not in left_key_set:
                yield k, v
            else:
                conflict_result[k] = v

        # apply func to merge conflict key
        for k, v in left_table.collect(partition=left_op._partition):
            if k in conflict_result:
                final_v = _func(v, conflict_result[k])
                yield k, final_v
            else:
                yield k, v

    dst_table.put_all(result_generator())
    return rtn


def do_flat_map(p: _UnaryProcess):
    _func = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info)

    src_table = _get_table_from_operand(op)
    dst_table = _get_table_from_operand(rtn)

    def result_generator():
        for k, v in src_table.collect(partition=op._partition):
            map_result = _func(k, v)
            for result_k, result_v in map_result:
                yield result_k, result_v

    dst_table.put_all(result_generator())
    return rtn


def map(data_source, func):
    results = _submit_to_pool(data_source, func, do_map)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def map_value(data_source, func):
    results = _submit_to_pool(data_source, func, do_map_value)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def apply_partitions(data_source, func):
    results = _submit_to_pool(data_source, func, do_apply_partitions)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def map_partitions(data_source, func):
    results = _submit_to_pool(data_source, func, do_map_partitions)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def map_reduce_partitions(data_source, mapper, reducer):
    dup = DBRuntime.get_instance().table(
        str(uuid.uuid1()),
        data_source._namespace,
        data_source._partitions,
        persistent=False
    )

    def _dict_reduce(a: dict, b: dict):
        for k, v in b.items():
            if k not in a:
                a[k] = v
            else:
                a[k] = reducer(a[k], v)
        return a

    def _local_map_reduce(it):
        ret = {}
        for _k, _v in mapper(it):
            if _k not in ret:
                ret[_k] = _v
            else:
                ret[_k] = reducer(ret[_k], _v)
        return ret

    dup.put_all(
        data_source.applyPartitions(_local_map_reduce).reduce(_dict_reduce).items()
    )
    return dup


def map_partition2(data_source, func, need_shuffle=True):
    results = _submit_to_pool(data_source, func, do_map_partitions2)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def reduce(data_source, func):
    rs = [r.result() for r in _submit_to_pool(data_source, func, do_reduce)]
    rs = [r for r in filter(partial(is_not, None), rs)]
    if len(rs) <= 0:
        return None
    rtn = rs[0]
    for r in rs[1:]:
        rtn = func(rtn, r)
    return rtn


def join(data_source, other, func):
    _session_id = DBRuntime.get_instance().session_id
    if other._partitions != data_source._partitions:
        if other.count() > data_source.count():
            return data_source.save_as(str(uuid.uuid1()), _session_id, partition=other._partitions).join(other,
                                                                                                         func)
        else:
            return data_source.join(other.save_as(str(uuid.uuid1()), _session_id, partition=data_source._partitions),
                                    func)

    func_id, pickled_function = _serialize_and_hash_func(func)
    _task_info = _TaskInfo(_session_id, func_id, pickled_function, data_source.get_in_place_computing())
    results = []
    for p in range(data_source._partitions):
        _left = _Operand(data_source._type, data_source._namespace, data_source._name, p, data_source._partitions)
        _right = _Operand(other._type, other._namespace, other._name, p, data_source._partitions)
        _p = _BinaryProcess(_task_info, _left, _right)
        results.append(DBRuntime.get_instance().pool.submit(do_join, _p))
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def glom(data_source):
    results = _submit_to_pool(data_source, None, do_glom)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def glom(data_source):
    results = _submit_to_pool(data_source, None, do_glom)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def sample(data_source, fraction, seed=None):
    results = _submit_to_pool(data_source, (fraction, seed), do_sample)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def subtract_by_key(data_source, other):
    _session_id = DBRuntime.get_instance().session_id
    if other._partitions != data_source._partitions:
        if other.count() > data_source.count():
            return data_source.save_as(str(uuid.uuid1()), _session_id, partition=other._partitions).subtractByKey(other)
        else:
            return data_source.union(other.save_as(str(uuid.uuid1()), _session_id, partition=data_source._partitions))
    func_id, pickled_function = _serialize_and_hash_func(
        data_source._namespace + '.' + data_source._name + '-' + other._namespace + '.' + other._name)
    _task_info = _TaskInfo(_session_id, func_id, pickled_function, data_source.get_in_place_computing())
    results = []
    for p in range(data_source._partitions):
        _left = _Operand(data_source._type, data_source._namespace, data_source._name, p, data_source._partitions)
        _right = _Operand(other._type, other._namespace, other._name, p, other._partitions)
        _p = _BinaryProcess(_task_info, _left, _right)
        results.append(DBRuntime.get_instance().pool.submit(do_subtract_by_key, _p))
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def filter_(data_source, func):
    results = _submit_to_pool(data_source, func, do_filter)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def union(data_source, other, func=lambda v1, v2: v1):
    _session_id = DBRuntime.get_instance().session_id
    if other._partitions != data_source._partitions:
        if other.count() > data_source.count():
            return data_source.save_as(str(uuid.uuid1()), _session_id, partition=other._partitions).union(other,
                                                                                                          func)
        else:
            return data_source.union(other.save_as(str(uuid.uuid1()), _session_id, partition=data_source._partitions),
                                     func)
    func_id, pickled_function = _serialize_and_hash_func(func)
    _task_info = _TaskInfo(_session_id, func_id, pickled_function, data_source.get_in_place_computing())
    results = []
    for p in range(data_source._partitions):
        _left = _Operand(data_source._type, data_source._namespace, data_source._name, p, data_source._partitions)
        _right = _Operand(other._type, other._namespace, other._name, p, other._partitions)
        _p = _BinaryProcess(_task_info, _left, _right)
        results.append(DBRuntime.get_instance().pool.submit(do_union, _p))
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)


def flat_map(data_source, func):
    results = _submit_to_pool(data_source, func, do_flat_map)
    for r in results:
        result = r.result()
    return DBRuntime.get_instance().table(result._name, result._namespace, data_source._partitions, persistent=False)
