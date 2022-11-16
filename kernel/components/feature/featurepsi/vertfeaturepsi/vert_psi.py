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

import numpy as np
import functools
import math
import sys

from common.python.utils import log_utils
from kernel.model_base import ModelBase
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.core.bucket_binning import BucketBinning
from kernel.components.binning.core.base_binning import Binning
from kernel.components.feature.featurepsi.vertfeaturepsi.param import VertFeaturePSIParam
from kernel.components.binning.core.iv_calculator import IvCalculator
from kernel.utils import consts
from kernel.utils import data_util
from common.python.common.exception.custom_exception import ParameterError,DataSetEmptyError
from kernel.transfer.variables.transfer_class.feature_psi_transfer_variable import VertFeaturePSICalculateTransferVariable
from kernel.components.feature.featurepsi.vertfeaturepsi.base_psi import VertFeaturePSIBase

LOGGER = log_utils.get_logger()

class VertFeaturePSICalculate(ModelBase):
    def __init__(self):
        super(VertFeaturePSICalculate, self).__init__()
        self.bin_psi_results = None
        self.task_result_type = 'feature_psi'
        self.model_param = VertFeaturePSIParam()
        self.need_transfer = True
        self.feature_psi_result = None
        self.all_parties_psi_result = {}
        self.max_value = - sys.maxsize - 1
        self.base_psi = VertFeaturePSIBase()
        self.iv_calculator = IvCalculator(self.model_param.adjustment_factor,role=self.role,
                                          party_id=self.component_properties.local_member_id)

    def _init_model(self, param: VertFeaturePSIParam):
        self.model_param = param
        self.bin_names = param.bin_names
        self.bin_nums = param.bin_num
        self.method = param.method
        self.transfer_variable = VertFeaturePSICalculateTransferVariable()

        # if param.method == consts.CUSTOM:
        #     self.feature_split_points = param.split_points

    def fit(self, data_instances, validate_data=None):
        LOGGER.info("Start Feature PSI fit")
        if validate_data is None:
            raise DataSetEmptyError("validate data set is empty, please check data set")
        self._abnormal_detection(data_instances)

        LOGGER.info("bin_names: {}, bin_nums:{}, bin_method: {}".format(self.bin_names,
                                                                        self.bin_nums, self.method))
        if self.method == consts.QUANTILE:
            self.binning_obj = QuantileBinning(self.model_param)
        elif self.method == consts.BUCKET:
            self.binning_obj = BucketBinning(self.model_param)
        # elif self.bin_method == consts.CUSTOM:
        #     self.model_param.feature_split_points = self.feature_split_points
        #     self.binning_obj = CustomBinning(self.model_param)
        #     self.binning_obj.params.feature_split_points = self.model_param.feature_split_points
        self.model_param.bin_indexes = self.get_indexes(self.bin_names, data_instances)
        self.binning_obj._setup_bin_inner_param(data_instances, self.model_param)
        self.binning_obj.fit_split_points(data_instances)
        bin_cols_map = self.binning_obj.bin_inner_param.get_need_cal_iv_cols_map()
        split_points = self.binning_obj.split_points
        LOGGER.info("bin_cols_map: {} and split_points: {}".format(bin_cols_map, split_points))
        train_bin_results = self.get_bin_result(data_instances, split_points, bin_cols_map)
        eval_bin_results = self.get_bin_result(validate_data, split_points, bin_cols_map)
        LOGGER.info('train_bin_results ====> {}\neval_bin_results ====>{}'.
                    format(train_bin_results, eval_bin_results))

        bin_psi_results = self.get_all_feature_psi_results(train_bin_results, eval_bin_results, split_points)

        if self.role == 'provider' and self.need_transfer:
            party_results = {
                'member_role': self.role,
                'member_id': self.member_id,
                'feature_psi_results': bin_psi_results
            }
            results = {'feature_psi_results': bin_psi_results}
            self.transfer_variable.feature_psi_result.remote(party_results, role=consts.PROMOTER,idx= -1)
            self.save_results(results, self.task_result_type)

        elif self.role == 'promoter' and self.need_transfer:
            provider_feature_psi_results = self.transfer_variable.feature_psi_result.get(idx=-1)
            all_parties_results = {}
            all_parties_results['promoter_psi'] = {
                'feature_psi_results':bin_psi_results
            }
            all_parties_results['provider_psi'] = provider_feature_psi_results
            self.save_results(all_parties_results, self.task_result_type)

    def get_all_feature_psi_results(self, train_bin_results, eval_bin_results, split_points):
        if len(train_bin_results) != len(eval_bin_results):
            raise ValueError("The number of features in the training set {} and test set {} is not equal".format
                (len(train_bin_results), len(eval_bin_results)))
        train_test_result = dict()
        for col_name, train_feature_bin_result in train_bin_results.items():
            eval_feature_bin_result = eval_bin_results.get(col_name)
            train_feature_bin_result, eval_feature_bin_result = \
                self.base_psi.check_bin_result(train_feature_bin_result, eval_feature_bin_result)
            train_feature_bin_rates = train_feature_bin_result.get('count_rate')
            eval_feature_bin_rates = eval_bin_results.get(col_name).get('count_rate')
            bin_psi_list, feature_psi = self.base_psi.cal_psi(train_feature_bin_rates, eval_feature_bin_rates)
            train_test_result[col_name] = {
                'train_feature_static':train_feature_bin_result,
                'test_feature_static': eval_feature_bin_result,
                'bin_cal_results' : bin_psi_list,
                'feature_psi' : feature_psi,
                'split_point': list(split_points.get(col_name))
            }
        return train_test_result

    def _abnormal_detection(self, data_instances):
        data_util.empty_table_detection(data_instances)
        data_util.empty_feature_detection(data_instances)

    @staticmethod
    def get_indexes(bin_feature_names, data_instances):
        bin_indexes = []
        if len(bin_feature_names) == 0:
            raise ParameterError("Please select a feature for calculating psi")
        data_feature_names = data_instances.schema["header"]
        for bin_feature_name in bin_feature_names:
            index = data_feature_names.index(bin_feature_name)
            bin_indexes.append(index)
        return bin_indexes

    def cal_bin_label(self, data_bin_table):
        f = functools.partial(self.add_label_in_partition)
        result_counts = data_bin_table.mapReducePartitions(f, self.iv_calculator.aggregate_partition_label)
        result_counts = dict(result_counts.collect())
        return result_counts

    def get_bin_result(self, data_instances, split_points, bin_cols_map):
        data_bin_table = Binning.get_data_bin_v2(data_instances, split_points, bin_cols_map)
        result_counts = self.cal_bin_label(data_bin_table)
        all_bin_results = dict()
        for col_name, bin_result in result_counts.items():
            bin_result = [list(i)[0] for i in bin_result]
            bin_results_dict = dict()
            col_total_sum = sum(bin_result)
            col_bin_results_dict = bin_results_dict[col_name] = dict()
            col_bin_results_dict['count'] = bin_result
            col_bin_results_dict['count_rate'] = [i/col_total_sum for i in bin_result]
            col_bin_results_dict['total_count'] = col_total_sum
            all_bin_results.update(bin_results_dict)
        return all_bin_results

    @staticmethod
    def add_label_in_partition(data_bin_with_table):
        result_sum = {}
        for _, datas in data_bin_with_table:
            bin_idx_dict = datas
            for col_name, bin_idx in bin_idx_dict.items():
                result_sum.setdefault(col_name, [])
                col_sum = result_sum[col_name]
                while bin_idx >= len(col_sum):
                    col_sum.append(np.zeros(1))
                col_sum[bin_idx] = col_sum[bin_idx] + 1
        return list(result_sum.items())

    def save_results(self, bin_psi_results, task_result_type):
        results = dict()
        results['role'] = self.role
        results['member_id'] = self.member_id
        results['meta_param'] = {
            'bin_method': self.method,
            'bin_nums': self.bin_nums,
        }
        results['psi_results'] = bin_psi_results
        self.tracker.save_task_result(results, task_result_type)