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
from sklearn.metrics import adjusted_mutual_info_score
from sklearn.metrics import adjusted_rand_score
from sklearn.metrics import fowlkes_mallows_score
from sklearn.metrics import homogeneity_score, completeness_score, v_measure_score
from sklearn.metrics import jaccard_score


class JaccardScore(object):
    """
    Compute jaccard_score
    """

    def compute(self, labels, pred_scores):
        return jaccard_score(labels, pred_scores)


class FowlkesMallowsScore(object):
    """
    Compute fowlkes_mallows_score, as in FMI
    """

    def compute(self, labels, pred_scores):
        return fowlkes_mallows_score(labels, pred_scores)


class AdjustedRandScore(object):
    """
    Compute adjusted_rand_score，as in RI
    """

    def compute(self, labels, pred_scores):
        return adjusted_rand_score(labels, pred_scores)


class AdjustedMutualInfoScore(object):
    """
    Compute adjusted_mutual_info_score，as in ami
    """

    def compute(self, labels, pred_scores):
        return adjusted_mutual_info_score(labels, pred_scores)


class HomogeneityScore(object):
    """
    Compute homogeneity_score，as in h
    """

    def compute(self, labels, pred_scores):
        return homogeneity_score(labels, pred_scores)


class CompletenessScore(object):
    """
    Compute completeness_score，as in c
    """

    def compute(self, labels, pred_scores):
        return completeness_score(labels, pred_scores)


class VMeasureScore(object):
    """
    Compute v_measure_score，as in v_measure
    """

    def compute(self, labels, pred_scores):
        return v_measure_score(labels, pred_scores)


class ContengincyMatrix(object):
    """
    Compute contengincy_matrix
    """

    def compute(self, labels, pred_scores):
        # total_count = len(labels)
        label_predict = list(zip(labels, pred_scores))
        predicted_label = list(range(0, max(pred_scores) + 1))
        unique_true_label = np.unique(labels)
        result_array = np.zeros([len(unique_true_label), max(pred_scores) + 1])
        for v1, v2 in label_predict:
            result_array[v1][v2] += 1
        return result_array, predicted_label, unique_true_label


class DistanceMeasure(object):
    """
    Compute distance_measure
    """

    def compute(self, dist_table, inter_cluster_dist, max_radius):
        max_radius_result = max_radius
        cluster_nearest_result = []
        if len(dist_table) == 1:
            cluster_nearest_result.append(0)
        else:
            for j in range(0, len(dist_table)):
                arr = inter_cluster_dist[j * (len(dist_table) - 1): (j + 1) * (len(dist_table) - 1)]
                smallest_index = list(arr).index(min(arr))
                if smallest_index > j:
                    smallest_index += 1
                cluster_nearest_result.append(smallest_index)
        distance_measure_result = dict()
        for n in range(0, len(dist_table)):
            distance_measure_result[n] = [max_radius_result[n], cluster_nearest_result[n]]
        return distance_measure_result


class DaviesBouldinIndex(object):
    """
        Compute dbi，as in dbi
    """

    def compute(self, dist_table, cluster_dist):
        if len(dist_table) == 1:
            return np.nan
        max_dij_list = []
        d = 0
        for i in range(0, len(dist_table)):
            dij_list = []
            for j in range(0, len(dist_table)):
                if j != i:
                    dij_list.append((dist_table[i] + dist_table[j]) / (cluster_dist[d] ** 0.5))
                    d += 1
            max_dij = max(dij_list)
            max_dij_list.append(max_dij)
        return np.sum(max_dij_list) / len(dist_table)
