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
from kernel.components.feature.onehot.onehot import OneHotEncoder
from kernel.components.feature.onehot.vertonehot.param import OneHotEncoderParam

LOGGER = log_utils.get_logger()


class VertOneHotEncoder(OneHotEncoder):
    def __init__(self):
        super(VertOneHotEncoder, self).__init__()
        self.model_name = 'VertOneHotEncoder'
        self.model_param_name = 'VertOneHotParam'
        self.model_meta_name = 'VertOneHotMeta'
        self.model_param = OneHotEncoderParam()

    def _init_model(self, params):
        super(VertOneHotEncoder, self)._init_model(params)

    def fit(self, data_instances):
        return super(VertOneHotEncoder, self).fit(data_instances)
