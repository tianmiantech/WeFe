#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class DHTransVar(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.p_power_r = self._create_variable(name='p_power_r')
        self.p_power_r_bc = self._create_variable(name='p_power_r_bc')
        self.pubkey = self._create_variable(name='pubkey')
