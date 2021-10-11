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
import time

from common.python.common.enums import FlowQueueActionType
from common.python.db.flow_action_queue_dao import FlowActionQueueDao
from common.python.utils.log_utils import LoggerFactory
from flow.cycle_actions.common_thread_pool import CommonThreadPool
from flow.cycle_actions.flow_action_queue.worker.base_flow_action_worker import BaseFlowActionWorker
from flow.cycle_actions.flow_action_queue.worker.resume_job_worker import ResumeJobWorker
from flow.cycle_actions.flow_action_queue.worker.save_output_data_worker import SaveOutputDataWorker
from flow.cycle_actions.flow_action_queue.worker.start_job_worker import StartJobWorker
from flow.cycle_actions.flow_action_queue.worker.stop_job_worker import StopJobWorker


class FlowActionQueueConsumer(threading.Thread):
    logger = LoggerFactory.get_logger("FlowActionQueueConsumer")
    """
    Consume the data in the flow_action_queue table
    """

    def run(self):

        while True:
            queue_item = FlowActionQueueDao.pull()
            if queue_item is None:
                time.sleep(1)
                continue

            self.logger.info(
                "receive action {} from {}:{}".format(queue_item.action, queue_item.producer, queue_item.params)
            )

            worker: BaseFlowActionWorker = self.create_worker(queue_item)

            CommonThreadPool.submit(worker.run)

    def create_worker(self, queue_item) -> BaseFlowActionWorker:
        """
        Create a worker object.
        The specific execution process of various instructions is described in the worker.
        """
        dic = {
            FlowQueueActionType.STOP_JOB: StopJobWorker,
            FlowQueueActionType.RUN_JOB: StartJobWorker,
            FlowQueueActionType.RESUME_JOB: ResumeJobWorker,
            FlowQueueActionType.SAVE_OUTPUT_DATA: SaveOutputDataWorker
        }

        worker = dic.get(queue_item.action)(queue_item)
        return worker


if __name__ == '__main__':
    FlowActionQueueConsumer().run()
