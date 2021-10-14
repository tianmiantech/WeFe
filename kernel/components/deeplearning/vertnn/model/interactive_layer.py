#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright 2021 Tianmian Tech. All Rights Reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Copyright 2019 The FATE Authors. All Rights Reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



import pickle

import numpy as np

from common.python.utils import log_utils
from kernel.components.deeplearning.vertnn.backend.tf_keras.interactive.dense_model import ProviderDenseModel
from kernel.components.deeplearning.vertnn.backend.tf_keras.interactive.dense_model import PromoterDenseModel
from kernel.components.deeplearning.vertnn.backend.tf_keras.interactive.drop_out import DropOut
from kernel.protobuf.generated.vert_nn_model_param_pb2 import InteractiveLayerParam
from kernel.security import PaillierEncrypt
from kernel.security.encrypt_mode import EncryptModeCalculator
from kernel.utils import consts
from kernel.utils import random_number_generator
from kernel.utils.fixpoint_solver import FixedPointEncoder
from kernel.utils.paillier_tensor import PaillierTensor

LOGGER = log_utils.get_logger()


class InterActivePromoterDenseLayer(object):

    def __init__(self, params=None, layer_config=None, model_builder=None):
        self.nn_define = layer_config
        self.layer_config = layer_config

        self.provider_input_shape = None
        self.promoter_input_shape = None
        self.model = None
        self.rng_generator = random_number_generator.RandomNumberGenerator()
        self.model_builder = model_builder
        self.transfer_variable = None
        self.learning_rate = params.interactive_layer_lr
        self.encrypted_provider_dense_output = None

        self.encrypted_provider_input = None
        self.promoter_input = None
        self.promoter_output = None
        self.provider_output = None

        self.dense_output_data = None

        self.promoter_model = None
        self.provider_model = None

        self.partitions = 0
        self.do_backward_select_strategy = False
        self.encrypted_provider_input_cached = None
        self.drop_out_keep_rate = params.drop_out_keep_rate
        self.drop_out = None

        self.fixed_point_encoder = None if params.floating_point_precision is None else FixedPointEncoder(
            2 ** params.floating_point_precision)

        self.sync_output_unit = False

    def set_transfer_variable(self, transfer_variable):
        self.transfer_variable = transfer_variable

    def set_backward_select_strategy(self):
        self.do_backward_select_strategy = True

    def set_partition(self, partition):
        self.partitions = partition

    def __build_model(self, restore_stage=False):
        self.provider_model = ProviderDenseModel()
        self.provider_model.build(self.provider_input_shape, self.layer_config, self.model_builder, restore_stage)
        self.provider_model.set_learning_rate(self.learning_rate)

        self.promoter_model = PromoterDenseModel()
        self.promoter_model.build(self.promoter_input_shape, self.layer_config, self.model_builder, restore_stage)
        self.promoter_model.set_learning_rate(self.learning_rate)

    def forward(self, promoter_input, epoch=0, batch=0, train=True):
        LOGGER.info("interactive layer start forward propagation of epoch {} batch {}".format(epoch, batch))
        encrypted_provider_input = PaillierTensor(
            tb_obj=self.get_provider_encrypted_forward_from_provider(epoch, batch))

        if not self.partitions:
            self.partitions = encrypted_provider_input.partitions

        self.encrypted_provider_input = encrypted_provider_input
        self.promoter_input = promoter_input

        if self.promoter_model is None:
            LOGGER.info("building interactive layers' training model")
            self.provider_input_shape = encrypted_provider_input.shape[1]
            self.promoter_input_shape = promoter_input.shape[1] if promoter_input is not None else 0
            self.__build_model()

        if not self.sync_output_unit:
            self.sync_output_unit = True
            self.sync_interactive_layer_output_unit(self.provider_model.output_shape[0])

        provider_output = self.forward_interactive(encrypted_provider_input, epoch, batch, train)

        promoter_output = self.promoter_model.forward_dense(promoter_input)

        if not self.promoter_model.empty:
            dense_output_data = provider_output + PaillierTensor(ori_data=promoter_output, partitions=self.partitions)
        else:
            dense_output_data = provider_output

        self.dense_output_data = dense_output_data

        self.promoter_output = promoter_output
        self.provider_output = provider_output

        LOGGER.info("start to get interactive layer's activation output of epoch {} batch {}".format(epoch, batch))
        activation_out = self.provider_model.forward_activation(self.dense_output_data.numpy())
        LOGGER.info("end to get interactive layer's activation output of epoch {} batch {}".format(epoch, batch))

        if train and self.drop_out:
            activation_out = self.drop_out.forward(activation_out)

        return activation_out

    def backward(self, output_gradient, selective_ids, epoch, batch):
        if selective_ids:
            self.provider_model.select_backward_sample(selective_ids)
            self.promoter_model.select_backward_sample(selective_ids)
            if self.drop_out:
                self.drop_out.select_backward_sample(selective_ids)

        if self.do_backward_select_strategy:
            self.sync_backward_select_info(selective_ids, len(output_gradient), epoch, batch)

        if len(output_gradient) > 0:
            LOGGER.debug("interactive layer start backward propagation of epoch {} batch {}".format(epoch, batch))
            activation_backward = self.provider_model.backward_activation()[0]

            activation_gradient = output_gradient * activation_backward
            if self.drop_out:
                activation_gradient = self.drop_out.backward(activation_gradient)

            LOGGER.debug("interactive layer update promoter weight of epoch {} batch {}".format(epoch, batch))
            promoter_input_gradient = self.update_promoter(activation_gradient)

            provider_weight_gradient, acc_noise = self.backward_interactive(activation_gradient, epoch, batch)

            provider_input_gradient = self.update_provider(activation_gradient, provider_weight_gradient, acc_noise)

            self.send_provider_backward_to_provider(provider_input_gradient.get_obj(), epoch, batch)

            return promoter_input_gradient
        else:
            return []

    def _create_drop_out(self, shape):
        if self.drop_out_keep_rate and self.drop_out_keep_rate != 1:
            if not self.drop_out:
                self.drop_out = DropOut(noise_shape=shape, rate=self.drop_out_keep_rate)
                self.drop_out.set_partition(self.partitions)
                if self.do_backward_select_strategy:
                    self.drop_out.do_backward_select_strategy()

            self.drop_out.generate_mask()

    def sync_interactive_layer_output_unit(self, shape):
        self.transfer_variable.interactive_layer_output_unit.remote(shape,
                                                                    role=consts.PROVIDER,
                                                                    idx=0)

    def sync_backward_select_info(self, selective_ids, gradient_len, epoch, batch):
        self.transfer_variable.selective_info.remote((selective_ids, gradient_len),
                                                     role=consts.PROVIDER,
                                                     idx=0,
                                                     suffix=(epoch, batch,))

    def send_provider_backward_to_provider(self, provider_error, epoch, batch):
        self.transfer_variable.provider_backward.remote(provider_error,
                                                        role=consts.PROVIDER,
                                                        idx=0,
                                                        suffix=(epoch, batch,))

    def update_promoter(self, activation_gradient):
        input_gradient = self.promoter_model.get_input_gradient(activation_gradient)
        weight_gradient = self.promoter_model.get_weight_gradient(activation_gradient)
        self.promoter_model.apply_update(weight_gradient)

        return input_gradient

    def update_provider(self, activation_gradient, weight_gradient, acc_noise):
        activation_gradient_tensor = PaillierTensor(ori_data=activation_gradient, partitions=self.partitions)
        input_gradient = self.provider_model.get_input_gradient(activation_gradient_tensor, acc_noise,
                                                                encoder=self.fixed_point_encoder)
        # input_gradient = self.provider_model.get_input_gradient(activation_gradient, acc_noise)

        self.provider_model.update_weight(weight_gradient)
        self.provider_model.update_bias(activation_gradient)

        return input_gradient

    def forward_interactive(self, encrypted_provider_input, epoch, batch, train=True):
        LOGGER.info("get encrypted dense output of provider model of epoch {} batch {}".format(epoch, batch))
        mask_table = None

        encrypted_dense_output = self.provider_model.forward_dense(encrypted_provider_input, self.fixed_point_encoder)
        if train:
            self._create_drop_out(encrypted_dense_output.shape)
            if self.drop_out:
                mask_table = self.drop_out.generate_mask_table()

        self.encrypted_provider_dense_output = encrypted_dense_output

        if mask_table:
            encrypted_dense_output = encrypted_dense_output.select_columns(mask_table)

        promoter_forward_noise = self.rng_generator.fast_generate_random_number(encrypted_dense_output.shape,
                                                                                encrypted_dense_output.partitions,
                                                                                keep_table=mask_table)

        if self.fixed_point_encoder:
            encrypted_dense_output += promoter_forward_noise.encode(self.fixed_point_encoder)
        else:
            encrypted_dense_output += promoter_forward_noise

        self.send_promoter_encrypted_forward_output_with_noise_to_provider(encrypted_dense_output.get_obj(), epoch,
                                                                           batch)
        if mask_table:
            self.send_interactive_layer_drop_out_table(mask_table, epoch, batch)

        LOGGER.info("get decrypted dense output of provider model of epoch {} batch {}".format(epoch, batch))
        decrypted_dense_output = self.get_promoter_decrypted_forward_from_provider(epoch, batch)

        if mask_table:
            out = PaillierTensor(tb_obj=decrypted_dense_output) - promoter_forward_noise
            out = out.get_obj().join(mask_table, self.expand_columns)
            return PaillierTensor(tb_obj=out)
        else:
            return PaillierTensor(tb_obj=decrypted_dense_output) - promoter_forward_noise

    def backward_interactive(self, activation_gradient, epoch, batch):
        LOGGER.info("get encrypted weight gradient of epoch {} batch {}".format(epoch, batch))
        encrypted_weight_gradient = self.provider_model.get_weight_gradient(activation_gradient,
                                                                            encoder=self.fixed_point_encoder)
        if self.fixed_point_encoder:
            encrypted_weight_gradient = self.fixed_point_encoder.decode(encrypted_weight_gradient)

        noise_w = self.rng_generator.generate_random_number(encrypted_weight_gradient.shape)
        self.transfer_variable.encrypted_promoter_weight_gradient.remote(encrypted_weight_gradient + noise_w,
                                                                         role=consts.PROVIDER,
                                                                         idx=-1,
                                                                         suffix=(epoch, batch,))

        LOGGER.info("get decrypted weight graident of epoch {} batch {}".format(epoch, batch))
        decrypted_weight_gradient = self.transfer_variable.decrypted_promoter_weight_gradient.get(idx=0,
                                                                                                  suffix=(
                                                                                                      epoch, batch,))

        decrypted_weight_gradient -= noise_w

        encrypted_acc_noise = self.get_encrypted_acc_noise_from_provider(epoch, batch)

        return decrypted_weight_gradient, encrypted_acc_noise

    def get_provider_encrypted_forward_from_provider(self, epoch, batch):
        return self.transfer_variable.encrypted_provider_forward.get(idx=0,
                                                                     suffix=(epoch, batch,))

    def send_promoter_encrypted_forward_output_with_noise_to_provider(self, encrypted_promoter_forward_with_noise,
                                                                      epoch, batch):
        return self.transfer_variable.encrypted_promoter_forward.remote(encrypted_promoter_forward_with_noise,
                                                                        role=consts.PROVIDER,
                                                                        idx=-1,
                                                                        suffix=(epoch, batch,))

    def send_interactive_layer_drop_out_table(self, mask_table, epoch, batch):
        return self.transfer_variable.drop_out_table.remote(mask_table,
                                                            role=consts.PROVIDER,
                                                            idx=-1,
                                                            suffix=(epoch, batch,))

    def get_promoter_decrypted_forward_from_provider(self, epoch, batch):
        return self.transfer_variable.decrypted_promoter_fowrad.get(idx=0,
                                                                    suffix=(epoch, batch,))

    def get_encrypted_acc_noise_from_provider(self, epoch, batch):
        return self.transfer_variable.encrypted_acc_noise.get(idx=0,
                                                              suffix=(epoch, batch,))

    def get_output_shape(self):
        return self.provider_model.output_shape

    @staticmethod
    def expand_columns(tensor, keep_array):
        shape = keep_array.shape
        tensor = np.reshape(tensor, (tensor.size,))
        keep = np.reshape(keep_array, (keep_array.size,))
        ret_tensor = []
        idx = 0
        for x in keep:
            if x == 0:
                ret_tensor.append(0)
            else:
                ret_tensor.append(tensor[idx])
                idx += 1

        return np.reshape(np.array(ret_tensor), shape)

    def export_model(self):
        interactive_layer_param = InteractiveLayerParam()
        interactive_layer_param.interactive_promoter_saved_model_bytes = self.promoter_model.export_model()
        interactive_layer_param.interactive_provider_saved_model_bytes = self.provider_model.export_model()
        interactive_layer_param.provider_input_shape = self.provider_input_shape
        interactive_layer_param.promoter_input_shape = self.promoter_input_shape

        return interactive_layer_param

    def restore_model(self, interactive_layer_param):
        self.provider_input_shape = interactive_layer_param.provider_input_shape
        self.promoter_input_shape = interactive_layer_param.promoter_input_shape

        self.__build_model(restore_stage=True)
        self.promoter_model.restore_model(interactive_layer_param.interactive_promoter_saved_model_bytes)
        self.provider_model.restore_model(interactive_layer_param.interactive_provider_saved_model_bytes)


