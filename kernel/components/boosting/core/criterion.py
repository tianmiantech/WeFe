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

from common.python.utils import log_utils
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class Criterion(object):
    def __init__(self, criterion_params):
        pass

    @staticmethod
    def split_gain(node_sum, left_node_sum, right_node_sum):
        raise NotImplementedError("node gain calculation method should be define!!!")


class XgboostCriterion(Criterion):

    def __init__(self, reg_lambda=0.1, reg_alpha=0):

        self.reg_lambda = reg_lambda  # l2 reg
        self.reg_alpha = reg_alpha  # l1 reg
        LOGGER.info('splitter criterion setting done: l1 {}, l2 {}'.format(self.reg_alpha, self.reg_lambda))

    @staticmethod
    def _g_alpha_cmp(gradient, reg_alpha):
        if gradient < - reg_alpha:
            return gradient + reg_alpha
        elif gradient > reg_alpha:
            return gradient - reg_alpha
        else:
            return 0

    @staticmethod
    def truncate(f, n=consts.TREE_DECIMAL_ROUND):
        return math.floor(f * 10 ** n) / 10 ** n

    def split_gain(self, node_sum, left_node_sum, right_node_sum):
        sum_grad, sum_hess = node_sum
        left_node_sum_grad, left_node_sum_hess = left_node_sum
        right_node_sum_grad, right_node_sum_hess = right_node_sum
        rs = self.node_gain(left_node_sum_grad, left_node_sum_hess) + \
             self.node_gain(right_node_sum_grad, right_node_sum_hess) - \
             self.node_gain(sum_grad, sum_hess)
        return self.truncate(rs)

    def node_gain(self, sum_grad, sum_hess):
        sum_grad, sum_hess = self.truncate(sum_grad), self.truncate(sum_hess)
        num = self._g_alpha_cmp(sum_grad, self.reg_alpha)
        return self.truncate(num * num / (sum_hess + self.reg_lambda))

    def node_weight(self, sum_grad, sum_hess):
        return self.truncate(-(self._g_alpha_cmp(sum_grad, self.reg_alpha)) / (sum_hess + self.reg_lambda))
