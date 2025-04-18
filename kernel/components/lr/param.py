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

import copy

from kernel.base.params.base_param import BaseParam
from kernel.base.params.cross_validation_param import CrossValidationParam
from kernel.base.params.grid_search_param import GridSearchParam
from kernel.base.params.encrypt_param import EncryptParam
from kernel.base.params.encrypted_mode_calculation_param import EncryptedModeCalculatorParam
from kernel.base.params.init_model_param import InitParam
from kernel.base.params.predict_param import PredictParam
from kernel.base.params.sqn_param import StochasticQuasiNewtonParam
from kernel.base.params.stepwise_param import StepwiseParam
from kernel.utils import consts


class LogisticParam(BaseParam):
    """
    Parameters used for Logistic Regression both for Horz mode or Vert mode.

    Parameters
    ----------
    penalty : str, 'L1', 'L2' or None. default: 'L2'
        Penalty method used in LR. Please note that, when using encrypted version in HorzLR,
        'L1' is not supported.

    tol : float, default: 1e-5
        The tolerance of convergence

    alpha : float, default: 1.0
        Regularization strength coefficient.

    optimizer : str, 'sgd', 'rmsprop', 'adam', 'nesterov_momentum_sgd', 'sqn' or 'adagrad', default: 'sgd'
        Optimize method, if 'sqn' has been set, sqn_param will take effect. Currently, 'sqn' support vert mode only.

    batch_size : int, default: -1
        Batch size when updating model. -1 means use all data in a batch. i.e. Not to use mini-batch strategy.

    learning_rate : float, default: 0.01
        Learning rate

    max_iter : int, default: 100
        The maximum iteration for training.

    early_stop : str, 'diff', 'weight_diff' or 'abs', default: 'diff'
        Method used to judge converge or not.
            a)	diff： Use difference of loss between two iterations to judge whether converge.
            b)  weight_diff: Use difference between weights of two consecutive iterations
            c)	abs: Use the absolute value of loss to judge whether converge. i.e. if loss < eps, it is converged.

    decay: int or float, default: 1
        Decay rate for learning rate. learning rate will follow the following decay schedule.
        lr = lr0/(1+decay*t) if decay_sqrt is False. If decay_sqrt is True, lr = lr0 / sqrt(1+decay*t)
        where t is the iter number.

    decay_sqrt: Bool, default: True
        lr = lr0/(1+decay*t) if decay_sqrt is False, otherwise, lr = lr0 / sqrt(1+decay*t)

    encrypt_param: EncryptParam object, default: default EncryptParam object

    predict_param: PredictParam object, default: default PredictParam object

    cv_param: CrossValidationParam object, default: default CrossValidationParam object

    grid_search_param: GridSearchParam object, default: default GridSearchParam object

    multi_class: str, 'ovr', default: 'ovr'
        If it is a multi_class task, indicate what strategy to use. Currently, support 'ovr' short for one_vs_rest only.

    validation_freqs: int, list, tuple, set, or None
        validation frequency during training.

    early_stopping_rounds: int, default: None
        Will stop training if one metric doesn’t improve in last early_stopping_round rounds

    metrics: list or None, default: None
        Indicate when executing evaluation during train process, which metrics will be used. If set as empty,
        default metrics for specific task type will be used. As for binary classification, default metrics are
        ['auc', 'ks']

    use_first_metric_only: bool, default: False
        Indicate whether use the first metric only for early stopping judgement.

    reveal_strategy: str, "respectively", "encrypted_reveal_in_host", default: "respectively"
        "respectively": Means promoter and provider can reveal their own part of weights only.
        "encrypted_reveal_in_host": Means provider can be revealed his weights in encrypted mode, and promoter can be revealed in normal mode.

    reveal_every_iter: bool, default: True
        Whether reconstruct model weights every iteration. If so, Regularization is available.
        The performance will be better as well since the algorithm process is simplified.


    """

    def __init__(self, penalty='L2',
                 tol=1e-5, alpha=1.0, optimizer='sgd',
                 batch_size=-1, learning_rate=0.01, init_param=InitParam(),
                 max_iter=100, early_stop='diff', encrypt_param=EncryptParam(),
                 predict_param=PredictParam(), cv_param=CrossValidationParam(),
                 grid_search_param=GridSearchParam(), decay=1, decay_sqrt=True,
                 multi_class='ovr', validation_freqs=None, early_stopping_rounds=None,
                 stepwise_param=StepwiseParam(),
                 metrics=None,
                 use_first_metric_only=False,
                 use_mix_rand=False,
                 reveal_strategy="respectively",
                 reveal_every_iter=True,
                 encrypted_mode_calculator_param=EncryptedModeCalculatorParam()
                 ):
        super(LogisticParam, self).__init__()
        self.penalty = penalty
        self.tol = tol
        self.alpha = alpha
        self.optimizer = optimizer
        self.batch_size = batch_size
        self.learning_rate = learning_rate
        self.init_param = copy.deepcopy(init_param)
        self.max_iter = max_iter
        self.early_stop = early_stop
        self.encrypt_param = encrypt_param
        self.predict_param = copy.deepcopy(predict_param)
        self.cv_param = copy.deepcopy(cv_param)
        self.grid_search_param = copy.deepcopy(grid_search_param)
        self.decay = decay
        self.decay_sqrt = decay_sqrt
        self.multi_class = multi_class
        self.validation_freqs = validation_freqs
        self.stepwise_param = copy.deepcopy(stepwise_param)
        self.early_stopping_rounds = early_stopping_rounds
        self.metrics = metrics or []
        self.use_first_metric_only = use_first_metric_only
        self.model_save_to_storage = True

        self.use_mix_rand = use_mix_rand
        self.reveal_strategy = reveal_strategy
        self.reveal_every_iter = reveal_every_iter
        self.encrypted_mode_calculator_param = copy.deepcopy(encrypted_mode_calculator_param)

    def check(self):
        descr = "logistic_param's"

        if self.penalty is None:
            pass
        elif type(self.penalty).__name__ != "str":
            raise ValueError(
                "logistic_param's penalty {} not supported, should be str type".format(self.penalty))
        else:
            self.penalty = self.penalty.upper()
            if self.penalty not in [consts.L1_PENALTY, consts.L2_PENALTY, 'NONE']:
                raise ValueError(
                    "logistic_param's penalty not supported, penalty should be 'L1', 'L2' or 'none'")

        if not isinstance(self.tol, (int, float)):
            raise ValueError(
                "logistic_param's tol {} not supported, should be float type".format(self.tol))

        if type(self.alpha).__name__ not in ["float", 'int']:
            raise ValueError(
                "logistic_param's alpha {} not supported, should be float or int type".format(self.alpha))

        if type(self.optimizer).__name__ != "str":
            raise ValueError(
                "logistic_param's optimizer {} not supported, should be str type".format(self.optimizer))
        else:
            self.optimizer = self.optimizer.lower()
            if self.optimizer not in ['sgd', 'rmsprop', 'adam', 'adagrad', 'nesterov_momentum_sgd', 'sqn']:
                raise ValueError(
                    "logistic_param's optimizer not supported, optimizer should be"
                    " 'sgd', 'rmsprop', 'adam', 'nesterov_momentum_sgd', 'sqn' or 'adagrad'")

        if self.batch_size != -1:
            if type(self.batch_size).__name__ not in ["int"] \
                    or self.batch_size < consts.MIN_BATCH_SIZE:
                raise ValueError(descr + " {} not supported, should be larger than {} or "
                                         "-1 represent for all data".format(self.batch_size, consts.MIN_BATCH_SIZE))

        if not isinstance(self.learning_rate, (float, int)):
            raise ValueError(
                "logistic_param's learning_rate {} not supported, should be float or int type".format(
                    self.learning_rate))

        self.init_param.check()

        if type(self.max_iter).__name__ != "int":
            raise ValueError(
                "logistic_param's max_iter {} not supported, should be int type".format(self.max_iter))
        elif self.max_iter <= 0:
            raise ValueError(
                "logistic_param's max_iter must be greater or equal to 1")

        if type(self.early_stop).__name__ != "str":
            raise ValueError(
                "logistic_param's early_stop {} not supported, should be str type".format(
                    self.early_stop))
        else:
            self.early_stop = self.early_stop.lower()
            if self.early_stop not in ['diff', 'abs', 'weight_diff']:
                raise ValueError(
                    "logistic_param's early_stop not supported, converge_func should be"
                    " 'diff', 'weight_diff' or 'abs'")

        self.encrypt_param.check()
        self.predict_param.check()
        if self.encrypt_param.method not in [consts.PAILLIER, None]:
            raise ValueError(
                "logistic_param's encrypted method support 'Paillier' or None only")

        if type(self.decay).__name__ not in ["int", 'float']:
            raise ValueError(
                "logistic_param's decay {} not supported, should be 'int' or 'float'".format(
                    self.decay))

        if type(self.decay_sqrt).__name__ not in ['bool']:
            raise ValueError(
                "logistic_param's decay_sqrt {} not supported, should be 'bool'".format(
                    self.decay_sqrt))
        self.stepwise_param.check()

        if self.early_stopping_rounds is None:
            pass
        elif isinstance(self.early_stopping_rounds, int):
            if self.early_stopping_rounds < 1:
                raise ValueError("early stopping rounds should be larger than 0 when it's integer")
            if self.validation_freqs is None:
                raise ValueError("validation freqs must be set when early stopping is enabled")

        if self.metrics is not None and not isinstance(self.metrics, list):
            raise ValueError("metrics should be a list")

        if not isinstance(self.use_first_metric_only, bool):
            raise ValueError("use_first_metric_only should be a boolean")

        self.check_boolean(self.reveal_every_iter, descr)
        self.cv_param.check()

        return True


