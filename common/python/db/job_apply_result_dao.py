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

from common.python.db.db_models import DB, Job, JobApplyResult


class JobApplyResultDao:

    @staticmethod
    def find_one_by_job_id(job_id, task_id) -> JobApplyResult:
        with DB.connection_context():
            return JobApplyResult.get_or_none(JobApplyResult.job_id == job_id, JobApplyResult.task_id == task_id)

    @staticmethod
    def find_one_by_id(id) -> JobApplyResult:
        with DB.connection_context():
            return JobApplyResult.get_or_none(JobApplyResult.id == id)

    @staticmethod
    def save(model: JobApplyResult, force_insert=False):
        with DB.connection_context():
            return model.save(force_insert=force_insert)

