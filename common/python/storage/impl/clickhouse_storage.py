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

import time
import datetime
import uuid
import multiprocessing
from collections.abc import Iterable
from fnmatch import fnmatch

from clickhouse_driver import Client

from common.python.common import consts
from common.python.common.consts import NAMESPACE
from common.python.storage.storage import Storage
from common.python.utils import conf_utils, log_utils
from common.python.utils.core_utils import deserialize
from multiprocessing.pool import Pool

FORCE_SERI = False
LOGGER = log_utils.get_logger()
BATCH_BYTE_SIZE = 4 * 1024 * 1024


def set_force_serialize(in_seri):
    return in_seri if not FORCE_SERI else FORCE_SERI


def get_db_conn(database='default'):
    env_ck_host = conf_utils.get_env_config(consts.ENV_CONF_KEY_CK_HOST)
    env_ck_port = conf_utils.get_env_config(consts.ENV_CONF_KEY_CK_PORT)

    ck_host = conf_utils.get_comm_config(consts.COMM_CONF_KEY_CK_HOST)
    ck_user = conf_utils.get_comm_config(consts.COMM_CONF_KEY_CK_USER)
    ck_pwd = conf_utils.get_comm_config(consts.COMM_CONF_KEY_CK_PWD)
    ck_port = conf_utils.get_comm_config(consts.COMM_CONF_KEY_CK_PORT)

    return Client(host=env_ck_host or ck_host, user=ck_user, password=ck_pwd, database=database,
                  port=env_ck_port or ck_port)


def fun_deserialize(item_batch_data):
    result = []
    for key, value in item_batch_data:
        result.append((deserialize(key), deserialize(value)))
    return result


