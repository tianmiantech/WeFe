# -*- coding: utf-8 -*-

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

import json
import traceback

import requests

from common.python.common import consts
from common.python.common.consts import DataSetSourceType
from common.python.db.db_models import GlobalSetting, get_comm_config, DataSet
from common.python.utils import rsa_utils
from common.python.utils.log_utils import schedule_logger


def upload_data_set_to_union(data_set: DataSet):
    try:
        # If the data set is not original data, it will not be reported.
        if data_set.source_type != DataSetSourceType.RAW:
            return
        # If data exposure is prohibited globally, it will not be reported.
        if GlobalSetting.get_member_allow_public_data_set() is False:
            return
        # union Report interface
        url = get_comm_config(consts.COMM_CONF_KEY_UNION_BASE_URL) + 'data_set/put'

        params = {
            'name': data_set.name,
            'description': data_set.description,
            'id': data_set.id,
            'dimension_count': data_set.columns,
            'dimension_list': data_set.column_name_list,
            'contains_y': data_set.contains_y,
            'use_count': data_set.usage_count_in_job,
            'member_id': GlobalSetting.get_member_id(),
            'tags': data_set.tags,
            'sample_count': data_set.rows,
            'public_member_list': data_set.public_member_list,
        }
        data = json.dumps(params, separators=(',', ':'))
        # rsa sign
        sign = rsa_utils.sign(data, GlobalSetting.get_rsa_private_key())
        body = {
            'member_id': GlobalSetting.get_member_id(),
            'sign': str(sign, 'utf-8'),
            'data': data
        }
        body_data = json.dumps(body)

        result = requests.post(url, data=body_data).json()
        print(result)
        if result['code'] == 0:
            return True
    except Exception as e:
        traceback.print_exc()
        schedule_logger().exception(e)
