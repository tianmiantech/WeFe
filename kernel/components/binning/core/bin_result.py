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



import numpy as np

from common.python.utils import log_utils
from kernel.protobuf.generated import feature_binning_param_pb2

LOGGER = log_utils.get_logger()


class BinColResults(object):
    def __init__(self, woe_array=(), iv_array=(), event_count_array=(), non_event_count_array=(), event_rate_array=(),
                 non_event_rate_array=(), iv=None):
        self.woe_array = list(woe_array)
        self.iv_array = list(iv_array)
        self.event_count_array = list(event_count_array)
        self.non_event_count_array = list(non_event_count_array)
        self.count_array, self.count_rate_array = self.calc_count_array(self.event_count_array,
                                                                        self.non_event_count_array)
        self.event_rate_array = list(event_rate_array)
        self.non_event_rate_array = list(non_event_rate_array)
        self.split_points = None
        if iv is None:
            iv = 0
            for idx, woe in enumerate(self.woe_array):
                non_event_rate = non_event_count_array[idx]
                event_rate = event_rate_array[idx]
                iv += (non_event_rate - event_rate) * woe
        self.iv = iv
        self.params_method = None
        self.params_bin_nums = -1

    def set_split_points(self, split_points):
        self.split_points = split_points

    def set_mode(self, bin_nums, method):
        self.params_bin_nums = bin_nums
        self.params_method = method

    def set_split_points(self, split_points):
        self.split_points = split_points

    def get_split_points(self):
        return np.array(self.split_points)

    @property
    def is_woe_monotonic(self):
        """
        Check the woe is monotonic or not
        """
        woe_array = self.woe_array
        if len(woe_array) <= 1:
            return True

        is_increasing = all(x <= y for x, y in zip(woe_array, woe_array[1:]))
        is_decreasing = all(x >= y for x, y in zip(woe_array, woe_array[1:]))
        return is_increasing or is_decreasing

    @property
    def bin_nums(self):
        return len(self.woe_array)

    def result_dict(self):
        save_dict = self.__dict__
        save_dict['is_woe_monotonic'] = self.is_woe_monotonic
        save_dict['bin_nums'] = self.bin_nums
        return save_dict

    def reconstruct(self, iv_obj):
        self.woe_array = list(iv_obj.woe_array)
        self.iv_array = list(iv_obj.iv_array)
        self.event_count_array = list(iv_obj.event_count_array)
        self.non_event_count_array = list(iv_obj.non_event_count_array)
        self.count_array, self.count_rate_array = self.calc_count_array(self.non_event_count_array,
                                                                        self.event_count_array)
        self.event_rate_array = list(iv_obj.event_rate_array)
        self.non_event_rate_array = list(iv_obj.non_event_rate_array)
        self.split_points = list(iv_obj.split_points)
        self.iv = iv_obj.iv

    def reconstruct2(self, iv_obj):
        self.woe_array = list(iv_obj["woe_array"])
        self.iv_array = list(iv_obj["iv_array"])
        self.event_count_array = list(iv_obj["event_count_array"])
        self.non_event_count_array = list(iv_obj["non_event_count_array"])
        self.count_array, self.count_rate_array = self.calc_count_array(self.non_event_count_array,
                                                                        self.event_count_array)
        self.event_rate_array = list(iv_obj["event_rate_array"])
        self.non_event_rate_array = list(iv_obj["non_event_rate_array"])
        self.split_points = list(iv_obj["split_points"])
        self.iv = iv_obj["iv"]

    @staticmethod
    def calc_count_array(non_event_count_array, event_count_array):
        count_array = np.array(non_event_count_array, dtype=int) + np.array(event_count_array, dtype=int)
        rate_array = list(count_array / count_array.sum())
        return list(count_array), rate_array

    def generate_pb(self, params_bin_nums=None, params_method=None):
        result = feature_binning_param_pb2.IVParam(woe_array=self.woe_array,
                                                   iv_array=self.iv_array,
                                                   event_count_array=self.event_count_array,
                                                   non_event_count_array=self.non_event_count_array,
                                                   event_rate_array=self.event_rate_array,
                                                   non_event_rate_array=self.non_event_rate_array,
                                                   split_points=self.split_points,
                                                   iv=self.iv,
                                                   is_woe_monotonic=self.is_woe_monotonic,
                                                   bin_nums=self.bin_nums,
                                                   params_bin_nums=params_bin_nums,
                                                   params_method=params_method,
                                                   count_array=self.count_array,
                                                   count_rate_array=self.count_rate_array)
        return result


class BinResults(object):
    def __init__(self):
        self.all_cols_results = {}
        self.role = ''
        self.member_id = ''

    def set_role_party(self, role, member_id):
        self.role = role
        self.member_id = member_id

    def put_col_results(self, col_name, col_results: BinColResults, bin_num=-1, method=""):
        ori_col_results = self.all_cols_results.get(col_name)
        col_results.set_mode(bin_num, method)
        if ori_col_results is not None:
            col_results.set_split_points(ori_col_results.get_split_points())
        self.all_cols_results[col_name] = col_results

    def put_col_split_points(self, col_name, split_points):
        col_results = self.all_cols_results.get(col_name, BinColResults())
        col_results.set_split_points(split_points)
        self.all_cols_results[col_name] = col_results

    def query_split_points(self, col_name):
        col_results = self.all_cols_results.get(col_name)
        if col_results is None:
            LOGGER.warning("Querying non-exist split_points")
            return None
        return col_results.split_points

    @property
    def all_split_points(self):
        results = {}
        for col_name, col_result in self.all_cols_results.items():
            results[col_name] = col_result.get_split_points()
        return results

    def get_split_points_array(self, bin_names):
        split_points_result = []
        for bin_name in bin_names:
            if bin_name not in self.all_cols_results:
                continue
            split_points_result.append(self.all_cols_results[bin_name].get_split_points())
        return np.array(split_points_result)

    def generated_pb(self):
        col_result_dict = {}
        for col_name, col_bin_result in self.all_cols_results.items():
            col_result_dict[col_name] = col_bin_result.generate_pb(params_bin_nums=int(col_bin_result.params_bin_nums),
                                                                   params_method=col_bin_result.params_method)
        LOGGER.debug("In generated_pb, role: {}, member_id: {}".format(self.role, self.member_id))
        result_pb = feature_binning_param_pb2.FeatureBinningResult(binning_result=col_result_dict,
                                                                   role=self.role,
                                                                   member_id=str(self.member_id))
        return result_pb

    def reconstruct(self, result_pb):
        self.role = result_pb.role
        self.member_id = result_pb.member_id
        binning_result = dict(result_pb.binning_result)
        for col_name, col_bin_result in binning_result.items():
            col_bin_obj = BinColResults()
            col_bin_obj.reconstruct(col_bin_result)
            self.all_cols_results[col_name] = col_bin_obj
        return self

    def reconstruct2(self, result_pb):
        self.role = result_pb["role"]
        self.member_id = result_pb["member_id"]
        binning_result = dict(result_pb["binning_result"])
        for col_name, col_bin_result in binning_result.items():
            col_bin_obj = BinColResults()
            col_bin_obj.reconstruct2(col_bin_result)
            self.all_cols_results[col_name] = col_bin_obj
        return self
