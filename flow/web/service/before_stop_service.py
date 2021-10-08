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

from common.python.common.consts import JobStatus
from common.python.db.job_dao import JobDao
from common.python.utils.log_utils import LoggerFactory

logger = LoggerFactory.get_logger("API")


class BeforeStop:
    """The action before python project dead

    In order to avoid the abnormal status;
    There are some actions need to do before the 'python project' dead.

    Action 1: Stop unfinished Jobs, retry 3 times.
    """

    @staticmethod
    def do():
        retry_times = 0
        while retry_times < 3:
            unfinished_job_list = JobDao.list_all_not_finished_job()
            for job in unfinished_job_list:
                JobDao.stop(job, JobStatus.STOP_ON_RUNNING, "系统暂停，任务停止运行")

            logger.info("暂停所有任务，第" + str(retry_times) + "次尝试")

            retry_times = retry_times + 1
            # if all job finished, jump out of loop
            if len(JobDao.list_all_not_finished_job()) == 0:
                logger.info("暂停所有任务成功，系统正常停止")
                break
