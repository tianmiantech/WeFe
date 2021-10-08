#!/usr/bin/env python
# -*- coding: utf-8 -*-

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

from common.python.utils import log_utils
from kernel.components.featurecalculation.base import calculation_info_sync
from kernel.components.featurecalculation.base.calculation_properties import CalculationProperties
from kernel.components.featurecalculation.base.filter_base import BaseFilterMethod
from kernel.components.featurecalculation.param import IVValueCalculationParam
from kernel.protobuf.generated import feature_calculation_meta_pb2

LOGGER = log_utils.get_logger()


def fit_iv_values(binning_model, threshold, calculation_param: CalculationProperties):
    alternative_col_name = None
    for col_name, col_results in binning_model.bin_results.all_cols_results.items():
        if col_name not in calculation_param.calculate_col_names:
            LOGGER.debug("col_name not in calculation_param.calculate_col_names")
            LOGGER.debug("col_name: {}, calculation_param.calculate_col_names: {}".format(col_name,
                                                                                          str(calculation_param.calculate_col_names)))
            continue
        alternative_col_name = col_name
        iv = col_results.iv
        LOGGER.debug(
            "fit_iv_values.alternative_col_name: {}, iv: {}, threshold: {}".format(alternative_col_name, iv, threshold))

        # if iv > threshold:
        #     calculation_param.add_left_col_name(col_name)
        calculation_param.add_left_col_name(col_name)
        calculation_param.add_feature_value(col_name, iv)
    if len(calculation_param.all_left_col_names) == 0:
        assert alternative_col_name is not None
        calculation_param.add_left_col_name(alternative_col_name)
    return calculation_param


class IVValueCalculateFilter(BaseFilterMethod, metaclass=abc.ABCMeta):
    """
    filter the columns if iv value is less than a threshold
    """

    def __init__(self, filter_param: IVValueCalculationParam):
        super().__init__(filter_param)
        self.binning_obj = None
        self.local_only = False
        self.transfer_variable = None
        self.sync_obj = None

    def set_transfer_variable(self, transfer_variable):
        self.transfer_variable = transfer_variable
        self.sync_obj.register_calculation_trans_vars(transfer_variable)

    def set_binning_obj(self, binning_model):
        if binning_model is None:
            raise ValueError("To use iv filter, binning module should be called and setup in 'isomatric_model'"
                             " input for feature calculation.")
        self.binning_obj = binning_model


class Promoter(IVValueCalculateFilter):
    def __init__(self, filter_param: IVValueCalculationParam):
        super().__init__(filter_param)
        self.provider_thresholds = None
        self.provider_calculation_properties = []
        self.sync_obj = calculation_info_sync.Promoter()

    def _parse_filter_param(self, filter_param):
        self.value_threshold = filter_param.value_threshold
        self.provider_thresholds = filter_param.provider_thresholds
        self.local_only = filter_param.local_only

    def fit(self, data_instances, suffix):
        self.calculation_properties = fit_iv_values(self.binning_obj.binning_obj,
                                                    self.value_threshold,
                                                    self.calculation_properties)
        if not self.local_only:
            self.provider_calculation_properties = self.sync_obj.sync_calculate_cols(suffix=suffix)
            for provider_id, provider_properties in enumerate(self.provider_calculation_properties):
                if self.provider_thresholds is None:
                    threshold = self.value_threshold
                else:
                    threshold = self.provider_thresholds[provider_id]

                fit_iv_values(self.binning_obj.provider_results[provider_id],
                              threshold,
                              provider_properties)

            self.sync_obj.sync_calculate_results(self.provider_calculation_properties, suffix=suffix)
        return self

    def get_meta_obj(self, meta_dicts):
        result = feature_calculation_meta_pb2.IVValueCalculationMeta(value_threshold=self.value_threshold,
                                                                     local_only=self.local_only)
        meta_dicts['iv_value_meta'] = result
        return meta_dicts


class Provider(IVValueCalculateFilter):
    def __init__(self, filter_param: IVValueCalculationParam):
        super().__init__(filter_param)
        self.sync_obj = calculation_info_sync.Provider()
        self.value_threshold = filter_param.value_threshold
        self.provider_thresholds = filter_param.provider_thresholds

    def _parse_filter_param(self, filter_param):
        self.local_only = False

    def fit(self, data_instances, suffix):
        #
        self.calculation_properties = fit_iv_values(self.binning_obj.binning_obj,
                                                    self.value_threshold,
                                                    self.calculation_properties)

        encoded_names = self.binning_obj.bin_inner_param.encode_col_name_list(
            self.calculation_properties.calculate_col_names)
        LOGGER.debug("calculation_properties.calculate_col_names: {}, encoded_names: {}".format(
            self.calculation_properties.calculate_col_names, encoded_names
        ))

        self.sync_obj.sync_calculate_cols(encoded_names, suffix=suffix)
        self.sync_obj.sync_calculate_results(self.calculation_properties,
                                             decode_func=self.binning_obj.bin_inner_param.decode_col_name,
                                             suffix=suffix)
        LOGGER.debug("In fit calculated result, left_col_names: {}".format(self.calculation_properties.left_col_names))
        return self

    def get_meta_obj(self, meta_dicts):
        result = feature_calculation_meta_pb2.IVValueCalculationMeta(local_only=self.local_only)
        meta_dicts['iv_value_meta'] = result
        return meta_dicts
