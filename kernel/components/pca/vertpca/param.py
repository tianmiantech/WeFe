# !/usr/bin/env python
# -*- coding: utf-8 -*-

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


class VertPCAParam(BaseParam):

    def __init__(self, column_names=None, column_indexes=None, cross_parties=True):
        super().__init__()
        self.column_names = column_names
        self.column_indexes = column_indexes
        self.cross_parties = cross_parties
        if column_names is None:
            self.column_names = []
        if column_indexes is None:
            self.column_indexes = []

    def check(self):
        pass
