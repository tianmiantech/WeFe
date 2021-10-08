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


class VertSampleFilterParam(BaseParam):
    """
    parm:
        filter_rules: string,
            "x1>2&x1<50&x3=100&x5!=30"
    """

    def check(self):
        pass

    def __init__(self, filter_rules=None):
        super(VertSampleFilterParam, self).__init__()
        self.filter_rules = filter_rules
