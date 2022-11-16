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


import copy

from common.python.utils import log_utils
from kernel.base.params.base_param import BaseParam
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class TransformParam(BaseParam):
    """
    Define how to transfer the cols

    Parameters
    ----------
    transform_cols : list of column index, default: -1
        Specify which columns need to be transform. If column index is None, None of columns will be transformed.
        If it is -1, it will use same columns as cols in binning module.

    transform_names: list of string, default: []
        Specify which columns need to calculated. Each element in the list represent for a column name in header.


    transform_type: str, 'bin_num'or 'woe' or None default: 'bin_num'
        Specify which value these columns going to replace.
         1. bin_num: Transfer original feature value to bin index in which this value belongs to.
         2. woe: This is valid for promoter party only. It will replace original value to its woe value
         3. None: nothing will be replaced.
    """

    def __init__(self, transform_cols=-1, transform_names=None, transform_type="woe"):
        super(TransformParam, self).__init__()
        self.transform_cols = transform_cols
        self.transform_names = transform_names
        self.transform_type = transform_type
        self.model_save_to_storage = True

    def check(self):
        descr = "Transform Param's "
        if self.transform_cols is not None and self.transform_cols != -1:
            self.check_defined_type(self.transform_cols, descr, ['list'])
        self.check_defined_type(self.transform_names, descr, ['list', "NoneType"])
        if self.transform_names is not None:
            for name in self.transform_names:
                if not isinstance(name, str):
                    raise ValueError("Elements in transform_names should be string type")
        self.check_valid_value(self.transform_type, descr, ['bin_num', 'woe', None])


class OptimalBinningParam(BaseParam):
    """
    Indicate optimal binning params

    Parameters
    ----------
    metric_method: str, default: "iv"
        The algorithm metric method. Support iv, gini, ks, chi-square


    min_bin_pct: float, default: 0.05
        The minimum percentage of each bucket

    max_bin_pct: float, default: 1.0
        The maximum percentage of each bucket

    init_bin_nums: int, default 100
        Number of bins when initialize

    mixture: bool, default: True
        Whether each bucket need event and non-event records

    init_bucket_method: str default: quantile
        Init bucket methods. Accept quantile and bucket.

    """

    def __init__(self, metric_method='chi-square', min_bin_pct=0.05, max_bin_pct=1.0,
                 init_bin_nums=100, mixture=True, init_bucket_method='quantile'):
        super().__init__()
        self.init_bucket_method = init_bucket_method
        self.metric_method = metric_method
        self.max_bin = None
        self.mixture = mixture
        self.max_bin_pct = max_bin_pct
        self.min_bin_pct = min_bin_pct
        self.init_bin_nums = init_bin_nums
        self.adjustment_factor = None

    def check(self):
        descr = "vert binning's optimal binning param's"
        self.check_string(self.metric_method, descr)

        self.metric_method = self.metric_method.lower()
        if self.metric_method in ['chi_square', 'chi-square']:
            self.metric_method = 'chi_square'
        self.check_valid_value(self.metric_method, descr, ['iv', 'gini', 'chi_square', 'ks'])
        self.check_positive_integer(self.init_bin_nums, descr)

        self.init_bucket_method = self.init_bucket_method.lower()
        self.check_valid_value(self.init_bucket_method, descr, ['quantile', 'bucket'])

        if self.max_bin_pct not in [1, 0]:
            self.check_decimal_float(self.max_bin_pct, descr)
        if self.min_bin_pct not in [1, 0]:
            self.check_decimal_float(self.min_bin_pct, descr)
        if self.min_bin_pct > self.max_bin_pct:
            raise ValueError("Optimal binning's min_bin_pct should less or equal than max_bin_pct")

        self.check_boolean(self.mixture, descr)
        self.check_positive_integer(self.init_bin_nums, descr)


