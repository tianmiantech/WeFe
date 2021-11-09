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
import json
from common.python.common.consts import TaskStatus, JobStatus
from common.python.db.job_dao import JobDao
from common.python.utils.core_utils import current_datetime
from common.python.utils.log_utils import LoggerFactory, schedule_logger
from common.python.db.db_models import Task, Job, GlobalSetting, JobApplyResult
from common.python.db.task_dao import TaskDao
from common.python.db.job_apply_result_dao import JobApplyResultDao
from service.visualfl.visualfl_service import VisualFLService


class RunVisualFLTaskAction:
    logger = LoggerFactory.get_logger("RunVisualFLTaskAction")
    job: Job
    task: Task
    running_job: str

    def __init__(self, job, task) -> None:
        super().__init__()
        self.job = job
        self.task = task
        self.running_job = self.job.job_id + '_' + self.job.my_role

    def do(self):

        # 等待前置任务完毕
        # if not self.wait_for_parents_success():
        #     return
        flag = self.apply_resource()
        apply_result = None
        if flag:
            self.logger.info(
                "Task apply resource {}（{}）start，time：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            # 等待 apply resource 执行完成
            while apply_result is None:
                self.logger.info("Wait apply resource {}（{}）done".format(self.task.task_type, self.task.task_id))
                time.sleep(3)
                apply_result = self.is_apply_progress_done()
        else:
            self.logger.info(
                "Task {}（{}）failed, apply resource request error，time：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            return
        flag = self.submit_task(apply_result)
        if flag:
            self.logger.info(
                "Task apply resource {}（{}）start，时间：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            # # task 执行完毕后更新 job 进度
            # JobService.update_progress(self.job)
            # 等待 task 执行完成
            while not self.is_task_progress_done():
                self.logger.info("Wait task {}（{}）done".format(self.task.task_type, self.task.task_id))
                time.sleep(3)
        else:
            self.logger.info(
                "Task {}（{}）failed， submit task request error, time：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            # todo
            self.error_on_task('submit task error')
            return
        # todo
        self.finish_task()

    def error_on_task(self, message):
        self.task.status = TaskStatus.ERROR
        self.task.start_time = current_datetime()
        self.task.updated_time = current_datetime()
        self.task.message = message
        TaskDao.save(self.task)

        job = JobDao.find_one_by_id(self.job.id)
        job.status = JobStatus.ERROR_ON_RUNNING
        job.status_updated_time = current_datetime()
        job.updated_time = current_datetime()
        job.finish_time = current_datetime()
        job.message = message
        job.save()

    def finish_task(self):
        self.task.status = TaskStatus.SUCCESS
        self.task.start_time = current_datetime()
        self.task.updated_time = current_datetime()
        TaskDao.save(self.task)

        job = JobDao.find_one_by_id(self.job.id)
        job.status = JobStatus.SUCCESS
        job.status_updated_time = current_datetime()
        job.updated_time = current_datetime()
        job.finish_time = current_datetime()
        job.save()

    def is_apply_progress_done(self) -> bool:
        return JobApplyResultDao.find_one_by_job_id(self.job.job_id, self.task.task_id)

    # todo
    def is_task_progress_done(self) -> bool:
        apply_result = JobApplyResultDao.find_one_by_job_id(self.job.job_id, self.task.task_id)
        if apply_result is None or apply_result.status == '待运行' or apply_result.status == '运行中':
            return False
        return True

    # submit task
    def submit_task(self, apply_result: JobApplyResult):
        submit_task_start_status = False
        task_config_json = json.loads(self.task.task_conf)
        try:
            params = task_config_json['params']
            self.log_job_info('submit_task params:' + str(params))
            # todo 申请资源请求
            p = VisualFLService.request('submit', params)
            if p:
                submit_task_start_status = True
                # self.task.pid = p.pid
                self.task.status = TaskStatus.RUNNING
                self.task.start_time = current_datetime()
                self.task.updated_time = current_datetime()
                TaskDao.save(self.task)
            return p
        except Exception as e:
            schedule_logger(self.running_job).exception(e)
        finally:
            self.log_job_info('success' if submit_task_start_status else 'failed')

    # apply resource
    def apply_resource(self):
        apply_resource_start_status = False
        try:
            params = {
                'job_id': '',
                'task_id': '',
                'job_type': 'paddle_fl',
                'role': '',
                'member_id': '',
                'callback_url': ''
            }
            self.log_job_info('apply_resource params:' + str(params))
            # todo 申请资源请求
            p = VisualFLService.request('apply', params)
            if p:
                # todo 这里不一定需要
                apply_resource_start_status = True
                # self.task.pid = p.pid
                self.task.status = TaskStatus.RUNNING
                self.task.start_time = current_datetime()
                self.task.updated_time = current_datetime()
                TaskDao.save(self.task)
            return p
        except Exception as e:
            schedule_logger(self.running_job).exception(e)
        finally:
            self.log_job_info('success' if apply_resource_start_status else 'failed')

    def log_job_info(self, message):
        message = 'job {} on {} {} start task subprocess:{}'.format(
            self.job.job_id,
            self.task.role,
            GlobalSetting.get_member_id(),
            message
        )
        schedule_logger(self.running_job).info(message)
