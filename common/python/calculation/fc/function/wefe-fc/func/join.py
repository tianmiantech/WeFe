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
        join method
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

    # get the source,others,destination fcStorage
    source_fcs_dict, others_fcs_dict, dest_fcs = dataUtil.get_fc_storages_has_other(evt, context)
    tc.end('join,get_fc_storages_has_other', evt, context)

    # get data
    partition = evt['partition']
    tc.end('join,get data', evt, context)

    # do join
    func = None
    if 'func' in evt.keys():
        func = cloudpickle.loads(bytes.fromhex(evt['func']))
    result = []
    # get others keys
    others_keys = others_fcs_dict.keys()
    # self join other
    count = 0
    for source_k, source_v in source_fcs_dict.items():
        count += 1
        if source_k in others_keys:
            v = others_fcs_dict[source_k]
            if func is not None:
                v = func(source_v, others_fcs_dict[source_k])
                result.append((source_k, v))
            else:
                result.append((source_k, (source_v, v)))
    tc.end('do join', evt, context)

    # put result to destination fcStorage
    if len(result) > 0:
        dest_fcs.put_all(result)
    tc.end(f'join: put_all, count: {len(result)}', evt, context)
    return dataUtil.fc_result(count=count, partition=partition)
