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



import numpy as np

from common.python.utils import log_utils
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class SqnSyncBase(object):
    def __init__(self):
        self.batch_data_index_transfer = None
        self.provider_forwards_transfer = None
        self.forward_hess = None
        self.forward_hess_transfer = None


class Promoter(SqnSyncBase):
    def __init__(self):
        super().__init__()
        self.promoter_hess_vector = None

    def register_transfer_variable(self, transfer_variable):
        self.batch_data_index_transfer = transfer_variable.sqn_sample_index
        self.promoter_hess_vector = transfer_variable.promoter_hess_vector
        self.provider_forwards_transfer = transfer_variable.provider_sqn_forwards
        self.forward_hess_transfer = transfer_variable.forward_hess

    def sync_sample_data(self, data_instances, sample_size, random_seed, suffix=tuple()):
        n = data_instances.count()
        if sample_size >= n:
            sample_rate = 1.0
        else:
            sample_rate = sample_size / n
        sampled_data = data_instances.sample(sample_rate, random_seed)

        batch_index = sampled_data.mapValues(lambda x: None)
        self.batch_data_index_transfer.remote(obj=batch_index,
                                              role=consts.PROVIDER,
                                              suffix=suffix)
        return sampled_data

    def get_provider_forwards(self, suffix=tuple()):
        provider_forwards = self.provider_forwards_transfer.get(idx=-1,
                                                                suffix=suffix)
        return provider_forwards

    def remote_forward_hess(self, forward_hess, suffix=tuple()):
        self.forward_hess_transfer.remote(obj=forward_hess,
                                          role=consts.PROVIDER,
                                          suffix=suffix)

    def sync_hess_vector(self, hess_vector, suffix):
        self.promoter_hess_vector.remote(obj=hess_vector,
                                         role=consts.ARBITER,
                                         suffix=suffix)


class Provider(SqnSyncBase):
    def __init__(self):
        super().__init__()
        self.provider_hess_vector = None

    def register_transfer_variable(self, transfer_variable):
        self.batch_data_index_transfer = transfer_variable.sqn_sample_index
        self.provider_forwards_transfer = transfer_variable.provider_sqn_forwards
        self.provider_hess_vector = transfer_variable.provider_hess_vector
        self.forward_hess_transfer = transfer_variable.forward_hess

    def sync_sample_data(self, data_instances, suffix=tuple()):
        batch_index = self.batch_data_index_transfer.get(idx=0,
                                                         suffix=suffix)
        sample_data = data_instances.join(batch_index, lambda x, y: x)
        return sample_data

    def remote_provider_forwards(self, provider_forwards, suffix=tuple()):
        self.provider_forwards_transfer.remote(obj=provider_forwards,
                                               role=consts.PROMOTER,
                                               suffix=suffix)

    def get_forward_hess(self, suffix=tuple()):
        forward_hess = self.forward_hess_transfer.get(idx=0,
                                                      suffix=suffix)
        return forward_hess

    def sync_hess_vector(self, hess_vector, suffix):
        self.provider_hess_vector.remote(obj=hess_vector,
                                         role=consts.ARBITER,
                                         suffix=suffix)


class Arbiter(object):
    def __init__(self):
        super().__init__()
        self.promoter_hess_vector = None
        self.provider_hess_vector = None

    def register_transfer_variable(self, transfer_variable):
        self.promoter_hess_vector = transfer_variable.promoter_hess_vector
        self.provider_hess_vector = transfer_variable.provider_hess_vector

    def sync_hess_vector(self, suffix):
        promoter_hess_vector = self.promoter_hess_vector.get(idx=0,
                                                             suffix=suffix)
        provider_hess_vectors = self.provider_hess_vector.get(idx=-1,
                                                              suffix=suffix)
        provider_hess_vectors = [x.reshape(-1) for x in provider_hess_vectors]
        hess_vectors = np.hstack((h for h in provider_hess_vectors))
        hess_vectors = np.hstack((hess_vectors, promoter_hess_vector))
        return hess_vectors
