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


from common.python.utils import log_utils
from kernel.base.statics import MultivariateStatisticalSummary
from kernel.components.binning.core import binning_util
from kernel.components.binning.core.base_binning import Binning
from kernel.components.binning.horzfeaturebinning.param import HorzFeatureBinningParam

LOGGER = log_utils.get_logger()


class VirtualSummaryBinningBase(Binning):
    def fit_split_points(self, data_instances):
        pass

    def __init__(self, params: HorzFeatureBinningParam = None, abnormal_list=None, allow_duplicate=False):
        super(VirtualSummaryBinningBase, self).__init__()
        super().__init__(params, abnormal_list)
        self.allow_duplicate = allow_duplicate
        self.total_count = 0
        self.missing_count = 0
        self.transfer_variable = None
        self.query_points, self.global_ranks, self.max_values, self.min_values = None, None, None, None

        self.provider_other_inner_id = None
        self.provider_master_inner_id = None
        self.provider_inner_id = None

        self.member_id = None
        self.mix_promoter_member_id = None

    def set_transfer_variable(self, transfer_variable):
        self.transfer_variable = transfer_variable

    def set_provider_param(self, provider_other_inner_id=None, provider_master_inner_id=None, provider_inner_id=None,
                           member_id=None, mix_promoter_member_id=None):
        self.provider_other_inner_id = provider_other_inner_id
        self.provider_master_inner_id = provider_master_inner_id
        self.provider_inner_id = provider_inner_id
        self.member_id = member_id
        self.mix_promoter_member_id = mix_promoter_member_id

    def _get_min_max(self, data_instances):
        statistic_obj = MultivariateStatisticalSummary(data_instances,
                                                       cols_index=self.bin_inner_param.bin_indexes,
                                                       abnormal_list=self.abnormal_list)
        max_values = statistic_obj.get_max()
        min_values = statistic_obj.get_min()

        self.max_values = [max_values[x] for x in self.bin_inner_param.bin_names]
        self.min_values = [min_values[x] for x in self.bin_inner_param.bin_names]

        return self.max_values, self.min_values

    @staticmethod
    def _get_missing_count(summary_table):
        missing_table = summary_table.mapValues(lambda x: x.missing_count)
        return dict(missing_table.collect())

    @staticmethod
    def _query_values(summary_table, query_points, need_send=False):
        local_ranks = summary_table.join(query_points, binning_util.query_table, need_send=need_send)
        return local_ranks

    def _calc_all_split_points(self):
        return binning_util.calc_query_point(self)
