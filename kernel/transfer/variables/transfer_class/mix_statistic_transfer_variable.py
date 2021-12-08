#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class MixStatisticTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.provider_merge_statistic_magnitude = self._create_variable(name='provider_merge_statistic_magnitude')
        self.provider_statistic_magnitude = self._create_variable(name='provider_statistic_magnitude')
        self.provider_statistic_result = self._create_variable(name='provider_statistic_result')
