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


class VertFeatureSoftenParam(BaseParam):
    """
    parm:
        scale_rules: json string,
        method: z_score, min：confidence level，max: fill 0
                min_max_per， min：min_per，max：max_per
                min_max_thresh，min：min_thresh，max：max_thresh
        example:
        "{
            "x1":{
                "method": "z_score",
                "min": 10.0,
                "max": 0
            },
            "x2":{
                "method": "min_max_per",
                "min": 10.0,
                "max": 10.0
            },
            "x3":{
                "method": "min_max_thresh",
                "min": 10.0,
                "max": 10.0
            }
        }"
    """

    def check(self):
        pass

    def __init__(self, soften_rules=None):
        super(VertFeatureSoftenParam, self).__init__()
        self.soften_rules = soften_rules
