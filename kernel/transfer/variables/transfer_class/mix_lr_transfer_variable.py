#!/usr/bin/env python
# -*- coding: utf-8 -*-


################################################################################
#
# AUTO GENERATED TRANSFER VARIABLE CLASS. DO NOT MODIFY
#
################################################################################

from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables


# noinspection PyAttributeOutsideInit
class MixLRTransferVariable(BaseTransferVariables):
    def __init__(self, flowid=0):
        super().__init__(flowid)
        self.aggregated_model = self._create_variable(name='aggregated_model')
        self.batch_data_index = self._create_variable(name='batch_data_index')
        self.batch_info = self._create_variable(name='batch_info')
        self.begin_iter = self._create_variable(name='begin_iter')
        self.converge_flag = self._create_variable(name='converge_flag')
        self.dh_ciphertext_bc = self._create_variable(name='dh_ciphertext_bc')
        self.dh_ciphertext_promoter = self._create_variable(name='dh_ciphertext_promoter')
        self.dh_ciphertext_provider = self._create_variable(name='dh_ciphertext_provider')
        self.dh_pubkey = self._create_variable(name='dh_pubkey')
        self.fore_gradient = self._create_variable(name='fore_gradient')
        self.is_converge = self._create_variable(name='is_converge')
        self.paillier_pubkey = self._create_variable(name='paillier_pubkey')
        self.predict_result = self._create_variable(name='predict_result')
        self.predict_wx = self._create_variable(name='predict_wx')
        self.promoter_loss = self._create_variable(name='promoter_loss')
        self.promoter_model = self._create_variable(name='promoter_model')
        self.promoter_party_weight = self._create_variable(name='promoter_party_weight')
        self.promoter_uuid = self._create_variable(name='promoter_uuid')
        self.provider_en_gradient_r = self._create_variable(name='provider_en_gradient_r')
        self.provider_forward_dict = self._create_variable(name='provider_forward_dict')
        self.provider_gradient_r = self._create_variable(name='provider_gradient_r')
        self.provider_loss = self._create_variable(name='provider_loss')
        self.provider_loss_regular = self._create_variable(name='provider_loss_regular')
        self.provider_model = self._create_variable(name='provider_model')
        self.provider_model_weights_r = self._create_variable(name='provider_model_weights_r')
        self.provider_optimizer_model_weights = self._create_variable(name='provider_optimizer_model_weights')
        self.provider_party_weight = self._create_variable(name='provider_party_weight')
        self.provider_prob = self._create_variable(name='provider_prob')
        self.provider_uuid = self._create_variable(name='provider_uuid')
        self.provider_weight = self._create_variable(name='provider_weight')
        self.re_encrypt_times = self._create_variable(name='re_encrypt_times')
        self.re_encrypted_model = self._create_variable(name='re_encrypted_model')
        self.to_encrypt_model = self._create_variable(name='to_encrypt_model')
        self.use_encrypt = self._create_variable(name='use_encrypt')
        self.uuid_conflict_flag = self._create_variable(name='uuid_conflict_flag')
