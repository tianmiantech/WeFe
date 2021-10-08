# -*- coding: utf-8 -*-

# Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import hashlib
import os
import shutil
from collections import Iterable
from heapq import heapify, heappop, heapreplace

from cachetools import cached

from common.python.storage.impl.dsource import DBRuntime
from common.python.storage.storage import Storage
from common.python.utils import cache_utils
from common.python.utils.core_utils import bytes_to_string, deserialize


def _evict(_, env):
    env.close()


@cached(cache=cache_utils.EvictLRUCache(maxsize=256, evict=_evict))
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


class LMDBStorage(Storage):

    def __init__(self, _type, namespace, name, partitions=1, in_place_computing=False):
        self._type = _type
        self._namespace = namespace
        self._name = name
        self._partitions = partitions
        self.schema = {}
        self._in_place_computing = in_place_computing

    def _get_env_for_partition(self, p: int, write=False):
        return _get_env(self._namespace, self._name, str(p), write=write)
        # return _get_env(self._type, self._namespace, self._name, str(p), write=write)

    @staticmethod
    def _merge(cursors, use_serialize=True):
        ''' Merge sorted iterators. '''
        entries = []
        for _id, it in enumerate(cursors):
            if it.next():
                key, value = it.item()
                entries.append([key, value, _id, it])
            else:
                it.close()
        heapify(entries)
        while entries:
            key, value, _, it = entry = entries[0]
            if use_serialize:
                yield deserialize(key), deserialize(value)
            else:
                yield bytes_to_string(key), value
            if it.next():
                entry[0], entry[1] = it.item()
                heapreplace(entries, entry)
            else:
                _, _, _, it = heappop(entries)
                it.close()

    def put(self, k, v, use_serialize=True):
        k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v, use_serialize=use_serialize)
        p = _hash_key_to_partition(k_bytes, self._partitions)
        env = self._get_env_for_partition(p, write=True)
        with env.begin(write=True) as txn:
            return txn.put(k_bytes, v_bytes)
        return False

    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        txn_map = {}
        _succ = True
        for p in range(self._partitions):
            env = self._get_env_for_partition(p, write=True)
            txn = env.begin(write=True)
            txn_map[p] = env, txn
        for k, v in kv_list:
            try:
                k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v, use_serialize=use_serialize)
                p = _hash_key_to_partition(k_bytes, self._partitions)
                _succ = _succ and txn_map[p][1].put(k_bytes, v_bytes)
            except Exception as e:
                print(e)
                _succ = False
                break
        for p, (env, txn) in txn_map.items():
            txn.commit() if _succ else txn.abort()

    def put_if_absent(self, k, v, use_serialize=True):
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        p = _hash_key_to_partition(k_bytes, self._partitions)
        env = self._get_env_for_partition(p, write=True)
        with env.begin(write=True) as txn:
            old_value_bytes = txn.get(k_bytes)
            if old_value_bytes is None:
                v_bytes = self.kv_to_bytes(v=v, use_serialize=use_serialize)
                txn.put(k_bytes, v_bytes)
                return None
            return deserialize(old_value_bytes) if use_serialize else old_value_bytes

    def get(self, k, use_serialize=True, maybe_large_value=False):
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        p = _hash_key_to_partition(k_bytes, self._partitions)
        env = self._get_env_for_partition(p)
        with env.begin(write=True) as txn:
            old_value_bytes = txn.get(k_bytes)
            return None if old_value_bytes is None else (
                deserialize(old_value_bytes) if use_serialize else old_value_bytes)

    def collect(self, min_chunk_size=0, use_serialize=True) -> list:
        iterators = []
        for p in range(self._partitions):
            env = self._get_env_for_partition(p)
            txn = env.begin()
            iterators.append(txn.cursor())
        return self._merge(iterators, use_serialize)

    def delete(self, k, use_serialize=True):
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        p = _hash_key_to_partition(k_bytes, self._partitions)
        env = self._get_env_for_partition(p, write=True)
        with env.begin(write=True) as txn:
            old_value_bytes = txn.get(k_bytes)
            if txn.delete(k_bytes):
                return None if old_value_bytes is None else (
                    deserialize(old_value_bytes) if use_serialize else old_value_bytes)
            return None

    def destroy(self):
        for p in range(self._partitions):
            env = self._get_env_for_partition(p, write=True)
            db = env.open_db()
            with env.begin(write=True) as txn:
                txn.drop(db)
        _table_key = ".".join([self._type, self._namespace, self._name])
        from common.python.storage.impl.dsource import DBRuntime
        DBRuntime.get_instance().meta_table.delete(_table_key)
        _path = _get_db_path(self._type, self._namespace, self._name)
        shutil.rmtree(_path, ignore_errors=True)

    def count(self):
        cnt = 0
        for p in range(self._partitions):
            env = self._get_env_for_partition(p)
            cnt += env.stat()['entries']
        return cnt

    def take(self, n=1, keysOnly=False, use_serialize=True):
        if n <= 0:
            n = 1
        it = self.collect(use_serialize=use_serialize)
        rtn = list()
        i = 0
        for item in it:
            if keysOnly:
                rtn.append(item[0])
            else:
                rtn.append(item)
            i += 1
            if i == n:
                break
        return rtn

    def first(self, keysOnly=False, use_serialize=True):
        resp = self.take(1, keysOnly=keysOnly, use_serialize=use_serialize)
        if resp:
            return resp[0]
        else:
            return None

    def save_as(self, name, namespace, partition=None, use_serialize=True, **kwargs):
        if partition is None:
            partition = self._partitions

        persistent = kwargs["persistent"]
        persistent_engine = kwargs["persistent_engine"]
        dsource = kwargs["dsource"]

        dsource.set_gc_disable()

        from common.python.storage.impl.dsource import DBRuntime
        dup = DBRuntime.get_instance().table(name, namespace, partition,
                                             persistent=persistent, persistent_engine=persistent_engine)

        data = dsource.collect(use_serialize=use_serialize)
        dup.put_all(data, use_serialize=use_serialize)

        dsource.set_gc_enable()
        return dup


if __name__ == '__main__':
    storage = LMDBStorage("LMDB", "inner_test", "test", 1)
    print(storage.put("k", "v"))
    print(storage.get("k"))

    print(storage.put_all(iter([("k1", "v1"), ("k2", "v2")])))
    print(storage.collect())
    print(list(storage.collect()))

    # print(storage.delete("k"))
    print(storage.count())

    print(storage.take(1))
