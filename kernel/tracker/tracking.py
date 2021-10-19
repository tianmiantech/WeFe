# -*- coding: utf-8 -*-

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
import json
import os

import numpy as np
from google.protobuf.json_format import MessageToJson

from common.python import RuntimeInstance
from common.python import session
from common.python.calculation.fc.fc_source import FCSource
from common.python.calculation.fc.fc_storage import FCStorage
from common.python.common.consts import NAMESPACE, TaskResultDataType, \
    ProjectStatus, ModelType, TaskStatus
from common.python.common.enums import FlowQueueActionType
from common.python.db.data_set_dao import DataSetDao
from common.python.db.db_models import *
from common.python.db.job_member_dao import JobMemberDao
from common.python.db.project_dao import ProjectDao
from common.python.db.project_data_set_dao import ProjectDataSetDao
from common.python.db.task_dao import TaskDao
from common.python.db.task_progress_dao import TaskProgressDao
from common.python.db.flow_action_queue_dao import FlowActionQueueDao
from common.python.db.task_result_dao import TaskResultDao
from common.python.db.current_best_model_dao import CurrentBestModelDao
from common.python.db.provider_model_param_dao import ProviderModelParamsDao
from common.python.db.job_dao import JobDao
from common.python.protobuf.pyproto import default_empty_fill_pb2
from common.python.utils import file_utils
from common.python.utils.core_utils import current_datetime, timestamp_to_date, get_commit_id, md5, get_delta_seconds
from kernel.tracker import model_manager
from kernel.tracker import model_utils
from kernel.utils.decorator_utils import update_task_status_env

LOGGER = log_utils.get_logger()


def generate_unit_id(task_id):
    str_list = task_id.split("_")
    for item in str_list:
        if item in ["arbiter", "promoter", "provider"]:
            str_list.remove(item)
    return "_".join(str_list)


