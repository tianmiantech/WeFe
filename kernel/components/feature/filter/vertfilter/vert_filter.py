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
import math
from common.python.common.exception.custom_exception import CommonCustomError

from common.python.utils import log_utils
from kernel.components.feature.filter.vertfilter.param import VertSampleFilterParam
from kernel.model_base import ModelBase
from kernel.transfer.variables.transfer_class.vert_filter_transfer_variable import VertFilterTransferVariable
from kernel.utils import consts
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
        self.transfer_variable = VertFilterTransferVariable()
        self.set_show_name("(Data VertFilter)")
        self.metric_name = MODEL_NAME
        self.metric_namespace = "train"
        self.need_data_check = False

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
            value_type = 'num'
            try:
                value = float(result[2])
            except Exception:
                value_type = 'str'
                value = result[2].strip()
                LOGGER.warn(f'{feature_name} in rule {feature_filter} is str')
                # raise ValueError(f"{self.filter_rules} format wrong")
            feature = {}
            if feature_name in self.feature_filter_rules.keys():
                feature = self.feature_filter_rules[feature_name]
            feature['value_type'] = value_type
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
        LOGGER.info('feature_filter_rules:{}'.format(self.feature_filter_rules))

    def process_value(self, value):
        flag = False
        for feature_name, feature in self.feature_filter_rules.items():
            idx = feature['idx']
            real_value = value.features[idx]
            operator = feature['operator']
            value_type = feature['value_type']
            if operator == '=':
                if value_type == 'num':
                    if math.fabs(real_value - feature['min']) > consts.FLOAT_ZERO:
                        flag = True
                # str
                elif real_value != feature['min']:
                    flag = True

            elif operator == '!=':
                if value_type == 'num':
                    if math.fabs(real_value - feature['min']) <= consts.FLOAT_ZERO:
                        flag = True
                # str
                elif real_value == feature['min']:
                    flag = True
            # >, >=, <, <=
            else:
                value_min = feature['min'] if 'min' in feature.keys() else None
                value_max = feature['max'] if 'max' in feature.keys() else None
                right = feature['right'] if 'right' in feature.keys() else False
                left = feature['left'] if 'left' in feature.keys() else False
                if value_min is not None:
                    if real_value < value_min:
                        flag = True
                    if not left:
                        if value_type == 'num' and math.fabs(real_value - value_min) < consts.FLOAT_ZERO:
                            flag = True
                        elif real_value == value_min:
                            flag = True
                if value_max is not None:
                    if real_value > value_max:
                        flag = True
                    if not right:
                        if value_type == 'num' and math.fabs(real_value - value_max) < consts.FLOAT_ZERO:
                            flag = True
                        elif real_value == value_max:
                            flag = True
        if flag:
            value.features = None
        return value

    def fit(self, data_instances):
        LOGGER.info('start vert filter')
        self._init_param()
        self.header = get_header(data_instances)
        LOGGER.info(f'origin data:{data_instances.first()[1].features}')

        self.parse_params()

        new_data_instances = data_instances.mapValues(lambda v: self.process_value(v))
        new_data_instances = new_data_instances.filter(lambda k, v: v is not None and len(v.features) > 0)

        send_data_instances = new_data_instances.mapValues(lambda v: 1)

        if consts.PROMOTER == self.role:
            LOGGER.debug('send_data_instances.count(): {}'.format(send_data_instances.count()))
            self.transfer_variable.promoter_ids.remote(send_data_instances)
            other_data_instances = self.transfer_variable.provider_ids.get(-1)[0]
            LOGGER.debug('other_data_instances.count(): {}'.format(other_data_instances.count()))
        else:
            LOGGER.debug('send_data_instances.count(): {}'.format(send_data_instances.count()))
            self.transfer_variable.provider_ids.remote(send_data_instances)
            other_data_instances = self.transfer_variable.promoter_ids.get(-1)[0]
            LOGGER.debug('other_data_instances.count(): {}'.format(other_data_instances.count()))

        new_data_instances = new_data_instances.join(other_data_instances, lambda v, v1: v)
        new_data_instances.schema = data_instances.schema

        new_count = new_data_instances.count()
        LOGGER.debug(f'schema={new_data_instances.schema},  count={new_data_instances.count()}')
        metric_data = [("count", new_data_instances.count()), ('feature_num', len(self.header))]
        LOGGER.info(f'metric_data: {metric_data}, metric_name:{self.metric_name}')
        self.tracker.saveMetricData(self.metric_name, self.metric_namespace, None, metric_data)
        if new_count == 0:
            raise CommonCustomError(message="数据集过滤结果为0，请检查过滤条件.")
        return new_data_instances
