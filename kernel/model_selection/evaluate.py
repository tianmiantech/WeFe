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

from kernel.components.evaluation.evaluation import Evaluation
from kernel.utils import consts


def evaluate(eval_data, eval_name, model):
    if eval_data is None:
        return

    eval_obj = Evaluation()
    # LOGGER.debug("In KFold, evaluate_param is: {}".format(self.evaluate_param.__dict__))
    # eval_obj._init_model(self.evaluate_param)
    eval_param = model.get_metrics_param()

    eval_param.check_single_value_default_metric()
    eval_obj._init_model(eval_param)
    eval_obj.set_tracker(model.tracker)
    eval_data = {eval_name: eval_data}
    eval_obj.fit(eval_data)
    eval_obj.output_data()
    score_result = eval_obj.eval_results_score()

    score = 0.0
    if score_result:
        validate_dict = score_result.get('validate')
        train_dict = score_result.get('train')
        collect_dict = validate_dict if validate_dict else train_dict
        if eval_param.eval_type == consts.REGRESSION:
            score = collect_dict.get(consts.R2_SCORE)
        elif eval_param.eval_type == consts.BINARY:
            score = collect_dict.get(consts.KS)
            # if score == 0:
            #     score = collect_dict.get(consts.AUC)
        elif eval_param.eval_type == consts.MULTY:
            score = collect_dict.get(consts.ACCURACY)

    return score
