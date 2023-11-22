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

from kernel.examples.handler.component.component_base import Component
from kernel.examples.handler.component.nn.models.sequantial import Sequential
from kernel.examples.handler.interface import Input
from kernel.examples.handler.interface import Output
from kernel.examples.handler.utils.tools import extract_explicit_parameter


class HorzNN(Component):
    @extract_explicit_parameter
    def __init__(self, name=None, max_iter=100, batch_size=-1,
                 secure_aggregate=True, aggregate_every_n_epoch=1,
                 early_stop="diff", encode_label=False,
                 predict_param=None, cv_param=None, **kwargs):

        explicit_parameters = kwargs["explict_parameters"]
        explicit_parameters["optimizer"] = None
        explicit_parameters["loss"] = None
        explicit_parameters["metrics"] = None
        explicit_parameters["nn_define"] = None
        explicit_parameters["config_type"] = "keras"
        Component.__init__(self, **explicit_parameters)

        if "name" in explicit_parameters:
            del explicit_parameters["name"]
        for param_key, param_value in explicit_parameters.items():
            setattr(self, param_key, param_value)

        self.optimizer = None
        self.loss = None
        self.metrics = None
        self.nn_define = None
        self.config_type = "keras"
        self.input = Input(self.name, data_type="multi")
        self.output = Output(self.name, data_type='single')
        self._module_name = "HorzNN"
        self._model = Sequential()

    def set_model(self, model):
        self._model = model

    def add(self, layer):
        self._model.add(layer)
        return self

    def compile(self, nn_define,optimizer, loss=None, metrics=None,config_type=None):
        if metrics and not isinstance(metrics, list):
            raise ValueError("metrics should be a list")

        self.optimizer = optimizer
        self.loss = loss
        self.metrics = metrics
        self.config_type = config_type
        # self.nn_define = self._model.get_network_config()
        self.nn_define = nn_define
        return self

    def __getstate__(self):
        state = dict(self.__dict__)
        del state["_model"]

        return state
