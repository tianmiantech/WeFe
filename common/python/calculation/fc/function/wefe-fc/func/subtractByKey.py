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

    subtractByKey method

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
    # get the source,others,destination fcStorage
    source_fcs_dict, others_fcs_dict, dest_fcs = dataUtil.get_fc_storages_has_other(evt, context)
    # get data
    partition = evt['partition']
    # do subtractByKey
    result = []
    # get others keys
    others_keys = others_fcs_dict.keys()
    # self subtractByKey other
    count = 0
    for source_k, source_v in source_fcs_dict.items():
        count += 1
        if source_k not in others_keys:
            result.append((source_k, source_v))
    # put result to destination fcStorage
    if len(result) > 0:
        dest_fcs.put_all(result)
    return dataUtil.fc_result(count=count, partition=partition)
