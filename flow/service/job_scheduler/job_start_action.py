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

import time
from concurrent.futures.thread import ThreadPoolExecutor

from common.python.common.consts import JobStatus
from common.python.common.consts import MemberRole
from common.python.common.enums import FlowQueueActionType
from common.python.db.db_models import Job
from common.python.db.flow_dao import ProjectFlowDao
from common.python.db.job_dao import JobDao
from common.python.db.task_dao import TaskDao
from common.python.utils.core_utils import current_datetime, current_timestamp
from common.python.utils.log_utils import schedule_logger
from flow.service.job_scheduler.clear_job_middle_data_scheduler import ClearJobMiddleDataScheduler
from flow.service.job_scheduler.job_service import JobService
from flow.service.job_scheduler.job_stop_action import JobStopAction
from flow.service.job_scheduler.run_task_action import RunTaskAction


class JobStartAction(object):
    job: Job
    my_role: MemberRole
    task_executor_pool: ThreadPoolExecutor

    def __init__(self, job_id, my_role) -> None:
        super().__init__()
        self.job = JobDao.find_one_by_job_id(job_id, my_role)
        self.my_role = my_role
        self.task_executor_pool = ThreadPoolExecutor(max_workers=4)

    def do(self, action: FlowQueueActionType):

        if self.job is None:
            return

        if self.job.status != JobStatus.WAIT_RUN:
            return

        try:
            if not self.wait_for_all_members_are_ready():
                message = "等待其他成员 Job Ready 超时"
                JobStopAction(self.job.job_id, self.job.my_role).do(JobStatus.ERROR_ON_RUNNING, message)
                return

            # update job status
            self.job.status = JobStatus.RUNNING
            self.job.status_updated_time = current_datetime()
            self.job.start_time = current_datetime()
            JobDao.save(self.job)

            # For tasks that continue to run, clean up the intermediate state data before executing the task.
            if action == FlowQueueActionType.RESUME_JOB:
                ClearJobMiddleDataScheduler.clean_job_middle_data(self.job, False)

            # Get the task in the job
            tasks = TaskDao.list_by_job(self.job)
            for task in tasks:
                # Multi-threaded consumption task
                run_task_action = RunTaskAction(self.job, task)
                self.task_executor_pool.submit(run_task_action.do)

        except Exception as e:
            self.job.status = JobStatus.ERROR_ON_RUNNING
            self.job.status_updated_time = current_datetime()
            JobDao.save(self.job)
            schedule_logger(self.job.job_id + '_' + self.my_role).exception(e)
        finally:
            self.after_start()

    def after_start(self):
        """
        Events: after the start action is executed
        """
        # update job progress
        JobService.update_progress(self.job)
        # update flow status
        ProjectFlowDao.update_status_by_job(self.job)

    def wait_for_all_members_are_ready(self):
        """
        All members waiting for the job are ready
        """
        # Maximum waiting time(ms)
        max_wait_time = 30 * 1000
        start_time = current_timestamp()

        while True:
            # Waiting timed out, not waiting anymore.
            if current_timestamp() - start_time > max_wait_time:
                return False

            # check once
            if JobService.all_job_member_are_ready_to_start(self.job.job_id):
                return True
            time.sleep(3)