class HorzLogisticParam(LogisticParam):
    """
    Parameters
    ----------
    re_encrypt_batches : int, default: 2
        Required when using encrypted version HorzLR. Since multiple batch updating coefficient may cause
        overflow error. The model need to be re-encrypt for every several batches. Please be careful when setting
        this parameter. Too large batches may cause training failure.

    aggregate_iters : int, default: 1
        Indicate how many iterations are aggregated once.

    """

    def __init__(self, penalty='L2',
                 tol=1e-5, alpha=1.0, optimizer='sgd',
                 batch_size=-1, learning_rate=0.01, init_param=InitParam(),
                 max_iter=100, early_stop='diff',
                 encrypt_param=EncryptParam(), re_encrypt_batches=2,
                 predict_param=PredictParam(), cv_param=CrossValidationParam(),
                 grid_search_param=GridSearchParam(), decay=1, decay_sqrt=True,
                 aggregate_iters=1, multi_class='ovr', validation_freqs=None,
                 early_stopping_rounds=None,
                 metrics=['auc', 'ks'],
                 use_first_metric_only=False
                 ):
        super(HorzLogisticParam, self).__init__(penalty=penalty, tol=tol, alpha=alpha, optimizer=optimizer,
                                                batch_size=batch_size,
                                                learning_rate=learning_rate,
                                                init_param=init_param, max_iter=max_iter, early_stop=early_stop,
                                                encrypt_param=encrypt_param, predict_param=predict_param,
                                                cv_param=cv_param, grid_search_param=grid_search_param, multi_class=multi_class,
                                                validation_freqs=validation_freqs,
                                                decay=decay, decay_sqrt=decay_sqrt,
                                                early_stopping_rounds=early_stopping_rounds,
                                                metrics=metrics, use_first_metric_only=use_first_metric_only)
        self.re_encrypt_batches = re_encrypt_batches
        self.aggregate_iters = aggregate_iters

    def check(self):
        super().check()
        if type(self.re_encrypt_batches).__name__ != "int":
            raise ValueError(
                "logistic_param's re_encrypt_batches {} not supported, should be int type".format(
                    self.re_encrypt_batches))
        elif self.re_encrypt_batches < 0:
            raise ValueError(
                "logistic_param's re_encrypt_batches must be greater or equal to 0")

        if not isinstance(self.aggregate_iters, int):
            raise ValueError(
                "logistic_param's aggregate_iters {} not supported, should be int type".format(
                    self.aggregate_iters))

        # if self.encrypt_param.method == consts.PAILLIER:
        #     if self.optimizer != 'sgd':
        #         raise ValueError("Paillier encryption mode supports 'sgd' optimizer method only.")
        #
        #     if self.penalty == consts.L1_PENALTY:
        #         raise ValueError("Paillier encryption mode supports 'L2' penalty or None only.")

        if self.optimizer == 'sqn':
            raise ValueError("'sqn' optimizer is supported for vert mode only.")

        return True


