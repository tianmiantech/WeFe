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


from enum import IntEnum

from common.python.calculation.acceleration.abc.aclr_abc import ACLR_ABC
from common.python.common import consts
from common.python.common.consts import RuntimeOptionKey
from common.python.p_federation.federation import Federation
from common.python.p_session.build import Builder
from common.python.utils import conf_utils
from common.python.utils.wrap import FederationWrapped

_STORAGE_VERSION = 1


class WorkMode(IntEnum):
    STANDALONE = 0
    CLUSTER = 1

    def is_standalone(self):
        return self.value == self.STANDALONE

    def is_cluster(self):
        return self.value == self.CLUSTER


class Backend(object):
    LOCAL = "LOCAL"
    SPARK = "SPARK"
    FC = "FC"

    def __init__(self, value):
        self.value = value

    def is_spark(self):
        return self.value == self.SPARK

    def is_local(self):
        return self.value == self.LOCAL

    def is_fc(self):
        return self.value == self.FC

    def get(self):
        return self.value

    def __str__(self):
        return self.value

    @staticmethod
    def get_by_task_config(task_config_json):
        backend = task_config_json['job']['env']['backend']
        return Backend(backend)


class RuntimeInstance(object):
    SESSION = None
    MODE: WorkMode = None
    FEDERATION: Federation = None
    TABLE_WRAPPER: FederationWrapped = None
    BACKEND: Backend = None
    BUILDER: Builder = None
    OPTIONS: dict = None
    ACLR_INS: ACLR_ABC = None

    @classmethod
    def get_fc_partition(cls):
        return cls.OPTIONS.get(RuntimeOptionKey.FC_PARTITION)

    @classmethod
    def get_spark_partition(cls):
        return cls.OPTIONS.get(RuntimeOptionKey.SPARK_PARTITION)

    @classmethod
    def get_features_count(cls):
        return cls.OPTIONS.get(RuntimeOptionKey.FEATURE_COUNT)

    @classmethod
    def get_member_backend(cls, member_id) -> str:
        return cls.OPTIONS.get(RuntimeOptionKey.MEMBERS_BACKEND).get(member_id)

    @classmethod
    def get_alcr_ins(cls):
        if cls.ACLR_INS is None:
            aclr_type = conf_utils.get_comm_config(consts.COMM_CONF_KEY_ACCELERATION, "")
            if aclr_type == consts.AccelerationType.GPU:
                from common.python.calculation.acceleration.gpu.aclr_gpu import ACLR_GPU
                cls.ACLR_INS = ACLR_GPU()
        return cls.ACLR_INS
