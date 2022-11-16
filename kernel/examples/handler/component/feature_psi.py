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

# from business.api.utils import log_utils
from common.python.utils import log_utils
from kernel.components.feature.featurepsi.vertfeaturepsi.param import VertFeaturePSIParam
from kernel.examples.handler.component.component_base import Component
from kernel.examples.handler.interface import Input
from kernel.examples.handler.interface import Output

LOGGER = log_utils.get_logger()

class VertFeaturePSI(Component, VertFeaturePSIParam):
    def __init__(self, **kwargs):
        Component.__init__(self, **kwargs)
        LOGGER.debug(f"{self.name} component created")

        new_kwargs = self.erase_component_base_param(**kwargs)
        VertFeaturePSIParam.__init__(self, **new_kwargs)
        self.input = Input(self.name)
        self.output = Output(self.name)
        self._module_name = "VertFeaturePSI"
        self._param_name = "VertFeaturePSIParam"
