#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class MultiVertPearsonTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.promoter_features = self._create_variable(name='promoter_features')
        self.provider_corr_infos = self._create_variable(name='provider_corr_infos')
        self.provider_corr_provider = self._create_variable(name='provider_corr_provider')
