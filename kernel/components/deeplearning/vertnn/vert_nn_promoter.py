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



import numpy as np

from common.python import session
from common.python.utils import log_utils
from kernel.components.deeplearning.vertnn.backend.model_builder import model_builder
from kernel.components.deeplearning.vertnn.vert_nn_base import VertNNBase
from kernel.components.evaluation.param import EvaluateParam
from kernel.optimizer.convergence import converge_func_factory
from kernel.protobuf.generated.vert_nn_model_meta_pb2 import VertNNMeta
from kernel.protobuf.generated.vert_nn_model_param_pb2 import VertNNParam
from kernel.transfer.framework.vert.procedure import batch_generator
from kernel.utils import consts
from kernel.utils.io_check import assert_io_num_rows_equal

LOGGER = log_utils.get_logger()

MODELMETA = "VertNNPromoterMeta"
MODELPARAM = "VertNNPromoterParam"


class VertNNPromoter(VertNNBase):
    def __init__(self):
        super(VertNNPromoter, self).__init__()
        self.task_type = None
        self.converge_func = None

        self.batch_generator = batch_generator.Promoter()
        self.data_keys = []

        self.model_builder = None
        self.label_dict = {}

        self.model = None
        self.role = consts.PROMOTER
        self.history_loss = []
        self.iter_epoch = 0
        self.num_label = 2

        self.input_shape = None
        self.validation_strategy = None
        self._summary_buf = {"history_loss": [],
                             "is_converged": False,
                             "best_iteration": -1}

    def _init_model(self, vert_nn_param):
        super(VertNNPromoter, self)._init_model(vert_nn_param)

        self.task_type = vert_nn_param.task_type
        self.converge_func = converge_func_factory(self.early_stop, self.tol)

    def _build_model(self):
        self.model = model_builder("promoter", self.vert_nn_param)
        self.model.set_transfer_variable(self.transfer_variable)

    def callback_loss(self, iter_num, loss):
        metric_meta = {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS', 'pair_type': ''}
        self.callback_metric(metric_name='loss',
                             metric_namespace='train',
                             metric_meta=metric_meta,
                             metric_data=(iter_num, loss))

    def fit(self, data_inst, validate_data=None):
        self.validation_strategy = self.init_validation_strategy(data_inst, validate_data)
        self._build_model()
        self.prepare_batch_data(self.batch_generator, data_inst)
        if not self.input_shape:
            self.model.set_empty()

        # self._set_loss_callback_info()
        cur_epoch = 0
        while cur_epoch < self.epochs:
            LOGGER.debug("cur epoch is {}".format(cur_epoch))
            epoch_loss = 0

            for batch_idx in range(len(self.data_x)):
                batch_loss = self.model.train(self.data_x[batch_idx], self.data_y[batch_idx], cur_epoch, batch_idx)

                epoch_loss += batch_loss

            epoch_loss /= len(self.data_x)

            LOGGER.debug("epoch {}' loss is {}".format(cur_epoch, epoch_loss))

            self.callback_loss(cur_epoch, epoch_loss)

            self.history_loss.append(epoch_loss)

            if self.validation_strategy:
                self.validation_strategy.validate(self, cur_epoch)
                if self.validation_strategy.need_stop():
                    LOGGER.debug('early stopping triggered')
                    break

            if self.vert_nn_param.selector_param.method:
                # when use selective bp, loss converge will be disabled
                is_converge = False
            else:
                is_converge = self.converge_func.is_converge(epoch_loss)
            self._summary_buf["is_converged"] = is_converge
            self.transfer_variable.is_converge.remote(is_converge,
                                                      role=consts.PROVIDER,
                                                      idx=0,
                                                      suffix=(cur_epoch,))

            if is_converge:
                LOGGER.debug("Training process is converged in epoch {}".format(cur_epoch))
                break

            cur_epoch += 1

            self.tracker.add_task_progress(1, self.need_grid_search)

        if cur_epoch == self.epochs:
            LOGGER.debug("Training process reach max training epochs {} and not converged".format(self.epochs))

        if self.validation_strategy and self.validation_strategy.has_saved_best_model():
            self.load_model(self.validation_strategy.cur_best_model)

        # self.set_summary(self._get_model_summary())

    @assert_io_num_rows_equal
    def predict(self, data_inst):
        data_inst = self.align_data_header(data_inst, self._header)
        keys, test_x, test_y = self._load_data(data_inst)
        self.set_partition(data_inst)

        preds = self.model.predict(test_x)

        if self.task_type == "regression":
            preds = [float(pred[0]) for pred in preds]
            predict_tb = session.parallelize(zip(keys, preds), include_key=True, partition=data_inst._partitions)
            result = self.predict_score_to_output(data_inst, predict_tb)
        else:
            if self.num_label > 2:
                preds = [list(map(float, pred)) for pred in preds]
                predict_tb = session.parallelize(zip(keys, preds), include_key=True, partition=data_inst._partitions)
                result = self.predict_score_to_output(data_inst, predict_tb, classes=list(range(self.num_label)))

            else:
                preds = [float(pred[0]) for pred in preds]
                predict_tb = session.parallelize(zip(keys, preds), include_key=True, partition=data_inst._partitions)
                threshold = self.predict_param.threshold
                result = self.predict_score_to_output(data_inst, predict_tb, classes=[0, 1], threshold=threshold)

        return result

    def export_model(self):
        if self.model is None:
            return

        return {MODELMETA: self._get_model_meta(),
                MODELPARAM: self._get_model_param()}

    def load_model(self, model_dict):
        model_dict = list(model_dict["model"].values())[0]
        param = model_dict.get(MODELPARAM)
        meta = model_dict.get(MODELMETA)

        self._build_model()
        self._restore_model_meta(meta)
        self._restore_model_param(param)

    def _get_model_summary(self):
        self._summary_buf[
            "best_iteration"] = -1 if self.validation_strategy is None else self.validation_strategy.best_iteration
        self._summary_buf["history_loss"] = self.history_loss

        if self.validation_strategy:
            validation_summary = self.validation_strategy.summary()
            if validation_summary:
                self._summary_buf["validation_metrics"] = validation_summary

        return self._summary_buf

    def _get_model_meta(self):
        model_meta = VertNNMeta()
        model_meta.task_type = self.task_type

        model_meta.batch_size = self.batch_size
        model_meta.epochs = self.epochs
        model_meta.early_stop = self.early_stop
        model_meta.tol = self.tol
        # model_meta.interactive_layer_lr = self.vert_nn_param.interacitve_layer_lr

        model_meta.vert_nn_model_meta.CopyFrom(self.model.get_vert_nn_model_meta())

        return model_meta

    def _get_model_param(self):
        model_param = VertNNParam()
        model_param.iter_epoch = self.iter_epoch
        model_param.vert_nn_model_param.CopyFrom(self.model.get_vert_nn_model_param())
        model_param.num_label = self.num_label
        model_param.best_iteration = -1 if self.validation_strategy is None else self.validation_strategy.best_iteration
        model_param.header.extend(self._header)

        for loss in self.history_loss:
            model_param.history_loss.append(loss)

        return model_param

    def get_metrics_param(self):
        if self.task_type == consts.CLASSIFICATION:
            if self.num_label == 2:
                return EvaluateParam(eval_type="binary",
                                     pos_label=1, metrics=self.metrics)
            else:
                return EvaluateParam(eval_type="multi", metrics=self.metrics)
        else:
            return EvaluateParam(eval_type="regression", metrics=self.metrics)

    def prepare_batch_data(self, batch_generator, data_inst):
        self._header = data_inst.schema["header"]
        batch_generator.initialize_batch_generator(data_inst, self.batch_size)
        batch_data_generator = batch_generator.generate_batch_data()

        for batch_data in batch_data_generator:
            keys, batch_x, batch_y = self._load_data(batch_data)
            self.data_x.append(batch_x)
            self.data_y.append(batch_y)
            self.data_keys.append(keys)

        self._convert_label()
        self.set_partition(data_inst)

    def _load_data(self, data_inst):
        data = list(data_inst.collect())
        data_keys = [key for (key, val) in data]
        data_keys_map = dict(zip(sorted(data_keys), range(len(data_keys))))

        keys = [None for idx in range(len(data_keys))]
        batch_x = [None for idx in range(len(data_keys))]
        batch_y = [None for idx in range(len(data_keys))]

        for (key, inst) in data:
            idx = data_keys_map[key]
            keys[idx] = key
            batch_x[idx] = inst.features
            batch_y[idx] = inst.label

            if self.input_shape is None:
                try:
                    self.input_shape = inst.features.shape[0]
                except AttributeError:
                    self.input_shape = 0

        batch_x = np.asarray(batch_x)
        batch_y = np.asarray(batch_y)

        return keys, batch_x, batch_y

    def _convert_label(self):
        diff_label = np.unique(np.concatenate(self.data_y))
        self.label_dict = dict(zip(diff_label, range(diff_label.shape[0])))

        transform_y = []
        self.num_label = diff_label.shape[0]

        if self.task_type == "regression" or self.num_label <= 2:
            for batch_y in self.data_y:
                new_batch_y = np.zeros((batch_y.shape[0], 1))
                for idx in range(new_batch_y.shape[0]):
                    new_batch_y[idx] = batch_y[idx]

                transform_y.append(new_batch_y)

            self.data_y = transform_y
            return

        for batch_y in self.data_y:
            new_batch_y = np.zeros((batch_y.shape[0], self.num_label))
            for idx in range(new_batch_y.shape[0]):
                y = batch_y[idx]
                new_batch_y[idx][y] = 1

            transform_y.append(new_batch_y)

        self.data_y = transform_y

    def _restore_model_param(self, param):
        super(VertNNPromoter, self)._restore_model_param(param)
        self.num_label = param.num_label
