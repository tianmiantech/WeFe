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
from kernel.components.binning.core.iv_calculator import IvCalculator
from kernel.components.binning.core.optimal_binning.optimal_binning import OptimalBinning
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.security import EncryptModeCalculator
from kernel.security.cipher_compressor.packer import PromoterIntegerPacker
from kernel.security.encrypt import PaillierEncrypt
from kernel.security.paillier import PaillierEncryptedNumber
from kernel.utils import consts
from kernel.utils import data_util

LOGGER = log_utils.get_logger()


class VertFeatureBinningPromoter(BaseVertFeatureBinning):

    def __init__(self):
        super(VertFeatureBinningPromoter, self).__init__()
        self.labels = None
        self._packer: PromoterIntegerPacker = None
        self.iv_calculator = IvCalculator(self.model_param.adjustment_factor,
                                          role=self.role,
                                          party_id=self.component_properties.local_member_id)

    def fit(self, data_instances):
        LOGGER.info("Start feature binning fit and transform")
        self._abnormal_detection(data_instances)

        label_counts_dict = data_util.get_label_count(data_instances)
        modes = self.model_param.modes
        binning_methods = self.parse_binning_method(modes)
        if len(label_counts_dict) > 2:
            if consts.OPTIMAL in binning_methods:
                raise ValueError("Have not supported optimal binning in multi-class data yet")

        self.labels = list(label_counts_dict.keys())
        label_counts = [label_counts_dict[k] for k in self.labels]
        label_table = IvCalculator.convert_label(data_instances, self.labels)
        LOGGER.debug(f'label_tabel={label_table.first()}')

        self.caculate_promoter_iv(modes, label_table, label_counts, data_instances)

        if self.model_param.local_only:
            LOGGER.info("This is a local only binning fit")
            # self.binning_obj.cal_local_iv(data_instances, label_table=label_table)
            self.transform(data_instances)
            return self.data_output

        paillier_encryptor = PaillierEncrypt()
        paillier_encryptor.generate_key()
        cipher = EncryptModeCalculator(encrypter=paillier_encryptor)
        self._packer = PromoterIntegerPacker(pack_num=len(self.labels), pack_num_range=label_counts,
                                             encrypt_mode_calculator=cipher)

        self.federated_iv(data_instances=data_instances, label_table=label_table,
                          cipher=cipher, result_counts=label_counts_dict, label_elements=self.labels)

        f = functools.partial(self.encrypt, cipher=cipher)
        encrypted_label_table = label_table.mapValues(f, need_send=True)

        LOGGER.info("Sent encrypted_label_table to provider")
        self.transfer_variable.encrypted_label.remote(encrypted_label_table, role=consts.PROVIDER, idx=-1)
        LOGGER.info("Get encrypted_bin_sum from provider")

        encrypted_bin_infos_list = self.transfer_variable.encrypted_bin_sum.get(idx=-1)

        all_provider_result_list = self.caculate_provider_iv(data_instances, encrypted_bin_infos_list, cipher)
        LOGGER.debug(" all_provider_result_list {}".format(all_provider_result_list))
        for provider_idx, per_provider_results in enumerate(all_provider_result_list):
            LOGGER.info("send result to providers ")
            self.transfer_variable.provider_bin_results.remote(per_provider_results, role=consts.PROVIDER,
                                                               idx=provider_idx)
            LOGGER.debug("Send per_provider_results to providers{}".format(per_provider_results))
            LOGGER.info("send encoded_split_points to providers")
            self.transfer_variable.bucket_idx.remote(per_provider_results, role=consts.PROVIDER, idx=provider_idx)

        self.set_schema(data_instances)
        LOGGER.debug("Finish set_schema,data_instances{}".format(data_instances.first()))
        self.transform(data_instances)
        LOGGER.debug("Finish feature binning fit and transform,data_output,{}".format(self.data_output))
        return self.data_output

    @staticmethod
    def parse_binning_method(modes):
        methods = []
        for mode in modes:
            methods.append(mode.get('method'))
        return methods

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

    def caculate_promoter_iv(self, modes, label_table, label_counts, data_instances):
        all_bin_col_indexs = []
        all_bin_col_names = []
        for mode in modes:
            for member in mode["members"]:
                if (member["role"] == self.role) and (member["bin_feature_names"]) and \
                        (str(member["member_id"]) == self.member_id):
                    bin_feature_names = member["bin_feature_names"]
                    bin_indexes = self.get_indexes(bin_feature_names, data_instances)
                    all_bin_col_names.extend(bin_feature_names)
                    all_bin_col_indexs.extend(bin_indexes)
                    self.model_param.method = mode["method"]
                    self.model_param.bin_num = mode["bin_num"]
                    self.model_param.bin_indexes = bin_indexes

                    if self.model_param.method == consts.QUANTILE:
                        self.binning_obj = QuantileBinning(self.model_param)
                    elif self.model_param.method == consts.BUCKET:
                        self.binning_obj = BucketBinning(self.model_param)
                    elif self.model_param.method == consts.CUSTOM:
                        self.model_param.feature_split_points = member["feature_split_points"]
                        self.binning_obj = CustomBinning(self.model_param)
                        self.binning_obj.params.feature_split_points = self.model_param.feature_split_points
                    elif self.model_param.method == consts.OPTIMAL:
                        self.binning_obj = OptimalBinning(self.model_param)

                    LOGGER.debug(
                        "in _init_model, role: {}, local_member_id: {}".format(self.role, self.component_properties))
                    self.binning_obj.set_role_party(self.role, self.component_properties.local_member_id)
                    self.binning_obj.params.bin_num = self.model_param.bin_num
                    self.binning_obj.params.method = self.model_param.method
                    self._setup_bin_inner_param(data_instances, self.model_param)
                    self.binning_obj.fit_split_points(data_instances)
                    LOGGER.debug(f"data_instances={data_instances}")
                    self.binning_obj.bin_results = self.iv_calculator.cal_local_iv(data_instances=data_instances,
                                                                                   split_points=self.binning_obj.split_points,
                                                                                   labels=self.labels,
                                                                                   label_counts=label_counts,
                                                                                   bin_cols_map=self.bin_inner_param.get_need_cal_iv_cols_map(),
                                                                                   label_table=label_table)
                    # self.binning_obj.cal_local_iv(data_instances, label_table=label_table)
                    LOGGER.debug("After cal_local_iv data_instances: {}".format(data_instances))
                    # print(self.binning_obj)
                    # LOGGER.debug("encrypted_bin_sums: {}".format(encrypted_bin_sums))
                    self.binning_obj_list.append(self.binning_obj)

            # add the different results to all_cols_results
        binning_obj_list = self.binning_obj_list
        for binning_obj in binning_obj_list:
            for feature in binning_obj.bin_results.all_cols_results.keys():
                self.binning_obj.bin_results.all_cols_results[feature] = \
                    binning_obj.bin_results.all_cols_results[feature]
        self.model_param.bin_indexes = all_bin_col_indexs
        self.model_param.bin_names = all_bin_col_names

    def federated_iv(self, data_instances, label_table, cipher, result_counts, label_elements):
        converted_label_table = label_table.mapValues(lambda x: [int(i) for i in x])
        encrypted_label_table = self._packer.pack_and_encrypt(converted_label_table)
        self.transfer_variable.encrypted_label.remote(encrypted_label_table,
                                                      role=consts.PROVIDER,
                                                      idx=-1)

        encrypted_bin_infos_list = self.transfer_variable.encrypted_bin_sum.get(idx=-1)

        return self.caculate_provider_iv(data_instances, encrypted_bin_infos_list, cipher)

    def caculate_provider_iv(self, data_instances, encrypted_bin_infos_list, cipher):
        all_provider_result_list = []
        for provider_idx, encrypted_bin_infos in enumerate(encrypted_bin_infos_list):
            per_provider_results_list = []
            for idx, encrypted_bin_info in enumerate(encrypted_bin_infos):
                per_provider_result = {}
                encoded_split_points = {}
                provider_member_id = self.component_properties.provider_member_idlist[provider_idx]
                encrypted_bin_sum = encrypted_bin_info['encrypted_bin_sum']
                category_names = encrypted_bin_info['category_names']
                provider_model_params = encrypted_bin_info['model_param']

                result_counts_table = self._packer.decrypt_cipher_package_and_unpack(encrypted_bin_sum)
                #  TODO
                result_counts = result_counts_table
                # result_counts = self.__decrypt_bin_sum(encrypted_bin_sum, cipher)
                LOGGER.debug("result_counts: {}".format(result_counts_table))
                LOGGER.debug(
                    "Received provider {} result, length of buckets: {}".format(provider_idx, len(result_counts_table)))
                # LOGGER.debug("category_name: {}, provider_bin_methods: {}".format(category_names, provider_bin_methods))

                if provider_model_params.method == consts.OPTIMAL:

                    self.binning_obj.event_total, self.binning_obj.non_event_total = self.get_histogram(data_instances)

                    optimal_binning_cols = {x: y for x, y in result_counts.items() if x not in category_names}
                    provider_binning_obj, encoded_split_points = self.optimal_binning_sync(optimal_binning_cols,
                                                                                           data_instances.count(),
                                                                                           data_instances._partitions,
                                                                                           provider_idx,
                                                                                           provider_model_params)

                    category_bins = {x: y for x, y in result_counts.items() if x in category_names}
                    provider_binning_obj.cal_iv_woe(category_bins, provider_model_params.adjustment_factor)
                elif provider_model_params.method == consts.QUANTILE:
                    provider_binning_obj = QuantileBinning(provider_model_params)
                    provider_binning_obj.cal_iv_woe(result_counts, provider_model_params.adjustment_factor)
                elif provider_model_params.method == consts.BUCKET:
                    provider_binning_obj = BucketBinning(provider_model_params)
                    provider_binning_obj.cal_iv_woe(result_counts, provider_model_params.adjustment_factor)
                elif provider_model_params.method == consts.CUSTOM:
                    provider_binning_obj = CustomBinning(provider_model_params)
                    provider_binning_obj.cal_iv_woe(result_counts, provider_model_params.adjustment_factor)
                provider_binning_obj.set_role_party(role=consts.PROVIDER, member_id=provider_member_id)
                self.provider_results.append(provider_binning_obj)
                per_provider_result["result"] = provider_binning_obj
                per_provider_result["method"] = provider_model_params.method
                per_provider_result["bin_num"] = provider_model_params.bin_num
                encoded_split_points_result = encoded_split_points
                per_provider_result["encoded_split_points_result"] = encoded_split_points_result
                per_provider_result_copy = copy.deepcopy(per_provider_result)
                per_provider_results_list.append(per_provider_result_copy)
            all_provider_result_list.append(per_provider_results_list)
        return all_provider_result_list
