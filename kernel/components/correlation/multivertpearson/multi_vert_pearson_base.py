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
from kernel.components.correlation.core import base_pearson
from kernel.components.correlation.vertpearson.param import PearsonParam
from kernel.model_base import ModelBase
from kernel.transfer.variables.transfer_class.multi_vert_pearson_transfer_variable import \
    MultiVertPearsonTransferVariable

LOGGER = log_utils.get_logger()


class MultiVertPearsonBase(ModelBase):

    def __init__(self):
        super().__init__()
        self.model_param = PearsonParam()
        self.role = None
        self.corr = None
        self.local_corr = None

        self.metric_name = "multi_pearson"
        self.metric_namespace = "statistics"

        self.shapes = []
        self.names = []
        self.parties = []
        self.local_party = None
        self.promoter_party = None
        self.provider_parties = []
        self._set_parties()

        self._summary = {}
        self.transfer_variable = MultiVertPearsonTransferVariable()

    def _set_parties(self):
        self.promoter_party, self.provider_parties = base_pearson.set_parties(self)

    def _init_model(self, param):
        super()._init_model(param)
        self.model_param = param

    def _select_columns(self, data_instance):
        return base_pearson.select_columns(self, data_instance)

    @staticmethod
    def _standardized(data):
        n = data.count()
        sum_x, sum_square_x = data.mapValues(lambda x: (x, x ** 2)) \
            .reduce(lambda pair1, pair2: (pair1[0] + pair2[0], pair1[1] + pair2[1]))
        mu = sum_x / n
        sigma_square = list(sum_square_x / n - mu ** 2)
        sigma = np.sqrt(sigma_square)
        if (sigma <= 0).any():
            raise ValueError(f"zero standard deviation detected, sigma={sigma}")
        return n, data.mapValues(lambda x: (x - mu) / sigma)

    @staticmethod
    def _calc_two_party_corr_spdz(normed, n, local_party, other_party, all_parties, left=False, name='pearson'):
        return base_pearson.calculate_corr_spdz(normed, n, local_party, other_party, all_parties, left, name)

    # noinspection PyTypeChecker
    def _callback(self):
        metric_data = [("corr", self._summary)]
        LOGGER.debug(f'metric_data: {metric_data}, metric_name:{self.metric_name}')
        self.tracker.saveMetricData(self.metric_name, self.metric_namespace, None, metric_data)