class InteractiveProviderDenseLayer(object):
    def __init__(self, params):
        self.acc_noise = None
        self.learning_rate = params.interactive_layer_lr
        self.encrypted_mode_calculator_param = params.encrypted_model_calculator_param
        self.encrypter = self.generate_encrypter(params)
        self.train_encrypted_calculator = []
        self.predict_encrypted_calculator = []
        self.transfer_variable = None
        self.partitions = 1
        self.input_shape = None
        self.output_unit = None
        self.rng_generator = random_number_generator.RandomNumberGenerator()
        self.do_backward_select_strategy = False
        self.drop_out_keep_rate = params.drop_out_keep_rate

        self.fixed_point_encoder = None if params.floating_point_precision is None else FixedPointEncoder(
            2 ** params.floating_point_precision)
        self.mask_table = None

    def set_transfer_variable(self, transfer_variable):
        self.transfer_variable = transfer_variable

    def generated_encrypted_calculator(self):
        encrypted_calculator = EncryptModeCalculator(self.encrypter,
                                                     self.encrypted_mode_calculator_param.mode,
                                                     self.encrypted_mode_calculator_param.re_encrypted_rate)

        return encrypted_calculator

    def set_partition(self, partition):
        self.partitions = partition

    def set_backward_select_strategy(self):
        self.do_backward_select_strategy = True

    def forward(self, provider_input, epoch=0, batch=0, train=True):
        if batch >= len(self.train_encrypted_calculator):
            self.train_encrypted_calculator.append(self.generated_encrypted_calculator())

        LOGGER.info("forward propagation: encrypt provider_bottom_output of epoch {} batch {}".format(epoch, batch))
        provider_input = PaillierTensor(ori_data=provider_input, partitions=self.partitions)

        encrypted_provider_input = provider_input.encrypt(self.train_encrypted_calculator[batch])
        self.send_provider_encrypted_forward_to_promoter(encrypted_provider_input.get_obj(), epoch, batch)

        encrypted_promoter_forward = PaillierTensor(
            tb_obj=self.get_promoter_encrypted_forwrad_from_promoter(epoch, batch))

        decrypted_promoter_forward = encrypted_promoter_forward.decrypt(self.encrypter)
        if self.fixed_point_encoder:
            decrypted_promoter_forward = decrypted_promoter_forward.decode(self.fixed_point_encoder)

        if self.acc_noise is None:
            self.input_shape = provider_input.shape[1]
            self.output_unit = self.get_interactive_layer_output_unit()
            self.acc_noise = np.zeros((self.input_shape, self.output_unit))

        mask_table = None
        if train and self.drop_out_keep_rate and self.drop_out_keep_rate < 1:
            mask_table = self.get_interactive_layer_drop_out_table(epoch, batch)

        if mask_table:
            decrypted_promoter_forward_with_noise = decrypted_promoter_forward + (
                    provider_input * self.acc_noise).select_columns(mask_table)
            self.mask_table = mask_table
        else:
            decrypted_promoter_forward_with_noise = decrypted_promoter_forward + (provider_input * self.acc_noise)

        self.send_decrypted_promoter_forward_with_noise_to_promoter(decrypted_promoter_forward_with_noise.get_obj(),
                                                                    epoch,
                                                                    batch)

    def backward(self, epoch, batch):
        do_backward = True
        selective_ids = []
        if self.do_backward_select_strategy:
            selective_ids, do_backward = self.sync_backward_select_info(epoch, batch)

        if not do_backward:
            return [], selective_ids

        encrypted_promoter_weight_gradient = self.get_promoter_encrypted_weight_gradient_from_promoter(epoch, batch)

        LOGGER.info("decrypt weight gradient of epoch {} batch {}".format(epoch, batch))
        decrypted_promoter_weight_gradient = self.encrypter.recursive_decrypt(encrypted_promoter_weight_gradient)

        noise_weight_gradient = self.rng_generator.generate_random_number((self.input_shape, self.output_unit))

        decrypted_promoter_weight_gradient += noise_weight_gradient / self.learning_rate

        self.send_promoter_decrypted_weight_gradient_to_promoter(decrypted_promoter_weight_gradient, epoch, batch)

        LOGGER.info("encrypt acc_noise of epoch {} batch {}".format(epoch, batch))
        encrypted_acc_noise = self.encrypter.recursive_encrypt(self.acc_noise)
        self.send_encrypted_acc_noise_to_promoter(encrypted_acc_noise, epoch, batch)

        self.acc_noise += noise_weight_gradient
        provider_input_gradient = PaillierTensor(tb_obj=self.get_provider_backward_from_promoter(epoch, batch))

        provider_input_gradient = provider_input_gradient.decrypt(self.encrypter)

        if self.fixed_point_encoder:
            provider_input_gradient = provider_input_gradient.decode(self.fixed_point_encoder).numpy()
        else:
            provider_input_gradient = provider_input_gradient.numpy()

        return provider_input_gradient, selective_ids

    def sync_backward_select_info(self, epoch, batch):
        selective_ids, do_backward = self.transfer_variable.selective_info.get(idx=0,
                                                                               suffix=(epoch, batch,))

        return selective_ids, do_backward

    def send_encrypted_acc_noise_to_promoter(self, encrypted_acc_noise, epoch, batch):
        self.transfer_variable.encrypted_acc_noise.remote(encrypted_acc_noise,
                                                          idx=0,
                                                          role=consts.PROMOTER,
                                                          suffix=(epoch, batch,))

    def get_interactive_layer_output_unit(self):
        return self.transfer_variable.interactive_layer_output_unit.get(idx=0)

    def get_promoter_encrypted_weight_gradient_from_promoter(self, epoch, batch):
        encrypted_promoter_weight_gradient = self.transfer_variable.encrypted_promoter_weight_gradient.get(idx=0,
                                                                                                           suffix=(
                                                                                                               epoch,
                                                                                                               batch,))

        return encrypted_promoter_weight_gradient

    def get_interactive_layer_drop_out_table(self, epoch, batch):
        return self.transfer_variable.drop_out_table.get(idx=0,
                                                         suffix=(epoch, batch,))

    def send_provider_encrypted_forward_to_promoter(self, encrypted_provider_input, epoch, batch):
        self.transfer_variable.encrypted_provider_forward.remote(encrypted_provider_input,
                                                                 idx=0,
                                                                 role=consts.PROMOTER,
                                                                 suffix=(epoch, batch,))

    def send_promoter_decrypted_weight_gradient_to_promoter(self, decrypted_promoter_weight_gradient, epoch, batch):
        self.transfer_variable.decrypted_promoter_weight_gradient.remote(decrypted_promoter_weight_gradient,
                                                                         idx=0,
                                                                         role=consts.PROMOTER,
                                                                         suffix=(epoch, batch,))

    def get_provider_backward_from_promoter(self, epoch, batch):
        provider_backward = self.transfer_variable.provider_backward.get(idx=0,
                                                                         suffix=(epoch, batch,))

        return provider_backward

    def get_promoter_encrypted_forwrad_from_promoter(self, epoch, batch):
        encrypted_promoter_forward = self.transfer_variable.encrypted_promoter_forward.get(idx=0,
                                                                                           suffix=(epoch, batch,))

        return encrypted_promoter_forward

    def send_decrypted_promoter_forward_with_noise_to_promoter(self, decrypted_promoter_forward_with_noise, epoch,
                                                               batch):
        self.transfer_variable.decrypted_promoter_fowrad.remote(decrypted_promoter_forward_with_noise,
                                                                idx=0,
                                                                role=consts.PROMOTER,
                                                                suffix=(epoch, batch,))

    def generate_encrypter(self, param):
        LOGGER.info("generate encrypter")
        if param.encrypt_param.method.lower() == consts.PAILLIER.lower():
            encrypter = PaillierEncrypt()
            encrypter.generate_key(param.encrypt_param.key_length)
        else:
            raise NotImplementedError("encrypt method not supported yet!!!")

        return encrypter

    def export_model(self):
        interactive_layer_param = InteractiveLayerParam()
        interactive_layer_param.acc_noise = pickle.dumps(self.acc_noise)

        return interactive_layer_param

    def restore_model(self, interactive_layer_param):
        self.acc_noise = pickle.loads(interactive_layer_param.acc_noise)
