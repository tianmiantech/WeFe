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

import copy
import json

import numpy as np

from common.python.common.consts import DataSetSourceType
from common.python.utils import log_utils
from kernel.base.statistics import MultivariateStatistical
from kernel.components.fillmissingvalue.param import Params
from kernel.model_base import ModelBase
from kernel.transfer.variables.transfer_class.fill_missing_value_transfer_variable import \
    FillMissingValueTransferVariable
from kernel.utils import consts, abnormal_detection
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()


class FillMissingValue(ModelBase):

    def __init__(self, params=None):
        super(FillMissingValue, self).__init__()
        if params:
            self.model_param = params
        else:
            self.model_param = Params()
        self.multivariateStatistical = None
        self.data_instances = None

        self.statistics = None
        self.header = None

        self.metric_name = "FillMissingValue"
        self.metric_namespace = "train"
        self.metric_type = "FILLMISSINGVALUE"

        self.task_result_type = "data_fill_missing_value"

        self.set_show_name("(FillMissingValue)")
        self.source_type = DataSetSourceType.FILLMISSINGVALUE

        self.transfer_variable = FillMissingValueTransferVariable()

    def _init_params(self):
        self.features_rules = json.loads(self.model_param.features)
        self.statistics = self.model_param.statistics
        self.save_dataset = self.model_param.save_dataset
        self.with_label = self.model_param.with_label
        self.methods = self.get_fill_methods()

    def fit(self, data_instances):
        self._init_params()
        if not data_instances.schema:
            data_instances.schema = data_instances.get_metas()
        self.header = get_header(data_instances)
        self.calc_statistics(data_instances)

        fill_result = self.generate_fill_result()

        _features_rules = self.features_rules
        _multivariateStatistical = self.multivariateStatistical
        _statistics = self.statistics
        feature_missing_count = {}
        for feature in self.features_rules:
            feature_missing_count[feature] = 0

        feature_missing_count = self.calc_missing_count(data_instances)
        new_data_instances = data_instances.mapValues(
            lambda v: fill_value(_features_rules, _statistics, v, self.header))
        new_data_instances.schema = data_instances.schema
        self.data_instances = new_data_instances

        for feature in self.features_rules:
            fill_result[feature]['missing_count'] = feature_missing_count[feature]

        send_result = {"member_id": self.member_id, "role": self.role, "result": fill_result}
        members = [send_result]

        if self.role == consts.PROVIDER:
            self.transfer_variable.fill_missing_value_result.remote(send_result, role=consts.PROMOTER)
        elif self.role == consts.PROMOTER:
            provider_fill_missing_value_results = self.transfer_variable.fill_missing_value_result.get(-1)
            for provider_result in provider_fill_missing_value_results:
                members.append(provider_result)

        LOGGER.info(f"fill_result:{members}")
        self.tracker.save_task_result({'members': members}, self.task_result_type)

        return new_data_instances

    def generate_fill_result(self):
        results = {}
        for feature, rule in self.features_rules.items():
            result = copy.deepcopy(rule)
            method = rule['method']
            result['missing_count'] = self.statistics['missing_count'][feature]
            if method != 'const':
                result['value'] = self.statistics[method][feature]

            results[feature] = result

        return results

    def get_fill_methods(self):
        methods = set()
        for feature in self.features_rules:
            rule = self.features_rules[feature]
            method = rule['method']
            methods.add(method)
        return methods

    def calc_statistics(self, data_instances):
        self.statistics = {}
        statistics = MultivariateStatistical(data_instances=data_instances)
        self.multivariateStatistical = statistics
        self.statistics['missing_count'] = statistics.get_missing_count()
        if len(self.methods) == 1 and 'const' in self.methods:
            return

        for method in self.methods:
            if method == 'max':
                self.statistics['max'] = statistics.get_max()
            elif method == 'min':
                self.statistics['min'] = statistics.get_min()
            elif method == 'mean':
                self.statistics['mean'] = statistics.get_mean()
            elif method == 'median':
                self.statistics['median'] = statistics.get_percentile(percentage=50)
            elif method == 'mode':
                self.statistics['mode'] = statistics.get_mode()
            elif method == 'const':
                continue
            else:
                raise ValueError('{} method not support'.format(method))

    def output_data(self):
        return self.data_instances

    def calc_missing_count(self, data_instance):
        def _check_empty(iterator):
            result = {}
            for feature in self.header:
                result[feature] = 0
            for k, v in iterator:
                for idx in range(len(self.header)):
                    if np.isnan(v.features[idx]):
                        result[self.header[idx]] = result[self.header[idx]] + 1
            return result

        def _merge(a, b):
            for feature in a.keys():
                a[feature] = a[feature] + b[feature]
            return a

        result = data_instance.mapPartitions(_check_empty).reduce(_merge)
        return result


def fill_value(features_rules, statistics, data_value, header):
    for feature in features_rules:
        feature_index = header.index(feature)
        if feature_index < 0:
            continue
        if np.isnan(data_value.features[feature_index]):
            value = get_statistic_value(features_rules, statistics, feature=feature)
            data_value.features[feature_index] = value
    return data_value


def get_statistic_value(features_rules, statistics, feature=None):
    rule = features_rules[feature]
    method = rule['method']
    if method == 'const':
        return rule['value']
    return statistics[method].get(feature)
