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

import json

from common.python import session
from common.python.utils import log_utils
from kernel.components.deeplearning.horznn import nn_model
from kernel.components.deeplearning.horznn._consts import _build_model_dict
from kernel.components.deeplearning.horznn.nn_model import restore_nn_model
from kernel.optimizer.convergence import converge_func_factory
from kernel.transfer.framework.horz.blocks import (
    secure_mean_aggregator,
    loss_scatter,
    has_converged,
)
from kernel.utils.horz_label_encoder import (HorzLabelEncoderArbiter, HorzLabelEncoderClient, )

LOGGER = log_utils.get_logger()


def _suffix(self):
    return (self.aggregate_iteration_num,)


def server_init_model(self, param):
    self.aggregate_iteration_num = 0
    self.aggregator = secure_mean_aggregator.Server(
        self.transfer_variable.secure_aggregator_trans_var
    )
    self.loss_scatter = loss_scatter.Server(
        self.transfer_variable.loss_scatter_trans_var
    )
    self.has_converged = has_converged.Server(
        self.transfer_variable.has_converged_trans_var
    )

    self._summary = dict(loss_history=[], is_converged=False)

    self.param = param
    self.enable_secure_aggregate = param.secure_aggregate
    self.max_aggregate_iteration_num = param.max_iter
    early_stop = self.model_param.early_stop
    self.converge_func = converge_func_factory(
        early_stop.converge_func, early_stop.eps
    ).is_converge
    self.loss_consumed = early_stop.converge_func != "weight_diff"


def server_fit(self, data_inst):
    label_mapping = HorzLabelEncoderArbiter().label_alignment()
    LOGGER.info(f"label mapping: {label_mapping}")
    while self.aggregate_iteration_num < self.max_aggregate_iteration_num:
        self.model = self.aggregator.weighted_mean_model(suffix=_suffix(self))
        self.aggregator.send_aggregated_model(model=self.model, suffix=_suffix(self))

        if server_is_converged(self):
            LOGGER.info(f"early stop at iter {self.aggregate_iteration_num}")
            break
        self.aggregate_iteration_num += 1

        self.tracker.add_task_progress(1, self.need_grid_search)
    else:
        LOGGER.warn(f"reach max iter: {self.aggregate_iteration_num}, not converged")
    self.set_summary(self._summary)


def server_is_converged(self):
    loss = self.loss_scatter.weighted_loss_mean(suffix=_suffix(self))
    LOGGER.info(f"loss at iter {self.aggregate_iteration_num}: {loss}")
    callback_loss(self, self.aggregate_iteration_num, loss)
    if self.loss_consumed:
        is_converged = self.converge_func(loss)
    else:
        is_converged = self.converge_func(self.model)
    self.has_converged.remote_converge_status(
        is_converge=is_converged, suffix=_suffix(self)
    )
    self._summary["is_converged"] = is_converged
    return is_converged


