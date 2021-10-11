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


import fnmatch
import hashlib
import os
import random
import shutil
import socket
import time
import uuid
from collections import Iterable
from concurrent.futures import ProcessPoolExecutor as Executor
from contextlib import ExitStack
from functools import partial
from operator import is_not

import numpy as np
from cachetools import cached

from common.python.common.consts import NAMESPACE, TABLENAME
from common.python.storage import StoreType
from common.python.utils import cache_utils, file_utils, cloudpickle as f_pickle
from common.python.utils.core_utils import serialize, deserialize
from common.python.utils.store_type import DBTypes

DELIMETER = '-'
DELIMETER_ENCODED = DELIMETER.encode()


class DBRuntime:
    __instance = None

    def __init__(self, storage_session):
        self.data_dir = os.path.join(file_utils.get_project_base_directory(), 'data', 'LMDB')
        self.session_id = storage_session.get_session_id()

        self.db_type = storage_session.get_db_type()

        self.meta_table = _DSource(StoreType.PERSISTENCE.value, NAMESPACE.DATA, TABLENAME.FRAGMENTS, 10,
                                   db_type=self.db_type)
        self.pool = Executor()
        DBRuntime.__instance = self

        self.storage_session = storage_session

        self.unique_id_template = '_Storage_%s_%s_%s_%.20f_%d'

        storage_session.set_gc_table(self)
        storage_session.add_cleanup_task(storage_session.clean_duplicated_table)

        try:
            self.host_name = socket.gethostname()
            self.host_ip = socket.gethostbyname(self.host_name)
        except socket.gaierror:
            self.host_name = 'unknown'
            self.host_ip = 'unknown'

    def get_storage_session(self):
        return self.storage_session

    def stop(self):
        self.storage_session.run_cleanup_tasks(DBRuntime.get_instance())
        self.__instance = None

    def is_stopped(self):
        return (self.__instance is None)

    def table(self, name, namespace, partition=1, create_if_missing=True, error_if_exist=False, persistent=True,
              in_place_computing=False, persistent_engine=StoreType.PERSISTENCE):
        __type = persistent_engine.value if persistent else StoreType.PROCESS.value
        _table_key = ".".join([__type, namespace, name])
        self.meta_table.put_if_absent(_table_key, partition)
        partition = self.meta_table.get(_table_key)

        __table = _DSource(__type, namespace, name, partition, in_place_computing, self.db_type)
        if persistent is False and persistent_engine == StoreType.PROCESS:
            count = self.storage_session._gc_table.get(name)
            if count is None:
                count = 0
            self.storage_session._gc_table.put(name, count + 1)
        return __table

    def parallelize(self, data: Iterable, include_key=False, name=None, partition=1, namespace=None,
                    create_if_missing=True,
                    error_if_exist=False,
                    persistent=False, chunk_size=100000, in_place_computing=False,
                    persistent_engine=StoreType.PERSISTENCE):
        _iter = data if include_key else enumerate(data)
        if name is None:
            name = self.session_id + "_" + str(uuid.uuid1())
        if namespace is None:
            namespace = NAMESPACE.PROCESS
        __table = self.table(name, namespace, partition, persistent=persistent, in_place_computing=in_place_computing)
        __table.put_all(_iter, chunk_size=chunk_size)
        return __table

    def cleanup(self, name, namespace, persistent=None):
        if not namespace or not name:
            raise ValueError("neither name nor namespace can be blank")

        if self.db_type == DBTypes.CLICKHOUSE:
            from common.python.storage.impl import clickhouse_storage
            clickhouse_storage.clean_up_tables(name, namespace)

        else:
            # _type = StoreType.PERSISTENCE.value if persistent else StoreType.PROCESS.value
            # _base_dir = os.sep.join([DBRuntime.get_instance().data_dir, _type])
            _base_dir = DBRuntime.get_instance().data_dir
            if not os.path.isdir(_base_dir):
                raise EnvironmentError("illegal datadir")

            _namespace_dir = os.sep.join([_base_dir, namespace])
            if not os.path.isdir(_namespace_dir):
                return

            _tables_to_delete = fnmatch.filter(os.listdir(_namespace_dir), name)
            for table in _tables_to_delete:
                shutil.rmtree(os.sep.join([_namespace_dir, table]))

    def generateUniqueId(self):
        return self.unique_id_template % (
            self.session_id, self.host_name, self.host_ip, time.time(), random.randint(10000, 99999))

    @staticmethod
    def get_instance():
        if DBRuntime.__instance is None:
            raise EnvironmentError("should initialize before use")
        return DBRuntime.__instance


