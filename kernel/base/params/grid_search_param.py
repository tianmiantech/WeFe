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



from kernel.base.params.base_param import BaseParam
from kernel.utils import consts


class GridSearchParam(BaseParam):
    """
    Define grid search params

    Parameters
    ----------
    mode: str, default: 'Vert'
        Indicate what mode is current task

    role: str, default: 'Promoter'
        Indicate what role is current party

    params_list: map, default: None
        Specify grid search params list. such as:
       "params_list": {
          "max_iter": [30,50,100],
          "batch_size": [320,500,1000],
          "learning_rate": [0.001,0.01,0.015],
        },

    need_grid_search: bool, default True
        Indicate if this module needed to be run

    """

    def __init__(self, mode=consts.VERT, role=consts.PROMOTER, params_list=None, need_grid_search=False):
        super(GridSearchParam, self).__init__()
        self.mode = mode
        self.role = role
        self.params_list = params_list
        self.need_grid_search = need_grid_search

    def check(self):
        model_param_descr = "grid search param's "
        self.check_valid_value(self.mode, model_param_descr, valid_values=[consts.HORZ, consts.VERT])
        self.check_valid_value(self.role, model_param_descr,
                               valid_values=[consts.PROVIDER, consts.PROMOTER, consts.ARBITER])
        if self.random_seed is not None:
            self.check_positive_integer(self.random_seed, model_param_descr)
