#!/usr/bin/env python
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

import copy

import numpy as np

from common.python.common.consts import MemberRole
from common.python.federation import roles_to_parties
from common.python.utils import log_utils
from kernel.components.evaluation.param import EvaluateParam
from kernel.transfer.variables.transfer_class.common_transfer_variable import CommonTransferVariable
from kernel.utils import consts
from kernel.utils.component_properties import ComponentProperties
from kernel.utils.data_util import header_alignment
from kernel.utils.param_extract import ParamExtract

LOGGER = log_utils.get_logger()


class ModelBase(object):
    def __init__(self):
        self.model_output = None
        self.mode = None
        self.role = None
        self.member_id = None
        self.mix_promoter_member_id = None
        self.data_output = None
        self.model_param = None
        self.transfer_variable = None
        self.flowid = ''
        self.taskid = ''
        self.need_one_vs_rest = False
        self.tracker = None
        self.cv_fold = 0
        self.validation_freqs = None
        self.component_properties = ComponentProperties()

        # whether save out data to data set
        self.save_dataset = False

        # Does the model result need to be additionally saved to the storage system (such as CK)?
        # Note: By default, it has been saved uniformly to the mysql `model output` table
        self.model_save_to_storage = False
        self.is_serving_model = False

        # The component with the data set output is used to display the name
        self.show_name = None

        self.source_type = None
        self.federated_learning_mode = None
        self.federated_learning_type = None

        self._summary = dict()
        self._align_cache = dict()

        self.provider_master = False
        self.provider_other_inner_id = None
        self.provider_master_inner_id = None
        self.provider_inner_id = None

        # common variable transfer
        self.common_transfer_variable = CommonTransferVariable()

    def set_show_name(self, name):
        self.show_name = name

    def _init_runtime_parameters(self, component_parameters):
        param_extracter = ParamExtract()
        param = param_extracter.parse_param_from_config(self.model_param, component_parameters)
        param.check()
        LOGGER.debug("final parameters====>{}".format(param.__dict__))
        componentProperties = self.component_properties.parse_component_param(component_parameters, param)
        self.role = componentProperties.role
        self.member_id = componentProperties.local_member_id
        self.mix_promoter_member_id = componentProperties.mix_promoter_member_id
        self.provider_master = componentProperties.provider_master
        self.provider_other_inner_id = componentProperties.provider_other_inner_id
        self.provider_master_inner_id = componentProperties.provider_master_inner_id
        self.provider_inner_id = componentProperties.provider_inner_id
        self.federated_learning_mode = componentProperties.federated_learning_mode
        self.federated_learning_type = componentProperties.federated_learning_type
        self._init_model(param)
        self.set_save_dataset_flag(param)
        return param

    def set_save_dataset_flag(self, param):
        if hasattr(param, 'save_dataset'):
            self.save_dataset = param.save_dataset

    @property
    def need_cv(self):
        return self.component_properties.need_cv

    @property
    def need_run(self):
        return self.component_properties.need_run

    @need_run.setter
    def need_run(self, value: bool):
        self.component_properties.need_run = value

    def _init_model(self, model):
        pass

    def load_model(self, model_dict):
        pass

    def _parse_need_run(self, model_dict, model_meta_name):
        meta_obj = list(model_dict.get('model').values())[0].get(model_meta_name)
        need_run = meta_obj.need_run
        # self.need_run = need_run
        self.component_properties.need_run = need_run

    def run(self, component_parameters=None, args=None):
        LOGGER.debug("component_parameters====>{}".format(component_parameters))
        LOGGER.debug("args====>{}".format(args))

        self._init_runtime_parameters(component_parameters)
        self.component_properties.parse_dsl_args(args)

        running_funcs = self.component_properties.extract_running_rules(args, self)
        saved_result = []
        for func, params, save_result, use_previews in running_funcs:
            # for func, params in zip(todo_func_list, todo_func_params):
            if use_previews:
                if params:
                    real_param = [saved_result, params]
                else:
                    real_param = saved_result
                LOGGER.debug("func: {}".format(func))
                this_data_output = func(*real_param)
                saved_result = []
            else:
                this_data_output = func(*params)

            LOGGER.debug("save_result:{}".format(saved_result))
            if save_result:
                saved_result.append(this_data_output)

        if len(saved_result) == 1:
            self.data_output = saved_result[0]
            # LOGGER.debug("One data: {}".format(self.data_output.first()[1].features))
        LOGGER.debug("saved_result is : {}, data_output: {}".format(saved_result, self.data_output))

    def get_metrics_param(self):
        return EvaluateParam(eval_type="binary",
                             pos_label=1)

    def predict(self, data_inst):
        pass

    def fit(self, *args):
        pass

    def transform(self, data_inst):
        pass

    def cross_validation(self, data_inst):
        pass

    def one_vs_rest_fit(self, train_data=None):
        pass

    def one_vs_rest_predict(self, train_data):
        pass

    def init_validation_strategy(self, train_data=None, validate_data=None):
        pass

    def output_data(self):
        return self.data_output

    def export_model(self):
        return self.model_output

    def data_is_empty(self, data_instances):
        count = data_instances.count()
        if count < 1:
            raise ValueError("data instances is empty")
        return count

    def set_flowid(self, flowid):
        # self.flowid = '.'.join([self.taskid, str(flowid)])
        self.flowid = flowid
        self.set_transfer_variable()

    def set_transfer_variable(self):
        if self.transfer_variable is not None:
            LOGGER.debug("set flowid to transfer_variable, flowid: {}".format(self.flowid))
            self.transfer_variable.set_flowid(self.flowid)

    def set_taskid(self, taskid):
        """ taskid: jobid + component_name, reserved variable """
        self.taskid = taskid

    def get_metric_name(self, name_prefix):
        if not self.need_cv:
            return name_prefix

        return '_'.join(map(str, [name_prefix, self.flowid]))

    def set_tracker(self, tracker):
        self.tracker = tracker

    def set_predict_data_schema(self, predict_datas, schemas):
        if predict_datas is None:
            return predict_datas
        if isinstance(predict_datas, list):
            predict_data = predict_datas[0]
            schema = schemas[0]
        else:
            predict_data = predict_datas
            schema = schemas
        if predict_data is not None:
            predict_data.schema = {"header": ["label", "predict_result", "predict_score", "predict_detail", "type"],
                                   "sid_name": schema.get('sid_name')}
        return predict_data

    def callback_metric(self, metric_name, metric_namespace, metric_meta, metric_data):
        if self.need_cv:
            metric_name = '.'.join([metric_name, str(self.cv_fold)])
            flow_id_list = self.flowid.split('.')
            LOGGER.debug("Need cv, change callback_metric, flow_id_list: {}".format(flow_id_list))
            if len(flow_id_list) > 1:
                curve_name = '.'.join(flow_id_list[1:])
                metric_meta['curve_name'] = curve_name
        else:
            metric_meta['curve_name'] = metric_name

        self.tracker.saveSingleMetricData(metric_name, metric_namespace, metric_meta, metric_data)

    def set_cv_fold(self, cv_fold):
        self.cv_fold = cv_fold

    def data_instance_to_str(self, data_instances, with_label):
        if data_instances is None:
            return data_instances

        schema = data_instances.schema
        new_data_instances = data_instances.mapValues(lambda v: v.to_csv())
        data_instances.schema = schema
        header = ''
        if schema.get('header') is not None:
            header = ",".join(schema.get('header'))
        if with_label:
            header = schema.get('label_name') + ',' + header
        schema['header'] = header
        return new_data_instances

    def summary(self):
        return copy.deepcopy(self._summary)

    def set_summary(self, new_summary):
        """
        Model summary setter
        Parameters
        ----------
        new_summary: dict, summary to replace the original one

        Returns
        -------

        """

        if not isinstance(new_summary, dict):
            raise ValueError(f"summary should be of dict type, received {type(new_summary)} instead.")
        self._summary = copy.deepcopy(new_summary)

    def _whether_with_arbiter(self, task_config):
        """

        Determine whether arbiter is involved

        Parameters
        ----------
        task_config

        Returns
        -------

        """
        with_arbiter = False
        if task_config and 'task' in task_config:
            task_info = task_config['task']
            if 'members' in task_info:
                for member in task_info['members']:
                    if member['member_role'] == MemberRole.ARBITER:
                        with_arbiter = True
                        break
        return with_arbiter

    def _common_status_sync(self, with_arbiter):

        # first step ：promoter receive other member complete status
        if self.role == MemberRole.PROMOTER:
            self.common_transfer_variable.provider2promoter_complete_status.get()
            if with_arbiter:
                self.common_transfer_variable.arbiter2promoter_complete_status.get()

        elif self.role == MemberRole.PROVIDER:
            self.common_transfer_variable.provider2promoter_complete_status.remote("completed")

        elif with_arbiter:
            self.common_transfer_variable.arbiter2promoter_complete_status.remote("completed")

        # second step: other member receive promoter complete status
        if self.role == MemberRole.PROMOTER:
            self.common_transfer_variable.promoter2provider_complete_status.remote("completed")
            if with_arbiter:
                self.common_transfer_variable.promoter2arbiter_complete_status.remote("completed")

        elif self.role == MemberRole.PROVIDER:
            self.common_transfer_variable.promoter2provider_complete_status.get()

        elif with_arbiter:
            self.common_transfer_variable.promoter2arbiter_complete_status.get()

    def _mix_status_sync(self, with_arbiter):

        # first, each promoter get each provider sub task success status
        complete_flag = "completed"
        if self.role == MemberRole.PROMOTER:
            self.common_transfer_variable.provider2promoter_complete_status.get()
        elif self.role == MemberRole.PROVIDER:
            self.common_transfer_variable.provider2promoter_complete_status.remote(
                complete_flag,
                member_id_list=[self.mix_promoter_member_id]
            )

        # second,each sub task get promoter success status
        if self.role == MemberRole.PROMOTER:
            self.common_transfer_variable.promoter2provider_complete_status.remote(
                complete_flag
            )
        elif self.role == MemberRole.PROVIDER:
            self.common_transfer_variable.promoter2provider_complete_status.get(
                member_id_list=[self.mix_promoter_member_id]
            )

        # third,each promoter and arbiter change success status
        promoter_members = roles_to_parties([consts.PROMOTER])
        if self.role == MemberRole.PROMOTER:
            other_promoter_member_ids = [item_promoter.member_id for item_promoter in promoter_members
                                         if item_promoter.member_id != self.member_id]

            # remote complete status to other promoter
            self.common_transfer_variable.promoter2promoter_complete_status.remote(
                complete_flag,
                member_id_list=other_promoter_member_ids
            )
            if with_arbiter:
                self.common_transfer_variable.promoter2arbiter_complete_status.remote(
                    complete_flag
                )

            # get complete status from other promoter
            self.common_transfer_variable.promoter2promoter_complete_status.get(
                member_id_list=other_promoter_member_ids
            )

        elif self.role == MemberRole.ARBITER:
            self.common_transfer_variable.promoter2arbiter_complete_status.get()

    def status_sync(self, task_config):
        """

        task status sync

        Parameters
        ----------
        task_config:dict

        Returns
        -------

        """
        if not self.transfer_variable:
            LOGGER.debug('without transfer variable, do not sync status')
            return

        # member count
        member_count = len(task_config.get("task", {}).get("members", []))
        if member_count <= 1:
            LOGGER.debug(f'member count:{member_count}, do not sync status')
            return

        with_arbiter = self._whether_with_arbiter(task_config)

        if self.federated_learning_type == consts.MIX:
            self._mix_status_sync(with_arbiter)

        else:
            self._common_status_sync(with_arbiter)

        LOGGER.debug(f'sync status complete,role:{self.role}')

    @staticmethod
    def extract_data(data: dict):
        LOGGER.debug("In extract_data, data input: {}".format(data))
        if len(data) == 0:
            return data
        if len(data) == 1:
            return list(data.values())[0]
        return data

    @staticmethod
    def predict_score_to_output(data_instances, predict_score, classes=None, threshold=0.5):
        """
        Get predict result output
        Parameters
        ----------
        data_instances: table, data used for prediction
        predict_score: table, probability scores
        classes: list or None, all classes/label names
        threshold: float, predict threshold, used for binary label

        Returns
        -------
        Table, predict result
        """

        # regression
        if classes is None:
            predict_result = data_instances.join(predict_score, lambda d, pred: [d.label, pred,
                                                                                 pred, {"label": pred}])
        # binary
        elif isinstance(classes, list) and len(classes) == 2:
            class_neg, class_pos = classes[0], classes[1]
            pred_label = predict_score.mapValues(lambda x: class_pos if x > threshold else class_neg)
            predict_result = data_instances.mapValues(lambda x: x.label)
            predict_result = predict_result.join(predict_score, lambda x, y: (x, y))
            class_neg_name, class_pos_name = str(class_neg), str(class_pos)
            predict_result = predict_result.join(pred_label, lambda x, y: [x[0], y, x[1],
                                                                           {class_neg_name: (1 - x[1]),
                                                                            class_pos_name: x[1]}])

        # multi-label: input = array of predicted score of all labels
        elif isinstance(classes, list) and len(classes) > 2:
            # pred_label = predict_score.mapValues(lambda x: classes[x.index(max(x))])
            classes = [str(val) for val in classes]
            predict_result = data_instances.mapValues(lambda x: x.label)
            predict_result = predict_result.join(predict_score, lambda x, y: [x, int(classes[np.argmax(y)]),
                                                                              float(np.max(y)),
                                                                              dict(zip(classes, list(y)))])
        else:
            raise ValueError(f"Model's classes type is {type(classes)}, classes must be None or list.")

        return predict_result

    def align_data_header(self, data_instances, pre_header):
        """
        align features of given data, raise error if value in given schema not found
        :param data_instances: data table
        :param pre_header: list, header of model
        :return: dtable, aligned data
        """
        result_data = self._align_cache.get(id(data_instances))
        if result_data is None:
            result_data = header_alignment(data_instances=data_instances, pre_header=pre_header)
            self._align_cache[id(data_instances)] = result_data
        return result_data