class ClickHouseStorage(Storage):

    def __init__(self, _type, namespace, name, partitions=1, in_place_computing=False):
        self._type = _type
        self._namespace = namespace
        self._name = name
        self.table_name = f"`{namespace}`.`{name}`"
        self._partitions = partitions
        self.schema = {}
        self._in_place_computing = in_place_computing

        # self.init_db()
        self.init_tb()

    def init_db(self):
        sql = f"CREATE DATABASE IF NOT EXISTS `{self._namespace}`"
        try:
            client = self.get_conn()
            client.execute(sql)
        except Exception as ex:
            LOGGER.error(ex, exc_info=True, stack_info=True)
        finally:
            client.disconnect()

    def init_tb(self):
        sql = f"CREATE TABLE IF NOT EXISTS {self.table_name}(`eventDate` Date, `k` String, `v` String, `id` String)" \
              f"ENGINE = MergeTree() PARTITION BY toYYYYMM(eventDate) ORDER BY tuple() SETTINGS index_granularity = 8192"
        try:
            client = self.get_conn()
            client.execute(sql)
        except Exception as ex:
            LOGGER.error(ex, exc_info=True, stack_info=True)
        finally:
            client.disconnect()

    def get_conn(self):
        return get_db_conn()

    def put(self, k, v, use_serialize=True):
        use_serialize = set_force_serialize(use_serialize)
        k_bytes, v_bytes = self.kv_to_bytes(
            k=k, v=v, use_serialize=use_serialize)
        sql = f"INSERT INTO {self.table_name} (eventDate,k,v,id) values"
        try:
            client = self.get_conn()
            result = client.execute(sql,
                                    [[datetime.datetime.now(), k_bytes, v_bytes, str(uuid.uuid1())]])
        finally:
            client.disconnect()
        return result

    def ck_execute(self, sql, data):
        client = self.get_conn()
        client.execute(sql, data)
        client.disconnect()

    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        use_serialize = set_force_serialize(use_serialize)
        sql = f"INSERT INTO {self.table_name} (eventDate,k,v,id) values"

        now = datetime.datetime.now()
        data = []
        batch_count = None
        max_batch_count = 500000
        use_generator_param = True

        if use_generator_param:

            def get_param_generator(kv_list):
                for k, v in kv_list:
                    k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v, use_serialize=use_serialize)
                    yield now, k_bytes, v_bytes, ''

            self.ck_execute(sql, get_param_generator(kv_list))

        else:
            for k, v in kv_list:
                k_bytes, v_bytes = self.kv_to_bytes(
                    k=k, v=v, use_serialize=use_serialize)
                data.append([now, k_bytes, v_bytes, ''])

                if batch_count is None:
                    row_size = len(k_bytes) + len(v_bytes)
                    batch_count = BATCH_BYTE_SIZE // row_size
                    if batch_count > max_batch_count:
                        batch_count = max_batch_count

                if len(data) == batch_count:
                    self.ck_execute(sql, data)
                    data = []
            if len(data) > 0:
                self.ck_execute(sql, data)

    def put_if_absent(self, k, v, use_serialize=True):
        use_serialize = set_force_serialize(use_serialize)
        exist_v = self.get(k, use_serialize=use_serialize)
        if not exist_v:
            return self.put(k, v, use_serialize)

    def get(self, k, use_serialize=True, maybe_large_value=False):
        use_serialize = set_force_serialize(use_serialize)
        k_hex = self.kv_to_bytes(
            k=k, use_serialize=use_serialize).hex().upper()
        sql = f"SELECT v FROM {self.table_name} WHERE hex(k)=%(a)s ORDER BY id DESC LIMIT 1"
        client = self.get_conn()
        try:
            result = client.execute(sql, {"a": k_hex})
            data = None if len(result) == 0 else result[0][0]
            return None if data is None else (
                deserialize(data) if use_serialize else data)
        finally:
            client.disconnect()

    def collect(self, min_chunk_size=0, use_serialize=True, partition=None) -> Iterable:
        use_serialize = set_force_serialize(use_serialize)
        # sql = f"SELECT k,v FROM {self.table_name} ORDER BY k DESC"

        sql = f"SELECT k,v FROM {self.table_name}"

        use_pool = True if self.count() > 1000000 else False
        use_minibatch = False
        max_pool_size = 10 if multiprocessing.cpu_count() > 10 else multiprocessing.cpu_count()

        try:
            client = self.get_conn()
            data = client.execute_iter(sql)

            if not use_pool:
                for item in data:
                    key, value = item[0], item[1]
                    if use_serialize:
                        yield deserialize(key), deserialize(value)
                    else:
                        yield key, value
            else:
                batch_data = self.generate_batch(data, batch_count=1000)
                pool = Pool(max_pool_size)

                # 采用minibatch的方案，避免读取效率比处理速度快太多，导致内存堆积
                if use_minibatch:
                    mini_batch = []
                    mini_batch_length = 50
                    for item_batch in batch_data:
                        mini_batch.append(item_batch)
                        if len(mini_batch) == mini_batch_length:
                            result = pool.imap_unordered(
                                fun_deserialize, mini_batch)
                            for item_batch_result in result:
                                for k, v in item_batch_result:
                                    yield k, v
                            mini_batch = []

                    if len(mini_batch) > 0:
                        result = pool.imap_unordered(
                            fun_deserialize, mini_batch)
                        for item_batch_result in result:
                            for k, v in item_batch_result:
                                yield k, v
                # 不限制
                else:
                    result = pool.imap_unordered(fun_deserialize, batch_data)
                    for item_batch_result in result:
                        for k, v in item_batch_result:
                            yield k, v

        finally:
            client.disconnect()

    def generate_batch(self, data: Iterable, batch_count=10000):
        batch = []
        for k, v in data:
            batch.append((k, v))
            if len(batch) == batch_count:
                yield batch
                batch = []
        if batch:
            yield batch

    def delete(self, k, use_serialize=True):
        use_serialize = set_force_serialize(use_serialize)
        try:
            client = self.get_conn()
            value = self.get(k)
            k_hex = self.kv_to_bytes(
                k=k, use_serialize=use_serialize).hex().upper()
            sql = f"ALTER TABLE {self.table_name} DELETE WHERE hex(k)=%(a)s"
            client.execute(sql, {"a": k_hex})
        finally:
            client.disconnect()
        return value

    def destroy(self):
        try:
            client = self.get_conn()
            sql = f"DROP TABLE {self.table_name}"
            print(f"destory: {sql}")
            client.execute(sql)
        finally:
            client.disconnect()

        _table_key = ".".join([self._type, self._namespace, self._name])
        from common.python.p_session.base_impl.db_runtime import DBRuntime
        DBRuntime.get_instance().meta_table.delete(_table_key)

    def count(self):
        sql = f"SELECT count(*) FROM {self.table_name}"
        try:
            client = self.get_conn()
            result = client.execute(sql)
            result = result[0][0]
        finally:
            client.disconnect()
        return result

    def take(self, n=1, keysOnly=False, use_serialize=True):
        use_serialize = set_force_serialize(use_serialize)

        if n <= 0:
            n = 1

        rtn = []
        try:
            sql = f"SELECT k,v FROM {self.table_name} limit {n}"
            client = self.get_conn()
            data = client.execute_iter(sql)

            for item in data:
                key, value = item[0], item[1]
                if use_serialize:
                    if keysOnly:
                        rtn.append(deserialize(key))
                    else:
                        rtn.append((deserialize(key), deserialize(value)))
                else:
                    if keysOnly:
                        rtn.append(key)
                    else:
                        rtn.append((key, value))
        finally:
            client.disconnect()
        return rtn

    def first(self, keysOnly=False, use_serialize=True):
        use_serialize = set_force_serialize(use_serialize)

        resp = self.take(1, keysOnly=keysOnly, use_serialize=use_serialize)
        if resp:
            return resp[0]
        else:
            return None

    def save_as(self, name, namespace, partition=None, use_serialize=True, **kwargs):
        use_serialize = set_force_serialize(use_serialize)

        if partition is None:
            partition = self._partitions

        persistent = kwargs["persistent"]
        persistent_engine = kwargs["persistent_engine"]
        dsource = kwargs["dsource"]

        dsource.set_gc_disable()

        from common.python.p_session.base_impl.db_runtime import DBRuntime
        dup = DBRuntime.get_instance().table(name, namespace, partition,
                                             persistent=persistent, persistent_engine=persistent_engine)

        new_table_name = f"`{namespace}`.`{name}`"
        try:
            client = self.get_conn()
            sql = f"INSERT INTO {new_table_name} SELECT * FROM {self.table_name} ORDER BY k DESC"
            client.execute(sql)
        finally:
            client.disconnect()

        dsource.set_gc_enable()
        return dup


