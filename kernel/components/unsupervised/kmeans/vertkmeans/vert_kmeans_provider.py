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
from kernel.components.unsupervised.kmeans.vertkmeans.vert_kmeans_base import VertKmeansBase
from kernel.utils import abnormal_detection, consts
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()


class VertKmeansProvider(VertKmeansBase):

    def __init__(self):
        super(VertKmeansProvider, self).__init__()

    def fit(self, data_instances):
        LOGGER.info('start kmeans')
        self._init_params()
        self.header = get_header(data_instances)
        abnormal_detection.empty_table_detection(data_instances)
        if self.k > data_instances.count() or self.k < 2:
            raise ValueError('K is too larger or too small for current data')

        np.random.seed(self.random_stat)
        first_center_id_key = self.transfer_variable.init_center.get(idx=0)
        self.center_list = self.first_center(first_center_id_key, data_instances)

        while self.n_iter_ < self.max_iter:
            LOGGER.info(f'{self.n_iter_}, {self.center_list}')
            provider_dist_all_table = self.compute_dist_all_table(data_instances, self.center_list, need_send=True)
            self.transfer_variable.provider_dist.remote(provider_dist_all_table, role=consts.PROMOTER,
                                                        suffix=(self.n_iter_,),
                                                        idx=0)

            cluster_result = self.transfer_variable.cluster_result.get(suffix=(self.n_iter_,), idx=0)
            center_new, self.cluster_count = self.center_cal(cluster_result, data_instances)

            provider_tol = np.sum(np.sum((np.array(self.center_list) - np.array(center_new)) ** 2, axis=1))
            self.transfer_variable.provider_tol.remote(provider_tol, role=consts.PROMOTER, suffix=(self.n_iter_,),
                                                       idx=0)
            self.is_converged = self.transfer_variable.converged_result.get(suffix=(self.n_iter_,), idx=0)

            self.center_list = center_new
            self.cluster_result = cluster_result

            provider_center_dist = self.center_dist(self.center_list)
            self.transfer_variable.provider_center_dist.remote(provider_center_dist, role=consts.PROMOTER,
                                                               suffix=(self.n_iter_,), idx=0)
            if self.is_converged:
                break

            self.n_iter_ += 1
