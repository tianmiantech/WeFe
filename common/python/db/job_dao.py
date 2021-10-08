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

from typing import List

from common.python.common.consts import JobStatus, TaskStatus
from common.python.db.db_models import DB, Job
from common.python.db.task_dao import TaskDao
from common.python.utils.core_utils import current_datetime


class JobDao:

    @staticmethod
    def stop(job: Job, job_status, message):
        """
        Close job

        Parameters
        ----------
        job: Job
            The job want to close.

        job_status: JobStatus
            The final status of the job.

        message: str
            Reasons for closing the job.

        """

        job = JobDao.find_one_by_id(job.id)

        # if the job was closed, return.
        if JobStatus.is_finished(job.status):
            return

        job.status = job_status
        job.message = message
        job.status_updated_time = current_datetime()
        job.updated_time = current_datetime()
        job.finish_time = current_datetime()
        job.save()

    @staticmethod
    def update_progress(job):
        """
        Update the progress of the job

        Parameters
        ----------
        job: Job
            The job want to update

        """
        task_list = TaskDao.list_by_job(job)
        success_count = 0
        for task in task_list:
            if task.status == TaskStatus.SUCCESS:
                success_count = success_count + 1

        job.progress = int(success_count / len(task_list) * 100)
        job.progress_updated_time = current_datetime()
        with DB.connection_context():
            job.save()

    @staticmethod
    def list_all_not_finished_job() -> List[Job]:
        """
        Get all unfinished jobs

        Returns
        -------
        List of jobs.
        """
        with DB.connection_context():
            running_job_list = Job \
                .select() \
                .where((Job.status == JobStatus.RUNNING) | (Job.status == JobStatus.WAIT_SUCCESS))

            return running_job_list

    @staticmethod
    def get_by_job_id_and_role(job_id, role) -> Job:
        """
        Get job by job_id and role.

        Parameters
        ----------
        job_id: str
        role: str

        Returns
        -------
        The job or None
        """
        with DB.connection_context():
            return Job.get_or_none(Job.job_id == job_id, Job.my_role == role)

    @staticmethod
    def find_one_by_job_id(job_id, role) -> Job:
        with DB.connection_context():
            return Job.get_or_none(Job.job_id == job_id, Job.my_role == role)

    @staticmethod
    def find_one_by_id(id) -> Job:
        """
        Find a job by id

        Parameters
        ----------
        id: str
            Primary key, not job_id.
        """

        with DB.connection_context():
            return Job.get_or_none(Job.id == id)

    @staticmethod
    def save(job):
        with DB.connection_context():
            return job.save()
