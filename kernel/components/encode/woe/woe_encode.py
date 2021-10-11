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


import functools

from common.python.utils import log_utils
from kernel.components.binning.core.base_binning import Binning
from kernel.components.binning.core.bin_inner_param import BinInnerParam
from kernel.components.binning.core.bin_result import BinResults, BinColResults
from kernel.components.encode.woe.param import WoeEncodeParam
from kernel.model_base import ModelBase

LOGGER = log_utils.get_logger()


class WoeEncode(ModelBase):
    def __init__(self):
        super(WoeEncode, self).__init__()
        self.model_param = WoeEncodeParam()

    def fit(self, data_instances):
        bin_inner_param, bin_results = self._get_binning_result()
        f = functools.partial(Binning._convert_dense_data,
                              bin_inner_param=bin_inner_param,
                              bin_results=bin_results,
                              abnormal_list=[],
                              convert_type='woe')
        new_data = data_instances.mapValues(f)
        new_data.schema = data_instances.schema
        LOGGER.info(f'new_data schema={new_data.schema}')
        LOGGER.info(f'data_instances={data_instances.first()[1]}')
        LOGGER.info(f'new_data={new_data.first()[1]}')
        return new_data

    def _get_binning_result(self):
        model_param, binning_results = self.tracker.get_binning_result()
        if binning_results is None:
            raise ValueError('not find binning result')
        bin_inner_param = BinInnerParam()
        bin_inner_param.header = model_param.get('header')
        bin_inner_param.transform_bin_indexes = model_param.get('transform_bin_indexes')
        binResults = BinResults()
        all_cols_results = {}
        LOGGER.debug(f'binning_results={binning_results}')
        for feature, result in binning_results.items():
            binColResults = BinColResults(woe_array=result.get('woe'), iv=-99)
            binColResults.set_split_points(result.get('split_points'))
            all_cols_results[feature] = binColResults
        binResults.all_cols_results = all_cols_results
        return bin_inner_param, binResults
