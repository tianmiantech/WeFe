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

class FeatureImportance(object):

    def __init__(self, importance=0, importance_2=0, main_type='split'):

        self.legal_type = ['split', 'gain']
        assert main_type in self.legal_type, 'illegal importance type {}'.format(main_type)
        self.importance = importance
        self.importance_2 = importance_2
        self.main_type = main_type

    def add_gain(self, val):
        if self.main_type == 'gain':
            self.importance += val
        else:
            self.importance_2 += val

    def add_split(self, val):
        if self.main_type == 'split':
            self.importance += val
        else:
            self.importance_2 += val

    def __cmp__(self, other):

        if self.importance > other.importance:
            return 1
        elif self.importance < other.importance:
            return -1
        else:
            return 0

    def __eq__(self, other):
        return self.importance == other.importance

    def __lt__(self, other):
        return self.importance < other.importance

    def __repr__(self):
        return 'importance type: {}, importance: {}, importance2 {}'.format(self.main_type, self.importance,
                                                                            self.importance_2)

    def __add__(self, other):
        new_importance = FeatureImportance(main_type=self.main_type, importance=self.importance + other.importance,
                                           importance_2=self.importance_2 + other.importance_2)
        return new_importance