def _evict(_, env):
    env.close()


@cached(cache=cache_utils.EvictLRUCache(maxsize=64, evict=_evict))
def _open_env(path, write=False):
    import lmdb
    os.makedirs(path, exist_ok=True)
    return lmdb.open(path, create=True, max_dbs=1, max_readers=1024, lock=write, sync=True, map_size=10_737_418_240)


def _get_db_path(*args):
    return os.sep.join([DBRuntime.get_instance().data_dir, *args])


def _get_env(*args, write=False):
    _path = _get_db_path(*args)
    return _open_env(_path, write=write)


def _hash_key_to_partition(key, partitions):
    _key = hashlib.sha1(key).digest()
    if isinstance(_key, bytes):
        _key = int.from_bytes(_key, byteorder='little', signed=False)
    if partitions < 1:
        raise ValueError('partitions must be a positive number')
    b, j = -1, 0
    while j < partitions:
        b = int(j)
        _key = ((_key * 2862933555777941757) + 1) & 0xffffffffffffffff
        j = float(b + 1) * (float(1 << 31) / float((_key >> 33) + 1))
    return int(b)


class _TaskInfo:
    def __init__(self, task_id, function_id, function_bytes, is_in_place_computing):
        self._task_id = task_id
        self._function_id = function_id
        self._function_bytes = function_bytes
        self._is_in_place_computing = is_in_place_computing


class _MapReduceTaskInfo:
    def __init__(self, task_id, function_id, map_function_bytes, reduce_function_bytes, is_in_place_computing):
        self._task_id = task_id
        self._function_id = function_id
        self._map_function_bytes = map_function_bytes
        self._reduce_function_bytes = reduce_function_bytes
        self._is_in_place_computing = is_in_place_computing

    def get_mapper(self):
        return f_pickle.loads(self._map_function_bytes)

    def get_reducer(self):
        return f_pickle.loads(self._reduce_function_bytes)


class _ProcessConf:
    def __init__(self, naming_policy):
        self._naming_policy = naming_policy

    @staticmethod
    def get_default():
        return _ProcessConf(DBRuntime.get_instance().get_storage_session().get_naming_policy().value)


class _Operand:
    def __init__(self, _type, namespace, name, partition):
        self._type = _type
        self._namespace = namespace
        self._name = name
        self._partition = partition

    def __str__(self):
        return _get_db_path(self._type, self._namespace, self._name, str(self._partition))

    def as_env(self, write=False):
        return _get_env(self._namespace, self._name, str(self._partition), write=write)


class _UnaryProcess:
    def __init__(self, task_info: _TaskInfo, operand: _Operand, process_conf: _ProcessConf):
        self._info = task_info
        self._operand = operand
        self._process_conf = process_conf


class _MapReduceProcess:
    def __init__(self, task_info: _MapReduceTaskInfo, operand: _Operand):
        self._info = task_info
        self._operand = operand
        # self._process_conf = process_conf

    def output_operand(self):
        return _Operand(self._operand._type, self._info._task_id, self._info._function_id, self._operand._partition)

    def get_mapper(self):
        return self._info.get_mapper()

    def get_reducer(self):
        return self._info.get_reducer()


class _BinaryProcess:
    def __init__(self, task_info: _TaskInfo, left: _Operand, right: _Operand, process_conf: _ProcessConf):
        self._info = task_info
        self._left = left
        self._right = right
        self._process_conf = process_conf


def __get_function(info: _TaskInfo):
    return f_pickle.loads(info._function_bytes)


def __get_is_in_place_computing(info: _TaskInfo):
    return info._is_in_place_computing


def _generator_from_cursor(cursor):
    for k, v in cursor:
        yield deserialize(k), deserialize(v)


def do_map(p: _UnaryProcess):
    _mapper = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info, p._process_conf, False)
    source_env = op.as_env()
    _table_key = ".".join([op._type, op._namespace, op._name])
    txn_map = {}
    partitions = DBRuntime.get_instance().meta_table.get(_table_key)
    for p in range(partitions):
        env = _get_env(rtn._namespace, rtn._name, str(p), write=True)
        txn = env.begin(write=True)
        txn_map[p] = txn
    with source_env.begin() as source_txn:
        cursor = source_txn.cursor()
        for k_bytes, v_bytes in cursor:
            k, v = deserialize(k_bytes), deserialize(v_bytes)
            k1, v1 = _mapper(k, v)
            k1_bytes, v1_bytes = serialize(k1), serialize(v1)
            p = _hash_key_to_partition(k1_bytes, partitions)
            dest_txn = txn_map[p]
            dest_txn.put(k1_bytes, v1_bytes)
        cursor.close()
    for p, txn in txn_map.items():
        txn.commit()
    return rtn


