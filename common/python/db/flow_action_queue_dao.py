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

from common.python.db.db_models import DB, FlowActionQueue


class FlowActionQueueDao:

    @staticmethod
    def pull() -> FlowActionQueue:
        """
        Take an instruction from the queue.
        """
        with DB.connection_context():
            data_list = FlowActionQueue.select().order_by(FlowActionQueue.priority.desc()).limit(1)

            if data_list:
                queue_item = data_list[0]
                # The data in the queue is deleted when it is taken out
                queue_item.delete_instance()
                return queue_item
            else:
                return None

    @staticmethod
    def delete(action):
        """
        Delete the action in the database.

        Args:
            action: The action entity you want to delete
        """
        with DB.connection_context():
            action.delete_instance()

    @staticmethod
    def save(model: FlowActionQueue, force_insert=False):
        with DB.connection_context():
            model.save(force_insert=force_insert)
