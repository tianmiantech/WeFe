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

from typing import Tuple, List

from common.python.utils import log_utils
from kernel.utils import consts

LOGGER = log_utils.get_logger()

tree_type_dict = {
    'promoter_feat_only': 0,  # use only promoter feature to build this tree
    'provider_feat_only': 1,  # use only provider feature to build this tree
    'normal_tree': 2,  # a normal decision tree
    'layered_tree': 3  # a layered decision tree
}

tree_actions = {
    'promoter_only': 0,  # use only promoter feature to build this layer
    'provider_only': 1,  # use only provider feature to build this layer
    'promoter_and_provider': 2,  # use global feature to build this layer
}


def create_tree_plan(work_mode: str, k=1, tree_num=10, provider_list=None):
    """
    Args:
        work_mode:
        k: k is needed when work_mode is 'layered'
        tree_num: decision tree number
        provider_list: need to specify provider idx when under multi-provider scenario, default is None
        complete_secure:
    Returns: tree plan: (work mode, provider id) provider id -1 is default value
    """

    LOGGER.info('boosting_core trees work mode is {}'.format(work_mode))
    tree_plan = []

    if work_mode == consts.MIX_TREE:
        assert k > 0
        assert len(provider_list) > 0

        one_round = [(tree_type_dict['promoter_feat_only'], -1)] * k
        for provider_idx, provider_id in enumerate(provider_list):
            one_round += [(tree_type_dict['provider_feat_only'], provider_id)] * k

        round_num = (tree_num // (2 * k)) + 1
        tree_plan = (one_round * round_num)[0:tree_num]

    elif work_mode == consts.LAYERED_TREE:
        tree_plan = [(tree_type_dict['layered_tree'], -1) for i in range(tree_num)]

    return tree_plan


def create_node_plan(tree_type, target_provider_id, max_depth, ) -> List[Tuple[int, int]]:
    LOGGER.debug('cur tree working mode is {}'.format((tree_type, target_provider_id)))
    node_plan = []

    if tree_type == tree_type_dict['promoter_feat_only']:
        node_plan = [(tree_actions['promoter_only'], target_provider_id) for i in range(max_depth)]

    elif tree_type == tree_type_dict['provider_feat_only']:
        node_plan = [(tree_actions['provider_only'], target_provider_id) for i in range(max_depth)]

    return node_plan


def create_layered_tree_node_plan(promoter_depth=0, provider_depth=0, provider_list=None):
    assert promoter_depth > 0 and provider_depth > 0
    assert len(provider_list) > 0

    one_round = []
    for provider_idx, provider_id in enumerate(provider_list):
        one_round += [(tree_type_dict['provider_feat_only'], provider_id)] * provider_depth
    one_round += [(tree_type_dict['promoter_feat_only'], -1)] * promoter_depth

    return one_round


def encode_plan(p, split_token='_'):
    result = []
    for tree_type_or_action, provider_id in p:
        result.append(str(tree_type_or_action) + split_token + str(provider_id))
    return result


def decode_plan(s, split_token='_'):
    result = []
    for string in s:
        t = string.split(split_token)
        result.append((int(t[0]), int(t[1])))

    return result
