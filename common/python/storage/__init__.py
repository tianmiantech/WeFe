



from enum import Enum


class StoreType(Enum):
    PROCESS = "PROCESS"
    PERSISTENCE = "PERSISTENCE"


class NamingPolicy(Enum):
    DEFAULT = 'DEFAULT'
    ITER_AWARE = 'ITER_AWARE'


class ComputingEngine(Enum):
    LOCAL = 'LOCAL'
    LOCAL_DSOURCE = 'LOCAL_DSOURCE'
