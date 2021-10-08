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

from kernel.components.feature.statistic.horzstatistic.param import HorzStatisticParam
from kernel.model_base import ModelBase
from kernel.transfer.variables.transfer_class.horz_statistic_transfer_variable import HorzStatisticTransferVariable
from kernel.utils import consts


class HorzStatisticBase(ModelBase):
    def __init__(self):
        super(HorzStatisticBase, self).__init__()
        self.task_result_type = "data_feature_statistic"
        self.mode = consts.HORZ
        self.model_param = HorzStatisticParam()
        self.aggregator = None
        self.col_names = None

    def _init_model(self, params):
        super(HorzStatisticBase, self)._init_model(params)

        self.transfer_variable = HorzStatisticTransferVariable()
        self.col_names = params.col_names
