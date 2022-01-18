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

import datetime

from common.python.db.db_models import DB, Task, TaskProgress


class TaskProgressDao:

    @staticmethod
    def after_close_task(task: Task):
        progress = TaskProgressDao.find_one_by_task(task)
        if progress is None:
            return
        with DB.connection_context():
            progress.updated_time = datetime.datetime.now()
            progress.pid_success = 1
            progress.save()

    @staticmethod
    def find_one_by_task(task: Task):
        with DB.connection_context():
            return TaskProgress.get_or_none(
                TaskProgress.task_id == task.task_id, TaskProgress.role == task.role
            )

    @staticmethod
    def save(task_progress: TaskProgress, force_insert=False):
        with DB.connection_context():
            task_progress.save(force_insert=force_insert)

    @staticmethod
    def get_by_unique_id(task_id, role):
        with DB.connection_context():
            return TaskProgress.get_or_none(
                TaskProgress.task_id == task_id,
                TaskProgress.role == role
            )
