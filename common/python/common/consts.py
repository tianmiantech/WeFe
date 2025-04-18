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

# global config start ------------------------------------------------------------------------------

# global config: clickhouse_storage_config
CLICKHOUSE_STORAGE_CONFIG = "clickhouse_storage_config"
COMM_CONF_KEY_CK_HOST = CLICKHOUSE_STORAGE_CONFIG, "host"
COMM_CONF_KEY_CK_PORT = CLICKHOUSE_STORAGE_CONFIG, "tcp_port"
COMM_CONF_KEY_CK_HTTP_PORT = CLICKHOUSE_STORAGE_CONFIG, "http_port"
COMM_CONF_KEY_CK_USER = CLICKHOUSE_STORAGE_CONFIG, "username"
COMM_CONF_KEY_CK_PWD = CLICKHOUSE_STORAGE_CONFIG, "password"

STORAGE_CONFIG = "storage_config"
COMM_CONF_KEY_DATA_TYPE = STORAGE_CONFIG, "storage_type"

# SPARK_STANDALONE_CONFIG
SPARK_STANDALONE_CONFIG = "spark_standalone_config"
COMM_CONF_KEY_SPARK_DEFAULT_DRIVER_MEMORY = SPARK_STANDALONE_CONFIG, "driver_memory"
COMM_CONF_KEY_SPARK_DEFAULT_DRIVER_MAX_RESULT_SIZE = SPARK_STANDALONE_CONFIG, "driver_max_result_size"
COMM_CONF_KEY_SPARK_DEFAULT_EXECUTOR_MEMORY = SPARK_STANDALONE_CONFIG, "executor_memory"
COMM_CONF_KEY_ACCELERATION = SPARK_STANDALONE_CONFIG, "hardware_acceleration"

FUNCTION_COMPUTE_CONFIG = "function_compute_config"
COMM_CONF_CLOUD_PROVIDER = FUNCTION_COMPUTE_CONFIG, "cloud_provider"

ALIYUN_FUNCTION_COMPUTE_CONFIG = "aliyun_function_compute_config"
COMM_CONF_KEY_FC_ACCOUNT_TYPE = ALIYUN_FUNCTION_COMPUTE_CONFIG, "account_type"
COMM_CONF_KEY_FC_V_SWITCH_IDS = ALIYUN_FUNCTION_COMPUTE_CONFIG, "v_switch_ids"
COMM_CONF_KEY_FC_VPC_ID = ALIYUN_FUNCTION_COMPUTE_CONFIG, "vpc_id"
COMM_CONF_KEY_FC_SECURITY_GROUP_ID = ALIYUN_FUNCTION_COMPUTE_CONFIG, "security_group_id"
COMM_CONF_KEY_FC_REGION = ALIYUN_FUNCTION_COMPUTE_CONFIG, "region"
COMM_CONF_KEY_FC_ACCOUNT_ID = ALIYUN_FUNCTION_COMPUTE_CONFIG, "account_id"
COMM_CONF_KEY_FC_ACCESS_KEY_ID = ALIYUN_FUNCTION_COMPUTE_CONFIG, "access_key_id"
COMM_CONF_KEY_FC_KEY_SECRET = ALIYUN_FUNCTION_COMPUTE_CONFIG, "access_key_secret"
COMM_CONF_KEY_FC_OSS_BUCKET_NAME = ALIYUN_FUNCTION_COMPUTE_CONFIG, "oss_bucket_name"
COMM_CONF_KEY_FC_QUALIFIER = ALIYUN_FUNCTION_COMPUTE_CONFIG, "qualifier"

TENCENT_SERVERLESS_CLOUD_FUNCTION_CONFID = "tencent_serverless_cloud_function_config"
COMM_CONF_KEY_SCF_REGION = TENCENT_SERVERLESS_CLOUD_FUNCTION_CONFID, "region"
COMM_CONF_KEY_SCF_ACCOUNT_ID = TENCENT_SERVERLESS_CLOUD_FUNCTION_CONFID, "account_id"
COMM_CONF_KEY_SCF_ACCESS_KEY_ID = TENCENT_SERVERLESS_CLOUD_FUNCTION_CONFID, "access_key_id"
COMM_CONF_KEY_SCF_KEY_SECRET = TENCENT_SERVERLESS_CLOUD_FUNCTION_CONFID, "access_key_secret"
COMM_CONF_KEY_SCF_COS_BUCKET_NAME = TENCENT_SERVERLESS_CLOUD_FUNCTION_CONFID, "cos_bucket_name"
COMM_CONF_KEY_SCF_QUALIFIER = TENCENT_SERVERLESS_CLOUD_FUNCTION_CONFID, "qualifier"
COMM_CONF_KEY_SCF_SERVER_URL = TENCENT_SERVERLESS_CLOUD_FUNCTION_CONFID, "scf_server_url"

COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_INTERNAL_END_POINT = ALIYUN_FUNCTION_COMPUTE_CONFIG, "temp_auth_internal_end_point"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_END_POINT = ALIYUN_FUNCTION_COMPUTE_CONFIG, "temp_auth_end_point"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_ROLE_ARN = ALIYUN_FUNCTION_COMPUTE_CONFIG, "temp_auth_role_arn"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_ROLE_SESSION_NAME = ALIYUN_FUNCTION_COMPUTE_CONFIG, "temp_auth_role_session_name"
COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_DURATION_SECONDS = ALIYUN_FUNCTION_COMPUTE_CONFIG, "temp_auth_duration_seconds"
COMM_CONF_KEY_FC_END_POINT = ALIYUN_FUNCTION_COMPUTE_CONFIG, "end_point"
COMM_CONF_KEY_FC_SERVICE_NAME = ALIYUN_FUNCTION_COMPUTE_CONFIG, "service_name"
COMM_CONF_KEY_FC_OSS_ENDPOINT = ALIYUN_FUNCTION_COMPUTE_CONFIG, "oss_endpoint"
COMM_CONF_KEY_FC_OSS_INTERNAL_ENDPOINT = ALIYUN_FUNCTION_COMPUTE_CONFIG, "oss_internal_endpoint"
# global config end ------------------------------------------------------------------------------

# member-base.properties start -----------------------------------------------------------
MEMBER_BASE = "member.base"
COMM_CONF_KEY_MYSQL_HOST = MEMBER_BASE, "db.mysql.host"
COMM_CONF_KEY_MYSQL_PORT = MEMBER_BASE, "db.mysql.port"
COMM_CONF_KEY_MYSQL_DATABASE = MEMBER_BASE, "db.mysql.database"
COMM_CONF_KEY_MYSQL_USERNAME = MEMBER_BASE, "db.mysql.username"
COMM_CONF_KEY_MYSQL_PASSWORD = MEMBER_BASE, "db.mysql.password"
COMM_CONF_KEY_EXAMPLE_RUN = MEMBER_BASE, "example.run"
COMM_CONF_KEY_LOG_ROOT = MEMBER_BASE, "flow.log.root.path"
# member-base.properties end -----------------------------------------------------------


# common.properties start -------------------------------------------------------------
COMMON = "common"
COMM_CONF_KEY_UNION_BASE_URL = COMMON, "wefe.union.base-url"
COMM_CONF_KEY_PRIVACY_DATABASE_ENCRYPT_ENABLE = COMMON, "privacy.database.encrypt.enable"
COMM_CONF_KEY_PRIVACY_DATABASE_ENCRYPT_SECRET_KEY = COMMON, "privacy.database.encrypt.secret.key"
# common.properties end ---------------------------------------------------------------


COMM_CONF_KEY_SPARK_NUM_SLICES = "flow.spark.default.num.slices"


COMM_CONF_KEY_SPARK_DEFAULT_NUM_EXECUTORS = "flow.spark.submit.default.num.executors"
COMM_CONF_KEY_SPARK_DEFAULT_EXECUTOR_CORES = "flow.spark.submit.default.executor.cores"
# COMM_CONF_KEY_BACKEND = "wefe.job.backend"
COMM_CONF_KEY_GPU_INSTANCE = "wefe.gpu.instance"

# FC config
COMM_CONF_KEY_FC_STORAGE_TYPE = "fc.storage.type"
COMM_CONF_MAKE_TENCENT_IMAGE = "fc.make.tencent.image"
# COMM_CONF_KEY_FC_OTS_END_POINT = "fc.ots.end_point"
# COMM_CONF_KEY_FC_OTS_INTERNAL_END_POINT = "fc.ots.internal_end_point"
# COMM_CONF_KEY_FC_OTS_INSTANCE_NAME = "fc.ots.instance_name"


# SM4 config
COMM_CONF_SM4_SECRET_KEY = "sm4.secret.key"

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
    COS = 'cos'

class CLOUDPROVIDER(object):
    """
    cloud Provider: aliyun, tencentcloud
    """
    ALIYUN = 'aliyun'
    TENCENTCLOUD = 'tencentcloud'


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
    VERT_DP_SECURE_BOOST = "VertDPSecureBoost"
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
    SPARK_MAX_PARTITION = 200


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
    MEMBERS_FC_PROVIDER = "members_fc_provider"


if __name__ == '__main__':
    pass
    # print(BACKEND.__dict__.get("SPARK")[0])
