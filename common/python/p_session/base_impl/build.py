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

from common.python import FederationWrapped
from common.python.common import consts
from common.python.db.global_config_dao import GlobalConfigDao
from common.python.p_session import build
from common.python.p_session.base_impl.session import build_session
from common.python.p_session.base_impl.table import DSource
from common.python.utils import conf_utils


class Builder(build.Builder):
    _table_cls = DSource

    def __init__(self, session_id, work_mode, persistent_engine, db_type=None):
        self._session_id = session_id
        self._work_mode = work_mode
        self._persistent_engine = persistent_engine
        self._db_type = db_type

    def build_session(self):
        return build_session(job_id=self._session_id, work_mode=self._work_mode,
                             persistent_engine=self._persistent_engine, db_type=self._db_type)

    def build_federation(self, federation_id, runtime_conf, server_conf_path):
        if self._work_mode.is_standalone():
            from common.python.p_federation.impl.federation_standalone import FederationRuntime
            return FederationRuntime(session_id=federation_id, runtime_conf=runtime_conf)

        elif self._work_mode.is_cluster():
            from common.python.p_federation.impl.federation_cluster import FederationRuntime
            env_host = conf_utils.get_env_config(consts.ENV_CONF_KEY_GATEWAY_HOST)
            env_port = conf_utils.get_env_config(consts.ENV_CONF_KEY_GATEWAY_PORT)

            # host = env_host or GlobalSetting.get_gateway_host()
            # port = env_port or GlobalSetting.get_gateway_port()

            gateway_intranet = GlobalConfigDao.getGatewayConfig().intranet_base_uri.split(":")

            host = env_host or gateway_intranet[0]
            port = env_port or gateway_intranet[1]

            return FederationRuntime(session_id=federation_id, runtime_conf=runtime_conf, host=host, port=port)

    # noinspection PyUnresolvedReferences,PyProtectedMember
    def build_wrapper(self):
        if self._work_mode.is_standalone() or self._work_mode.is_cluster():
            from common.python.storage.impl.dsource import _DSource
            return FederationWrapped(session_id=self._session_id, dsource_cls=_DSource, table_cls=self._table_cls)

        raise ValueError(f"work_mode: ${self._work_mode} unknown")
