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

from common.python.utils import log_utils
from kernel.components.binning.core import binning_util
from kernel.components.binning.core.quantile_binning import QuantileBinningTool
from kernel.components.binning.horzfeaturebinning import horz_binning_base
from kernel.components.binning.horzfeaturebinning.param import HorzFeatureBinningParam
from kernel.transfer.framework.horz.procedure import table_aggregator

LOGGER = log_utils.get_logger()


class Server(horz_binning_base.Server):
    def __init__(self, params=None, abnormal_list=None, table_transVar=None,
                 clients=None):
        super().__init__(params, abnormal_list)
        self.tableScatterTransVar = table_transVar
        self.clients = clients

    def fit_split_points(self, data=None):
        if self.aggregator is None:
            self.aggregator = table_aggregator.Server(enable_secure_aggregate=True,
                                                      trans_var=self.tableScatterTransVar)
        self.get_total_count()
        self.get_min_max(self.clients)
        self.get_missing_count()
        self.query_values()
        self.calc_event()


class Client(horz_binning_base.Client):
    def __init__(self, params: HorzFeatureBinningParam = None, abnormal_list=None, allow_duplicate=False):
        super().__init__(params, abnormal_list)
        self.allow_duplicate = allow_duplicate
        self.query_points = None
        self.global_ranks = None
        self.total_count = 0
        self.missing_count = 0

    def fit(self, data_inst):
        if self.bin_inner_param is None:
            self._setup_bin_inner_param(data_inst, self.params)
        self.total_count = self.get_total_count(data_inst)
        LOGGER.debug(f"abnormal_list: {self.abnormal_list}, total_count={self.total_count}")

        quantile_tool = QuantileBinningTool(param_obj=self.params,
                                            abnormal_list=self.abnormal_list,
                                            allow_duplicate=self.allow_duplicate)
        quantile_tool.set_bin_inner_param(self.bin_inner_param)

        summary_table = quantile_tool.fit_summary(data_inst)

        self.get_min_max(data_inst)
        LOGGER.debug(f'summary_table={summary_table.first()}')
        self.missing_count = self.get_missing_count(summary_table)
        LOGGER.debug(f'missing_count={self.missing_count}')
        self.query_points = binning_util.init_query_points(self, summary_table._partitions,
                                                           split_num=self.params.sample_bins)
        LOGGER.debug(f'query_points={self.query_points.first()}')
        self.global_ranks = self.query_values(summary_table, self.query_points)
        LOGGER.debug(f'global_ranks={self.global_ranks.first()}')

    def fit_split_points(self, data_instances):
        if self.aggregator is None:
            self.aggregator = table_aggregator.Client(enable_secure_aggregate=True)
        self.fit(data_instances)

        return binning_util.calc_query_point(self)
