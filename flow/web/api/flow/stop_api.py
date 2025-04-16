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


from common.python.common.consts import JobStatus
from common.python.db.db_models import DB, Job
from flow.service.job_scheduler.job_stop_action import JobStopAction
from flow.web.api.base.base_api import BaseApi
from flow.web.api.base.dto.base_api_input import BaseApiInput
from flow.web.api.base.dto.base_api_output import BaseApiOutput


class Input(BaseApiInput):
    pass


class Api(BaseApi):

    def run(self, input):
        """
        stop all running job
        """

        self.stop_all()

        return BaseApiOutput.success(input)

    @staticmethod
    def stop_all():
        with DB.connection_context():
            jobs = Job.select().where(
                (Job.status == JobStatus.WAIT_RUN) |
                (Job.status == JobStatus.WAIT_STOP) |
                (Job.status == JobStatus.RUNNING)
            )

            for job in jobs:
                JobStopAction(job.job_id, job.my_role).do(
                    JobStatus.ERROR_ON_RUNNING,
                    "由于 flow 服务关闭导致该任务被中断，请重新启动任务"
                )
