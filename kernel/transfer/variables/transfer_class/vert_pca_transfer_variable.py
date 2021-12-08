#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class VertPCATransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.pca_result = self._create_variable(name='pca_result')
        self.promoter_features = self._create_variable(name='promoter_features')
        self.provider_cov_infos = self._create_variable(name='provider_cov_infos')
