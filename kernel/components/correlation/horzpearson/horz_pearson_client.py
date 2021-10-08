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


import numpy as np

from common.python import session
from common.python.utils import log_utils
from kernel.components.correlation.horzpearson.horz_pearson_base import HorzPearsonBase
from kernel.security.protol.spdz.tensor.fixedpoint_table import table_dot
from kernel.transfer.framework.horz.procedure import table_aggregator

LOGGER = log_utils.get_logger()


class HorzPearsonClient(HorzPearsonBase):
    def __init__(self):
        super(HorzPearsonClient, self).__init__()
        self.aggregator = table_aggregator.Client(enable_secure_aggregate=True)
        self._summary = {}

        self.metric_name = "pearson"
        self.metric_namespace = "statistics"

    def fit(self, data_instances):
        data = self._select_columns(data_instances)
        n, mean, std = self.get_mean_and_std()
        normed = self._standardized(data, mean, std)
        local_corr = table_dot(normed, normed).tolist()
        corr_count = []
        for idx, name in enumerate(self.names):
            corr_count.append((name, local_corr[idx]))
        corr_tables = session.parallelize(corr_count, partition=1, include_key=True)
        self.aggregator.send_table(corr_tables, suffix=('corr',))
        merge_corr_table = self.aggregator.get_aggregated_table(suffix=('corr',))
        merge_corr = []
        for value in list(merge_corr_table.collect()):
            merge_corr.append([x / n for x in value[1].tolist()])

        self._summary['mix_corr'] = merge_corr
        self._summary['mix_feature_names'] = self.names
        metric_data = [("corr", self._summary)]
        LOGGER.debug(f'metric_data: {metric_data}, metric_name:{self.metric_name}')
        self.tracker.saveMetricData(self.metric_name, self.metric_namespace, None, metric_data)
        self.set_summary(self._summary)

    @staticmethod
    def _standardized(data, mu, sigma):
        if (np.array(sigma) <= 0).any():
            raise ValueError(f"zero standard deviation detected, sigma={sigma}")
        return data.mapValues(lambda x: (x - mu) / sigma)

    def _select_columns(self, data_instance):
        col_names = data_instance.schema["header"]
        name_to_idx = {col_names[i]: i for i in range(len(col_names))}
        selected = set()
        for name in self.column_names:
            if name in name_to_idx:
                selected.add(name_to_idx[name])
                continue
            raise ValueError(f"{name} not found")
        selected = sorted(list(selected))
        if len(selected) == len(col_names):
            self.names = col_names
            return data_instance.mapValues(lambda inst: inst.features)

        self.names = [col_names[i] for i in selected]
        return data_instance.mapValues(lambda inst: inst.features[selected])

    def get_mean_and_std(self):
        statistics = self.tracker.get_statics_result()
        LOGGER.debug(f'statistics={statistics}')
        if statistics is None:
            raise ValueError('must run horz statistic before current component')
        mean = []
        std = []
        count = 0
        for name in self.names:
            mean.append(statistics['mean'].get(name))
            std.append(statistics['std_variance'].get(name))
            count = statistics['count']
        return count, mean, std
