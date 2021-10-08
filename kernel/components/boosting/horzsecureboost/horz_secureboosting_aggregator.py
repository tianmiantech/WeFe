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

from typing import List, Dict

from common.python.utils import log_utils
from kernel.components.boosting import HistogramBag, FeatureHistogramWeights
from kernel.transfer.framework.horz.blocks import secure_sum_aggregator, loss_scatter, has_converged
from kernel.transfer.framework.weights import DictWeights

LOGGER = log_utils.get_logger()


class SecureBoostArbiterAggregator(object):

    def __init__(self, ):
        """
        Args:
            transfer_variable:
            converge_type: see kernel/optimizer/convergence.py
            tolerate_val:
        """
        self.loss_scatter = loss_scatter.Server()
        self.has_converged = has_converged.Server()

    def aggregate_loss(self, suffix):
        global_loss = self.loss_scatter.weighted_loss_mean(suffix=suffix)
        return global_loss

    def broadcast_converge_status(self, func, loss, suffix):
        is_converged = func(*loss)
        self.has_converged.remote_converge_status(is_converged, suffix=suffix)
        LOGGER.debug('convergence status sent with suffix {},is_converged {}'.format(suffix, is_converged))
        return is_converged


class SecureBoostClientAggregator(object):

    def __init__(self, ):
        self.loss_scatter = loss_scatter.Client()
        self.has_converged = has_converged.Client()

    def send_local_loss(self, loss, sample_num, suffix):
        self.loss_scatter.send_loss(loss=(loss, sample_num), suffix=suffix)
        LOGGER.debug('loss sent with suffix {}'.format(suffix))

    def get_converge_status(self, suffix):
        converge_status = self.has_converged.get_converge_status(suffix)
        return converge_status


class DecisionTreeArbiterAggregator(object):
    """
     secure aggregator for secureboosting Arbiter, gather histogram and numbers
    """

    def __init__(self, verbose=False):
        self.aggregator = secure_sum_aggregator.Server(enable_secure_aggregate=True)
        self.scatter = loss_scatter.Server()
        self.verbose = verbose

    def aggregate_num(self, suffix):
        self.scatter.weighted_loss_mean(suffix=suffix)

    def aggregate_histogram(self, suffix) -> List[HistogramBag]:

        def _func(x, y):
            return x[0] + y[0], None

        agg_histogram = self.aggregator.sum_model(suffix=suffix)

        if self.verbose:
            for hist in agg_histogram._weights:
                LOGGER.debug('showing aggregated hist{}, hid is {}'.format(hist, hist.hid))

        return agg_histogram._weights

    def aggregate_root_node_info(self, suffix):

        agg_data = self.aggregator.sum_model(suffix=suffix)
        d = agg_data._weights
        return d['g_sum'], d['h_sum']

    def broadcast_root_info(self, g_sum, h_sum, suffix):
        d = {'g_sum': g_sum, 'h_sum': h_sum}
        weight = DictWeights(d=d, )
        self.aggregator.send_aggregated_model(weight, suffix=suffix)


class DecisionTreeClientAggregator(object):
    """
    secure aggregator for secureboosting Client, send histogram and numbers
    """

    def __init__(self, verbose=False):
        self.aggregator = secure_sum_aggregator.Client(enable_secure_aggregate=True)
        self.scatter = loss_scatter.Client()
        self.verbose = verbose

    def send_number(self, number: float, degree: int, suffix):
        self.scatter.send_loss((number, degree), suffix=suffix)

    def send_histogram(self, hist: List[HistogramBag], suffix):
        if self.verbose:
            for idx, histbag in enumerate(hist):
                LOGGER.debug('showing client hist {}'.format(idx))
                LOGGER.debug(histbag)
        weights = FeatureHistogramWeights(list_of_histogram_bags=hist)
        self.aggregator.send_model(weights, suffix=suffix)

    def get_aggregated_root_info(self, suffix) -> Dict:
        dict_weight = self.aggregator.get_aggregated_model(suffix=suffix)
        content = dict_weight._weights
        return content

    def send_local_root_node_info(self, g_sum, h_sum, suffix):
        d = {'g_sum': g_sum, 'h_sum': h_sum}
        dict_weights = DictWeights(d=d)
        self.aggregator.send_model(dict_weights, suffix=suffix)

    def get_best_split_points(self):
        pass
