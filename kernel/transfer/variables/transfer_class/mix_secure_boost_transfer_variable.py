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



################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class MixSecureBoostingTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.begin_iter = self._create_variable(name='begin_iter')
        self.feature_number = self._create_variable(name='feature_number')
        self.label_mapping = self._create_variable(name='label_mapping')
        self.local_labels = self._create_variable(name='local_labels')
        self.loss_status = self._create_variable(name='loss_status')
        self.optim_split_points = self._create_variable(name='optim_split_points')
        self.predict_start_round = self._create_variable(name='predict_start_round')
        self.predict_stop_flag = self._create_variable(name='predict_stop_flag')
        self.promoter_predict_data = self._create_variable(name='promoter_predict_data')
        self.provider_feature_number = self._create_variable(name='provider_feature_number')
        self.provider_predict_data = self._create_variable(name='provider_predict_data')
        self.provider_split_points = self._create_variable(name='provider_split_points')
        self.provider_stop_flag = self._create_variable(name='provider_stop_flag')
        self.stop_flag = self._create_variable(name='stop_flag')
        self.tree_dim = self._create_variable(name='tree_dim')
        self.valid_features = self._create_variable(name='valid_features')
