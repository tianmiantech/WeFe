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

ROLES = ["arbiter", "promoter", "provider"]
# TRANSFER_CONF_PATH = "business/transfer_variables/auth_conf"
TRANSFER_CONF_PATH = "kernel/transfer/variables/definition"
CONF_KEY_LOCAL = "local"
CONF_KEY_FEDERATION = "gateway"
CONF_KEY_SERVER = "servers"

# global config
COMM_CONF_KEY_DATA_TYPE = "db.storage.type"
COMM_CONF_KEY_CK_HOST = "db.storage.clickhouse.host"
COMM_CONF_KEY_CK_PORT = "db.storage.clickhouse.tcp.port"
COMM_CONF_KEY_CK_USER = "db.storage.clickhouse.username"
COMM_CONF_KEY_CK_PWD = "db.storage.clickhouse.password"
COMM_CONF_KEY_MYSQL_HOST = "db.mysql.host"
COMM_CONF_KEY_MYSQL_PORT = "db.mysql.port"
COMM_CONF_KEY_MYSQL_DATABASE = "db.mysql.database"
COMM_CONF_KEY_MYSQL_USERNAME = "db.mysql.username"
COMM_CONF_KEY_MYSQL_PASSWORD = "db.mysql.password"

# SQLite config
COMM_CONF_DB_SQLITE_DATABASE = "db.sqlite.database"

COMM_CONF_WEFE_JOB_WORK_MODE = "wefe.job.work_mode"

COMM_CONF_KEY_SPARK_NUM_SLICES = "flow.spark.default.num.slices"
COMM_CONF_KEY_UNION_BASE_URL = "wefe.union.base-url"
COMM_CONF_KEY_LOG_ROOT = "flow.log.root.path"

COMM_CONF_KEY_SPARK_DEFAULT_DRIVER_MEMORY = "flow.spark.submit.default.driver.memory"
COMM_CONF_KEY_SPARK_DEFAULT_DRIVER_MAX_RESULT_SIZE = "flow.spark.submit.default.driver.maxResultSize"
COMM_CONF_KEY_SPARK_DEFAULT_NUM_EXECUTORS = "flow.spark.submit.default.num.executors"
COMM_CONF_KEY_SPARK_DEFAULT_EXECUTOR_MEMORY = "flow.spark.submit.default.executor.memory"
COMM_CONF_KEY_SPARK_DEFAULT_EXECUTOR_CORES = "flow.spark.submit.default.executor.cores"
# COMM_CONF_KEY_BACKEND = "wefe.job.backend"
COMM_CONF_KEY_ACCELERATION = "wefe.job.acceleration"

COMM_CONF_KEY_FC_STORAGE_TYPE = "fc.storage.type"
COMM_CONF_KEY_FC_REGION = "fc.region"
COMM_CONF_KEY_FC_ACCOUNT_ID = "fc.account_id"
COMM_CONF_KEY_FC_OTS_END_POINT = "fc.ots.end_point"
COMM_CONF_KEY_FC_OTS_INTERNAL_END_POINT = "fc.ots.internal_end_point"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_INTERNAL_END_POINT = "fc.cloud_store.temp_auth_internal_end_point"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_END_POINT = "fc.cloud_store.temp_auth_end_point"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_ROLE_ARN = "fc.cloud_store.temp_auth_role_arn"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_ROLE_SESSION_NAME = "fc.cloud_store.temp_auth_role_session_name"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_DURATION_SECONDS = "fc.cloud_store.temp_auth_duration_seconds"
COMM_CONF_KEY_FC_ACCESS_KEY_ID = "fc.access_key_id"
COMM_CONF_KEY_FC_KEY_SECRET = "fc.access_key_secret"
COMM_CONF_KEY_FC_OTS_INSTANCE_NAME = "fc.ots.instance_name"
COMM_CONF_KEY_FC_OSS_BUCKET_NAME = "fc.oss.bucket_name"
COMM_CONF_KEY_FC_END_POINT = "fc.end_point"
COMM_CONF_KEY_FC_QUALIFIER = "fc.qualifier"
COMM_CONF_KEY_FC_SERVICE_NAME = 'fc.service.name'
COMM_CONF_KEY_FC_OSS_ENDPOINT = "fc.oss.endpoint"
COMM_CONF_KEY_FC_OSS_INTERNAL_ENDPOINT = "fc.oss.internal_endpoint"

