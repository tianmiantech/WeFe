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



import abc
import math

from common.python.utils import log_utils
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.components.featurecalculation.base import calculation_info_sync
from kernel.components.featurecalculation.base.filter_base import BaseFilterMethod
from kernel.components.featurecalculation.base.iv_value_calculate_filter import fit_iv_values
from kernel.components.featurecalculation.param import IVPercentileCalculationParam
from kernel.protobuf.generated import feature_calculation_meta_pb2
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class IVPercentileFilter(BaseFilterMethod, metaclass=abc.ABCMeta):
    """
    filter the columns if iv value is less than a percentile threshold
    """

    def __init__(self, filter_param):
        super().__init__(filter_param)
        self.transfer_variable = None
        self.binning_obj: BaseVertFeatureBinning = None
        self.local_only = False
        self.sync_obj = None

    def set_transfer_variable(self, transfer_variable):
        self.transfer_variable = transfer_variable
        self.sync_obj.register_calculation_trans_vars(transfer_variable)

    def _parse_filter_param(self, filter_param):
        self.percentile_threshold = filter_param.percentile_threshold
        self.local_only = filter_param.local_only

    def set_binning_obj(self, binning_model):
        if binning_model is None:
            raise ValueError("To use iv filter, binning module should be called and setup in 'isomatric_model'"
                             " input for feature calculation.")
        self.binning_obj = binning_model


class Promoter(IVPercentileFilter):
    def __init__(self, filter_param: IVPercentileCalculationParam):
        super().__init__(filter_param)
        self.provider_calculation_properties = []
        self.sync_obj = calculation_info_sync.Promoter()

    def fit(self, data_instances, suffix):
        if not self.local_only:
            self.provider_calculation_properties = self.sync_obj.sync_calculate_cols(suffix=suffix)

        value_threshold = self.get_value_threshold()
        self.calculation_properties = fit_iv_values(self.binning_obj.binning_obj,
                                                    value_threshold,
                                                    self.calculation_properties)

        if not self.local_only:
            for provider_id, provider_binning_obj in enumerate(self.binning_obj.provider_results):
                fit_iv_values(provider_binning_obj,
                              value_threshold,
                              self.provider_calculation_properties[provider_id])
            self.sync_obj.sync_calculate_results(self.provider_calculation_properties, suffix=suffix)
        return self

    def get_value_threshold(self):
        total_values = []
        for col_name, col_results in self.binning_obj.binning_obj.bin_results.all_cols_results.items():
            if col_name in self.calculation_properties.calculate_col_names:
                total_values.append(col_results.iv)

        if not self.local_only:
            LOGGER.debug("provider_results: {}, provider_calculation_properties: {}".format(
                self.binning_obj.provider_results, self.provider_calculation_properties
            ))

            for provider_id, provider_binning_obj in enumerate(self.binning_obj.provider_results):
                provider_calculate_param = self.provider_calculation_properties[provider_id]
                for col_name, col_results in provider_binning_obj.bin_results.all_cols_results.items():
                    if col_name in provider_calculate_param.calculate_col_names:
                        total_values.append(col_results.iv)
        sorted_value = sorted(total_values, reverse=True)
        thres_idx = int(math.floor(self.percentile_threshold * len(sorted_value) - consts.FLOAT_ZERO))
        return sorted_value[thres_idx]

    def get_meta_obj(self, meta_dicts):
        result = feature_calculation_meta_pb2.IVPercentileCalculationMeta(
            percentile_threshold=self.percentile_threshold,
            local_only=self.local_only)
        meta_dicts['iv_percentile_meta'] = result
        return meta_dicts


class Provider(IVPercentileFilter):
    def __init__(self, filter_param: IVPercentileCalculationParam):
        super().__init__(filter_param)
        self.sync_obj = calculation_info_sync.Provider()

    def _parse_filter_param(self, filter_param):
        self.local_only = False

    def fit(self, data_instances, suffix):
        encoded_names = self.binning_obj.bin_inner_param.encode_col_name_list(
            self.calculation_properties.calculate_col_names)
        self.sync_obj.sync_calculate_cols(encoded_names, suffix=suffix)
        self.sync_obj.sync_calculate_results(self.calculation_properties,
                                             decode_func=self.binning_obj.bin_inner_param.decode_col_name,
                                             suffix=suffix)
        return self

    def get_meta_obj(self, meta_dicts):
        result = feature_calculation_meta_pb2.IVPercentileCalculationMeta(local_only=self.local_only)
        meta_dicts['iv_percentile_meta'] = result
        return meta_dicts