class VertLogisticParam(LogisticParam):
    def __init__(self, penalty='L2',
                 tol=1e-5, alpha=1.0, optimizer='sgd',
                 batch_size=-1, learning_rate=0.01, init_param=InitParam(),
                 max_iter=100, early_stop='diff',
                 encrypted_mode_calculator_param=EncryptedModeCalculatorParam(),
                 predict_param=PredictParam(), cv_param=CrossValidationParam(),
                 grid_search_param=GridSearchParam(),
                 decay=1, decay_sqrt=True, sqn_param=StochasticQuasiNewtonParam(),
                 multi_class='ovr', validation_freqs=None, early_stopping_rounds=None,
                 metrics=['auc', 'ks'],
                 use_first_metric_only=False,
                 lr_method='lr'
                 ):
        super(VertLogisticParam, self).__init__(penalty=penalty, tol=tol, alpha=alpha, optimizer=optimizer,
                                                batch_size=batch_size,
                                                learning_rate=learning_rate,
                                                init_param=init_param, max_iter=max_iter, early_stop=early_stop,
                                                predict_param=predict_param, cv_param=cv_param,
                                                grid_search_param=grid_search_param, decay=decay,
                                                decay_sqrt=decay_sqrt, multi_class=multi_class,
                                                validation_freqs=validation_freqs,
                                                early_stopping_rounds=early_stopping_rounds,
                                                metrics=metrics,
                                                use_first_metric_only=use_first_metric_only)
        self.encrypted_mode_calculator_param = copy.deepcopy(encrypted_mode_calculator_param)
        self.sqn_param = copy.deepcopy(sqn_param)
        self.lr_method = lr_method

    def check(self):
        super().check()
        self.encrypted_mode_calculator_param.check()
        self.sqn_param.check()
        return True


