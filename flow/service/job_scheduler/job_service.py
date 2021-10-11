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

from common.python.common.consts import JobStatus, TaskStatus
from common.python.db.db_models import Job
from common.python.db.job_dao import JobDao
from common.python.db.task_dao import TaskDao
from common.python.utils.core_utils import current_datetime
from flow.service.board.board_service import BoardService


class JobService:

    @staticmethod
    def job_is_running(job: Job):
        """
        Check whether the job is still in the running state
        """
        job = JobDao.find_one_by_id(job.id)
        return job.status == JobStatus.RUNNING

    @staticmethod
    def update_progress(job: Job):
        """
        Update job progress
        """
        job = JobDao.find_one_by_id(job.id)
        task_list = TaskDao.list_by_job(job)
        success_count = sum(map(lambda x: x.status == TaskStatus.SUCCESS, task_list))

        if len(task_list) == 0:
            progress = 100
        else:
            progress = int(success_count / len(task_list) * 100)

        if job.progress == progress:
            return

        job.progress = progress
        job.progress_updated_time = current_datetime()
        JobDao.save(job)

    @staticmethod
    def all_job_member_are_ready_to_start(job_id) -> bool:
        """
        Check if all participants of the task are ready
        1. All members got the job
        2. The job of all members is wait_run
        """
        # get progress
        progress_list = BoardService.get_job_progress(job_id)

        # If you don't get the progress
        if not progress_list:
            return False

        for progress in progress_list:
            # Some members did not get the progress
            if not progress.get_progress_success:
                return False

            # the status is not wait_run
            if (progress.job_status != JobStatus.WAIT_RUN) and (progress.job_status != JobStatus.RUNNING):
                return False

        return True

    @staticmethod
    def all_job_member_are_ready_to_success(job) -> bool:
        """
        Check if all members are ready for the job to enter the success state.
        """

        # Get the progress of all members
        progress_list = BoardService.get_job_progress(job.job_id)

        # Get failed, jump out, can’t perform success operation
        if not progress_list:
            return False

        for progress in progress_list:
            # Some members cannot get the progress, jump out, and cannot perform the success operation.
            if not progress.get_progress_success:
                return False

            # Not ready to enter the successful state（status != success/wait_success）
            # jump out, and cannot perform the success operation.
            if progress.job_status != JobStatus.WAIT_SUCCESS and progress.job_status != JobStatus.SUCCESS:
                return False

        return True
