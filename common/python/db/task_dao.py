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

from typing import List

from common.python.common.consts import TaskStatus
from common.python.db.db_models import DB, Task, Job
from common.python.utils.core_utils import current_datetime


class TaskDao:

    @staticmethod
    def get(*query, **filters):
        with DB.connection_context():
            return Task.get_or_none(*query, **filters)

    @staticmethod
    def find_one_by_task(task: Task):
        with DB.connection_context():
            return Task.get(Task.id == task.id)

    @staticmethod
    def find_parents(task: Task) -> List[Task]:
        parent_list = []

        if not task.parent_task_id_list:
            return parent_list

        for task_id in list(task.parent_task_id_list.split(',')):
            with DB.connection_context():
                parent = Task.get(Task.task_id == task_id)
                parent_list.append(parent)

        return parent_list

    @staticmethod
    def stop(task: Task, message):
        if task.status != TaskStatus.RUNNING:
            return
        with DB.connection_context():
            task.status = TaskStatus.STOP
            task.message = message
            task.updated_time = current_datetime()
            task.finish_time = current_datetime()
            task.save()

    @staticmethod
    def save(task: Task):
        with DB.connection_context():
            task.save()

    @staticmethod
    def list_by_job(job: Job) -> List[Task]:
        return TaskDao.list_by_job_id(job.job_id, job.my_role)

    @staticmethod
    def list_by_job_id(job_id, my_role) -> List[Task]:
        """
        Get all tasks of the specified job

        Parameters
        ----------
        job_id: str
        my_role: str

        Returns
        -------
        List of Tasks
        """
        with DB.connection_context():
            tasks = Task.select().where(Task.job_id == job_id, Task.role == my_role).order_by(Task.position.asc())

            return tasks

    @staticmethod
    def list_all_running_task() -> List[Task]:
        """
        Get all running jobs

        Returns
        -------
        List of Tasks
        """
        with DB.connection_context():
            running_task_list = Task.select().where(Job.status == TaskStatus.RUNNING)

            return running_task_list

    @staticmethod
    def find_one_by_task_id(task_id) -> Task:
        with DB.connection_context():
            task = Task.select().where(Task.task_id == task_id)
            if task:
                return task[0]

    @staticmethod
    def update_time(task_id) -> None:
        task: Task
        with DB.connection_context():
            task = Task.get_or_none(Task.task_id == task_id)

            if task:
                task.updated_time = current_datetime()
                task.save()

    @staticmethod
    def update_by_id(update_info: dict, task_id) -> None:
        with DB.connection_context():
            Task.update(update_info).where(Task.task_id == task_id).execute()
