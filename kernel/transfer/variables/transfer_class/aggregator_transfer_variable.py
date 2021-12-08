#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class AggregatorTransVar(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.ModelBroadcasterTransVar.server_model = self._create_variable(name='ModelBroadcasterTransVar.server_model')
        self.ModelScatterTransVar.client_model = self._create_variable(name='ModelScatterTransVar.client_model')
