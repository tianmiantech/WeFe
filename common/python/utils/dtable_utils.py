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

from common.python.utils.core_utils import get_commit_id
from common.python.utils.version_control import get_latest_commit

gen_namespace_separator = '#'


def get_table_info(config, create=False):
    table_name, namespace, role, member_id, all_party, data_type = config.get('table_name'), \
                                                                   config.get('namespace'), \
                                                                   config.get('local', {}).get('role'), \
                                                                   config.get('local', {}).get('member_id'), \
                                                                   config.get('role'), \
                                                                   config.get('data_type')
    if not config.get('gen_table_info', False):
        return table_name, namespace
    if not namespace:
        namespace = gen_party_namespace(all_party=all_party, data_type=data_type, role=role, member_id=member_id)
    if not table_name:
        if create:
            table_name = get_commit_id()
        else:
            table_name = get_latest_commit(data_table_namespace=namespace, branch='master')
    return table_name, namespace


def gen_party_version(namespace, branch='master', create=False):
    if create:
        table_name = get_commit_id()
    else:
        table_name = get_latest_commit(data_table_namespace=namespace, branch=branch)

    return table_name


def gen_party_namespace(all_party, data_type, role, member_id):
    return gen_namespace_separator.join([role, str(member_id), all_party_key(all_party), data_type])


def gen_party_namespace_by_federated_namespace(federated_namespace, role, member_id):
    return gen_namespace_separator.join([role, str(member_id), federated_namespace])


def all_party_key(all_party):
    """
    Join all party as party key
    :param all_party:
        "role": {
            "promoter": [9999],
            "provider": [10000],
            "arbiter": [10000]
         }
    :return:
    """
    if not all_party:
        all_party_key = 'all'
    elif isinstance(all_party, dict):
        sorted_role_name = sorted(all_party.keys())
        all_party_key = gen_namespace_separator.join([
            ('%s-%s' % (
                role_name,
                '_'.join([str(p) for p in sorted(set(all_party[role_name]))]))
             )
            for role_name in sorted_role_name])
    else:
        all_party_key = None
    return all_party_key
