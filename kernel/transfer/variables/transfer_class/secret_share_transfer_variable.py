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


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class SecretShareTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.encrypted_share_matrix = self._create_variable(name='encrypted_share_matrix')
        self.multiply_triplets_cross = self._create_variable(name='multiply_triplets_cross')
        self.multiply_triplets_encrypted = self._create_variable(name='multiply_triplets_encrypted')
        self.reconstruct = self._create_variable(name='reconstruct')
        self.share = self._create_variable(name='share')
        self.q_field = self._create_variable(name='q_field')
