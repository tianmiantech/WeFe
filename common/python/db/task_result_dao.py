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

from common.python.db.db_models import DB, TaskResult


class TaskResultDao(object):

    @staticmethod
    def get(*query, **filters):
        with DB.connection_context():
            return TaskResult.get_or_none(*query, **filters)

    @staticmethod
    def save(model: TaskResult, force_insert=False):
        with DB.connection_context():
            model.save(force_insert=force_insert)

    @staticmethod
    def get_last_task_result(job_id, role, result_type):
        results = TaskResult.select().where(
            TaskResult.job_id == job_id,
            TaskResult.role == role,
            TaskResult.type == result_type
        ).order_by(TaskResult.created_time.desc()).limit(1)
        if results:
            return results[0]

    @staticmethod
    def get_last_statics_result(job_id, role, result_type):
        return TaskResultDao.get_last_task_result(job_id, role, result_type)
