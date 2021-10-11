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

# noinspection PyProtectedMember
import uuid
from typing import Iterable

from common.python import WorkMode
from common.python.p_session.base_impl.table import DSource
from common.python.p_session.session import WefeSession
from common.python.utils.core import StorageSession
# noinspection PyProtectedMember
from common.python.utils.store_type import StoreTypes


def build_db_runtime(work_mode: WorkMode, storage_session):
    if work_mode.is_standalone() or work_mode.is_cluster():
        from common.python.storage.impl.dsource import DBRuntime
        return DBRuntime(storage_session)
    else:
        raise ValueError(f"work_mode: {work_mode} not supported!")


def build_storage_session(work_mode: WorkMode, job_id=None, db_type=None, server_conf_path=""):
    if work_mode.is_standalone() or work_mode.is_cluster():
        session_id = job_id or str(uuid.uuid1())
        session = StorageSession(session_id=session_id, db_type=db_type)
        return session

    raise ValueError(f"work_mode: {work_mode} not supported!")


def build_session(job_id, work_mode: WorkMode, persistent_engine: str, db_type=None):
    storage_session = build_storage_session(work_mode=work_mode, job_id=job_id, db_type=db_type)
    session = WefeSessionImpl(storage_session, work_mode, persistent_engine)
    return session


# noinspection PyProtectedMember
class WefeSessionImpl(WefeSession):
    """
    wefe session
    """

    def __init__(self, storage_session, work_mode, persistent_engine: str):
        self._db_runtime = build_db_runtime(work_mode=work_mode, storage_session=storage_session)
        self._session_id = storage_session.get_session_id()
        # self._db_type = storage_session.get_db_type()

        from common.python.storage import StoreType as StoreTypeV1
        if persistent_engine == StoreTypes.STORE_TYPE_PERSISTENCE:
            self._persistent_engine = StoreTypeV1.PERSISTENCE
        elif persistent_engine == StoreTypes.STORE_TYPE_PROCESS:
            self._persistent_engine = StoreTypeV1.PROCESS
        else:
            raise ValueError(f"{persistent_engine} not supported, use one of {[e.value for e in StoreTypeV1]}")
        WefeSession.set_instance(self)

    def get_persistent_engine(self):
        return self._persistent_engine

    def table(self,
              name,
              namespace,
              partition,
              persistent,
              in_place_computing,
              create_if_missing,
              error_if_exist,
              **kwargs):
        _dsource = self._db_runtime.table(name=name, namespace=namespace, partition=partition,
                                          persistent=persistent, in_place_computing=in_place_computing,
                                          create_if_missing=create_if_missing, error_if_exist=error_if_exist,
                                          persistent_engine=self._persistent_engine)
        return DSource(dsource=_dsource, session_id=self._session_id)

    def parallelize(self,
                    data: Iterable,
                    include_key,
                    name,
                    partition,
                    namespace,
                    persistent,
                    chunk_size,
                    in_place_computing,
                    create_if_missing,
                    error_if_exist,
                    need_send):
        _dsource = self._db_runtime.parallelize(data=data,
                                                include_key=include_key,
                                                name=name,
                                                partition=partition,
                                                namespace=namespace,
                                                persistent=persistent,
                                                chunk_size=chunk_size,
                                                in_place_computing=in_place_computing,
                                                create_if_missing=create_if_missing,
                                                error_if_exist=error_if_exist,
                                                persistent_engine=self._persistent_engine)

        rdd_inst = DSource(_dsource, session_id=self._session_id)

        return rdd_inst

    def cleanup(self, name, namespace, persistent):
        self._db_runtime.cleanup(name=name, namespace=namespace, persistent=persistent)

    def generateUniqueId(self):
        return self._db_runtime.generateUniqueId()

    def get_session_id(self):
        return self._session_id

    def stop(self):
        self._db_runtime.stop()

    def kill(self):
        self._db_runtime.stop()
