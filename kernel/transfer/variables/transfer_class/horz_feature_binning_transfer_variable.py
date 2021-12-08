#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class HorzBinningTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.global_static_values = self._create_variable(name='global_static_values')
        self.is_converge = self._create_variable(name='is_converge')
        self.local_static_values = self._create_variable(name='local_static_values')
        self.query_array = self._create_variable(name='query_array')
