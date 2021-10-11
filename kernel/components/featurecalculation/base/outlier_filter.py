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



from common.python.utils import log_utils
from kernel.base.statics import MultivariateStatisticalSummary
from kernel.components.featurecalculation.base.filter_base import BaseFilterMethod
from kernel.components.featurecalculation.param import OutlierColsCalculationParam
from kernel.protobuf.generated import feature_calculation_meta_pb2

LOGGER = log_utils.get_logger()


class OutlierFilter(BaseFilterMethod):
    """
    Filter the columns if coefficient of variance is less than a threshold.
    """

    def __init__(self, filter_param: OutlierColsCalculationParam):
        super().__init__(filter_param)
        self.statics_obj = None

    def _parse_filter_param(self, filter_param: OutlierColsCalculationParam):
        self.percentile = filter_param.percentile
        self.upper_threshold = filter_param.upper_threshold

    def set_statics_obj(self, statics_obj):
        self.statics_obj = statics_obj

    def fit(self, data_instances, suffix):
        if self.statics_obj is None:
            self.statics_obj = MultivariateStatisticalSummary(data_instances)

        quantile_points = self.statics_obj.get_quantile_point(self.percentile)

        for col_name in self.calculation_properties.calculate_col_names:
            quantile_value = quantile_points.get(col_name)
            if quantile_value < self.upper_threshold:
                self.calculation_properties.add_left_col_name(col_name)
            self.calculation_properties.add_feature_value(col_name, quantile_value)
        self._keep_one_feature(pick_high=True)
        return self

    def get_meta_obj(self, meta_dicts):
        result = feature_calculation_meta_pb2.OutlierColsCalculationMeta(percentile=self.percentile,
                                                                         upper_threshold=self.upper_threshold)
        meta_dicts['outlier_meta'] = result
        return meta_dicts
