#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class SSHEModelTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.host_prob = self._create_variable(name='host_prob')
        self.is_converged = self._create_variable(name='is_converged')
        self.loss = self._create_variable(name='loss')
        self.q_field = self._create_variable(name='q_field')
