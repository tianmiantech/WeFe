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
from common.python.common import consts
from common.python.db.global_config_dao import GlobalConfigDao
from common.python.utils.conf_utils import get_env_config

ENV_INTRANET_IP = get_env_config(consts.ENV_CONF_KEY_INTRANET_IP) or 'ip'
ENV_GATEWAY_PORT = get_env_config(consts.ENV_CONF_KEY_GATEWAY_PORT) or 'port'
ENV_FLOW_PORT = get_env_config(consts.ENV_CONF_KEY_FLOW_PORT) or 'port'
ENV_NGINX_PORT = get_env_config(consts.ENV_CONF_KEY_NGINX_PORT) or 'port'


class GlobalSettingInit:
    """
    Initialize the relevant configuration based on the environment variables
    """

    def __init__(self):
        """
        wefe_board.intranet_base_uri
,       wefe_flow.intranet_base_uri
        wefe_gateway.intranet_base_uri
        wefe_serving.intranet_base_uri
        """
        intranet_dict = {
            'wefe_board': 'http://' + ENV_INTRANET_IP + ':' + ENV_NGINX_PORT + '/board-service',
            'wefe_flow': 'http://' + ENV_INTRANET_IP + ':' + ENV_FLOW_PORT,
            'wefe_gateway': ENV_INTRANET_IP + ':' + ENV_GATEWAY_PORT,
            'wefe_serving': ''
        }
        GlobalConfigDao.fill_intranet_base_uri(intranet_dict)

    @staticmethod
    def is_ready_to_boot():
        """
        The Member must to be initialized
        Check the variable global-config.member_id
        :return: bool
        """
        member_info = GlobalConfigDao.getMemberInfo()
        if member_info.member_id is None:
            return False
        else:
            return True


if __name__ == '__main__':
    print(GlobalSettingInit().is_ready_to_boot())
