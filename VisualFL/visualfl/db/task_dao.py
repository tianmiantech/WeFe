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

from typing import List

from visualfl.utils.consts import TaskStatus
from visualfl.db.db_models import DB, Task, TaskResult
from visualfl.utils.core_utils import current_datetime
import datetime
import json
from common.python.utils.core_utils import  get_commit_id
import logging

class TaskDao:

    @staticmethod
    def update_task_status(task_id, status,message=None):
        """
        Update task status
        """
        try:
            with DB.connection_context():
                task = Task.select().where(
                    Task.task_id == task_id
                ).get()

                task.status = status
                task.message = message
                task.updated_time = current_datetime()
                task.finish_time = current_datetime()
                task.save()
        except Exception as e:
            logging.error(f"update task status error as {e} ")

    @staticmethod
    def save_task_result(task_id: str, task_result: dict,component_name: str,type: str):
        """
        Save task result

        Parameters
        ----------
        task_result
        result_type
        component_name:str
            Component name, special case can be specified separately

        Returns
        -------

        """
        try:
            with DB.connection_context():
                models = TaskResult.select().where(
                    TaskResult.task_id == task_id
                )

                tasks = Task.select().where(
                    Task.task_id == task_id,
                )

                # Compatible with local test without task information
                if len(tasks) != 0:
                    task = tasks[0]
                else:
                    return

                is_insert = True
                if models:
                    model = models[0]
                    is_insert = False
                else:
                    model = TaskResult()
                    model.created_time = datetime.datetime.now()

                model.job_id = task.job_id
                model.name = component_name
                model.task_id = task_id
                model.role = task.role
                model.type = type
                model.updated_time = datetime.datetime.now()
                model.result = json.dumps(task_result)
                model.component_type = component_name
                model.flow_id = task.flow_id
                model.flow_node_id = task.flow_node_id

                if is_insert:
                    model.id = get_commit_id()
                    model.save(force_insert=True)
                else:
                    model.save()
                return model
        except Exception as e:
            logging.error(f"save task result error as {e} ")
