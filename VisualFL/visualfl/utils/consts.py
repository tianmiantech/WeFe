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


JOB_TYPE_PADDLE = "paddle_fl"
TASK_TYPE_AGG = "fl_aggregator"
TASK_TYPE_TRAINER = "fl_trainer"


ROLES = ["arbiter", "promoter", "provider"]

# global config

COMM_CONF_KEY_MYSQL_HOST = "db.mysql.host"
COMM_CONF_KEY_MYSQL_PORT = "db.mysql.port"
COMM_CONF_KEY_MYSQL_DATABASE = "db.mysql.database"
COMM_CONF_KEY_MYSQL_USERNAME = "db.mysql.username"
COMM_CONF_KEY_MYSQL_PASSWORD = "db.mysql.password"
COMM_CONF_IS_LOCAL = "is_local"

# SQLite config
COMM_CONF_DB_SQLITE_DATABASE = "db.sqlite.database"

COMM_CONF_WEFE_JOB_WORK_MODE = "wefe.job.work_mode"


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

class MemberRole(object):
    """
    Member role
    """
    PROVIDER = "provider"
    PROMOTER = "promoter"
    ARBITER = "arbiter"

class ComponentName(object):
    """
    component name
    """
    CLASSIFY = "PaddleClassify"
    DETECTION = "PaddleDetection"

class TaskResultType(object):
    """
    task result type
    """
    LOSS = "loss"
    ACCURACY = "accuracy"
    MAP = "mAP"
    INFER = "infer"
    LABEL = "label"

if __name__ == '__main__':
    pass
