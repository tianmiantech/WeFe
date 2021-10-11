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


from kernel.components.correlation.horzpearson.horz_pearson_base import HorzPearsonBase
from kernel.transfer.framework.horz.procedure import table_aggregator
from kernel.utils import consts


class HorzPearsonArbiter(HorzPearsonBase):
    def __init__(self):
        super(HorzPearsonArbiter, self).__init__()
        self.role = consts.ARBITER
        self.aggregator = table_aggregator.Server(enable_secure_aggregate=True)

    def fit(self, data_instance):
        merge_corr = self.aggregator.add_tables(suffix=('corr',))
        self.aggregator.send_aggregated_tables(merge_corr, suffix=('corr',))
