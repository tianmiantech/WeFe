#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class VertFeatureCalculationTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.provider_calculate_cols = self._create_variable(name='provider_calculate_cols')
        self.provider_calculate_results = self._create_variable(name='provider_calculate_results')
        self.result_left_cols = self._create_variable(name='result_left_cols')