def do_apply_partitions(p: _UnaryProcess):
    with ExitStack() as s:
        _mapper = __get_function(p._info)
        op = p._operand
        rtn = __create_output_operand(op, p._info, p._process_conf, False)
        source_env = s.enter_context(op.as_env())
        dst_env = s.enter_context(rtn.as_env(write=True))

        source_txn = s.enter_context(source_env.begin())
        dst_txn = s.enter_context(dst_env.begin(write=True))

        cursor = s.enter_context(source_txn.cursor())
        v = _mapper(_generator_from_cursor(cursor))
        if cursor.last():
            k_bytes = cursor.key()
            dst_txn.put(k_bytes, serialize(v))
    return rtn


def do_map_partitions(p: _UnaryProcess):
    _mapper = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info, p._process_conf, False)
    source_env = op.as_env()
    dst_env = rtn.as_env(write=True)
    with source_env.begin() as source_txn:
        with dst_env.begin(write=True) as dst_txn:
            cursor = source_txn.cursor()
            v = _mapper(_generator_from_cursor(cursor))
            if cursor.last():
                k_bytes = cursor.key()
                dst_txn.put(k_bytes, serialize(v))
            cursor.close()
    return rtn


# def _do_map_reduce_partitions(p: _MapReduceProcess):
#     rtn = p.output_operand()
#     op = p._operand
#     source_env = op.as_env()
#     _table_key = ".".join([op._type, op._namespace, op._name])
#     partitions = DBRuntime.get_instance().meta_table.get(_table_key)
#     txn_map = {}
#     for partition in range(partitions):
#         env = _get_env(rtn._namespace, rtn._name, str(partition), write=True)
#         txn_map[partition] = env.begin(write=True)
#     source_txn = source_env.begin()
#     cursor = source_txn.cursor()
#     mapped = p.get_mapper()(_generator_from_cursor(cursor))
#     if not isinstance(mapped, Iterable):
#         raise ValueError(f"mapper function should return a iterable of pair")
#     reducer = p.get_reducer()
#     for k, v in mapped:
#         k_bytes = serialize(k)
#         partition = _hash_key_to_partition(k_bytes, partitions)
#         # todo: not atomic, fix me
#         pre_v = txn_map[partition].get(k_bytes, None)
#         if pre_v is None:
#             txn_map[partition].put(k_bytes, serialize(v))
#         else:
#             txn_map[partition].put(k_bytes, serialize(reducer(deserialize(pre_v), v)))
#     return rtn

def do_map_partitions2(p: _UnaryProcess):
    _mapper = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info, p._process_conf, False)
    source_env = op.as_env()
    dst_env = rtn.as_env(write=True)
    with source_env.begin() as source_txn:
        with dst_env.begin(write=True) as dst_txn:
            cursor = source_txn.cursor()
            v = _mapper(_generator_from_cursor(cursor))
            if cursor.last():
                if isinstance(v, Iterable):
                    for k1, v1 in v:
                        dst_txn.put(serialize(k1), serialize(v1))
                else:
                    k_bytes = cursor.key()
                    dst_txn.put(k_bytes, serialize(v))
            cursor.close()
    return rtn


def do_map_values(p: _UnaryProcess):
    _mapper = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info, p._process_conf, True)
    source_env = op.as_env()
    dst_env = rtn.as_env(write=True)
    with source_env.begin() as source_txn:
        with dst_env.begin(write=True) as dst_txn:
            cursor = source_txn.cursor()
            for k_bytes, v_bytes in cursor:
                v = deserialize(v_bytes)
                v1 = _mapper(v)
                dst_txn.put(k_bytes, serialize(v1))
            cursor.close()
    return rtn


