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


import os

from common.python.utils import log_utils

# avoid kernel referencing the flow setting
log_utils.LoggerFactory.set_directory(os.path.join(log_utils.get_log_root_path(), 'wefe_flow'))
stat_logger = log_utils.get_logger("wefe_flow_stat")
detect_logger = log_utils.get_logger("wefe_flow_detect")
access_logger = log_utils.get_logger("wefe_flow_access")
