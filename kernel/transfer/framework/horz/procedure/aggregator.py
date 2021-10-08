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
import types
import typing
from functools import reduce

from common.python.utils import log_utils
from kernel.transfer.framework.horz.blocks import has_converged, loss_scatter, model_scatter, model_broadcaster
from kernel.transfer.framework.horz.blocks import random_padding_cipher
from kernel.transfer.framework.horz.blocks.base import HorzTransferBase
from kernel.transfer.framework.horz.blocks.has_converged import HasConvergedTransVar
from kernel.transfer.framework.horz.blocks.loss_scatter import LossScatterTransVar
from kernel.transfer.framework.horz.blocks.model_broadcaster import ModelBroadcasterTransVar
from kernel.transfer.framework.horz.blocks.model_scatter import ModelScatterTransVar
from kernel.transfer.framework.horz.blocks.random_padding_cipher import RandomPaddingCipherTransVar
from kernel.transfer.framework.weights import Weights, NumericWeights, TransferableWeights
from kernel.transfer.variables.base_transfer_variable import BaseTransferVariables
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class LegacyAggregatorTransVar(HorzTransferBase):
    def __init__(self, server=(consts.ARBITER,), clients=(consts.PROMOTER, consts.PROVIDER,), prefix=None):
        super().__init__(server=server, clients=clients, prefix=prefix)
        self.loss_scatter = LossScatterTransVar(server=server, clients=clients, prefix=self.prefix)
        self.has_converged = HasConvergedTransVar(server=server, clients=clients, prefix=self.prefix)
        self.model_scatter = ModelScatterTransVar(server=server, clients=clients, prefix=self.prefix)
        self.model_broadcaster = ModelBroadcasterTransVar(server=server, clients=clients, prefix=self.prefix)
        self.random_padding_cipher = RandomPaddingCipherTransVar(server=server, clients=clients, prefix=self.prefix)


class Arbiter(object):

    def __init__(self, trans_var=LegacyAggregatorTransVar()):
        self._promoter_parties = trans_var.get_parties([consts.PROMOTER])
        self._provider_parties = trans_var.get_parties([consts.PROVIDER])
        self._client_parties = trans_var.client_parties

        self._loss_sync = loss_scatter.Server(trans_var.loss_scatter)
        self._converge_sync = has_converged.Server(trans_var.has_converged)
        self._model_scatter = model_scatter.Server(trans_var.model_scatter)
        self._model_broadcaster = model_broadcaster.Server(trans_var.model_broadcaster)
        self._random_padding_cipher = random_padding_cipher.Server(trans_var.random_padding_cipher)

    # noinspection PyUnusedLocal,PyAttributeOutsideInit,PyProtectedMember
    def register_aggregator(self, transfer_variables: BaseTransferVariables, enable_secure_aggregate=True):
        if enable_secure_aggregate:
            self._random_padding_cipher.exchange_secret_keys()
        return self

    def aggregate_model(self, suffix=tuple()) -> Weights:
        models = self.get_models_for_aggregate(suffix=suffix)
        total_model, total_degree = reduce(lambda x, y: (x[0] + y[0], x[1] + y[1]), models)
        total_model /= total_degree
        LOGGER.debug("In aggregate model, total_model: {}, total_degree: {}".format(total_model.unboxed, total_degree))
        return total_model

    def aggregate_and_broadcast(self, suffix=tuple()):
        """
        aggregate models from promoter and providers, then broadcast the aggregated model.

        Args:
            ciphers_dict: a dict of provider id to provider cipher
            suffix: tag suffix
        """
        model = self.aggregate_model(suffix=suffix)
        self.send_aggregated_model(model, suffix=suffix)
        return model

    def get_models_for_aggregate(self, suffix=tuple()):
        models = self._model_scatter.get_models(suffix=suffix)
        promoter_model = models[0]
        yield (promoter_model.weights, promoter_model.get_degree() or 1.0)

        # provider model
        index = 0
        for model in models[1:]:
            weights = model.weights
            yield (weights, model.get_degree() or 1.0)
            index += 1

    def send_aggregated_model(self, model: Weights,
                              suffix=tuple()):
        self._model_broadcaster.send_model(model=model.for_remote(), suffix=suffix)

    def aggregate_loss(self, suffix=tuple()):
        losses = self._loss_sync.get_losses(suffix=suffix)
        total_loss = 0.0
        total_degree = 0.0
        for loss in losses:
            total_loss += loss.unboxed
            total_degree += loss.get_degree(1.0)
        return total_loss / total_degree

    def send_converge_status(self, converge_func: types.FunctionType, converge_args, suffix=tuple()):
        is_converge = converge_func(*converge_args)
        return self._converge_sync.remote_converge_status(is_converge, suffix=suffix)


