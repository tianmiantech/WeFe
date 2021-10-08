#!/usr/bin/python3
# -*- coding:utf-8 -*-　

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

from common.python import RuntimeInstance
from common.python.utils import log_utils
from kernel.security.protol.spdz import SPDZ
from kernel.security.protol.spdz.tensor.fixedpoint_table import FixedPointTensor
from kernel.utils import consts

LOGGER = log_utils.get_logger()


def select_columns(self, data_instance):
    col_names = data_instance.schema["header"]
    if self.model_param.column_indexes == -1:
        self.names = col_names
        name_set = set(self.names)
        for name in self.model_param.column_names:
            if name not in name_set:
                raise ValueError(f"name={name} not found in header")
        return data_instance.mapValues(lambda inst: inst.features)

    name_to_idx = {col_names[i]: i for i in range(len(col_names))}
    selected = set()
    for name in self.model_param.column_names:
        if name in name_to_idx:
            selected.add(name_to_idx[name])
            continue
        raise ValueError(f"{name} not found")
    for idx in self.model_param.column_indexes:
        if 0 <= idx < len(col_names):
            selected.add(idx)
            continue
        raise ValueError(f"idx={idx} out of bound")
    selected = sorted(list(selected))
    if len(selected) == len(col_names):
        self.names = col_names
        return data_instance.mapValues(lambda inst: inst.features)

    self.names = [col_names[i] for i in selected]
    return data_instance.mapValues(lambda inst: inst.features[selected])


def calculate_corr_spdz(normed, n, local_party, other_party, all_parties, left=False, name='pearson'):
    with SPDZ(name, local_party=local_party, all_parties=all_parties) as spdz:
        source = [normed, other_party]
        if left:
            x, y = FixedPointTensor.from_source("x", source[0]), FixedPointTensor.from_source("y", source[1])
        else:
            y, x = FixedPointTensor.from_source("y", source[0]), FixedPointTensor.from_source("x", source[1])
        LOGGER.info(f"x:{x}, y:{y}")
        m1 = len(x.value.first()[1])
        m2 = len(y.value.first()[1])
        corr = spdz.dot(x, y, 'corr').get() / n

    return m1, m2, corr


def set_parties(self):
    # since multi-host not supported yet, we assume parties are one from promoter and one from provider
    parties = []
    promoter_parties = RuntimeInstance.FEDERATION.roles_to_parties([consts.PROMOTER])
    provider_parties = RuntimeInstance.FEDERATION.roles_to_parties([consts.PROVIDER])
    if len(promoter_parties) != 1 or len(provider_parties) != 1:
        raise ValueError(f"one promoter and one provider required, "
                         f"while {len(promoter_parties)} promoter and {len(provider_parties)} provider provided")
    parties.extend(promoter_parties)
    parties.extend(provider_parties)

    local_party = RuntimeInstance.FEDERATION.local_party
    other_party = parties[0] if str(parties[0]) != str(local_party) else parties[1]

    self.parties = parties
    self.local_party = local_party
    self.other_party = other_party
