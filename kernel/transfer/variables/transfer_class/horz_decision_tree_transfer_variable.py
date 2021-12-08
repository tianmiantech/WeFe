#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class HorzDecisionTreeTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.best_split_points = self._create_variable(name='best_split_points')
        self.cur_layer_node_num = self._create_variable(name='cur_layer_node_num')
        self.node_sample_num = self._create_variable(name='node_sample_num')
