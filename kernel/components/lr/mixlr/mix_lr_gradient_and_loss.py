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



import functools
import random

import numpy as np
import scipy.sparse as sp

from common.python.utils import log_utils
from kernel.base.sparse_vector import SparseVector
from kernel.components.lr.vertlr.sync import loss_sync
from kernel.optimizer.activation import sigmoid
from kernel.utils import base_operator
from kernel.utils import consts
from kernel.utils import data_util
from kernel.utils.base_operator import vec_dot
from kernel.utils.random_number_generator import RandomNumberGenerator

LOGGER = log_utils.get_logger()


def __compute_partition_gradient(data, fit_intercept=True, is_sparse=False):
    """
    Compute vert regression gradient for:
    gradient = ∑d*x, where d is fore_gradient which differ from different algorithm
    Parameters
    ----------
    data: DSource, include fore_gradient and features
    fit_intercept: bool, if model has interception or not. Default True

    Returns
    ----------
    numpy.ndarray
        vert regression model gradient
    """
    feature = []
    fore_gradient = []

    if is_sparse:
        row_indice = []
        col_indice = []
        data_value = []

        row = 0
        feature_shape = None
        for key, (sparse_features, d) in data:
            fore_gradient.append(d)
            assert isinstance(sparse_features, SparseVector)
            if feature_shape is None:
                feature_shape = sparse_features.get_shape()
            for idx, v in sparse_features.get_all_data():
                col_indice.append(idx)
                row_indice.append(row)
                data_value.append(v)
            row += 1
        if feature_shape is None or feature_shape == 0:
            return 0
        sparse_matrix = sp.csr_matrix((data_value, (row_indice, col_indice)), shape=(row, feature_shape))
        fore_gradient = np.array(fore_gradient)

        # gradient = sparse_matrix.transpose().dot(fore_gradient).tolist()
        gradient = base_operator.dot(sparse_matrix.transpose(), fore_gradient).tolist()
        if fit_intercept:
            bias_grad = np.sum(fore_gradient)
            gradient.append(bias_grad)
            LOGGER.debug("In first method, gradient: {}, bias_grad: {}".format(gradient, bias_grad))
        return np.array(gradient)

    else:
        for key, value in data:
            feature.append(value[0])
            fore_gradient.append(value[1])
        feature = np.array(feature)
        fore_gradient = np.array(fore_gradient)
        if feature.shape[0] <= 0:
            return 0

        gradient = base_operator.dot(feature.transpose(), fore_gradient)
        gradient = gradient.tolist()
        if fit_intercept:
            bias_grad = np.sum(fore_gradient)
            gradient.append(bias_grad)
        return np.array(gradient)


def compute_gradient(data_instances, fore_gradient, fit_intercept):
    """
    Compute vert-regression gradient
    Parameters
    ----------
    data_instances: DSource, input data
    fore_gradient: DSource, fore_gradient
    fit_intercept: bool, if model has intercept or not

    Returns
    ----------
    DSource
        the vert regression model's gradient
    """
    feat_join_grad = data_instances.join(fore_gradient,
                                         lambda d, g: (d.features, g))
    is_sparse = data_util.is_sparse_data(data_instances)
    f = functools.partial(__compute_partition_gradient,
                          fit_intercept=fit_intercept,
                          is_sparse=is_sparse)
    gradient_partition = feat_join_grad.mapPartitions(f)
    gradient_partition = gradient_partition.reduce(lambda x, y: x + y)

    gradient = gradient_partition / data_instances.count()

    return gradient


class MixGradientBase(object):
    def federated_compute_gradient_and_loss(self, *args):
        raise NotImplementedError("Should not call here")

    def set_total_batch_nums(self, total_batch_nums):
        """
        Use for sqn gradient.
        """
        pass