class MixLogisticParam(LogisticParam):
    def __init__(self, penalty='L2',
                 tol=1e-5, alpha=1.0, optimizer='sgd',
                 batch_size=-1, learning_rate=0.01, init_param=InitParam(),
                 max_iter=100, early_stop='diff',
                 encrypted_mode_calculator_param=EncryptedModeCalculatorParam(),
                 predict_param=PredictParam(), cv_param=CrossValidationParam(),
                 grid_search_param=GridSearchParam(),
                 decay=1, decay_sqrt=True, sqn_param=StochasticQuasiNewtonParam(),
                 multi_class='ovr', validation_freqs=None, early_stopping_rounds=None,
                 metrics=['auc', 'ks'],
                 use_first_metric_only=False,
                 aggregate_iters=1, random_cipher_seed=None,
                 ):
        super(MixLogisticParam, self).__init__(penalty=penalty, tol=tol, alpha=alpha, optimizer=optimizer,
                                               batch_size=batch_size,
                                               learning_rate=learning_rate,
                                               init_param=init_param, max_iter=max_iter, early_stop=early_stop,
                                               predict_param=predict_param, cv_param=cv_param,
                                               grid_search_param=grid_search_param, decay=decay,
                                               decay_sqrt=decay_sqrt, multi_class=multi_class,
                                               validation_freqs=validation_freqs,
                                               early_stopping_rounds=early_stopping_rounds,
                                               metrics=metrics,
                                               use_first_metric_only=use_first_metric_only)
        self.encrypted_mode_calculator_param = copy.deepcopy(encrypted_mode_calculator_param)
        self.sqn_param = copy.deepcopy(sqn_param)
        self.aggregate_iters = aggregate_iters
        self.random_cipher_seed = random_cipher_seed

    def check(self):
        super().check()
        self.encrypted_mode_calculator_param.check()
        # self.sqn_param.check()
        if not isinstance(self.aggregate_iters, int):
            raise ValueError(
                "logistic_param's aggregate_iters {} not supported, should be int type".format(
                    self.aggregate_iters))

        if self.optimizer == 'sqn':
            raise ValueError("'sqn' optimizer is supported for vert mode only.")

        return True
