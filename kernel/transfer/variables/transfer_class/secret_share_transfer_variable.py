#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class SecretShareTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.encrypted_share_matrix = self._create_variable(name='encrypted_share_matrix')
        self.multiply_triplets_cross = self._create_variable(name='multiply_triplets_cross')
        self.multiply_triplets_encrypted = self._create_variable(name='multiply_triplets_encrypted')
        self.rescontruct = self._create_variable(name='rescontruct')
        self.share = self._create_variable(name='share')
