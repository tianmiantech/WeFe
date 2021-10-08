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

from common.python.utils import log_utils
from kernel.components.correlation.multivertpearson.multi_vert_pearson_base import MultiVertPearsonBase
from kernel.security.protol.spdz.tensor.fixedpoint_table import table_dot
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MultiVertPearsonProvider(MultiVertPearsonBase):

    def __init__(self):
        super(MultiVertPearsonProvider, self).__init__()

    def fit(self, data_instance):
        # local
        data = self._select_columns(data_instance)
        n, normed = self._standardized(data)
        self.local_corr = table_dot(normed, normed)
        self.local_corr /= n
        self._summary["local_corr"] = self.local_corr.tolist()
        self._summary["num_local_features"] = len(self.names)
        self._summary["features"] = self.names
        LOGGER.info(f"local_party:{self.local_party}")
        number_provider = len(self.provider_parties)
        if self.model_param.cross_parties:
            all_parties = [self.promoter_party, self.local_party]
            self._calc_two_party_corr_spdz(normed=normed, n=n, local_party=self.local_party,
                                           other_party=self.promoter_party, all_parties=all_parties)
            result = {'member_id': str(self.local_party.member_id), 'local_corr': self.local_corr,
                      'feature_names': self.names, 'feature_num': len(self.names)}
            self.transfer_variable.provider_corr_infos.remote(result, consts.PROMOTER)
            local_party_idx = [str(x) for x in self.provider_parties].index(str(self.local_party))
            i = 0
            while i < local_party_idx:
                all_parties = [self.provider_parties[i], self.local_party]
                self._calc_two_party_corr_spdz(normed=normed, n=n, local_party=self.local_party,
                                               other_party=self.provider_parties[i],
                                               all_parties=all_parties)
                i += 1

            j = local_party_idx + 1
            while j < number_provider:
                right_member = self.provider_parties[j]
                all_parties = [self.local_party, right_member]
                m1, m2, corr = self._calc_two_party_corr_spdz(normed=normed, n=n, local_party=self.local_party,
                                                              other_party=right_member,
                                                              all_parties=all_parties, left=True)
                result = {'left_member_id': self.local_party.get_member_id(),
                          'right_member_id': right_member.get_member_id(),
                          'corr': corr}
                self.transfer_variable.provider_corr_provider.remote(result, consts.PROMOTER, suffix=(
                    self.local_party.member_id, right_member.member_id))
                j += 1

        self._callback()
        LOGGER.info(f"summary:{self._summary}")
        self.set_summary(self._summary)
