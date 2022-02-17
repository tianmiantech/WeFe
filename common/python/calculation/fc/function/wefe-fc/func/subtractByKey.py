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
from comm.stat import Stat


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
    stat = Stat()

    partition = evt['partition']
    source_fcs, dest_fcs = dataUtil.get_fc_storages(evt)
    other_fcs = dataUtil.get_other_fc_storage(evt)

    # get data
    source_k_v = source_fcs.collect(partition=partition)
    others_keys = set(dataUtil.get_data_from_fcs(other_fcs, partition, only_key=True))

    dest_fcs.put_all(_do_subtract_by_key(source_k_v, others_keys, stat))
    return dataUtil.fc_result(count=stat.count, partition=partition)


def _do_subtract_by_key(source_k_v, others_keys: set, stat: Stat):
    for k, v in source_k_v:
        stat.incr()
        if k not in others_keys:
            yield k, v
