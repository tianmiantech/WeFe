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

import numpy as np

from common.python.utils import log_utils
from kernel.components.feature.statistic.core import base_statistic
from kernel.components.feature.statistic.core.base_statistic import calc_magnitude_result, \
    generate_statistic_result, calc_label_statistic, transfer_magnitude_names
from kernel.components.feature.statistic.horzstatistic.horz_statistic_base import HorzStatisticBase
from kernel.transfer.framework.horz.blocks import secure_sum_aggregator
from kernel.transfer.framework.weights import ListWeights

LOGGER = log_utils.get_logger()


class HorzStatisticClient(HorzStatisticBase):
    def __init__(self):
        super(HorzStatisticClient, self).__init__()
        self.aggregator = secure_sum_aggregator.Client(enable_secure_aggregate=True)

    def _init_model(self, params):
        super()._init_model(params)

    def fit(self, data_instances):
        local_statistic_result = base_statistic.calc_local_statistic(data_instances)

        merge_magnitude_result = {}
        for magnitude_name in transfer_magnitude_names:
            suffix = (magnitude_name,)
            result = self.transform_result(local_statistic_result.get(magnitude_name), self.col_names)
            self.aggregator.send_model(result, suffix=suffix)
            merge_result = self.aggregator.get_aggregated_model(suffix=suffix)
            merge_magnitude_result[magnitude_name] = np.array(merge_result.unboxed)
            LOGGER.info(
                f'magnitude_name={magnitude_name}, result={np.array(result.unboxed)}, merge_result={np.array(merge_result.unboxed)}')

        labels_statistics = calc_label_statistic(data_instances)
        label_count = [0, 0]
        for key, value in labels_statistics.items():
            label_count[key] = value
        label_suffix = ('label',)
        self.aggregator.send_model(ListWeights(label_count), suffix=label_suffix)
        merge_label_result = self.aggregator.get_aggregated_model(suffix=label_suffix)
        merge_magnitude_result['label'] = merge_label_result.unboxed
        LOGGER.info(
            f'labels, labels_statistics={labels_statistics}, result={label_count}, merge_result={np.array(merge_label_result.unboxed)}')

        max_suffix = ('max',)
        max_value = self.transform_result(local_statistic_result.get('max'), self.col_names)
        self.aggregator._aggregator.send_model(model=max_value, suffix=max_suffix)
        max_result = self.aggregator.get_aggregated_model(suffix=max_suffix)
        merge_magnitude_result['max'] = max_result.unboxed
        LOGGER.info(f'max_result, result={np.array(max_value.unboxed)}, merge_result={np.array(max_result.unboxed)}')

        min_suffix = ('min',)
        min_value = self.transform_result(local_statistic_result.get('min'), self.col_names)
        self.aggregator._aggregator.send_model(model=min_value, suffix=min_suffix)
        min_result = self.aggregator.get_aggregated_model(suffix=min_suffix)
        merge_magnitude_result['min'] = min_result.unboxed
        LOGGER.info(f'min_result, result={np.array(min_value.unboxed)}, merge_result={np.array(min_result.unboxed)}')

        merge_magnitude_result = calc_magnitude_result(merge_magnitude_result)
        label_infos = merge_label_result.unboxed

        feature_statistic = {}
        for name in self.col_names:
            idx = self.col_names.index(name)
            feature_statistic[name] = generate_statistic_result(merge_magnitude_result, idx)

        label_statistic = {'label': {
            0: label_infos[0],
            1: label_infos[1]
        }}

        feature_statistics = {
            'members': [{"member_id": self.member_id, "role": self.role, "feature_statistic": feature_statistic,
                         'label_statistic': label_statistic}]}
        self.tracker.save_task_result(feature_statistics, self.task_result_type)

    def transform_result(self, result, col_names):
        result_list = []
        for col_name in col_names:
            value = 0
            if col_name in result.keys():
                value = result.get(col_name)
            result_list.append(value)
        return ListWeights(result_list)
