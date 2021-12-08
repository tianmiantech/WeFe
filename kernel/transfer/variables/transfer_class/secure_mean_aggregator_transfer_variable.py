#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class SecureMeanAggregatorTransVar(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.AggregatorTransVar.ModelBroadcasterTransVar.server_model = self._create_variable(name='AggregatorTransVar.ModelBroadcasterTransVar.server_model')
        self.AggregatorTransVar.ModelScatterTransVar.client_model = self._create_variable(name='AggregatorTransVar.ModelScatterTransVar.client_model')
        self.RandomPaddingCipherTransVar.DHTransVar.p_power_r = self._create_variable(name='RandomPaddingCipherTransVar.DHTransVar.p_power_r')
        self.RandomPaddingCipherTransVar.DHTransVar.p_power_r_bc = self._create_variable(name='RandomPaddingCipherTransVar.DHTransVar.p_power_r_bc')
        self.RandomPaddingCipherTransVar.DHTransVar.pubkey = self._create_variable(name='RandomPaddingCipherTransVar.DHTransVar.pubkey')
        self.RandomPaddingCipherTransVar.UUIDTransVar.uuid = self._create_variable(name='RandomPaddingCipherTransVar.UUIDTransVar.uuid')
