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

from common.python.db.db_models import DB, Job, ProjectFlow
from common.python.utils.core_utils import current_datetime


class ProjectFlowDao:

    @staticmethod
    def update_status_by_job(job: Job):
        """
        Update the status of the flow

        Args:
            job: The job entity
        """
        ProjectFlowDao.update_status(job.flow_id, job.status)

    @staticmethod
    def update_status(flow_id, job_status):
        """
        Update the status of the flow

        Args:
            flow_id: Id of flow
            job_status:
        """
        with DB.connection_context():
            ProjectFlow.update(
                flow_status=job_status, updated_time=current_datetime()
            ).where(
                ProjectFlow.flow_id == flow_id
            ).execute()
