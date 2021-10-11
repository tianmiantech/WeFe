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

from common.python.utils import log_utils
from kernel.transfer.framework.horz.blocks import secure_aggregator
from kernel.transfer.framework.horz.blocks.aggregator import AggregatorTransVar
from kernel.transfer.framework.horz.blocks.random_padding_cipher import RandomPaddingCipherTransVar
from kernel.transfer.framework.horz.blocks.secure_aggregator import SecureAggregatorTransVar
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class SecureSumAggregatorTransVar(SecureAggregatorTransVar):
    def __init__(self, server=(consts.ARBITER,), clients=(consts.PROMOTER, consts.PROVIDER), prefix=None):
        super().__init__(server=server, clients=clients, prefix=prefix)
        self.aggregator_trans_var = AggregatorTransVar(server=server, clients=clients, prefix=self.prefix)
        self.random_padding_cipher_trans_var = \
            RandomPaddingCipherTransVar(server=server, clients=clients, prefix=self.prefix)


class Server(secure_aggregator.Server):
    def __init__(self, trans_var: SecureSumAggregatorTransVar = SecureSumAggregatorTransVar(),
                 enable_secure_aggregate=True):
        super().__init__(trans_var=trans_var, enable_secure_aggregate=enable_secure_aggregate)

    def sum_model(self, suffix=tuple()):
        def _func(models):
            return functools.reduce(model_add, models)

        return self.aggregate(_func, suffix=suffix)

    def max_model(self, suffix=tuple()):
        def _func(models):
            return functools.reduce(model_max, models)

        return self.aggregate(_func, suffix=suffix)

    def min_model(self, suffix=tuple()):
        def _func(models):
            return functools.reduce(model_min, models)

        return self.aggregate(_func, suffix=suffix)


class Client(secure_aggregator.Client):
    def __init__(self, trans_var: SecureSumAggregatorTransVar = SecureSumAggregatorTransVar(),
                 enable_secure_aggregate=True):
        super().__init__(trans_var=trans_var, enable_secure_aggregate=enable_secure_aggregate)


@functools.singledispatch
def model_encrypted(model, cipher):
    return model.encrypted(cipher, inplace=True)


@functools.singledispatch
def model_add(model, other):
    return model + other


@functools.singledispatch
def model_max(model, other):
    import numpy as np
    return np.maximum(np.array(model.unboxed), np.array(other.unboxed))


@functools.singledispatch
def model_min(model, other):
    import numpy as np
    return np.minimum(np.array(model.unboxed), np.array(other.unboxed))


@functools.singledispatch
def model_mul_scalar(model, scalar):
    model *= scalar
    return model


@functools.singledispatch
def model_div_scalar(model, scalar):
    model /= scalar
    return model
