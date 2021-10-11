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


import numpy as np

from common.python.utils import log_utils
from kernel.components.binning.core import binning_util
from kernel.components.binning.core.quantile_binning import QuantileBinningTool
from kernel.components.binning.mixbinning.virtual_summary_binning_base import VirtualSummaryBinningBase

LOGGER = log_utils.get_logger()


class VirtualSummaryBinningServer(VirtualSummaryBinningBase):
    def __init__(self, model_param):
        super(VirtualSummaryBinningServer, self).__init__(params=model_param)

    def fit(self, data_instances):
        if self.bin_inner_param is None:
            self._setup_bin_inner_param(data_instances, self.params)
        total_count = data_instances.count()
        for provider_inner_id in self.provider_other_inner_id:
            suffix = (provider_inner_id, self.provider_inner_id)
            total_count += self.transfer_variable.data_count.get(suffix=suffix, member_id_list=[self.member_id])[0]
        LOGGER.debug(f'total_count={total_count}')
        for provider_inner_id in self.provider_other_inner_id:
            suffix = (self.provider_inner_id, provider_inner_id)
            self.transfer_variable.total_count.remote(total_count, suffix=suffix, member_id_list=[self.member_id])
        self.total_count = total_count

        quantile_tool = QuantileBinningTool(param_obj=self.params,
                                            abnormal_list=self.abnormal_list,
                                            allow_duplicate=self.allow_duplicate)
        quantile_tool.set_bin_inner_param(self.bin_inner_param)

        summary_table = quantile_tool.fit_summary(data_instances)

        self._get_min_max(data_instances)
        min_array = [self.min_values]
        max_array = [self.max_values]
        for provider_inner_id in self.provider_other_inner_id:
            suffix = (provider_inner_id, self.provider_inner_id)
            min_max = self.transfer_variable.min_max.get(suffix=suffix, member_id_list=[self.member_id])[0]
            min_array.append(min_max.get('min'))
            max_array.append(min_max.get('max'))
        self.max_values = list(np.max(max_array, axis=0))
        self.min_values = list(np.min(min_array, axis=0))
        for provider_inner_id in self.provider_other_inner_id:
            suffix = (self.provider_inner_id, provider_inner_id)
            self.transfer_variable.all_min_max.remote({'min': self.min_values, 'max': self.max_values}, suffix=suffix,
                                                      member_id_list=[self.member_id])

        missing_count = self._get_missing_count(summary_table)
        for provider_inner_id in self.provider_other_inner_id:
            suffix = (provider_inner_id, self.provider_inner_id)
            other_missing_count = self.transfer_variable.missing_count.get(suffix=suffix,
                                                                           member_id_list=[self.member_id])[0]
            for key, value in missing_count.items():
                value += other_missing_count.get(key)
                missing_count[key] = value
        for provider_inner_id in self.provider_other_inner_id:
            suffix = (self.provider_inner_id, provider_inner_id)
            self.transfer_variable.total_missing_count.remote(missing_count, suffix=suffix,
                                                              member_id_list=[self.member_id])
        self.missing_count = missing_count

        self.query_points = binning_util.init_query_points(self, summary_table._partitions,
                                                           split_num=self.params.sample_bins)
        local_ranks = self._query_values(summary_table, self.query_points)
        global_ranks = local_ranks
        for provider_inner_id in self.provider_other_inner_id:
            suffix = (provider_inner_id, self.provider_inner_id)
            other_local_ranks = self.transfer_variable.local_ranks.get(suffix=suffix,
                                                                       member_id_list=[self.member_id])[0]
            global_ranks = global_ranks.join(other_local_ranks, lambda x1, x2: x1 + x2)
        global_ranks = global_ranks.mapValues(lambda x: np.array(x, dtype=int), need_send=True)
        for provider_inner_id in self.provider_other_inner_id:
            suffix = (self.provider_inner_id, provider_inner_id)
            self.transfer_variable.global_ranks.remote(global_ranks, suffix=suffix,
                                                       member_id_list=[self.member_id])
        self.global_ranks = global_ranks

    def fit_split_points(self, data_instances):
        self.fit(data_instances)

        return self._calc_all_split_points()
