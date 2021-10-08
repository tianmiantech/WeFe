#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright 2021 The WeFe Authors. All Rights Reserved.
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





import numpy as np

from common.python.utils import log_utils
from kernel.components.lr.base_lr_model import BaseLRModel
from kernel.components.lr.lr_model_weight import LRModelWeights as LogisticRegressionWeights
from kernel.components.lr.param import MixLogisticParam
from kernel.optimizer.optimizer import optimizer_factory
from kernel.protobuf.generated import lr_model_meta_pb2
from kernel.transfer.framework.horz.procedure import aggregator
from kernel.transfer.variables.transfer_class.mix_lr_transfer_variable import MixLRTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MixLRArbiter(BaseLRModel):
    def __init__(self):
        super(MixLRArbiter, self).__init__()
        self.model_name = 'MixLogisticRegression'
        self.model_param_name = 'MixLogisticRegressionParam'
        self.model_meta_name = 'MixLogisticRegressionMeta'
        self.mode = consts.HORZ
        self.model_param = MixLogisticParam()
        self.aggregator = None
        self.is_serving_model = True
        self.re_encrypt_times = []  # Record the times needed for each provider

        self.loss_history = []
        self.is_converged = False
        self.role = consts.ARBITER
        self.aggregator = aggregator.Arbiter()
        self.model_weights = None
        self.provider_predict_results = []

    def _init_model(self, params):
        super(MixLRArbiter, self)._init_model(params)

        self.transfer_variable = MixLRTransferVariable()
        self.aggregator.register_aggregator(self.transfer_variable)
        self.optimizer = optimizer_factory(params)
        self.aggregate_iters = params.aggregate_iters

    def sync_begin_iter(self):
        begin_iters = self.transfer_variable.begin_iter.get(idx=-1)
        iter0 = begin_iters[0]
        for iter in begin_iters[1:]:
            assert iter0 == iter
        return iter0

    def fit(self, data_instances=None, validate_data=None):
        LOGGER.info("Enter mix lr arbiter fit")
        max_iter = self.max_iter
        # validation_strategy = self.init_validation_strategy()

        continue_flag = self.sync_begin_iter()
        self.n_iter_ = continue_flag
        while self.n_iter_ < max_iter + 1:
            suffix = (self.n_iter_,)

            if ((self.n_iter_ > 0 and self.n_iter_ % self.aggregate_iters == 0) or self.n_iter_ == max_iter) and continue_flag == 0:
                merged_model = self.aggregator.aggregate_and_broadcast(suffix=suffix)
                total_loss = self.aggregator.aggregate_loss(suffix)
                self.callback_loss(self.n_iter_, total_loss)
                self.loss_history.append(total_loss)
                if self.use_loss:
                    converge_var = total_loss
                else:
                    converge_var = np.array(merged_model.unboxed)

                self.is_converged = self.aggregator.send_converge_status(self.converge_func.is_converge,
                                                                         (converge_var,),
                                                                         suffix=(self.n_iter_,))
                LOGGER.info("n_iters: {}, total_loss: {}, converge flag is :{}".format(self.n_iter_,
                                                                                       total_loss,
                                                                                       self.is_converged))
                if self.is_converged or self.n_iter_ == max_iter:
                    break
                self.model_weights = LogisticRegressionWeights(merged_model.unboxed,
                                                               self.model_param.init_param.fit_intercept)
                if self.header is None:
                    self.header = ['x' + str(i) for i in range(len(self.model_weights.coef_))]

            # validation_strategy.validate(self, self.n_iter_)
            self.n_iter_ += 1

        LOGGER.info("Finish Training task, total iters: {}".format(self.n_iter_))

    def predict(self, data_instantces=None):

        LOGGER.info(f'Start predict task')

    @property
    def use_loss(self):
        if self.model_param.early_stop == 'weight_diff':
            return False
        return True

    def _get_meta(self):
        meta_protobuf_obj = lr_model_meta_pb2.LRModelMeta(penalty=self.model_param.penalty,
                                                          tol=self.model_param.tol,
                                                          alpha=self.alpha,
                                                          optimizer=self.model_param.optimizer,
                                                          batch_size=self.batch_size,
                                                          learning_rate=self.model_param.learning_rate,
                                                          max_iter=self.max_iter,
                                                          early_stop=self.model_param.early_stop,
                                                          fit_intercept=self.fit_intercept,
                                                          re_encrypt_batches=None,
                                                          need_one_vs_rest=self.need_one_vs_rest)
        return meta_protobuf_obj
