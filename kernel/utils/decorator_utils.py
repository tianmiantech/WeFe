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

import json
from functools import wraps

from common.python.common.consts import ENV
from common.python.common.global_config import global_config
from common.python.utils import file_utils


def load_config(args) -> object:
    if global_config.ENV == ENV.ENV_LOCAL:
        return file_utils.load_json_conf(args.config)
    else:
        return json.loads(args.config)


def update_task_status_env():
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            if global_config.ENV != ENV.ENV_LOCAL:
                func(*args, **kwargs)
            else:
                print("{} {} {}".format("This is", global_config.ENV, "environment"))

        return wrapper

    return decorator
