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

from typing import MutableMapping

from common.python.common.consts import NAMESPACE
from common.python.storage import NamingPolicy, ComputingEngine
from common.python.p_session.base_impl.db_runtime import DBRuntime
from common.python.utils.log_utils import get_logger
from common.python.utils.store_type import DBTypes

LOGGER = get_logger()


class StorageSession(object):
    def __init__(self, session_id, chunk_size=100000, computing_engine_conf: MutableMapping = None,
                 naming_policy: NamingPolicy = NamingPolicy.DEFAULT, tag=None, db_type=DBTypes.LMDB):
        if not computing_engine_conf:
            computing_engine_conf = dict()
        self._session_id = session_id
        self._chunk_size = chunk_size
        self._computing_engine_conf = computing_engine_conf
        self._naming_policy = naming_policy
        self._tag = tag
        self._cleanup_tasks = set()
        self._runtime = dict()
        self._gc_table = None
        self._db_type = db_type

    def get_session_id(self):
        return self._session_id

    def get_db_type(self):
        return self._db_type

    def get_chunk_size(self):
        return self._chunk_size

    def computing_engine_conf(self):
        return self.computing_engine_conf

    def add_conf(self, key, value):
        self._computing_engine_conf[key] = str(value)

    def get_conf(self, key):
        return self._computing_engine_conf.get(key)

    def has_conf(self, key):
        return self.get_conf(key) is not None

    def get_naming_policy(self):
        return self._naming_policy

    def get_tag(self):
        return self._tag

    def clean_duplicated_table(self, storage):
        for item in list(self._gc_table.collect()):
            name = item[0]
            if isinstance(storage, DBRuntime):
                storage.cleanup(name, NAMESPACE.PROCESS, False)
            else:
                table = storage.table(name=name, namespace=NAMESPACE.PROCESS, persistent=False)
                if not table.gc_enable:
                    storage.destroy(table)

    def add_cleanup_task(self, func):
        self._cleanup_tasks.add(func)

    def run_cleanup_tasks(self, storage):
        for func in self._cleanup_tasks:
            func(storage)

    def to_protobuf(self):
        pass
        # return SessionInfo(sessionId=self._session_id,
        #                    computingEngineConf=self._computing_engine_conf,
        #                    namingPolicy=self._naming_policy.name,
        #                    tag=self._tag)

    @staticmethod
    def from_protobuf(session):
        return StorageSession(session_id=session.get_session_id(),
                              computing_engine_conf=session.get_computing_engine_conf(),
                              naming_policy=session.get_naming_policy(),
                              tag=session.get_tag())

    def set_runtime(self, computing_engine: ComputingEngine, target):
        self._runtime[computing_engine] = target

    def set_gc_table(self, storage):
        self._gc_table = storage.table(name=self._session_id + "__gc_", namespace=NAMESPACE.PROCESS)

    def __str__(self):
        return "<StorageSession: session_id: {}, computing_engine_conf: {}, naming_policy: {}, tag: {}, runtime: {}>" \
            .format(self._session_id, self.computing_engine_conf(), self._naming_policy.name, self._tag, self._runtime)