def callback_loss(self, iter_num, loss):
    metric_meta = {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS', 'pair_type': ''}
    self.callback_metric(metric_name='loss',
                         metric_namespace='train',
                         metric_meta=metric_meta,
                         metric_data=(iter_num, loss))
    self._summary["loss_history"].append(loss)


def client_set_params(self, param):
    self.nn_model = None
    self._summary = dict(loss_history=[], is_converged=False)
    self._header = []
    self._label_align_mapping = None

    self.param = param
    self.enable_secure_aggregate = param.secure_aggregate
    self.max_aggregate_iteration_num = param.max_iter
    self.batch_size = param.batch_size
    self.aggregate_every_n_epoch = param.aggregate_every_n_epoch
    self.nn_define = param.nn_define
    self.config_type = param.config_type
    self.optimizer = param.optimizer
    self.loss = param.loss
    self.metrics = param.metrics
    self.encode_label = param.encode_label

    self.data_converter = nn_model.get_data_converter(self.config_type)
    self.model_builder = nn_model.get_nn_builder(self.config_type)


def client_init_model(self, param):
    self.aggregate_iteration_num = 0
    self.aggregator = secure_mean_aggregator.Client(
        self.transfer_variable.secure_aggregator_trans_var
    )
    self.loss_scatter = loss_scatter.Client(
        self.transfer_variable.loss_scatter_trans_var
    )
    self.has_converged = has_converged.Client(
        self.transfer_variable.has_converged_trans_var
    )
    client_set_params(self, param)


def client_fit(self, data_inst):
    self._header = data_inst.schema["header"]
    client_align_labels(self, data_inst=data_inst)
    data = self.data_converter.convert(
        data_inst,
        batch_size=self.batch_size,
        encode_label=self.encode_label,
        label_mapping=self._label_align_mapping,
    )
    self.nn_model = self.model_builder(
        input_shape=data.get_shape()[0],
        nn_define=self.nn_define,
        optimizer=self.optimizer,
        loss=self.loss,
        metrics=self.metrics,
    )

    epoch_degree = float(len(data)) * self.aggregate_every_n_epoch

    while self.aggregate_iteration_num < self.max_aggregate_iteration_num:
        LOGGER.info(f"start {self.aggregate_iteration_num}_th aggregation")

        # train
        self.nn_model.train(data, aggregate_every_n_epoch=self.aggregate_every_n_epoch)

        # send model for aggregate, then set aggregated model to local
        self.aggregator.send_weighted_model(
            weighted_model=self.nn_model.get_model_weights(),
            weight=epoch_degree * self.aggregate_every_n_epoch,
            suffix=_suffix(self),
        )
        weights = self.aggregator.get_aggregated_model(suffix=_suffix(self))
        self.nn_model.set_model_weights(weights=weights)

        # calc loss and check convergence
        if client_is_converged(self, data, epoch_degree):
            LOGGER.info(f"early stop at iter {self.aggregate_iteration_num}")
            break

        LOGGER.info(
            f"role {self.role} finish {self.aggregate_iteration_num}_th aggregation"
        )
        self.aggregate_iteration_num += 1

        self.tracker.add_task_progress(1, self.need_grid_search)
    else:
        LOGGER.warn(f"reach max iter: {self.aggregate_iteration_num}, not converged")

    self.set_summary(self._summary)


def client_align_labels(self, data_inst):
    local_labels = data_inst.map(lambda k, v: [k, {v.label}]).reduce(lambda x, y: x | y)
    _, self._label_align_mapping = HorzLabelEncoderClient().label_alignment(
        local_labels
    )
    num_classes = len(self._label_align_mapping)

    if self.config_type == "pytorch":
        for layer in reversed(self.nn_define):
            if layer["layer"] == "Linear":
                output_dim = layer["config"][1]
                if output_dim == 1 and num_classes == 2:
                    return
                layer["config"][1] = num_classes
                return

    if self.config_type == "nn":
        for layer in reversed(self.nn_define):
            if layer["layer"] == "Dense":
                output_dim = layer.get("units", None)
                if output_dim == 1 and num_classes == 2:
                    return
                layer["units"] = num_classes
                return

    if self.config_type == "keras":
        layers = self.nn_define["config"]["layers"]
        for layer in reversed(layers):
            if layer["class_name"] == "Dense":
                output_dim = layer["config"].get("units", None)
                if output_dim == 1 and num_classes == 2:
                    return
                layer["config"]["units"] = num_classes
                return


def client_is_converged(self, data, epoch_degree):
    metrics = self.nn_model.evaluate(data)
    LOGGER.info(f"metrics at iter {self.aggregate_iteration_num}: {metrics}")
    loss = metrics["loss"]
    callback_loss(self,self.aggregate_iteration_num, loss)
    self.loss_scatter.send_loss(loss=(loss, epoch_degree), suffix=_suffix(self))
    is_converged = self.has_converged.get_converge_status(suffix=_suffix(self))
    self._summary["is_converged"] = is_converged
    self._summary["loss_history"].append(loss)
    return is_converged


def client_export_model(self):
    return _build_model_dict(meta=client_get_meta(self), param=client_get_param(self))


def client_get_meta(self):
    from kernel.protobuf.generated import nn_model_meta_pb2

    meta_pb = nn_model_meta_pb2.NNModelMeta()
    meta_pb.params.CopyFrom(self.model_param.generate_pb())
    meta_pb.aggregate_iter = self.aggregate_iteration_num
    return meta_pb


def client_get_param(self):
    from kernel.protobuf.generated import nn_model_param_pb2

    param_pb = nn_model_param_pb2.NNModelParam()
    param_pb.saved_model_bytes = self.nn_model.export_model()
    param_pb.header.extend(self._header)
    for label, mapped in self._label_align_mapping.items():
        param_pb.label_mapping.add(label=json.dumps(label), mapped=json.dumps(mapped))
    return param_pb


def client_load_model(self, meta_obj, model_obj):
    self.model_param.restore_from_pb(meta_obj.params)
    client_set_params(self, self.model_param)
    self.aggregate_iteration_num = meta_obj.aggregate_iter
    self.nn_model = restore_nn_model(self.config_type, model_obj.saved_model_bytes)
    self._header = list(model_obj.header)
    self._label_align_mapping = {}
    for item in model_obj.label_mapping:
        label = json.loads(item.label)
        mapped = json.loads(item.mapped)
        self._label_align_mapping[label] = mapped


def client_predict(self, data_inst):
    self.align_data_header(data_instances=data_inst, pre_header=self._header)
    data = self.data_converter.convert(
        data_inst,
        batch_size=self.batch_size,
        encode_label=self.encode_label,
        label_mapping=self._label_align_mapping,
    )
    predict = self.nn_model.predict(data)
    num_output_units = predict.shape[1]
    if num_output_units == 1:
        kv = zip(data.get_keys(), map(lambda x: x.tolist()[0], predict))
    else:
        kv = zip(data.get_keys(), predict.tolist())
    pred_tbl = session.parallelize(
        kv, include_key=True, partition=data_inst._partitions
    )
    classes = [0, 1] if num_output_units == 1 else [i for i in range(num_output_units)]
    return self.predict_score_to_output(
        data_inst,
        pred_tbl,
        classes=classes,
        threshold=self.param.predict_param.threshold,
    )
