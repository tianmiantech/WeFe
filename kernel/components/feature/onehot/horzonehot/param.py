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

from kernel.base.params.base_param import BaseParam


class HorzOneHotParam(BaseParam):
    """
    parm:
        transform_col_indexes: list,
        transform_col_names: list
            ["x1", "x2","x3"]
        need_run: bool, default True
            Indicate if this module needed to be run

        need_alignment: bool, default True
            Indicated whether alignment of features is turned on
    """

    def check(self):
        pass

    def __init__(self, transform_col_indexes=-1, transform_col_names=None, need_run=True, need_alignment=True,
                 save_dataset=True):
        super(HorzOneHotParam, self).__init__()
        if transform_col_names is None:
            transform_col_names = []
        self.transform_col_indexes = transform_col_indexes
        self.transform_col_names = transform_col_names
        self.need_run = need_run
        self.need_alignment = need_alignment
        self.save_dataset = save_dataset
