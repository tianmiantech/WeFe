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

import traceback
from common.python.common.consts import TaskStatus
from flow.utils.budget_utils import BudgetUtils
from common.python.db.global_config_dao import GlobalConfigDao
import threading
from common.python.db.db_models import *
import time
from common.python.utils.log_utils import schedule_logger, LoggerFactory


class FcBudgetScheduler(threading.Thread):
    """
        Detects whether the budget of function computing exceeds the limit,
        and then stops the corresponding task.
    """
    logger = LoggerFactory.get_logger("FcBudgetScheduler")

    def __init__(self):
        super().__init__()
        function_compute_config = GlobalConfigDao.get_function_compute_config()
        self.month_budget = function_compute_config.max_cost_in_month
        self.day_budget = function_compute_config.max_cost_in_day

    @staticmethod
    def get_running_task():
        with DB.connection_context():
            task_list = Task.select().where(
                Task.status == TaskStatus.RUNNING
            ).execute()
            return task_list

    def get_month_budget(self):
        return self.month_budget

    def get_day_budget(self):
        return self.day_budget

    @staticmethod
    def stop_tasks(task_list, is_month=True):
        if is_month:
            FcBudgetScheduler.logger.warn("函数计算已超最大月限额, 随即停止所有任务！")
        else:
            FcBudgetScheduler.logger.warn("函数计算已超最大日限额, 随即停止所有任务！")
        with DB.connection_context():
            for task in task_list:
                task.status = TaskStatus.STOP
                task.save()

    def run(self):
        budget_util = BudgetUtils()

        while True:
            try:
                month_cost = budget_util.get_month_cost()
                day_cost = budget_util.get_day_cost()

                # Overspend daily or monthly
                if int(self.month_budget) <= month_cost:
                    task_list = self.get_running_task()
                    self.stop_tasks(task_list)
                    break
                elif self.day_budget <= day_cost:
                    task_list = self.get_running_task()
                    self.stop_tasks(task_list, is_month=False)
                    break
                else:
                    # judge once every 10min
                    time.sleep(10 * 60)
            except Exception as e:
                traceback.print_exc()
                schedule_logger().exception("函数计算预算检测出现异常：%s", e)
                time.sleep(5)
                continue
