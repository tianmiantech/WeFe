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
from kernel.utils import consts


class SegmentParam(BaseParam):

    def __init__(self, mode=consts.VERT, train_ratio=0.7, random_num=10000,
                 feature_columns='', with_label=False, label_name='y',
                 label_type='int'):
        """

        Parameters
        ----------
        mode: string, segment mode, options (vert, horz)
        train_ratio: float, between 0 and 1
        random_num: int, segment random seed
        feature_columns: string or list, the selected features, '' or None represents all features
        with_label: boolean, whether contains label
        label_name: string, label name
        label_type: string, label type
        """

        self.mode = mode
        self.train_ratio = train_ratio
        self.random_num = random_num
        self.feature_columns = feature_columns
        self.with_label = with_label
        self.label_name = label_name
        self.label_type = label_type

    def check(self):
        descr = "segment param's"
        self.check_valid_value(self.mode, descr, [consts.VERT, consts.HORZ])
        self.check_boolean(self.with_label, descr)
        self.check_string(self.label_name, descr)
        self.check_positive_integer(self.random_num, descr)

        if not (0 < self.train_ratio <= 1):
            raise ValueError("train_ratio should be in range (0,1]")

        return True
