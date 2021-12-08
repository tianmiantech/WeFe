#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class VertFeatureBinningTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.bucket_idx = self._create_variable(name='bucket_idx')
        self.encrypted_bin_sum = self._create_variable(name='encrypted_bin_sum')
        self.encrypted_label = self._create_variable(name='encrypted_label')
        self.paillier_pubkey = self._create_variable(name='paillier_pubkey')
        self.provider_bin_results = self._create_variable(name='provider_bin_results')
        self.provider_binning_results = self._create_variable(name='provider_binning_results')
        self.ready_to_get_data = self._create_variable(name='ready_to_get_data')
