#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class SecureAddExampleTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.promoter_share = self._create_variable(name='promoter_share')
        self.provider_share = self._create_variable(name='provider_share')
        self.provider_sum = self._create_variable(name='provider_sum')
