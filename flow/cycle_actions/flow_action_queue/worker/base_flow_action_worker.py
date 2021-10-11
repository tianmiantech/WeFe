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

import json
import sys
import traceback

from common.python.db.db_models import FlowActionQueue, FlowActionLog
from common.python.db.flow_action_log_dao import FlowActionLogDao
from common.python.utils import network_utils
from common.python.utils.core_utils import get_commit_id
from common.python.utils.log_utils import schedule_logger


class BaseFlowActionWorker:
    """
    Abstract class
    Used to execute the actions specified in the flow_action_queue.

    Attributes:
        action: A item from flow_action_queue.
        flow_action_log: The action work execution log.
    """

    action: FlowActionQueue
    flow_action_log: FlowActionLog

    def __init__(self, queue_item):
        self.action = queue_item
        self.flow_action_log = FlowActionLog(
            id=get_commit_id(),
            producer=self.action.producer,
            priority=self.action.priority,
            action=self.action.action,
            params=self.action.params,
            consumer_ip=network_utils.get_local_ip()
        )

    def work(self, params):
        """
        Please override this method in the subclass

        Args:
            params: The params of action.
        """
        pass

    def run(self):
        """
        Execute the action
        """
        params = json.loads(self.action.params)

        try:
            self.work(params)

            self.flow_action_log.status = 'success'
        except Exception as e:
            self.flow_action_log.status = 'fail'

            # Print exception information
            traceback.print_exc()
            schedule_logger().exception("执行%s事件异常：%s", self.action.action, e)

            exc_type, exc_value, exc_traceback = sys.exc_info()
            error = str(repr(traceback.format_exception(exc_type, exc_value, exc_traceback)))

            self.flow_action_log.remark = error
        finally:
            FlowActionLogDao.log(self.flow_action_log)