COMM_CONF_KEY_WEB_IP = "flow.web.ip"
COMM_CONF_KEY_WEB_PORT = "flow.web.port"

ENV_CONF_KEY_FLOW_PORT = "FLOW_PORT"
ENV_CONF_KEY_NGINX_PORT = "NGINX_PORT"

# Configuration of environment variables
ENV_CONF_KEY_GATEWAY_HOST = "GATEWAY_HOST"
ENV_CONF_KEY_GATEWAY_PORT = "GATEWAY_PORT"
ENV_CONF_KEY_CK_HOST = "CK_HOST"
ENV_CONF_KEY_CK_PORT = "CK_PORT"
ENV_CONF_KEY_CONFIG = "config"

# Network Config
ENV_CONF_KEY_INTRANET_IP = "INTRANET_IP"
ENV_CONF_KEY_EXTRANET_IP = "EXTRANET_IP"


class TransferAction(object):
    """
    Transfer action
    """
    DSOURCE = "dsource"
    DOBJECT = "objectM"
    FCSOURCE = 'fcsource'
    STOP_JOB = "stop_job"


class NAMESPACE(object):
    """
    Database
    """
    DATA = "wefe_data"
    PROCESS = "wefe_process"
    TRANSFER = "wefe_transfer"


class BACKEND(object):
    """
    Computing backend：local, spark, fc
    """

    LOCAL = 0
    SPARK = 1
    FC = 2


class STORAGETYPE(object):
    """
    Storage Type： ck, lmdb, ots
    """
    CLICKHOUSE = 'clickhouse'
    LMDB = 'lmdb'
    OTS = 'ots'
    OSS = 'oss'


class TABLENAME(object):
    FRAGMENTS = "fragments"


class ProjectStatus(object):
    AGREE = "agree"
    AUDITING = "auditing"


class ProjectFlowStatus(object):
    EDITING = "editing"
    RUNNING = "running"
    FINISHED = "finished"
    ERROR_ON_RUNNING = 'error_on_running'
    STOP_ON_RUNNING = 'stop_on_running'


class JobStatus(object):
    WAIT_RUN = 'wait_run'
    WAIT_STOP = 'wait_stop'
    RUNNING = 'running'
    STOP_ON_RUNNING = 'stop_on_running'
    ERROR_ON_RUNNING = 'error_on_running'
    SUCCESS = 'success'
    WAIT_SUCCESS = 'wait_success'
    TIMEOUT = 'timeout'

    @staticmethod
    def is_finished(status):
        """
        Determine whether the specified task status is stopped
        """
        return status == JobStatus.STOP_ON_RUNNING \
               or status == JobStatus.ERROR_ON_RUNNING \
               or status == JobStatus.SUCCESS


class TaskStatus(object):
    # CREATED = 'created'
    WAITRUN = 'wait_run'
    RUNNING = 'running'
    SUCCESS = 'success'
    ERROR = 'error'
    TIMEOUT = 'timeout'
    STOP = 'stop'


class JobBusinessType(object):
    JOB = 'Job'
    FEATURE = 'Feature'


class DataSetSourceType(object):
    RAW = 'Raw'
    INTERSECT = 'Intersect'
    BINNING = 'Binning'
    FEATURESELECTION = "FeatureSelection"
    FILLMISSINGVALUE = "FillMissingValue"