def do_join(p: _BinaryProcess):
    _joiner = __get_function(p._info)
    is_in_place_computing = __get_is_in_place_computing(p._info)
    left_op = p._left
    right_op = p._right
    rtn = __create_output_operand(left_op, p._info, p._process_conf, True)
    right_env = right_op.as_env()
    left_env = left_op.as_env()
    dst_env = rtn.as_env(write=True)
    with left_env.begin() as left_txn:
        with right_env.begin() as right_txn:
            with dst_env.begin(write=True) as dst_txn:
                cursor = left_txn.cursor()
                for k_bytes, v1_bytes in cursor:
                    v2_bytes = right_txn.get(k_bytes)
                    if v2_bytes is None:
                        if is_in_place_computing:
                            dst_txn.delete(k_bytes)
                        continue
                    v1 = deserialize(v1_bytes)
                    v2 = deserialize(v2_bytes)
                    v3 = _joiner(v1, v2)
                    dst_txn.put(k_bytes, serialize(v3))
    return rtn


def do_reduce(p: _UnaryProcess):
    _reducer = __get_function(p._info)
    op = p._operand
    source_env = op.as_env()
    value = None
    with source_env.begin() as source_txn:
        cursor = source_txn.cursor()
        for k_bytes, v_bytes in cursor:
            v = deserialize(v_bytes)
            if value is None:
                value = v
            else:
                value = _reducer(value, v)
    return value


def do_glom(p: _UnaryProcess):
    op = p._operand
    rtn = __create_output_operand(op, p._info, p._process_conf, False)
    source_env = op.as_env()
    dst_env = rtn.as_env(write=True)
    with source_env.begin() as source_txn:
        with dst_env.begin(write=True) as dest_txn:
            cursor = source_txn.cursor()
            v_list = []
            k_bytes = None
            for k, v in cursor:
                v_list.append((deserialize(k), deserialize(v)))
                k_bytes = k
            if k_bytes is not None:
                dest_txn.put(k_bytes, serialize(v_list))
    return rtn


def do_sample(p: _UnaryProcess):
    op = p._operand
    rtn = __create_output_operand(op, p._info, p._process_conf, False)
    source_env = op.as_env()
    dst_env = rtn.as_env(write=True)
    fraction, seed = deserialize(p._info._function_bytes)
    with source_env.begin() as source_txn:
        with dst_env.begin(write=True) as dst_txn:
            cursor = source_txn.cursor()
            cursor.first()
            random_state = np.random.RandomState(seed)
            for k, v in cursor:
                if random_state.rand() < fraction:
                    dst_txn.put(k, v)
    return rtn


def do_subtract_by_key(p: _BinaryProcess):
    is_in_place_computing = __get_is_in_place_computing(p._info)
    left_op = p._left
    right_op = p._right
    rtn = __create_output_operand(left_op, p._info, p._process_conf, True)
    right_env = right_op.as_env()
    left_env = left_op.as_env()
    dst_env = rtn.as_env(write=True)

    with left_env.begin() as left_txn:
        with right_env.begin() as right_txn:
            with dst_env.begin(write=True) as dst_txn:
                cursor = left_txn.cursor()
                for k_bytes, left_v_bytes in cursor:
                    right_v_bytes = right_txn.get(k_bytes)
                    if right_v_bytes is None:
                        if not is_in_place_computing:  # add to new table (not in-place)
                            dst_txn.put(k_bytes, left_v_bytes)
                    else:  # delete in existing table (in-place)
                        if is_in_place_computing:
                            dst_txn.delete(k_bytes)
                cursor.close()
    return rtn


def do_filter(p: _UnaryProcess):
    _func = __get_function(p._info)
    is_in_place_computing = __get_is_in_place_computing(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info, p._process_conf, True)
    source_env = op.as_env()
    dst_env = rtn.as_env(write=True)

    with source_env.begin() as source_txn:
        with dst_env.begin(write=True) as dst_txn:
            cursor = source_txn.cursor()
            for k_bytes, v_bytes in cursor:
                k = deserialize(k_bytes)
                v = deserialize(v_bytes)
                if _func(k, v):
                    if not is_in_place_computing:
                        dst_txn.put(k_bytes, v_bytes)
                else:
                    if is_in_place_computing:
                        dst_txn.delete(k_bytes)
            cursor.close()
    return rtn


