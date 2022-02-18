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

import json

from comm import dataUtil
from common.python.utils import cloudpickle


def handler(event, context):
    """

    union method

    Parameters
    ----------
    event: the data passed in when the user calls the function
    context: contains some information about the runtime of the function like:
            - requestId
            - function
            - credentials
            - service
            - region
            - accountId

    Returns
    -------
    function call result:
        {
            'code': code,
            'message': message,
            'req_id': req_id,
            'count': count,
            'partition': partition
        }

    """

    evt = json.loads(event)

    source_fcs, dest_fcs = dataUtil.get_fc_storages(evt)
    other_fcs = dataUtil.get_other_fc_storage(evt)

    partition = evt['partition']
    source_count = source_fcs.count(partition)
    other_count = other_fcs.count(partition)

    # left more than right
    left_is_source = True
    if source_count >= other_count:
        left_fcs = source_fcs
        right_fcs = other_fcs
    else:
        left_fcs = other_fcs
        right_fcs = source_fcs
        left_is_source = False

    left_dict = dict(dataUtil.get_data_from_fcs(left_fcs, partition))
    func = cloudpickle.loads(bytes.fromhex(evt['func']))

    right_kv = right_fcs.collect(partition=partition)
    dest_fcs.put_all(_do_union(right_kv, left_dict, left_is_source, func))
    return dataUtil.fc_result(count=source_count + other_count, partition=partition)


def _do_union(right_kv, left_dict: dict, left_is_source: bool, func):
    for right_k, right_v in right_kv:
        if right_k in left_dict:
            left_v = left_dict.get(right_k)
            left_dict[right_k] = func(left_v, right_v) if left_is_source else func(right_v, left_v)
        else:
            left_dict[right_k] = right_v
    return left_dict.items()
