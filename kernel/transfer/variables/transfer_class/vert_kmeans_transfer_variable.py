#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class VertKmeansTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.cluster_result = self._create_variable(name='cluster_result')
        self.converged_result = self._create_variable(name='converged_result')
        self.init_center = self._create_variable(name='init_center')
        self.provider_center_dist = self._create_variable(name='provider_center_dist')
        self.provider_dist = self._create_variable(name='provider_dist')
        self.provider_tol = self._create_variable(name='provider_tol')
