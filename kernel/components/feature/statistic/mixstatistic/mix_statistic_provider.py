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

from common.python.utils import log_utils
from kernel.components.feature.statistic.core.base_statistic import generate_statistic_result, calc_magnitude_result, \
    calc_local_statistic, transfer_magnitude_names
from kernel.components.feature.statistic.mixstatistic.mix_statistic_base import MixStatisticBase
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MixStatisticProvider(MixStatisticBase):
    def __init__(self):
        super(MixStatisticProvider, self).__init__()
        self.role = consts.PROVIDER

    def _init_model(self, params):
        super()._init_model(params)

    def fit(self, data_instances):
        local_statistic_result = calc_local_statistic(data_instances)
        LOGGER.debug(f'local_statistic_result={local_statistic_result}')

        if self.provider_master:
            all_data_instance_statistics = [local_statistic_result]
            for provider_inner_id in self.provider_other_inner_id:
                suffix = (provider_inner_id, self.provider_inner_id)
                other_local_statistic_result = \
                    self.transfer_variable.provider_statistic_magnitude.get(suffix=suffix,
                                                                            member_id_list=[self.member_id])[0]
                all_data_instance_statistics.append(other_local_statistic_result)

            merge_magnitude_result = {}
            for magnitude_name in transfer_magnitude_names:
                merge_magnitudes = []
                for local_statistic_result in all_data_instance_statistics:
                    merge_magnitudes.append(
                        self.transform_result(local_statistic_result.get(magnitude_name), self.col_names))
                merge_result = np.asarray(merge_magnitudes).sum(axis=0)
                merge_magnitude_result[magnitude_name] = merge_result.tolist()
                LOGGER.info(
                    f'magnitude_name={magnitude_name}, merge_result={merge_result}')

                max_magnitudes = []
                for local_statistic_result in all_data_instance_statistics:
                    max_magnitudes.append(
                        self.transform_result(local_statistic_result.get('max'), self.col_names))
                max_result = np.asarray(max_magnitudes).max(axis=0)
                merge_magnitude_result['max'] = max_result.tolist()
                LOGGER.info(f'max_result, merge_result={max_result}')

                min_magnitudes = []
                for local_statistic_result in all_data_instance_statistics:
                    min_magnitudes.append(
                        self.transform_result(local_statistic_result.get('min'), self.col_names))
                min_result = np.asarray(min_magnitudes).min(axis=0)
                merge_magnitude_result['min'] = min_result.tolist()
                LOGGER.info(f'max_result, merge_result={min_result}')
            for provider_inner_id in self.provider_other_inner_id:
                suffix = (self.provider_inner_id, provider_inner_id)
                self.transfer_variable.provider_merge_statistic_magnitude.remote(merge_magnitude_result, suffix=suffix,
                                                                                 member_id_list=[self.member_id])
        else:
            self.transfer_variable.provider_statistic_magnitude.remote(local_statistic_result,
                                                                       suffix=(self.provider_inner_id,
                                                                               self.provider_master_inner_id),
                                                                       member_id_list=[self.member_id])
            merge_magnitude_result = self.transfer_variable.provider_merge_statistic_magnitude.get(
                suffix=(self.provider_master_inner_id, self.provider_inner_id), member_id_list=[self.member_id])[0]

        merge_magnitude_result = calc_magnitude_result(merge_magnitude_result)
        LOGGER.info(f"merge_result={merge_magnitude_result}")

        feature_statistic = {}

        for name in self.col_names:
            idx = self.col_names.index(name)
            feature_statistic[name] = generate_statistic_result(merge_magnitude_result, idx)

        feature_statistics = {
            'members': [{"member_id": self.member_id, "role": self.role, "feature_statistic": feature_statistic}]}
        local_statistics = {"member_id": self.member_id, "role": self.role, "feature_statistic": feature_statistic}
        LOGGER.debug(f'local_statistics={local_statistics}')
        self.transfer_variable.provider_statistic_result.remote(local_statistics, role=consts.PROMOTER,
                                                                member_id_list=[self.mix_promoter_member_id])
        LOGGER.info(f'feature_statistics={feature_statistics}')
        import json
        LOGGER.debug(json.dumps(feature_statistics))
        self.tracker.save_task_result(feature_statistics, self.task_result_type)

    def transform_result(self, result, col_names):
        result_list = []
        for col_name in col_names:
            value = 0
            if col_name in result.keys():
                value = result.get(col_name)
            result_list.append(value)
        return result_list