class Promoter(MixGradientBase, loss_sync.Promoter):

    def __init__(self):
        self.provider_forwards = None
        self.fore_gradient = None
        self.forwards = None
        # self.aggregated_forwards = None

    def _register_gradient_sync(self, provider_weight_transfer, provider_forward_transfer, fore_gradient_transfer,
                                provider_gradient_r_transfer, provider_en_gradient_r_transfer,
                                provider_model_weights_r_transfer,
                                provider_optimizer_model_weights_transfer):
        self.provider_weight_transfer = provider_weight_transfer
        self.provider_forward_transfer = provider_forward_transfer
        self.fore_gradient_transfer = fore_gradient_transfer
        self.provider_gradient_r_transfer = provider_gradient_r_transfer
        self.provider_en_gradient_r_transfer = provider_en_gradient_r_transfer
        self.provider_model_weights_r_transfer = provider_model_weights_r_transfer
        self.provider_optimizer_model_weights_transfer = provider_optimizer_model_weights_transfer

    def register_gradient_procedure(self, transfer_variables):
        self._register_gradient_sync(transfer_variables.provider_weight,
                                     transfer_variables.provider_forward_dict,
                                     transfer_variables.fore_gradient,
                                     transfer_variables.provider_gradient_r,
                                     transfer_variables.provider_en_gradient_r,
                                     transfer_variables.provider_model_weights_r,
                                     transfer_variables.provider_optimizer_model_weights)

        self._register_loss_sync(transfer_variables.provider_loss_regular)

    def compute_fore_gradient(self, data_instances, model_weights, offset=None):
        """
        gradient = d.dot(x)
        Define (sigmoid(wx+b) - y) as fore_gradient

        """
        # X.dot(W)+b
        half_wx = data_instances.mapValues(
            lambda v: vec_dot(v.features, model_weights.coef_) + model_weights.intercept_)
        self.forwards = half_wx

        for provider_forward in self.provider_forwards:
            self.forwards = self.forwards.join(provider_forward, lambda g, h: g + h)

        y_hat = self.forwards.mapValues(lambda p: sigmoid(p))

        fore_gradient = y_hat.join(data_instances, lambda y_hat, d: y_hat - d.label)

        return fore_gradient, y_hat

    def compute_forward_hess(self, data_instances, delta_s, provider_forwards):
        """
        To compute Hessian matrix, y, s are needed.
        g = (1/N)*∑(0.25 * wx - 0.5 * y) * x
        y = ∇2^F(w_t)s_t = g' * s = (1/N)*∑(0.25 * x * s) * x
        define forward_hess = (1/N)*∑(0.25 * x * s)
        """
        forwards = data_instances.mapValues(
            lambda v: (np.dot(v.features, delta_s.coef_) + delta_s.intercept_) * 0.25)
        for provider_forward in provider_forwards:
            forwards = forwards.join(provider_forward, lambda g, h: g + (h * 0.25))
        # forward_hess = forwards.mapValues(lambda x: 0.25 * x / sample_size)
        hess_vector = compute_gradient(data_instances, forwards, delta_s.fit_intercept)
        return forwards, np.array(hess_vector)

    def compute_and_aggregate_forwards(self, data_instances, model_weights,
                                       encrypted_calculator, batch_index, offset=None):
        raise NotImplementedError("Function should not be called here")

    @staticmethod
    def separate(value, size_list):
        """
        Separate value in order to several set according size_list
        Parameters
        ----------
        value: list or ndarray, input data
        size_list: list, each set size

        Returns
        ----------
        list
            set after separate
        """
        separate_res = []
        cur = 0
        for size in size_list:
            separate_res.append(value[cur:cur + size])
            cur += size
        return separate_res

    def separate_weights_and_sync(self, model_weights, weight_size_list, suffix):
        weight_list = self.separate(model_weights.unboxed, weight_size_list)
        promoter_weight = weight_list[-1]
        self.remote_provider_optimizer_weights(weight_list[:-1], suffix)
        return promoter_weight

    def federated_compute_gradient_and_loss(self, data_instances, cipher_operator, encrypted_calculator, model_weights,
                                            optimizer,
                                            loss_method, n_iter_, batch_index, offset=None):
        """
          Linear model gradient core
          Step 1: get provider forwards which differ from different algorithm
                  For Logistic Regression and Linear Regression: forwards = wx
                  For Poisson Regression, forwards = exp(wx)

          Step 2: Compute  fore_gradient:  d = sigmoid(wx)-y

          Step 3: send encrypted fore_gradient:  d = [sigmoid(wx)-y]

          Step 4: Compute unilateral gradient = ∑d*x,

          """
        current_suffix = (n_iter_, batch_index)

        self.provider_forwards = self.get_provider_forward(suffix=current_suffix)

        self.fore_gradient, y_hat = self.compute_fore_gradient(data_instances, model_weights, offset)
        encrypted_fore_gradient = encrypted_calculator[batch_index].encrypt(self.fore_gradient)
        self.remote_fore_gradient(encrypted_fore_gradient, suffix=current_suffix)

        self.decrypt_provider_gradient_and_remote(cipher_operator, suffix=current_suffix)

        unilateral_gradient = []
        if model_weights:
            unilateral_gradient = compute_gradient(data_instances,
                                                   self.fore_gradient,
                                                   model_weights.fit_intercept)
            if optimizer is not None:
                unilateral_gradient = optimizer.add_regular_to_grad(unilateral_gradient, model_weights)

        gradient = optimizer.apply_gradients(unilateral_gradient)

        loss_norm = optimizer.loss_norm(model_weights)
        if loss_norm is not None:
            provider_loss_regular = self.get_provider_loss_regular(suffix=current_suffix)
        else:
            provider_loss_regular = []

        # if len(self.provider_forwards) > 1:
        #     LOGGER.info("More than one provider exist, loss is not available")
        # else:
        y = data_instances.mapValues(lambda instance: instance.label)
        loss = loss_method.compute_loss(y, y_hat)

        if loss_norm is not None:
            loss += loss_norm
            for provider_loss_norm in provider_loss_regular:
                loss += provider_loss_norm
        LOGGER.debug("In compute_loss, iter: {} ,loss is: {}".format(n_iter_, loss))

        return gradient, loss

    def get_provider_forward(self, suffix=tuple()):
        provider_forward = self.provider_forward_transfer.get(idx=-1, suffix=suffix)
        return provider_forward

    def get_provider_model_weights_r(self, suffix=tuple()):
        model_weights_r = self.provider_model_weights_r_transfer.get(idx=-1, suffix=suffix)
        return model_weights_r

    def get_provider_weight(self):
        provider_weight = self.provider_weight_transfer.get(idx=-1)
        return provider_weight

    def remote_fore_gradient(self, fore_gradient, suffix=tuple()):
        self.fore_gradient_transfer.remote(obj=fore_gradient, role=consts.PROVIDER, idx=-1, suffix=suffix)

    def decrypt_provider_gradient_and_remote(self, cipher_operator, suffix=tuple()):
        en_provider_gradient_rs = self.provider_en_gradient_r_transfer.get(idx=-1, suffix=suffix)
        # provider_grad_r = en_provider_gradient_r[0].decrypt(cipher_operator)
        for idx, en_provider_gradient_r in enumerate(en_provider_gradient_rs):
            provider_grad_r = np.array(cipher_operator.decrypt_list(en_provider_gradient_r))
            self.provider_gradient_r_transfer.remote(provider_grad_r,
                                                     role=consts.PROVIDER,
                                                     idx=idx,
                                                     suffix=suffix)

    def remote_provider_optimizer_weights(self, provider_optimizer_weights, suffix=tuple()):
        for idx, provider_optimizer_weight in enumerate(provider_optimizer_weights):
            self.provider_optimizer_model_weights_transfer.remote(provider_optimizer_weight,
                                                                  role=consts.PROVIDER,
                                                                  idx=idx,
                                                                  suffix=suffix)


