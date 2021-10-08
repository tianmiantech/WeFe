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

import functools

import numpy as np

from common.python import session
from kernel.components.unsupervised.kmeans.vertkmeans.param import VertKMeansParam
from kernel.model_base import ModelBase
from kernel.transfer.variables.transfer_class.vert_kmeans_transfer_variable import VertKmeansTransferVariable


class VertKmeansBase(ModelBase):

    def __init__(self):
        super(VertKmeansBase, self).__init__()
        self.model_param = VertKMeansParam()
        self.transfer_variable = VertKmeansTransferVariable()

        self.n_iter_ = 0
        self.k = 0
        self.max_iter = 0
        self.tol = 0
        self.random_stat = None
        self.iter = iter
        self.center_list = None
        self.cluster_result = None

        self.metric_name = "VertKmeans"
        self.metric_namespace = "train"
        self.metric_type = "Kmeans"

        self.task_result_type = "data_vert_kmeans"

        self.set_show_name("(K-Means)")
        self.source_type = 'VERTKMEANS'

        self.header = None
        self.is_converged = False
        self.cluster_detail = None
        self.cluster_count = None

    def _init_params(self):
        self.k = self.model_param.k
        self.max_iter = self.model_param.max_iter
        self.tol = self.model_param.tol
        self.random_stat = self.model_param.random_stat

    @staticmethod
    def educl_dist(u, center_list):
        result = []
        for c in center_list:
            result.append(np.sum(np.square(np.array(c) - u.features)))
        return np.array(result)

    def compute_dist_all_table(self, data_instances, center_list, need_send=False):
        d = functools.partial(self.educl_dist, center_list=center_list)
        return data_instances.mapValues(d, need_send=need_send)

    @staticmethod
    def count(iterator):
        count_result = dict()
        for k, v in iterator:
            if v not in count_result:
                count_result[v] = 1
            else:
                count_result[v] += 1
        return count_result

    @staticmethod
    def sum_dict(d1, d2):
        temp = dict()
        for key in d1.keys() | d2.keys():
            temp[key] = sum([d.get(key, 0) for d in (d1, d2)])
        return temp

    @staticmethod
    def cluster_sum(iterator):
        cluster_result = dict()
        for k, v in iterator:
            if v[1] not in cluster_result:
                cluster_result[v[1]] = v[0]
            else:
                cluster_result[v[1]] += v[0]
        return cluster_result

    def center_dist(self, center_list):
        cluster_dist_list = []
        for i in range(0, len(center_list)):
            for j in range(0, len(center_list)):
                if j != i:
                    cluster_dist_list.append(np.sum((np.array(center_list[i]) - np.array(center_list[j])) ** 2))
        return cluster_dist_list

    def center_cal(self, cluster_result, data_instances):
        cluster_result_table = data_instances.join(cluster_result, lambda v1, v2: [v1.features, v2])
        centroid_feature_sum = cluster_result_table.applyPartitions(self.cluster_sum).reduce(self.sum_dict)
        cluster_count = cluster_result.applyPartitions(self.count).reduce(self.sum_dict)
        centroid_list = []
        cluster_count_list = []
        count_all = data_instances.count()
        # for k in centroid_feature_sum:
        for k in range(self.k):
            if k not in centroid_feature_sum:
                centroid_list.append(self.center_list[int(k)])
                cluster_count_list.append([k, 0, 0])
            else:
                count = cluster_count[k]
                centroid_list.append(centroid_feature_sum[k] / count)
                cluster_count_list.append([k, count, count / count_all])
        return centroid_list, cluster_count_list

    def first_center(self, first_center_id_key, data_instances):
        key_table = session.parallelize(tuple(zip(first_center_id_key, first_center_id_key)),
                                        partition=data_instances.get_partitions(), include_key=True)
        center_list = list(key_table.join(data_instances, lambda v1, v2: v2.features).collect())
        return [v[1] for v in center_list]
