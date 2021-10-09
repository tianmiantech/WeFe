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

import os
import sqlite3
from collections import Iterable

from common.python.storage.storage import Storage
from common.python.utils import file_utils
from common.python.utils.core_utils import bytes_to_string, deserialize


def _get_data_dir():
    data_dir = os.path.join(file_utils.get_project_base_directory(), 'data/sqlite')
    return data_dir


class SqliteStorage(Storage):

    def __init__(self, _type, namespace, name, partitions=1, in_place_computing=False):
        self._type = _type
        self._namespace = namespace
        self._name = name
        self._partitions = partitions
        self.schema = {}
        self._in_place_computing = in_place_computing

        self.init_dir()
        self.init_db()

    def init_dir(self):
        data_dir = _get_data_dir()
        if not os.path.exists(data_dir):
            os.makedirs(data_dir)

    def get_db_file(self):
        return _get_data_dir() + "/" + self._namespace + ".db"

    def get_conn(self):
        db_file = self.get_db_file()
        return sqlite3.connect(db_file)

    def init_db(self):
        sql = f"CREATE TABLE IF NOT EXISTS '{self._name}' (id BLOB PRIMARY KEY,value BLOB)"
        self.exec_sql(sql)

    def exec(self, sql, param=None):
        conn = self.get_conn()
        cursor = conn.cursor()
        if param:
            cursor.execute(sql, param)
        else:
            cursor.execute(sql)
        return conn, cursor

    def exec_sql(self, sql, param=None):
        conn, cursor = self.exec(sql, param)
        result = cursor.rowcount
        cursor.close()
        conn.commit()
        conn.close()
        return result

    def exec_query_one(self, sql, param=None):
        conn, cursor = self.exec(sql, param)
        data = cursor.fetchone()
        cursor.close()
        conn.close()
        return data

    def put(self, k, v, use_serialize=True):
        k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v, use_serialize=use_serialize)
        sql = f"INSERT OR REPLACE INTO '{self._name}' (id,value) VALUES(?,?)"
        return True if self.exec_sql(sql, (k_bytes, v_bytes)) else False

    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        conn = self.get_conn()
        cursor = conn.cursor()
        cursor.execute("BEGIN TRANSACTION")
        sql = f"INSERT OR REPLACE INTO '{self._name}' (id,value) VALUES(?,?)"
        for k, v in kv_list:
            k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v, use_serialize=use_serialize)
            cursor.execute(sql, (k_bytes, v_bytes))
        cursor.execute("COMMIT")
        cursor.close()
        conn.commit()
        conn.close()

    def put_if_absent(self, k, v, use_serialize=True):
        exist_v = self.get(k, use_serialize=use_serialize)
        if not exist_v:
            self.put(k, v, use_serialize)

    def get(self, k, use_serialize=True, maybe_large_value=False):
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        sql = f"SELECT value FROM '{self._name}' WHERE id=?"
        old_value_bytes = self.exec_query_one(sql, (k_bytes,))
        old_value_bytes = old_value_bytes[0] if old_value_bytes is not None else old_value_bytes
        return None if old_value_bytes is None else (
            deserialize(old_value_bytes) if use_serialize else old_value_bytes)

    def collect(self, min_chunk_size=0, use_serialize=True) -> list:
        sql = f"SELECT id,value FROM '{self._name}'"
        conn, cursor = self.exec(sql, None)
        data = cursor.fetchall()
        for item in data:
            key, value = item[0], item[1]
            if use_serialize:
                yield deserialize(key), deserialize(value)
            else:
                yield bytes_to_string(key), value

    def delete(self, k, use_serialize=True):
        value = self.get(k)
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        sql = f"DELETE FROM '{self._name}' WHERE id=?"
        self.exec_sql(sql, (k_bytes,))
        return value

    def destroy(self):
        sql = f"DROP TABLE '{self._name}'"
        self.exec_sql(sql)
        print(f"destory :{sql}")

        _table_key = ".".join([self._type, self._namespace, self._name])
        from common.python.storage.impl.dsource import DBRuntime
        DBRuntime.get_instance().meta_table.delete(_table_key)

    def count(self):
        sql = f"SELECT count(*) FROM '{self._name}'"
        old_value_bytes = self.exec_query_one(sql)
        old_value_bytes = old_value_bytes[0] if old_value_bytes is not None else old_value_bytes
        return old_value_bytes

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
        dup.put_all(iter(list(data)), use_serialize=use_serialize)

        dsource.set_gc_enable()
        return dup


if __name__ == '__main__':
    # storage = SqliteStorage("", "inner_test", "test")
    # print(storage.put("k", "v"))
    # print(storage.get("k"))
    #
    # print(storage.put_all(iter([("k1", "v1"), ("k2", "v2")])))
    # print(storage.collect())
    # print(list(storage.collect()))
    #
    # # print(storage.delete("k"))
    # print(storage.count())
    # print(storage.take(1))

    storage = SqliteStorage("", "__META__", "fragments")
    data_list = storage.collect()
    for item in data_list:
        print(item)

    # test2_db = storage.save_as("inner_test", "test2", 1)
