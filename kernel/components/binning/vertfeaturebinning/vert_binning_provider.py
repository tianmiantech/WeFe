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
import operator

import numpy as np

from common.python.utils import log_utils
from kernel.components.binning.core.bucket_binning import BucketBinning
from kernel.components.binning.core.custom_binning import CustomBinning
from kernel.components.binning.core.iv_calculator import IvCalculator
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.security.cipher_compressor.compressor import PackingCipherTensor, CipherCompressorProvider
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class VertFeatureBinningProvider(BaseVertFeatureBinning):

    def __init__(self):
        super(VertFeatureBinningProvider, self).__init__()
        self.compressor = None
        self.iv_calculator = IvCalculator(self.model_param.adjustment_factor,
                                          role=self.role,
                                          party_id=self.component_properties.local_member_id)

    def fit(self, data_instances):
        self._abnormal_detection(data_instances)

        # get the encrypted_label from promoter
        # encrypted_label_table = self.transfer_variable.encrypted_label.get(idx=0)

        # caculate encrypted bin sum and send the variable binning results to promoter
        modes = self.model_param.modes
        send_results = self.cal_encrypted_bin_sum(modes, data_instances)
        self.transfer_variable.encrypted_bin_sum.remote(send_results, role=consts.PROMOTER, idx=0)

        # get the provider bin results list
        provider_bin_results_list = self.transfer_variable.provider_bin_results.get(idx=0)
        LOGGER.debug(" get provider_bin_results_list from promoter {}".format(provider_bin_results_list))
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
            LOGGER.debug("provider_bin_results.keys() {}".format(provider_bin_results.keys()))
            for feature in provider_bin_results.keys():
                old_all_cols_results = self.binning_obj_list[i].bin_results.all_cols_results
                LOGGER.debug("old_all_cols_results{}".format(old_all_cols_results))
                feature_name = self.bin_inner_param.decode_col_name(feature)
                bin_col_results = provider_bin_results[feature]
                bin_col_results.split_points = old_all_cols_results[feature_name].split_points
                all_cols_results[feature_name] = bin_col_results
            self.binning_obj_list[i].bin_results.all_cols_results = all_cols_results

        LOGGER.debug("binning_obj_list :{}".format(self.binning_obj_list))
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
        encrypted_bin_sum = self.compressor.compress_dtable(encrypted_bin_sum)
        # LOGGER.debug("encrypted_bin_sum: {}".format(encrypted_bin_sum))

        LOGGER.debug(f"encrypted_bin_sum={encrypted_bin_sum.first()}")
        LOGGER.debug(f"bin_inner_param={self.bin_inner_param.col_name_maps}")
        # encode_name_f = functools.partial(self.bin_inner_param.encode_col_name_dict,
        #                                   col_name_dict=self.bin_inner_param.col_name_maps)
        # encrypted_bin_sum = self.bin_inner_param.encode_col_name_dict(encrypted_bin_sum, self)
        # encrypted_bin_sum = encrypted_bin_sum.map(encode_name_f)

        # self.transfer_variable.encrypted_bin_sum.remote(encrypted_bin_sum,
        #                                                 role=consts.PROMOTER,
        #                                                 idx=0)

        model_param = copy.deepcopy(self.model_param)
        model_param.category_names = []
        model_param.category_indexs = []
        model_param.bin_indexes = []
        send_result = {
            "encrypted_bin_sum": list(encrypted_bin_sum.collect()),
            "category_names": self.bin_inner_param.encode_col_name_list(self.bin_inner_param.category_names),
            "model_param": model_param
            # "bin_method": self.model_param.method,
            # "bin_nums": self.model_param.bin_num,
            # "optimal_params": {
            #     "metric_method": self.model_param.optimal_binning_param.metric_method,
            #     "bin_num": self.model_param.bin_num,
            #     "mixture": self.model_param.optimal_binning_param.mixture,
            #     "max_bin_pct": self.model_param.optimal_binning_param.max_bin_pct,
            #     "min_bin_pct": self.model_param.optimal_binning_param.min_bin_pct
            # }
        }
        LOGGER.debug(f"send_reuslt:{send_result}")
        LOGGER.debug("Send bin_info.category_names: {}, bin_info.bin_method: {}".format(send_result['category_names'],
                                                                                        send_result['model_param']))

        return send_result

    def __static_encrypted_bin_label(self, data_bin_table, encrypted_label, cols_dict, split_points):
        label_counts = encrypted_label.reduce(operator.add)
        sparse_bin_points = self.binning_obj.get_sparse_bin(self.bin_inner_param.bin_indexes,
                                                            self.binning_obj.bin_results.all_split_points)
        sparse_bin_points = {self.bin_inner_param.header[k]: v for k, v in sparse_bin_points.items()}

        encrypted_bin_sum = self.cal_bin_label(
            data_bin_table=data_bin_table,
            sparse_bin_points=sparse_bin_points,
            label_table=encrypted_label,
            label_counts=label_counts
        )
        return encrypted_bin_sum

    def cal_bin_label(self, data_bin_table, sparse_bin_points, label_table, label_counts):
        """

        data_bin_table : Table.
            Each element represent for the corresponding bin number this feature belongs to.
            e.g. it could be:
            [{'x1': 1, 'x2': 5, 'x3': 2}
            ...
             ]

        sparse_bin_points: dict
            Dict of sparse bin num
                {"x0": 2, "x1": 3, "x2": 5 ... }

        label_table : Table
            id with labels

        Returns:
            Table with value:
            [[label_0_sum, label_1_sum, ...], [label_0_sum, label_1_sum, ...] ... ]
        """
        data_bin_with_label = data_bin_table.join(label_table, lambda x, y: (x, y))
        f = functools.partial(self.add_label_in_partition,
                              sparse_bin_points=sparse_bin_points)

        result_counts = data_bin_with_label.mapReducePartitions(f, self.aggregate_partition_label)

        return result_counts

    @staticmethod
    def add_label_in_partition(data_bin_with_table, sparse_bin_points):
        """
        Add all label, so that become convenient to calculate woe and iv

        Parameters
        ----------
        data_bin_with_table : Table
            The input data, the Table is like:
            (id, {'x1': 1, 'x2': 5, 'x3': 2}, y)
            where y = [is_label_0, is_label_1, ...]  which is one-hot format array of label

        sparse_bin_points: dict
            Dict of sparse bin num
                {0: 2, 1: 3, 2:5 ... }

        Returns
        -------
            ['x1', [[label_0_sum, label_1_sum, ...], [label_0_sum, label_1_sum, ...] ... ],
             'x2', [[label_0_sum, label_1_sum, ...], [label_0_sum, label_1_sum, ...] ... ],
             ...
            ]

        """
        result_sum = {}
        for _, datas in data_bin_with_table:
            bin_idx_dict = datas[0]
            y = datas[1]
            for col_name, bin_idx in bin_idx_dict.items():
                result_sum.setdefault(col_name, [])
                col_sum = result_sum[col_name]
                while bin_idx >= len(col_sum):
                    if isinstance(y, PackingCipherTensor):
                        zero_y = np.zeros(y.dim)
                        col_sum.append(PackingCipherTensor(zero_y.tolist()))
                    else:
                        col_sum.append(np.zeros(len(y)))

                # if bin_idx == sparse_bin_points[col_name]:
                #     continue
                col_sum[bin_idx] = col_sum[bin_idx] + y
        return list(result_sum.items())

    @staticmethod
    def aggregate_partition_label(sum1, sum2):
        """
        Used in reduce function. Aggregate the result calculate from each partition.

        Parameters
        ----------
        sum1 :  list.
            It is like:
            [[label_0_sum, label_1_sum, ...], [label_0_sum, label_1_sum, ...] ... ]
        sum2 : list
            Same as sum1
        Returns
        -------
        Merged sum. The format is same as sum1.

        """
        if sum1 is None and sum2 is None:
            return None

        if sum1 is None:
            return sum2

        if sum2 is None:
            return sum1

        for idx, label_sum2 in enumerate(sum2):
            if idx >= len(sum1):
                sum1.append(label_sum2)
            else:
                sum1[idx] = sum1[idx] + label_sum2
        return sum1

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

    def cal_encrypted_bin_sum(self, modes, data_instances):

        encrypted_label_table = None

        send_results = []
        binning_obj_list = []
        all_bin_col_indexs = []
        all_bin_col_names = []
        for mode in modes:
            for member in mode["members"]:
                if (member["role"] == self.role) and (member["bin_feature_names"]) \
                        and (member["member_id"] == self.member_id):
                    bin_feature_names = member["bin_feature_names"]
                    bin_indexes = self.get_indexes(bin_feature_names, data_instances)
                    self.model_param.method = mode["method"]
                    self.model_param.bin_num = mode["bin_num"]
                    self.model_param.bin_indexes = bin_indexes
                    all_bin_col_names.extend(bin_feature_names)
                    all_bin_col_indexs.extend(bin_indexes)

                    if self.model_param.method == consts.QUANTILE:
                        self.binning_obj = QuantileBinning(self.model_param)
                    elif self.model_param.method == consts.BUCKET:
                        self.binning_obj = BucketBinning(self.model_param)
                    elif self.model_param.method == consts.CUSTOM:
                        self.model_param.feature_split_points = member["feature_split_points"]
                        self.binning_obj = CustomBinning(self.model_param)
                        self.binning_obj.params.feature_split_points = self.model_param.feature_split_points
                    elif self.model_param.method == consts.OPTIMAL:
                        self.model_param.bin_num = self.model_param.optimal_binning_param.init_bin_nums
                        self.binning_obj = QuantileBinning(self.model_param)
                    LOGGER.debug("in _init_model, role: {}, local_member_id: {}".format(self.role,
                                                                                        self.component_properties))
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
                            self.model_param.bin_num = mode["bin_num"]

                        if encrypted_label_table is None:
                            # get the encrypted_label from promoter
                            encrypted_label_table = self.transfer_variable.encrypted_label.get(idx=0)
                            LOGGER.info("Get encrypted_label_table from promoter")

                        if self.compressor is None:
                            self.compressor = CipherCompressorProvider()

                        send_result = self._sync_init_bucket(encrypted_label_table, data_instances, split_points)
                        send_results.append(send_result)

                    self.binning_obj_list.append(self.binning_obj)
        self.model_param.bin_indexes = all_bin_col_indexs
        self.model_param.bin_names = all_bin_col_names
        return send_results
