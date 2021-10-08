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

import math

import numpy as np

from kernel.base.statistics import get_statistics_value
from kernel.utils import consts
from kernel.utils.data_util import get_label_count

result_magnitude_names = ['mean', 'variance', 'std_variance', 'skewness', 'kurtosis', 'not_null_count',
                          'missing_count', 'count', 'max', 'min']

transfer_magnitude_names = ['m', 'm2', 'm3', 'm4', 'count', 'not_null_count', 'missing_count']


def load_data(data_instance):
    # Here suppose this is a binary question and the event label is 1
    if data_instance.label != 1:
        data_instance.label = 0
    return data_instance


def calc_label_statistic(data_instances):
    new_data_instances = data_instances.mapValues(lambda v: load_data(v))
    return get_label_count(new_data_instances)


def calc_local_statistic(data_instances, is_vert=False):
    return get_statistics_value(data_instances=data_instances, is_vert=is_vert)


def calc_skewness(m, m2, m3, count):
    """
    Returns:
        skewness
    """
    m = m / count
    m2 = m2 / count
    m3 = m3 / count
    mu = m
    sigma = np.sqrt(m2 - mu * mu)
    skewness = (m3 - 3 * mu * m2 + 2 * mu ** 3) / sigma ** 3
    return [0 if np.isnan(x) else x for x in skewness]


def calc_kurtosis(m, m2, m3, m4, count):
    """
    Returns:
        kurtosis
    """
    m = m / count
    m2 = m2 / count
    m3 = m3 / count
    m4 = m4 / count
    mu = m
    sigma = np.sqrt(m2 - mu * mu)
    kurtosis = (m4 - 4 * mu * m3 + 6 * mu * mu * m2 - 4 * mu ** 3 * mu + mu ** 4) / sigma ** 4 - 3
    return [0 if np.isnan(x) else x for x in kurtosis]


def calc_magnitude_result(merge_magnitude_result: dict):
    m = np.array(merge_magnitude_result.get('m'))
    m2 = np.array(merge_magnitude_result.get('m2'))
    m3 = np.array(merge_magnitude_result.get('m3'))
    m4 = np.array(merge_magnitude_result.get('m4'))
    count = np.array(merge_magnitude_result.get('count'))
    mean = m / count
    variance = m2 / count - mean ** 2
    std_variance = np.sqrt(variance)
    skewness = calc_skewness(m, m2, m3, count)
    kurtosis = calc_kurtosis(m, m2, m3, m4, count)
    merge_magnitude_result['mean'] = mean.tolist()
    merge_magnitude_result['variance'] = variance.tolist()
    merge_magnitude_result['std_variance'] = std_variance.tolist()
    merge_magnitude_result['skewness'] = skewness
    merge_magnitude_result['kurtosis'] = kurtosis
    return merge_magnitude_result


def generate_statistic_result(merge_magnitude_result: dict, idx):
    value = {'mode': [], 'percentile': {}, 'row': 0, 'unique_count': {}, 'distribution': {}}
    for magnitude_name in result_magnitude_names:
        value[magnitude_name] = merge_magnitude_result.get(magnitude_name)[idx]
    feature_missing_count = merge_magnitude_result.get('missing_count')[idx]
    feature_count = merge_magnitude_result.get('count')[idx]
    value['missing_rate'] = feature_missing_count / feature_count
    feature_mean = merge_magnitude_result.get('mean')[idx]
    feature_std_variance = merge_magnitude_result.get('std_variance')[idx]
    if math.fabs(feature_mean) < consts.FLOAT_ZERO:
        feature_mean = consts.FLOAT_ZERO
    value['cv'] = math.fabs(feature_std_variance / feature_mean)
    return value
