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


class ScorecardParam(BaseParam):

    def __init__(self, p0 = None, pdo = None):
        super(ScorecardParam, self).__init__()
        self.p0 = p0
        self.pdo = pdo

    def check(self):
        if self.p0 is None:
            raise ValueError("The value of p0 is None")
        if self.pdo is None:
            raise ValueError("The value of pdo is None")
        if self.p0 is not None and self.p0 <0 :
            raise ValueError("The value of p0 is {}, and its value should be greater than 0".format(self.p0))
        if self.pdo is not None and self.pdo <0:
            raise ValueError("The value of pdo is {}, and its value should be greater than 0".format(self.pdo))

