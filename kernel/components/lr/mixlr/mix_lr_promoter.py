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
from kernel.components.lr.lr_model_weight import LRModelWeights
from kernel.components.lr.mixlr import mix_lr_gradient_and_loss
from kernel.components.lr.mixlr.mix_lr_base import MixLRBaseModel
from kernel.components.lr.vertlr.sync import converg_sync
from kernel.components.lr.vertlr.sync import iter_sync, paillier_keygen_sync, batch_info_sync
from kernel.optimizer import activation
from kernel.optimizer.loss import SigmoidBinaryCrossEntropyLoss
from kernel.optimizer.optimizer import optimizer_factory
from kernel.security import EncryptModeCalculator
from kernel.transfer.framework.horz.procedure import aggregator
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MixLRPromoter(MixLRBaseModel):
    def __init__(self):
        super().__init__()
        self.data_batch_count = []
        # self.promoter_forward = None
        self.role = consts.PROMOTER
        self.cipher = paillier_keygen_sync.Promoter()
        self.batch_generator = batch_info_sync.Promoter()
        self.gradient_loss_operator = mix_lr_gradient_and_loss.Promoter()
        self.converge_procedure = converg_sync.Promoter()
        self.iter_transfer = iter_sync.Promoter()
        self.encrypted_calculator = None
        self.no_weights = False
        self.loss_method = SigmoidBinaryCrossEntropyLoss()
        self.model_save_to_storage = True
        self.need_one_vs_rest = False
        self.aggregator = aggregator.Client()

    def _init_model(self, params):
        super()._init_model(params)

        self.aggregator.register_aggregator(self.transfer_variable)
        self.optimizer = optimizer_factory(params)
        self.aggregate_iters = params.aggregate_iters

    def fit(self, data_instances, validate_data=None):
        """
        Train lr model of role promoter
        Parameters
        ----------
        data_instances: DSource of Instance, input data
        """

        LOGGER.info("Enter vert_lr_promoter fit")
        self._abnormal_detection(data_instances)
        self.header = self.get_header(data_instances)

        classes = self.one_vs_rest_obj.get_data_classes(data_instances)
        #
        if len(classes) > 2:
            self.need_one_vs_rest = True
            self.in_one_vs_rest = True
            self.one_vs_rest_fit(train_data=data_instances, validate_data=validate_data)
        else:
            self.need_one_vs_rest = False
            self.fit_binary(data_instances, validate_data)

    def fit_binary(self, data_instances, validate_data=None):
        LOGGER.info("Enter mix_lr_promoter fit_binary")
        self.header = self.get_header(data_instances)

        self.validation_strategy = self.init_validation_strategy(data_instances, validate_data)
        # data_instances = data_instances.mapValues(VertLRPromoter.load_data)
        LOGGER.debug(f"MODEL_STEP After load data, data count: {data_instances.count()}")

        # step 1: gen key and send to provider
        self.cipher_operator = self.cipher.paillier_keygen_and_broadcast(self.model_param.encrypt_param.key_length)

        LOGGER.info("Generate mini-batch from input data")

        self.batch_generator.initialize_batch_generator(data_instances, self.batch_size)
        self.gradient_loss_operator.set_total_batch_nums(self.batch_generator.batch_nums)
        self.encrypted_calculator = [EncryptModeCalculator(self.cipher_operator,
                                                           self.encrypted_mode_calculator_param.mode,
                                                           self.encrypted_mode_calculator_param.re_encrypted_rate) for _
                                     in range(self.batch_generator.batch_nums)]

        LOGGER.info("Start initialize model.")
        LOGGER.info("fit_intercept:{}".format(self.init_param_obj.fit_intercept))
        model_shape = self.get_features_shape(data_instances)

        w = self.initializer.init_model(model_shape, init_params=self.init_param_obj)
        self.model_weights = LRModelWeights(w, fit_intercept=self.fit_intercept)
        cur_best_model = self.tracker.get_training_best_model()
        continue_flag = 0
        if cur_best_model is not None:
            model_param = cur_best_model["Model_Param"]
            iteration = model_param['iters']
            self.load_single_model(model_param)
            continue_flag = iteration + 1
        self.n_iter_ = continue_flag
        self.iter_transfer.sync_cur_iter(self.n_iter_)
        self.tracker.set_task_progress(self.n_iter_)
        batch_model_weights = self.model_weights
        degree = 0
        iter_loss = None
        while self.n_iter_ < self.max_iter + 1:
            LOGGER.info("iter:{}".format(self.n_iter_))
            batch_data_generator = self.batch_generator.generate_batch_data()
            self.optimizer.set_iters(self.n_iter_)
            if ((
                        self.n_iter_ > 0 and self.n_iter_ % self.aggregate_iters == 0) or self.n_iter_ == self.max_iter) and continue_flag == 0:

                self.federated_compute_weights(batch_model_weights, degree)
                # loss = self._compute_loss(data_instances)
                self.aggregator.send_loss(iter_loss, degree=degree, suffix=(self.n_iter_,))
                degree = 0

                self.is_converged = self.aggregator.get_converge_status(suffix=(self.n_iter_,))
                LOGGER.info(
                    "n_iters: {}, loss: {} converge flag is :{}".format(self.n_iter_, iter_loss, self.is_converged))

                self.converge_procedure.sync_converge_info(self.is_converged, suffix=(self.n_iter_,))
                LOGGER.info("iter: {},  is_converged: {}".format(self.n_iter_, self.is_converged))

                if self.is_converged or self.n_iter_ == self.max_iter:
                    break
                iter_loss = None
                batch_model_weights = self.model_weights

            batch_index = 0
            for batch_data in batch_data_generator:
                n = batch_data.count()
                LOGGER.debug("iter: {}, before compute gradient, data count: {}".format(self.n_iter_,
                                                                                        batch_data.count()))
                gradient, loss = self.gradient_loss_operator. \
                    federated_compute_gradient_and_loss(
                    batch_data,
                    self.cipher_operator,
                    self.encrypted_calculator,
                    batch_model_weights,
                    self.optimizer,
                    self.loss_method,
                    self.n_iter_,
                    batch_index)
                degree += n
                batch_model_weights = self.optimizer.update_model(batch_model_weights, gradient)
                batch_index += 1

                LOGGER.debug("lr_weight, iters: {}, update_model: {}".format(self.n_iter_, self.model_weights.unboxed))

                if iter_loss is None:
                    iter_loss = loss
                else:
                    iter_loss += loss

            # if converge
            if iter_loss is not None:
                iter_loss /= self.batch_generator.batch_nums
                if not self.in_one_vs_rest:
                    print("callback_loss did not realize")
                    self.callback_loss(self.n_iter_, iter_loss)

            if self.validation_strategy:
                LOGGER.debug('LR promoter running validation')
                self.validation_strategy.validate(self, self.n_iter_)
                if self.validation_strategy.need_stop():
                    LOGGER.debug('early stopping triggered')
                    break
            self.n_iter_ += 1

            self.tracker.save_training_best_model(self.export_model())
            self.tracker.add_task_progress(1)
        # if self.validation_strategy and self.validation_strategy.has_saved_best_model():
        #     self.load_model(self.validation_strategy.cur_best_model)

        provider_weights = self.gradient_loss_operator.get_provider_weight()
        for pw in provider_weights:
            provider_member_id = pw[1]
            provider_header = pw[2]
            provider_weight_dict = self.parse_param(pw[0], provider_header)
            self.tracker.save_provider_model_params(provider_weight_dict, provider_member_id)

        LOGGER.debug("Final lr weights: {}".format(self.model_weights.unboxed))

    def predict(self, data_instances):
        """
        Prediction of lr
        Parameters
        ----------
        data_instances: DSource of Instance, input data

        result_name: str,
            Showing the output type name

        Returns
        ----------
        DSource
            include input data label, predict probably, label
        """
        LOGGER.info("Start predict is a one_vs_rest task: {}".format(self.need_one_vs_rest))
        if self.need_one_vs_rest:
            predict_result = self.one_vs_rest_obj.predict(data_instances)
            return predict_result

        pred_prob = self.compute_wx(data_instances, self.model_weights.coef_, self.model_weights.intercept_)
        provider_probs = self.transfer_variable.provider_prob.get(idx=-1)

        LOGGER.info("Get probability from Provider")

        # promoter probability
        for provider_prob in provider_probs:
            pred_prob = pred_prob.join(provider_prob, lambda g, h: g + h)
        pred_prob = pred_prob.mapValues(lambda p: activation.sigmoid(p))
        threshold = self.model_param.predict_param.threshold
        pred_label = pred_prob.mapValues(lambda x: 1 if x > threshold else 0)

        predict_result = data_instances.mapValues(lambda x: x.label)
        predict_result = predict_result.join(pred_prob, lambda x, y: (x, y))
        predict_result = predict_result.join(pred_label, lambda x, y: [x[0], y, x[1],
                                                                       {"0": (1 - x[1]), "1": x[1]}])

        return predict_result

    def federated_compute_weights(self, tmp_model_weights, degree):

        provider_model_weights_rs = self.gradient_loss_operator.get_provider_model_weights_r(suffix=(self.n_iter_,))
        size_list = []
        weights = []
        for provider_model_weights in provider_model_weights_rs:
            weights = np.append(weights, provider_model_weights)
            size_list.append(len(provider_model_weights))
        size_list.append(len(tmp_model_weights.unboxed))
        weights = np.append(weights, tmp_model_weights.unboxed)
        agg_model_weights = LRModelWeights(weights, self.fit_intercept)

        weight = self.aggregator.aggregate_then_get(agg_model_weights, degree=degree,
                                                    suffix=self.n_iter_)
        LOGGER.debug("Before aggregate: {}, degree: {} after aggregated: {}".format(
            agg_model_weights.unboxed / degree,
            degree,
            weight.unboxed))

        promoter_weight = self.gradient_loss_operator.separate_weights_and_sync(weight, size_list,
                                                                                suffix=(self.n_iter_,))
        self.model_weights = LRModelWeights(promoter_weight, self.fit_intercept)
