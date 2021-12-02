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

import functools
import math
import sys
from collections import Counter

import numpy as np

from common.python.utils import log_utils
from kernel.base.instance import Instance
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.horzfeaturebinning.param import FeatureBinningParam
from kernel.utils import consts
from kernel.utils import data_util

LOGGER = log_utils.get_logger()


# Encapsulation for processing single-column raw data
class Statistics(object):
    def __init__(self, abnormal_list=None, unique_count_threshold=20):
        """
        Args:
            abnormal_list: Specify which values are not permitted.
        """
        if abnormal_list is None:
            self.abnormal_list = []
        else:
            self.abnormal_list = abnormal_list

        # Determine whether it is a discrete or continuous threshold, default = 20
        self.unique_count_threshold = unique_count_threshold
        self.beyond = False

        self.sum = 0
        self.sum_square = 0
        self.max_value = - sys.maxsize - 1
        self.min_value = sys.maxsize
        self.count = 0
        # represent each k matrix
        self.m = 0
        self.m2 = 0
        self.m3 = 0
        self.m4 = 0

        # not None count
        self.not_null_count = 0

        # None count
        self.missing_count = 0

        # unique count
        self.unique_count = {}

        self.skewness = 0

        self.kurtosis = 0

    def add_value(self, features, col_index, cols_dict):
        value = features[col_index]
        if value in self.abnormal_list:
            return
        try:
            if value is not None and value != '' and not np.isnan(float(value)):
                value = float(value)
        except ValueError:
            LOGGER.warning(
                f'The feature:{list(cols_dict.keys())[col_index]}, value: {value} cannot be converted to float')

        if value is not None and value != '' and isinstance(value, float) and not np.isnan(value):
            self.count += 1
            self.sum += value
            self.sum_square += value ** 2
            self.m += value
            self.m2 += value ** 2
            self.m3 += value ** 3
            self.m4 += value ** 4
            self.not_null_count += 1

            if value > self.max_value:
                self.max_value = value
            if value < self.min_value:
                self.min_value = value

            if not self.beyond:
                if value not in self.unique_count.keys():
                    self.unique_count[value] = 1
                    if len(self.unique_count) >= self.unique_count_threshold:
                        self.beyond = True
                else:
                    self.unique_count[value] += 1

        elif value is not None and value != '' and isinstance(value, str):
            self.count += 1
            self.not_null_count += 1
            self.max_value = 0
            self.min_value = 0

            if not self.beyond:
                if value not in self.unique_count.keys():
                    self.unique_count[value] = 1
                    if len(self.unique_count) >= self.unique_count_threshold:
                        self.beyond = True
                else:
                    self.unique_count[value] += 1
        else:
            self.missing_count += 1

    def merge(self, other):
        self.count += other.count
        self.sum += other.sum
        self.sum_square += other.sum_square

        self.m += other.m
        self.m2 += other.m2
        self.m3 += other.m3
        self.m4 += other.m4
        self.not_null_count += other.not_null_count
        self.missing_count += other.missing_count

        if self.max_value < other.max_value:
            self.max_value = other.max_value

        if self.min_value > other.min_value:
            self.min_value = other.min_value

        if not self.beyond:
            self.unique_count = dict(Counter(self.unique_count) + Counter(other.unique_count))
            if len(self.unique_count) >= self.unique_count_threshold:
                self.beyond = True

    @property
    def get_mean(self):
        return self.sum / self.count

    @property
    def get_variance(self):
        """

        Returns:
            variance
        """
        mean = self.get_mean
        variance = self.sum_square / self.count - mean ** 2
        if math.fabs(variance) < consts.FLOAT_ZERO:
            return 0.0
        return variance

    @property
    def get_std_variance(self):
        """

        Returns:
            std_variance： standard variance
        """
        if math.fabs(self.get_variance) < consts.FLOAT_ZERO:
            return 0.0
        return math.sqrt(self.get_variance)

    @property
    def get_count(self):
        return self.count

    @property
    def get_m(self):
        return self.m

    @property
    def get_m2(self):
        return self.m2

    @property
    def get_m3(self):
        return self.m3

    @property
    def get_m4(self):
        return self.m4

    @property
    def get_unique_count(self):
        """

        Returns:
            unique_count: unique count
        """
        if not self.beyond:
            return self.unique_count
        else:
            return {}

    @property
    def get_not_null_count(self):
        """

        Returns:
            not_null_count : not Null number
        """
        return self.not_null_count

    @property
    def get_missing_count(self):
        """

        Returns:
            missing_count: null number
        """
        return self.missing_count

    @property
    def get_min_value(self):
        return self.min_value

    @property
    def get_max_value(self):
        return self.max_value

    @property
    def get_skewness(self):
        """

        Returns:
            skewness
        """
        m = self.m / self.count
        m2 = self.m2 / self.count
        m3 = self.m3 / self.count
        mu = m
        sigma = np.sqrt(m2 - mu * mu)
        skewness = (m3 - 3 * mu * m2 + 2 * mu ** 3) / sigma ** 3
        return skewness if not np.isnan(skewness) else 0

    @property
    def get_kurtosis(self):
        """

        Returns:
            kurtosis
        """
        m = self.m / self.count
        m2 = self.m2 / self.count
        m3 = self.m3 / self.count
        m4 = self.m4 / self.count
        mu = m
        sigma = np.sqrt(m2 - mu * mu)
        kurtosis = (m4 - 4 * mu * m3 + 6 * mu * mu * m2 - 4 * mu ** 3 * mu + mu ** 4) / sigma ** 4 - 3
        return kurtosis if not np.isnan(kurtosis) else 0