class Provider(MixGradientBase, loss_sync.Provider):

    def __init__(self):
        self.forwards = None
        self.fore_gradient = None

    def _register_gradient_sync(self, provider_weight_transfer, provider_forward_transfer, fore_gradient_transfer,
                                provider_gradient_r_transfer, provider_en_gradient_r_transfer,
                                provider_model_weights_r_transfer,
                                provider_optimizer_model_weights_transfer):
        self.provider_weight_transfer = provider_weight_transfer
        self.provider_forward_transfer = provider_forward_transfer
        self.fore_gradient_transfer = fore_gradient_transfer
        self.provider_gradient_r_transfer = provider_gradient_r_transfer
        self.provider_en_gradient_r_transfer = provider_en_gradient_r_transfer
        self.provider_model_weights_r_transfer = provider_model_weights_r_transfer
        self.provider_optimizer_model_weights_transfer = provider_optimizer_model_weights_transfer

    def register_gradient_procedure(self, transfer_variables):
        self._register_gradient_sync(transfer_variables.provider_weight,
                                     transfer_variables.provider_forward_dict,
                                     transfer_variables.fore_gradient,
                                     transfer_variables.provider_gradient_r,
                                     transfer_variables.provider_en_gradient_r,
                                     transfer_variables.provider_model_weights_r,
                                     transfer_variables.provider_optimizer_model_weights)

        self._register_loss_sync(transfer_variables.provider_loss_regular)

    def federated_compute_weights(self, model_weights, n_iter_, random_cipher_seed, mix_promoter_member_id):
        LOGGER.debug("before agg weights: {}".format(model_weights.unboxed))
        random_seed = n_iter_ + random_cipher_seed
        r_w = RandomNumberGenerator(-1, 1).generate_fake_random_number(model_weights.unboxed.shape, seed=random_seed)
        weights = np.add(model_weights.unboxed, r_w)
        self.sync_model_weights_r(weights, suffix=(n_iter_,), member_id_list=[mix_promoter_member_id])
        optim_provider_weights = self.get_optimizer_model_weights(suffix=(n_iter_,),
                                                                  member_id_list=[mix_promoter_member_id])
        optim_provider_weights = np.subtract(optim_provider_weights, r_w)
        LOGGER.debug("after agg weights: {}".format(optim_provider_weights))
        return optim_provider_weights

    def federated_compute_gradient_and_loss(self, data_instances, cipher_operator,
                                            model_weights, optimizer, n_iter_, batch_index,
                                            mix_promoter_member_id):
        """
        Linear model gradient core
        Step 1: compute forwards and send to promoter : forwards = wx + b
        Step 2:get fore_gradient from promoter: d = [sigmoid(wx)-y]
        Step 3: compute gradient and add random r :  gradient = (1/n)*∑(d.dot(x))

        """
        current_suffix = (n_iter_, batch_index)
        self.forwards = self.compute_forwards(data_instances, model_weights)
        self.remote_provider_forward(self.forwards, suffix=current_suffix, member_id_list=[mix_promoter_member_id])
        fore_gradient = self.get_fore_gradient(suffix=current_suffix, member_id_list=[mix_promoter_member_id])

        unilateral_gradient = compute_gradient(data_instances,
                                               fore_gradient,
                                               model_weights.fit_intercept)
        if optimizer is not None:
            unilateral_gradient = optimizer.add_regular_to_grad(unilateral_gradient, model_weights)

        r = RandomNumberGenerator(-1, 1).generate_random_number(unilateral_gradient.shape)
        # r = PaillierTensor(ori_data=r)
        # encrypted_r = r.encrypt(encrypted_calculator[batch_index])
        # en_gradient_r = encrypted_r.__add__(PaillierTensor(unilateral_gradient))
        # encrypted_forward = encrypted_calculator[batch_index].encrypt(self.forwards)

        encrypted_r = cipher_operator.recursive_encrypt(r)
        # en_gradient_r = encrypted_r + unilateral_gradient
        en_gradient_r = np.add(encrypted_r, unilateral_gradient)

        gradient_r = self.sync_gradient_r(en_gradient_r, suffix=current_suffix, member_id_list=[mix_promoter_member_id])
        # gradient = gradient_r - r
        gradient = np.subtract(gradient_r, r)
        gradient = optimizer.apply_gradients(gradient)

        loss_regular = optimizer.loss_norm(model_weights)
        norm_r = random.uniform(-loss_regular * 0.1, loss_regular * 0.1)
        loss_regular = loss_regular + norm_r
        # if loss_regular is not None:
        #     loss_regular = cipher_operator.encrypt(loss_regular)
        self.remote_loss_regular(loss_regular, suffix=current_suffix, member_id_list=[mix_promoter_member_id])

        return gradient

    def compute_sqn_forwards(self, data_instances, delta_s, cipher_operator):
        """
        To compute Hessian matrix, y, s are needed.
        g = (1/N)*∑(0.25 * wx - 0.5 * y) * x
        y = ∇2^F(w_t)s_t = g' * s = (1/N)*∑(0.25 * x * s) * x
        define forward_hess = ∑(0.25 * x * s)
        """
        sqn_forwards = data_instances.mapValues(
            lambda v: cipher_operator.encrypt(np.dot(v.features, delta_s.coef_) + delta_s.intercept_))
        # forward_sum = sqn_forwards.reduce(reduce_add)
        return sqn_forwards

    def compute_forward_hess(self, data_instances, delta_s, forward_hess):
        """
        To compute Hessian matrix, y, s are needed.
        g = (1/N)*∑(0.25 * wx - 0.5 * y) * x
        y = ∇2^F(w_t)s_t = g' * s = (1/N)*∑(0.25 * x * s) * x
        define forward_hess = (0.25 * x * s)
        """
        hess_vector = compute_gradient(data_instances,
                                       forward_hess,
                                       delta_s.fit_intercept)
        return np.array(hess_vector)

    def compute_forwards(self, data_instances, model_weights):
        """
        forwards = wx
        """
        # w = model_weights.coef_.reshape(model_weights.coef_.size)
        wx = data_instances.mapValues(lambda v: vec_dot(v.features, model_weights.coef_) + model_weights.intercept_,
                                      need_send=True)
        return wx

    def remote_provider_forward(self, provider_forward, suffix=tuple(), member_id_list=None):
        self.provider_forward_transfer.remote(obj=provider_forward, role=consts.PROMOTER, idx=0, suffix=suffix,
                                              member_id_list=member_id_list)

    def remote_provider_weight(self, provider_weight, member_id_list):
        self.provider_weight_transfer.remote(obj=provider_weight, role=consts.PROMOTER, idx=0,
                                             member_id_list=member_id_list)

    def get_fore_gradient(self, suffix=tuple(), member_id_list=None):
        fore_gradients = self.fore_gradient_transfer.get(idx=0, suffix=suffix, member_id_list=member_id_list)
        return fore_gradients

    def sync_gradient_r(self, e_gradient_r, suffix=tuple(), member_id_list=None):
        self.provider_en_gradient_r_transfer.remote(obj=e_gradient_r, role=consts.PROMOTER, idx=-1, suffix=suffix,
                                                    member_id_list=member_id_list)
        gradient_r = self.provider_gradient_r_transfer.get(idx=0, suffix=suffix, member_id_list=member_id_list)
        return gradient_r

    def sync_model_weights_r(self, model_weights_r, suffix=tuple(), member_id_list=None):
        self.provider_model_weights_r_transfer.remote(obj=model_weights_r, role=consts.PROMOTER, idx=-1, suffix=suffix,
                                                      member_id_list=member_id_list)

    def get_optimizer_model_weights(self, suffix=tuple(), member_id_list=None):
        optimizer_model_weights = self.provider_optimizer_model_weights_transfer.get(idx=0, suffix=suffix,
                                                                                     member_id_list=member_id_list)
        return optimizer_model_weights
