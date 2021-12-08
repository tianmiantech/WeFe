#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class PaillierCipherTransVar(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.model_re_encrypted = self._create_variable(name='model_re_encrypted')
        self.model_to_re_encrypt = self._create_variable(name='model_to_re_encrypt')
        self.pailler_pubkey = self._create_variable(name='pailler_pubkey')
        self.re_encrypt_times = self._create_variable(name='re_encrypt_times')
        self.use_encrypt = self._create_variable(name='use_encrypt')