class FeatureBinningParam(BaseParam):
    """
    Define the feature binning method

    Parameters
    ----------
    method : str, 'quantile' or 'bucket', default: 'quantile'
        Binning method.

    compress_thres: int, default: 10000
        When the number of saved summaries exceed this threshold, it will call its compress function

    head_size: int, default: 10000
        The buffer size to store inserted observations. When head list reach this buffer size, the
        QuantileSummaries object start to generate summary(or stats) and insert into its sampled list.

    error: float, 0 <= error < 1 default: 0.001
        The error of tolerance of binning. The final split point comes from original data, and the rank
        of this value is close to the exact rank. More precisely,
        floor((p - 2 * error) * N) <= rank(x) <= ceil((p + 2 * error) * N)
        where p is the quantile in float, and N is total number of data.

    bin_num: int, bin_num > 0, default: 10
        The max bin number for binning

    bin_indexes : list of int or int, default: -1
        Specify which columns need to be binned. -1 represent for all columns. If you need to indicate specific
        cols, provide a list of header index instead of -1.

    bin_names : list of string, default: []
        Specify which columns need to calculated. Each element in the list represent for a column name in header.

    adjustment_factor : float, default: 0.5
        the adjustment factor when calculating WOE. This is useful when there is no event or non-event in
        a bin. Please note that this parameter will NOT take effect for setting in provider.

    category_indexes : list of int or int, default: []
        Specify which columns are category features. -1 represent for all columns. List of int indicate a set of
        such features. For category features, bin_obj will take its original values as split_points and treat them
        as have been binned. If this is not what you expect, please do NOT put it into this parameters.

        The number of categories should not exceed bin_num set above.

    category_names : list of string, default: []
        Use column names to specify category features. Each element in the list represent for a column name in header.

    local_only : bool, default: False
        Whether just provide binning method to promoter party. If true, provider party will do nothing.

    transform_param: TransformParam
        Define how to transfer the binned data.

    need_run: bool, default True
        Indicate if this module needed to be run

    feature_split_points: default None
        custom split points:
                            {'x1': [0.1, 0.2, 0.3, 0.4 ...],    # The first feature
                            'x2': [1, 2, 3, 4, ...],           # The second feature
                            ...                               # Other features
                            }

    """

    def __init__(self, method=consts.QUANTILE,
                 compress_thres=consts.DEFAULT_COMPRESS_THRESHOLD,
                 head_size=consts.DEFAULT_HEAD_SIZE,
                 error=consts.DEFAULT_RELATIVE_ERROR,
                 bin_num=consts.G_BIN_NUM, bin_indexes=-1, bin_names=None, adjustment_factor=0.5,
                 transform_param=TransformParam(), optimal_binning_param=OptimalBinningParam(),
                 local_only=False, category_indexes=None, category_names=None,
                 need_run=True, with_label=False, modes=[], feature_split_points=None):
        super(FeatureBinningParam, self).__init__()
        self.method = method
        self.compress_thres = compress_thres
        self.head_size = head_size
        self.error = error
        self.adjustment_factor = adjustment_factor
        self.bin_num = bin_num
        self.bin_indexes = bin_indexes
        self.bin_names = bin_names
        self.category_indexes = category_indexes
        self.category_names = category_names
        self.local_only = local_only
        self.transform_param = copy.deepcopy(transform_param)
        self.optimal_binning_param = copy.deepcopy(optimal_binning_param)
        self.need_run = need_run
        self.with_label = with_label
        self.modes = modes
        self.model_save_to_storage = True
        self.feature_split_points = feature_split_points

    def check(self):
        descr = "vert binning param's"
        self.check_string(self.method, descr)
        self.method = self.method.lower()
        self.check_valid_value(self.method, descr,
                               [consts.QUANTILE, consts.BUCKET, consts.OPTIMAL, consts.VIRTUAL_SUMMARY,
                                consts.RECURSIVE_QUERY])
        self.check_positive_integer(self.compress_thres, descr)
        self.check_positive_integer(self.head_size, descr)
        self.check_decimal_float(self.error, descr)
        self.check_positive_integer(self.bin_num, descr)
        if self.bin_indexes != -1:
            self.check_defined_type(self.bin_indexes, descr, ['list', 'RepeatedScalarContainer', "NoneType"])
        self.check_defined_type(self.bin_names, descr, ['list', "NoneType"])
        self.check_defined_type(self.category_indexes, descr, ['list', "NoneType"])
        self.check_defined_type(self.category_names, descr, ['list', "NoneType"])
        self.check_open_unit_interval(self.adjustment_factor, descr)
        self.transform_param.check()
        self.optimal_binning_param.check()
