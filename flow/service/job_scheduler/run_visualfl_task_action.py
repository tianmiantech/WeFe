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
from common.python.db.db_models import Task, Job, GlobalSetting, JobApplyResult
from common.python.db.task_dao import TaskDao
from common.python.db.job_apply_result_dao import JobApplyResultDao
from flow.service.visualfl.visualfl_service import VisualFLService
from flow.service.job_scheduler.job_service import JobService
from flow.utils import job_utils
from common.python.utils.log_utils import schedule_logger


class RunVisualFLTaskAction:
    job: Job
    task: Task
    running_job: str

    def __init__(self, job, task) -> None:
        super().__init__()
        self.job = job
        self.task = task
        self.running_job = self.job.job_id + '_' + self.job.my_role

    def do(self):
        schedule_logger(self.running_job).info(
            "Task apply resource {}（{}）start，time：{}".format(self.task.task_type, self.task.task_id,
                                                             current_datetime()))
        session_id = self.job.job_id + "_visual_fl_aggregator_info"
        apply_result = JobApplyResult()
        # send
        if self.job.my_role == 'promoter':
            response = self.apply_resource()
            if response is not None:
                # wait apply resource
                schedule_logger(self.running_job).info(
                    "Wait apply resource {}（{}）".format(self.task.task_type, self.task.task_id))
                while apply_result is None or apply_result.server_endpoint is None:
                    time.sleep(3)
                    apply_result = self.query_apply_progress_result()
                schedule_logger(self.running_job).info("Wait apply resource finished, apply_result={}".format(str(apply_result)))
            else:
                raise RuntimeError(("Task {}（{}）failed, apply resource request error，time：{}".format(
                    self.task.task_type, self.task.task_id, current_datetime())))
            aggregator_info = {
                "server_endpoint": str(apply_result.server_endpoint),
                "aggregator_endpoint": str(apply_result.aggregator_endpoint),
                "aggregator_assignee": str(apply_result.aggregator_assignee)
            }
            task_config_json = json.loads(self.task.task_conf)
            schedule_logger(self.running_job).info("task_config_json = {}".format(task_config_json))
            members = task_config_json['members']
            for m in members:
                member_id = m['member_id']
                if member_id == GlobalSetting.get_member_id():
                    continue
                schedule_logger(self.running_job).info(
                    "send aggregator_info to {}, content is : {}".format(member_id, json.dumps(aggregator_info)))
                job_utils.send_fl(dst_member_id=member_id, processor="residentMemoryProcessor", content_str=json.dumps(aggregator_info), session_id = session_id)
        # receive
        else:
            result = None
            schedule_logger(self.running_job).info("wait aggregator_info")
            while result is None or len(result) <= 10:
                result = job_utils.receive_fl(session_id=session_id)
                time.sleep(3)
            schedule_logger(self.running_job).info("receive aggregator_info , content is : {}".format(result))
            if result is not None:
                result_json = json.loads(result)
                apply_result.server_endpoint = result_json['server_endpoint']
                apply_result.aggregator_endpoint = result_json['aggregator_endpoint']
                apply_result.aggregator_assignee = result_json['aggregator_assignee']
        schedule_logger(self.running_job).info("begin submit_task : {},{}".format(self.job.job_id, self.job.my_role))
        response = self.submit_task(apply_result)
        if response:
            schedule_logger(self.running_job).info(
                "Task apply resource {}（{}）start，时间：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            # update job progress
            JobService.update_progress(self.job)
            # wait task finished
            schedule_logger(self.running_job).info(
                "Wait task {}（{}）done".format(self.task.task_type, self.task.task_id))
            while not self.is_task_progress_done():
                time.sleep(3)
        else:
            schedule_logger(self.running_job).info(
                "Task {}（{}）failed， submit task request error, time：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            # self.error_on_task('submit task error')
            raise RuntimeError('submit task error')
        schedule_logger(self.running_job).info("task {}（{}）done".format(self.task.task_type, self.task.task_id))
        JobService.update_progress(self.job)
        # self.finish_task()

    def error_on_task(self, message):
        # self.task.status = TaskStatus.ERROR
        # self.task.start_time = current_datetime()
        # self.task.updated_time = current_datetime()
        # self.task.message = message
        # TaskDao.save(self.task)
        job = JobDao.find_one_by_id(self.job.id)
        job.status = JobStatus.ERROR_ON_RUNNING
        job.status_updated_time = current_datetime()
        job.updated_time = current_datetime()
        job.finish_time = current_datetime()
        job.message = message
        job.save()

    def finish_task(self):
        # self.task.status = TaskStatus.SUCCESS
        # self.task.start_time = current_datetime()
        # self.task.updated_time = current_datetime()
        # TaskDao.save(self.task)
        job = JobDao.find_one_by_id(self.job.id)
        job.status = JobStatus.SUCCESS
        job.status_updated_time = current_datetime()
        job.updated_time = current_datetime()
        job.finish_time = current_datetime()
        job.save()

    def query_apply_progress_result(self):
        return JobApplyResultDao.find_one_by_job_id(self.job.job_id, self.task.task_id)

    def is_task_progress_done(self) -> bool:
        # apply_result = JobApplyResultDao.find_one_by_job_id(self.job.job_id, self.task.task_id)
        # if apply_result is None or apply_result.status == 'wait_run' or apply_result.status == 'running':
        #     return False
        # return True
        self.task = TaskDao.find_one_by_task(self.task)
        if self.task.status != TaskStatus.WAITRUN and self.task.status != TaskStatus.RUNNING:
            return True
        return False

    # submit task
    def submit_task(self, apply_result: JobApplyResult):
        submit_task_start_status = False
        task_config_json = json.loads(self.task.task_conf)
        try:
            task_config_json['env']['aggregator_endpoint'] = apply_result.aggregator_endpoint
            task_config_json['env']['aggregator_assignee'] = apply_result.aggregator_assignee
            task_config_json['env']['server_endpoint'] = apply_result.server_endpoint
            # task_config_json['algorithm_config']['need_shuffle'] = True
            schedule_logger(self.running_job).info('submit_task request: {}'.format(task_config_json))
            # submit
            response = VisualFLService.request('submit', task_config_json)
            schedule_logger(self.running_job).info('submit response: {}'.format(response))
            submit_task_start_status = response is not None
            return response
        except Exception as e:
            schedule_logger(self.running_job).exception(e)
        finally:
            schedule_logger(self.running_job).info('success' if submit_task_start_status else 'failed')

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
            params['algorithm_config'] = task_config_json['algorithm_config']
            # params['algorithm_config']['need_shuffle'] = True
            schedule_logger(self.running_job).info('apply_resource request : {}'.format(params))
            # return job_id
            response = VisualFLService.request('apply', params)
            schedule_logger(self.running_job).info('apply_resource response: {}'.format(response))
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
            schedule_logger(self.running_job).info('success' if apply_resource_start_status else 'failed')

    def log_job_info(self, message):
        message = 'job {} on {} {} start task subprocess:{}'.format(
            self.job.job_id,
            self.task.role,
            GlobalSetting.get_member_id(),
            message
        )
        schedule_logger(self.running_job).info.info(message)
