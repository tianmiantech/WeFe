#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class StepwiseTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.promoter_data_info = self._create_variable(name='promoter_data_info')
        self.provider_data_info = self._create_variable(name='provider_data_info')
        self.step_best = self._create_variable(name='step_best')
