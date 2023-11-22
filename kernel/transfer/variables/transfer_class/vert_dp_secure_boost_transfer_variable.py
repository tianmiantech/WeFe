#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class VertDPSecureBoostingTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.begin_iter = self._create_variable(name='begin_iter')
        self.bin_split_points = self._create_variable(name='bin_split_points')
        self.data_bin_with_dp = self._create_variable(name='data_bin_with_dp')
        self.predict_start_round = self._create_variable(name='predict_start_round')
        self.predict_stop_flag = self._create_variable(name='predict_stop_flag')
        self.promoter_predict_data = self._create_variable(name='promoter_predict_data')
        self.provider_predict_data = self._create_variable(name='provider_predict_data')
        self.stop_flag = self._create_variable(name='stop_flag')
        self.tree_dim = self._create_variable(name='tree_dim')
