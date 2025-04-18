#!/usr/bin/env python
# -*- coding: utf-8 -*-

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
from kernel.base.params.base_param import BaseParam
from kernel.utils import consts


LOGGER = log_utils.get_logger()


class EvaluateParam(BaseParam):
    """
    Define the evaluation method of binary/multiple classification and regression

    Parameters
    ----------
    eval_type: string, support 'binary' for HorzLR, VertLR and Secureboosting. support 'regression' for Secureboosting. 'multi' is not support these version

    pos_label: specify positive label type, can be int, float and str, this depend on the data's label, this parameter effective only for 'binary'

    need_run: bool, default True
        Indicate if this module needed to be run
    """

    def __init__(self, eval_type="binary", pos_label=1, need_run=True, metrics=None):
        super().__init__()
        self.eval_type = eval_type
        self.pos_label = pos_label
        self.need_run = need_run
        self.metrics = metrics
        self.score_param = ScoreParam()
        self.psi_param = PSIParam()

        self.default_metrics = {
            consts.BINARY: consts.ALL_BINARY_METRICS,
            consts.MULTY: consts.ALL_MULTI_METRICS,
            consts.REGRESSION: consts.ALL_REGRESSION_METRICS
        }

        self.allowed_metrics = {
            consts.BINARY: consts.ALL_BINARY_METRICS,
            consts.MULTY: consts.ALL_MULTI_METRICS,
            consts.REGRESSION: consts.ALL_REGRESSION_METRICS
        }

    def _use_single_value_default_metrics(self):

        self.default_metrics = {
            consts.BINARY: consts.DEFAULT_BINARY_METRIC,
            consts.MULTY: consts.DEFAULT_MULTI_METRIC,
            consts.REGRESSION: consts.DEFAULT_REGRESSION_METRIC
        }

    def _check_valid_metric(self, metrics_list):

        metric_list = consts.ALL_METRIC_NAME
        alias_name: dict = consts.ALIAS

        full_name_list = []

        metrics_list = [str.lower(i) for i in metrics_list]

        for metric in metrics_list:

            if metric in metric_list:
                if metric not in full_name_list:
                    full_name_list.append(metric)
                continue

            valid_flag = False
            for alias, full_name in alias_name.items():
                if metric in alias:
                    if full_name not in full_name_list:
                        full_name_list.append(full_name)
                    valid_flag = True
                    break

            if not valid_flag:
                raise ValueError('metric {} is not supported'.format(metric))

        allowed_metrics = self.allowed_metrics[self.eval_type]

        for m in full_name_list:
            if m not in allowed_metrics:
                raise ValueError('metric {} is not used for {} task'.format(m, self.eval_type))

        if consts.RECALL in full_name_list and consts.PRECISION not in full_name_list:
            full_name_list.append(consts.PRECISION)

        if consts.RECALL not in full_name_list and consts.PRECISION in full_name_list:
            full_name_list.append(consts.RECALL)

        return full_name_list

    def check(self):

        descr = "evaluate param's "
        self.eval_type = self.check_and_change_lower(self.eval_type,
                                                     [consts.BINARY, consts.MULTY, consts.REGRESSION],
                                                     descr)

        if type(self.pos_label).__name__ not in ["str", "float", "int"]:
            raise ValueError(
                "evaluate param's pos_label {} not supported, should be str or float or int type".format(
                    self.pos_label))

        if type(self.need_run).__name__ != "bool":
            raise ValueError(
                "evaluate param's need_run {} not supported, should be bool".format(
                    self.need_run))

        if self.metrics is None or len(self.metrics) == 0:
            self.metrics = self.default_metrics[self.eval_type]
            LOGGER.warning('use default metric {} for eval type {}'.format(self.metrics, self.eval_type))

        self.metrics = self._check_valid_metric(self.metrics)
        LOGGER.info("Finish evaluation parameter check!")

        return True

    def check_single_value_default_metric(self):
        self._use_single_value_default_metrics()
        self.check()

class ScoreParam(BaseParam):
    def __init__(self, prob_need_to_bin = False, bin_method = None, bin_num = None):
        super().__init__()
        self.prob_need_to_bin = prob_need_to_bin
        self.bin_num = bin_num
        self.bin_method = bin_method


    def check(self):
        if self.prob_need_to_bin:
            if self.bin_method not in ["bucket"]:
                raise ValueError("bin_method{} not support".format(self.bin_method))
            if self.bin_num <= 0:
                raise ValueError("bin_num is {}, should choose number of binning greater than 0".format(self.bin_num))

class PSIParam(BaseParam):
    def __init__(self, need_psi = False, bin_method = None, bin_num = None, split_points=None):
        super().__init__()
        self.need_psi = need_psi
        self.bin_num = bin_num
        self.bin_method = bin_method
        self.split_points = split_points

    def check(self):
        if self.need_psi:
            if self.bin_method not in ["bucket", "custom"]:
                raise ValueError("bin_method{} not support".format(self.bin_method))
            if self.bin_method == 'custom':
                if not self.split_points:
                    raise ValueError("split_points {} not NULL")
            if self.bin_num <= 0:
                raise ValueError(
                    "bin_num is {}, should choose number of binning greater than 0".format(self.bin_num))



