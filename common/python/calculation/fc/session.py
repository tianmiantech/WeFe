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

import uuid
from typing import Iterable

from common.python.calculation.fc.fc_source import FCSource
from common.python.calculation.fc.fc_storage import FCStorage
from common.python.common.consts import NAMESPACE

__all__ = ["WefeSessionImpl"]

from common.python.p_session.session import WefeSession
from common.python.utils.store_type import StoreTypes


class WefeSessionImpl(WefeSession):
    """
    manage FCSource
    """

    def __init__(self, session_id, db_runtime, persistent_engine):
        self._session_id = session_id
        self._persistent_engine = persistent_engine
        self._db_runtime = db_runtime

        # convert to StoreType class in eggroll v1.x
        from common.python.storage import StoreType as StoreTypeV1
        if persistent_engine == StoreTypes.STORE_TYPE_PERSISTENCE:
            self._persistent_engine = StoreTypeV1.PERSISTENCE
        elif persistent_engine == StoreTypes.ROLLPAIR_LEVELDB:
            self._persistent_engine = StoreTypeV1.LEVEL_DB
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
        dsource = self._db_runtime.table(name=name, namespace=namespace, partition=partition,
                                         persistent=persistent, in_place_computing=in_place_computing,
                                         create_if_missing=create_if_missing, error_if_exist=error_if_exist,
                                         persistent_engine=self._persistent_engine)
        return FCSource.from_dsource(session_id=self._session_id, dsource=dsource)

    # noinspection PyUnresolvedReferences
    def parallelize(self,
                    data: Iterable,
                    name,
                    namespace,
                    partition,
                    include_key,
                    persistent,
                    chunk_size,
                    in_place_computing,
                    create_if_missing,
                    error_if_exist,
                    need_send):

        if namespace is None:
            # namespace = NAMESPACE.TRANSFER if need_send else NAMESPACE.PROCESS
            namespace = NAMESPACE.PROCESS

        if name is None:
            name = self._session_id + "_" + str(uuid.uuid1())

        _iter = data if include_key else enumerate(data)
        p = FCSource.get_fcs_partitions(num_partition=partition, data_count=0)
        fcs = FCStorage(namespace, name, p)
        fcs.put_all(_iter)
        return FCSource.from_fcs(fcs, self._session_id, namespace, name)

    def cleanup(self, name, namespace, persistent):
        return self._db_runtime.cleanup(name=name, namespace=namespace, persistent=persistent)

    def generateUniqueId(self):
        return self._db_runtime.generateUniqueId()

    def get_session_id(self):
        return self._session_id

    def stop(self):
        self._db_runtime.stop()

    def kill(self):
        self._db_runtime.stop()
