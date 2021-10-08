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

from kernel.components.binning.vertfeaturebinning.param import FeatureBinningParam, TransformParam
from kernel.utils import consts


class HorzFeatureBinningParam(FeatureBinningParam):
    def __init__(self, method=consts.VIRTUAL_SUMMARY,
                 compress_thres=consts.DEFAULT_COMPRESS_THRESHOLD,
                 head_size=consts.DEFAULT_HEAD_SIZE,
                 error=consts.DEFAULT_RELATIVE_ERROR,
                 sample_bins=100,
                 bin_num=consts.G_BIN_NUM, bin_indexes=-1, bin_names=None, adjustment_factor=0.5,
                 transform_param=TransformParam(),
                 category_indexes=None, category_names=None,
                 need_run=True, max_iter=100):
        super(HorzFeatureBinningParam, self).__init__(method=method, compress_thres=compress_thres,
                                                      head_size=head_size, error=error,
                                                      bin_num=bin_num, bin_indexes=bin_indexes,
                                                      bin_names=bin_names, adjustment_factor=adjustment_factor,
                                                      transform_param=transform_param,
                                                      category_indexes=category_indexes, category_names=category_names,
                                                      need_run=need_run)
        self.sample_bins = sample_bins
        self.max_iter = max_iter

    def check(self):
        descr = "horz binning param's"
        super(HorzFeatureBinningParam, self).check()
        self.check_string(self.method, descr)
        self.method = self.method.lower()
        self.check_valid_value(self.method, descr, [consts.VIRTUAL_SUMMARY, consts.RECURSIVE_QUERY])
        self.check_positive_integer(self.max_iter, descr)
        if self.max_iter > 100:
            raise ValueError("Max iter is not allowed exceed 100")
