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

import datetime
import threading
import time

from common.python.common.consts import JobStatus
from common.python.db.task_dao import TaskDao
from common.python.utils.core_utils import current_datetime
from common.python.utils.log_utils import LoggerFactory
from flow.service.job_scheduler.job_stop_action import JobStopAction


class TaskGuard(threading.Thread):
    LOG = LoggerFactory.get_logger("JobGuard")
    """
    Responsibilities:
    - Close tasks that are not running properly
    Behavior:
    1）Detect the heartbeat of the running task process, and upgrade to the progress of each component later
    2）Ensure that the task process is consistent with the task state through pid_success
    """

    def run(self):

        while True:
            # Get all running tasks
            running_task_list = TaskDao.list_all_running_task()

            if len(running_task_list) == 0:
                time.sleep(5)
                continue

            for task in running_task_list:
                # Detect the heartbeat of each task, temporarily use the task.update field
                self.check_task_alive(task)

    def check_task_alive(self, task):
        time_format_str = "%Y-%m-%d %H:%M:%S"
        now = datetime.datetime.strptime(
            time.strftime(time_format_str, current_datetime()), time_format_str
        )
        last_update_time = task.updated_time
        if (now - last_update_time).seconds > 90:
            # The update time is greater than 90s, the task is timed out
            self.set_task_timeout(task)

    def set_task_timeout(self, task):
        JobStopAction(task.job_id, task.role).do(JobStatus.TIMEOUT, "任务超时，请检查服务后重试")
