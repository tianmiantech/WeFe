# -*- coding: utf-8 -*-

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


import importlib
import json

from comm import dataUtil
from comm.const import ErrorCode
from comm.dataUtil import TimeConsume


def handler(event, context):
    """
    main func
    :param event:
    :param context:
    :return:
    """
    try:
        print(f'FC Invoke Start RequestId:{dataUtil.get_request_id(context)}')
        evt = json.loads(event)
        cot = context
        ins = importlib.import_module("func." + evt['fc_name'])
        tc = TimeConsume()
        result = ins.handler(event, context)
        total_consume = tc.end(f'fc_name:{evt["fc_name"]}', evt, cot)

        result['total_consume'] = total_consume
        result['req_id'] = dataUtil.get_request_id(context)
        result['fc_name'] = evt['fc_name']
        print(f'FC Invoke End RequestId:{dataUtil.get_request_id(context)}')
        return json.dumps(result)
    except KeyError as e:
        print(e)
        import traceback
        traceback.print_exc()
        print(f'FC Invoke End RequestId:{dataUtil.get_request_id(context)}')
        return json.dumps(
            dataUtil.fc_result(code=ErrorCode.KEY_ERROR, message=str(e), req_id=dataUtil.get_request_id(context)))
    except Exception as inst:
        print(inst)
        import traceback
        traceback.print_exc()
        print(f'FC Invoke End RequestId:{dataUtil.get_request_id(context)}')
        return json.dumps(
            dataUtil.fc_result(code=ErrorCode.SYSTEM_ERROR, message=str(inst), req_id=dataUtil.get_request_id(context)))
