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

from common.python.db.db_models import DB, JobMember


class JobMemberDao:

    @staticmethod
    def list_by_job_id(job_id) -> [JobMember]:
        """
        Get the member list of the specified job

        Parameters
        ----------
        job_id: str

        Returns
        -------
        List of JobMember
        """
        with DB.connection_context():
            job_members = JobMember.select().where(job_id == JobMember.job_id)

            return job_members

    @staticmethod
    def get(*query, **filters):
        with DB.connection_context():
            return JobMember.get_or_none(*query, **filters)
