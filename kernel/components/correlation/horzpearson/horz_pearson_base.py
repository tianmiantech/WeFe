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


from kernel.components.correlation.horzpearson.param import HorzPearsonParam
from kernel.model_base import ModelBase
from kernel.utils import consts


class HorzPearsonBase(ModelBase):
    def __init__(self):
        super(HorzPearsonBase, self).__init__()
        self.task_result_type = "horz_pearson"
        self.mode = consts.HORZ
        self.model_param = HorzPearsonParam()
        self.aggregator = None
        self.col_names = None

    def _init_model(self, params):
        super(HorzPearsonBase, self)._init_model(params)
        self.column_names = params.column_names
