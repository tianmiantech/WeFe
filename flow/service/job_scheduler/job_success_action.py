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
from common.python.db.db_models import Job
from common.python.db.flow_dao import ProjectFlowDao
from common.python.db.job_dao import JobDao
from common.python.utils.core_utils import current_datetime
from common.python.utils.log_utils import LoggerFactory
from flow.service.board.board_service import BoardService
from flow.service.job_scheduler.job_service import JobService


class JobSuccessAction:
    logger = LoggerFactory.get_logger("SuccessJobAction")
    """
    When all tasks have run successfully, this action will be entered to mark the Job as success.
    """
    job: Job

    def __init__(self, job_id, my_role) -> None:
        super().__init__()
        self.job = JobDao.get_by_job_id_and_role(job_id, my_role)

    def do(self, message):
        self.logger.info('call job success, job_id={},message={}'.format(self.job.job_id,message))
        if self.job.status != JobStatus.WAIT_SUCCESS:
            self.logger.info('call job fail, job_id={},status={}'.format(self.job.job_id, self.job.status))
            return

        # Check if all members are ready for success
        if not JobService.all_job_member_are_ready_to_success(self.job):
            self.logger.info('call job fail, job_id={},not all_job_member_are_ready_to_success'.format(self.job.job_id))
            return

        # Enter the success status
        self.job.status = JobStatus.SUCCESS
        self.job.message = message
        self.job.status_updated_time = current_datetime()
        self.job.updated_time = current_datetime()
        self.job.finish_time = current_datetime()
        JobDao.save(self.job)
        # update job progress
        JobService.update_progress(self.job)
        # update flow status
        ProjectFlowDao.update_status_by_job(self.job)
        # Notice board service
        BoardService.on_job_finished(self.job.job_id)
