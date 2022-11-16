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
        if binning_results is None:
            raise ValueError("Don't find binning result")
        for feature, binning_result in binning_results.items():
            if binning_result:
                event_count_array = binning_result.get('eventCount')
                nonevent_count_array = binning_result.get('noneventCount')
                return event_count_array, nonevent_count_array, model_param
        return ValueError("The binning result is NULL")

    @staticmethod
    def get_count_odds(event_counts, non_event_counts):
        odds = sum(list(map(int, event_counts))) / sum(list(map(int, non_event_counts)))
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
        event_count_array, non_event_count_array, model_param = self._get_binning_result()
        # caculate static scores
        odds = self.get_count_odds(event_count_array, non_event_count_array)
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
        event_count_array, non_event_count_array, model_param = self._get_binning_result()
        if 'horz' in model_param.get('component_type').lower():
            odds = self.get_count_odds(event_count_array, non_event_count_array)
            A_score, B_score = self.cal_score(odds)
            self.score_card_result["odds"] = odds
            self.score_card_result["a_score"] = A_score
        self.score_card_result["b_score"] = B_score
        return self.callback_score_data()

