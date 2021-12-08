#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class VertLRTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.batch_data_index = self._create_variable(name='batch_data_index')
        self.batch_info = self._create_variable(name='batch_info')
        self.begin_iter = self._create_variable(name='begin_iter')
        self.converge_flag = self._create_variable(name='converge_flag')
        self.fore_gradient = self._create_variable(name='fore_gradient')
        self.paillier_pubkey = self._create_variable(name='paillier_pubkey')
        self.provider_en_gradient_r = self._create_variable(name='provider_en_gradient_r')
        self.provider_forward_dict = self._create_variable(name='provider_forward_dict')
        self.provider_gradient_r = self._create_variable(name='provider_gradient_r')
        self.provider_loss_regular = self._create_variable(name='provider_loss_regular')
        self.provider_prob = self._create_variable(name='provider_prob')
        self.provider_weight = self._create_variable(name='provider_weight')
