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
from common.python.utils import file_utils


def get_db_config(key: tuple):
    from common.python.db.global_config_dao import GlobalConfigDao
    group_name, var_name = key
    group_config = GlobalConfigDao.list(group_name)
    if key == consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_INTERNAL_END_POINT:
        return 'https://oss-' + group_config['region'] + '-internal.aliyuncs.com'
    elif key == consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_END_POINT:
        return 'https://oss-' + group_config['region'] + '.aliyuncs.com'
    elif key == consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_ROLE_ARN:
        return 'acs:ram::' + group_config['account_id'] + ':role/wefe-fc-ossread'
    elif key == consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_ROLE_SESSION_NAME:
        return 'oss_data'
    elif key == consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_DURATION_SECONDS:
        return 36000
    elif key == consts.COMM_CONF_KEY_FC_END_POINT:
        return 'https://' + group_config['account_id'] + '.' + group_config['region'] + '-internal.fc.aliyuncs.com'
    elif key == consts.COMM_CONF_KEY_FC_SERVICE_NAME:
        return "wefe-fc"
    elif key == consts.COMM_CONF_KEY_FC_OSS_ENDPOINT:
        return 'http://oss-' + group_config['region'] + '.aliyuncs.com'
    elif key == consts.COMM_CONF_KEY_FC_OSS_INTERNAL_ENDPOINT:
        return 'http://oss-' + group_config['region'] + '-internal.aliyuncs.com'
    else:
        return group_config[var_name]


def get_local_config(key):
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
    db_local_dict = {
        consts.COMM_CONF_KEY_FC_OSS_ENDPOINT: 'fc.oss.endpoint',
        consts.COMM_CONF_KEY_FC_OSS_INTERNAL_ENDPOINT: 'fc.oss.internal_endpoint',
        consts.COMM_CONF_KEY_FC_OSS_BUCKET_NAME: 'fc.oss.bucket_name',
    }

    fc_env = os.getenv('IN_FC_ENV')
    if isinstance(key, tuple) and key is not None:
        # 需从数据库读取
        if fc_env is None:
            result = get_db_config(key)
        else:
            result = get_local_config(db_local_dict[key])
        if result is not None:
            return result
    else:

        result = get_local_config(key)
        if result is not None:
            return result
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

# def get_backend_from_string(backend_string):
#     try:
#         return BACKEND.__dict__.get(backend_string)
#     except ValueError:
#         schedule_logger().error("BackType is Wrong")
#
# if __name__ == '__main__':
#     get_comm_config(consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_INTERNAL_END_POINT)
