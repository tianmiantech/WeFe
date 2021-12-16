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
from flow.service.visualfl.visualfl_service import VisualFLService
from flow.service.job_scheduler.job_service import JobService
from flow.utils import job_utils


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
        self.logger.info(
            "Task apply resource {}（{}）start，time：{}".format(self.task.task_type, self.task.task_id,
                                                             current_datetime()))
        response = self.apply_resource()
        apply_result = JobApplyResult()
        if response is not None:
            # 等待 apply resource 执行完成
            while apply_result is None or apply_result.server_endpoint is None:
                self.logger.info("Wait apply resource {}（{}）".format(self.task.task_type, self.task.task_id))
                time.sleep(3)
                apply_result = self.query_apply_progress_result()
        else:
            raise RuntimeError(("Task {}（{}）failed, apply resource request error，time：{}".format(self.task.task_type, self.task.task_id, current_datetime())))
        self.logger.info("get apply resource finished, {} {}".format(self.task.task_type, self.task.task_id))
        self.logger.info("apply result = {}".format(apply_result))
        self.logger.info("my_role = {}".format(self.job.my_role))
        result = None
        # send
        if self.job.my_role == 'promoter':
            aggregator_info = {
                'server_endpoint': apply_result.server_endpoint,
                'aggregator_endpoint': apply_result.aggregator_endpoint,
                'aggregator_assignee': apply_result.aggregator_assignee
            }
            task_config_json = json.loads(self.task.task_conf)
            self.logger.info("task_config_json = {}".format(task_config_json))
            members = task_config_json['members']
            for m in members:
                member_id = m['member_id']
                self.logger.info(
                    "send aggregator_info to {}, content is : {}".format(member_id, str(aggregator_info)))
                job_utils.send(dst_member_id=member_id, content_str=str(aggregator_info))
        # receive
        else:
            while result is None:
                self.logger.info("wait aggregator_info")
                result = job_utils.receive()
                time.sleep(3)
            self.logger.info("receive aggregator_info , content is : {}".format(str(result)))
        # append result to apply_result
        if result is not None:
            result_json = json.loads(str(result))
            apply_result.append(result_json)
        self.logger.info("receive aggregator_info , content is : {}".format(apply_result))
        response = self.submit_task(apply_result)
        if response:
            self.logger.info(
                "Task apply resource {}（{}）start，时间：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            # update job progress
            JobService.update_progress(self.job)
            # wait task finished
            while not self.is_task_progress_done():
                self.logger.info("Wait task {}（{}）done".format(self.task.task_type, self.task.task_id))
                time.sleep(3)
        else:
            self.logger.info(
                "Task {}（{}）failed， submit task request error, time：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            self.error_on_task('submit task error')
            raise RuntimeError('submit task error')
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

    def query_apply_progress_result(self):
        return JobApplyResultDao.find_one_by_job_id(self.job.job_id, self.task.task_id)

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
            env = task_config_json['env']
            # todo 将apply_result 填充到 params里面
            env.append(apply_result)
            # todo local_trainer_indexs need_shuffle
            task_config_json['algorithm_config']['need_shuffle'] = True
            task_config_json['env'] = env
            self.log_job_info('submit_task params: {}'.format(task_config_json))
            # submit
            response = VisualFLService.request('submit', task_config_json)
            self.log_job_info('submit response: {}'.format(response))
            submit_task_start_status = response is not None and response['job_id'] is not None
            return response
        except Exception as e:
            schedule_logger(self.running_job).exception(e)
        finally:
            self.log_job_info('success' if submit_task_start_status else 'failed')

    # apply resource
    def apply_resource(self):
        apply_resource_start_status = False
        try:
            params = {
                'job_id': self.job.job_id,
                'task_id': self.task.task_id,
                'job_type': 'paddle_fl',
                'role': self.job.my_role,
                'member_id': GlobalSetting.get_member_id(),
                'callback_url': GlobalSetting.get_flow_base_url().value + '/visualfl/apply_callback'
            }
            task_config_json = json.loads(self.task.task_conf)
            params['env'] = task_config_json['env']
            params['data_set'] = task_config_json['data_set']
            # todo local_trainer_indexs need_shuffle
            params['algorithm_config'] = task_config_json['algorithm_config']
            params['algorithm_config']['need_shuffle'] = True
            self.log_job_info('apply_resource params: {}'.format(params))
            response = VisualFLService.request('apply', params)
            self.log_job_info('apply_resource response: {}'.format(response))
            if response:
                apply_resource_start_status = True
                # self.task.pid = p.pid
                self.task.status = TaskStatus.RUNNING
                self.task.start_time = current_datetime()
                self.task.updated_time = current_datetime()
                TaskDao.save(self.task)
            return response
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
