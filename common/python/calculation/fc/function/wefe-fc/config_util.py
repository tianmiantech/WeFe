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
from common.python.utils.conf_utils import get_comm_config


def gen_config_file():
    file_name = 'config.properties'
    access_key_id = get_comm_config(consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID)
    access_key_secret = get_comm_config(consts.COMM_CONF_KEY_FC_KEY_SECRET)
    account_id = get_comm_config(consts.COMM_CONF_KEY_FC_ACCOUNT_ID)
    vpc_id = get_comm_config(consts.COMM_CONF_KEY_FC_VPC_ID)
    v_switch_ids = get_comm_config(consts.COMM_CONF_KEY_FC_V_SWITCH_IDS)
    security_group_id = get_comm_config(consts.COMM_CONF_KEY_FC_SECURITY_GROUP_ID)
    account_type = get_comm_config(consts.COMM_CONF_KEY_FC_ACCOUNT_TYPE)
    bucket_name = get_comm_config(consts.COMM_CONF_KEY_FC_OSS_BUCKET_NAME)
    oss_endpoint = get_comm_config(consts.COMM_CONF_KEY_FC_OSS_ENDPOINT)
    oss_internal_endpoint = get_comm_config(consts.COMM_CONF_KEY_FC_OSS_INTERNAL_ENDPOINT)

    json_obj = {
        'fc.access_key_id': access_key_id,
        'fc.access_key_secret': access_key_secret,
        'fc.account_id': account_id,
        'fc.vpc_id': vpc_id,
        'fc.v_switch_ids': v_switch_ids,
        'fc.security_group_id': security_group_id,
        'fc.account_type': account_type,
        'fc.oss.bucket_name': bucket_name,
        'fc.oss.endpoint': oss_endpoint,
        'fc.oss.internal_endpoint': oss_internal_endpoint
    }

    with open(file_name, 'w') as f:
        for k, v in json_obj.items():
            v = v if v is not None else ''
            f.write(str(k + '=' + v + '\n'))
        f.close()


if __name__ == '__main__':
    gen_config_file()
