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
class VertNNTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.batch_data_index = self._create_variable(name='batch_data_index')
        self.batch_info = self._create_variable(name='batch_info')
        self.decrypted_promoter_fowrad = self._create_variable(name='decrypted_promoter_fowrad')
        self.decrypted_promoter_weight_gradient = self._create_variable(name='decrypted_promoter_weight_gradient')
        self.drop_out_table = self._create_variable(name='drop_out_table')
        self.encrypted_acc_noise = self._create_variable(name='encrypted_acc_noise')
        self.encrypted_promoter_forward = self._create_variable(name='encrypted_promoter_forward')
        self.encrypted_promoter_weight_gradient = self._create_variable(name='encrypted_promoter_weight_gradient')
        self.encrypted_provider_forward = self._create_variable(name='encrypted_provider_forward')
        self.interactive_layer_output_unit = self._create_variable(name='interactive_layer_output_unit')
        self.is_converge = self._create_variable(name='is_converge')
        self.provider_backward = self._create_variable(name='provider_backward')
        self.selective_info = self._create_variable(name='selective_info')
