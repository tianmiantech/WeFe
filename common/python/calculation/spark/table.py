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
from typing import Iterable

from common.python.calculation.spark import util
from common.python.common import consts
from common.python.common.consts import NAMESPACE
from common.python.table import Table
from common.python.utils import conf_utils
from common.python.utils.profile_util import log_elapsed
from common.python.utils.split import split_put, split_get


class RDDSource(Table):

    # noinspection PyProtectedMember
    @classmethod
    def from_dsource(cls, session_id: str, dsource):
        namespace = dsource._namespace
        name = dsource._name
        partitions = dsource._partitions
        return RDDSource(session_id=session_id, namespace=namespace, name=name, partitions=partitions, dsource=dsource)

    @classmethod
    def from_rdd(cls, rdd, job_id: str, namespace: str, name: str):
        partitions = rdd.getNumPartitions()
        return RDDSource(session_id=job_id, namespace=namespace, name=name, partitions=partitions, rdd=rdd)

    def __init__(self, session_id: str,
                 namespace: str,
                 name: str = None,
                 partitions: int = 1,
                 rdd=None,
                 dsource=None):

        self._valid_param_check(rdd, dsource, namespace, partitions)
        setattr(self, util.RDD_ATTR_NAME, rdd)
        self._rdd = rdd
        self._partitions = partitions
        self._dsource = dsource
        self.schema = {}
        self._name = name or str(uuid.uuid1())
        self._namespace = namespace
        self._session_id = session_id

    def get_name(self):
        return self._name

    def get_namespace(self):
        return self._namespace

    def __str__(self):
        return f"{self._namespace}, {self._name}, {self._dsource}"

    def __repr__(self):
        return f"{self._namespace}, {self._name}, {self._dsource}"

    def _tmp_table_from_rdd(self, rdd, name=None):
        """
        tmp table, with namespace == job_id
        """
        rdd = util.materialize(rdd)
        name = name or f"{self._session_id}_{str(uuid.uuid1())}"
        return RDDSource(session_id=self._session_id,
                         # namespace=self._namespace,
                         namespace=NAMESPACE.PROCESS,
                         name=name,
                         partitions=rdd.getNumPartitions(),
                         rdd=rdd,
                         dsource=None)

    # self._rdd should not be pickled(spark requires all transformer/action to be invoked in driver).
    def __getstate__(self):
        state = dict(self.__dict__)
        if "_rdd" in state:
            del state["_rdd"]
        return state

    @staticmethod
    def _valid_param_check(rdd, dtable, namespace, partitions):
        assert (rdd is not None) or (dtable is not None), "params rdd and storage are both None"
        assert namespace is not None, "namespace is None"
        assert partitions > 0, "invalid partitions={0}".format(partitions)

    def rdd(self):
        if hasattr(self, "_rdd") and self._rdd is not None:
            return self._rdd

        if self._dsource is None:
            raise AssertionError("try create rdd from None storage")

        return self._rdd_from_dtable()

    # noinspection PyProtectedMember,PyUnresolvedReferences
    @log_elapsed
    def _rdd_from_dtable(self):
        storage_iterator = self._dsource.collect(use_serialize=True)
        if self._dsource.count() <= 0:
            storage_iterator = []

        num_partition = self._dsource._partitions

        # If the system forces to specify the number of shards, use the specified number
        num_slices = conf_utils.get_comm_config(consts.COMM_CONF_KEY_SPARK_NUM_SLICES)
        num_partition = int(num_slices) if num_slices else num_partition

        from pyspark import SparkContext
        self._rdd = SparkContext.getOrCreate() \
            .parallelize(storage_iterator, num_partition) \
            .persist(util.get_storage_level())
        return self._rdd

    def dsource(self):
        """
        rdd -> storage
        """
        if self._dsource:
            return self._dsource
        else:
            if not hasattr(self, "_rdd") or self._rdd is None:
                raise AssertionError("try create dtable from None")
            return self._rdd_to_dtable()

    # noinspection PyUnusedLocal
    @log_elapsed
    def _rdd_to_dtable(self, **kwargs):
        self._dsource = self.save_as(name=self._name,
                                     namespace=self._namespace,
                                     partition=self._partitions,
                                     persistent=False)._dsource
        return self._dsource

    def get_partitions(self):
        return self._partitions

    @log_elapsed
    def map(self, func, **kwargs):
        from common.python.calculation.spark.rdd_func import _map
        rtn_rdd = _map(self.rdd(), func)
        return self._tmp_table_from_rdd(rtn_rdd)

    @log_elapsed
    def mapValues(self, func, **kwargs):
        from common.python.calculation.spark.rdd_func import _map_value
        rtn_rdd = _map_value(self.rdd(), func)
        return self._tmp_table_from_rdd(rtn_rdd)

    @log_elapsed
    def mapPartitions(self, func, **kwargs):
        from common.python.calculation.spark.rdd_func import _map_partitions
        rtn_rdd = _map_partitions(self.rdd(), func)
        return self._tmp_table_from_rdd(rtn_rdd)

    @log_elapsed
    def mapPartitions2(self, func, **kwargs):
        return self._tmp_table_from_rdd(self.rdd().mapPartitions(func))

    @log_elapsed
    def mapReducePartitions(self, mapper, reducer, **kwargs):
        return self._tmp_table_from_rdd(self.rdd().mapPartitions(mapper).reduceByKey(reducer))

    @log_elapsed
    def applyPartitions(self, func, **kwargs):
        return self.mapPartitions(func)

    @log_elapsed
    def reduce(self, func, key_func=None, **kwargs):
        if key_func is None:
            return self.rdd().values().reduce(func)

        return dict(self.rdd().map(lambda x: (key_func(x[0]), x[1])).reduceByKey(func).collect())

    def join(self, other, func=None, **kwargs):
        rdd1 = self.rdd()
        rdd2 = other.rdd()

        # noinspection PyUnusedLocal,PyShadowingNames
        @log_elapsed
        def _join(rdda, rddb, **kwargs):
            from common.python.calculation.spark.rdd_func import _join
            return self._tmp_table_from_rdd(_join(rdda, rddb, func))

        return _join(rdd1, rdd2, **kwargs)

    @log_elapsed
    def glom(self, **kwargs):
        from common.python.calculation.spark.rdd_func import _glom
        return self._tmp_table_from_rdd(_glom(self.rdd()))

    @log_elapsed
    def sample(self, fraction, seed=None, **kwargs):
        from common.python.calculation.spark.rdd_func import _sample
        return self._tmp_table_from_rdd(_sample(self.rdd(), fraction, seed))

    @log_elapsed
    def subtractByKey(self, other, **kwargs):
        from common.python.calculation.spark.rdd_func import _subtract_by_key
        return self._tmp_table_from_rdd(_subtract_by_key(self.rdd(), other.rdd()))

    @log_elapsed
    def filter(self, func, **kwargs):
        from common.python.calculation.spark.rdd_func import _filter
        return self._tmp_table_from_rdd(_filter(self.rdd(), func))

    @log_elapsed
    def union(self, other, func=lambda v1, v2: v1, **kwargs):
        from common.python.calculation.spark.rdd_func import _union
        return self._tmp_table_from_rdd(_union(self.rdd(), other.rdd(), func))

    @log_elapsed
    def flatMap(self, func, **kwargs):
        from common.python.calculation.spark.rdd_func import _flat_map
        return self._tmp_table_from_rdd(_flat_map(self.rdd(), func))

    @log_elapsed
    def collect(self, min_chunk_size=0, use_serialize=True, **kwargs):
        if self._dsource:
            return self._dsource.collect(min_chunk_size, use_serialize)
        else:
            return iter(self.rdd().collect())

    """
    storage api
    """

    def put(self, k, v, use_serialize=True, maybe_large_value=False):
        if not maybe_large_value:
            rtn = self.dsource().put(k, v, use_serialize)
        else:
            rtn = split_put(k, v, use_serialize=use_serialize, put_call_back_func=self.dsource().put)
        self._rdd = None
        return rtn

    @log_elapsed
    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        rtn = self.dsource().put_all(kv_list, use_serialize, chunk_size)
        self._rdd = None
        return rtn

    def get(self, k, use_serialize=True, maybe_large_value=False):
        if not maybe_large_value:
            return self.dsource().get(k, use_serialize)
        else:
            return split_get(k=k, use_serialize=use_serialize, get_call_back_func=self.dsource().get)

    def delete(self, k, use_serialize=True):
        rtn = self.dsource().delete(k, use_serialize)
        self._rdd = None
        return rtn

    def destroy(self):
        if self._dsource:
            self._dsource.destroy()
        else:
            self._rdd = None
        return True

    def put_if_absent(self, k, v, use_serialize=True):
        rtn = self.dsource().put_if_absent(k, v, use_serialize)
        self._rdd = None
        return rtn

    # noinspection PyPep8Naming
    def take(self, n=1, keysOnly=False, use_serialize=True):
        if self._dsource:
            return self._dsource.take(n, keysOnly, use_serialize)
        else:
            rtn = self._rdd.take(n)
            if keysOnly:
                rtn = [pair[0] for pair in rtn]
            return rtn

    # noinspection PyPep8Naming
    def first(self, keysOnly=False, use_serialize=True):
        first = self.take(1, keysOnly, use_serialize)
        return first[0] if first else None

    def count(self, **kwargs):
        if self._dsource:
            return self._dsource.count()
        else:
            return self._rdd.count()

    @log_elapsed
    def save_as(self, name, namespace, partition=None, use_serialize=True, persistent=True, **kwargs) -> 'RDDSource':
        if partition is None:
            partition = self._partitions
        partition = partition or self._partitions
        from common.python import RuntimeInstance
        persistent_engine = RuntimeInstance.SESSION.get_persistent_engine()
        if self._dsource:
            _dtable = self._dsource.save_as(name, namespace, partition,
                                            use_serialize=use_serialize,
                                            persistent_engine=persistent_engine)
            return RDDSource.from_dsource(session_id=self._session_id, dsource=_dtable)
        else:
            from common.python.calculation.spark.rdd_func import _save_as_func
            return _save_as_func(self._rdd, name=name, namespace=namespace, partition=partition, persistent=persistent)
