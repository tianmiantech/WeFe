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

import numpy as np

from common.python.utils import log_utils
from kernel.components.correlation.core import base_pearson
from kernel.components.pca.vertpca.param import VertPCAParam
from kernel.model_base import ModelBase
from kernel.security.protol.spdz.tensor.fixedpoint_table import table_dot
from kernel.transfer.variables.transfer_class.vert_pca_transfer_variable import VertPCATransferVariable
from kernel.utils import consts, abnormal_detection

LOGGER = log_utils.get_logger()

MODEL_META_NAME = "VertPCAModelMeta"
MODEL_PARAM_NAME = "VertPCAModelParam"

LOCAL_DATA = "local_cov"
LOCAL_FEATURES_NUM = "local_features_num"

REMOTE_DATE = "remote_cov"
REMOTE_FEATURES_NUM = "remote_num_features"
REMOTE_FEATURES_NAMES = "remote_features_names"

CROSS_DATA = "cross_cov"
CROSS_FEATURE_NAMES = "cross_feature_names"

MIX_DATA = "mix_cov"
MIX_FEATURE_NAMES = "mix_feature_names"

SHAPE = "cross_shape"


class VertPCA(ModelBase):

    def __init__(self):
        super().__init__()
        self.model_param = VertPCAParam()
        self.role = None
        self.cov = None
        self.local_cov = None

        self.metric_name = "pca"
        self.metric_namespace = "statistics"

        self.shapes = []
        self.names = []
        self.parties = []
        self.local_party = None
        self.other_party = None
        self._set_parties()
        self.mix_cov = None

        self.transfer_variable = VertPCATransferVariable()

        self._summary = {}

    def _set_parties(self):
        base_pearson.set_parties(self)

    def _init_model(self, param):
        super()._init_model(param)
        self.model_param = param

    def _select_columns(self, data_instance):
        return base_pearson.select_columns(self, data_instance)

    @staticmethod
    def _standardized(data):
        n = data.count()
        sum_x = data.reduce(lambda pair1, pair2: pair1 + pair2)
        mu = sum_x / n
        return n, data.mapValues(lambda x: x - mu)

    def fit(self, data_instance):
        abnormal_detection.empty_table_detection(data_instance)
        # local
        data = self._select_columns(data_instance)
        n, normed = self._standardized(data)
        self.local_cov = table_dot(normed, normed)
        self.local_cov /= n
        self._summary[LOCAL_DATA] = self.local_cov.tolist()
        self._summary[LOCAL_FEATURES_NUM] = len(self.names)
        LOGGER.info(f"local_party:{self.local_party}, other_part:{self.other_party}")
        if self.model_param.cross_parties:
            if self.role == consts.PROMOTER:
                m1, m2, cov = base_pearson.calculate_corr_spdz(normed, n, self.local_party, self.other_party,
                                                               self.parties, left=True, name="cov")
            else:
                m1, m2, cov = base_pearson.calculate_corr_spdz(normed, n, self.local_party, self.other_party,
                                                               self.parties, name="cov")
            self.shapes.append(m1)
            self.shapes.append(m2)
            self.cov = cov

            self._summary[CROSS_DATA] = self.cov.tolist()
            self._summary[REMOTE_FEATURES_NUM] = m2 if self.local_party.role == consts.PROMOTER else m1

            self._summary[SHAPE] = self.shapes
            _names = []
            for name in self.names:
                _names.append(f"{self.role}_{name}")
            if self.role == consts.PROVIDER:
                provider_corr_result = {REMOTE_DATE: self.local_cov.tolist(),
                                        REMOTE_FEATURES_NUM: len(self.names),
                                        REMOTE_FEATURES_NAMES: _names}
                self.transfer_variable.provider_cov_infos.remote(provider_corr_result, role=consts.PROMOTER)
                remote_features_names = self.transfer_variable.promoter_features.get(idx=0)
                for k, v in remote_features_names.items():
                    self._summary[k] = v
                corr_feature_names = [self._summary[REMOTE_FEATURES_NAMES], _names]
                self._summary[CROSS_FEATURE_NAMES] = corr_feature_names
                pca_result = self.transfer_variable.pca_result.get(idx=0)
                for k, v in pca_result.items():
                    self._summary[k] = v
            elif self.role == consts.PROMOTER:
                self.transfer_variable.promoter_features.remote({REMOTE_FEATURES_NAMES: _names}, consts.PROVIDER)
                provider_corr = self.transfer_variable.provider_cov_infos.get(-1)
                LOGGER.info(provider_corr[0])
                for k, v in provider_corr[0].items():
                    self._summary[k] = v

                A = np.asarray(self.local_cov)
                B = np.asarray(self.cov)
                C = np.asarray(self._summary[REMOTE_DATE])
                D = np.append(A, B, axis=1)
                E = np.append(B.T, C, axis=1)
                mix_cov = np.append(D, E, axis=0)
                LOGGER.info(f"mix_cov={mix_cov.shape}")
                self._summary[MIX_DATA] = mix_cov.tolist()
                mix_features = []
                mix_features.extend(_names)
                mix_features.extend(self._summary[REMOTE_FEATURES_NAMES])
                self._summary[MIX_FEATURE_NAMES] = mix_features
                self.mix_cov = mix_cov.tolist()
                eig_values, eig_vectors = self._computer_eigenvalue(self.mix_cov)
                pca_result = {'eig_values': eig_values.tolist(),
                              'eig_vectors': eig_vectors.tolist(),
                              MIX_FEATURE_NAMES: mix_features}
                self.transfer_variable.pca_result.remote(pca_result, consts.PROVIDER)

        else:
            self.shapes.append(self.local_cov.shape[0])
            self.parties = [self.local_party]
            self._summary[MIX_DATA] = self.local_cov.tolist()
            self._summary[MIX_FEATURE_NAMES] = self.names
            self.mix_cov = self.local_cov.tolist()
            self._computer_eigenvalue(self.mix_cov)

        self._callback()
        LOGGER.info(f"summary:{self._summary}")
        self.set_summary(self._summary)

    def _computer_eigenvalue(self, cov):
        eig_values, eig_vectors = np.linalg.eig(cov)
        self._summary['eig_values'] = eig_values.tolist()
        self._summary['eig_vectors'] = eig_vectors.tolist()
        return eig_values, eig_vectors

    # noinspection PyTypeChecker
    def _callback(self):
        metric_data = [("cov", self._summary)]
        print(f'metric_data: {metric_data}, metric_name:{self.metric_name}')
        self.tracker.saveMetricData(self.metric_name, self.metric_namespace, None, metric_data)
