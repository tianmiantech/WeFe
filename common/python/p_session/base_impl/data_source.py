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


from collections import Iterable

from common.python.storage import StoreType
from common.python.utils.store_type import DBTypes


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
        elif db_type == DBTypes.LOCAL_FS:
            from common.python.storage.impl.lfs_storage import LfsStorage
            self.storage = LfsStorage(self._namespace, self._name, self._partitions,
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

    # def _get_env_for_partition(self, p: int, write=False):
    #     return _get_env(self._namespace, self._name, str(p), write=write)
    #
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

    def collect(self, min_chunk_size=0, use_serialize=True, partition=None):
        return self.storage.collect(min_chunk_size, use_serialize, partition=partition)

    def save_as(self, name, namespace, partition=None, use_serialize=True,
                persistent=True, persistent_engine=StoreType.PERSISTENCE):
        return self.storage.save_as(name, namespace, partition, use_serialize, persistent=persistent,
                                    persistent_engine=persistent_engine, dsource=self)

    def take(self, n, keysOnly=False, use_serialize=True):
        return self.storage.take(n, keysOnly, use_serialize)

    def first(self, keysOnly=False, use_serialize=True):
        return self.storage.first(keysOnly, use_serialize)

    # @staticmethod
    # def _repartition(dtable, partition_num, repartition_policy=None):
    #     return dtable.save_as(str(uuid.uuid1()), DBRuntime.get_instance().session_id, partition_num)
    #
    #
    # def insert_gc_table(self, name):
    #     count = DBRuntime.get_instance().get_storage_session()._gc_table.get(name)
    #     if count is None:
    #         count = 0
    #     DBRuntime.get_instance().get_storage_session()._gc_table.put(name, (count + 1))

    def map(self, func):
        from common.python.calculation.local.local_computing import map
        return map(self, func)

    def mapValues(self, func):
        from common.python.calculation.local.local_computing import map_value
        return map_value(self, func)

    def applyPartitions(self, func):
        from common.python.calculation.local.local_computing import apply_partitions
        return apply_partitions(self, func)

    def mapPartitions(self, func):
        from common.python.calculation.local.local_computing import map_partitions
        return map_partitions(self, func)

    def mapReducePartitions(self, mapper, reducer):
        from common.python.calculation.local.local_computing import map_reduce_partitions
        return map_reduce_partitions(self, mapper, reducer)

    def mapPartitions2(self, func, need_shuffle=True):
        from common.python.calculation.local.local_computing import map_partition2
        return map_partition2(self, func)

    def reduce(self, func):
        from common.python.calculation.local.local_computing import reduce
        return reduce(self, func)

    def glom(self):
        from common.python.calculation.local.local_computing import glom
        return glom(self)

    def join(self, other, func):
        from common.python.calculation.local.local_computing import join
        return join(self, other, func)

    def sample(self, fraction, seed=None):
        from common.python.calculation.local.local_computing import sample
        return sample(self, fraction, seed)

    def subtractByKey(self, other):
        from common.python.calculation.local.local_computing import subtract_by_key
        return subtract_by_key(self, other)

    def filter(self, func):
        from common.python.calculation.local.local_computing import filter_
        return filter_(self, func)

    def union(self, other, func=lambda v1, v2: v1):
        from common.python.calculation.local.local_computing import union
        return union(self, other, func)

    def flatMap(self, func):
        from common.python.calculation.local.local_computing import flat_map
        return flat_map(self, func)
