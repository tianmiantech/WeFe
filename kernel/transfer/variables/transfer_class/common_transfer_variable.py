#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class CommonTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.arbiter2promoter_complete_status = self._create_variable(name='arbiter2promoter_complete_status')
        self.promoter2arbiter_complete_status = self._create_variable(name='promoter2arbiter_complete_status')
        self.promoter2promoter_complete_status = self._create_variable(name='promoter2promoter_complete_status')
        self.promoter2provider_complete_status = self._create_variable(name='promoter2provider_complete_status')
        self.provider2promoter_complete_status = self._create_variable(name='provider2promoter_complete_status')
        self.provider2provider_complete_status = self._create_variable(name='provider2provider_complete_status')
