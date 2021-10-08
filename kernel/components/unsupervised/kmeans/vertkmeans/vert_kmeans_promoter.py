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

import numpy as np

from common.python.utils import log_utils
from kernel.components.unsupervised.kmeans.vertkmeans.vert_kmeans_base import VertKmeansBase
from kernel.metrics import clustering_metric
from kernel.utils import abnormal_detection, consts
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()


class VertKmeansPromoter(VertKmeansBase):

    def __init__(self):
        super(VertKmeansPromoter, self).__init__()
        self.DBI = 0
        self.ari = 0
        self.ami = 0
        self.h_score = 0
        self.c_score = 0
        self.v_measure = 0
        self.fmi = 0

    def fit(self, data_instances):
        LOGGER.info("start kmeans")
        self._init_params()
        self.header = get_header(data_instances)
        abnormal_detection.empty_table_detection(data_instances)
        if self.k > data_instances.count() or self.k < 2:
            raise ValueError('K is too larger or too small for current data')

        np.random.seed(self.random_stat)
        first_center_id_key = self.get_center_id(data_instances)
        self.transfer_variable.init_center.remote(first_center_id_key, role=consts.PROVIDER)
        self.center_list = self.first_center(first_center_id_key, data_instances)
        LOGGER.info(f'{self.n_iter_}, {self.center_list}')
        while self.n_iter_ < self.max_iter:
            promoter_dist_all_table = self.compute_dist_all_table(data_instances, self.center_list)
            provider_dist_all_tables = self.transfer_variable.provider_dist.get(idx=-1, suffix=(self.n_iter_,))
            dist_sum = promoter_dist_all_table
            for provider_dist_all_table in provider_dist_all_tables:
                dist_sum.join(provider_dist_all_table, lambda x1, x2: x1 + x2)
            cluster_result = dist_sum.mapValues(lambda v: np.argmin(v), need_send=True)
            self.transfer_variable.cluster_result.remote(cluster_result, suffix=(self.n_iter_,))
            center_new, self.cluster_count = self.center_cal(cluster_result, data_instances)

            promoter_tol = np.sum(np.sum((np.array(self.center_list) - np.array(center_new)) ** 2, axis=1))
            provider_tols = self.transfer_variable.provider_tol.get(idx=-1, suffix=(self.n_iter_,))
            tol_sum = promoter_tol
            for provider_tol in provider_tols:
                tol_sum += provider_tol
            self.is_converged = True if tol_sum < self.tol else False
            self.transfer_variable.converged_result.remote(self.is_converged, suffix=(self.n_iter_,))

            self.center_list = center_new
            self.cluster_result = cluster_result
            LOGGER.info(
                f'{self.n_iter_}, tol_sum={tol_sum}, is_converged={self.is_converged}, center_list={self.center_list}, '
                f'cluster_result={self.cluster_result.first()}')
            self.calc_cluster_target(data_instances, dist_sum, self.cluster_result, suffix=(self.n_iter_,))
            if self.is_converged:
                break

            self.n_iter_ += 1

        self._callback()
        return self.cluster_result

    def get_center_id(self, data_instances):
        random_key = []
        key = list(data_instances.mapValues(lambda data_instance: None).collect())
        random_list = list(np.random.choice(data_instances.count(), self.k, replace=False))
        for k in random_list:
            random_key.append(key[k][0])
        return random_key

    def calc_cluster_target(self, data_instances, dist_sum, cluster_result, suffix):
        self.calc_dbi(dist_sum, cluster_result, suffix)
        self.calc_external(data_instances)

    def calc_dbi(self, dist_sum, cluster_result, suffix):
        dist_cluster_table = dist_sum.join(cluster_result, lambda v1, v2: [v1, v2])
        dist_table = self.calc_ave_dist(dist_cluster_table, cluster_result)  # ave dist in each cluster
        if len(dist_table) == 1:
            raise ValueError('Only one class detected. DBI calculation error')
        promoter_center_dist = self.center_dist(self.center_list)
        provider_center_dists = self.transfer_variable.provider_center_dist.get(idx=-1, suffix=suffix)
        center_dist = promoter_center_dist
        for provider_center_dist in provider_center_dists:
            center_dist += provider_center_dist
        cluster_avg_intra_dist = []
        for i in range(len(dist_table)):
            cluster_avg_intra_dist.append(dist_table[i][2])
        self.DBI = clustering_metric.DaviesBouldinIndex.compute(self, cluster_avg_intra_dist, center_dist)
        LOGGER.info(f'dbi={self.DBI}')

    def calc_external(self, data_instances):
        result = data_instances.join(self.cluster_result, lambda v1, v2: (v1.label, v2))
        labels = list(result.collect())
        labels_true = []
        labels_pred = []
        for label in labels:
            labels_true.append(label[1][0])
            labels_pred.append(label[1][1])
        self.ari = clustering_metric.AdjustedRandScore.compute(self, labels_true, labels_pred)
        self.ami = clustering_metric.AdjustedMutualInfoScore.compute(self, labels_true, labels_pred)
        self.h_score = clustering_metric.HomogeneityScore.compute(self, labels_true, labels_pred)
        self.c_score = clustering_metric.CompletenessScore.compute(self, labels_true, labels_pred)
        self.v_measure = clustering_metric.VMeasureScore.compute(self, labels_true, labels_pred)
        self.fmi = clustering_metric.FowlkesMallowsScore.compute(self, labels_true, labels_pred)
        LOGGER.info(
            f'ari={self.ari}, ami={self.ami}, h_score={self.h_score}, c_score={self.c_score}, '
            f'v_measure={self.v_measure}, fmi={self.fmi}')

    def calc_ave_dist(self, dist_cluster_table, cluster_result):
        dist_centroid_dist_table = dist_cluster_table.applyPartitions(self.sum_in_cluster).reduce(self.sum_dict)
        cluster_count = cluster_result.applyPartitions(self.count).reduce(self.sum_dict)
        calc_ave_dist_list = []
        for key in cluster_count.keys():
            count = cluster_count[key]
            calc_ave_dist_list.append([key, count, dist_centroid_dist_table[key] / count])
        return calc_ave_dist_list

    def sum_in_cluster(self, iterator):
        sum_result = dict()
        for k, v in iterator:
            if v[1] not in sum_result:
                sum_result[v[1]] = np.sqrt(v[0][v[1]])
            else:
                sum_result[v[1]] += np.sqrt(v[0][v[1]])
        return sum_result

    def _callback(self):
        data = {'dbi': self.DBI, 'ari': self.ari, 'ami': self.ami, 'h_score': self.h_score, 'c_score': self.c_score,
                'v_measure': self.v_measure, 'fmi': self.fmi}
        metric_data = [('kmeans', data)]
        self.tracker.saveMetricData(self.metric_name, self.metric_namespace, None, metric_data)
