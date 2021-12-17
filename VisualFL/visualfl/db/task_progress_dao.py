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

import datetime
import logging
from visualfl.db.db_models import DB, Task, TaskProgress
from common.python.utils.core_utils import  get_commit_id


class TaskProgressDao:

    @staticmethod
    def calc_progress(model: TaskProgress) -> TaskProgress:
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

    @staticmethod
    def init_task_progress(task_id: str,work_amount: int):
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
            with DB.connection_context():
                model = TaskProgress.get_or_none(
                    TaskProgress.task_id == task_id,
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
                        Task.task_id == task_id,
                    )
                    if task_info:
                        model.flow_id = task_info.flow_id
                        model.flow_node_id = task_info.flow_node_id
                        model.project_id = task_info.project_id
                        model.job_id = task_info.job_id
                        model.role = task_info.role
                        model.task_type = task_info.task_type
                    else:
                        return

                model.expect_work_amount = work_amount
                TaskProgressDao.calc_progress(model)
                model.save(force_insert=is_insert)
        except Exception as e:
            logging.error(f"init task progress error as {e} ")

    @staticmethod
    def set_task_progress(task_id: str, work_amount: int):
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
            if work_amount >= 0:
                with DB.connection_context():
                    model = TaskProgress.select().where(
                        TaskProgress.task_id == task_id,
                    ).get()

                    model.progress = work_amount
                    model.updated_time = datetime.datetime.now()
                    TaskProgressDao.calc_progress(model)
                    model.save()
        except Exception as e:
            logging.error(f"set task progress error as {e} ")

    @staticmethod
    def add_task_progress(task_id: str, step: int = 1):
        """

        Increase progress according to step

        Parameters
        ----------
        step:int

        Returns
        -------

        """
        try:
            work_amount = 0
            with DB.connection_context():
                model = TaskProgress.select().where(
                    TaskProgress.task_id == task_id,
                ).get()
                if model.progress is not None:
                    work_amount = model.progress + step
                else:
                    work_amount = step

                # Reserve one amount for use when the finish call
                if work_amount > model.expect_work_amount - 1:
                    work_amount = model.expect_work_amount - 1

            TaskProgressDao.set_task_progress(work_amount)
        except Exception as e:
            logging.error(f"add task progress error as {e} ")

    @staticmethod
    def finish_task_progress(task_id):
        """
        Finish task progress

        Returns
        -------

        """
        try:
            with DB.connection_context():
                model = TaskProgress.get_or_none(
                    TaskProgress.task_id == task_id,
                )
                if model:
                    model.progress = model.progress + 1
                    model.really_work_amount = model.progress

                    if model.really_work_amount > model.expect_work_amount:
                        model.really_work_amount = model.expect_work_amount

                    model.updated_time = datetime.datetime.now()
                    TaskProgressDao.calc_progress(model)
                    model.pid_success = 1
                    model.save()
        except Exception as e:
            logging.error(f"finish task progress error as {e} ")
