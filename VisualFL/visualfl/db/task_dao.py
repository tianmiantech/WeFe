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


from visualfl.db.db_models import DB, Task, TaskResult,TaskProgress,is_local
from visualfl.utils.core_utils import current_datetime,get_commit_id
import datetime
import json
from visualfl.utils.logger import Logger
from visualfl.utils.consts import TaskStatus
import logging

class TaskDao(Logger):
    
    def __init__(self,task_id):
        self._task_id = task_id

    def start_task(self):
        """
        start task
        """
        try:
            if is_local:
                return
            with DB.connection_context():
                task = Task.select().where(
                    Task.task_id == self._task_id
                ).get()

                task.start_time = current_datetime()
                task.updated_time = current_datetime()
                task.status = TaskStatus.RUNNING
                task.save()
        except Exception as e:
            self.exception(e)
            self.error(f"save start task {self._task_id}  error as {e} ")

    def update_task_status(self, status,message=None):
        """
        Update task status
        """
        try:
            if is_local:
                return
            with DB.connection_context():
                task = Task.select().where(
                    Task.task_id == self._task_id
                ).get()

                task.status = status
                task.message = message
                task.updated_time = current_datetime()
                task.finish_time = current_datetime()
                task.save()
        except Exception as e:
            self.exception(e)
            self.error(f"update task {self._task_id} status to {status} error as {e} ")

    def get_task_result(self, result_type):
        """
        Get task result

        Parameters
        ----------
        result_type
        Returns
        -------

        """
        if is_local:
            return

        with DB.connection_context():

            where_condition = [TaskResult.task_id == self._task_id,TaskResult.type == result_type]

            models = TaskResult.select().where(*tuple(where_condition))

            if models:
                return models[0]
            else:
                return None

    def save_task_result(self,task_result: dict,component_name: str,type: str):
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
            if is_local:
                return

            with DB.connection_context():
                models = TaskResult.select().where(
                    TaskResult.task_id == self._task_id,
                    TaskResult.type == type
                )

                tasks = Task.select().where(
                    Task.task_id == self._task_id,
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
                model.task_id = self._task_id
                model.role = task.role
                model.type = type
                model.updated_time = datetime.datetime.now()
                model.result = json.dumps(task_result)
                model.component_type = component_name
                model.flow_id = task.flow_id
                model.flow_node_id = task.flow_node_id
                model.project_id = task.project_id

                if is_insert:
                    model.id = get_commit_id()
                    model.save(force_insert=True)
                else:
                    model.save()
                return model
        except Exception as e:
            logging.error(f"save task {self._task_id} result error as {e} ")

    def update_serving_model(self,type: str):
        """
        Update serving model
        """
        try:
            if is_local:
                return

            with DB.connection_context():
                models = TaskResult.select().where(
                    TaskResult.task_id == self._task_id,
                    TaskResult.type == type
                )

                if models:
                    model = models[0]
                else:
                    return

                model.serving_model = 1
                model.save()
                return model
        except Exception as e:
            logging.error(f"udate serving model error as {e},with task id : {self._task_id} ")

    def calc_progress(self,model: TaskProgress) -> TaskProgress:
        """

        Calculation progress

        According to the total engineering quantity, the current completion engineering quantity calculation progress
        If there is actual engineering quantity, calculate the percentage based on actual engineering quantity, that is, it is finished
        Otherwise, calculate the progress percentage according to the estimated engineering quantity

        Parameters
        ----------
        model

        Returns
        -------

        """
        if is_local:
            return

        if model.progress is None:
            model.progress = 0
        if model.progress > model.expect_work_amount:
            model.progress = model.expect_work_amount

        work_amount = model.really_work_amount or model.expect_work_amount
        model.progress_rate = round(model.progress / work_amount * 100, 2)
        if model.progress_rate > 100:
            model.progress_rate = 100

        if model.updated_time is not None and model.progress_rate > 0:
            model.spend = int((model.updated_time - model.created_time).total_seconds() * 1000)
            need_time = int(model.spend * 100 / model.progress_rate - model.spend)
            model.expect_end_time = model.updated_time + datetime.timedelta(milliseconds=need_time)

        return model

    def init_task_progress(self,work_amount: int):
        """

        Initialize the total engineering quantity of the task schedule

        eg. Logistic regression algorithm parameters need to run 300 iterations,
        then work_amount can be set to 300, then after each iteration is completed,
        the current work amount needs to be +1

        Parameters
        ----------
        work_amount:int
            Total engineering

        Returns
        -------

        """
        try:
            if is_local:
                return

            with DB.connection_context():
                model = TaskProgress.get_or_none(
                    TaskProgress.task_id == self._task_id,
                )

                is_insert = True

                if model:
                    is_insert = False
                    # reset
                    model.progress = 0
                    model.really_work_amount = None
                    model.created_time = datetime.datetime.now()
                    model.updated_time = None
                    model.expect_end_time = None
                    model.spend = None

                else:
                    model = TaskProgress()
                    model.id = get_commit_id()
                    model.progress = 0
                    model.created_time = datetime.datetime.now()

                    # get task info
                    task_info = Task.get_or_none(
                        Task.task_id == self._task_id,
                    )
                    if task_info:
                        model.flow_id = task_info.flow_id
                        model.flow_node_id = task_info.flow_node_id
                        model.project_id = task_info.project_id
                        model.job_id = task_info.job_id
                        model.task_id = self._task_id
                        model.role = task_info.role
                        model.task_type = task_info.task_type
                    else:
                        return

                model.expect_work_amount = work_amount
                self.calc_progress(model)
                model.save(force_insert=is_insert)
        except Exception as e:
            self.exception(e)
            logging.error(f"init task {self._task_id} progress error as {e} ")

    def set_task_progress(self, work_amount: int):
        """
        Update the progress according to the specified work amount

        Parameters
        ----------
        work_amount:int
            The amount of work currently completed

        Returns
        -------

        """
        try:
            if is_local:
                return

            if work_amount >= 0:
                with DB.connection_context():
                    model = TaskProgress.select().where(
                        TaskProgress.task_id == self._task_id,
                    ).get()

                    model.progress = work_amount
                    model.updated_time = datetime.datetime.now()
                    self.calc_progress(model)
                    model.save()
        except Exception as e:
            self.exception(e)
            self.error(f"set task {self._task_id} progress error as {e} ")

    def add_task_progress(self, step: int = 1):
        """

        Increase progress according to step

        Parameters
        ----------
        step:int

        Returns
        -------

        """
        try:
            if is_local:
                return

            work_amount = 0
            with DB.connection_context():
                model = TaskProgress.select().where(
                    TaskProgress.task_id == self._task_id,
                ).get()
                if model.progress is not None:
                    work_amount = model.progress + step
                else:
                    work_amount = step

                # Reserve one amount for use when the finish call
                if work_amount > model.expect_work_amount - 1:
                    work_amount = model.expect_work_amount - 1

            self.set_task_progress(work_amount)
        except Exception as e:
            self.exception(e)
            logging.error(f"add task {self._task_id} progress error as {e} ")

    def get_task_progress(self):
        """

        get task progress

        Parameters
        ----------

        Returns
        -------

        """
        if is_local:
            return

        with DB.connection_context():
            model = TaskProgress.select().where(
                TaskProgress.task_id == self._task_id,
            ).get()
            if model.progress is not None:
                return model.progress
            else:
                return None


    def finish_task_progress(self):
        """
        Finish task progress

        Returns
        -------

        """
        try:
            if is_local:
                return

            with DB.connection_context():
                model = TaskProgress.get_or_none(
                    TaskProgress.task_id == self._task_id,
                )
                if model:
                    model.progress = model.progress + 1
                    model.really_work_amount = model.progress

                    if model.really_work_amount > model.expect_work_amount:
                        model.really_work_amount = model.expect_work_amount

                    model.updated_time = datetime.datetime.now()
                    self.calc_progress(model)
                    model.pid_success = 1
                    model.save()
        except Exception as e:
            self.exception(e)
            logging.error(f"finish task {self._task_id} progress error as {e} ")