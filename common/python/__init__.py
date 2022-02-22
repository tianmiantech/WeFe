



from enum import IntEnum

from common.python.calculation.acceleration.abc.aclr_abc import ACLR_ABC
from common.python.common import consts
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


class Backend(IntEnum):
    LOCAL = 0
    SPARK = 1
    FC = 2

    def is_spark(self):
        return self.value == self.SPARK

    def is_local(self):
        return self.value == self.LOCAL

    def is_fc(self):
        return self.value == self.FC

    @staticmethod
    def get(task_config_json):
        # backend = task_config_json['job']['env']['backend']
        backend = conf_utils.get_backend_from_string(
            conf_utils.get_comm_config(consts.COMM_CONF_KEY_BACKEND)
        )
        backend = int(backend) if backend else task_config_json['job']['env']['backend']
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
        return cls.OPTIONS.get("fc_partition")

    @classmethod
    def get_spark_partition(cls):
        return cls.OPTIONS.get("spark_partition")

    @classmethod
    def get_features_count(cls):
        return cls.OPTIONS.get("features_count")

    @classmethod
    def get_alcr_ins(cls):
        if cls.ACLR_INS is None:
            aclr_type = conf_utils.get_comm_config(consts.COMM_CONF_KEY_ACCELERATION, "")
            if aclr_type == consts.AccelerationType.GPU:
                from common.python.calculation.acceleration.gpu.aclr_gpu import ACLR_GPU
                cls.ACLR_INS = ACLR_GPU()
        return cls.ACLR_INS
