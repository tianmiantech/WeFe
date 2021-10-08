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

import os

import grpc

from common.python.db.db_models import GlobalSetting
# logger
from common.python.db.global_config_dao import GlobalConfigDao
from common.python.protobuf.pyproto import gateway_service_pb2_grpc
from common.python.utils import log_utils

log_utils.LoggerFactory.set_directory(os.path.join(log_utils.get_log_root_path(), 'wefe_flow'))
stat_logger = log_utils.get_logger("wefe_flow_stat")
detect_logger = log_utils.get_logger("wefe_flow_detect")
access_logger = log_utils.get_logger("wefe_flow_access")

MEMBER_ID = GlobalSetting.get_member_id()
MEMBER_NAME = GlobalSetting.get_member_name()

gateway_intranet = GlobalConfigDao.getGatewayConfig().intranet_base_uri.split(":")
GATEWAY_HOST = gateway_intranet[0]
GATEWAY_PORT = gateway_intranet[1]

ONE_DAY_IN_SECONDS = 60 * 60 * 24
MAX_CONCURRENT_JOB_RUN = 10
JOB_DEFAULT_TIMEOUT = 7 * 24 * 60 * 60
JOB_GRPC = gateway_service_pb2_grpc.TransferServiceStub(grpc.insecure_channel(target=f"{GATEWAY_HOST}:{GATEWAY_PORT}",
                                                                              options=[
                                                                                  ('grpc.max_send_message_length', -1),
                                                                                  ('grpc.max_receive_message_length',
                                                                                   -1)]))
