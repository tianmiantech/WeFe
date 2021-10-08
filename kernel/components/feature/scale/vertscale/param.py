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


class VertFeatureScaleParam(BaseParam):
    """
    parm:
        scale_rules: json string,
        scale function: log2,log10,ln,abs,sqrt
        "{
        "x1":"log2",
        "x2":"abs",
        "x3":"sqrt"
        }"
    """

    def check(self):
        if self.scale_rules is not None:
            scale_function = ['log2', 'log10', 'ln', 'abs', 'sqrt']
            import json
            for value in json.loads(self.scale_rules).values():
                if value not in scale_function:
                    raise ValueError(f'feature scale component not support function {value}')

    def __init__(self, scale_rules=None):
        super(VertFeatureScaleParam, self).__init__()
        self.scale_rules = scale_rules
