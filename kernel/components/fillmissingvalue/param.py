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

from kernel.base.params.base_param import BaseParam


class Params(BaseParam):
    """
    parm:
        features: dict,
        {'x1':{'method':'mean', 'value':0.0},'x2':{'method':'mean', 'value':0.0}}
        statistics: dict, {'mean':{'x1':1.0, 'x2':2.0}, 'min':{'x1':2.0, 'x2':3.0}}
    """

    def check(self):
        pass

    def __init__(self, features="{}", statistics=None, save_dataset=False, with_label=False):
        self.features = features
        self.statistics = statistics
        self.save_dataset = save_dataset
        self.with_label = with_label
