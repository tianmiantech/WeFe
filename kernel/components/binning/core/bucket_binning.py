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


from kernel.base.statistics import MultivariateStatistical
from kernel.components.binning.core.base_binning import Binning
from kernel.utils import data_util


class BucketBinning(Binning):
    """
    For bucket binning, the length of each bin is the same which is:
    L = [max(x) - min(x)] / n

    The split points are min(x) + L * k
    where k is the index of a bin.
    """

    def fit_split_points(self, data_instances):
        """
        Apply the binning method

        Parameters
        ----------
        data_instances : DSource
            The input data

        Returns
        -------
        split_points : dict.
            Each value represent for the split points for a feature. The element in each row represent for
            the corresponding split point.
            e.g.
            split_points = {'x1': [0.1, 0.2, 0.3, 0.4 ...],    # The first feature
                            'x2': [1, 2, 3, 4, ...],           # The second feature
                            ...]                         # Other features

        """
        header = data_util.get_header(data_instances)
        self._default_setting(header)

        is_sparse = data_util.is_sparse_data(data_instances)
        if is_sparse:
            raise RuntimeError("Bucket Binning method has not supported sparse data yet.")

        # statistics = MultivariateStatisticalSummary(data_instances,
        #                                             self.bin_inner_param.bin_indexes,
        #                                             abnormal_list=self.abnormal_list)
        statistics = MultivariateStatistical(data_instances=data_instances,
                                             cols_index=self.bin_inner_param.bin_indexes,
                                             abnormal_list=self.abnormal_list)
        max_dict = statistics.get_max()
        min_dict = statistics.get_min()
        for col_name, max_value in max_dict.items():
            min_value = min_dict.get(col_name)
            split_points = []
            L = (max_value - min_value) / self.bin_num
            for k in range(self.bin_num - 1):
                s_p = min_value + (k + 1) * L
                split_points.append(s_p)
            split_points.append(max_value)
            self.bin_results.put_col_split_points(col_name, split_points)
        self.fit_category_features(data_instances)
        return self.bin_results.all_split_points
