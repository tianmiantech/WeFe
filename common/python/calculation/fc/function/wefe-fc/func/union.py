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
    # get the source,others,destination fcStorage
    source_fcs_dict, others_fcs_dict, dest_fcs = dataUtil.get_fc_storages_has_other(evt, context)
    # get data
    partition = evt['partition']
    func = cloudpickle.loads(bytes.fromhex(evt['func']))
    # do union
    result = []
    source_count = len(source_fcs_dict)
    others_count = len(others_fcs_dict)
    count = 0
    if source_count >= others_count:
        count = others_count
        result = union(source_fcs_dict, others_fcs_dict, func, True)
    else:
        count = source_count
        result = union(others_fcs_dict, source_fcs_dict, func, False)
    # put result to destination fcStorage
    dest_fcs.put_all(result)
    return dataUtil.fc_result(count=count, partition=partition)


def union(self_k_v, others_k_v, func, self_is_source):
    """

    Parameters
    ----------
    self_k_v: [(k,v)]
    others_k_v: [(k,v)]
    func: deal with the self and other's value
    self_is_source: boolean

    Returns
    -------

    """
    self_keys = self_k_v.keys()
    for k, v in others_k_v.items():
        if k in self_keys:
            if self_is_source:
                self_k_v[k] = func(self_k_v[k], v)
            else:
                self_k_v[k] = func(v, self_k_v[k])
        else:
            self_k_v[k] = others_k_v[k]
    return self_k_v.items()
