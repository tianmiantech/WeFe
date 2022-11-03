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
from common.python.utils import file_utils, log_utils
from common.python.utils.sm4_utils import SM4CBC
LOGGER = log_utils.get_logger()


def get_db_config(key: tuple):
    from common.python.db.global_config_dao import GlobalConfigDao
    group_name, var_name = key
    group_config = GlobalConfigDao.list(group_name)  # 已经解密了
    try:
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
    except Exception as e:
        LOGGER.error(e)
        return ""


def get_value_by_enable(value):
    enable = get_comm_config(consts.COMM_CONF_KEY_PRIVACY_DATABASE_ENCRYPT_ENABLE)
    sm4_key = bytes.fromhex(get_comm_config(consts.COMM_CONF_KEY_PRIVACY_DATABASE_ENCRYPT_SECRET_KEY))
    if "true" == enable:
        sm4_cipher = SM4CBC()
        try:
            return sm4_cipher.decrypt(sm4_key, value)
        except Exception as e:
            return value
    else:
        return value


def set_value_by_enable(value):
    enable = get_comm_config(consts.COMM_CONF_KEY_PRIVACY_DATABASE_ENCRYPT_ENABLE)
    sm4_key = bytes.fromhex(get_comm_config(consts.COMM_CONF_KEY_PRIVACY_DATABASE_ENCRYPT_SECRET_KEY))
    if "true" == enable:
        sm4_cipher = SM4CBC()
        try:
            return sm4_cipher.encrypt(sm4_key, value)
        except Exception as e:
            return value
    else:
        return value


def get_fc_local_config(key):
    """
        前提：函数已部署上云端
        目的：读取已和函数一起上传云端的配置
    Parameters
    ----------
    key

    Returns
    -------

    """
    root_path = os.getenv('PYTHONPATH')
    print(f'root_path: {root_path}/config.properties')
    comm_file_path = root_path + '/config.properties'
    if os.path.exists(comm_file_path):
        with open(comm_file_path, encoding="utf8") as fp:
            lines = fp.readlines()
            for line in lines:
                if line and not line.startswith("#"):
                    split_arr = line.split('=')
                    if split_arr[0].strip() == key:
                        return split_arr[1].strip()
    return None


def get_local_config(key, config_type):
    if config_type == consts.COMMON:
        comm_file_path = os.path.join(file_utils.get_project_base_directory(),
                                      get_env_config(consts.ENV_CONF_KEY_CONFIG) or "common.properties")
    elif config_type == consts.MEMBER_BASE:
        comm_file_path = os.path.join(file_utils.get_project_base_directory(),
                                      get_env_config(consts.ENV_CONF_KEY_CONFIG) or "member-base.properties")
    else:
        raise AttributeError(f'未知配置类型：{config_type}')
    if os.path.exists(comm_file_path):
        with open(comm_file_path, encoding="utf8") as fp:
            lines = fp.readlines()
            for line in lines:
                if line and not line.startswith("#"):
                    split_arr = line.split('=')
                    if split_arr[0].strip() == key[1]:
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

    if isinstance(key, tuple) and key is not None:
        config_type = key[0]
        if config_type == consts.MEMBER_BASE:
            # 读取 member-base.properties
            result = get_local_config(key, consts.MEMBER_BASE)
        elif config_type == consts.COMMON:
            # 读取 common.properties
            result = get_local_config(key, consts.COMMON)
        else:
            fc_env = os.getenv('IN_FC_ENV')
            if fc_env:
                if get_fc_local_config(consts.COMM_CONF_MAKE_TENCENT_IMAGE):
                    # 读取云端腾讯云函数计算配置
                    db_local_dict = {
                        consts.COMM_CONF_KEY_SCF_ACCOUNT_ID: 'scf.account_id',
                        consts.COMM_CONF_KEY_SCF_REGION: 'scf.cos.region',
                        consts.COMM_CONF_KEY_SCF_ACCESS_KEY_ID: "scf.access_key_id",
                        consts.COMM_CONF_KEY_SCF_KEY_SECRET: "scf.access_key_secret",
                        consts.COMM_CONF_KEY_SCF_COS_BUCKET_NAME: "scf.cos_bucket_name",
                        consts.COMM_CONF_CLOUD_PROVIDER: 'fc.cloud.provider'
                    }
                    result = get_fc_local_config(db_local_dict[key])
                else:
                    # 读取云端阿里云函数计算配置
                    db_local_dict = {
                        consts.COMM_CONF_KEY_FC_OSS_ENDPOINT: 'fc.oss.endpoint',
                        consts.COMM_CONF_KEY_FC_OSS_INTERNAL_ENDPOINT: 'fc.oss.internal_endpoint',
                        consts.COMM_CONF_KEY_FC_OSS_BUCKET_NAME: 'fc.oss.bucket_name',
                        consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID: 'fc.access_key_id',
                        consts.COMM_CONF_KEY_FC_KEY_SECRET: 'fc.access_key_secret',
                        consts.COMM_CONF_KEY_FC_ACCOUNT_ID: 'fc.account_id',
                        consts.COMM_CONF_KEY_LOG_ROOT: 'flow.log.root.path',
                        consts.COMM_CONF_CLOUD_PROVIDER: 'fc.cloud.provider'
                    }
                    result = get_fc_local_config(db_local_dict[key])
            else:
                # 需从数据库读取
                result = get_db_config(key)

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
    val = env_dist.get(key[1])
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
