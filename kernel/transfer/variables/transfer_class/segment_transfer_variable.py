#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class SegmentTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.promoter_key = self._create_variable(name='promoter_key')
        self.provider_key = self._create_variable(name='provider_key')
        self.segment_ids = self._create_variable(name='segment_ids')
