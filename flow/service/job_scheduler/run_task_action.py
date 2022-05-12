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
import os
import sys
import time

from common.python import Backend
from common.python.common import consts
from common.python.common.consts import JobStatus, TaskStatus
from common.python.db.db_models import Task, Job, GlobalSetting
from common.python.db.job_dao import JobDao
from common.python.db.task_dao import TaskDao
from common.python.db.task_progress_dao import TaskProgressDao
from common.python.utils import conf_utils
from common.python.utils.core_utils import current_datetime
from common.python.utils.log_utils import LoggerFactory, schedule_logger
from flow.service.job_scheduler.job_service import JobService
from flow.settings import MemberInfo
from flow.utils import job_utils
from kernel.task_executor import TaskExecutor

MEMBER_ID = MemberInfo.MEMBER_ID


class RunTaskAction:
    logger = LoggerFactory.get_logger("RunTaskAction")
    job: Job
    task: Task
    running_job: str

    def __init__(self, job, task) -> None:
        super().__init__()
        self.job = job
        self.task = task
        self.running_job = self.job.job_id + '_' + self.job.my_role

    def do(self):

        # Wait for the predecessor tasks to success
        if not self.wait_for_parents_success():
            return

        self.logger.info("Task {}（{}）满足执行条件，时间：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
        subprocess = self.start_subprocess()
        if subprocess:
            self.logger.info(
                "Task {}（{}）已启动进程，时间：{}".format(self.task.task_type, self.task.task_id, current_datetime()))
            # Wait for task execution to complete
            while subprocess.poll() is None and not self.is_task_progress_done():
                self.logger.info("Wait Task {}（{}）done".format(self.task.task_type, self.task.task_id))
                time.sleep(3)

            # Update job progress after task execution
            JobService.update_progress(self.job)

    def start_subprocess(self):
        """
        start child process
        """
        task_process_start_status = False
        try:

            self.log_job_info('task_conf:' + self.task.task_conf)

            # assemble the startup command
            process_cmd = self.build_process_cmd()
            # assemble task_dir
            task_dir = os.path.join(
                job_utils.get_job_directory(job_id=self.job.job_id),
                self.task.role,
                MEMBER_ID,
                self.task.name
            )
            # assemble task_log_dir
            task_log_dir = os.path.join(
                job_utils.get_job_log_directory(job_id=self.job.job_id),
                self.job.my_role,
                MEMBER_ID,
                self.task.task_type.lower()
            )

            self.log_job_info('task_dir:' + task_dir)
            self.log_job_info('process_cmd:' + str(process_cmd))
            self.log_job_info('task_log_dir:' + task_log_dir)

            # start process
            p = job_utils.run_subprocess(config_dir=task_dir, process_cmd=process_cmd, log_dir=task_log_dir)
            if p:
                task_process_start_status = True

                TaskDao.update_by_id({
                    Task.status: TaskStatus.RUNNING,
                    Task.pid: p.pid,
                    Task.start_time: current_datetime(),
                    Task.updated_time: current_datetime()},
                    self.task.task_id)

            return p
        except Exception as e:
            schedule_logger(self.running_job).exception(e)
        finally:
            self.log_job_info('success' if task_process_start_status else 'failed')

    def is_task_progress_done(self) -> bool:
        """
        Judge whether the task is over through task_progress
        """
        task_progress = TaskProgressDao.find_one_by_task(self.task)
        if task_progress is None:
            return False
        elif task_progress.pid_success is None:
            return False
        elif task_progress.pid_success == 0:
            return False

        return True

    def build_federation_session_id(self):
        """
        build federation session id

        Because the task_id of the task table has added role information,
        the task_id of multiple parties is inconsistent.
        so flow regenerates a unique federation_session_id in the original format,
        which is used for multi-party interaction session_id.

        Returns
        -------
        The session id
        """

        return f'{self.task.job_id}_{self.task.task_type}_{self.task.flow_node_id}'

    def build_process_cmd_for_local_or_fc(self):
        """
        splicing the startup command for function calculation
        """
        return [
            'python3',
            sys.modules[TaskExecutor.__module__].__file__,
            '-j', self.job.job_id,
            '-n', self.task.name,
            '-t', self.task.task_id,
            '-r', self.job.my_role,
            '-m', MEMBER_ID,
            '-c', self.task.task_conf,
            '-s', self.build_federation_session_id()
        ]

    def build_process_cmd_for_spark(self, task_config_json):
        """
        splicing the startup command for spark
        """
        if "SPARK_HOME" not in os.environ:
            raise EnvironmentError("SPARK_HOME not found")

        # default_submit_params
        default_driver_memory = conf_utils.get_comm_config(consts.COMM_CONF_KEY_SPARK_DEFAULT_DRIVER_MEMORY)
        default_driver_max_result_size = conf_utils.get_comm_config(
            consts.COMM_CONF_KEY_SPARK_DEFAULT_DRIVER_MAX_RESULT_SIZE, "2g")
        default_num_executors = int(conf_utils.get_comm_config(consts.COMM_CONF_KEY_SPARK_DEFAULT_NUM_EXECUTORS, 1))
        default_executor_memory = conf_utils.get_comm_config(consts.COMM_CONF_KEY_SPARK_DEFAULT_EXECUTOR_MEMORY)
        default_executor_cores = int(conf_utils.get_comm_config(consts.COMM_CONF_KEY_SPARK_DEFAULT_EXECUTOR_CORES, 1))
        if self.task.role == 'arbiter':
            default_num_executors = 1 if default_num_executors < 4 else int(default_num_executors / 4)
        default_total_executor_cores = default_num_executors * default_executor_cores

        spark_submit_config = task_config_json['job']['env'].get("spark_submit_config", dict())
        deploy_mode = spark_submit_config.get("deploy-mode", "client")
        queue = spark_submit_config.get("queue", "default")
        driver_memory = spark_submit_config.get("driver-memory", default_driver_memory)
        num_executors = spark_submit_config.get("num-executors", default_num_executors)
        executor_memory = spark_submit_config.get("executor-memory", default_executor_memory)
        executor_cores = spark_submit_config.get("executor-cores", default_executor_cores)
        total_executor_cores = spark_submit_config.get("total_executor_cores",
                                                       default_total_executor_cores)

        if deploy_mode not in ["client"]:
            raise ValueError(f"deploy mode {deploy_mode} not supported")
        spark_home = os.environ["SPARK_HOME"]
        spark_submit_cmd = os.path.join(spark_home, "bin/spark-submit")
        return [
            spark_submit_cmd,
            f'--name={self.task.task_id}#{self.task.role}',
            f'--deploy-mode={deploy_mode}',
            f'--queue={queue}',
            f'--driver-memory={driver_memory}',
            f'--num-executors={num_executors}',
            f'--executor-memory={executor_memory}',
            f'--executor-cores={executor_cores}',
            f'--total-executor-cores={total_executor_cores}',
            f'--conf=spark.driver.maxResultSize={default_driver_max_result_size}',
            sys.modules[TaskExecutor.__module__].__file__,
            '-j', self.job.job_id,
            '-n', self.task.name,
            '-t', self.task.task_id,
            '-r', self.task.role,
            '-m', MEMBER_ID,
            '-c', self.task.task_conf,
            '-s', self.build_federation_session_id()
        ]

    def build_process_cmd(self):
        """
        splicing the startup command
        """
        job_config_json = json.loads(self.job.job_config)
        backend = Backend.get_by_task_config(job_config_json)

        if backend.is_local() or backend.is_fc():
            process_cmd = self.build_process_cmd_for_local_or_fc()
        elif backend.is_spark():
            process_cmd = self.build_process_cmd_for_spark(task_config_json)
        else:
            raise ValueError(f"${backend} supported")

        print("run subprocess command:" + " ".join(process_cmd))
        return process_cmd

    def wait_for_parents_success(self) -> bool:
        """
        Wait for the parent node to finish executing.

        This is a wait without a timeout period until the job is not in the running state
        or the parent node is executed.
        """
        while True:
            # If the job is not in the running state, it will jump out and stop running.
            self.job = JobDao.find_one_by_id(self.job.id)
            if self.job.status != JobStatus.RUNNING:
                return False

            # If the task is not in the waiting state, jump out and stop running.
            self.task = TaskDao.find_one_by_task(self.task)
            if self.task.status != TaskStatus.WAITRUN:
                return False

            # If all the parent nodes succeed, the waiting is finished.
            parent_list = TaskDao.find_parents(self.task)
            if not any(x.status != TaskStatus.SUCCESS for x in parent_list):
                return True

            time.sleep(3)

    def log_job_info(self, message):
        message = 'job {} component {} on {} {} start task subprocess:{}'.format(
            self.job.job_id,
            self.task.task_type,
            self.task.role,
            MEMBER_ID,
            message
        )
        schedule_logger(self.running_job).info(message)