class Tracking(object):
    METRIC_DATA_PARTITION = 48
    METRIC_LIST_PARTITION = 48
    JOB_VIEW_PARTITION = 8

    def __init__(self, project_id: str, job_id: str, role: str, member_id: int,
                 model_id: str = None,
                 model_version: str = None,
                 component_name: str = None,
                 module_name: str = None,
                 task_id: str = None,
                 oot: bool = False):
        self.is_serving_model = False
        self.show_name = ""
        self.source_type = ""
        self.project_id = project_id
        self.job_id = job_id
        self.role = role
        self.member_id = member_id
        self.component_name = component_name if component_name else 'pipeline'
        self.module_name = module_name if module_name else 'Pipeline'
        self.task_id = task_id if task_id else Tracking.generate_task_id(job_id=self.job_id, role=self.role,
                                                                         component_name=self.component_name)

        self.table_namespace = '_'.join(
            ['wefe', 'tracking', 'data', self.job_id, self.role, str(self.member_id), self.component_name])
        self.job_table_namespace = '_'.join(
            ['wefe', 'tracking', 'data', self.job_id, self.role, str(self.member_id)])
        self.model_id = model_id
        self.member_model_id = model_utils.gen_member_model_id(model_id=model_id, role=role, member_id=member_id)
        self.model_version = model_version
        self.oot = oot

    def set_is_serving_model(self, flag):
        self.is_serving_model = flag

    def set_show_name(self, name):
        self.show_name = name

    def set_source_type(self, source_type):
        self.source_type = source_type

    def _get_task_result_type(self, data_type, data_name=None):
        """
        Get type for task result

        Parameters
        ----------
        data_type:TaskResultDataType

        data_name:str
            train、eval

        Returns
        -------

        """
        if data_name:

            # In oot mode, in order to avoid primary key conflicts,
            # only the type field can be used to de-duplicate
            if self.oot:
                return '_'.join([data_type, data_name, self.component_name, 'oot'])
            return '_'.join([data_type, data_name])

        return data_type + '_' + self.component_name + "_oot" if self.oot else data_type

    def save_output_data_table(self, data_table, data_name: str = 'component', save_dataset=False):
        if data_table:
            save_name = '{}_persistent'.format(data_table._name)
            save_namespace = NAMESPACE.DATA
            save_partitions = data_table.get_partitions()

            async_save = False
            fcs_info = None
            if RuntimeInstance.BACKEND.is_fc() and isinstance(data_table, FCSource) and data_table.get_exist_fcs():
                async_save = True
                fcs_info = data_table.get_exist_fcs().to_dict()
                params = {
                    "fcs_info": fcs_info,
                    "name": save_name,
                    "namespace": save_namespace,
                    "partitions": save_partitions
                }

                # save data asynchronously
                flow_action_queue = FlowActionQueue()
                flow_action_queue.id = get_commit_id()
                flow_action_queue.producer = 'kernel'
                flow_action_queue.action = FlowQueueActionType.SAVE_OUTPUT_DATA
                flow_action_queue.params = json.dumps(params)
                flow_action_queue.channel = ''
                FlowActionQueueDao.save(flow_action_queue,force_insert=True)

            if not async_save:
                # save data synchronously
                data_table.save_as(namespace=save_namespace, name=save_name)

            # save meta
            header_list = data_table.schema.get('header', [])
            session.save_data_table_meta(
                {'schema': data_table.schema, 'header': header_list,
                 'sid': data_table.schema.get('sid_name', '')},
                data_table_namespace=save_namespace, data_table_name=save_name)

            data_input = {'table_name': save_name, 'table_namespace': save_namespace, 'partition': save_partitions,
                          'table_create_count': data_table.count() if data_table else 0, 'fcs_info': fcs_info}

            # self.save_data_info(data_input=data_input, mark=True, data_name=data_name)

            self.save_task_result(data_input, self._get_task_result_type(TaskResultDataType.DATA, data_name))

            if save_dataset:
                self.save_dataset(data_input, header_list)

    def get_output_data_table(self, data_name: str = 'component'):
        """

        Get output data

        Parameters
        ----------
        data_name

        Returns
        -------
           table of dsource
        """
        task_result = self.get_task_result(self._get_task_result_type(TaskResultDataType.DATA, data_name))
        if task_result and task_result.result:
            data_table_info = json.loads(task_result.result)

            if data_table_info and data_table_info.get("table_name") and data_table_info.get("table_namespace"):
                data_table = session.table(name=data_table_info.get('table_name', ''),
                                           namespace=data_table_info.get('table_namespace', ''))
                data_table_meta = data_table.get_metas()
                if data_table_meta.get('schema', None):
                    data_table.schema = data_table_meta['schema']

                # If fcs exists, load fcs directly
                if 'fcs_info' in data_table_info and isinstance(data_table, FCSource):
                    fcs_info = data_table_info.get('fcs_info')
                    fcs = FCStorage.from_fcs_info(fcs_info)
                    if fcs:
                        fc_source = FCSource.from_fcs(fcs, session.get_session_id(), data_table.get_namespace(),
                                                      data_table.get_name())
                        fc_source.schema = data_table.schema
                        data_table = fc_source
                return data_table
        else:
            return None

    def save_output_model(self, model_buffers: dict, model_name: str, data_name, save_to_storage=False):
        if model_buffers:
            if save_to_storage:
                name_space = 'wefe_data'
                name = self.task_id + '_' + self.job_id
                model_manager.save_component_model(component_model_key='{}.{}'.format(self.component_name, model_name),
                                                   model_buffers=model_buffers,
                                                   member_model_id=name_space,
                                                   model_version=name)
            # save to task result
            model_json_obj = self._model_buffers_to_json_obj(model_buffers, self.member_model_id, self.model_version,
                                                             component_model_key='{}.{}'.format(self.component_name,
                                                                                                model_name))
            self.save_task_result(model_json_obj, self._get_task_result_type(TaskResultDataType.MODEL, model_name))

    def _model_buffers_to_json_obj(self, model_buffers: dict, member_model_id, model_version, component_model_key):
        """

        Model buffers to json obj

        Parameters
        ----------
        model_buffers
        member_model_id
        model_version
        component_model_key

        Returns
        -------

        """
        model = {'member_model_id': member_model_id, 'model_version': model_version,
                 'component_model_key': component_model_key}
        for buffer_name, buffer_object in model_buffers.items():
            json_obj = MessageToJson(buffer_object, including_default_value_fields=True)
            if not json_obj:
                fill_message = default_empty_fill_pb2.DefaultEmptyFillMessage()
                fill_message.flag = 'set'
                json_obj = MessageToJson(fill_message, including_default_value_fields=True)
            if 'meta' in buffer_name.lower():
                model['model_meta'] = json.loads(json_obj)
            if 'param' in buffer_name.lower():
                model['model_param'] = json.loads(json_obj)
        return model

    def save_task_result(self, task_result: dict, result_type, component_name=None):
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
        model = TaskResultDao.get(
            TaskResult.job_id == self.job_id,
            TaskResult.task_id == self.task_id,
            TaskResult.role == self.role,
            TaskResult.type == result_type
        )

        task = TaskDao.get(
            Task.job_id == self.job_id,
            Task.task_id == self.task_id
        )

        # Compatible with local test without task information
        if not task:
            task = Task()
            task.flow_id = "local_test_flow_id"
            task.flow_node_id = "local_test_flow_node_id"

        is_insert = True
        if model:
            is_insert = False
        else:
            model = TaskResult()
            model.id = get_commit_id()
            model.created_time = datetime.datetime.now()

        model.job_id = self.job_id
        model.name = component_name or self.component_name
        model.task_id = self.task_id
        model.role = self.role
        model.type = result_type
        model.updated_time = datetime.datetime.now()
        model.result = json.dumps(task_result)
        model.component_type = self.component_name.rsplit('_')[0]
        model.flow_id = task.flow_id
        model.flow_node_id = task.flow_node_id
        model.project_id = task.project_id

        if self.is_serving_model and model.type.split("_")[0] == "model":
            model.serving_model = 1

        TaskResultDao.save(model, force_insert=is_insert)

        return model

    def get_task_result(self, result_type, task_id=None):
        """
        Get task result

        Parameters
        ----------
        result_type
        task_id

        Returns
        -------

        """
        where_condition = [TaskResult.job_id == self.job_id,
                           TaskResult.name == self.component_name,
                           TaskResult.role == self.role,
                           TaskResult.type == result_type]
        if task_id:
            where_condition.append(TaskResult.task_id == task_id)

        return TaskResultDao.get(*tuple(where_condition))

    def save_training_best_model(self, model_buffers):
        # save to task_result
        model_json_obj = self._model_buffers_to_json_obj(model_buffers, self.member_model_id, self.model_version,
                                                         component_model_key='{}.{}'.format(self.component_name,
                                                                                            "default"))
        self.save_task_result(model_json_obj, self._get_task_result_type(TaskResultDataType.TRAINING_MODEL, "default"))

    def save_cur_best_model(self, model_buffers, iteration):
        model = CurrentBestModelDao.get(
            CurrentBestModel.job_id == self.job_id,
            CurrentBestModel.component_name == self.component_name,
            CurrentBestModel.role == self.role,
            CurrentBestModel.member_id == self.member_id
        )

        is_insert = True
        if model:
            is_insert = False
        else:
            model = CurrentBestModel()
            model.id = get_commit_id()
            model.created_time = current_datetime()

        model.job_id = self.job_id
        model.component_name = self.component_name
        model.task_id = self.task_id
        model.role = self.role
        model.member_id = self.member_id
        model.updated_time = current_datetime()
        model.iteration = iteration

        for buffer_name, buffer_object in model_buffers.items():
            json_obj = MessageToJson(buffer_object, including_default_value_fields=True)
            if not json_obj:
                fill_message = default_empty_fill_pb2.DefaultEmptyFillMessage()
                fill_message.flag = 'set'
                json_obj = MessageToJson(fill_message, including_default_value_fields=True)
            if 'meta' in buffer_name.lower():
                model.model_meta = json_obj
            if 'param' in buffer_name.lower():
                model.model_param = json_obj

        CurrentBestModelDao.save(model, force_insert=is_insert)

        return model

    def save_provider_model_params(self, model_buffers, provider_member_id):
        model = ProviderModelParamsDao.get(
            ProviderModelParams.job_id == self.job_id,
            ProviderModelParams.component_name == self.component_name,
            ProviderModelParams.role == self.role,
            ProviderModelParams.member_id == self.member_id
        )

        is_insert = True
        if model:
            is_insert = False
        else:
            model = ProviderModelParams()
            model.id = get_commit_id()
            model.created_time = datetime.datetime.now()

        model.job_id = self.job_id
        model.component_name = self.component_name
        model.task_id = self.task_id
        model.role = self.role
        model.member_id = self.member_id
        model.updated_time = datetime.datetime.now()
        model.provider_member_id = provider_member_id
        # model.updated_by = ""
        # model.created_by = ""

        json_obj = MessageToJson(model_buffers, including_default_value_fields=True)
        if not json_obj:
            fill_message = default_empty_fill_pb2.DefaultEmptyFillMessage()
            fill_message.flag = 'set'
            json_obj = MessageToJson(fill_message, including_default_value_fields=True)
        model.provider_model_param = json_obj

        ProviderModelParamsDao.save(model, force_insert=is_insert)

        return model

    def get_output_model(self, model_name=ModelType.BINNING_MODEL):
        model = TaskResultDao.get(
            TaskResult.task_id == self.task_id,
            TaskResult.role == self.role,
            TaskResult.type == self._get_task_result_type(TaskResultDataType.MODEL, model_name)
        )

        if model:
            model = json.loads(model.result)
            return {"Model_Meta": model["model_meta"], "Model_Param": model["model_param"]}
        else:
            return None

    def get_training_best_model(self):

        model = TaskResultDao.get(
            TaskResult.task_id == self.task_id,
            TaskResult.role == self.role,
            TaskResult.type == self._get_task_result_type(TaskResultDataType.TRAINING_MODEL, "default")
        )

        if model:
            model = json.loads(model.result)
            return {"Model_Meta": model["model_meta"], "Model_Param": model["model_param"]}
        else:
            return None

    def get_statics_result(self, type='data_feature_statistic'):
        model = TaskResultDao.get_last_statics_result(self.job_id, self.role, type)
        if model:
            max = {}
            min = {}
            mean = {}
            median = {}
            missing_count = {}
            std_variance = {}
            count = 0
            mode = {}
            result = json.loads(model.result)
            LOGGER.info("mysql result:{}".format(result))
            members = result['members']
            feature_statistic = None
            for member in members:
                if member['role'] == self.role:
                    feature_statistic = member['feature_statistic']
            if feature_statistic:
                for feature, value in feature_statistic.items():
                    max[feature] = value['max']
                    min[feature] = value['min']
                    mean[feature] = value['mean']
                    if '50' in value['percentile']:
                        median[feature] = value['percentile']['50']
                    missing_count[feature] = value['missing_count']
                    std_variance[feature] = value['std_variance']
                    count = value['count']
                    mode[feature] = value.get('mode')
                statics = {"max": max, "min": min, "mean": mean, "median": median, "missing_count": missing_count,
                           "std_variance": std_variance, "std": std_variance, 'count': count, "mode": mode}
                return statics

        return None

    def get_binning_result(self):
        model = TaskResultDao.get_last_task_result(self.job_id, self.role, 'model_train')
        if model:
            result = json.loads(model.result)
            LOGGER.debug("mysql result:{}".format(result))
            binning_result = result.get('model_param').get('binningResult').get('binningResult')
            binning_results = {}
            for feature, value in binning_result.items():
                binning_results[feature] = {'woe': value.get('woeArray'), 'split_points': value.get('splitPoints')}
            model_meta = result.get('model_meta')
            model_param = {'header': model_meta.get('cols')}
            transform_cols = model_meta.get('transformParam').get('transformCols')
            model_param['transform_bin_indexes'] = [int(x) for x in transform_cols]
            return model_param, binning_results
        return None, None

    def saveSingleMetricData(self, metric_name: str, metric_namespace: str, metric_meta, kv, job_level=False):
        self.save_metric_data_to_task_result(metric_name, metric_namespace, metric_meta, kv, job_level)

    def saveMetricData(self, metric_name: str, metric_namespace: str, metric_meta, kv, job_level=False):
        self.save_metric_data_to_task_result(metric_name, metric_namespace, metric_meta, kv, job_level)

    def _get_item_metric(self, metric_name: str, metric_namespace: str, metric_meta: {}, data: {}):
        """
        Get metric item

        Parameters
        ----------
        metric_name
        metric_namespace
        metric_meta
        data

        Returns
        -------

        """
        return {"metric_name": metric_name, "metric_namespace": metric_namespace,
                "metric_meta": metric_meta, "data": data}

    def _get_metric_data_value(self, v):
        # return {'value': v, 'create_time': timestamp_to_date()}
        if isinstance(v, dict):
            return {'value': v}
        if np.isinf(v):
            return {'value': 'Infinity'}
        if type(v) == float:
            return {'value': str(v)}
        return {'value': v}

    def save_metric_data_to_task_result(self, metric_name: str, metric_namespace: str, metric_meta, kv,
                                        job_level=False, need_value=True):
        """
        Save metric data to task result

        Parameters
        ----------
        metric_name
        metric_namespace
        metric_meta
        kv
        job_level
        need_value

        Returns
        -------

        """
        result_type = self._get_task_result_type(TaskResultDataType.METRIC, metric_namespace)
        metric_task_result = self.get_task_result(result_type, self.task_id)

        result = {}
        if metric_task_result and metric_task_result.result:
            result = json.loads(metric_task_result.result)

        metric_key = '_'.join([metric_namespace, metric_name])
        component_name = self.component_name if not job_level else 'dag'

        if metric_key in result.keys():
            item_metric = result.get(metric_key)
        else:
            item_metric = self._get_item_metric(metric_name, metric_namespace, metric_meta, {})
        if not need_value:
            item_metric['data'] = kv
        elif isinstance(kv, list):
            for k, v in kv:
                item_metric['data'].update({k: self._get_metric_data_value(v)})
        else:
            item_metric['data'].update({kv[0]: self._get_metric_data_value(kv[1])})

        result[metric_key] = item_metric

        self.save_task_result(result, result_type, component_name)

    def save_dataset(self, data_input, header_list):
        # Determine whether the task exists
        task = TaskDao.find_one_by_task_id(self.task_id)
        if not task:
            return

        # Determine whether the job exists
        job = JobDao.find_one_by_job_id(self.job_id, self.role)
        if not job:
            return

        # Determine whether the project exists
        project = ProjectDao.get(self.project_id == Project.project_id, Project.my_role == self.role)
        if not project:
            return

        job_member = JobMemberDao.get(
            JobMember.job_id == self.job_id,
            JobMember.member_id == self.member_id,
            JobMember.job_role == self.role
        )
        if not job_member:
            return

        data_set_old = DataSetDao.get(
            DataSet.id == job_member.data_set_id
        )
        if not data_set_old:
            return

        data_set = DataSet()
        # data_set_id = get_commit_id()
        unit_id = generate_unit_id(self.task_id)
        data_set.id = md5(unit_id)
        data_set.created_time = current_datetime()
        data_set.updated_time = current_datetime()
        data_set.name = job.name + self.show_name
        data_set.source_type = self.module_name
        data_set.source_job_id = job.job_id
        data_set.name = data_set.name + '_' + timestamp_to_date(format_string='%Y%m%d%H%M%S')
        data_set.storage_type = data_set_old.storage_type

        data_set.public_member_list = data_set_old.public_member_list
        data_set.tags = data_set_old.tags
        data_set.description = data_set_old.description
        data_set.source_flow_id = data_set_old.source_flow_id
        data_set.source_task_id = self.task_id
        data_set.y_name_list = data_set.y_name_list
        data_set.usage_count_in_job = 0
        data_set.usage_count_in_flow = 0
        data_set.usage_count_in_project = 0

        data_set.namespace = data_input['table_namespace']
        data_set.table_name = data_input['table_name']
        data_set.row_count = data_input['table_create_count']

        data_set.feature_name_list = ",".join(header_list)
        data_set.y_name_list = data_set_old.y_name_list
        data_set.primary_key_column = data_set_old.primary_key_column
        # column = feature + primary_key + y
        if data_set.y_name_list is None:
            data_set.column_name_list = ",".join(header_list) + "," + data_set.primary_key_column
        else:
            data_set.column_name_list = ",".join(
                header_list) + "," + data_set.y_name_list + "," + data_set.primary_key_column
        if len(header_list) == 0:
            data_set.column_name_list = data_set.column_name_list[1:]
        data_set.contains_y = data_set_old.contains_y
        data_set.column_count = len(data_set.column_name_list.split(","))
        data_set.feature_count = len(data_set.feature_name_list.split(","))

        DataSetDao.save(data_set, force_insert=True)

        self.save_project_data_set(data_set.id, self.job_id, self.task_id, self.component_name)
        self.save_data_set_column(job_member.data_set_id, data_set.id, header_list)

        return data_set

    @staticmethod
    def generate_task_id(job_id, role, component_name):
        return '{}_{}_{}'.format(job_id, role, component_name)

    def get_job_log_directory(job_id):
        return os.path.join(log_utils.get_log_root_path(), job_id)

    def get_job_directory(job_id):
        return os.path.join(file_utils.get_project_base_directory(), 'jobs', job_id)

    def save_project_data_set(self, data_set_id, job_id, task_id, component_name):
        project_data_set = ProjectDataSet()
        project_data_set.id = get_commit_id()
        project_data_set.member_role = self.role
        project_data_set.created_by = self.member_id
        project_data_set.created_time = current_datetime()
        project_data_set.updated_by = self.member_id
        project_data_set.updated_time = current_datetime()

        project_data_set.project_id = self.project_id
        project_data_set.member_id = self.member_id
        project_data_set.data_set_id = data_set_id
        project_data_set.audit_status = ProjectStatus.AGREE
        project_data_set.status_updated_time = current_datetime()

        project_data_set.source_task_id = task_id
        project_data_set.source_type = component_name.split("_")[0]
        project_data_set.source_job_id = job_id

        ProjectDataSetDao.save(project_data_set, force_insert=True)

        return project_data_set

    @staticmethod
    def save_data_set_column(old_data_set_id, data_set_id, header):
        pass

    def _calc_progress(self, model):
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

    def init_task_progress(self, work_amount: int):
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
        if self.oot:
            return

        is_insert = True
        model = TaskProgressDao.get_by_unique_id(self.task_id, self.role)

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
            task_info = TaskDao.get(
                Task.task_id == self.task_id,
                Task.role == self.role
            )

            if task_info:
                model.flow_id = task_info.flow_id
                model.flow_node_id = task_info.flow_node_id
            else:
                model.flow_id = 0
                model.flow_node_id = 0

        model.project_id = self.project_id

        model.job_id = self.job_id
        model.role = self.role
        model.task_id = self.task_id
        model.task_type = self.component_name.split('_')[0]
        model.expect_work_amount = work_amount
        self._calc_progress(model)

        TaskProgressDao.save(model, force_insert=is_insert)

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
        if self.oot:
            return

        if work_amount >= 0:
            model = TaskProgressDao.get_by_unique_id(self.task_id, self.role)
            if model:
                model.progress = work_amount
                model.updated_time = datetime.datetime.now()
                self._calc_progress(model)
                TaskProgressDao.save(model)

    def add_task_progress(self, step: int = 1):
        """

        Increase progress according to step

        Parameters
        ----------
        step:int

        Returns
        -------

        """
        if self.oot:
            return

        model = TaskProgressDao.get_by_unique_id(self.task_id, self.role)

        if model.progress is not None:
            work_amount = model.progress + step
        else:
            work_amount = step

        # Reserve one amount for use when the finish call
        if work_amount > model.expect_work_amount - 1:
            work_amount = model.expect_work_amount - 1

        self.set_task_progress(work_amount)

    def finish_task_progress(self):
        """
        Finish task progress

        Returns
        -------

        """
        model = TaskProgressDao.get_by_unique_id(self.task_id, self.role)

        if model:
            model.progress = model.progress + 1
            model.really_work_amount = model.progress

            if model.really_work_amount > model.expect_work_amount:
                model.really_work_amount = model.expect_work_amount

            model.updated_time = datetime.datetime.now()
            self._calc_progress(model)
            model.pid_success = 1
            TaskProgressDao.save(model)

    @update_task_status_env()
    def set_task_success(self):
        """
        Set task success

        Returns
        -------

        """
        running_task = TaskDao.find_one_by_task_id(self.task_id)
        if running_task:
            running_task.status = TaskStatus.SUCCESS
            running_task.message = "任务运行完成"
            running_task.updated_time = datetime.datetime.now()
            running_task.finish_time = datetime.datetime.now()
            running_task.spend = get_delta_seconds(
                running_task.finish_time, running_task.start_time)
            TaskDao.save(running_task)


if __name__ == '__main__':
    task = TaskDao.find_one_by_task_id('69ccd7ca9ff444f3a93a7e950fbf432d_promoter_Intersection_16238974992057754')
    a = task.start_time
    b = task.finish_time
    print(type(a))
    c = b - a
    print(type(c))
    print(c.seconds)
