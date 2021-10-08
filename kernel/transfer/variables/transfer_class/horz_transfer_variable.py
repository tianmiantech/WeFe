#!/usr/bin/env python
# -*- coding: utf-8 -*-

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



################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class HorzTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.aggregated_model = self._create_variable(name='aggregated_model')
        self.dh_ciphertext_bc = self._create_variable(name='dh_ciphertext_bc')
        self.dh_ciphertext_promoter = self._create_variable(name='dh_ciphertext_promoter')
        self.dh_ciphertext_provider = self._create_variable(name='dh_ciphertext_provider')
        self.dh_pubkey = self._create_variable(name='dh_pubkey')
        self.is_converge = self._create_variable(name='is_converge')
        self.model_re_encrypted = self._create_variable(name='model_re_encrypted')
        self.model_to_re_encrypt = self._create_variable(name='model_to_re_encrypt')
        self.paillier_pubkey = self._create_variable(name='paillier_pubkey')
        self.promoter_loss = self._create_variable(name='promoter_loss')
        self.promoter_model = self._create_variable(name='promoter_model')
        self.promoter_party_weight = self._create_variable(name='promoter_party_weight')
        self.promoter_uuid = self._create_variable(name='promoter_uuid')
        self.provider_loss = self._create_variable(name='provider_loss')
        self.provider_model = self._create_variable(name='provider_model')
        self.provider_party_weight = self._create_variable(name='provider_party_weight')
        self.provider_uuid = self._create_variable(name='provider_uuid')
        self.re_encrypt_times = self._create_variable(name='re_encrypt_times')
        self.re_encrypted_model = self._create_variable(name='re_encrypted_model')
        self.to_encrypt_model = self._create_variable(name='to_encrypt_model')
        self.use_encrypt = self._create_variable(name='use_encrypt')
        self.uuid_conflict_flag = self._create_variable(name='uuid_conflict_flag')
