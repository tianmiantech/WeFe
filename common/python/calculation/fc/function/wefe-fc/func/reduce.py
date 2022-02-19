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

    reduce method

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
    partition = evt['partition']
    source_k_v = source_fcs.collect(partition=partition, debug_info=dataUtil.get_request_id(context))

    # do reduce
    # result = []
    if 'key_func' in evt.keys():
        result, count = has_key_func(source_k_v, evt)
    else:
        result, count = none_key_func(source_k_v, evt)

    # put result to destination fcStorage
    if result is not None:
        dest_fcs.put(partition, result)

    return dataUtil.fc_result(count=count, partition=partition)


def none_key_func(source_k_v, evt):
    func = cloudpickle.loads(bytes.fromhex(evt['func']))
    reduce_v = None
    count = 0
    for _, v in source_k_v:
        count += 1
        if reduce_v is None:
            reduce_v = v
        else:
            reduce_v = func(reduce_v, v)
    return reduce_v, count


def has_key_func(source_k_v, evt):
    func = cloudpickle.loads(bytes.fromhex(evt['func']))
    key_func = cloudpickle.loads(bytes.fromhex(evt['key_func']))
    count = 0
    k_v_list = {}

    for k, v in source_k_v:
        count += 1
        _k = key_func(k)
        if _k not in k_v_list:
            k_v_list[_k] = [v]
        else:
            k_v_list[_k].append(v)

    for k, v_list in k_v_list.items():
        v_last = None
        for v in v_list:
            if v_last is None:
                v_last = v
            else:
                v_last = func(v_last, v)

        if v_last is not None:
            k_v_list[k] = v_last

    return k_v_list, count
