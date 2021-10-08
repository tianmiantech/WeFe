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
import json

from common.python.utils import log_utils
from kernel.base.imputer import Imputer
from kernel.base.statistics import MultivariateStatistical
from kernel.utils import data_util
from kernel.utils.data_util import get_header

LOGGER = log_utils.get_logger()


class ImputerMissingFill(Imputer):
    """
    Populates feature columns with missing values

    missing_fill_rule: list, Missing value fill rule in the format:
                       [{"feature_column": "x1","fill_type": "Min"}, {"feature_column": "x2","fill_type": "Max"}]
    """
    # Rules: max value
    RULE_MAX = 'Max'
    # Rules：min value
    RULE_Min = 'Min'
    # Rules：avg value
    RULE_AVG = 'Avg'
    # Rules：mode value
    RULE_MODE = 'Mode'
    # Rules：custom
    RULE_CUSTOM = 'Custom'

    RULE_FEATURE_NAME_KEY = 'feature_column'
    RULE_FILL_TYPE_KEY = 'fill_type'
    RULE_FILL_VALUE_KEY = 'file_value'

    def __init__(self, missing_value_list=None, missing_fill_rule=None):
        Imputer.__init__(self, missing_value_list)
        if missing_fill_rule is not None:
            if len(missing_fill_rule) == 0:
                self.missing_fill_rule = []
            else:
                self.missing_fill_rule = json.loads(missing_fill_rule)
        else:
            self.missing_fill_rule = None

        """
        Supported fill rules
        Max: max value
        Min: min value
        Avg: avg value
        Mode: mode value
        Custom: custom
        """
        self.support_missing_fill_rule = [ImputerMissingFill.RULE_MAX, ImputerMissingFill.RULE_Min,
                                          ImputerMissingFill.RULE_AVG, ImputerMissingFill.RULE_MODE,
                                          ImputerMissingFill.RULE_CUSTOM]

    def fit_replace(self, data, replace_method, replace_value=None, output_format=None, quantile=None):
        """
        Override parent method
        """
        """data：tup format：('39', ['-0.153073', '0.055819', '0.001155', '-0.246430', '1.255083', '1.070209'])"""
        if self.missing_fill_rule is None or len(self.missing_fill_rule) == 0:
            """If no fill rule is defined, the original method is called directly"""
            return super().fit_replace(data, replace_method, replace_value, output_format,
                                       quantile)
        """
        The null value replacement rule counts the maximum and minimum values of each feature column....
        """
        # Header, format：['x0', 'x1', 'x2']
        header = get_header(data)
        print(header)
        # Complete replacement rule
        whole_missing_fill_rule = self.__check_missing_fill_rule(header, replace_value)

        summary_obj = MultivariateStatistical(data_instances=data, cols_index=-1,
                                              abnormal_list=self.missing_value_list)

        """
        All feature word values of each rule (key: rule name, value: feature value list corresponding to rule name)
        format：{'Min': [-1.726901, -2.223994, -1.693361], 'Max': [4.094189, 3.885905, 4.287337], 'Avg': [-0.0, 0.0, 0.0]}
        """
        summary_statics_dict = {}
        for rule in whole_missing_fill_rule:
            fill_type = rule[ImputerMissingFill.RULE_FILL_TYPE_KEY]
            if fill_type != ImputerMissingFill.RULE_CUSTOM and fill_type not in summary_statics_dict:
                summary_statics_dict[fill_type] = ImputerMissingFill.__get_cols_summary_statics_value(summary_obj,
                                                                                                      header, fill_type)

        f = functools.partial(ImputerMissingFill.__replace_missing_value_with_rule,
                              summary_statics_dict=summary_statics_dict,
                              header=header,
                              missing_value_list=self.missing_value_list,
                              whole_missing_fill_rule=whole_missing_fill_rule)

        replace_missing_data = data.mapValues(f)
        LOGGER.info("finish replace missing value with missing_fill_rule: {}, summary_statics_dict: {}".format(
            self.missing_fill_rule, whole_missing_fill_rule))

        shape = data_util.get_data_shape(data)
        replace_value = [replace_value for _ in range(shape)]

        return replace_missing_data, replace_value

    @staticmethod
    def __replace_missing_value_with_rule(data_row, summary_statics_dict, header, missing_value_list,
                                          whole_missing_fill_rule):
        """
        Populating a dataset with population rules
        :return:
        """
        # Find replacement column index
        missing_fill_index_dict = {}
        for rule in whole_missing_fill_rule:
            feature_index = header.index(rule[ImputerMissingFill.RULE_FEATURE_NAME_KEY])
            missing_fill_index_dict[feature_index] = rule

        replace_cols_index_list = []
        for i, v in enumerate(data_row):
            # Rule column and the value is empty
            if i in missing_fill_index_dict and str(v) in missing_value_list:
                replace_cols_index_list.append(i)
                fill_type = missing_fill_index_dict[i][ImputerMissingFill.RULE_FILL_TYPE_KEY]
                if fill_type == ImputerMissingFill.RULE_CUSTOM:
                    data_row[i] = str(missing_fill_index_dict[i][ImputerMissingFill.RULE_FILL_VALUE_KEY])
                else:
                    # Get the fill of each feature_ Value value
                    summary_statics = summary_statics_dict[fill_type]
                    if summary_statics is None:
                        continue

                    data_row[i] = str(summary_statics[i])

        return data_row, replace_cols_index_list

    @staticmethod
    def __get_cols_summary_statics_value(summary_obj=None, header=None, fill_type=None):
        """
        Return the summary statistics (maximum value, minimum value...) of the corresponding feature column
        according to the filling rule name

        :return {'x0': -1.726901, 'x1': -2.223994, 'x2': -1.693361, 'x3': -1.222423, 'x4': -2.682695, 'x5': -1.443878}
        """
        cols_summary_statics_value = None
        if fill_type == ImputerMissingFill.RULE_Min:
            cols_summary_statics_value = summary_obj.get_min()
        elif fill_type == ImputerMissingFill.RULE_MAX:
            cols_summary_statics_value = summary_obj.get_max()
        elif fill_type == ImputerMissingFill.RULE_AVG:
            cols_summary_statics_value = summary_obj.get_mean()
        elif fill_type == ImputerMissingFill.RULE_MODE:
            """
            Original format：{'x0': [(-0.809525, 20)], 'x1': [(0.257745, 12), (-1.299041, 12)], 'x2': [(-0.040545, 12)]}
            """
            cols_summary_statics_value = summary_obj.get_mode()
            """
            After discussion with the model group, the first one is selected by default, and the return format is consistent with the format of Max, min, etc
            {'x0': 4.094189, 'x1': 3.885905, 'x2': 4.287337}
            """
            # for key, value in cols_summary_statics_value.items():
            #     cols_summary_statics_value[key] = value
        elif fill_type == ImputerMissingFill.RULE_CUSTOM:
            pass

        if cols_summary_statics_value is None:
            return None

        cols_summary_statics_value = [round(cols_summary_statics_value[key], 6) for key in header]
        return cols_summary_statics_value

    def __check_missing_fill_rule(self, header, replace_value=None):
        """
        Check whether all feature columns have filling rules,
        otherwise the columns without rules are replaced with the original default values
        :return:
        """
        if len(header) == len(self.missing_fill_rule):
            return self.missing_fill_rule

        # Some feature columns do not have a fill rule selected
        if replace_value is None:
            raise ValueError("Replace value should not be None")

        default_fill_rule_list = []
        for feature in header:
            is_exist = False
            for rule in self.missing_fill_rule:
                if feature == rule[ImputerMissingFill.RULE_FEATURE_NAME_KEY]:
                    is_exist = True
                    break
            if not is_exist:
                # If it does not exist, it is replaced by the default method
                default_fill_dict = {ImputerMissingFill.RULE_FEATURE_NAME_KEY: feature,
                                     ImputerMissingFill.RULE_FILL_TYPE_KEY: ImputerMissingFill.RULE_CUSTOM,
                                     ImputerMissingFill.RULE_FILL_VALUE_KEY: replace_value}
                default_fill_rule_list.append(default_fill_dict)

        return self.missing_fill_rule + default_fill_rule_list
