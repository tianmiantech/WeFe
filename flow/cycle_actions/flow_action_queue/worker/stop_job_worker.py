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

from common.python.common.consts import JobStatus
from flow.cycle_actions.flow_action_queue.worker.base_flow_action_worker import BaseFlowActionWorker
from flow.service.job_scheduler.job_stop_action import JobStopAction


class StopJobWorker(BaseFlowActionWorker):
    """
    The processor when the stop_job signal is received
    """

    def work(self, params):
        job_id = params.get('jobId')
        dst_role = params.get('dstRole', None)
        job_status = params.get('jobStatus', JobStatus.STOP_ON_RUNNING)
        message = params.get('message', '发起方终止了任务')

        JobStopAction(job_id, dst_role).do(job_status, message)