def do_union(p: _BinaryProcess):
    _func = __get_function(p._info)
    is_in_place_computing = __get_is_in_place_computing(p._info)
    left_op = p._left
    right_op = p._right
    rtn = __create_output_operand(left_op, p._info, p._process_conf, True)
    right_env = right_op.as_env()
    left_env = left_op.as_env()
    dst_env = rtn.as_env(write=True)

    with left_env.begin() as left_txn:
        with right_env.begin() as right_txn:
            with dst_env.begin(write=True) as dst_txn:
                # process left op
                left_cursor = left_txn.cursor()
                for k_bytes, left_v_bytes in left_cursor:
                    right_v_bytes = right_txn.get(k_bytes)
                    if right_v_bytes is None:
                        if not is_in_place_computing:  # add left-only to new table
                            dst_txn.put(k_bytes, left_v_bytes)
                    else:
                        left_v = deserialize(left_v_bytes)
                        right_v = deserialize(right_v_bytes)
                        final_v = _func(left_v, right_v)
                        dst_txn.put(k_bytes, serialize(final_v))
                left_cursor.close()

                # process right op
                right_cursor = right_txn.cursor()
                for k_bytes, right_v_bytes in right_cursor:
                    final_v_bytes = dst_txn.get(k_bytes)
                    if final_v_bytes is None:
                        dst_txn.put(k_bytes, right_v_bytes)
                right_cursor.close()
    return rtn


def do_flat_map(p: _UnaryProcess):
    _func = __get_function(p._info)
    op = p._operand
    rtn = __create_output_operand(op, p._info, p._process_conf, False)
    source_env = op.as_env()
    dst_env = rtn.as_env(write=True)

    with source_env.begin() as source_txn:
        with dst_env.begin(write=True) as dst_txn:
            cursor = source_txn.cursor()
            for k_bytes, v_bytes in cursor:
                k = deserialize(k_bytes)
                v = deserialize(v_bytes)
                map_result = _func(k, v)
                for result_k, result_v in map_result:
                    dst_txn.put(serialize(result_k), serialize(result_v))
            cursor.close()
    return rtn


def __get_in_place_computing_from_task_info(task_info):
    return task_info._is_in_place_computing


def __create_output_operand(src_op, task_info, process_conf, is_in_place_computing_effective):
    if is_in_place_computing_effective:
        if __get_in_place_computing_from_task_info(task_info):
            return src_op

    naming_policy = process_conf._naming_policy
    if naming_policy == 'ITER_AWARE':
        storage_name = DELIMETER.join([src_op._namespace, src_op._name, src_op._type])
        name_ba = bytearray(storage_name.encode())
        name_ba.extend(DELIMETER_ENCODED)
        name_ba.extend(task_info._function_bytes)

        name = hashlib.md5(name_ba).hexdigest()
    else:
        name = task_info._function_id

    from common.python import RuntimeInstance
    from common.python.common.consts import NAMESPACE
    tname = f"{RuntimeInstance.SESSION._session_id}_{name}"
    return _Operand(StoreType.PROCESS.value, NAMESPACE.PROCESS, tname, src_op._partition)