class ENV(object):
    ENV_DEV = 'dev'
    ENV_PROD = 'prod'
    ENV_LOCAL = 'local'


class TaskResultDataType(object):
    """
    Data type of task result
    """
    DATA = "data"
    MODEL = "model"
    METRIC = 'metric'
    TRAINING_MODEL = "training_model"


class MemberRole(object):
    """
    Member role
    """
    PROVIDER = "provider"
    PROMOTER = "promoter"
    ARBITER = "arbiter"


class ComponentName:
    DATA_IO = "DataIO"
    INTERSECTION = "Intersection"
    FEATURE_STATISTIC = "FeatureStatistic"
    FILL_MISSING_VALUE = "FillMissingValue"
    BINNING = "Binning"
    FEATURE_CALCULATION = "FeatureCalculation"
    FEATURE_SELECTION = "FeatureSelection"
    SEGMENT = "Segment"
    HORZ_LR = "HorzLR"
    VERT_LR = "VertLR"
    HORZ_SECURE_BOOST = "HorzSecureBoost"
    VERT_SECURE_BOOST = "VertSecureBoost"
    VERT_FAST_SECURE_BOOST = "VertFastSecureBoost"
    HORZ_LR_VALIDATION_DATA_SET_LOADER = "HorzLRValidationDataSetLoader"
    VERT_LR_VALIDATION_DATA_SET_LOADER = "VertLRValidationDataSetLoader"
    HORZ_XG_BOOST_VALIDATION_DATA_SET_LOADER = "HorzXGBoostValidationDataSetLoader"
    VERT_XG_BOOST_VALIDATION_DATA_SET_LOADER = "VertXGBoostValidationDataSetLoader"
    HORZ_FEATURE_BINNING = "HorzFeatureBinning"
    VERT_FEATURE_BINNING = "VertFeatureBinning"
    VERT_FEATURE_CALCULATION = "VertFeatureCalculation"
    OOT = "Oot"
    EVALUATION = "Evaluation"


class FederatedLearningType:
    HORIZONTAL = "horizontal"
    VERTICAL = "vertical"
    MIXTURE = "mixture"


class DataSetType:
    NORMAL_DATA_SET = "normal"
    TRAIN_DATA_SET = "train"
    EVALUATION_DATA_SET = "evaluation"


class ModelType:
    TRAIN_MODEL = "train"
    BINNING_MODEL = "binning"


class GatewayTransferProcess(object):
    """
    gateway transfer process
    """
    MEMORY_PROCESS = 'residentMemoryProcessor'
    DSOURCE_PROCESS = 'dSourceProcessor'
    GATEWAY_ALIVE_PROCESS = 'gatewayAliveProcessor'
    REFRESH_SYSTEM_CONFIG_CACHE_PROCESS = "refreshSystemConfigCacheProcessor"


class FunctionIndexName(object):
    """
    function index
    """
    INDEX = "index"
    HIGH_PERFORMANCE_INDEX = "hpIndex"
    SUPER_HIGH_PERFORMANCE_INDEX = "shpIndex"


class FunctionConfig(object):
    """
    FC config
    """
    FC_DEFAULT_PARTITION = 10
    FC_MAX_PARTITION = 100
    FC_PARTITION_DATA_SIZE = 5000


class AccelerationType(object):
    """
    acceleration type
    """
    GPU = "GPU"
    FPGA = "FPGA"


class IntermediateDataFlag(object):
    """
    Intermediate data serialization type
    """
    ITEM_SERIALIZATION = 1
    BATCH_SERIALIZATION = 2


class RuntimeOptionKey(object):
    """
    Runtime option key
    """
    FC_PARTITION = "fc_partition"
    SPARK_PARTITION = "spark_partition"
    FEATURE_COUNT = "features_count"
    MEMBERS_BACKEND = "members_backend"


if __name__ == '__main__':
    pass
    # print(BACKEND.__dict__.get("SPARK")[0])
