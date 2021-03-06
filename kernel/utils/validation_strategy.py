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



from common.python.utils import log_utils
from kernel.components.evaluation.evaluation import Evaluation
from kernel.components.evaluation.evaluation import PerformanceRecorder
from kernel.components.evaluation.param import EvaluateParam
from kernel.transfer.variables.transfer_class.validation_strategy_transfer_variable import \
    ValidationStrategyVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class ValidationStrategy(object):
    """
    This module is used for evaluating the performance of model during training process.
        it will be called only in fit process of models.

    Attributes
    ----------

    validation_freqs: None or positive integer or container object in python. Do validation in training process or Not.
                      if equals None, will not do validation in train process;
                      if equals positive integer, will validate data every validation_freqs epochs passes;
                      if container object in python, will validate data if epochs belong to this container.
                        e.g. validation_freqs = [10, 15], will validate data when epoch equals to 10 and 15.
                      Default: None

    train_data: None or DSource,
                if train_data not equal to None, and judge need to validate data according to validation_freqs,
                training data will be used for evaluating

    validate_data: None or DSource,
                if validate_data not equal to None, and judge need to validate data according to validation_freqs,
                validate data will be used for evaluating
    """

    def __init__(self, role=None, mode=None, validation_freqs=None, early_stopping_rounds=None,
                 use_first_metric_only=False):

        self.validation_freqs = validation_freqs
        self.role = role
        self.mode = mode
        self.flowid = ''
        self.train_data = None
        self.validate_data = None
        self.sync_status = False
        self.early_stopping_rounds = early_stopping_rounds
        self.use_first_metric_only = use_first_metric_only
        self.first_metric = None
        self.best_iteration = -1

        if early_stopping_rounds is not None:
            if early_stopping_rounds <= 0:
                raise ValueError('early stopping error should be larger than 0')
            if self.mode == consts.HORZ:
                raise ValueError('early stopping is not supported for horz algorithms')

            self.sync_status = True
            LOGGER.debug("early stopping round is {}".format(self.early_stopping_rounds))

        self.cur_best_model = None
        self.performance_recorder = PerformanceRecorder()
        self.transfer_inst = ValidationStrategyVariable()

        LOGGER.debug("end to init validation_strategy, freqs is {}".format(self.validation_freqs))

    def set_train_data(self, train_data):
        self.train_data = train_data

    def set_validate_data(self, validate_data):
        self.validate_data = validate_data
        if self.early_stopping_rounds and self.validate_data is None:
            raise ValueError('validate data is needed when early stopping is enabled')

    def set_flowid(self, flowid):
        self.flowid = flowid

    def need_run_validation(self, epoch):
        LOGGER.debug("validation_freqs is {}".format(self.validation_freqs))
        if not self.validation_freqs:
            return False

        if isinstance(self.validation_freqs, int):
            return (epoch + 1) % self.validation_freqs == 0

        return epoch in self.validation_freqs

    @staticmethod
    def generate_flowid(prefix, epoch, keywords="iteration", data_type="train"):
        return "_".join([prefix, keywords, str(epoch), data_type])

    @staticmethod
    def make_data_set_name(need_cv, model_flowid, epoch):
        data_iteration_name = "_".join(["iteration", str(epoch)])
        if not need_cv:
            return data_iteration_name

        cv_fold = "_".join(["fold", model_flowid.split(".", -1)[-1]])
        return ".".join([cv_fold, data_iteration_name])

    def is_best_performance_updated(self, use_first_metric_only=False):
        if len(self.performance_recorder.no_improvement_round.items()) == 0:
            return False
        for metric, no_improve_val in self.performance_recorder.no_improvement_round.items():
            if no_improve_val != 0:
                return False
            if use_first_metric_only:
                break
        return True

    def check_early_stopping(self, ):
        """
        check if satisfy early_stopping_round
        Returns bool
        """
        LOGGER.info('checking early stopping')

        no_improvement_dict = self.performance_recorder.no_improvement_round
        for metric in no_improvement_dict:
            if no_improvement_dict[metric] >= self.early_stopping_rounds:
                return True
        return False

    def sync_performance_recorder(self, epoch):
        """
        sync synchronize self.performance_recorder
        """
        if self.mode == consts.VERT and self.role == consts.PROMOTER:
            self.transfer_inst.validation_status.remote(self.performance_recorder, idx=-1, suffix=(epoch,))

        elif self.mode == consts.VERT:
            self.performance_recorder = self.transfer_inst.validation_status.get(idx=-1, suffix=(epoch,))[0]

        else:
            return

    def need_stop(self):
        return False if not self.early_stopping_rounds else self.check_early_stopping()

    def has_saved_best_model(self):
        return (self.early_stopping_rounds is not None) and (self.cur_best_model is not None)

    def export_best_model(self):
        if self.has_saved_best_model():
            return self.cur_best_model
        else:
            return None

    def evaluate(self, predicts, model, epoch):

        evaluate_param: EvaluateParam = model.get_metrics_param()
        evaluate_param.check_single_value_default_metric()

        eval_obj = Evaluation()
        eval_type = evaluate_param.eval_type

        metric_list = evaluate_param.metrics
        if self.early_stopping_rounds and self.use_first_metric_only and len(metric_list) != 0:

            single_metric_list = None
            if eval_type == consts.BINARY:
                single_metric_list = consts.BINARY_SINGLE_VALUE_METRIC
            elif eval_type == consts.REGRESSION:
                single_metric_list = consts.REGRESSION_SINGLE_VALUE_METRICS
            elif eval_type == consts.MULTY:
                single_metric_list = consts.MULTI_SINGLE_VALUE_METRIC

            for metric in metric_list:
                if metric in single_metric_list:
                    self.first_metric = metric
                    LOGGER.debug('use {} as first metric'.format(self.first_metric))
                    break

        eval_obj._init_model(evaluate_param)
        eval_obj.set_tracker(model.tracker)
        data_set_name = self.make_data_set_name(model.need_cv, model.flowid, epoch)
        eval_data = {data_set_name: predicts}
        eval_result_dict = eval_obj.fit(eval_data, return_result=True)
        eval_obj.output_data()
        LOGGER.debug("end to eval")

        return eval_result_dict

    def evaluate_data(self, model, epoch, data, data_type):
        if not data:
            return

        LOGGER.debug("start to evaluate data {}".format(data_type))
        model_flowid = model.flowid
        # model_flowid = ".".join(model.flowid.split(".", -1)[1:])
        flowid = self.generate_flowid(model_flowid, epoch, "iteration", data_type)
        model.set_flowid(flowid)
        predicts = model.predict(data)
        model.set_flowid(model_flowid)

        if self.mode == consts.HORZ and self.role == consts.ARBITER:
            pass
        elif self.mode == consts.VERT and self.role == consts.PROVIDER:
            pass
        else:
            predicts = predicts.mapValues(lambda value: value + [data_type])

        return predicts

    def validate(self, model, epoch):

        LOGGER.debug(
            "begin to check validate status, need_run_validation is {}".format(self.need_run_validation(epoch)))
        if not self.need_run_validation(epoch):
            return

        if self.mode == consts.HORZ and self.role == consts.ARBITER:
            return

        train_predicts = self.evaluate_data(model, epoch, self.train_data, "train")
        validate_predicts = self.evaluate_data(model, epoch, self.validate_data, "validate")

        if train_predicts is not None or validate_predicts is not None:

            predicts = train_predicts
            if validate_predicts:
                predicts = predicts.union(validate_predicts)

            eval_result_dict = self.evaluate(predicts, model, epoch)
            LOGGER.debug('showing eval_result_dict here')
            LOGGER.debug(eval_result_dict)

            if self.early_stopping_rounds:

                if len(eval_result_dict) == 0:
                    raise ValueError(
                        "eval_result len is 0, no single value metric detected for early stopping checking")

                if self.use_first_metric_only:
                    if self.first_metric:
                        eval_result_dict = {self.first_metric: eval_result_dict[self.first_metric]}
                    else:
                        LOGGER.warning('use first metric only but no single metric found in metric list')

                self.performance_recorder.update(eval_result_dict)

        if self.sync_status:
            self.sync_performance_recorder(epoch)
            LOGGER.debug('showing early stopping status, {} shows cur best performances'.format(self.role))
            LOGGER.debug(self.performance_recorder.cur_best_performance)
            LOGGER.debug('showing early stopping status, {} shows early stopping no improve rounds'.format(self.role))
            LOGGER.debug(self.performance_recorder.no_improvement_round)

        if self.early_stopping_rounds and self.is_best_performance_updated() and self.mode == consts.VERT:
            self.best_iteration = epoch
            best_model = model.export_model()
            self.cur_best_model = {'model': {'best_model': best_model}} if best_model is not None else None
            LOGGER.debug('cur best model saved, best iter is {}'.format(self.best_iteration))

            # model.tracker.save_training_best_model(best_model,self.best_iteration)
