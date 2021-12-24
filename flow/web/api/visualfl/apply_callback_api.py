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
import uuid

from common.python.db.db_models import JobApplyResult
from common.python.db.job_apply_result_dao import JobApplyResultDao
from flow.web.api.base.base_api import BaseApi
from flow.web.api.base.dto.base_api_input import BaseApiInput
from flow.web.api.base.dto.base_api_output import BaseApiOutput
from common.python.utils.log_utils import schedule_logger


class Input(BaseApiInput):
    job_id: str
    task_id: str
    server_endpoint: str
    aggregator_endpoint: str
    aggregator_assignee: str
    status:str

    def check(self):
        super().required([self.task_id])


class Api(BaseApi):

    def run(self, input: Input):
        schedule_logger().info("get request apply_callback_api:{}".format(input))
        resp = 'success'
        apply_result = JobApplyResultDao.find_one_by_job_id(input.job_id, input.task_id)
        if apply_result is None:
            apply_result = JobApplyResult()
            apply_result.id = str(uuid.uuid1())
            apply_result.job_id = input.job_id
            apply_result.task_id = input.task_id
            apply_result.status = input.status
        if 'wait_run' == input.status or 'running' == input.status:
            apply_result.server_endpoint = input.server_endpoint
            apply_result.aggregator_endpoint = input.aggregator_endpoint
            apply_result.aggregator_assignee = input.aggregator_assignee
            apply_result.status = input.status
        else:
            apply_result.status = input.status
        apply_result.id = str(uuid.uuid1())
        apply_result.save()
        return BaseApiOutput.success(resp)
