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


import math

from common.python.utils import log_utils
from kernel.base.statistics import MultivariateStatistical
from kernel.components.featurestandardized.param import FeatureStandardizedParam
from kernel.model_base import ModelBase
from kernel.utils import consts, abnormal_detection
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()


class FeatureStandardized(ModelBase):

    def __init__(self, standardized_param=FeatureStandardizedParam()):
        super(FeatureStandardized, self).__init__()
        if standardized_param:
            self.model_param = standardized_param
        else:
            self.model_param = FeatureStandardizedParam()
        self.data_instances = None
        self.statistics = {}

        self.set_show_name("(Standardized)")

    def _init_param(self):
        self.save_dataset = self.model_param.save_dataset
        self.with_label = self.model_param.with_label
        self.method = self.model_param.method
        self.fields = self.model_param.fields

    def fit(self, data_instances):
        self._init_param()
        abnormal_detection.empty_table_detection(data_instances)
        if not data_instances.schema:
            data_instances.schema = data_instances.get_metas()
        header = get_header(data_instances)
        if self.fields is None:
            self.fields = header
        self.calc_statistics(data_instances)
        if self.method == 'min-max':
            new_data_instances = data_instances.mapValues(lambda v: self.fill_value(v))
        else:
            new_data_instances = data_instances.mapValues(lambda v: self.fill_value_std(v))
        new_data_instances.schema = data_instances.schema
        self.data_instances = new_data_instances
        return data_instances

    def calc_statistics(self, data_instances):
        statistics = self.tracker.get_statics_result()
        if statistics:
            self.statistics = statistics
            return
        statistics = MultivariateStatistical(data_instances=data_instances)
        if self.method == 'min-max':
            self.statistics['min'] = statistics.get_min()
            self.statistics['max'] = statistics.get_max()
        else:
            self.statistics['mean'] = statistics.get_mean()
            self.statistics['std'] = statistics.get_std_variance()

    def fill_value(self, data_value):
        for i, feature in enumerate(self.fields):
            feature_max = self.statistics['max'].get(feature)
            feature_min = self.statistics['min'].get(feature)
            old_value = data_value.features[i]
            new_value = old_value
            if math.fabs(feature_max - feature_min) > consts.FLOAT_ZERO:
                new_value = (old_value - feature_min) / (feature_max - feature_min)
            data_value.features[i] = new_value
        return data_value

    def fill_value_std(self, data_value):
        for i, feature in enumerate(self.fields):
            feature_mean = self.statistics['mean'].get(feature)
            feature_std = self.statistics['std'].get(feature)
            old_value = data_value.features[i]
            new_value = old_value
            if math.fabs(feature_std) > consts.FLOAT_ZERO:
                new_value = (old_value - feature_mean) / feature_std
            data_value.features[i] = new_value
        return data_value

    def output_data(self):
        return self.data_instances
