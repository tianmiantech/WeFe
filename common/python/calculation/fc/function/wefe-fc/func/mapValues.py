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
from comm.dataUtil import TimeConsume
from common.python.utils import cloudpickle


def handler(event, context):
    """

    mapValues method

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
    tc = TimeConsume()
    # get the source and destination fcStorage
    source_fcs, dest_fcs = dataUtil.get_fc_storages(evt)
    # get data
    partition = evt['partition']
    source_k_v = source_fcs.collect(partition=partition, debug_info=dataUtil.get_request_id(context))
    tc.end('get_data', evt, context)
    # do mapValues
    func = None
    func_init = False
    result = []
    count = 0
    for k, v in source_k_v:
        # load the func when source_k_v is not None
        if not func_init:
            tc.start()
            func = cloudpickle.loads(bytes.fromhex(evt['func']))
            func_init = True
            tc.end('cloudpickle.loads', evt, context)
        count += 1
        result.append((k, func(v)))
    tc.end('mapValues:collect_and_map_values', evt, context)
    # put result to destination fcStorage
    if len(result) > 0:
        dest_fcs.put_all(result)
    tc.end(f'mapValues:put_all_consume,count:{len(result)}', evt, context)
    return dataUtil.fc_result(partition=partition, count=count)