class _DSource(object):

    def __init__(self, _type, namespace, name, partitions=1, in_place_computing=False, db_type=None):
        self._type = _type
        self._namespace = namespace
        self._name = name
        self._partitions = partitions
        self.schema = {}
        self._in_place_computing = in_place_computing
        self.gc_enable = True

        # self.storage_type = self.LMDB
        self.db_type = db_type

        self.storage = None

        if db_type is None or db_type == DBTypes.LMDB:
            from common.python.storage.impl.lmdb_storage import LMDBStorage
            self.storage = LMDBStorage(self._type, self._namespace, self._name, self._partitions,
                                       self._in_place_computing)
        elif db_type == DBTypes.CLICKHOUSE:
            from common.python.storage.impl.clickhouse_storage import ClickHouseStorage
            self.storage = ClickHouseStorage(self._type, self._namespace, self._name, self._partitions,
                                             self._in_place_computing)
        elif db_type == DBTypes.SQLITE:
            from common.python.storage.impl.sqlite_storage import SqliteStorage
            self.storage = SqliteStorage(self._type, self._namespace, self._name, self._partitions,
                                         self._in_place_computing)

    def __del__(self):
        pass

    def __str__(self):
        return "storage_type: {}, namespace: {}, name: {}, " \
               "partitions: {}, in_place_computing: {}".format(self._type,
                                                               self._namespace,
                                                               self._name,
                                                               self._partitions,
                                                               self._in_place_computing)

    '''
    Getter / Setter
    '''

    def get_in_place_computing(self):
        return self._in_place_computing

    def set_in_place_computing(self, is_in_place_computing):
        self._in_place_computing = is_in_place_computing
        return self

    def set_gc_enable(self):
        self.gc_enable = True

    def set_gc_disable(self):
        self.gc_enable = False

    def get_partitions(self):
        return self._partitions

    def get_type(self):
        return self._type

    def get_name(self):
        return self._name

    def get_namespace(self):
        return self._namespace

    def copy(self):
        return self.mapValues(lambda v: v)

    def _get_env_for_partition(self, p: int, write=False):
        return _get_env(self._namespace, self._name, str(p), write=write)

    def put(self, k, v, use_serialize=True):
        return self.storage.put(k, v, use_serialize)

    def count(self):
        return self.storage.count()

    def delete(self, k, use_serialize=True):
        return self.storage.delete(k, use_serialize)

    def put_if_absent(self, k, v, use_serialize=True):
        return self.storage.put_if_absent(k, v, use_serialize)

    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        return self.storage.put_all(kv_list, use_serialize, chunk_size)

    def get(self, k, use_serialize=True):
        return self.storage.get(k, use_serialize)

    def destroy(self):
        return self.storage.destroy()

    def collect(self, min_chunk_size=0, use_serialize=True):
        return self.storage.collect(min_chunk_size, use_serialize)

    def save_as(self, name, namespace, partition=None, use_serialize=True,
                persistent=True, persistent_engine=StoreType.PERSISTENCE):
        return self.storage.save_as(name, namespace, partition, use_serialize, persistent=persistent,
                                    persistent_engine=persistent_engine, dsource=self)

    def take(self, n, keysOnly=False, use_serialize=True):
        return self.storage.take(n, keysOnly, use_serialize)

    def first(self, keysOnly=False, use_serialize=True):
        return self.storage.first(keysOnly, use_serialize)

    @staticmethod
    def _serialize_and_hash_func(func):
        pickled_function = f_pickle.dumps(func)
        func_id = str(uuid.uuid1())
        return func_id, pickled_function

    @staticmethod
    def _repartition(dtable, partition_num, repartition_policy=None):
        return dtable.save_as(str(uuid.uuid1()), DBRuntime.get_instance().session_id, partition_num)

    def _submit_to_pool(self, func, _do_func):
        func_id, pickled_function = self._serialize_and_hash_func(func)
        _task_info = _TaskInfo(DBRuntime.get_instance().session_id, func_id, pickled_function,
                               self.get_in_place_computing())
        results = []
        for p in range(self._partitions):
            _op = _Operand(self._type, self._namespace, self._name, p)
            _p_conf = _ProcessConf.get_default()
            _p = _UnaryProcess(_task_info, _op, _p_conf)
            results.append(DBRuntime.get_instance().pool.submit(_do_func, _p))
        return results

    def _submit_map_reduce(self, mapper, reducer, _do_func):
        _task_info = _MapReduceTaskInfo(task_id=DBRuntime.get_instance().session_id,
                                        function_id=str(uuid.uuid1()),
                                        map_function_bytes=f_pickle.dumps(mapper),
                                        reduce_function_bytes=f_pickle.dumps(reducer),
                                        is_in_place_computing=self.get_in_place_computing())
        futures = []
        for p in range(self._partitions):
            _op = _Operand(self._type, self._namespace, self._name, p)
            _p = _MapReduceProcess(_task_info, _op)
            futures.append(DBRuntime.get_instance().pool.submit(_do_func, _p))
        results = [r.result() for r in futures]
        return results

    def insert_gc_table(self, name):
        count = DBRuntime.get_instance().get_storage_session()._gc_table.get(name)
        if count is None:
            count = 0
        DBRuntime.get_instance().get_storage_session()._gc_table.put(name, (count + 1))

    def map(self, func):
        results = self._submit_to_pool(func, do_map)
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def mapValues(self, func):
        results = self._submit_to_pool(func, do_map_values)
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def applyPartitions(self, func):
        results = self._submit_to_pool(func, do_apply_partitions)
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def mapPartitions(self, func):
        results = self._submit_to_pool(func, do_map_partitions)
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def mapReducePartitions(self, mapper, reducer):
        dup = DBRuntime.get_instance().table(
            str(uuid.uuid1()),
            self._namespace,
            self._partitions,
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
            self.applyPartitions(_local_map_reduce).reduce(_dict_reduce).items()
        )
        return dup

    #
    # def mapReducePartitions(self, mapper, reducer):
    #     results = self._submit_map_reduce(mapper, reducer,_do_map_reduce_partitions)
    #     result = results[0]
    #     return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def mapPartitions2(self, func, need_shuffle=True):
        results = self._submit_to_pool(func, do_map_partitions2)
        for r in results:
            result = r.result()
        if need_shuffle:
            _intermediate_result = DBRuntime.get_instance().table(result._name, result._namespace,
                                                                  self._partitions, persistent=False)
            return _intermediate_result.save_as(str(uuid.uuid1()), _intermediate_result._namespace,
                                                partition=_intermediate_result._partitions, persistent=False)
        else:
            return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def reduce(self, func):
        rs = [r.result() for r in self._submit_to_pool(func, do_reduce)]
        rs = [r for r in filter(partial(is_not, None), rs)]
        if len(rs) <= 0:
            return None
        rtn = rs[0]
        for r in rs[1:]:
            rtn = func(rtn, r)
        return rtn

    def glom(self):
        results = self._submit_to_pool(None, do_glom)
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def join(self, other, func):
        _session_id = DBRuntime.get_instance().session_id
        if other._partitions != self._partitions:
            if other.count() > self.count():
                return self.save_as(str(uuid.uuid1()), _session_id, partition=other._partitions).join(other,
                                                                                                      func)
            else:
                return self.join(other.save_as(str(uuid.uuid1()), _session_id, partition=self._partitions),
                                 func)
        func_id, pickled_function = self._serialize_and_hash_func(func)
        _task_info = _TaskInfo(_session_id, func_id, pickled_function, self.get_in_place_computing())
        results = []
        for p in range(self._partitions):
            _left = _Operand(self._type, self._namespace, self._name, p)
            _right = _Operand(other._type, other._namespace, other._name, p)
            _p_conf = _ProcessConf.get_default()
            _p = _BinaryProcess(_task_info, _left, _right, _p_conf)
            results.append(DBRuntime.get_instance().pool.submit(do_join, _p))
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def sample(self, fraction, seed=None):
        results = self._submit_to_pool((fraction, seed), do_sample)
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def subtractByKey(self, other):
        _session_id = DBRuntime.get_instance().session_id
        if other._partitions != self._partitions:
            if other.count() > self.count():
                return self.save_as(str(uuid.uuid1()), _session_id, partition=other._partitions).subtractByKey(other)
            else:
                return self.union(other.save_as(str(uuid.uuid1()), _session_id, partition=self._partitions))
        func_id, pickled_function = self._serialize_and_hash_func(
            self._namespace + '.' + self._name + '-' + other._namespace + '.' + other._name)
        _task_info = _TaskInfo(_session_id, func_id, pickled_function, self.get_in_place_computing())
        results = []
        for p in range(self._partitions):
            _left = _Operand(self._type, self._namespace, self._name, p)
            _right = _Operand(other._type, other._namespace, other._name, p)
            _p_conf = _ProcessConf.get_default()
            _p = _BinaryProcess(_task_info, _left, _right, _p_conf)
            results.append(DBRuntime.get_instance().pool.submit(do_subtract_by_key, _p))
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def filter(self, func):
        results = self._submit_to_pool(func, do_filter)
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def union(self, other, func=lambda v1, v2: v1):
        _session_id = DBRuntime.get_instance().session_id
        if other._partitions != self._partitions:
            if other.count() > self.count():
                return self.save_as(str(uuid.uuid1()), _session_id, partition=other._partitions).union(other,
                                                                                                       func)
            else:
                return self.union(other.save_as(str(uuid.uuid1()), _session_id, partition=self._partitions),
                                  func)
        func_id, pickled_function = self._serialize_and_hash_func(func)
        _task_info = _TaskInfo(_session_id, func_id, pickled_function, self.get_in_place_computing())
        results = []
        for p in range(self._partitions):
            _left = _Operand(self._type, self._namespace, self._name, p)
            _right = _Operand(other._type, other._namespace, other._name, p)
            _p_conf = _ProcessConf.get_default()
            _p = _BinaryProcess(_task_info, _left, _right, _p_conf)
            results.append(DBRuntime.get_instance().pool.submit(do_union, _p))
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)

    def flatMap(self, func):
        results = self._submit_to_pool(func, do_flat_map)
        for r in results:
            result = r.result()
        return DBRuntime.get_instance().table(result._name, result._namespace, self._partitions, persistent=False)
