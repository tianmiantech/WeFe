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

    mapReducePartitions method

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

    map_func = cloudpickle.loads(bytes.fromhex(evt['map_func']))
    reduce_func = cloudpickle.loads(bytes.fromhex(evt['reduce_func']))

    def _dict_reduce(a: dict, b: dict):
        for k, v in b.items():
            if k not in a:
                a[k] = v
            else:
                a[k] = reduce_func(a[k], v)
        return a

    def _local_map_reduce(it):
        ret = {}
        for _k, _v in map_func(it):
            if _k not in ret:
                ret[_k] = _v
            else:
                ret[_k] = reduce_func(ret[_k], _v)
        return ret

    partition_map_reduce_result = _local_map_reduce(source_k_v)




    # do reduce
    # result, count = none_key_func(source_k_v, evt)

# def none_key_func(source_k_v, evt):
#     func = cloudpickle.loads(bytes.fromhex(evt['func']))
#     reduce_v = None
#     count = 0
#     for _, v in source_k_v:
#         count += 1
#         if reduce_v is None:
#             reduce_v = v
#         else:
#             reduce_v = func(reduce_v, v)
#     return reduce_v, count

    # tc = TimeConsume()

    # # get the source and destination fcStorage
    # source_fcs, dest_fcs = dataUtil.get_fc_storages(evt)
    # # get data
    # partition = evt['partition']
    # source_k_v = source_fcs.collect(partition=partition, debug_info=dataUtil.get_request_id(context))
    # tc.end('get data', evt, context)
    # # do mapReducePartitions
    # map_func = cloudpickle.loads(bytes.fromhex(evt['map_func']))
    # tc.end('cloudpickle.loads', evt, context)
    # map_result = []
    # count = 0
    # # do map
    # for k, v in source_k_v:
    #     count += 1
    #     map_result.append(map_func(k, v))
    # # do reduce
    # result = 0
    # if 'key_func' in evt.keys():
    #     result = has_key_func(map_result, evt)
    # else:
    #     result = none_key_func(map_result, evt)
    # tc.end('mapReducePartitions:map and reduce', evt, context)
    # # put result to destination fcStorage
    # dest_fcs.put_all([(partition, result)])
    # return dataUtil.fc_result(count=count, partition=partition)

#
# def none_key_func(source_k_v, evt):
#     reduce_func = cloudpickle.loads(bytes.fromhex(evt['reduce_func']))
#     reduce_v = None
#     for _, v in source_k_v:
#         if reduce_v is None:
#             reduce_v = v
#         else:
#             reduce_v = reduce_func(reduce_v, v)
#     return [(evt['partition'], reduce_v)] if reduce_v is not None else []
#
#
# def has_key_func(source_k_v, evt):
#     reduce_func = cloudpickle.loads(bytes.fromhex(evt['reduce_func']))
#     key_func = cloudpickle.loads(bytes.fromhex(evt['key_func']))
#     k_v_list = {}
#     for k, v in source_k_v:
#         _k = key_func(k)
#         if _k not in k_v_list.keys():
#             k_v_list[_k] = [v]
#         else:
#             k_v_list[_k].append(v)
#     for k, vList in k_v_list.items():
#         v_last = None
#         for v in vList:
#             if v_last is None:
#                 v_last = v
#             else:
#                 v_last = reduce_func(v_last, v)
#         if v_last is not None:
#             k_v_list[k] = v_last
#
#     return [(evt['partition'], k_v_list)]
