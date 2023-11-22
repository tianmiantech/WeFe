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

from common.python.common.consts import ModelType, DataSetType
from common.python.utils import log_utils
from kernel.utils import abnormal_detection, consts

LOGGER = log_utils.get_logger()


class RunningFuncs(object):
    def __init__(self):
        self.todo_func_list = []
        self.todo_func_params = []
        self.save_result = []
        self.use_previews_result = []

    def add_func(self, func, params, save_result=False, use_previews=False):
        self.todo_func_list.append(func)
        self.todo_func_params.append(params)
        self.save_result.append(save_result)
        self.use_previews_result.append(use_previews)

    def __iter__(self):
        for func, params, save_result, use_previews in zip(self.todo_func_list, self.todo_func_params,
                                                           self.save_result, self.use_previews_result):
            yield func, params, save_result, use_previews


class ComponentProperties(object):
    def __init__(self):
        self.need_cv = False
        self.need_run = False
        self.has_model = False
        self.has_binning_model = False
        self.has_train_data = False
        self.has_eval_data = False
        self.has_normal_data = False
        self.role = None
        self.provider_member_idlist = []
        self.local_member_id = -1
        self.promoter_member_id = -1
        self.mix_promoter_member_id = -1
        self.federated_learning_mode = None
        self.federated_learning_type = None
        self.provider_master = False
        self.provider_other_inner_id = None
        self.provider_master_inner_id = None
        self.provider_inner_id = None

    def parse_component_param(self, component_parameters, param):

        try:
            need_cv = param.cv_param.need_cv
        except AttributeError:
            need_cv = False
        self.need_cv = need_cv
        LOGGER.debug(component_parameters)

        try:
            need_run = param.need_run
        except AttributeError:
            need_run = True
        self.need_run = need_run
        LOGGER.debug("need_run: {}, need_cv: {}".format(self.need_run, self.need_cv))
        self.role = component_parameters["local"]["role"]
        self.provider_member_idlist = component_parameters["role"].get("provider")
        self.local_member_id = component_parameters["local"].get("member_id")
        self.promoter_member_id = component_parameters["role"].get("promoter")
        if self.promoter_member_id is not None:
            self.promoter_member_id = self.promoter_member_id[0]
        if 'mix_promoter_member_id' in component_parameters:
            self.mix_promoter_member_id = component_parameters["mix_promoter_member_id"]
        self.provider_master = component_parameters['task'].get('provider_master')
        self.provider_inner_id = component_parameters['task'].get('provider_inner_id')
        self.provider_other_inner_id = component_parameters['task'].get('provider_other_inner_id')
        self.provider_master_inner_id = component_parameters['task'].get('provider_master_inner_id')
        self.federated_learning_mode = component_parameters['job'].get('federated_learning_mode')
        self.federated_learning_type = component_parameters['job'].get('federated_learning_type')
        return self

    def parse_dsl_args(self, args):
        if ModelType.TRAIN_MODEL in args.get("model", {}):
            self.has_model = True
        if ModelType.BINNING_MODEL in args.get("model", {}):
            self.has_binning_model = True
        data_sets = args.get("data")
        if data_sets is None:
            return self
        for data_key in data_sets:
            if DataSetType.TRAIN_DATA_SET in data_sets[data_key]:
                self.has_train_data = True
            if DataSetType.EVALUATION_DATA_SET in data_sets[data_key]:
                self.has_eval_data = True
            if DataSetType.NORMAL_DATA_SET in data_sets[data_key]:
                self.has_normal_data = True
        return self

    @staticmethod
    def extract_input_data(args):
        data_sets = args.get("data")
        train_data_set = None
        evaluation_data_set = None
        normal_data_set = {}

        if data_sets is None:
            return train_data_set, evaluation_data_set, normal_data_set
        for data_key in data_sets:

            if data_sets[data_key].get(DataSetType.TRAIN_DATA_SET, None):
                if train_data_set is None:
                    train_data_set = []
                train_data_set.append(data_sets[data_key][DataSetType.TRAIN_DATA_SET])

            if data_sets[data_key].get(DataSetType.EVALUATION_DATA_SET, None):
                evaluation_data_set = data_sets[data_key][DataSetType.EVALUATION_DATA_SET]

            if data_sets[data_key].get(DataSetType.NORMAL_DATA_SET, None):
                # data = data_sets[data_key]["data"]
                normal_data_set[data_key] = data_sets[data_key][DataSetType.NORMAL_DATA_SET]
        LOGGER.debug("args: {}, data_sets: {}".format(args, data_sets))
        if train_data_set is not None and len(train_data_set) == 1:
            train_data_set = train_data_set[0]

        return train_data_set, evaluation_data_set, normal_data_set

    def extract_running_rules(self, args, model):
        train_data, eval_data, normal_data = self.extract_input_data(args)

        running_funcs = RunningFuncs()

        # schema = {'header': ['x0', 'x1', 'x2', 'x3', 'x4', 'x5', 'x6', 'x7', 'x8', 'x9'], 'sid_name': 'id', 'label_name': 'y'}
        schema = None
        for d in [train_data, eval_data]:
            if d is not None:
                if isinstance(d, list):
                    schema = d[0].schema
                else:
                    schema = d.schema
                break

        if not self.need_run:
            running_funcs.add_func(self.pass_data, [normal_data], save_result=True)
            # todo_func_list.append(self.pass_data)
            # todo_func_params.append([data])
            # use_previews_result.append(False)
            return running_funcs

        if self.need_cv:
            running_funcs.add_func(model.cross_validation, [train_data])
            # todo_func_list.append(model.cross_validation)
            # todo_func_params.append([train_data])
            # return todo_func_list, todo_func_params
            return running_funcs

        if self.has_model or self.has_binning_model:
            # todo_func_list.append(model.load_model)
            # todo_func_params.append([args])
            running_funcs.add_func(model.load_model, [args])

        if self.has_train_data and self.has_eval_data:
            # todo_func_list.extend([model.set_flowid, model.fit, model.set_flowid, model.predict])
            # todo_func_params.extend([['fit'], [train_data], ['validate'], [train_data, 'validate']])
            self.check_data([train_data, eval_data])
            running_funcs.add_func(model.set_flowid, ['fit'])
            running_funcs.add_func(model.fit, [train_data, eval_data])
            running_funcs.add_func(model.set_flowid, ['validate'])
            running_funcs.add_func(model.predict, [train_data], save_result=True)
            running_funcs.add_func(model.set_flowid, ['predict'])
            running_funcs.add_func(model.predict, [eval_data], save_result=True)
            running_funcs.add_func(self.union_data, ["train", "validate"], use_previews=True, save_result=True)
            running_funcs.add_func(model.set_predict_data_schema, [schema],
                                   use_previews=True, save_result=True)

        elif self.has_train_data:
            self.check_data(train_data)
            running_funcs.add_func(model.set_flowid, ['fit'])
            running_funcs.add_func(model.fit, [train_data])
            running_funcs.add_func(model.set_flowid, ['validate'])
            running_funcs.add_func(model.predict, [train_data], save_result=True)
            running_funcs.add_func(self.union_data, ["train"], use_previews=True, save_result=True)
            running_funcs.add_func(model.set_predict_data_schema, [schema],
                                   use_previews=True, save_result=True)

        elif self.has_eval_data:
            self.check_data(eval_data)
            running_funcs.add_func(model.set_flowid, ['predict'])
            running_funcs.add_func(model.predict, [eval_data], save_result=True)
            running_funcs.add_func(self.union_data, ["predict"], use_previews=True, save_result=True)
            running_funcs.add_func(model.set_predict_data_schema, [schema],
                                   use_previews=True, save_result=True)

        if self.has_normal_data and not self.has_model:
            running_funcs.add_func(model.extract_data, [normal_data], save_result=True)
            running_funcs.add_func(model.set_flowid, ['fit'])
            running_funcs.add_func(model.fit, [], use_previews=True, save_result=True)

        if self.has_normal_data and self.has_model:
            self.check_data(normal_data)
            running_funcs.add_func(model.extract_data, [normal_data], save_result=True)
            running_funcs.add_func(model.set_flowid, ['transform'])
            running_funcs.add_func(model.transform, [], use_previews=True, save_result=True)

        LOGGER.debug("func list: {}, param list: {}, save_results: {}, use_previews: {}".format(
            running_funcs.todo_func_list, running_funcs.todo_func_params,
            running_funcs.save_result, running_funcs.use_previews_result
        ))
        return running_funcs

    def check_data(self, data_instances):
        if self.role == consts.ARBITER:
            return
        if isinstance(data_instances, list):
            for data_instance in data_instances:
                if isinstance(data_instance, list):
                    for data in data_instance:
                        abnormal_detection.empty_table_detection(data)
                else:
                    abnormal_detection.empty_table_detection(data_instance)
        else:
            abnormal_detection.empty_table_detection(data_instances)

    @staticmethod
    def pass_data(data):
        if isinstance(data, dict) and len(data) >= 1:
            data = list(data.values())[0]
        return data

    @staticmethod
    def union_data(previews_data, name_list):
        if len(previews_data) == 0:
            return None

        if any([x is None for x in previews_data]):
            return None

        assert len(previews_data) == len(name_list)

        result_data = None
        for data, name in zip(previews_data, name_list):
            data = data.mapValues(lambda value: value + [name])

            if result_data is None:
                result_data = data
            else:
                result_data = result_data.union(data)

        return result_data
