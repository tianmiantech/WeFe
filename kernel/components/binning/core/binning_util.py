#!/usr/bin/python3
# -*- coding:utf-8 -*-　

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

import bisect
import functools
import math

import numpy as np

from common.python import session
from common.python.utils import log_utils
from kernel.utils import consts

LOGGER = log_utils.get_logger()


def query_table(summary, query_points):
    queries = [x.value for x in query_points]
    original_idx = np.argsort(np.argsort(queries))
    queries = np.sort(queries)
    ranks = summary.query_value_list(queries)
    ranks = np.array(ranks)[original_idx]
    return np.array(ranks, dtype=int)


def query(feature_name, values, bin_num, missing_count, total_count, allow_duplicate=False):
    percent_value = 1.0 / bin_num

    # calculate the split points
    percentile_rate = [i * percent_value for i in range(1, bin_num)]
    percentile_rate.append(1.0)
    this_count = total_count - missing_count[feature_name]
    query_ranks = [int(x * this_count) for x in percentile_rate]

    query_points, global_ranks = values[0], values[1]
    query_values = [x.value for x in query_points]
    query_res = []

    for rank in query_ranks:
        idx = bisect.bisect_left(global_ranks, rank)
        if idx >= len(global_ranks) - 1:
            approx_value = query_values[-1]
            query_res.append(approx_value)
        else:
            if np.fabs(query_values[idx + 1] - query_values[idx]) < consts.FLOAT_ZERO:
                query_res.append(query_values[idx])
            elif np.fabs(global_ranks[idx + 1] - global_ranks[idx]) < consts.FLOAT_ZERO:
                query_res.append(query_values[idx])
            else:
                approx_value = query_values[idx] + (query_values[idx + 1] - query_values[idx]) * \
                               ((rank - global_ranks[idx]) /
                                (global_ranks[idx + 1] - global_ranks[idx]))
                query_res.append(approx_value)
    if not allow_duplicate:
        query_res = sorted(set(query_res))
    return feature_name, query_res


def init_query_points(self, partitions, split_num, error_rank=1, need_first=True):
    query_points = []
    for idx, col_name in enumerate(self.bin_inner_param.bin_names):
        max_value = self.max_values[idx]
        min_value = self.min_values[idx]
        sps = np.linspace(min_value, max_value, split_num)

        if not need_first:
            sps = sps[1:]
        from kernel.components.binning.horzfeaturebinning.horz_binning_base import SplitPointNode
        split_point_array = [SplitPointNode(sps[i], min_value, max_value, allow_error_rank=error_rank)
                             for i in range(len(sps))]
        query_points.append((col_name, split_point_array))
    query_points_table = session.parallelize(query_points, include_key=True, partition=partitions)
    return query_points_table


def calc_query_point(self):
    query_func = functools.partial(query, bin_num=self.bin_num,
                                   missing_count=self.missing_count,
                                   total_count=self.total_count,
                                   allow_duplicate=self.allow_duplicate)
    split_point_table = self.query_points.join(self.global_ranks, lambda x, y: (x, y))
    split_point_table = split_point_table.map(query_func)
    split_points = dict(split_point_table.collect())
    LOGGER.info(f"split_points={split_points}")
    for col_name, sps in split_points.items():
        self.bin_results.put_col_split_points(col_name, sps)
    return self.bin_results.all_split_points


def calc_woe(self):
    """
        calc woe
        only have EVENT records or Non-Event records
    Parameters
    ----------
    self object

    Returns event rate
        non event rate
        woe value
    -------

    """
    if self.event_count == 0 or self.non_event_count == 0:
        event_rate = 1.0 * (self.event_count + self.adjustment_factor) / max(self.event_total, 1)
        non_event_rate = 1.0 * (self.non_event_count + self.adjustment_factor) / max(self.non_event_total, 1)
    else:
        event_rate = 1.0 * self.event_count / max(self.event_total, 1)
        non_event_rate = 1.0 * self.non_event_count / max(self.non_event_total, 1)
    woe = math.log(non_event_rate / event_rate)
    return event_rate, non_event_rate, woe
