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

import asyncio
from typing import Union, Tuple

from common.python import Federation
from common.python.common.consts import NAMESPACE
from common.python.p_session.base_impl.db_runtime import DBRuntime
# noinspection PyProtectedMember
from common.python.p_session.base_impl.data_source import _DSource
from common.python.utils.clean import Rubbish
from common.python.utils.log_utils import get_logger
from common.python.utils.member import Member
from common.python.utils.store_type import StoreTypes

OBJECT_STORAGE_NAME = "__federation__"
STATUS_TABLE_NAME = "__status__"

_remote_tag_histories = set()
_get_tag_histories = set()

LOGGER = get_logger()


def get_object_storage_name(session_id):
    return f"{session_id}_{OBJECT_STORAGE_NAME}"


def get_status_table_name(session_id):
    return f"{session_id}_{STATUS_TABLE_NAME}"


async def check_status_and_get_value(_table, _key):
    _value = _table.get(_key)
    while _value is None:
        await asyncio.sleep(0.1)
        _value = _table.get(_key)
    LOGGER.debug("[GET] Got {} type {}".format(_key, 'Table' if isinstance(_value, tuple) else 'Object'))
    return _value


def _get_meta_table(_name, _job_id):
    return DBRuntime.get_instance().table(_name, _job_id, partition=10)


class FederationRuntime(Federation):

    @staticmethod
    def __remote__object_key(*args):
        return "-".join(["{}".format(arg) for arg in args])

    def __init__(self, session_id, runtime_conf):
        super().__init__(session_id, runtime_conf)

        self._loop = asyncio.get_event_loop()

    def remote(self, obj, name: str, tag: str, parties: Union[Member, list]) -> Rubbish:
        if obj is None:
            raise EnvironmentError(f"federation try to remote None to {parties} with name {name}, tag {tag}")

        if isinstance(parties, Member):
            parties = [parties]

        for party in parties:
            if (name, tag, party) not in _remote_tag_histories:
                # raise EnvironmentError(f"remote duplicate tag {(name, tag)}")
                _remote_tag_histories.add((name, tag, party))

        self._remote_side_auth(name=name, parties=parties)

        rubbish = Rubbish(name, tag)
        for party in parties:
            _tagged_key = self.__remote__object_key(self._session_id, name, tag, self._role, self._member_id,
                                                    party.role,
                                                    party.member_id)
            _status_table = _get_meta_table(get_status_table_name(self._session_id), NAMESPACE.PROCESS)
            if isinstance(obj, _DSource):
                obj.set_gc_disable()
                # noinspection PyProtectedMember
                _status_table.put(_tagged_key, (obj._type, obj._name, obj._namespace, obj._partitions))
                rubbish.add_table(obj)
                rubbish.add_obj(_status_table, _tagged_key)
            else:
                _table = _get_meta_table(get_object_storage_name(self._session_id), NAMESPACE.PROCESS)
                _table.put(_tagged_key, obj)
                _status_table.put(_tagged_key, _tagged_key)
                rubbish.add_obj(_table, _tagged_key)
                rubbish.add_obj(_status_table, _tagged_key)
            LOGGER.debug("[REMOTE] Sent {}".format(_tagged_key))
        return rubbish

    def get(self, name: str, tag: str, parties: Union[Member, list]) -> Tuple[list, Rubbish]:
        if isinstance(parties, Member):
            parties = [parties]

        for party in parties:
            if (name, tag, party) in _get_tag_histories:
                raise EnvironmentError(f"get duplicate tag {(name, tag)}")
            _remote_tag_histories.add((name, tag, party))

        self._get_side_auth(name=name, parties=parties)

        _status_table = _get_meta_table(get_status_table_name(self._session_id), NAMESPACE.PROCESS)
        LOGGER.debug(f"[GET] {self._local_party} getting {name}.{tag} from {parties}")
        tasks = []

        for party in parties:
            _tagged_key = self.__remote__object_key(self._session_id, name, tag, party.role, party.member_id,
                                                    self._role,
                                                    self._member_id)
            tasks.append(check_status_and_get_value(_status_table, _tagged_key))
        results = self._loop.run_until_complete(asyncio.gather(*tasks))
        rtn = []
        rubbish = Rubbish(name, tag)
        _object_table = _get_meta_table(get_object_storage_name(self._session_id), NAMESPACE.PROCESS)
        for r in results:
            LOGGER.debug(f"[GET] {self._local_party} getting {r} from {parties}")
            if isinstance(r, tuple):
                _persistent = r[0] == StoreTypes.STORE_TYPE_PERSISTENCE
                table = DBRuntime.get_instance().table(name=r[1], namespace=r[2], persistent=_persistent,
                                                       partition=r[3])
                rtn.append(table)
                rubbish.add_table(table)

            else:  # todo: should standalone mode split large object?
                obj = _object_table.get(r)
                if obj is None:
                    raise EnvironmentError(f"federation get None from {parties} with name {name}, tag {tag}")
                rtn.append(obj)
                rubbish.add_obj(_object_table, r)
                rubbish.add_obj(_status_table, r)
        return rtn, rubbish
