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

import threading

from common.python.db.task_dao import TaskDao


class TaskHeartbeatService(threading.Thread):
    """
    The heartbeat sending thread of the task process
    """

    def __init__(self, task_id):
        super().__init__()
        self.task_id = task_id

    def run(self):
        """
        Prove that the process is alive by modifying task.updated_time
        """
        TaskDao.update_time(self.task_id)
