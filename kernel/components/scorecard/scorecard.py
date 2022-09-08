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

import math
from collections import defaultdict

from common.python.utils import log_utils
from kernel.components.binning.core.bin_inner_param import BinInnerParam
from kernel.components.binning.core.bin_result import BinResults, BinColResults
from kernel.components.scorecard.param import ScorecardParam
from kernel.model_base import ModelBase

LOGGER = log_utils.get_logger()

class ScoreCard(ModelBase):
    def __init__(self):
        super(ScoreCard, self).__init__()
        self.model_param = ScorecardParam()
        self.score_card_result = defaultdict(float)

    def _init_model(self, params):
        self.model_param = params

    def _init_param(self):
        self.p0 = self.model_param.p0
        self.pdo = self.model_param.pdo

    def callback_score_data(self):
        metric_name = self.tracker.component_name
        self.__save_score(self.pdo, self.p0,  metric_name, metric_namespace= "score", kv = self.score_card_result)

    def __save_score(self, pdo, p0, metric_name, metric_namespace, kv):
        extra_metas = {}
        key_list = ["pdo", "p0"]
        for key in key_list:
            value = locals()[key]
            if value:
                extra_metas[key] = value
        self.tracker.saveScoreData(metric_name, metric_namespace, extra_metas, kv)

    def _get_binning_result(self):
        model_param, binning_results = self.tracker.get_binning_result()
        component_type = model_param.get('component_type')
        if binning_results is None:
            raise ValueError('not find binning result')
        bin_inner_param = BinInnerParam()
        bin_inner_param.header = model_param.get('header')
        bin_inner_param.transform_bin_indexes = model_param.get('transform_bin_indexes')
        binResults = BinResults()
        all_cols_results = {}
        LOGGER.debug(f'binning_results={binning_results}')
        for feature, result in binning_results.items():
            binColResults = BinColResults(woe_array=result.get('woe'), iv=-99)
            binColResults.event_count_array,binColResults.non_event_count_array, binColResults.count_array = \
                result.get('eventCount'), result.get('noneventCount'), result.get('countArray')
            binColResults.set_split_points(result.get('split_points'))
            all_cols_results[feature] = binColResults
        binResults.all_cols_results = all_cols_results
        return bin_inner_param, binResults, component_type

    @staticmethod
    def get_count_odds(bin_results):
        print(bin_results)
        p = sum(list(map(int, bin_results.non_event_count_array))) / \
            sum(list(map(int, bin_results.count_array)))
        odds = p / (1 - p)
        return odds

    def cal_score(self, odds):
        B_score = self.pdo / math.log(2, )
        A_score = self.p0 - B_score * math.log(odds, )
        return A_score, B_score


class ScoreCardPromoter(ScoreCard):
    def __init__(self):
        super(ScoreCardPromoter, self).__init__()

    def fit(self, *args):
        self._init_param()
        LOGGER.debug("scorecard begainning, arg = {}".format(args))
        # get binning result
        bin_inner_param, bin_results, component_type = self._get_binning_result()
        feature_bin_results = bin_results.all_cols_results.get(list(bin_results.all_cols_results.keys())[0])

        # caculate static scores
        odds = self.get_count_odds(feature_bin_results)
        A_score, B_score = self.cal_score(odds)
        self.score_card_result["odds"] = odds
        self.score_card_result["a_score"] = A_score
        self.score_card_result["b_score"] = B_score
        return self.callback_score_data()

class ScoreCardProvider(ScoreCard):
    def __init__(self):
        super(ScoreCardProvider, self).__init__()

    def fit(self, *args):
        self._init_param()
        B_score = self.pdo / math.log(2, )
        bin_inner_param, bin_results, component_type = self._get_binning_result()
        feature_bin_results = bin_results.all_cols_results.get(list(bin_results.all_cols_results.keys())[0])
        if 'horz' in component_type.lower():
            odds = self.get_count_odds(feature_bin_results)
            A_score, B_score = self.cal_score(odds)
            self.score_card_result["odds"] = odds
            self.score_card_result["a_score"] = A_score
        self.score_card_result["b_score"] = B_score
        return self.callback_score_data()

