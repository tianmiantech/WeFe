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

from kernel.components.deeplearning.vertnn.backend.vert_nn_model import VertNNKerasPromoterModel
from kernel.components.deeplearning.vertnn.backend.vert_nn_model import VertNNKerasProviderModel


def model_builder(role="promoter", vert_nn_param=None, backend="keras"):
    if backend != "keras":
        raise ValueError("Only support keras backend in this version!")

    if role == "promoter":
        return VertNNKerasPromoterModel(vert_nn_param)
    elif role == "provider":
        return VertNNKerasProviderModel(vert_nn_param)
