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


import numpy as np

from common.python.utils import log_utils
from kernel.components.feature.scale.vertscale.param import VertFeatureScaleParam
from kernel.model_base import ModelBase
from kernel.utils import abnormal_detection
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()


class VertFeatureScale(ModelBase):
    def __init__(self, params: VertFeatureScaleParam = None):
        super(VertFeatureScale, self).__init__()
        self.model_name = 'VertFeatureScale'
        self.model_param_name = 'VertFeatureScaleParam'
        self.model_meta_name = 'VertFeatureScaleMeta'
        if params:
            self.model_param = params
        else:
            self.model_param = VertFeatureScaleParam()
        self.header = None
        self.feature_names = set()
        self.feature_idx = {}
        self.scale_rules = None

    def _init_param(self):
        import json
        self.scale_rules = json.loads(self.model_param.scale_rules)
        self.feature_names = set(self.scale_rules.keys())

    def scale_function(self, feature_name, value):
        fun = self.scale_rules[feature_name]
        if fun == 'log2':
            return np.log2(value)
        elif fun == 'log10':
            return np.log10(value)
        elif fun == 'ln':
            return np.log(value)
        elif fun == 'abs':
            return np.abs(value)
        elif fun == 'sqrt':
            return np.sqrt(value)
        else:
            return value

    def scale_value(self, data_value):
        for feature_name in self.feature_names:
            feature_index = self.feature_idx.get(feature_name)
            value = data_value.features[feature_index]
            if value == '' or value is None:
                continue
            new_value = self.scale_function(feature_name, value)
            data_value.features[feature_index] = new_value
        return data_value

    def calc_feature_idx(self):
        for feature in self.feature_names:
            if feature not in self.header:
                raise ValueError(f'param error, {feature} is not data_instance feature')
            feature_idx = self.header.index(feature)
            self.feature_idx[feature] = feature_idx

    def fit(self, data_instances):
        self._init_param()
        abnormal_detection.empty_table_detection(data_instances)
        self.header = get_header(data_instances)
        LOGGER.info("origin data:{}".format(data_instances.first()[1].features))
        self.calc_feature_idx()

        new_data_instances = data_instances.mapValues(
            lambda v: self.scale_value(v)
        )
        new_data_instances.schema = data_instances.schema
        LOGGER.info("data_instances schema:{}".format(new_data_instances.schema))
        LOGGER.info("new data:{}".format(new_data_instances.first()[1].features))
        return new_data_instances
