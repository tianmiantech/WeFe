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



import operator

from common.python.utils import log_utils
from kernel.protobuf.generated import feature_calculation_param_pb2

LOGGER = log_utils.get_logger()


class CalculationProperties(object):
    def __init__(self):
        self.header = []
        self.col_name_maps = {}
        self.last_left_col_indexes = []
        self.calculate_col_indexes = []
        self.calculate_col_names = []
        self.left_col_indexes = []
        self.left_col_names = []
        self.feature_values = {}

    def set_header(self, header):
        self.header = header
        for idx, col_name in enumerate(self.header):
            self.col_name_maps[col_name] = idx

    def set_last_left_col_indexes(self, left_cols):
        self.last_left_col_indexes = left_cols.copy()

    def set_calculate_all_cols(self):
        self.calculate_col_indexes = [i for i in range(len(self.header))]
        self.calculate_col_names = self.header

    def add_calculate_col_indexes(self, calculate_col_indexes):
        for idx in calculate_col_indexes:
            if idx >= len(self.header):
                LOGGER.warning("Adding a index that out of header's bound")
                continue
            if idx not in self.last_left_col_indexes:
                continue

            if idx not in self.calculate_col_indexes:
                self.calculate_col_indexes.append(idx)
                self.calculate_col_names.append(self.header[idx])

    def add_calculate_col_names(self, calculate_col_names):
        for col_name in calculate_col_names:
            idx = self.col_name_maps.get(col_name)
            if idx is None:
                LOGGER.warning("Adding a col_name that is not exist in header")
                continue
            if idx not in self.last_left_col_indexes:
                continue
            if idx not in self.calculate_col_indexes:
                self.calculate_col_indexes.append(idx)
                self.calculate_col_names.append(self.header[idx])

    def add_left_col_name(self, left_col_name):
        idx = self.col_name_maps.get(left_col_name)
        if idx is None:
            LOGGER.warning("Adding a col_name that is not exist in header")
            return
        if idx not in self.left_col_indexes:
            self.left_col_indexes.append(idx)
            self.left_col_names.append(self.header[idx])
        # LOGGER.debug("After add_left_col_name, calculate_col_indexes: {}, calculate_col_names: {}".format(
        #     self.left_col_indexes, self.left_col_names
        # ))

    def add_feature_value(self, col_name, feature_value):
        self.feature_values[col_name] = feature_value

    @property
    def all_left_col_indexes(self):
        result = []
        for idx in self.last_left_col_indexes:
            if idx not in self.calculate_col_indexes:
                result.append(idx)
            elif idx in self.left_col_indexes:
                result.append(idx)
        return result

    @property
    def all_left_col_names(self):
        return [self.header[x] for x in self.all_left_col_indexes]

    @property
    def left_col_dicts(self):
        return {x: True for x in self.all_left_col_names}

    @property
    def last_left_col_names(self):
        return [self.header[x] for x in self.last_left_col_indexes]


class CompletedCalculationResults(object):
    def __init__(self):
        self.header = []
        self.col_name_maps = {}
        self.__calculate_col_names = None
        self.filter_results = []
        self.__promoter_pass_filter_nums = {}
        self.__provider_pass_filter_nums_list = []
        self.all_left_col_indexes = []

    def set_header(self, header):
        self.header = header
        for idx, col_name in enumerate(self.header):
            self.col_name_maps[col_name] = idx

    def set_calculate_col_names(self, select_col_names):
        if self.__calculate_col_names is None:
            self.__calculate_col_names = select_col_names

    def get_calculate_col_names(self):
        return self.__calculate_col_names

    def set_all_left_col_indexes(self, left_indexes):
        self.all_left_col_indexes = left_indexes.copy()

    @property
    def all_left_col_names(self):
        return [self.header[x] for x in self.all_left_col_indexes]

    def add_filter_results(self, filter_name, calculate_properties: CalculationProperties,
                           provider_calculate_properties=None):
        # self.all_left_col_indexes = calculate_properties.all_left_col_indexes.copy()
        self.set_all_left_col_indexes(calculate_properties.all_left_col_indexes)
        if filter_name == 'conclusion':
            return

        if provider_calculate_properties is None:
            provider_calculate_properties = []

        provider_feature_values = []
        provider_left_cols = []
        for idx, provider_result in enumerate(provider_calculate_properties):
            LOGGER.debug("In add_filter_results, idx: {}, provider_all_left_col_names: {}, "
                         "__provider_pass_filter_nums_list: {}".format(idx, provider_result.all_left_col_names,
                                                                       self.__provider_pass_filter_nums_list))
            if idx >= len(self.__provider_pass_filter_nums_list):
                _provider_pass_filter_nums = {}
                self.__provider_pass_filter_nums_list.append(_provider_pass_filter_nums)
            else:
                _provider_pass_filter_nums = self.__provider_pass_filter_nums_list[idx]
            for col_name in provider_result.last_left_col_names:
                _provider_pass_filter_nums.setdefault(col_name, 0)
                if col_name in provider_result.all_left_col_names:
                    _provider_pass_filter_nums[col_name] += 1

            feature_value_pb = feature_calculation_param_pb2.FeatureValue(feature_values=provider_result.feature_values)
            provider_feature_values.append(feature_value_pb)
            left_col_pb = feature_calculation_param_pb2.LeftCols(original_cols=provider_result.last_left_col_names,
                                                                 left_cols=provider_result.left_col_dicts)
            provider_left_cols.append(left_col_pb)

        # for col_name in calculate_properties.all_left_col_names:
        for col_name in calculate_properties.last_left_col_names:
            self.__promoter_pass_filter_nums.setdefault(col_name, 0)
            if col_name in calculate_properties.all_left_col_names:
                self.__promoter_pass_filter_nums[col_name] += 1

        left_cols_pb = feature_calculation_param_pb2.LeftCols(original_cols=calculate_properties.last_left_col_names,
                                                              left_cols=calculate_properties.left_col_dicts)
        this_filter_result = {
            'feature_values': calculate_properties.feature_values,
            # 'provider_feature_values': provider_feature_values,
            # 'left_cols': left_cols_pb,
            # 'provider_left_cols': provider_left_cols,
            'filter_name': filter_name
        }
        this_filter_result = feature_calculation_param_pb2.FeatureCalculationFilterParam(**this_filter_result)
        self.filter_results.append(this_filter_result)

    def get_sorted_col_names(self):
        result = sorted(self.__promoter_pass_filter_nums.items(), key=operator.itemgetter(1), reverse=True)
        return [x for x, _ in result]

    def get_provider_sorted_col_names(self):
        result = []
        for pass_name_dict in self.__provider_pass_filter_nums_list:
            sorted_list = sorted(pass_name_dict.items(), key=operator.itemgetter(1), reverse=True)
            result.append([x for x, _ in sorted_list])
        return result
