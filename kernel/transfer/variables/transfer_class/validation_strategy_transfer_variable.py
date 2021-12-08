#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class ValidationStrategyVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.validation_status = self._create_variable(name='validation_status')
