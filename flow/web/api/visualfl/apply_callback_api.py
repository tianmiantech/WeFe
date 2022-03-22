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
from common.python.utils.core_utils import current_datetime
from flow.web.api.base.base_api import BaseApi
from flow.web.api.base.dto.base_api_input import BaseApiInput
from flow.web.api.base.dto.base_api_output import BaseApiOutput
from common.python.utils.log_utils import schedule_logger


class Input(BaseApiInput):
    job_id: str
    task_id: str
    status: str
    message: str
    server_endpoint: str
    aggregator_endpoint: str
    aggregator_assignee: str


class Api(BaseApi):

    def run(self, input: Input):
        schedule_logger().info("get request apply_callback_api:{},{},{},{},{}".format(input.job_id,input.task_id,input.server_endpoint,input.aggregator_endpoint,input.aggregator_assignee))
        resp = 'success'
        apply_result = JobApplyResultDao.find_one_by_job_id(input.job_id, input.task_id)
        force_insert = False
        if apply_result is None:
            force_insert = True
            apply_result = JobApplyResult()
            apply_result.id = str(uuid.uuid1()).replace("-", "")
            apply_result.job_id = input.job_id
            apply_result.task_id = input.task_id
            apply_result.created_time = current_datetime()
        apply_result.server_endpoint = input.server_endpoint
        apply_result.aggregator_endpoint = input.aggregator_endpoint
        apply_result.aggregator_assignee = input.aggregator_assignee
        apply_result.updated_time = current_datetime()
        schedule_logger().info("save apply result:{}".format(apply_result.id))
        JobApplyResultDao.save(apply_result, force_insert=force_insert)
        return BaseApiOutput.success(resp)
