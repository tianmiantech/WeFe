#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class RandomPaddingCipherTransVar(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.DHTransVar.p_power_r = self._create_variable(name='DHTransVar.p_power_r')
        self.DHTransVar.p_power_r_bc = self._create_variable(name='DHTransVar.p_power_r_bc')
        self.DHTransVar.pubkey = self._create_variable(name='DHTransVar.pubkey')
        self.UUIDTransVar.uuid = self._create_variable(name='UUIDTransVar.uuid')
