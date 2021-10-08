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


from scipy import stats

from common.python.utils import log_utils
from kernel.base.statistics import MultivariateStatistical
from kernel.components.feature.soften.vertsoften.param import VertFeatureSoftenParam
from kernel.model_base import ModelBase
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()


class VertFeatureSoften(ModelBase):
    def __init__(self, params: VertFeatureSoftenParam = None):
        super(VertFeatureSoften, self).__init__()
        self.soften = 'VertFeatureSoften'
        self.model_name = self.soften
        self.model_param_name = 'VertFeatureSoftenParam'
        self.model_meta_name = 'VertFeatureSoftenMeta'
        if params:
            self.model_param = params
        else:
            self.model_param = VertFeatureSoftenParam()
        self.header = None
        self.feature_names = set()
        self.feature_idx = {}
        self.soften_rules = None
        self.statistics = None
        self.feature_intervals = {}

    def _init_param(self):
        import json
        self.soften_rules = json.loads(self.model_param.soften_rules)
        self.feature_names = set(self.soften_rules.keys())

    def soften_value(self, data_value):
        for feature_name in self.feature_names:
            feature_interval = self.feature_intervals[feature_name]
            feature_index = feature_interval['idx']
            value = data_value.features[feature_index]
            if value == '' or value is None:
                continue
            min_value = feature_interval['min_value']
            max_value = feature_interval['max_value']
            new_value = value
            if value < min_value:
                new_value = min_value
            elif value > max_value:
                new_value = max_value
            data_value.features[feature_index] = new_value
        return data_value

    def calc_interval(self, data_instances):
        old_statistics = self.tracker.get_statics_result()
        if old_statistics is None:
            raise ValueError('not find statistics result, please run statistic component')
        statistics = None
        for feature, rule in self.soften_rules.items():
            if feature not in self.header:
                raise ValueError(f'{feature} is not data features')
            method = rule['method']
            min = rule['min']
            max = rule['max']
            idx = self.header.index(feature)
            if method == 'z_score':
                level = min
                mean = self.calc_statistic(old_statistics, statistics, feature, 'mean', data_instances)
                std_variance = self.calc_statistic(old_statistics, statistics, feature, 'std_variance', data_instances)
                interval = stats.norm.interval(level, loc=mean, scale=std_variance)
                min_value = interval[0]
                max_value = interval[1]

            elif method == 'min_max_per':
                if min < 0 or max > 100:
                    raise ValueError(f'{feature} params error')
                if min < 1:
                    min_value = self.calc_statistic(old_statistics, statistics, feature, 'min', data_instances)
                else:
                    min_value = self.calc_statistic(old_statistics, statistics, feature, 'percentile', data_instances,
                                                    min)
                max_value = self.calc_statistic(old_statistics, statistics, feature, 'percentile', data_instances, max)
            elif method == 'min_max_thresh':
                min_value = min
                max_value = max
            else:
                raise ValueError(f'feature soften not support {method} method')
            self.feature_intervals[feature] = {'min_value': min_value, 'max_value': max_value, 'idx': idx}
        LOGGER.info(f'feature_intervals={self.feature_intervals}')

    @staticmethod
    def calc_statistic(old_statistics, statistics, feature, target, data_instances, percentile=0):
        if target == 'mean' or target == 'std_variance' or target == 'min':
            if old_statistics is not None:
                return old_statistics['mean'][feature]
            else:
                if statistics is None:
                    statistics = MultivariateStatistical(data_instances=data_instances)
                if target == 'mean':
                    return statistics.get_mean()[feature]
                elif target == 'std_variance':
                    return statistics.get_std_variance()[feature]
                elif target == 'min':
                    return statistics.get_min()[feature]
        elif target == 'percentile':
            if statistics is None:
                statistics = MultivariateStatistical(data_instances=data_instances)
            return statistics.get_percentile(percentage=percentile)[feature]

    def fit(self, data_instances):
        self._init_param()
        self.header = get_header(data_instances)
        LOGGER.info("origin data:{}".format(data_instances.first()[1].features))
        self.calc_interval(data_instances)

        new_data_instances = data_instances.mapValues(
            lambda v: self.soften_value(v)
        )
        new_data_instances.schema = data_instances.schema
        LOGGER.info("data_instances schema:{}".format(new_data_instances.schema))
        LOGGER.info("new data:{}".format(new_data_instances.first()[1].features))
        return new_data_instances
