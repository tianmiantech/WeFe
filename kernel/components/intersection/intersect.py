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

import hashlib

from common.python import session
from common.python.utils import log_utils

LOGGER = log_utils.get_logger()


class Intersect(object):
    def __init__(self, intersect_params):
        self.transfer_variable = None
        self.only_output_key = intersect_params.only_output_key
        self.sync_intersect_ids = intersect_params.sync_intersect_ids

        self._promoter_id = None
        self._provider_id = None
        self._provider_id_list = None

    @property
    def promoter_member_id(self):
        return self._promoter_id

    @promoter_member_id.setter
    def promoter_member_id(self, promoter_id):
        # if not isinstance(promoter_id, int):
        #     raise ValueError("party id should be integer, but get {}".format(promoter_id))
        self._promoter_id = promoter_id

    @property
    def provider_member_id(self):
        return self._provider_id

    @provider_member_id.setter
    def provider_member_id(self, provider_id):
        # if not isinstance(provider_id, int):
        #     raise ValueError("party id should be integer, but get {}".format(provider_id))
        self._provider_id = provider_id

    @property
    def provider_member_id_list(self):
        return self._provider_id_list

    @provider_member_id_list.setter
    def provider_member_id_list(self, provider_id_list):
        if not isinstance(provider_id_list, list):
            raise ValueError(
                "type provider_member_id should be list, but get {} with {}".format(type(provider_id_list),
                                                                                    provider_id_list))
        self._provider_id_list = provider_id_list

    def run(self, data_instances):
        raise NotImplementedError("method init must be define")

    def set_flowid(self, flowid=0):
        if self.transfer_variable is not None:
            self.transfer_variable.set_flowid(flowid)

    def _set_schema(self, schema):
        self.schema = schema

    def _get_schema(self):
        return self.schema

    def _get_value_from_data(self, intersect_ids, data_instances):
        intersect_ids = intersect_ids.join(data_instances, lambda i, d: (i, d)).map(lambda k, v: (v[0], v[1]))
        intersect_ids.schema = data_instances.schema
        LOGGER.info("get intersect data_instances!")
        return intersect_ids

    def get_common_intersection(self, intersect_ids_list: list):
        if len(intersect_ids_list) == 1:
            return intersect_ids_list[0]

        intersect_ids = None
        for i, value in enumerate(intersect_ids_list):
            if intersect_ids is None:
                intersect_ids = value
                continue
            intersect_ids = intersect_ids.join(value, lambda id, v: id)

        return intersect_ids

    def generate_id_num(self, intersect_ids, has_encrypt_key=True):
        i = 1
        for item in intersect_ids:
            if has_encrypt_key:
                yield item[0], (item[1], i)
            else:
                yield item[0], i
            i = i + 1

    def generate_id_nums(self, intersect_ids, has_encrypt_key=False):
        from common.python.common.consts import NAMESPACE
        data = session.parallelize(self.generate_id_num(intersect_ids.collect(), has_encrypt_key=has_encrypt_key),
                                   namespace=NAMESPACE.PROCESS,
                                   include_key=True,
                                   partition=intersect_ids.get_partitions())
        return data


class DhIntersect(Intersect):
    def __init__(self, intersect_params):
        super().__init__(intersect_params)

    @staticmethod
    def hash(value):
        return hashlib.sha256(bytes(str(value), encoding='utf-8')).hexdigest()
