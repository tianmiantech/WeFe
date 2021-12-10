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




################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class MixBinningTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.all_min_max = self._create_variable(name='all_min_max')
        self.bin_sum = self._create_variable(name='bin_sum')
        self.bucket_idx = self._create_variable(name='bucket_idx')
        self.data_count = self._create_variable(name='data_count')
        self.encrypted_bin_sum = self._create_variable(name='encrypted_bin_sum')
        self.encrypted_label = self._create_variable(name='encrypted_label')
        self.global_ranks = self._create_variable(name='global_ranks')
        self.global_static_values = self._create_variable(name='global_static_values')
        self.is_converge = self._create_variable(name='is_converge')
        self.local_ranks = self._create_variable(name='local_ranks')
        self.local_static_values = self._create_variable(name='local_static_values')
        self.merge_bin_sum = self._create_variable(name='merge_bin_sum')
        self.min_max = self._create_variable(name='min_max')
        self.missing_count = self._create_variable(name='missing_count')
        self.paillier_pubkey = self._create_variable(name='paillier_pubkey')
        self.provider_bin_bucket_info = self._create_variable(name='provider_bin_bucket_info')
        self.provider_bin_results = self._create_variable(name='provider_bin_results')
        self.provider_binning_results = self._create_variable(name='provider_binning_results')
        self.query_array = self._create_variable(name='query_array')
        self.total_count = self._create_variable(name='total_count')
        self.total_missing_count = self._create_variable(name='total_missing_count')
