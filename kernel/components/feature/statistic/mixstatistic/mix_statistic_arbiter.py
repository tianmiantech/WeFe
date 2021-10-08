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


import numpy as np

from common.python.utils import log_utils
from kernel.components.feature.statistic.core.base_statistic import transfer_magnitude_names
from kernel.components.feature.statistic.mixstatistic.mix_statistic_base import MixStatisticBase
from kernel.transfer.framework.horz.blocks import secure_sum_aggregator
from kernel.transfer.framework.horz.blocks.secure_sum_aggregator import SecureSumAggregatorTransVar
from kernel.transfer.framework.weights import ListWeights
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MixStatisticArbiter(MixStatisticBase):
    def __init__(self):
        super(MixStatisticArbiter, self).__init__()
        self.role = consts.ARBITER
        self.aggregator = secure_sum_aggregator.Server(enable_secure_aggregate=True,
                                                       trans_var=SecureSumAggregatorTransVar(
                                                           clients=(consts.PROMOTER,)))

    def _init_model(self, params):
        super()._init_model(params)

    def fit(self, data_instances=None):
        for magnitude_name in transfer_magnitude_names:
            suffix = (magnitude_name,)
            merged_model = self.aggregator.sum_model(suffix=suffix)
            self.aggregator.send_aggregated_model(merged_model, suffix=suffix)
            LOGGER.info(f'magnitude_name={magnitude_name}, merged_model={np.array(merged_model.unboxed)}')

        label_suffix = ('label',)
        merged_label = self.aggregator.sum_model(suffix=label_suffix)
        self.aggregator.send_aggregated_model(merged_label, suffix=label_suffix)
        LOGGER.info(f'label, merged_model={np.array(merged_label.unboxed)}')

        max_suffix = ('max',)
        merged_max = self.aggregator.max_model(suffix=max_suffix)
        self.aggregator.send_aggregated_model(ListWeights(merged_max), suffix=max_suffix)
        LOGGER.info(f'max, merged_model={np.array(merged_max)}')

        min_suffix = ('min',)
        merged_min = self.aggregator.min_model(suffix=min_suffix)
        self.aggregator.send_aggregated_model(ListWeights(merged_min), suffix=min_suffix)
        LOGGER.info(f'min, merged_model={np.array(merged_min)}')
