#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class HorzSecureBoostingTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.feature_number = self._create_variable(name='feature_number')
        self.label_mapping = self._create_variable(name='label_mapping')
        self.local_labels = self._create_variable(name='local_labels')
        self.loss_status = self._create_variable(name='loss_status')
        self.stop_flag = self._create_variable(name='stop_flag')
        self.tree_dim = self._create_variable(name='tree_dim')
        self.valid_features = self._create_variable(name='valid_features')
