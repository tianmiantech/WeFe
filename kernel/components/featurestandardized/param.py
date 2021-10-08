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


from kernel.base.params.base_param import BaseParam


class FeatureStandardizedParam(BaseParam):
    """
        method: Standardized method,
                min-max: (x - x_min)/(x_max - x_min)
                z-score: (x - x_mean)/std
    """

    def __init__(self, method='z-score', save_dataset=False, with_label=False, fields=None):
        self.save_dataset = save_dataset
        self.with_label = with_label
        self.method = method
        self.fields = fields

    def check(self):
        pass
