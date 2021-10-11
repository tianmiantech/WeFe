#!/usr/bin/env python
# -*- coding: utf-8 -*-

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


class FeatureStatisticParam(BaseParam):

    def __init__(self, percentage_list=None, delimiter=",", label_name="y", label_type="int", data_type="float",
                 work_mode=None, unique_count_threshold=20):
        if percentage_list is None:
            percentage_list = [5, 25, 50, 75, 95]
        self.percentage_list = percentage_list
        self.delimiter = delimiter
        self.label_name = label_name
        self.label_type = label_type
        self.data_type = data_type
        self.work_mode = work_mode
        self.unique_count_threshold = unique_count_threshold

    def check(self):
        for p in self.percentage_list:
            if 0 > p > 100:
                raise ValueError("percentage should be in range (0,100]")
