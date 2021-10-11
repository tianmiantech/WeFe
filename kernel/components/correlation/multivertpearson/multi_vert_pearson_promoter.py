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

import numpy as np

from common.python.utils import log_utils
from kernel.components.correlation.multivertpearson.multi_vert_pearson_base import MultiVertPearsonBase
from kernel.security.protol.spdz.tensor.fixedpoint_table import table_dot
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MultiVertPearsonPromoter(MultiVertPearsonBase):

    def __init__(self):
        super(MultiVertPearsonPromoter, self).__init__()

    def fit(self, data_instance):
        # local
        data = self._select_columns(data_instance)
        n, normed = self._standardized(data)
        self.local_corr = table_dot(normed, normed)
        self.local_corr /= n
        self._summary["local_corr"] = self.local_corr.tolist()
        self._summary["num_local_features"] = len(self.names)
        self._summary["features"] = self.names
        LOGGER.debug(f"local_party:{self.local_party}")
        if self.model_param.cross_parties:
            all_corr = {}
            all_feature_names = {self.local_party.get_member_id(): self.names}
            promoter_corr = {self.local_party.get_member_id(): self.local_corr}
            LOGGER.debug(f'provider_parties={self.provider_parties}')
            for provider_party in self.provider_parties:
                all_parties = [self.promoter_party, provider_party]
                LOGGER.debug(f'all_parties={all_parties}')
                m1, m2, corr = self._calc_two_party_corr_spdz(normed=normed, n=n, local_party=self.local_party,
                                                              other_party=provider_party, all_parties=all_parties,
                                                              left=True)
                promoter_corr[str(provider_party.member_id)] = corr
            all_corr[self.local_party.get_member_id()] = promoter_corr

            # send promoter feature names
            self.transfer_variable.promoter_features.remote({'feature_names': self.names}, consts.PROVIDER)
            provider_local_corr_list = self.transfer_variable.provider_corr_infos.get(idx=-1)
            for provider_local_corr in provider_local_corr_list:
                member_id = provider_local_corr['member_id']
                local_corr = provider_local_corr['local_corr']
                feature_names = provider_local_corr['feature_names']
                promoter_corr_provider = np.asarray(promoter_corr[member_id]).T
                provider_corr = {member_id: local_corr, self.local_party.get_member_id(): promoter_corr_provider}
                all_corr[member_id] = provider_corr
                all_feature_names[member_id] = feature_names

            for idx, party in enumerate(self.provider_parties):
                provider_corr = all_corr[party.get_member_id()]
                j = 0
                while j < idx:
                    other_party = self.provider_parties[j]
                    new_corr = np.asarray(all_corr[other_party.get_member_id()].get(party.get_member_id())).T
                    provider_corr[other_party.get_member_id()] = new_corr
                    j += 1

                i = idx + 1
                while i < len(self.provider_parties):
                    suffix = (party.member_id, self.provider_parties[i].member_id)
                    LOGGER.debug(f'party={party},type={type(party)}')
                    result = self.transfer_variable.provider_corr_provider.get_parties(parties=party, suffix=suffix)[0]
                    corr = result['corr']
                    right_member_id = result['right_member_id']
                    provider_corr[right_member_id] = corr
                    i += 1

            all_corr_matrix = None
            mix_feature_names = []
            for party in self.parties:
                party_corr_dict = all_corr.get(party.get_member_id())
                party_corr_matrix = None
                for other_party in self.parties:
                    value = party_corr_dict.get(other_party.get_member_id())
                    if party_corr_matrix is None:
                        party_corr_matrix = value
                    else:
                        party_corr_matrix = np.append(party_corr_matrix, value, axis=1)
                if all_corr_matrix is None:
                    all_corr_matrix = party_corr_matrix
                else:
                    all_corr_matrix = np.append(all_corr_matrix, party_corr_matrix, axis=0)
                feature_names = all_feature_names[party.get_member_id()]
                if party.role == consts.PROVIDER:
                    feature_names = [f'{party.role}_{party.get_member_id()}_{name}' for name in feature_names]
                mix_feature_names.extend(feature_names)

            LOGGER.debug(f'all_corr_matrix={all_corr_matrix.shape}')
            LOGGER.debug(f'all_corr_matrix={all_corr_matrix.tolist()}')

            self._summary['mix_corr'] = all_corr_matrix.tolist()
            self._summary['mix_feature_names'] = mix_feature_names

        else:
            self.shapes.append(self.local_corr.shape[0])
            self.parties = [self.local_party]
            if self.role == consts.PROMOTER:
                self._summary['mix_corr'] = self.local_corr.tolist()
                self._summary['mix_feature_names'] = self.names

        self._callback()
        LOGGER.info(f"summary:{self._summary}")
        self.set_summary(self._summary)
