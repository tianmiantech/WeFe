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

import abc
import hashlib
from collections import Iterable

import six

from common.python.utils.core_utils import string_to_bytes, serialize
from common.python.utils.store_type import DBTypes


@six.add_metaclass(abc.ABCMeta)
class Storage(object):
    LMDB = DBTypes.LMDB
    CLICKHOUSE = DBTypes.CLICKHOUSE

    storage_type = None

    def __init__(self):
        pass

    @abc.abstractmethod
    def put(self, k, v, use_serialize=True):
        """
        Stores a key-value record.

        Parameters
        ----------
        k : Key object
          Will be serialized. Must be less than 512 bytes.
        v : object
          Will be serialized. Must be less than 32 MB
        use_serialize : bool, defaults True

        Examples
        --------
        >>> from common.python import session
        >>> a = session.parallelize(range(10))
        >>> a.put('hello', 'world')
        >>> b = a.collect()
        >>> list(b)
        [(0, 0), (1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8), (9, 9), ('hello', 'world')]
        """
        pass

    @abc.abstractmethod
    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        """
        Puts (key, value) 2-tuple stream from the iterable items.

        Elements must be exact 2-tuples, they may not be of any other type, or tuple subclass.

        Parameters
        ----------
        kv_list : Iterable
          Key-Value 2-tuple iterable. Will be serialized.

        Notes
        -----
        Each key must be less than 512 bytes, value must be less than 32 MB(implementation depends).

        Examples
        --------
        >>> a = session.table('foo', 'bar')
        >>> t = [(1, 2), (3, 4), (5, 6)]
        >>> a.put_all(t)
        >>> list(a.collect())
        [(1, 2), (3, 4), (5, 6)]
        """
        pass

    @abc.abstractmethod
    def put_if_absent(self, k, v, use_serialize=True):
        """
        Stores a key-value record only if the key is not set.

        Parameters
        ----------
        k : key object
          Will be serialized. Must be less than 512 bytes.
        v : Value object
          Will be serialized. Must be less than 32 MB (or 2G in eggroll 2.x, depends on implements)

        Examples
        -------
        >>> a = sessiojn.parallelize(range(10))
        >>> a.put_if_absent(1, 2)
        >>> b = a.collect()
        >>> list(b)
        [(0, 0), (1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8), (9, 9)]
        >>> a.put_if_absent(-1, -1)
        >>> list(b)
        """
        pass

    @abc.abstractmethod
    def get(self, k, use_serialize=True, maybe_large_value=False):
        """
        Fetches the value matching key.

        Parameters
        ----------
        k : key object
          Will be serialized

        Notes
        -----
        key size Must be less than 512 bytes.

        Returns
        -------
        object
           Corresponding value of the key. Returns None if key does not exist.

        Examples
        --------
        >>> a = session.parallelize(range(10))
        >>> a.get(1)
        (1, 1)
        """
        pass

    @abc.abstractmethod
    def collect(self, min_chunk_size=0, use_serialize=True) -> list:
        """
        Returns an iterator of (key, value) 2-tuple from the Table.

        Parameters
        ---------
        min_chunk_size : int
          Minimum chunk size (key bytes + value bytes) returned if end of table is not hit.
          0 indicates a default chunk size (partition_num * 1.75 MB)
          negative number indicates no chunk limit, i.e. returning all records.
          Default chunk size is recommended if there is no special needs from user.

        Returns
        -------
        Iterator

        Examples
        --------
        >>> a = session.parallelize(range(10))
        >>> b = a.collect(min_chunk_size=1000)
        >>>list(b)
        [(0, 0), (1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8), (9, 9)]
        """
        pass

    @abc.abstractmethod
    def delete(self, k, use_serialize=True):
        """
        Returns the deleted value corresponding to the key.

        Parameters
        ----------
          k : object
            Key object. Will be serialized. Must be less than 512 bytes.
        Returns
        -------
        object
          Corresponding value of the deleted key. Returns None if key does not exist.

        Examples
        --------
        >>> a = session.parallelize(range(10))
        >>> a.delete(1)
        1
        """
        pass

    @abc.abstractmethod
    def destroy(self):
        """
        Destroys this Table, freeing its associated storage resources.

        Returns
        -------
        None

        Examples
        ----------
        >>> a = session.parallelize(range(10))
        >>> a.destroy()
        """
        pass

    @abc.abstractmethod
    def count(self):
        """
        Returns the number of elements in the Table.

        Returns
        -------
        int
          Number of elements in this Table.

        Examples
        --------
        >>> a = session.parallelize(range(10))
        >>> a.count()
        10
        """
        pass

    @abc.abstractmethod
    def take(self, n=1, keysOnly=False, use_serialize=True):
        """
        Returns the first n element(s) of a Table.

        Parameters
        ----------
        n : int
          Number of top data returned.
        keysOnly : bool
          Whether to return keys only. `True` returns keys only and `False` returns both keys and values.

        Returns
        -------
        list
          Lists of top n keys or key-value pairs.

        Examples
        --------
        >>> a = session.parallelize([1, 2, 3])
        >>> a.take(2)
        [(1, 1), (2, 2)]
        >>> a.take(2, keysOnly=True)
        [1, 2]
        """
        pass

    @abc.abstractmethod
    def first(self, keysOnly=False, use_serialize=True):
        """
        Returns the first element of a Table. Shortcut of `take(1, keysOnly)`

        Parameters
        ----------
        keysOnly : bool
          Whether to return keys only. `True` returns keys only and `False` returns both keys and values.
        use_serialize : bool

        Returns
        -------
        tuple or object
          First element of the Table. It is a tuple if `keysOnly=False`, or an object if `keysOnly=True`.

        Examples
        --------
        >>> a = session.parallelize([1, 2, 3])
        >>> a.first()
        (1, 1)
        """
        pass

    @abc.abstractmethod
    def save_as(self, name, namespace, partition=None, use_serialize=True, **kwargs):
        """
        Transforms a temporary table to a persistent table.

        Parameters
        ----------
        name : string
          Table name of result Table.
        namespace: string
          Table namespace of result Table.
        partition : int
          Number of partition for the new persistent table.
        use_serialize

        Returns
        -------
        Table
           Result persistent Table.

        Examples
        --------
        >>> a = session.parallelize(range(10))
        >>> b = a.save_as('foo', 'bar', partition=2)
        """
        pass

    @staticmethod
    def kv_to_bytes(**kwargs):
        use_serialize = kwargs.get("use_serialize", True)
        # can not use is None
        if "k" in kwargs and "v" in kwargs:
            k, v = kwargs["k"], kwargs["v"]
            return (serialize(k), serialize(v)) if use_serialize \
                else (string_to_bytes(k), string_to_bytes(v))
        elif "k" in kwargs:
            k = kwargs["k"]
            return serialize(k) if use_serialize else string_to_bytes(k)
        elif "v" in kwargs:
            v = kwargs["v"]
            return serialize(v) if use_serialize else string_to_bytes(v)

    @staticmethod
    def hash_key_to_partition(key, partitions):
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
