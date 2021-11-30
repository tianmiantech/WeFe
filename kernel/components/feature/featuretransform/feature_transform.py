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


from common.python.utils import log_utils
from kernel.components.feature.featuretransform.param import FeatureTransformParam
from kernel.model_base import ModelBase
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()


class FeatureTransform(ModelBase):
    def __init__(self, params: FeatureTransformParam = None):
        super(FeatureTransform, self).__init__()
        self.model_name = 'FeatureTransform'
        self.model_param_name = 'FeatureTransformParam'
        self.model_meta_name = 'FeatureTransformMeta'
        if params:
            self.model_param = params
        else:
            self.model_param = FeatureTransformParam()
        self.feature_names = set()
        self.header = None
        self.schema = None

    def _init_param(self):
        import json
        self.transform_rules = json.loads(self.model_param.transform_rules)

    def transform_features(self):
        column_types = self.schema.get('column_types', None)
        for feature in self.transform_rules:
            if feature not in self.header:
                LOGGER.warning("{} not in data header:{}".format(feature, self.header))
                continue
            self.feature_names.add(feature)
            if column_types:
                index = self.header.index(feature)
                column_types[index] = 'Integer'

    def transform_value(self, data_value):
        for feature_name in self.feature_names:
            if feature_name not in self.header:
                continue
            feature_index = self.header.index(feature_name)
            value = data_value.features[feature_index]
            if value == '' or value is None:
                continue
            new_value = self.transform_rules[feature_name][value]
            data_value.features[feature_index] = new_value
        return data_value

    def fit(self, data_instances):
        self._init_param()
        self.header = get_header(data_instances)
        self.schema = data_instances.schema
        LOGGER.info(f'origin data:{data_instances.first()[1].features}, header:{self.header}')

        self.transform_features()

        new_data_instances = data_instances.mapValues(
            lambda v: self.transform_value(v)
        )
        new_data_instances.schema = self.schema
        LOGGER.info(f'schema:{new_data_instances.schema}, new data:{new_data_instances.first()[1].features}')
        return new_data_instances
