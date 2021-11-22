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
from common.python.common.exception.custom_exception import CommonCustomError

from common.python.utils import log_utils
from kernel.components.feature.filter.vertfilter.param import VertSampleFilterParam
from kernel.model_base import ModelBase
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()

MODEL_PARAM_NAME = 'VertSampleFilterParam'
MODEL_META_NAME = 'VertSampleFilterMeta'
MODEL_NAME = 'VertSampleFilter'


class VertSampleFilter(ModelBase):
    def __init__(self, params: VertSampleFilterParam = None):
        super(VertSampleFilter, self).__init__()
        self.model_name = MODEL_NAME
        self.model_param_name = MODEL_PARAM_NAME
        self.model_meta_name = MODEL_META_NAME
        if params:
            self.model_param = params
        else:
            self.model_param = VertSampleFilterParam()
        self.header = None
        self.feature_names = set()
        self.feature_idx = {}
        self.filter_rules = None
        self.statistics = None
        self.feature_filter_rules = {}

        self.metric_name = MODEL_NAME
        self.metric_namespace = "filter"

    def _init_param(self):
        self.filter_rules = self.model_param.filter_rules

    @staticmethod
    def parse_express(value):
        operators = ['>=', '<=', '!=', '>', '<', '=']
        for operator in operators:
            if operator not in value:
                continue
            return value.partition(operator)

    def parse_params(self):
        feature_filters = self.filter_rules.split('&')
        for feature_filter in feature_filters:
            result = self.parse_express(feature_filter)
            feature_name = result[0]
            if feature_name not in self.header:
                raise ValueError(f'{feature_name} not in data features')
            operator = result[1]
            try:
                value = float(result[2])
            except Exception:
                raise ValueError(f"{self.filter_rules} format wrong")
            feature = {}
            if feature_name in self.feature_filter_rules.keys():
                feature = self.feature_filter_rules[feature_name]
            feature['operator'] = operator
            feature['idx'] = self.header.index(feature_name)
            if operator == '>=' or operator == '>':
                feature['min'] = value
                if operator == '>=':
                    feature['left'] = True
            elif operator == '<=' or operator == '<':
                feature['max'] = value
                if operator == '<=':
                    feature['right'] = True
            elif operator == '!=' or operator == '=':
                feature['min'] = value
            else:
                raise ValueError(f'{operator} not support')
            self.feature_filter_rules[feature_name] = feature

    def process_value(self, value):
        flag = True
        for feature_name, feature in self.feature_filter_rules.items():
            idx = feature['idx']
            real_value = value.features[idx]
            operator = feature['operator']
            if operator == '=':
                if real_value != feature['min']:
                    flag = False
                    break
            elif operator == '!=':
                if real_value == feature['min']:
                    flag = False
                    break
            else:
                min = feature['min'] if 'min' in feature.keys() else float('-inf')
                max = feature['max'] if 'max' in feature.keys() else float('inf')
                right = feature['right'] if 'right' in feature.keys() else False
                left = feature['left'] if 'left' in feature.keys() else False
                if left and real_value < min:
                    flag = False
                    break
                if not left and real_value <= min:
                    flag = False
                    break
                if right and real_value > max:
                    flag = False
                    break
                if not right and real_value >= max:
                    flag = False
                    break

        if not flag:
            value.features = []
        return value

    def fit(self, data_instances):
        self._init_param()
        self.header = get_header(data_instances)
        LOGGER.info(f'origin data:{data_instances.first()[1].features}')

        self.parse_params()

        new_data_instances = data_instances.mapValues(lambda v: self.process_value(v))
        new_data_instances = new_data_instances.filter(lambda k, v: v is not None and len(v.features) > 0)
        new_data_instances.schema = data_instances.schema
        LOGGER.debug(f'schema={new_data_instances.schema},  count={new_data_instances.count()}')
        metric_data = [("count", new_data_instances.count())]
        LOGGER.info(f'metric_data: {metric_data}, metric_name:{self.metric_name}')
        self.tracker.saveMetricData(self.metric_name, self.metric_namespace, None, metric_data)
        if new_count == 0:
            raise CommonCustomError(message="sample filter result is zero")
        return new_data_instances
