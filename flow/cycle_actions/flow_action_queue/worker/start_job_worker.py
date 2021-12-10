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

from common.python.common.enums import FlowQueueActionType
from common.python.utils.log_utils import schedule_logger
from flow.cycle_actions.flow_action_queue.worker.base_flow_action_worker import BaseFlowActionWorker
from flow.service.job_scheduler.job_start_action import JobStartAction
from flow.service.job_scheduler.visualfl_job_start_action import VisualFLJobStartAction


class StartJobWorker(BaseFlowActionWorker):
    """
    The processor when the start_job signal is received
    """

    def work(self, params):
        job_id = params.get('jobId', '')
        my_role = params.get('dstRole', '')
        t = params.get('type', '')
        running_job = job_id + '_' + my_role

        schedule_logger(running_job).info('schedule job {}'.format(params))
        if 'visualfl' == t:
            VisualFLJobStartAction(job_id, my_role).do()
        else:
            JobStartAction(job_id, my_role).do(FlowQueueActionType.RUN_JOB)
