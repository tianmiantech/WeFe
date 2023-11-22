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
import os
import random
import shutil
import socket
import time
import uuid
from collections import Iterable

from common.python.common.consts import NAMESPACE, TABLENAME
from common.python.p_session.base_impl.data_source import _DSource
from common.python.storage import StoreType
from common.python.utils import file_utils
from common.python.utils.core_utils import is_windows_sys
from common.python.utils.store_type import DBTypes

if is_windows_sys():
    from concurrent.futures import ThreadPoolExecutor as Executor
else:
    from concurrent.futures import ProcessPoolExecutor as Executor


class DBRuntime:
    __instance = None

    def __init__(self, storage_session):
        self.data_dir = os.path.join(file_utils.get_project_base_directory(), 'data', 'LMDB')
        self.session_id = storage_session.get_session_id()

        self.db_type = storage_session.get_db_type()

        # Using SQLite to store meta to provide performance if db_type is Local_FS
        meta_table_db_type = self.db_type
        if self.db_type == DBTypes.LOCAL_FS:
            meta_table_db_type = DBTypes.SQLITE

        self.meta_table = _DSource(StoreType.PERSISTENCE.value, NAMESPACE.DATA, TABLENAME.FRAGMENTS, 10,
                                      db_type=meta_table_db_type)
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
            # local fs do not auto gc table
            if self.db_type not in [DBTypes.LOCAL_FS] and self.storage_session._gc_table is not None:
                try:
                    count = self.storage_session._gc_table.get(name)
                    if count is None:
                        count = 0
                    self.storage_session._gc_table.put(name, count + 1)
                except Exception as ex:
                    print(f'_gc_table error:{ex}')
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

        elif self.db_type == DBTypes.LMDB:
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

        elif self.db_type == DBTypes.LOCAL_FS:
            self.table(name, namespace).destroy()

    def generateUniqueId(self):
        return self.unique_id_template % (
            self.session_id, self.host_name, self.host_ip, time.time(), random.randint(10000, 99999))

    @staticmethod
    def get_instance():
        if DBRuntime.__instance is None:
            raise EnvironmentError("should initialize before use")
        return DBRuntime.__instance
