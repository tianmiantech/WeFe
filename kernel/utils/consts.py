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


ARBITER = 'arbiter'
PROVIDER = 'provider'
PROMOTER = 'promoter'

MODEL_AGG = "model_agg"
GRAD_AGG = "grad_agg"

BINARY = 'binary'
MULTY = 'multi'
CLASSIFICATION = "classification"
REGRESSION = 'regression'
PAILLIER = 'Paillier'
RANDOM_PADS = "RandomPads"
NONE = "None"
AFFINE = 'Affine'
ITERATIVEAFFINE = 'IterativeAffine'
RANDOM_ITERATIVEAFFINE = 'RandomIterativeAffine'
L1_PENALTY = 'L1'
L2_PENALTY = 'L2'

FLOAT_ZERO = 1e-8

PARAM_MAXDEPTH = 5
MAX_CLASSNUM = 1000
MIN_BATCH_SIZE = 10
SPARSE_VECTOR = "SparseVector"

VERT = "vert"
HORZ = "horz"
MIX = "mix"
OOT = 'oot'

RAW = "raw"
RSA = "rsa"
DH = 'dh'

# evaluation
AUC = "auc"
KS = "ks"
LIFT = "lift"
GAIN = "gain"
PRECISION = "precision"
RECALL = "recall"
ACCURACY = "accuracy"
EXPLAINED_VARIANCE = "explained_variance"
MEAN_ABSOLUTE_ERROR = "mean_absolute_error"
MEAN_SQUARED_ERROR = "mean_squared_error"
MEAN_SQUARED_LOG_ERROR = "mean_squared_log_error"
MEDIAN_ABSOLUTE_ERROR = "median_absolute_error"
R2_SCORE = "r2_score"
ROOT_MEAN_SQUARED_ERROR = "root_mean_squared_error"
ROC = "roc"
TOPN = "topn"

# evaluation alias metric
ALL_METRIC_NAME = [AUC, KS, LIFT, GAIN, PRECISION, RECALL, ACCURACY, EXPLAINED_VARIANCE, MEAN_ABSOLUTE_ERROR,
                   MEAN_SQUARED_ERROR, MEAN_SQUARED_LOG_ERROR, MEDIAN_ABSOLUTE_ERROR, R2_SCORE, ROOT_MEAN_SQUARED_ERROR,
                   ROC, TOPN]
ALIAS = {
    ('l1', 'mae', 'regression_l1'): MEAN_ABSOLUTE_ERROR,
    ('l2', 'mse', 'regression_l2', 'regression'): MEAN_SQUARED_ERROR,
    ('l2_root', 'rmse'): ROOT_MEAN_SQUARED_ERROR,
    ('msle',): MEAN_SQUARED_LOG_ERROR,
    ('r2',): R2_SCORE,
    ('acc',): ACCURACY
}

# default evaluation metrics
DEFAULT_BINARY_METRIC = [AUC, KS, TOPN]
DEFAULT_REGRESSION_METRIC = [ROOT_MEAN_SQUARED_ERROR, MEAN_ABSOLUTE_ERROR]
DEFAULT_MULTI_METRIC = [ACCURACY, PRECISION, RECALL]

# allowed metrics for different tasks
ALL_BINARY_METRICS = [
    AUC,
    KS,
    LIFT,
    GAIN,
    ACCURACY,
    PRECISION,
    RECALL,
    ROC,
    TOPN
]

ALL_REGRESSION_METRICS = [
    EXPLAINED_VARIANCE,
    MEAN_ABSOLUTE_ERROR,
    MEAN_SQUARED_ERROR,
    MEDIAN_ABSOLUTE_ERROR,
    R2_SCORE,
    ROOT_MEAN_SQUARED_ERROR
]
ALL_MULTI_METRICS = [
    ACCURACY,
    PRECISION,
    RECALL
]

# single value metrics
REGRESSION_SINGLE_VALUE_METRICS = [
    EXPLAINED_VARIANCE,
    MEAN_ABSOLUTE_ERROR,
    MEAN_SQUARED_ERROR,
    MEAN_SQUARED_LOG_ERROR,
    MEDIAN_ABSOLUTE_ERROR,
    R2_SCORE,
    ROOT_MEAN_SQUARED_ERROR,
]

BINARY_SINGLE_VALUE_METRIC = [
    AUC,
    KS
]

MULTI_SINGLE_VALUE_METRIC = [
    PRECISION,
    RECALL,
    ACCURACY
]

# workflow
TRAIN_DATA = "train_data"
TEST_DATA = "test_data"

# initialize method
RANDOM_NORMAL = "random_normal"
RANDOM_UNIFORM = 'random_uniform'
ONES = 'ones'
ZEROS = 'zeros'
CONST = 'const'

# decision boosting
MAX_SPLIT_NODES = 2 ** 16
MAX_FEDERATED_NODES = 2 ** 10
NORMAL_TREE = 'normal'
COMPLETE_SECURE_TREE = 'complete_secure'
MIX_TREE = 'mix'
LAYERED_TREE = 'layered'

TRAIN_EVALUATE = 'train_evaluate'
VALIDATE_EVALUATE = 'validate_evaluate'

HORZ_SBT = 'horz_sbt'
VERT_SBT = 'vert_sbt'
VERT_FAST_SBT_MIX = 'vert_fast_sbt_mix'
VERT_FAST_SBT_LAYERED = 'vert_fast_sbt_layered'

# tree decimal round to prevent float error
TREE_DECIMAL_ROUND = 10

# Feature engineering
G_BIN_NUM = 10
DEFAULT_COMPRESS_THRESHOLD = 10000
DEFAULT_HEAD_SIZE = 10000
DEFAULT_RELATIVE_ERROR = 0.001
ONE_HOT_LIMIT = 1024  # No more than 10 possible values
SECURE_AGG_AMPLIFY_FACTOR = 1000

QUANTILE = 'quantile'
BUCKET = 'bucket'
OPTIMAL = 'optimal'
CUSTOM = 'custom'
VIRTUAL_SUMMARY = 'virtual_summary'
RECURSIVE_QUERY = 'recursive_query'

# Feature calculation methods
UNIQUE_VALUE = 'unique_value'
IV_VALUE_THRES = 'iv_value_thres'
IV_PERCENTILE = 'iv_percentile'
COEFFICIENT_OF_VARIATION_VALUE_THRES = 'coefficient_of_variation_value_thres'
# COEFFICIENT_OF_VARIATION_PERCENTILE = 'coefficient_of_variation_percentile'
OUTLIER_COLS = 'outlier_cols'
MANUALLY_FILTER = 'manually'

# imputer
MIN = 'min'
MAX = 'max'
MEAN = 'mean'
DESIGNATED = 'designated'
STR = 'str'
FLOAT = 'float'
INT = 'int'
ORIGIN = 'origin'
MEDIAN = 'median'

# min_max_scaler
NORMAL = 'normal'
CAP = 'cap'
MINMAXSCALE = 'min_max_scale'
STANDARDSCALE = 'standard_scale'
ALL = 'all'
COL = 'col'

# intersection cache
PHONE = 'phone'
IMEI = 'imei'
MD5 = 'md5'
SHA256 = 'sha256'
INTERSECT_CACHE_TAG = 'Za'

SELECTIVE_SIZE = 1024


class FeatureStatisticWorkMode:
    AUTO = "auto"
    LOCAL = "local"
    FEDERATION = "federation"
