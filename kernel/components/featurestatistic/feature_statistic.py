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

import json
from collections import Counter

import numpy as np

from common.python.db.db_models import DataSetColumn
from common.python.utils import log_utils
from kernel.base.instance import Instance
from kernel.base.sparse_vector import SparseVector
from kernel.base.statistics import get_statistics_value, change_json_shape
from kernel.components.binning.core.bucket_binning import BucketBinning
from kernel.model_base import ModelBase
from kernel.transfer.variables.transfer_class.feature_statistic_transfer_variable import \
    FeatureStatisticTransferVariable
from kernel.utils import consts, data_util
from kernel.utils.consts import FeatureStatisticWorkMode
from kernel.utils.decorator_utils import update_task_status_env

LOGGER = log_utils.get_logger()


class FeatureStatistic(ModelBase):

    def __init__(self):
        super(FeatureStatistic, self).__init__()
        self.reader = None
        from kernel.components.featurestatistic.param import FeatureStatisticParam
        self.model_param = FeatureStatisticParam()
        self.task_result_type = "data_feature_statistic"
        self.members_feature_statistic = {"members": []}

        self.delimiter = self.model_param.delimiter
        self.label_name = self.model_param.label_name
        self.data_type = self.model_param.data_type
        self.label_type = self.model_param.label_type
        self.work_mode = self.model_param.work_mode
        self.percentage_list = self.model_param.percentage_list
        self.unique_count_threshold = self.model_param.unique_count_threshold

        self.header = None
        self.with_label = None
        self.sid_name = None
        self.label_idx = None
        self.default_col_num = 10
        self.data_set_id = None
        self.schema = None
        self.header_dict = {}

    def _init_custom_settings(self):
        if len(self.model_param.percentage_list) != 0:
            self.percentage_list = self.model_param.percentage_list
        self.delimiter = self.model_param.delimiter
        self.label_name = self.model_param.label_name
        self.data_type = self.model_param.data_type
        self.label_type = self.model_param.label_type
        self.work_mode = self.model_param.work_mode
        self.unique_count_threshold = self.model_param.unique_count_threshold

    def generate_header(self, data_table):

        header = data_table.get_meta('header')
        sid_name = data_table.get_meta('sid')
        if sid_name is None:
            schema = data_table.get_meta('schema')
            if schema is not None:
                sid_name = schema['sid_name']

        print("header is {}".format(header))
        print("sid_name is {}".format(sid_name))

        if not header and not sid_name:
            raise ValueError("Data Exception!!! Don't have header or sid_name.")

        if self.with_label:
            self.label_idx = header.split(self.delimiter, -1).index(self.label_name)
            header_gen = header.split(self.delimiter, -1)[: self.label_idx] + \
                         header.split(self.delimiter, -1)[self.label_idx + 1:]
        else:
            if header and isinstance(header, str):
                header_gen = header.split(self.delimiter, -1)
            elif header and isinstance(header, list):
                header_gen = header
            else:
                header_gen = None

        self.header = header_gen
        self.sid_name = sid_name
        for feature_name in header_gen:
            self.header_dict[feature_name] = {}

    def check_if_with_label(self, data_set_id):

        with_label = DataSetColumn.check_if_with_label_by_data_set_id(data_set_id)
        if with_label is None:
            self.with_label = False
        else:
            self.with_label = True

        print('if have label y: {}'.format(self.with_label))

    def convert_to_instance(self, data_table, date_set_id):

        self.data_set_id = date_set_id

        input_data_labels = None

        self.check_if_with_label(date_set_id)

        self.generate_header(data_table)

        if self.with_label:
            data_shape = data_util.get_data_shape(data_table)
            if not data_shape or self.label_idx >= data_shape:
                raise ValueError("Data Exception!!! The data is empty.")

            input_data_features = data_table.mapValues(
                lambda value: [] if data_shape == 1 else value.split(self.delimiter, -1)[
                                                         :self.label_idx] + value.split(
                    self.delimiter, -1)[self.label_idx + 1:])
            input_data_labels = data_table.mapValues(lambda value: value.split(self.delimiter, -1)[self.label_idx])
        else:
            input_data_features = data_table.mapValues(
                lambda value: [] if not value else value.split(self.delimiter, -1))

        data_instance = self.generate_data_instance(input_data_features, input_data_labels)

        data_instance.schema = self.get_scheme(self.header, self.sid_name, self.label_name)
        return data_instance

    def generate_data_instance(self, input_data_features, input_data_labels):

        if self.with_label:
            # join: (k,)
            data_instance = input_data_features.join(input_data_labels,
                                                     lambda features, label:
                                                     self.to_instance(features, label))
        else:
            data_instance = input_data_features.mapValues(lambda features: self.to_instance(features))

        return data_instance

    def to_instance(self, features, label=None):

        if self.with_label:
            if self.label_type == 'int':
                label = int(label)
            elif self.label_type in ["float", "float64"]:
                label = float(label)

            features = FeatureStatistic.generate_output_format(features, self.data_type)

        else:
            features = FeatureStatistic.generate_output_format(features, self.data_type)

        return Instance(inst_id=None,
                        features=features,
                        label=label)

    @staticmethod
    def generate_output_format(features, data_type=float, exclusive_data_type_fid_map=None, output_format='dense',
                               missing_impute=None):
        if output_format not in ["dense", "sparse"]:
            raise ValueError("output format {} is not define".format(output_format))

        if output_format == "dense":
            if data_type in ["int", "int64", "long", "float", "float64", "double"]:
                for i in range(len(features)):
                    if (missing_impute is not None and features[i] in missing_impute) or \
                            (missing_impute is None and features[i] in ['', 'NULL', 'null', "NA"]):
                        features[i] = np.nan

            if exclusive_data_type_fid_map:
                for fid in range(len(features)):
                    if fid in exclusive_data_type_fid_map:
                        dtype = exclusive_data_type_fid_map[fid]
                    else:
                        dtype = data_type

                    features[fid] = getattr(np, dtype)(features[fid])

                return np.asarray(features, dtype=object)
            else:
                return np.asarray(features, dtype=data_type)

        indices = []
        data = []
        column_shape = len(features)
        non_zero = 0

        for i in range(column_shape):
            if (missing_impute is not None and features[i] in missing_impute) or \
                    (missing_impute is None and features[i] in ['', 'NULL', 'null', "NA"]):
                indices.append(i)
                data.append(np.nan)
                non_zero += 1

            if data_type in ['float', 'float64', "double"]:
                if np.fabs(float(features[i])) < consts.FLOAT_ZERO:
                    continue

                indices.append(i)
                data.append(float(features[i]))
                non_zero += 1

            elif data_type in ['int', "int64", "long"]:
                if int(features[i]) == 0:
                    continue
                indices.append(i)
                data.append(int(features[i]))

            else:
                indices.append(i)
                data.append(features[i])

        return SparseVector(indices, data, column_shape)

    def get_scheme(self, header=None, sid_name=None, label_name=None):

        schema = {}
        if header:
            schema["header"] = header

        if sid_name:
            schema["sid_name"] = sid_name

        if label_name:
            schema["label_name"] = label_name

        self.schema = schema

        return schema

    def count_distribution(self, data_instances):
        binning = BucketBinning()
        binning.abnormal_list = {}
        binning.bin_num = self.default_col_num
        split_point = binning.fit_split_points(data_instances)
        data_bin_dict = binning.get_data_bin(data_instances, split_point)
        count = data_bin_dict.count()
        data_bin = data_bin_dict.take(count)
        data_bin_list = [data_bin[i][1] for i in range(count)]
        col_list_dict = {}
        scheme_header = data_instances.schema['header']
        for x in scheme_header:
            col_list_dict[x] = []
        for x in scheme_header:
            for i in range(count):
                col_list_dict[x].append(data_bin_list[i].get(x))
        for x in scheme_header:
            col_list_dict[x] = Counter(col_list_dict[x])
            col_list_dict[x] = sorted(col_list_dict.get(x).items(), key=lambda item: item[0])
            col_list_dict[x] = dict(col_list_dict[x])

        result_json = json.dumps(col_list_dict)
        # print('Distribution Result：{}'.format(col_list_dict))
        return col_list_dict, count

    @update_task_status_env()
    def update_data_set_column(self, col_list_dict, data_set_id):
        for x in self.schema['header']:
            result = col_list_dict[x]
            # if DataSetColumn.get_column_type_by_data_set_id_and_name(data_set_id, x.strip()) == 'Double':
            #     cache_list = list(map(list, col_list_dict[x]))
            #     result = {'distribution': cache_list}
            DataSetColumn.update_value_distribution_by_data_set_id_and_name(
                self.data_set_id, x.strip(), json.dumps(result))

    def single_fit(self, origin_data=None, data_instances=None, percentage_list=None):
        self._init_custom_settings()
        feature_statistics = {}
        if data_instances is None:
            data_set_id = origin_data.get_name().split('_')[2]
            data_instance = self.convert_to_instance(origin_data, data_set_id)
            result_json, count = self.count_distribution(data_instance)
        else:
            result_json, count = self.count_distribution(data_instances)
            for feature_name in data_instances.get_meta('header'):
                self.header_dict[feature_name] = {}
        feature_statistics["distribution"] = result_json
        if data_instances is None:
            feature_statistics.update(get_statistics_value(
                origin_data=origin_data,
                count=count,
                unique_count_threshold=self.unique_count_threshold,
                percentage_list=percentage_list
            ))
        else:
            feature_statistics.update(
                get_statistics_value(
                    data_instances=data_instances,
                    percentage_list=percentage_list,
                    unique_count_threshold=self.unique_count_threshold,
                    count=count))
        print("{}:{}".format("feature_statistic", json.dumps(feature_statistics)))
        feature_statistics = change_json_shape(feature_statistics, self.header_dict)
        feature_statistics = {"member_id": self.member_id, "role": self.role, "feature_statistic": feature_statistics}
        self.members_feature_statistic["members"].append(feature_statistics)
        return self.members_feature_statistic

    def various_fit(self, input_data):
        self._init_custom_settings()
        if self.work_mode == FeatureStatisticWorkMode.AUTO:
            feature_statistics = self.single_fit(origin_data=input_data)
            data_set_id = input_data.get_name().split('_')[2]
            self.update_data_set_column(feature_statistics, data_set_id)

        elif self.work_mode == FeatureStatisticWorkMode.FEDERATION:
            self.transfer_variable.percentage_list.remote(self.percentage_list, consts.PROVIDER, idx=-1)
            LOGGER.info("Enter the feature statistic's fit method")
            prom_fs = self.single_fit(data_instances=input_data, percentage_list=self.percentage_list)
            prov_fs = self.transfer_variable.feature_statistic_result.get(idx=-1)
            if isinstance(prov_fs, list):
                for item in prov_fs:
                    self.members_feature_statistic["members"].append(item['members'][0])
            # result = {'promoter': prom_fs, 'provider': prov_fs}
            else:
                self.members_feature_statistic["members"].append(prov_fs['members'][0])
        else:
            self.single_fit(data_instances=input_data, percentage_list=self.percentage_list)


class FeatureStatisticPromoter(FeatureStatistic):

    def __init__(self):
        super().__init__()
        self.transfer_variable = FeatureStatisticTransferVariable()

    def fit(self, input_data):
        self.various_fit(input_data)
        self.tracker.save_task_result(self.members_feature_statistic, self.task_result_type)


class FeatureStatisticProvider(FeatureStatistic):

    def __init__(self):
        super(FeatureStatisticProvider, self).__init__()
        self.transfer_variable = FeatureStatisticTransferVariable()

    def fit(self, input_data):
        self._init_custom_settings()
        if self.work_mode == FeatureStatisticWorkMode.LOCAL:
            return
        self.percentage_list = self.transfer_variable.percentage_list.get(idx=0)
        LOGGER.info("Enter the feature statistic's fit method")
        prov_fs = self.single_fit(data_instances=input_data, percentage_list=self.percentage_list)
        self.transfer_variable.feature_statistic_result.remote(prov_fs, role=consts.PROMOTER, idx=0)
        self.tracker.save_task_result(self.members_feature_statistic, self.task_result_type)
