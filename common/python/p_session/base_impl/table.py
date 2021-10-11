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
from typing import Iterable

from common.python.table import Table
from common.python.utils.profile_util import log_elapsed
from common.python.utils.split import split_put, split_get


# noinspection SpellCheckingInspection,PyProtectedMember,PyPep8Naming
class DSource(Table):

    def __init__(self, dsource, session_id):
        self._dsource = dsource
        self._partitions = self._dsource._partitions
        self.schema = {}
        self._name = self._dsource._name or str(uuid.uuid1())
        self._namespace = self._dsource._namespace
        self._session_id = session_id

    @classmethod
    def from_dsource(cls, session_id, dsource):
        return DSource(dsource=dsource, session_id=session_id)

    def get_name(self):
        return self._name

    def get_namespace(self):
        return self._namespace

    def dsource(self):
        return self._dsource

    @log_elapsed
    def save_as(self, name, namespace, partition=None, use_serialize=True, **kwargs):
        if partition is None:
            partition = self._partitions
        from common.python import RuntimeInstance
        persistent_engine = RuntimeInstance.SESSION.get_persistent_engine()
        saved_table = self._dsource.save_as(name=name,
                                            namespace=namespace,
                                            partition=partition,
                                            use_serialize=use_serialize,
                                            persistent_engine=persistent_engine)
        return self.from_dsource(self._session_id, saved_table)

    def put(self, k, v, use_serialize=True, maybe_large_value=False):
        if not maybe_large_value:
            return self._dsource.put(k=k, v=v, use_serialize=use_serialize)
        else:
            return split_put(k, v, use_serialize=use_serialize, put_call_back_func=self._dsource.put)

    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        return self._dsource.put_all(kv_list=kv_list, use_serialize=use_serialize, chunk_size=chunk_size)

    def get(self, k, use_serialize=True, maybe_large_value=False):
        if not maybe_large_value:
            return self._dsource.get(k, use_serialize)
        else:
            return split_get(k=k, use_serialize=use_serialize, get_call_back_func=self._dsource.get)

    @log_elapsed
    def collect(self, min_chunk_size=0, use_serialize=True, **kwargs) -> list:
        return self._dsource.collect(min_chunk_size=min_chunk_size, use_serialize=use_serialize)

    def delete(self, k, use_serialize=True):
        return self._dsource.delete(k=k, use_serialize=use_serialize)

    def destroy(self):
        return self._dsource.destroy()

    @log_elapsed
    def count(self, **kwargs):
        return self._dsource.count()

    def put_if_absent(self, k, v, use_serialize=True):
        return self._dsource.put_if_absent(k=k, v=v, use_serialize=use_serialize)

    def take(self, n=1, keysOnly=False, use_serialize=True):
        return self._dsource.take(n=n, keysOnly=keysOnly, use_serialize=use_serialize)

    def first(self, keysOnly=False, use_serialize=True):
        return self._dsource.first(keysOnly=keysOnly, use_serialize=use_serialize)

    # noinspection PyProtectedMember
    def get_partitions(self):
        return self._dsource._partitions

    """
    computing apis
    """

    @log_elapsed
    def map(self, func, **kwargs):
        return DSource(self._dsource.map(func), session_id=self._session_id)

    @log_elapsed
    def mapValues(self, func, **kwargs):
        return DSource(self._dsource.mapValues(func), session_id=self._session_id)

    @log_elapsed
    def applyPartitions(self, func):
        return DSource(self._dsource.applyPartitions(func), session_id=self._session_id)

    @log_elapsed
    def mapPartitions(self, func, **kwargs):
        return DSource(self._dsource.mapPartitions(func), session_id=self._session_id)

    @log_elapsed
    def mapReducePartitions(self, mapper, reducer, **kwargs):
        return DSource(self._dsource.mapReducePartitions(mapper, reducer), session_id=self._session_id)

    @log_elapsed
    def mapPartitions2(self, func, **kwargs):
        return DSource(self._dsource.mapPartitions2(func), session_id=self._session_id)

    @log_elapsed
    def reduce(self, func, key_func=None, **kwargs):
        if key_func is None:
            return self._dsource.reduce(func)

        it = self._dsource.collect()
        ret = {}
        for k, v in it:
            agg_key = key_func(k)
            if agg_key in ret:
                ret[agg_key] = func(ret[agg_key], v)
            else:
                ret[agg_key] = v
        return ret

    @log_elapsed
    def join(self, other, func, **kwargs):
        return DSource(self._dsource.join(other._dsource, func=func), session_id=self._session_id)

    @log_elapsed
    def glom(self, **kwargs):
        return DSource(self._dsource.glom(), session_id=self._session_id)

    @log_elapsed
    def sample(self, fraction, seed=None, **kwargs):
        return DSource(self._dsource.sample(fraction=fraction, seed=seed), session_id=self._session_id)

    @log_elapsed
    def subtractByKey(self, other, **kwargs):
        return DSource(self._dsource.subtractByKey(other._dsource), session_id=self._session_id)

    @log_elapsed
    def filter(self, func, **kwargs):
        return DSource(self._dsource.filter(func), session_id=self._session_id)

    @log_elapsed
    def union(self, other, func=lambda v1, v2: v1, **kwargs):
        return DSource(self._dsource.union(other._dsource, func=func), session_id=self._session_id)

    @log_elapsed
    def flatMap(self, func, **kwargs):
        _temp_table = self.from_dsource(self._session_id, self._dsource.flatMap(func))
        return _temp_table.save_as(name=f"{_temp_table._name}.save_as", namespace=_temp_table._namespace)
