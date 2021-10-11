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

from kernel.transfer.framework.horz.util import scatter
from kernel.transfer.framework.weights import TransferableWeights
from kernel.utils import consts


class Arbiter(object):

    # noinspection PyAttributeOutsideInit
    def register_model_scatter(self, provider_model_transfer, promoter_model_transfer):
        self._models_sync = scatter.Scatter(provider_model_transfer, promoter_model_transfer)
        return self

    def get_models(self, ciphers_dict=None, suffix=tuple()):

        # promoter model
        # [ 364.44436338  -41.59516605  -22.97634444  -24.54673376  -37.65999191
        # -25.02225081  -44.58904665  -26.1643018   -17.20539893  -21.21426604
        # -27.63050736  -37.80325492  -30.27601551 -383.83758213 -340.89499223
        #  40.52149771 -155.67227652    9.05630857  620.55212505  564.83798097
        # 513.04012601  458.03732521  392.89909244]
        # 12000
        models_iter = self._models_sync.get(suffix=suffix)
        promoter_model = next(models_iter)
        yield (promoter_model.weights, promoter_model.get_degree() or 1.0)

        # provider model
        index = 0
        for model in models_iter:
            weights = model.weights
            if ciphers_dict and ciphers_dict.get(index, None):
                weights = weights.decrypted(ciphers_dict[index])
                # [421.56685291617055 66.73598851437191 26.093445360320494 43.72374254652317
                # 83.25967298442583 47.495353355247154 143.3771411173139
                # -23.190880623652834 -11.857719514310102 -20.87447365089803
                # -20.936092609468464 -27.101315708848947 -36.77292698040004
                # -257
            yield (weights, model.get_degree() or 1.0)
            index += 1


class _Client(object):
    # noinspection PyAttributeOutsideInit
    def register_model_scatter(self, model_transfer):
        self._models_sync = model_transfer
        return self

    def send_model(self, weights: TransferableWeights, suffix=tuple()):
        self._models_sync.remote(obj=weights, role=consts.ARBITER, idx=0, suffix=suffix)
        return weights


Promoter = _Client
Provider = _Client
