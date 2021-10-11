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
import time
import uuid
from concurrent.futures import ThreadPoolExecutor, as_completed

from aliyunsdkcore.client import AcsClient
from aliyunsdkfnf.request.v20190315 import DescribeExecutionRequest
from aliyunsdkfnf.request.v20190315 import GetExecutionHistoryRequest
from aliyunsdkfnf.request.v20190315 import StartExecutionRequest

from common.python.common import consts
from common.python.utils import conf_utils
from common.python.utils.profile_util import log_elapsed


class WorkFlow(object):

    @classmethod
    def get_client(cls):
        fc_region = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_REGION)
        access_key_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID)
        key_secret = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_KEY_SECRET)
        return AcsClient(access_key_id, key_secret, fc_region)

    @classmethod
    def execute(cls, flow_name, input_data: dict, execution_name=None):
        client = cls.get_client()
        execution_name = execution_name or ('wefe-' + str(uuid.uuid1()))
        request = StartExecutionRequest.StartExecutionRequest()
        request.set_FlowName(flow_name)
        request.set_Input(json.dumps(input_data))
        request.set_ExecutionName(execution_name)
        client.do_action_with_exception(request)

        @log_elapsed
        def _check_execution():
            return cls.check_execution(flow_name, execution_name, client)

        return _check_execution()

    @classmethod
    def check_execution(cls, flow_name, execution_name, client=None):
        client = client or cls.get_client()
        request = DescribeExecutionRequest.DescribeExecutionRequest()
        request.set_FlowName(flow_name)
        request.set_ExecutionName(execution_name)
        while True:
            result = json.loads(client.do_action_with_exception(request))
            status = result.get('Status')
            if status == 'Succeeded':
                break
            if status in ['Failed', 'Stopped']:
                raise Exception('sub-flow exception')
            time.sleep(0.1)

    @classmethod
    def check_execution2(cls, flow_name, execution_name, client=None):
        client = client or cls.get_client()
        next_token = None
        index = 0
        sub_flow_dict = {}

        while True:
            request = GetExecutionHistoryRequest.GetExecutionHistoryRequest()
            request.set_FlowName(flow_name)
            request.set_ExecutionName(execution_name)
            request.set_Limit(100)
            if next_token:
                request.set_NextToken(next_token)

            resp = client.do_action_with_exception(request)
            history_result = json.loads(resp)
            events = history_result.get('Events')
            for event in events:
                if event.get('Type') == 'IterationExited':
                    sub_flow_dict[str(event.get('EventId'))] = event.get('EventDetail')
            last_event = events[-1]
            if last_event.get('Type') == 'ExecutionSucceeded':
                break
            if last_event.get('Type') == 'ExecutionFailed':
                raise Exception("ExecutionFailed")
            next_token = history_result.get('NextToken')
            time.sleep(0.05)
            index += 1

        if len(sub_flow_dict) > 0:
            with ThreadPoolExecutor() as t:
                all_task = []
                for event_id, detail in sub_flow_dict.items():
                    all_task.append(t.submit(cls.check_subflow_status, detail, client))
                for future in as_completed(all_task):
                    future.result()
        else:
            return True

    @classmethod
    def check_subflow_status(cls, detail, client):
        while True:
            local = json.loads(detail).get('local')
            request = DescribeExecutionRequest.DescribeExecutionRequest()
            request.set_FlowName(local.get('FlowName'))
            request.set_ExecutionName(local.get('ExecutionName'))
            request.set_WaitTimeSeconds(60)
            request.set_read_timeout(60)
            request.set_connect_timeout(60)
            resp = client.do_action_with_exception(request)
            result = json.loads(resp)
            if result.get('Status') == 'Succeeded':
                break
            if result.get('Status') == 'Failed':
                raise Exception('sub-flow exception')
            time.sleep(0.1)
