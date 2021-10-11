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
from common.python.utils.core_utils import current_datetime, current_timestamp
from common.python.utils.log_utils import schedule_logger
from common.python.db.db_models import Job
from common.python.db.job_dao import JobDao
from common.python.db.task_dao import TaskDao
from common.python.common.enums import FlowQueueActionType
from flow.service.job_scheduler.job_service import JobService
from service.job_scheduler.job_stop_action import JobStopAction
from service.job_scheduler.run_visualfl_task_action import RunVisualFLTaskAction


class VisualFLJobStartAction(object):
    job: Job
    my_role: MemberRole
    task_executor_pool: ThreadPoolExecutor

    def __init__(self, job_id, my_role) -> None:
        super().__init__()
        self.job = JobDao.find_one_by_job_id(job_id, my_role)
        self.my_role = my_role
        self.task_executor_pool = ThreadPoolExecutor(max_workers=2)

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

            # 更新 job 状态
            self.job.status = JobStatus.RUNNING
            self.job.status_updated_time = current_datetime()
            self.job.start_time = current_datetime()
            JobDao.save(self.job)

            # 获取 Job 中的 Task
            tasks = TaskDao.list_by_job(self.job)
            for task in tasks:
                # todo 多线程消费 task
                run_task_action = RunVisualFLTaskAction(self.job, task)
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
        启动动作执行之后发生的事件
        """
        # 更新 job 进度
        JobService.update_progress(self.job)
        # # 更新 flow 状态
        # ProjectFlowDao.update_status_by_job(self.job)

    def wait_for_all_members_are_ready(self):
        """
        等待任务的所有参与者就绪
        """
        # 最大等待时长（ms）
        max_wait_time = 30 * 1000
        start_time = current_timestamp()

        while True:
            # 等待超时，不等了。
            if current_timestamp() - start_time > max_wait_time:
                return False

            # 检查一下
            if JobService.all_job_member_are_ready_to_start(self.job.job_id):
                return True
            time.sleep(3)
