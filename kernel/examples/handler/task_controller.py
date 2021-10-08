# Copyright 2021 The WeFe Authors. All Rights Reserved.
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
import subprocess
import time

from common.python import Backend
from common.python.db.db_models import *
from common.python.utils import conf_utils, file_utils
from kernel.task_executor import TaskExecutor

PYTHON = "python3"


class TaskController(object):

    @staticmethod
    def run_job(job_id, role, member_id, task_configs):

        for component_name, task_config in task_configs.items():
            task_config = json.dumps(task_config)
            task_id = job_id + '_' + component_name

            process_cmd = [
                PYTHON,
                sys.modules[TaskExecutor.__module__].__file__,
                '-j', job_id,
                '-n', component_name,
                '-t', task_id,
                '-r', role,
                '-m', member_id,
                '-c', task_config
            ]

            task_log_dir = os.path.join(TaskController.get_job_log_directory(job_id=job_id), role,
                                        member_id,
                                        component_name)
            print("run subprocess command:" + " ".join(process_cmd))
            p = TaskController.run_subprocess(process_cmd=process_cmd, log_dir=task_log_dir)

            while p.poll() is None:
                # print("Still working...")
                time.sleep(1)

    @staticmethod
    def run_task(backend, job_id, role, member_id, component_name, task_config_json):

        task_config = json.dumps(task_config_json)
        task_id = job_id + '_' + component_name

        backend = Backend(backend)

        if backend.is_local() or backend.is_fc():
            process_cmd = [
                PYTHON,
                sys.modules[TaskExecutor.__module__].__file__,
                '-j', job_id,
                '-n', component_name,
                '-t', task_id,
                '-r', role,
                '-m', member_id,
                '-c', task_config
            ]
        elif backend.is_spark():
            if "SPARK_HOME" not in os.environ:
                raise EnvironmentError("SPARK_HOME not found")

            # default_submit_params
            default_driver_memory = conf_utils.get_comm_config(
                consts.COMM_CONF_KEY_SPARK_DEFAULT_DRIVER_MEMORY)
            default_driver_max_result_size = conf_utils.get_comm_config(
                consts.COMM_CONF_KEY_SPARK_DEFAULT_DRIVER_MAX_RESULT_SIZE, "2g")
            default_num_executors = int(
                conf_utils.get_comm_config(consts.COMM_CONF_KEY_SPARK_DEFAULT_NUM_EXECUTORS))
            default_executor_memory = conf_utils.get_comm_config(
                consts.COMM_CONF_KEY_SPARK_DEFAULT_EXECUTOR_MEMORY)
            default_executor_cores = int(
                conf_utils.get_comm_config(consts.COMM_CONF_KEY_SPARK_DEFAULT_EXECUTOR_CORES))
            if role == 'arbiter':
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
            process_cmd = [
                spark_submit_cmd,
                f'--name={task_id}#{role}',
                f'--deploy-mode={deploy_mode}',
                f'--queue={queue}',
                f'--driver-memory={driver_memory}',
                f'--num-executors={num_executors}',
                f'--executor-memory={executor_memory}',
                f'--executor-cores={executor_cores}',
                f'--total-executor-cores={total_executor_cores}',
                f'--conf=spark.driver.maxResultSize={default_driver_max_result_size}',
                sys.modules[TaskExecutor.__module__].__file__,
                '-j', job_id,
                '-n', component_name,
                '-t', task_id,
                '-r', role,
                '-m', member_id,
                '-c', task_config
            ]
        else:
            raise ValueError(f"${backend} supported")

        task_log_dir = os.path.join(TaskController.get_job_log_directory(job_id=job_id), role,
                                    member_id,
                                    component_name)
        print("run subprocess command:" + " ".join(process_cmd))
        p = TaskController.run_subprocess(process_cmd=process_cmd, log_dir=task_log_dir)

        while p.poll() is None:
            # print("Still working...")
            time.sleep(1)

    @staticmethod
    def run_subprocess(process_cmd, log_dir=None):
        stat_logger.info('Starting process command: {}'.format(process_cmd))
        stat_logger.info(' '.join(process_cmd))

        if log_dir:
            os.makedirs(log_dir, exist_ok=True)
        std_log = open(os.path.join(log_dir, 'std.log'), 'w')

        if os.name == 'nt':
            startupinfo = subprocess.STARTUPINFO()
            startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
            startupinfo.wShowWindow = subprocess.SW_HIDE
        else:
            startupinfo = None
        p = subprocess.Popen(process_cmd,
                             stdout=std_log,
                             stderr=std_log,
                             startupinfo=startupinfo
                             )
        return p

    def get_job_log_directory(job_id):
        return os.path.join(file_utils.get_project_base_directory(), 'logs', job_id)