class MultivariateStatistical(object):

    def __init__(self, origin_data=None, data_instances=None, cols_index=-1, abnormal_list=None,
                 unique_count_threshold=20):
        self.finish_fit_statics = False  # Use for static data
        self.cols_dict = {}
        self.with_label = False
        self.unique_count_threshold = unique_count_threshold

        if origin_data is not None and data_instances is None:
            # dsource -> data_instance
            self.data_instances = self._to_data_instances(origin_data)
        elif origin_data is None and data_instances is not None:
            self.data_instances = data_instances
        else:
            LOGGER.warning('origin_data and data_instances have and only one is not empty !')
            raise ValueError('origin_data and data_instances have and only one is not empty !')

        self.cols_index = cols_index
        self.percentiles = None

        self.mode = None

        if abnormal_list is None:
            self.abnormal_list = []
        else:
            self.abnormal_list = abnormal_list
        self._init_cols(self.data_instances)

    def _to_data_instances(self, data):
        """

        Args:
            data: origin data : [('1','0,-1.2,2.3'),('2','1,2.3,-0.6'),...,]

        Returns:
            data_instances : [('1',<kernel.feature.instance.Instance object at 0x00000000000000>)]
        """
        data_table_meta = data.get_metas()
        with_label = True if 'y' in data_table_meta['header'] else False
        schema = {
            "sid_name": data_table_meta['sid'],
            "header": data_table_meta['header'].split(",") if 'y' not in data_table_meta['header'] else
            data_table_meta['header'].split(",")[1:],
            "label_name": 'y'
        }
        data_instances = data.mapValues(lambda v: self._to_instance(v.split(","), with_label))
        data_instances.schema = schema
        return data_instances

    def _to_instance(self, features, with_label):
        if with_label:
            return Instance(inst_id=None, features=np.array(features[1:]), label=features[0])
        else:
            return Instance(inst_id=None, features=np.array(features), label=None)

    def _init_cols(self, data_instances):

        if len(self.cols_dict) != 0:
            return

        header = data_util.get_header(data_instances)
        self.header = header
        if self.cols_index == -1:
            self.cols = header
            self.cols_index = [i for i in range(len(header))]
        else:
            cols = []
            for idx in self.cols_index:
                try:
                    idx = int(idx)
                except ValueError:
                    raise ValueError("In binning module, selected index: {} is not integer".format(idx))

                if idx >= len(header):
                    raise ValueError(
                        "In binning module, selected index: {} exceed length of data dimension".format(idx))
                cols.append(header[idx])
            self.cols = cols

        self.cols_dict = {}
        for col in self.cols:
            col_index = header.index(col)
            self.cols_dict[col] = col_index

    def _static_sums(self):
        """
        Statics sum, sum_square, max_value, min_value, mean, so that variance is available.
        """
        partition_cal = functools.partial(self.static_in_partition,
                                          cols_dict=self.cols_dict,
                                          abnormal_list=self.abnormal_list,
                                          unique_count_threshold=self.unique_count_threshold)
        summary_statistic_dict = self.data_instances.mapPartitions(partition_cal)
        self.summary_statistics = summary_statistic_dict.reduce(self.aggregate_statics)
        self.finish_fit_statics = True

    @staticmethod
    def static_in_partition(data_instances, cols_dict, abnormal_list, unique_count_threshold):
        """
        Statics sums, sum_square, max and min value through one traversal

        Args
            data_instances : DSource
                The input data

            cols_dict : dict
                Specify which column(s) need to apply statistic.

            abnormal_list: list
                Specify which values are not permitted.

        Returns
        Dict of SummaryStatistics object

        """
        summary_statistic_dict = {}
        for col_name in cols_dict:
            summary_statistic_dict[col_name] = Statistics(abnormal_list, unique_count_threshold)

        for k, instances in data_instances:
            if isinstance(instances, Instance):
                features = instances.features
            else:
                features = instances

            for col_name, col_index in cols_dict.items():
                # value = features[col_index]
                stat_obj = summary_statistic_dict[col_name]
                stat_obj.add_value(features, col_index, cols_dict)

        return summary_statistic_dict

    @staticmethod
    def aggregate_statics(s_dict1, s_dict2):
        if s_dict1 is None and s_dict2 is None:
            return None
        if s_dict1 is None:
            return s_dict2
        if s_dict2 is None:
            return s_dict1

        new_dict = {}
        for col_name, static_1 in s_dict1.items():
            static_1.merge(s_dict2[col_name])
            new_dict[col_name] = static_1
        return new_dict

    def _prepare_data(self, cols_dict, data_type):
        """

        Args
            cols_dict : dict
            Specify which column(s) need to apply statistic.

            data_type : str, "mean", "variance", "std_variance", "max_value" or "mim_value"
                ,"not_null_count", "missing_count", "skewness", "kurtosis"
            Specify which type to show.

        Returns
            return a list of result result. The order is the same as cols.
        """
        if not self.finish_fit_statics:
            self._static_sums()

        if cols_dict is None:
            cols_dict = self.cols_dict

        result = {}
        for col_name, col_index in cols_dict.items():
            if col_name not in self.cols_dict:
                LOGGER.warning("feature {} has not been static yet. Has been skipped".format(col_name))
                continue

            summary_obj = self.summary_statistics[col_name]
            if data_type == 'mean':
                result[col_name] = summary_obj.get_mean
            elif data_type == 'variance':
                result[col_name] = summary_obj.get_variance
            elif data_type == 'max_value':
                result[col_name] = summary_obj.get_max_value
            elif data_type == 'min_value':
                result[col_name] = summary_obj.get_min_value
            elif data_type == 'std_variance':
                result[col_name] = summary_obj.get_std_variance
            elif data_type == 'not_null_count':
                result[col_name] = summary_obj.get_not_null_count
            elif data_type == 'missing_count':
                result[col_name] = summary_obj.get_missing_count
            elif data_type == 'skewness':
                result[col_name] = summary_obj.get_skewness
            elif data_type == 'kurtosis':
                result[col_name] = summary_obj.get_kurtosis
            elif data_type == 'unique_count':
                result[col_name] = summary_obj.get_unique_count
            # elif data_type == 'mode':
            #     result[col_name] = summary_obj.get_mode
            elif data_type == 'm':
                result[col_name] = summary_obj.get_m
            elif data_type == 'm2':
                result[col_name] = summary_obj.get_m2
            elif data_type == 'm3':
                result[col_name] = summary_obj.get_m3
            elif data_type == 'm4':
                result[col_name] = summary_obj.get_m4
            elif data_type == 'count':
                result[col_name] = summary_obj.get_count

        return result

    def get_mean(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result mean.
        """

        return self._prepare_data(cols_dict, "mean")

    def get_variance(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result variance.
        """
        return self._prepare_data(cols_dict, "variance")

    def get_std_variance(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result std_variance.
        """
        return self._prepare_data(cols_dict, "std_variance")

    def get_max(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result max_value.
        """
        return self._prepare_data(cols_dict, "max_value")

    def get_min(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result min_value.
        """
        return self._prepare_data(cols_dict, "min_value")

    def get_count(self, cols_dict=None):
        return self._prepare_data(cols_dict, "count")

    def get_not_null_count(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result not_null_count.
        """
        return self._prepare_data(cols_dict, "not_null_count")

    def get_missing_count(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result missing_count.
        """
        return self._prepare_data(cols_dict, "missing_count")

    def get_skewness(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result skewness.
        """
        return self._prepare_data(cols_dict, "skewness")

    def get_kurtosis(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result kurtosis.
        """
        return self._prepare_data(cols_dict, "kurtosis")

    def get_pearson(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of result kurtosis.
        """
        return self._prepare_data(cols_dict, "pearson")

    def get_unique_count(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
            return a dict of unique count.
        """
        return self._prepare_data(cols_dict, "unique_count")

    def get_mode(self, cols_dict=None):
        """

        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }

        Returns
        -------
             return a dict of mode.
        """

        if cols_dict is None:
            cols_dict = self.cols_dict
        self.mode = self._get_mode(cols_dict)
        for col_name, col_index in cols_dict.items():
            if col_name not in self.mode:
                LOGGER.warning("The column {}, has not set in selection parameters."
                               "mode values is not available".format(col_name))
                continue
        return self.mode

    def _get_mode(self, cols_dict):
        """
            Moore voting method to calculate the mode
        Parameters
        ----------
        cols_dict : dict
           cols_dict = {
               'x0': 0,
               'x1': 1,
               'x3': 3
           }
        Returns
        -------

        """
        col_dict = {}
        # init col_dict
        for col_name, col_index in cols_dict.items():
            col_dict[col_name] = {}
        for data_instance in self.data_instances.collect():
            features = data_instance[1].features
            for col_name, col_index in cols_dict.items():
                if features[col_index] not in col_dict[col_name].keys():
                    col_dict[col_name][features[col_index]] = 1
                else:
                    col_dict[col_name][features[col_index]] += 1

        mode = {}
        for col_name in col_dict.keys():
            mode[col_name] = max(col_dict[col_name], key=col_dict[col_name].get)

        return mode

    def get_m(self, cols_dict=None, d_type='m'):
        return self._prepare_data(cols_dict, d_type)

    def _get_cols_index(self):
        cols_index = []
        for col in self.cols:
            idx = self.cols_dict[col]
            cols_index.append(idx)
        return cols_index

    def get_percentile(self, percentage_list, cols_dict=None, unique_num=None):
        """

        Args:
            cols_dict : dict
               cols_dict = {
                   'x0': 0,
                   'x1': 1,
                   'x3': 3
               }

        Returns: dict of percentiles result
        :param unique_num:

        """
        # percentiles = {}
        # if cols_dict is None:
        #     cols_dict = self.cols_dict

        percentiles_dict = self._get_percentile(percentage_list)

        # for col_name in cols_dict:
        #     if col_name not in self.percentiles:
        #         LOGGER.warning("The column {}, has not set in selection parameters."
        #                        "percentile values is not available".format(col_name))
        #         continue
        #     percentiles[col_name] = self.percentiles[col_name]

        return percentiles_dict


    def _get_percentile(self, percentage_list):
        """
        Percentile index: continuous characteristic index
        :param percentage:
        :return:
        """
        for p in percentage_list:
            if p > 100 or p < 1:
                raise ValueError("percentage must in 1-100, but value is: {}".format(p))
        """

        Args:
            percentage:
                default = 50% , it's mode / second quantile

        Returns:
            return a dict of percentile 
        """
        cols_index = self._get_cols_index()
        # if the type of data < 100, the split_points < 100, cause out of bounds
        bin_param = FeatureBinningParam(bin_num=100, bin_indexes=cols_index)
        binning_obj = QuantileBinning(bin_param, abnormal_list=self.abnormal_list)
        split_points = binning_obj.fit_split_points(self.data_instances)
        percentiles_dict = {}
        log_utils.get_logger().info('分箱点' + ','.join(list(map(str, split_points))))

        for col_name, split_point in split_points.items():
            percentiles = {}
            for p in percentage_list:
                if len(split_point) < 100:
                    percentiles[p] = np.NAN
                else:
                    percentiles[p] = split_point[p - 1]
                percentiles_dict[col_name] = percentiles

        return percentiles_dict

    def get_percentile_dict(self, percentage_list):
        percentile_dict = self.get_percentile(percentage_list=percentage_list)
        # percentile_dict = {}
        # for p in percentage_list:
        #     percentile_dict[p] = self.get_percentile(percentage=p)
        # header_dict = percentile_dict[percentage_list[0]].keys()
        # cache_dict = {}
        # for header_name in header_dict:
        #     cache_dict[header_name] = {}
        #     for p in percentage_list:
        #         cache_dict[header_name].update({p: percentile_dict[p][header_name]})
        # percentile_dict = cache_dict
        return percentile_dict


def get_statistics_value(data_instances=None, origin_data=None, percentage_list=None, count=None, is_vert=True,
                         unique_count_threshold=20):
    if percentage_list is None:
        percentage_list = []
    percentage_list = list(set(percentage_list + [5, 25, 50, 75, 95]))
    statistics_dict = {}
    if data_instances is None and origin_data is None:
        LOGGER.error("Data can not be empty!")
        return
    if data_instances is None:
        statistic = MultivariateStatistical(origin_data=origin_data, unique_count_threshold=unique_count_threshold)
    else:
        statistic = MultivariateStatistical(data_instances=data_instances,
                                            unique_count_threshold=unique_count_threshold)
    statistics_dict['max'] = statistic.get_max()
    statistics_dict['min'] = statistic.get_min()
    statistics_dict['mean'] = statistic.get_mean()
    statistics_dict['variance'] = statistic.get_variance()
    statistics_dict['std_variance'] = statistic.get_std_variance()
    statistics_dict['unique_count'] = statistic.get_unique_count()
    statistics_dict['not_null_count'] = statistic.get_not_null_count()
    statistics_dict['missing_count'] = statistic.get_missing_count()
    statistics_dict['mode'] = statistic.get_mode()
    statistics_dict['kurtosis'] = statistic.get_kurtosis()
    statistics_dict['skewness'] = statistic.get_skewness()
    if is_vert:
        statistics_dict['percentile'] = statistic.get_percentile_dict(percentage_list)
    statistics_dict['m'] = statistic.get_m()
    statistics_dict['m2'] = statistic.get_m(d_type='m2')
    statistics_dict['m3'] = statistic.get_m(d_type='m3')
    statistics_dict['m4'] = statistic.get_m(d_type='m4')
    statistics_dict['count'] = statistic.get_count()
    statistics_dict['row'] = {}
    for key in statistic.cols_dict:
        statistics_dict['row'][key] = count
    # CV = SD / mean * 100%
    statistics_dict['cv'] = {}
    for key in statistic.cols_dict:
        feature_mean = statistics_dict['mean'][key]
        if math.fabs(feature_mean) < consts.FLOAT_ZERO:
            feature_mean = consts.FLOAT_ZERO
        statistics_dict['cv'][key] = \
            math.fabs(statistics_dict['std_variance'][key] / feature_mean)
    return statistics_dict


# change {feature_name : value} >>> {header_name : value}
def change_json_shape(dict_before, header_dict):
    header_feature_dict = generate_feature_dict(header_dict)
    for key in dict_before:
        for header_name in dict_before[key]:
            header_feature_dict[header_name][key] = dict_before[key][header_name]
    return header_feature_dict


def generate_feature_dict(header_dict):
    feature_dict = {
        'distribution': [],
        'max': [],
        'min': [],
        'mean': [],
        'variance': [],
        'std_variance': [],
        'unique_count': [],
        'not_null_count': [],
        'missing_count': [],
        'mode': [],
        'kurtosis': [],
        'skewness': [],
        'percentile': [],
        'row': []
    }
    for key in header_dict.keys():
        header_dict[key].update(feature_dict)
    return header_dict
