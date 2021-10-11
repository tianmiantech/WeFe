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



from common.python.utils import log_utils
from kernel.components.lr.lr_model_weight import LRModelWeights
from kernel.components.lr.vertlr.sync import iter_sync, paillier_keygen_sync, batch_info_sync, converg_sync
from kernel.components.lr.vertlr.sync import vert_lr_gradient_and_loss
from kernel.components.lr.vertlr.vert_lr_base import VertLRBaseModel
from kernel.security import EncryptModeCalculator
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class VertLRProvider(VertLRBaseModel):
    def __init__(self):
        super(VertLRProvider, self).__init__()
        self.batch_num = None
        self.batch_index_list = []
        self.role = consts.PROVIDER

        self.cipher = paillier_keygen_sync.Provider()
        self.batch_generator = batch_info_sync.Provider()
        self.gradient_loss_operator = vert_lr_gradient_and_loss.Provider()
        self.converge_procedure = converg_sync.Provider()
        self.iter_transfer = iter_sync.Provider()
        self.encrypted_calculator = None
        self.model_save_to_storage = True

    def fit(self, data_instances, validate_data=None):
        """
        Train lr model of role provider
        Parameters
        ----------
        data_instances: DSource of Instance, input data
        """
        LOGGER.info("Enter vert_lr provider fit")

        self.header = self.get_header(data_instances)

        classes = self.one_vs_rest_obj.get_data_classes(data_instances)

        if len(classes) > 2:
            self.need_one_vs_rest = True
            self.in_one_vs_rest = True
            self.one_vs_rest_fit(train_data=data_instances, validate_data=validate_data)
        else:
            self.need_one_vs_rest = False
            self.fit_binary(data_instances, validate_data)

    def fit_binary(self, data_instances, validate_data):
        LOGGER.info("Enter vert_lr provider fit_binary")
        self._abnormal_detection(data_instances)

        self.validation_strategy = self.init_validation_strategy(data_instances, validate_data)
        LOGGER.debug(f"MODEL_STEP Start fin_binary, data count: {data_instances.count()}")

        self.header = self.get_header(data_instances)
        self.cipher_operator = self.cipher.gen_paillier_cipher()

        self.batch_generator.initialize_batch_generator(data_instances)
        self.gradient_loss_operator.set_total_batch_nums(self.batch_generator.batch_nums)

        self.encrypted_calculator = [EncryptModeCalculator(self.cipher_operator,
                                                           self.encrypted_mode_calculator_param.mode,
                                                           self.encrypted_mode_calculator_param.re_encrypted_rate) for _
                                     in range(self.batch_generator.batch_nums)]

        LOGGER.info("Start initialize model.")
        model_shape = self.get_features_shape(data_instances)
        if self.init_param_obj.fit_intercept:
            self.init_param_obj.fit_intercept = False
        w = self.initializer.init_model(model_shape, init_params=self.init_param_obj)
        LOGGER.debug("model_shape: {}, w shape: {}, w: {}".format(model_shape, w.shape, w))
        self.model_weights = LRModelWeights(w, fit_intercept=self.init_param_obj.fit_intercept)
        cur_best_model = self.tracker.get_training_best_model()
        if cur_best_model is not None:
            model_param = cur_best_model["Model_Param"]
            self.load_single_model(model_param)
            self.n_iter_ = self.iter_transfer.get_cur_iter()
            self.tracker.set_task_progress(self.n_iter_)
        while self.n_iter_ < self.max_iter:
            LOGGER.info("iter:" + str(self.n_iter_))
            batch_data_generator = self.batch_generator.generate_batch_data()
            batch_index = 0
            self.optimizer.set_iters(self.n_iter_)
            for batch_data in batch_data_generator:
                optim_provider_gradient, fore_gradient = self.gradient_loss_operator \
                    .federated_compute_gradient_and_loss(
                    batch_data,
                    self.cipher_operator,
                    self.encrypted_calculator,
                    self.model_weights,
                    self.optimizer,
                    self.n_iter_,
                    batch_index)
                LOGGER.debug('optim_provider_gradient: {}'.format(optim_provider_gradient))

                # update the weight
                self.model_weights = self.optimizer.update_model(self.model_weights, optim_provider_gradient)
                batch_index += 1

            self.is_converged = self.converge_procedure.get_converge_info(suffix=(self.n_iter_,))
            LOGGER.info("Get is_converged flag from promoter:{}".format(self.is_converged))

            if self.validation_strategy:
                LOGGER.debug('LR provider running validation')
                self.validation_strategy.validate(self, self.n_iter_)
                if self.validation_strategy.need_stop():
                    LOGGER.debug('early stopping triggered')
                    break
            self.n_iter_ += 1
            LOGGER.info("iter: {}, is_converged: {}".format(self.n_iter_, self.is_converged))
            if self.is_converged:
                break

            self.tracker.save_training_best_model(self.export_model())
            self.tracker.add_task_progress(1)
        if self.validation_strategy and self.validation_strategy.has_saved_best_model():
            self.load_model(self.validation_strategy.cur_best_model)

        self.gradient_loss_operator.remote_provider_weight((self.model_weights, self.member_id, self.header))

        LOGGER.debug("Final lr weights: {}".format(self.model_weights.unboxed))

    def predict(self, data_instances):
        self.transfer_variable.provider_prob.disable_auto_clean()
        LOGGER.info("Start predict ...")
        if self.need_one_vs_rest:
            self.one_vs_rest_obj.predict(data_instances)
            return

        # data_features = self.transform(data_instances)

        prob_provider = self.compute_wx(data_instances, self.model_weights.coef_, self.model_weights.intercept_)
        self.transfer_variable.provider_prob.remote(prob_provider, role=consts.PROMOTER, idx=0)
        LOGGER.info("Remote probability to Promoter")
