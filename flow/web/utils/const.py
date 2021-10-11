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
from common.python.db.db_models import GlobalSetting
from common.python.utils import conf_utils


class JsonField:
    CODE = "code"
    MESSAGE = "message"


class ServiceStatusCode:
    SUCCESS_CODE = 0
    REMOTE_SERVICE_ERROR = 10003


class ServiceStatusMessage:
    SUCCESS_MESSAGE = "SUCCESS"
    REMOTE_SERVICE_ERROR_MESSAGE = "访问 gateway 失败：请检查 gateway 服务是否正常"
    WHITELIST_NOT_ADDED = "访问 gateway 失败：请在全局设置中添加 flow ip 至白名单"
    ADDRESS_IS_EMPTY = "访问 gateway 失败：请在全局设置中添加 gateway 地址"


class GrpcStatusMessage:
    UNAVAILABLE = "StatusCode.UNAVAILABLE"
    PERMISSION_DENIED = "StatusCode.PERMISSION_DENIED"


class ServiceType:
    FLOW = "flow"
    GATEWAY = "gateway"
    BOARD = "board"


class ServiceMeta:
    HOST = "0.0.0.0"
    PORT = "5000"


class TimeGap:
    HEARTBEAT_TIME = 30
    SYNV_TIME = 30
    EXCEPT_TIME = 60


class FlowConfig:
    HOST = conf_utils.get_comm_config(consts.COMM_CONF_KEY_WEB_IP)
    PORT = conf_utils.get_comm_config(consts.COMM_CONF_KEY_WEB_PORT)


class GatewayConfig:
    # The gateway service address in the environment variable
    env_host = conf_utils.get_env_config(consts.ENV_CONF_KEY_GATEWAY_HOST)
    env_port = conf_utils.get_env_config(consts.ENV_CONF_KEY_GATEWAY_PORT)

    # Get from the environment variable or database
    HOST = env_host or GlobalSetting.get_gateway_host()
    PORT = env_port or GlobalSetting.get_gateway_port()
