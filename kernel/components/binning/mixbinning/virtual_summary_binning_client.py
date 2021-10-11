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


from common.python.utils import log_utils
from kernel.components.binning.core import binning_util
from kernel.components.binning.core.quantile_binning import QuantileBinningTool
from kernel.components.binning.mixbinning.virtual_summary_binning_base import VirtualSummaryBinningBase

LOGGER = log_utils.get_logger()


class VirtualSummaryBinningClient(VirtualSummaryBinningBase):
    def __init__(self, model_param):
        super(VirtualSummaryBinningClient, self).__init__(params=model_param)

    def fit(self, data_instances):
        if self.bin_inner_param is None:
            self._setup_bin_inner_param(data_instances, self.params)
        local_count = data_instances.count()
        self.transfer_variable.data_count.remote(local_count,
                                                 suffix=(self.provider_inner_id, self.provider_master_inner_id),
                                                 member_id_list=[self.member_id])
        self.total_count = self.transfer_variable.total_count.get(
            suffix=(self.provider_master_inner_id, self.provider_inner_id), member_id_list=[self.member_id])[0]

        quantile_tool = QuantileBinningTool(param_obj=self.params,
                                            abnormal_list=self.abnormal_list,
                                            allow_duplicate=self.allow_duplicate)
        quantile_tool.set_bin_inner_param(self.bin_inner_param)

        summary_table = quantile_tool.fit_summary(data_instances)

        self._get_min_max(data_instances)
        self.transfer_variable.min_max.remote({'min': self.min_values, 'max': self.max_values},
                                              suffix=(self.provider_inner_id, self.provider_master_inner_id),
                                              member_id_list=[self.member_id])
        all_min_max = self.transfer_variable.all_min_max.get(
            suffix=(self.provider_master_inner_id, self.provider_inner_id),
            member_id_list=[self.member_id])[0]
        self.min_values = all_min_max.get('min')
        self.max_values = all_min_max.get('max')

        missing_count = self._get_missing_count(summary_table)
        self.transfer_variable.missing_count.remote(missing_count,
                                                    suffix=(self.provider_inner_id, self.provider_master_inner_id),
                                                    member_id_list=[self.member_id])
        self.missing_count = self.transfer_variable.total_missing_count.get(
            suffix=(self.provider_master_inner_id, self.provider_inner_id), member_id_list=[self.member_id])[0]

        self.query_points = binning_util.init_query_points(self, summary_table._partitions,
                                                           split_num=self.params.sample_bins)
        local_ranks = self._query_values(summary_table, self.query_points, need_send=True)
        self.transfer_variable.local_ranks.remote(local_ranks,
                                                  suffix=(self.provider_inner_id, self.provider_master_inner_id),
                                                  member_id_list=[self.member_id])
        self.global_ranks = self.transfer_variable.global_ranks.get(
            suffix=(self.provider_master_inner_id, self.provider_inner_id), member_id_list=[self.member_id])[0]

    def fit_split_points(self, data_instances):
        self.fit(data_instances)

        return self._calc_all_split_points()
