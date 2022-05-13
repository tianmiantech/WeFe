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

# Copyright 2019 The FATE Authors. All Rights Reserved.
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

#
import argparse
import importlib
import os
import pickle
import re
import sys
import traceback

from common.python import federation
from common.python import session
from common.python.common import consts
from common.python.common.consts import TaskStatus, ComponentName, FederatedLearningType, DataSetType, \
    FunctionConfig, RuntimeOptionKey
from common.python.common.exception.custom_exception import *
from common.python.common.global_config import global_config
from common.python.db.db_models import DB, Job, Task
from common.python.utils import log_utils, conf_utils, file_utils
from common.python.utils.core_utils import current_datetime
from common.python.utils.log_utils import schedule_logger
from common.python.utils.store_type import DBTypes
from flow.service.job_scheduler.task_heartbeat_service import TaskHeartbeatService
from kernel.tracker.parameter_util import ParameterUtil
from kernel.tracker.runtime_config import RuntimeConfig
from kernel.tracker.tracking import Tracking
from kernel.utils.decorator_utils import load_config, update_task_status_env

class TaskExecutor(object):

    @staticmethod
    def run_task():
        try:
            parser = argparse.ArgumentParser()
            parser.add_argument('-j', '--job_id', required=True, type=str, help="job id")
            parser.add_argument('-n', '--component_name', required=True, type=str,
                                help="component name")
            parser.add_argument('-t', '--task_id', required=True, type=str, help="task id")
            parser.add_argument('-r', '--role', required=True, type=str, help="role")
            parser.add_argument('-m', '--member_id', required=True, type=str, help="member id")
            parser.add_argument('-c', '--config', required=True, type=str, help="task config")
            parser.add_argument('-s', '--federation_session_id', required=False, type=str, help="federation_session_id")
            parser.add_argument('-e', '--environment', required=False, type=str, help="running environment")
            args = parser.parse_args()
            schedule_logger(args.job_id).info('enter task process')
            schedule_logger(args.job_id).info(args)
            job_id = args.job_id
            component_name = args.component_name
            task_id = args.task_id
            role = args.role
            member_id = args.member_id
            global_config.ENV = args.environment
            task_config = load_config(args)
            print(f'task_config： {task_config}')
            params = task_config.get('params', {})

            # 改为从 job_config 中获取
            with DB.connection_context():
                job = Job.select().where(Job.job_id == job_id)
            job_env = job['env']
            task_input_dsl = task_config['input']
            task_output_dsl = task_config['output']
            module_name = task_config['module']
            project_id = task_config['job']['project']['project_id']

            parameters = TaskExecutor.get_parameters(role, member_id, module_name, component_name, task_config)

            # Start thread, send heartbeat
            TaskHeartbeatService(task_id).start()
        except Exception as e:
            exc_type, exc_value, exc_traceback = sys.exc_info()
            error = str(repr(traceback.format_exception(exc_type, exc_value, exc_traceback)))  # 将异常信息转为字符串
            message = TaskExecutor.get_error_message(exc_value, e)
            TaskExecutor.update_task_status(job_id, role, task_id, TaskStatus.ERROR, message=message, error_cause=error)

            traceback.print_exc()
            schedule_logger().exception(e)
            return
        try:
            schedule_logger().info(
                'update task status to running , job_id = {}, role={}, task_id={}'.format(job_id, role, task_id))
            TaskExecutor.update_task_status(job_id, role, task_id, TaskStatus.RUNNING)
            # backend = conf_utils.get_backend_from_string(
            #     conf_utils.get_comm_config(consts.COMM_CONF_KEY_BACKEND)
            # )
            backend = job_env['calculation_engine_config'].get('backend')
            # backend = 0
            options = TaskExecutor.session_options(task_config)
            RuntimeConfig.init_config(WORK_MODE=job_env['work_mode'],
                                      BACKEND=backend,
                                      DB_TYPE=job_env['storage_config'].get('db_type', DBTypes.CLICKHOUSE))
            session.init(job_id='{}_{}_{}'.format(task_id, role, member_id), mode=RuntimeConfig.WORK_MODE,
                         backend=RuntimeConfig.BACKEND, db_type=RuntimeConfig.DB_TYPE,
                         options=options)
            federation.init(job_id=args.federation_session_id or task_id, runtime_conf=parameters)
            job_log_dir = os.path.join(Tracking.get_job_log_directory(job_id=job_id), role, str(member_id))
            task_log_dir = os.path.join(job_log_dir, component_name)
            log_utils.LoggerFactory.set_directory(directory=task_log_dir, parent_log_dir=job_log_dir,
                                                  append_to_parent_log=True, force=True)
            # version 2 model_id -> task_id | model_version -> job_id
            tracker = Tracking(project_id=project_id, job_id=job_id, role=role, member_id=member_id,
                               model_id=task_id, model_version=job_id,
                               component_name=component_name, module_name=module_name, task_id=task_id)
            run_class_paths = parameters.get('CodePath').replace("\\", "/").split('/')
            run_class_package = '.'.join(run_class_paths[:-2]) + '.' + run_class_paths[-2].replace('.py', '')
            run_class_name = run_class_paths[-1]

            # Obtain the input data according to the rules
            task_run_args = TaskExecutor.get_task_run_args(
                project_id=project_id, job_id=job_id, role=role, task_id=task_id,
                member_id=member_id, params=params,
                module_name=module_name, input_dsl=task_input_dsl
            )

            schedule_logger().info(
                'job_id==> {} , role==>{} , member_id==>{} , job_env==>{} , input_dsl==>{}, options===>{}'.format(
                    job_id, role, member_id, job_env, task_input_dsl, options))
            run_object = getattr(importlib.import_module(run_class_package), run_class_name)()
            run_object.set_tracker(tracker=tracker)

            schedule_logger().info('run {} {} {} {} {} task'.format(job_id, component_name, task_id, role, member_id))
            schedule_logger().info(parameters)
            schedule_logger().info(task_input_dsl)

            run_object.run(parameters, task_run_args)

            output_data = run_object.output_data()
            tracker.set_is_serving_model(run_object.is_serving_model)
            tracker.set_show_name(run_object.show_name)
            tracker.set_source_type(run_object.source_type)
            if output_data is None or not isinstance(output_data, list):
                output_data = [output_data]
            for index, value in enumerate(output_data):
                tracker.save_output_data_table(value, task_output_dsl.get('data')[index] if task_output_dsl.get(
                    'data') else 'component', save_dataset=run_object.save_dataset)
            output_model = run_object.export_model()
            # There is only one model output at the current dsl version.
            tracker.save_output_model(output_model,
                                      task_output_dsl['model'][0] if task_output_dsl.get('model') else 'result',
                                      task_output_dsl.get('data')[0] if task_output_dsl.get('data') else 'result',
                                      save_to_storage=run_object.model_save_to_storage)

            # Federation task status synchronization
            run_object.status_sync(task_config)

            # task finish
            tracker.finish_task_progress()

            # task status
            tracker.set_task_success()

        except Exception as e:
            exc_type, exc_value, exc_traceback = sys.exc_info()
            error_cause = str(repr(traceback.format_exception(exc_type, exc_value, exc_traceback)))  # 堆栈
            message = TaskExecutor.get_error_message(exc_value, e)

            TaskExecutor.update_task_status(job_id, role, task_id, TaskStatus.ERROR, message=message,
                                            error_cause=error_cause)

            kill_path = os.path.join(Tracking.get_job_directory(job_id), str(role), str(member_id), component_name,
                                     'kill')
            if not os.path.exists(kill_path):
                traceback.print_exc()
                schedule_logger().exception(e)

    @staticmethod
    def get_parameters(role, member_id, module_name, component_name, runtime_conf):
        component_root = os.path.join(file_utils.get_project_base_directory(), 'kernel', 'components')
        module_name_dir = TaskExecutor.generate_module_name_dir(module_name,runtime_conf)

        component_full_path = None
        if os.path.exists(os.path.join(component_root, module_name_dir)):
            component_full_path = os.path.join(component_root, module_name_dir)
        else:
            component_full_path = file_utils.match_dir(component_root, module_name_dir)

        if not component_full_path:
            raise Exception(f'{module_name}不存在!')

        component_rel_path = component_full_path.replace(file_utils.get_project_base_directory(), '')[1:]
        parameter = ParameterUtil.override_parameter(runtime_conf,
                                                     module_name,
                                                     component_name,
                                                     role, member_id,
                                                     component_full_path,
                                                     component_rel_path
                                                     )
        return parameter

    @staticmethod
    def get_task_run_args(project_id, job_id, role, task_id, member_id, params, module_name, input_dsl):
        task_run_args = {}
        # input_dsl => {'data': {'': ['']}, 'model': {'': ['']}}
        for input_type, input_detail in input_dsl.items():
            if input_type == 'data':
                this_type_args = task_run_args[input_type] = task_run_args.get(input_type, {})
                if len(input_detail.keys()) == 0 and module_name in (ComponentName.DATA_IO, ComponentName.OOT):
                    data_table = session.table(
                        namespace=params['namespace'],
                        name=params['name']
                    )
                    args_from_component = this_type_args["upload"] = this_type_args.get(
                        "upload", {})
                    args_from_component[DataSetType.NORMAL_DATA_SET] = data_table

                else:
                    """
                    data_type
                        - normal : normal data
                        - train : train data
                        - evaluation : evaluation data
                        - result : 
                    data_list
                        - task_name
                    """
                    for data_type, data_list in input_detail.items():
                        for data_key in data_list:
                            # data_key_item = data_key.split('.')
                            search_component_name, search_data_name = data_key, data_type
                            data_table = Tracking(project_id, job_id=job_id, role=role, member_id=member_id,
                                                  component_name=search_component_name).get_output_data_table(
                                data_name=search_data_name)
                            args_from_component = this_type_args[search_component_name] = this_type_args.get(
                                search_component_name, {})
                            args_from_component[data_type] = data_table

            elif input_type == "model":
                # if input_type == "model":
                this_type_args = task_run_args[input_type] = task_run_args.get(input_type, {})
                # else:
                #     this_type_args = task_run_args[input_type] = task_run_args.get(input_type, {})
                for dsl_model_key in input_detail:
                    # dsl_model_key_items = dsl_model_key.split('.')
                    # if len(dsl_model_key_items) == 1:
                    #     search_component_name, search_model_name = dsl_model_key_items[0], "default"
                    # elif len(dsl_model_key_items) == 2:
                    search_component_name, search_model_name = input_detail[dsl_model_key][0], dsl_model_key
                    # else:
                    # raise Exception('get input {} failed'.format(input_type))
                    models = Tracking(project_id=project_id, job_id=job_id, role=role, member_id=member_id,
                                      component_name=search_component_name,
                                      model_id=task_id, model_version=job_id).get_output_model(
                        model_name=search_model_name)
                    this_type_args[search_model_name] = [models]
        return task_run_args

    @staticmethod
    @update_task_status_env()
    def update_task_status(job_id, role, task_id, status, message=None, error_cause=None):
        with DB.connection_context():
            jobs = Job().select().where(Job.job_id == job_id, Job.my_role == role)

            if jobs:
                job = jobs[0]
                tasks = Task().select().where(
                    Task.task_id == task_id,
                    Task.job_id == job.job_id,
                    Task.role == job.my_role)
                if tasks:
                    task = tasks[0]
                    task.status = status
                    task.message = message
                    task.error_cause = error_cause
                    task.updated_time = current_datetime()
                    task.save()

    @staticmethod
    def generate_module_name_dir(name, runtime_conf):
        """

        Generate the real catalog of the component according to the horizontal and vertical properties of the component

        Parameters
        ----------
        name:str
            Incoming component name
        train_type:str
            Training type: horizontal / vertical

        Returns
        -------

        """
        train_type = runtime_conf["job"]["federated_learning_type"]
        if name == ComponentName.BINNING:
            if train_type == FederatedLearningType.VERTICAL:
                return ComponentName.VERT_FEATURE_BINNING.lower()
            else:
                return ComponentName.HORZ_FEATURE_BINNING.lower()
        elif name == ComponentName.FEATURE_CALCULATION:
            if train_type == FederatedLearningType.VERTICAL:
                return ComponentName.VERT_FEATURE_CALCULATION.lower()
            else:
                raise ValueError("The HorzFeatureCalculation Does't Support Yet.")
        elif name == ComponentName.VERT_SECURE_BOOST:
            work_mode = runtime_conf["params"].get("work_mode")
            if work_mode is None or work_mode == "normal":
                return name.lower()
            elif work_mode == 'dp':
                return ComponentName.VERT_DP_SECURE_BOOST.lower()
            else:
                return ComponentName.VERT_FAST_SECURE_BOOST.lower()

        else:
            return name.lower()

    @staticmethod
    def session_options(task_config: dict):
        options = {}
        fc_partition_key = RuntimeOptionKey.FC_PARTITION
        spark_partition_key = RuntimeOptionKey.SPARK_PARTITION
        features_count_key = RuntimeOptionKey.FEATURE_COUNT

        # default partition
        default_partitions = FunctionConfig.FC_DEFAULT_PARTITION
        # max partition
        max_partitions = FunctionConfig.FC_MAX_PARTITION
        options[fc_partition_key] = default_partitions

        # Calculate the number of function shards based on the data set,
        # and ensure that multiple parties are consistent
        job_config = task_config["job"]
        if job_config and "data_sets" in job_config:
            data_sets = job_config["data_sets"]
            # The average amount of data processed by each function shard
            default_size = FunctionConfig.FC_PARTITION_DATA_SIZE

            min_rows = 0
            features_count = 0
            for component_dataset in data_sets:
                if component_dataset["component_type"] in (ComponentName.DATA_IO, ComponentName.OOT):
                    for member in component_dataset["members"]:
                        member_dataset_row = member["data_set_rows"]
                        features_count += member["data_set_features"] if "data_set_features" in member else 0
                        if member_dataset_row < min_rows or min_rows == 0:
                            min_rows = member_dataset_row

            fc_partitions = int(
                min_rows / default_size if min_rows % default_size == 0 else min_rows / default_size + 1)
            options[features_count_key] = features_count

            if fc_partitions > max_partitions:
                options[fc_partition_key] = max_partitions
            elif fc_partitions > 0:
                options[fc_partition_key] = fc_partitions

        # at present, the two parameters are consistent
        options[spark_partition_key] = options[fc_partition_key]

        # members_backend
        options[RuntimeOptionKey.MEMBERS_BACKEND] = TaskExecutor.parse_members_backend(task_config)

        return options

    @staticmethod
    def parse_members_backend(task_config: dict):
        members_backend = {}
        job_config = task_config["job"]
        members = job_config.get("members")
        for member in members:
            members_backend[member["member_id"]] = member.get("backend")
        return members_backend

    @staticmethod
    def get_error_message(exc_value, e: Exception):
        message = str(exc_value)
        # convert to CustomBaseException
        if isinstance(e, pickle.PickleError):
            e = PickleError()
        elif "NoneType" in message:
            e = NoneTypeError()
        elif "NaN" in message:
            e = NaNTypeError()
        elif "spark" in message or "Py4J" in message:
            pattern = re.compile('raise .*(.*)')
            result = re.search(pattern, message)
            if result is not None:
                e = SparkError(message=result.group(0))
            else:
                e = SparkError(message)
        elif isinstance(e, TypeError):
            e = CustomTypeError()

        if not isinstance(e, CustomBaseException):
            e = CommonCustomError(message="task执行异常：" + message)

        if isinstance(e, CustomBaseException):
            param_str = ''
            if e.kwargs:
                param_str = '\n参数信息:'
                for k, v in e.kwargs.items():
                    param_str += str(k) + '=' + str(v) + ','
                param_str = param_str[:-1]

            message = f'[{e.code}] {e.message} {param_str}'

        return message


if __name__ == '__main__':
    TaskExecutor.run_task()
