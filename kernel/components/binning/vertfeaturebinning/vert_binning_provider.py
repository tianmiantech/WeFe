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


import functools

from common.python.utils import log_utils
from kernel.components.binning.core.bucket_binning import BucketBinning
from kernel.components.binning.core.custom_binning import CustomBinning
from kernel.components.binning.core.optimal_binning.optimal_binning import OptimalBinning
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class VertFeatureBinningProvider(BaseVertFeatureBinning):

    def fit(self, data_instances):
        self._abnormal_detection(data_instances)

        binning_obj_list = []
        encrypted_label_table = self.transfer_variable.encrypted_label.get(idx=0)
        send_results = []
        modes = self.model_param.modes
        for mode in modes:
            method = mode["method"]
            bin_num = mode["bin_num"]
            members = mode["members"]

            self.model_param.bin_indexes = []
            self.model_param.bin_num = 10
            self.model_param.method = "quantile"
            for member in members:
                role = member["role"]
                member_id = member["member_id"]
                bin_feature_names = member["bin_feature_names"]
                # bin_indexes = member["bin_indexes"]
                feature_split_points = member.get('feature_split_points')

                if (role == self.role) and (str(member_id) == self.member_id):
                    bin_indexes = self.get_indexes(bin_feature_names, data_instances)
                    self.model_param.bin_indexes = bin_indexes
                    self.model_param.bin_num = bin_num
                    self.model_param.method = method
                    self.model_param.feature_split_points = feature_split_points
                    break

            if self.model_param.method == consts.QUANTILE:
                self.binning_obj = QuantileBinning(self.model_param)
            elif self.model_param.method == consts.BUCKET:
                self.binning_obj = BucketBinning(self.model_param)
            elif self.model_param.method == consts.CUSTOM:
                self.binning_obj = CustomBinning(self.model_param)
                self.binning_obj.params.feature_split_points = self.model_param.feature_split_points
            elif self.model_param.method == consts.OPTIMAL:
                if self.role == consts.PROVIDER:
                    self.model_param.bin_num = self.model_param.optimal_binning_param.init_bin_nums
                    self.binning_obj = QuantileBinning(self.model_param)
                else:
                    self.binning_obj = OptimalBinning(self.model_param)
            LOGGER.debug("in _init_model, role: {}, local_member_id: {}".format(self.role, self.component_properties))
            self.binning_obj.set_role_party(self.role, self.component_properties.local_member_id)

            """
            Apply binning method for both data instances in local party as well as the other one. Afterwards, calculate
            the specific metric value for specific columns.
            """

            # self._parse_cols(data_instances)
            self._setup_bin_inner_param(data_instances, self.model_param)

            # Calculates split points of datas in self party
            split_points = self.binning_obj.fit_split_points(data_instances)

            if not self.model_param.local_only:
                if self.model_param.method == consts.OPTIMAL:
                    self.model_param.bin_num = bin_num

                send_result = self._sync_init_bucket(encrypted_label_table, data_instances, split_points)
                send_results.append(send_result)

            self.binning_obj_list.append(self.binning_obj)

        print(self.transfer_variable.encrypted_bin_sum.remote(send_results,
                                                              role=consts.PROMOTER,
                                                              idx=0))
        provider_bin_results_list = self.transfer_variable.provider_bin_results.get(idx=0)

        bucket_idx_result_list = self.transfer_variable.bucket_idx.get(idx=0)
        for id, bucket_idx_result in enumerate(bucket_idx_result_list):
            binning_obj_method = bucket_idx_result["method"]
            if binning_obj_method == consts.OPTIMAL:
                bucket_idx = bucket_idx_result["encoded_split_points_result"]
                self.optimal_binning_list_sync(bucket_idx, index=id)

        all_cols_results = {}
        for i, provider_bin_results in enumerate(provider_bin_results_list):
            result = provider_bin_results["result"]
            bin_results = result.bin_results
            provider_bin_results = bin_results.all_cols_results
            for feature in provider_bin_results.keys():
                old_all_cols_results = self.binning_obj_list[i].bin_results.all_cols_results
                feature_name = self.bin_inner_param.decode_col_name(feature)
                bin_col_results = provider_bin_results[feature]
                bin_col_results.split_points = old_all_cols_results[feature_name].split_points
                all_cols_results[feature_name] = bin_col_results
            self.binning_obj_list[i].bin_results.all_cols_results = all_cols_results

        if self.transform_type != 'woe':
            data_instances = self.transform(data_instances)
        self.set_schema(data_instances)
        self.data_output = data_instances
        return self.data_output

    def _sync_init_bucket(self, encrypted_label_table, data_instances, split_points, need_shuffle=False):

        # self._make_iv_obj(split_points)  # Save split points

        data_bin_table = self.binning_obj.get_data_bin(data_instances, split_points)
        # LOGGER.debug("data_bin_table, count: {}".format(data_bin_table.count()))

        # encrypted_label_table_id = self.transfer_variable.generate_transferid(self.transfer_variable.encrypted_label)
        LOGGER.info(self.transfer_variable)
        # encrypted_label_table = self.transfer_variable.encrypted_label.get(idx=0)

        LOGGER.info("Get encrypted_label_table from promoter")

        encrypted_bin_sum = self.__static_encrypted_bin_label(data_bin_table, encrypted_label_table,
                                                              self.bin_inner_param.bin_cols_map, split_points)
        # LOGGER.debug("encrypted_bin_sum: {}".format(encrypted_bin_sum))

        if need_shuffle:
            encrypted_bin_sum = self.binning_obj.shuffle_static_counts(encrypted_bin_sum)

        encrypted_bin_sum = self.bin_inner_param.encode_col_name_dict(encrypted_bin_sum)
        send_result = {
            "encrypted_bin_sum": encrypted_bin_sum,
            "category_names": self.bin_inner_param.encode_col_name_list(self.bin_inner_param.category_names),
            "bin_method": self.model_param.method,
            "bin_nums": self.model_param.bin_num,
            "optimal_params": {
                "metric_method": self.model_param.optimal_binning_param.metric_method,
                "bin_num": self.model_param.bin_num,
                "mixture": self.model_param.optimal_binning_param.mixture,
                "max_bin_pct": self.model_param.optimal_binning_param.max_bin_pct,
                "min_bin_pct": self.model_param.optimal_binning_param.min_bin_pct
            }
        }
        LOGGER.debug("Send bin_info.category_names: {}, bin_info.bin_method: {}".format(send_result['category_names'],
                                                                                        send_result['bin_method']))

        return send_result

    def __static_encrypted_bin_label(self, data_bin_table, encrypted_label, cols_dict, split_points):
        data_bin_with_label = data_bin_table.join(encrypted_label, lambda x, y: (x, y))
        f = functools.partial(self.binning_obj.add_label_in_partition,
                              split_points=split_points,
                              cols_dict=cols_dict)
        result_sum = data_bin_with_label.mapPartitions(f)
        encrypted_bin_sum = result_sum.reduce(self.binning_obj.aggregate_partition_label)
        return encrypted_bin_sum

    def optimal_binning_sync(self):
        bucket_idx = self.transfer_variable.bucket_idx.get(idx=0)
        LOGGER.debug("In optimal_binning_sync, received bucket_idx: {}".format(bucket_idx))
        original_split_points = self.binning_obj.bin_results.all_split_points
        for encoded_col_name, b_idx in bucket_idx.items():
            col_name = self.bin_inner_param.decode_col_name(encoded_col_name)
            ori_sp_list = original_split_points.get(col_name)
            optimal_result = [ori_sp_list[i] for i in b_idx]
            self.binning_obj.bin_results.put_col_split_points(col_name, optimal_result)

    def optimal_binning_list_sync(self, bucket_idx, index):
        # bucket_idx = self.transfer_variable.bucket_idx.get(idx=0)
        LOGGER.debug("In optimal_binning_list_sync, received bucket_idx: {}".format(bucket_idx))
        original_split_points = self.binning_obj_list[index].bin_results.all_split_points
        for encoded_col_name, b_idx in bucket_idx.items():
            col_name = self.bin_inner_param.decode_col_name(encoded_col_name)
            ori_sp_list = original_split_points.get(col_name)
            optimal_result = [ori_sp_list[i] for i in b_idx]
            self.binning_obj_list[index].bin_results.put_col_split_points(col_name, optimal_result)
