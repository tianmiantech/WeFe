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

import math
from kernel.model_base import ModelBase
from common.python.utils import log_utils

LOGGER = log_utils.get_logger()

class VertFeaturePSIBase(ModelBase):
    def __init__(self):
        super().__init__()

    def cal_psi(self, train_feature_bin_rates, eval_feature_bin_rates):
        bin_psi_list = []
        sub_test_base = []
        ln_test_base = []
        for idx, train_bin_rate in enumerate(train_feature_bin_rates):
            eval_bin_rate = eval_feature_bin_rates[idx]
            if train_bin_rate > 0 and eval_bin_rate == 0:
                sub_value = -train_bin_rate
                log_value = float('-inf')
            elif train_bin_rate == 0 and eval_bin_rate > 0:
                sub_value = eval_bin_rate
                log_value = float('inf')
            elif train_bin_rate == 0 and eval_bin_rate == 0:
                sub_value = 0
                log_value = 0
            else:
                sub_value = eval_bin_rate - train_bin_rate
                log_value = math.log(eval_bin_rate / train_bin_rate, )
            bin_psi = sub_value * log_value

            bin_psi_list.append(bin_psi)
            sub_test_base.append(sub_value)
            ln_test_base.append(log_value)
        feature_psi = sum(bin_psi_list)

        bin_psi_list = self.check(bin_psi_list)
        sub_test_base = self.check(sub_test_base)
        ln_test_base = self.check(ln_test_base)
        feature_psi = self.check(feature_psi)
        result = {
            'bin_psi': bin_psi_list,
            'bin_sub_test_base_value': sub_test_base,
            'bin_ln_test_base_value': ln_test_base
        }
        return result, feature_psi

    @staticmethod
    def check(data_type):
        if isinstance(data_type, list):
            for index, i in enumerate(data_type):
                if i == float('-inf'):
                    data_type[index] = '-Infinity'
                elif i==float('inf'):
                    data_type[index] = 'Infinity'
        if isinstance(data_type, float):
            if data_type == float('-inf'):
                data_type = '-Infinity'
            elif data_type == float('inf'):
                data_type = 'Infinity'
        return data_type

    @staticmethod
    def check_bin_result(train_bin_result, eval_bin_result):
        train_feature_bin_rates = train_bin_result.get('count_rate')
        eval_feature_bin_rates = eval_bin_result.get('count_rate')
        LOGGER.debug('train_feature_bin_rates and eval_feature_bin_rates is '.format(train_feature_bin_rates,
                                                                                     eval_feature_bin_rates))
        if len(train_feature_bin_rates) != len(eval_feature_bin_rates):
            LOGGER.info('actual_train_bin_num : {} , actual_test_bin_num : {}'.
                        format(len(train_feature_bin_rates), len(eval_feature_bin_rates)))
            for i in range(len(train_feature_bin_rates)-len(eval_feature_bin_rates)):
                eval_bin_result.get('count_rate').append(0)
                eval_bin_result.get('count').append(0)
        return train_bin_result, eval_bin_result