class Client(object):
    def __init__(self, trans_var=LegacyAggregatorTransVar()):
        self._enable_secure_aggregate = False

        self._loss_sync = loss_scatter.Client(trans_var.loss_scatter)
        self._converge_sync = has_converged.Client(trans_var.has_converged)
        self._model_scatter = model_scatter.Client(trans_var.model_scatter)
        self._model_broadcaster = model_broadcaster.Client(trans_var.model_broadcaster)
        self._random_padding_cipher = random_padding_cipher.Client(trans_var.random_padding_cipher)

    # noinspection PyAttributeOutsideInit,PyUnusedLocal,PyProtectedMember
    def register_aggregator(self, transfer_variables: BaseTransferVariables, enable_secure_aggregate=True):
        self._enable_secure_aggregate = enable_secure_aggregate
        if enable_secure_aggregate:
            self._cipher = self._random_padding_cipher.create_cipher()
        return self

    def secure_aggregate(self, send_func, weights: Weights, degree: float = None, enable_secure_aggregate=True):
        # w -> w * degree
        if degree:
            weights *= degree
        # w * degree -> w * degree + \sum(\delta(i, j) * r_{ij}), namely， adding random mask.
        if enable_secure_aggregate:
            weights = weights.encrypted(cipher=self._cipher, inplace=True)
        # maybe remote degree
        remote_weights = weights.for_remote().with_degree(degree) if degree else weights.for_remote()

        send_func(remote_weights)

    def send_model(self, weights: Weights, degree: float = None, suffix=tuple()):
        def _func(_weights: TransferableWeights):
            self._model_scatter.send_model(model=_weights, suffix=suffix)

        return self.secure_aggregate(send_func=_func,
                                     weights=weights,
                                     degree=degree,
                                     enable_secure_aggregate=self._enable_secure_aggregate)

    def get_aggregated_model(self, suffix=tuple()):
        return self._model_broadcaster.get_model(suffix=suffix)

    def aggregate_then_get(self, model: Weights, degree: float = None, suffix=tuple()) -> Weights:
        self.send_model(weights=model, degree=degree, suffix=suffix)
        return self.get_aggregated_model(suffix=suffix)

    def send_loss(self, loss: typing.Union[float, Weights], degree: float = None, suffix=tuple()):
        if isinstance(loss, float):
            loss = NumericWeights(loss)
        return self.secure_aggregate(send_func=functools.partial(self._loss_sync.send_loss, suffix=suffix),
                                     weights=loss, degree=degree,
                                     enable_secure_aggregate=False)

    def get_converge_status(self, suffix=tuple()):
        return self._converge_sync.get_converge_status(suffix=suffix)


Promoter = Client
Provider = Client


def with_role(role, transfer_variable, enable_secure_aggregate=True):
    if role == consts.PROMOTER:
        return Client().register_aggregator(transfer_variable, enable_secure_aggregate)
    elif role == consts.PROVIDER:
        return Client().register_aggregator(transfer_variable, enable_secure_aggregate)
    elif role == consts.ARBITER:
        return Arbiter().register_aggregator(transfer_variable, enable_secure_aggregate)
    else:
        raise ValueError(f"role {role} not found")
