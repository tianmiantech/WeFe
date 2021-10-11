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

import os

from cachetools import cached, LRUCache

from common.python.common import consts
from common.python.common.consts import BACKEND
from common.python.utils import file_utils
from common.python.utils.log_utils import schedule_logger


def get_base_config(key, default=None):
    base_config = file_utils.load_yaml_conf(os.path.join(file_utils.get_project_base_directory(),
                                                         "arch/conf/base_conf.yaml")) or dict()
    return base_config.get(key, default)


@cached(cache=LRUCache(maxsize=64))
def get_comm_config(key, default=None):
    """
    Get config from config.properties

    Parameters
    ----------
    key
    default

    Returns
    -------

    """
    comm_file_path = os.path.join(file_utils.get_project_base_directory(),
                                  get_env_config(consts.ENV_CONF_KEY_CONFIG) or "config.properties")
    if os.path.exists(comm_file_path):
        with open(comm_file_path, encoding="utf8") as fp:
            lines = fp.readlines()
            for line in lines:
                if line and not line.startswith("#"):
                    split_arr = line.split('=')
                    if split_arr[0].strip() == key:
                        return split_arr[1].strip()
    return default


@cached(cache=LRUCache(maxsize=64))
def get_env_config(key, default=None):
    """
    Read configuration from environment variables

    Parameters
    ----------
    key
    default

    Returns
    -------

    """
    env_dist = os.environ
    val = env_dist.get(key)
    return val if val else default


def set_env(key, value):
    """
    Set environment variables

    Parameters
    ----------
    key
    value

    Returns
    -------

    """
    os.environ[key] = value


def get_backend_from_string(backend_string):
    try:
        return BACKEND.__dict__.get(backend_string)
    except ValueError:
        schedule_logger().error("BackType is Wrong")
