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

import json
from comm import dataUtil
from common.python.utils import cloudpickle


def handler(event, context):
    """

    collectReduce method, collect all reduce results

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
    # get the source and destination fcStorage
    source_fcs, dest_fcs = dataUtil.get_fc_storages(evt)
    # get data
    source_k_v = source_fcs.collect()
    func = cloudpickle.loads(bytes.fromhex(evt['func']))
    # do reduce
    result = []
    if 'key_func' in evt.keys():
        result, count = handle_dict(source_k_v, func)
    else:
        result, count = handle_value(source_k_v, func)
    # put result to destination fcStorage
    dest_fcs.put_all(result)
    return dataUtil.fc_result(count=count)


def handle_value(source_k_v, func):
    result = None
    count = 0
    for k, v in source_k_v:
        # v is single value
        count += 1
        if result is None:
            result = v
        else:
            result = func(result, v)
    return [(0, result)], count


def handle_dict(reduce_k_v, func):
    result = None
    count = 0
    for k, v in reduce_k_v:
        # v is dict
        count += 1
        if result is None:
            result = v
        else:
            result = merge_reduce(result, v, func)
    return [(0, result)], count


def merge_reduce(source_dict, other_dict, func):
    """
    merge the reduce result with the same key

    Args:
        source_dict:
        other_dict:
        func: the function to merge two dict

    Returns:
        dict1: result after merge
    """
    dict1_keys = source_dict.keys()
    for k, v in other_dict.items():
        if k in dict1_keys:
            source_dict[k] = func(source_dict[k], v)
        else:
            source_dict[k] = v
    return source_dict
