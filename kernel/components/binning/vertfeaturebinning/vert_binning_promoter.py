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


import copy
import functools

from common.python.utils import log_utils
from kernel.base import statics
from kernel.components.binning.core.bucket_binning import BucketBinning
from kernel.components.binning.core.custom_binning import CustomBinning
from kernel.components.binning.core.optimal_binning.optimal_binning import OptimalBinning
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.security.encrypt import PaillierEncrypt
from kernel.security.paillier import PaillierEncryptedNumber
from kernel.utils import consts
from kernel.utils import data_util

LOGGER = log_utils.get_logger()


class VertFeatureBinningPromoter(BaseVertFeatureBinning):

    def fit(self, data_instances):
        LOGGER.info("Start feature binning fit and transform")
        self._abnormal_detection(data_instances)

        label_counts = data_util.count_labels(data_instances)
        if label_counts < 2:
            raise ValueError("Iv calculation support binary-data only in this version.")

        schema = data_instances.schema
        data_instances = data_instances.mapValues(self.load_data)
        data_instances.schema = schema

        label_counts = data_util.count_labels(data_instances)
        if label_counts < 2:
            raise ValueError("after process Iv calculation support binary-data only in this version.")

        per_mode_provider_result_list = []
        label_table = data_instances.mapValues(lambda x: x.label)
        cipher = PaillierEncrypt()
        cipher.generate_key()

        f = functools.partial(self.encrypt, cipher=cipher)
        encrypted_label_table = label_table.mapValues(f, need_send=True)

        print(self.transfer_variable.encrypted_label.remote(encrypted_label_table,
                                                            role=consts.PROVIDER,
                                                            idx=-1))
        LOGGER.info("Sent encrypted_label_table to provider")

        encrypted_bin_infos_list = self.transfer_variable.encrypted_bin_sum.get(idx=-1)
        LOGGER.info("get encrypted_bin_infos to provider")

        modes = self.model_param.modes
        for id, mode in enumerate(modes):
            method = mode["method"]
            bin_num = mode["bin_num"]
            members = mode["members"]
            self.model_param.bin_indexes = []
            self.model_param.bin_num = 10
            self.model_param.method = "quantile"

            for member in members:
                role = member["role"]
                member_id = member["member_id"]
                # bin_indexes = member["bin_indexes"]
                bin_feature_names = member["bin_feature_names"]
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
            self.binning_obj.params.bin_num = self.model_param.bin_num
            self.binning_obj.params.method = self.model_param.method
            """
            Apply binning method for both data instances in local party as well as the other one. Afterwards, calculate
            the specific metric value for specific columns. Currently, iv is support for binary labeled data only.
            """

            self._setup_bin_inner_param(data_instances, self.model_param)
            self.set_schema(data_instances)
            # self._parse_cols(data_instances)
            self.binning_obj.fit_split_points(data_instances)

            if self.model_param.local_only:
                LOGGER.info("This is a local only binning fit")
                self.binning_obj.cal_local_iv(data_instances, label_table=label_table)
                self.transform(data_instances)
                return self.data_output

            self.binning_obj.cal_local_iv(data_instances, label_table=label_table)
            LOGGER.debug("After cal_local_iv data_instances: {}".format(data_instances))
            # print(self.binning_obj)
            # LOGGER.debug("encrypted_bin_sums: {}".format(encrypted_bin_sums))

            LOGGER.info("Get encrypted_bin_sum from provider")
            per_mode_provider_result = {}
            per_mode_provider_result["method"] = method
            per_mode_provider_result["bin_num"] = bin_num
            result = {}
            encoded_split_points_result = {}
            for provider_idx, encrypted_bin_infos in enumerate(encrypted_bin_infos_list):
                encrypted_bin_info = encrypted_bin_infos[id]
                provider_member_id = self.component_properties.provider_member_idlist[provider_idx]
                encrypted_bin_sum = encrypted_bin_info['encrypted_bin_sum']
                provider_bin_methods = encrypted_bin_info['bin_method']
                category_names = encrypted_bin_info['category_names']
                result_counts = self.__decrypt_bin_sum(encrypted_bin_sum, cipher)
                LOGGER.debug("result_counts: {}".format(result_counts))
                LOGGER.debug(
                    "Received provider {} result, length of buckets: {}".format(provider_idx, len(result_counts)))
                LOGGER.debug("category_name: {}, provider_bin_methods: {}".format(category_names, provider_bin_methods))

                # if self.model_param.method == consts.OPTIMAL:
                if provider_bin_methods == consts.OPTIMAL:
                    optimal_binning_params = encrypted_bin_info['optimal_params']

                    provider_model_params = copy.deepcopy(self.model_param)
                    provider_model_params.bin_num = optimal_binning_params.get('bin_num')
                    provider_model_params.optimal_binning_param.metric_method = optimal_binning_params.get(
                        'metric_method')
                    provider_model_params.optimal_binning_param.mixture = optimal_binning_params.get('mixture')
                    provider_model_params.optimal_binning_param.max_bin_pct = optimal_binning_params.get('max_bin_pct')
                    provider_model_params.optimal_binning_param.min_bin_pct = optimal_binning_params.get('min_bin_pct')

                    self.binning_obj.event_total, self.binning_obj.non_event_total = self.get_histogram(data_instances)
                    optimal_binning_cols = {x: y for x, y in result_counts.items() if x not in category_names}
                    provider_binning_obj, encoded_split_points = self.optimal_binning_sync(optimal_binning_cols,
                                                                                           data_instances.count(),
                                                                                           data_instances._partitions,
                                                                                           provider_idx,
                                                                                           provider_model_params)

                    category_bins = {x: y for x, y in result_counts.items() if x in category_names}
                    provider_binning_obj.cal_iv_woe(category_bins, self.model_param.adjustment_factor)
                elif provider_bin_methods == consts.QUANTILE:
                    provider_model_params = copy.deepcopy(self.model_param)
                    provider_model_params.bin_num = encrypted_bin_info["bin_nums"]
                    provider_model_params.method = encrypted_bin_info["bin_method"]
                    provider_binning_obj = QuantileBinning(provider_model_params)
                    provider_binning_obj.cal_iv_woe(result_counts, self.model_param.adjustment_factor)
                    encoded_split_points = {}
                elif provider_bin_methods == consts.BUCKET:
                    provider_model_params = copy.deepcopy(self.model_param)
                    provider_model_params.bin_num = encrypted_bin_info["bin_nums"]
                    provider_model_params.method = encrypted_bin_info["bin_method"]
                    provider_binning_obj = BucketBinning(provider_model_params)
                    provider_binning_obj.cal_iv_woe(result_counts, self.model_param.adjustment_factor)
                    encoded_split_points = {}
                provider_binning_obj.set_role_party(role=consts.PROVIDER, member_id=provider_member_id)
                self.provider_results.append(provider_binning_obj)
                result[provider_idx] = provider_binning_obj
                encoded_split_points_result[provider_idx] = encoded_split_points
                per_mode_provider_result["result"] = result
                per_mode_provider_result["encoded_split_points_result"] = encoded_split_points_result
            self.binning_obj_list.append(self.binning_obj)
            per_mode_provider_result_list.append(per_mode_provider_result)

        for provider_idx, encrypted_bin_infos in enumerate(encrypted_bin_infos_list):
            per_result_list = []
            for per in per_mode_provider_result_list:
                per_result_dic = {}
                per_method = per["method"]
                per_bin_num = per["bin_num"]
                per_result = per["result"][provider_idx]
                per_encoded_split_points_result = per["encoded_split_points_result"][provider_idx]

                per_result_dic["method"] = per_method
                per_result_dic["bin_num"] = per_bin_num
                per_result_dic["result"] = per_result
                per_result_dic["encoded_split_points_result"] = per_encoded_split_points_result
                per_result_list.append(per_result_dic)

            LOGGER.info("send result to providers ")
            self.transfer_variable.provider_bin_results.remote(per_result_list,
                                                               role=consts.PROVIDER,
                                                               idx=provider_idx)

            LOGGER.info("send encoded_split_points to providers")
            self.transfer_variable.bucket_idx.remote(per_result_list,
                                                     role=consts.PROVIDER,
                                                     idx=provider_idx)

        binning_obj_list = self.binning_obj_list
        for binning_obj in binning_obj_list:
            for feature in binning_obj.bin_results.all_cols_results.keys():
                self.binning_obj.bin_results.all_cols_results[feature] = binning_obj.bin_results.all_cols_results[
                    feature]

        self.set_schema(data_instances)
        LOGGER.debug("Finish set_schema,data_instances{}".format(data_instances.first()))
        self.transform(data_instances)
        LOGGER.debug("Finish feature binning fit and transform,data_output,{}".format(self.data_output))
        return self.data_output

    @staticmethod
    def encrypt(x, cipher):
        return cipher.encrypt(x), cipher.encrypt(1 - x)

    @staticmethod
    def __decrypt_bin_sum(encrypted_bin_sum, cipher):
        # for feature_sum in encrypted_bin_sum:
        decrypted_list = {}
        for col_name, count_list in encrypted_bin_sum.items():
            new_list = []
            for event_count, non_event_count in count_list:
                if isinstance(event_count, PaillierEncryptedNumber):
                    event_count = cipher.decrypt(event_count)
                if isinstance(non_event_count, PaillierEncryptedNumber):
                    non_event_count = cipher.decrypt(non_event_count)
                new_list.append((event_count, non_event_count))
            decrypted_list[col_name] = new_list
        return decrypted_list

    @staticmethod
    def load_data(data_instance):
        # Here suppose this is a binary question and the event label is 1
        if data_instance.label != 1:
            data_instance.label = 0
        return data_instance

    def optimal_binning_sync(self, result_counts, sample_count, partitions, provider_idx, provider_model_params):
        provider_binning_obj = OptimalBinning(params=provider_model_params,
                                              abnormal_list=self.binning_obj.abnormal_list)
        provider_binning_obj.event_total = self.binning_obj.event_total
        provider_binning_obj.non_event_total = self.binning_obj.non_event_total
        LOGGER.debug("Start provider party optimal binning train")
        bucket_table = provider_binning_obj.bin_sum_to_bucket_list(result_counts, partitions)
        provider_binning_obj.fit_buckets(bucket_table, sample_count)
        encoded_split_points = provider_binning_obj.bin_results.all_split_points

        return provider_binning_obj, encoded_split_points

    @staticmethod
    def get_histogram(data_instances):
        static_obj = statics.MultivariateStatisticalSummary(data_instances, cols_index=-1)
        label_historgram = static_obj.get_label_histogram()
        event_total = label_historgram.get(1, 0)
        non_event_total = label_historgram.get(0, 0)
        if event_total == 0 or non_event_total == 0:
            LOGGER.warning(f"event_total or non_event_total might have errors, event_total: {event_total},"
                           f" non_event_total: {non_event_total}")
        return event_total, non_event_total