def del_all_database():
    """
    Delete all database for test

    Returns
    -------

    """
    storage = ClickHouseStorage(_type=None, namespace="wefe", name="test")
    conn = storage.get_conn()
    sql = "show databases"

    exclude = ["wefe", "system", "default", "wefe_yuxin"]
    result = conn.execute(sql)
    print(result)
    for item in result:
        db_name = item[0]
        if db_name not in exclude:
            drop_sql = f"drop database if exists `{db_name}`"
            conn.execute(drop_sql)

    conn.disconnect()


def clean_up_tables(name_pattern, namespace=None):
    client = None
    try:
        if not namespace:
            namespace = NAMESPACE.PROCESS

        if not name_pattern:
            return

        client = get_db_conn(namespace)
        sql = "show tables"
        result = client.execute(sql)
        for item in result:
            db_name = item[0]
            if fnmatch(db_name, name_pattern):
                drop_sql = f"drop table `{namespace}`.`{db_name}`"
                client.execute(drop_sql)
    finally:
        if client:
            client.disconnect()


def test_collect_mem():
    # 5kw
    storage = ClickHouseStorage(_type=None, namespace="wefe_data", name="data_set_7cdfa44500c147a3ad78a8788550eec7")

    count_index = 0
    for k, v in storage.collect():
        count_index += 1
        if count_index % 100000 == 0 or count_index == 0:
            print(f'{count_index},{datetime.datetime.now()}')
            time.sleep(2)


if __name__ == '__main__':
    pass
    # test_collect_mem()

    # storage = ClickHouseStorage(_type=None, namespace="wefe_data", name="test_121601")
    # clean_up_tables("442a9be30a66423c9798e6ae18a152a0")
    # storage.put_all([(1, 1), (2, 2)])
    # print(list(storage.collect()))
    # del_all_database()

    # storage.save_as(name="yuxin_auto_create_test",namespace="wefe")

    # for k,v in storage.collect():
    #     print(k, v)

    # print(storage.take(n=100))
