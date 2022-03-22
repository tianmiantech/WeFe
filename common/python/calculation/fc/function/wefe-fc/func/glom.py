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


def handler(event, context):
    """

    glom method: function call result is different from Spark result, the result format like:
        [(0, [(2, 2), (3, 3), (8, 8)]), (1, [(4, 4), (6, 6)]), (2, [(0, 0), (1, 1), (7, 7), (9, 9)]), (3, [(5, 5)])]

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
    partition = evt['partition']
    source_k_v = source_fcs.collect(partition=partition, debug_info=dataUtil.get_request_id(context))
    # do glom
    result = []
    count = 0
    for k, v in source_k_v:
        count += 1
        result.append((k, v))
    # put result to storage
    if len(result) == 0:
        return dataUtil.fc_result(count=count, partition=partition)
    dest_fcs.put_all([(partition, result)])
    return dataUtil.fc_result(count=count, partition=partition)
