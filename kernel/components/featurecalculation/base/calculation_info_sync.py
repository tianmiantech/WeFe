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
from kernel.components.featurecalculation.base.calculation_properties import CalculationProperties
from kernel.transfer.variables.transfer_class.vert_feature_calculation_transfer_variable import \
    VertFeatureCalculationTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class Promoter(object):
    # noinspection PyAttributeOutsideInit
    def register_calculation_trans_vars(self, transfer_variable):
        self._provider_calculate_cols_transfer = transfer_variable.provider_calculate_cols
        self._result_left_cols_transfer = transfer_variable.result_left_cols

    def sync_calculate_cols(self, suffix=tuple()):
        provider_calculate_col_names = self._provider_calculate_cols_transfer.get(idx=-1, suffix=suffix)
        provider_calculation_params = []
        for provider_id, calculate_names in enumerate(provider_calculate_col_names):
            provider_calculation_properties = CalculationProperties()
            provider_calculation_properties.set_header(calculate_names)
            provider_calculation_properties.set_last_left_col_indexes([x for x in range(len(calculate_names))])
            provider_calculation_properties.add_calculate_col_names(calculate_names)
            provider_calculation_params.append(provider_calculation_properties)
        return provider_calculation_params

    def sync_calculate_results(self, provider_calculation_inner_params, suffix=tuple()):
        for provider_id, provider_calculate_results in enumerate(provider_calculation_inner_params):
            LOGGER.debug(
                "Send provider calculated result, left_col_names: {}".format(provider_calculate_results.left_col_names))
            self._result_left_cols_transfer.remote(provider_calculate_results.left_col_names,
                                                   role=consts.PROVIDER,
                                                   idx=provider_id,
                                                   suffix=suffix)


class Provider(object):
    # noinspection PyAttributeOutsideInit
    def register_calculation_trans_vars(self, transfer_variable: VertFeatureCalculationTransferVariable):
        self._provider_calculate_cols_transfer = transfer_variable.provider_calculate_cols
        self._result_left_cols_transfer = transfer_variable.result_left_cols

    def sync_calculate_cols(self, encoded_names, suffix=tuple()):
        self._provider_calculate_cols_transfer.remote(encoded_names,
                                                      role=consts.PROMOTER,
                                                      idx=0,
                                                      suffix=suffix)

    def sync_calculate_results(self, calculation_param, decode_func=None, suffix=tuple()):
        left_cols_names = self._result_left_cols_transfer.get(idx=0, suffix=suffix)
        for col_name in left_cols_names:
            if decode_func is not None:
                col_name = decode_func(col_name)
            calculation_param.add_left_col_name(col_name)
        LOGGER.debug("Received provider calculated result, original left_cols: {},"
                     " left_col_names: {}".format(left_cols_names, calculation_param.left_col_names))
