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

import numpy as np

from common.python.utils import log_utils
from kernel.components.correlation.core import base_pearson
from kernel.components.correlation.vertpearson.param import PearsonParam
from kernel.model_base import ModelBase
from kernel.security.protol.spdz.tensor.fixedpoint_table import table_dot
from kernel.transfer.variables.transfer_class.vert_pearson_transfer_variable import VertPearsonTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class VertPearson(ModelBase):

    def __init__(self):
        super().__init__()
        self.model_param = PearsonParam()
        self.role = None
        self.corr = None
        self.local_corr = None

        self.metric_name = "pearson"
        self.metric_namespace = "statistics"

        self.shapes = []
        self.names = []
        self.parties = []
        self.local_party = None
        self.other_party = None
        self._set_parties()

        self._summary = {}
        self.transfer_variable = VertPearsonTransferVariable()

    def _set_parties(self):
        base_pearson.set_parties(self)

    def _init_model(self, param):
        super()._init_model(param)
        self.model_param = param

    def _select_columns(self, data_instance):
        return base_pearson.select_columns(self, data_instance)

    @staticmethod
    def _standardized(data, features):
        n = data.count()
        LOGGER.debug("(data.first is {}".format((data.first()[1])))
        if len(data.first()[1]) == 0:
            return n, 0, np.array([])
        sum_x, sum_square_x = data.mapValues(lambda x: (x, x ** 2)) \
            .reduce(lambda pair1, pair2: (pair1[0] + pair2[0], pair1[1] + pair2[1]))
        mu = sum_x / n
        sigma_square = list(sum_square_x / n - mu ** 2)
        sigma = np.sqrt(sigma_square)
        LOGGER.debug(
            f'n={n},sum_x={sum_x},sum_square_x={sum_square_x},mu={mu},sigma_square={sigma_square},sigma={sigma}')
        null_idx = []
        not_null_idx = []
        for i in range(len(sigma)):
            if math.fabs(sigma[i]) <= consts.FLOAT_ZERO or np.isnan(sigma[i]):
                null_idx.append(i)
            else:
                not_null_idx.append(i)
        if len(null_idx) >= 1 and len(null_idx) < len(sigma):
            null_feature = np.array(features)[null_idx]
            data = data.mapValues(lambda inst: inst[not_null_idx])
            LOGGER.debug(f"dataset features {np.array(features)[not_null_idx]} features is not null")
            mu = mu[not_null_idx]
            sigma = sigma[not_null_idx]
            LOGGER.debug(f"dataset features {null_feature} features is null or 0")
        if len(null_idx) == len(sigma):
            return n, 0, np.array([])
        return n, data.mapValues(lambda x: (x - mu) / sigma) , np.array(features)[not_null_idx]

    def calc_local_corr(self, n, normed):
        self.local_corr = table_dot(normed, normed)
        self.local_corr /= n
        self._summary["local_corr"] = self.local_corr.tolist()
        self._summary["num_local_features"] = len(self.names)
        self._summary["features"] = self.names
        LOGGER.info(f"local_party:{self.local_party}, other_part:{self.other_party}")

    def fit(self, data_instance):
        # local
        data = self._select_columns(data_instance)
        n, normed, name = self._standardized(data, self.names)
        self.names = name.tolist()
        has_feature = True
        if len(name) == 0:
            has_feature = False
        LOGGER.debug("data instance feature statu:{}".format(has_feature))
        if has_feature:
            self.calc_local_corr(n, normed)

        self._summary["local_corr"] = self.local_corr.tolist() if self.local_corr is not None else []
        self._summary["num_local_features"] = len(self.names)
        self._summary["features"] = self.names
        LOGGER.info(f"local_party:{self.local_party}, other_part:{self.other_party}")

        if consts.PROMOTER == self.role:
            self.transfer_variable.promoter_feature_select.remote({"feature_select": has_feature}, consts.PROVIDER)
            other_has_feature = self.transfer_variable.provider_feature_select.get(-1)[0]['feature_select']
            if not other_has_feature or not has_feature:
                self.process_promoter(has_feature, other_has_feature)
        else:
            self.transfer_variable.provider_feature_select.remote({"feature_select": has_feature}, consts.PROMOTER)
            other_has_feature = self.transfer_variable.promoter_feature_select.get(-1)[0]['feature_select']
            if not other_has_feature or not has_feature:
                self.process_provider()

        if has_feature and other_has_feature:
            if self.model_param.cross_parties:
                if self.role == consts.PROMOTER:
                    m1, m2, corr = base_pearson.calculate_corr_spdz(normed, n, self.local_party, self.other_party,
                                                                    self.parties, left=True)
                else:
                    m1, m2, corr = base_pearson.calculate_corr_spdz(normed, n, self.local_party, self.other_party,
                                                                    self.parties)
                self.shapes.append(m1)
                self.shapes.append(m2)
                self.corr = corr
                self._summary["corr"] = self.corr.tolist()
                self._summary["num_remote_features"] = m2 if self.local_party.role == consts.PROMOTER else m1
                self._summary['corr_shape'] = self.shapes
                # _names = []
                # for name in self.names:
                #     _names.append(f"{self.role}_{name}")

                if self.role == consts.PROVIDER:
                    self.process_provider()
                elif self.role == consts.PROMOTER:
                    self.process_promoter(has_feature, other_has_feature)
            else:
                self.shapes.append(self.local_corr.shape[0])
                self.parties = [self.local_party]
                if self.role == consts.PROMOTER:
                    self._summary['mix_corr'] = self.local_corr.tolist()
                    self._summary['mix_feature_names'] = self.names

        self._callback()
        LOGGER.info(f"summary:{self._summary}")
        self.set_summary(self._summary)

    # noinspection PyTypeChecker
    def _callback(self):
        metric_data = [("corr", self._summary)]
        LOGGER.debug(f'metric_data: {metric_data}, metric_name:{self.metric_name}')
        self.tracker.saveMetricData(self.metric_name, self.metric_namespace, None, metric_data)

    def process_promoter(self, promoter_feature, provider_feature):
        _names = []
        for name in self.names:
            _names.append(f"{self.role}_{name}")
        self.transfer_variable.promoter_features.remote({"remote_features_names": _names}, consts.PROVIDER)
        provider_corr = self.transfer_variable.provider_corr_infos.get(-1)
        LOGGER.info(provider_corr[0])
        for k, v in provider_corr[0].items():
            self._summary[k] = v

        if promoter_feature and provider_feature:
            A = np.asarray(self.local_corr)
            B = np.asarray(self.corr)
            C = np.asarray(self._summary['remote_corr'])
            D = np.append(A, B, axis=1)
            E = np.append(B.T, C, axis=1)
            mix_corr = np.append(D, E, axis=0)
            mix_corr = mix_corr.tolist()
        elif provider_feature:
            mix_corr = self._summary['remote_corr']
        elif promoter_feature:
            mix_corr = self.local_corr.tolist()
        else:
            raise ValueError("all data sets is null, please check data!")

        LOGGER.info(f"mix_corr= {len(mix_corr)}")
        self._summary['mix_corr'] = mix_corr
        mix_features = []
        mix_features.extend(_names)
        mix_features.extend(self._summary['remote_features_names'])
        self._summary['mix_feature_names'] = mix_features

    def process_provider(self):
        _names = []
        for name in self.names:
            _names.append(f"{self.role}_{name}")
        provider_corr_result = {'remote_corr': self.local_corr.tolist() if len(self.names) > 0 else None,
                                'remote_num_features': len(self.names),
                                'remote_features_names': _names}
        self.transfer_variable.provider_corr_infos.remote(provider_corr_result, role=consts.PROMOTER)
        remote_features_names = self.transfer_variable.promoter_features.get(idx=0)
        for k, v in remote_features_names.items():
            self._summary[k] = v
        corr_feature_names = [self._summary['remote_features_names'], _names]
        self._summary['corr_feature_names'] = corr_feature_names
