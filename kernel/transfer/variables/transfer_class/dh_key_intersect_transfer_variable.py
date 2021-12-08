#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class DhKeyIntersectTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.dh_pubkey = self._create_variable(name='dh_pubkey')
        self.intersect_ids = self._create_variable(name='intersect_ids')
        self.intersect_promoter_ids_process = self._create_variable(name='intersect_promoter_ids_process')
        self.intersect_provider_ids_process = self._create_variable(name='intersect_provider_ids_process')
        self.promoter_key = self._create_variable(name='promoter_key')
