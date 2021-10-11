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

import inspect
import operator
import sys

from peewee import *
from peewee import CharField
from playhouse.pool import PooledMySQLDatabase

from common.python.common import consts
from common.python.utils import log_utils, sqlite_utils
from common.python.utils.conf_utils import get_comm_config, get_env_config

stat_logger = log_utils.get_logger("wefe_flow_stat")

# Database Connectivity
host = get_comm_config(consts.COMM_CONF_KEY_MYSQL_HOST)
password = get_comm_config(consts.COMM_CONF_KEY_MYSQL_PASSWORD)
port = int(get_comm_config(consts.COMM_CONF_KEY_MYSQL_PORT))
user = get_comm_config(consts.COMM_CONF_KEY_MYSQL_USERNAME)
database = get_comm_config(consts.COMM_CONF_KEY_MYSQL_DATABASE)

# Environment variable
env_host = get_env_config(consts.COMM_CONF_KEY_MYSQL_HOST)
env_password = get_env_config(consts.COMM_CONF_KEY_MYSQL_PASSWORD)
env_port = get_env_config(consts.COMM_CONF_KEY_MYSQL_PORT)
if env_port:
    env_port = int(env_port)
env_user = get_env_config(consts.COMM_CONF_KEY_MYSQL_USERNAME)
env_database = get_env_config(consts.COMM_CONF_KEY_MYSQL_DATABASE)

settings = {'host': env_host or host,
            'password': env_password or password,
            'port': env_port or port,
            'user': env_user or user,
            'max_connections': 100
            }

work_mode = get_comm_config(consts.COMM_CONF_WEFE_JOB_WORK_MODE)

DB = None

if int(work_mode) == 0:
    stat_logger.debug("Use SQLite")
    DB = sqlite_utils.get_sqlite_db()
else:
    stat_logger.debug("Use Mysql")
    DB = PooledMySQLDatabase(env_database or database, **settings)


class ModelBase(Model):
    id = CharField(primary_key=True)
    created_by = CharField(null=True)
    created_time = DateTimeField()
    updated_by = CharField(null=True)
    updated_time = DateTimeField()

    class Meta:
        database = DB

    def to_json(self):
        return self.__dict__['__data__']


class JobApplyResult(Model):
    id = CharField(primary_key=True)
    created_by = CharField(null=True)
    created_time = DateTimeField()
    updated_by = CharField(null=True)
    updated_time = DateTimeField()
    job_id = CharField()
    task_id = CharField()

    class Meta:
        database = DB

    def to_json(self):
        return self.__dict__['__data__']


# GlobalSetting
class GlobalSetting(object):
    """
    Get global setting

    Due to the adjustment of the GlobalSetting table,
    it is now necessary to obtain the relevant configuration from GlobalConfig
    """

    @staticmethod
    def get_member_id():
        from common.python.db.global_config_dao import GlobalConfigDao
        return GlobalConfigDao.getMemberInfo().member_id

    @staticmethod
    def get_member_name():
        from common.python.db.global_config_dao import GlobalConfigDao
        return GlobalConfigDao.getMemberInfo().member_name

    @staticmethod
    def get_gateway_host():
        from common.python.db.global_config_dao import GlobalConfigDao
        gateway_uri = GlobalConfigDao.getMemberInfo().member_gateway_uri
        return gateway_uri.split(':')[0] if gateway_uri else ""

    @staticmethod
    def get_gateway_port():
        from common.python.db.global_config_dao import GlobalConfigDao
        gateway_uri = GlobalConfigDao.getMemberInfo().member_gateway_uri
        return gateway_uri.split(':')[1] if gateway_uri else ""

    @staticmethod
    def get_member_allow_public_data_set():
        from common.python.db.global_config_dao import GlobalConfigDao
        return GlobalConfigDao.getMemberInfo().member_allow_public_data_set

    @staticmethod
    def get_rsa_private_key():
        from common.python.db.global_config_dao import GlobalConfigDao
        return GlobalConfigDao.getMemberInfo().rsa_private_key


# DataSet
class DataSet(ModelBase):
    column_count = IntegerField()
    column_name_list = TextField()
    contains_y = IntegerField()
    created_by = CharField(null=True)
    created_time = DateTimeField(constraints=[SQL("DEFAULT CURRENT_TIMESTAMP")])
    description = CharField(null=True)
    feature_count = IntegerField()
    feature_name_list = TextField(null=True)
    id = CharField(primary_key=True)
    name = CharField()
    namespace = CharField()
    primary_key_column = CharField()
    public_level = CharField(null=True)
    public_member_list = CharField(null=True)
    row_count = BigIntegerField()
    source_flow_id = CharField(null=True)
    source_job_id = CharField(null=True)
    source_task_id = CharField(null=True)
    source_type = CharField(null=True)
    storage_type = CharField(null=True)
    table_name = CharField()
    tags = CharField(null=True)
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)
    usage_count_in_flow = IntegerField(constraints=[SQL("DEFAULT 0")])
    usage_count_in_job = IntegerField(constraints=[SQL("DEFAULT 0")])
    usage_count_in_project = IntegerField(constraints=[SQL("DEFAULT 0")])
    y_count = IntegerField()
    y_name_list = TextField(null=True)

    class Meta:
        table_name = 'data_set'

    # wingo: use in local test
    @staticmethod
    def check_if_data_exit(data_set_id):
        with DB.connection_context():
            data_set = DataSet.get_or_none()
            if data_set is None:
                raise Exception("need register from board,then start flow")
            data = data_set.get_or_none(DataSet.id == data_set_id)
            if data is None:
                return None


# DataSetColumn
class DataSetColumn(ModelBase):
    data_set_id = CharField()
    index = IntegerField()
    name = CharField()
    data_type = CharField()
    comment = CharField()
    empty_rows = IntegerField()
    value_distribution = CharField()

    class Meta:
        db_table = "data_set_column"

    @staticmethod
    def get_column_data_type_list_by_data_set_id(data_set_id):
        with DB.connection_context():
            data_set_column = DataSetColumn.get_or_none()
            if data_set_column is None:
                raise Exception("need register from board,then start flow")
            data = data_set_column.select(). \
                where(DataSetColumn.data_set_id == data_set_id). \
                order_by(DataSetColumn.index)
            column_data_type_list = []
            for each in data:
                if 'id' == each.name:
                    continue
                column_data_type_list.append(each.name + '_' + each.data_type)
            return column_data_type_list

    @staticmethod
    def update_value_distribution_by_data_set_id_and_name(data_set_id, name, json_result):
        with DB.connection_context():
            data_set_column = DataSetColumn.get_or_none()
            if data_set_column is None:
                raise Exception("need register from board,then start flow")
            data = data_set_column.get(DataSetColumn.data_set_id == data_set_id, DataSetColumn.name == name)
            data.value_distribution = json_result
            data.save()

    @staticmethod
    def get_column_type_by_data_set_id_and_name(data_set_id, name):
        with DB.connection_context():
            data_set_column = DataSetColumn.get_or_none()
            if data_set_column is None:
                raise Exception("need register from board,then start flow")
            data = data_set_column.get(DataSetColumn.data_set_id == data_set_id, DataSetColumn.name == name)
            return data.data_type

    @staticmethod
    def check_if_with_label_by_data_set_id(data_set_id):
        with DB.connection_context():
            data_set_column = DataSetColumn.get_or_none()
            if data_set_column is None:
                raise Exception("need register from board,then start flow")
            data = data_set_column.get_or_none(DataSetColumn.data_set_id == data_set_id, DataSetColumn.name == 'y')
            return data


class ProjectDataSet(ModelBase):
    audit_comment = CharField(null=True)
    audit_status = CharField(null=True)
    created_by = CharField(null=True)
    created_time = DateTimeField(constraints=[SQL("DEFAULT CURRENT_TIMESTAMP")])
    data_set_id = CharField(null=True)
    id = CharField(primary_key=True)
    member_id = CharField()
    member_role = CharField()
    project_id = CharField()
    source_job_id = CharField(null=True)
    source_task_id = CharField(null=True)
    source_type = CharField(constraints=[SQL("DEFAULT 'Raw'")])
    status_updated_time = DateTimeField(null=True)
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)

    class Meta:
        table_name = 'project_data_set'
        indexes = (
            (('project_id', 'data_set_id'), True),
        )


# FlowActionLog
class FlowActionLog(ModelBase):
    producer = CharField()
    priority = IntegerField()
    action = CharField()
    params = CharField()
    status = CharField()
    remark = CharField()
    consumer_ip = CharField()

    class Meta:
        db_table = "flow_action_log"


# FlowActionQueue
class FlowActionQueue(ModelBase):
    producer = CharField()
    priority = IntegerField()
    action = CharField()
    params = CharField()

    class Meta:
        db_table = "flow_action_queue"


# Message
class Message(ModelBase):
    producer = CharField()
    level = IntegerField()
    title = CharField()
    content = CharField()
    unread = BooleanField()

    class Meta:
        db_table = "message"


# Job
class Job(ModelBase):
    created_by = CharField(null=True)
    created_time = DateTimeField(constraints=[SQL("DEFAULT CURRENT_TIMESTAMP")])
    federated_learning_type = CharField()
    finish_time = DateTimeField(null=True)
    flow_id = CharField()
    graph = TextField(null=True)
    has_modeling_result = IntegerField(constraints=[SQL("DEFAULT 0")])
    id = CharField(primary_key=True)
    job_id = CharField()
    message = TextField(null=True)
    my_role = CharField()
    name = CharField()
    progress = IntegerField(constraints=[SQL("DEFAULT 0")])
    progress_updated_time = DateTimeField(null=True)
    project_id = CharField()
    remark = TextField(null=True)
    star = IntegerField(constraints=[SQL("DEFAULT 0")])
    start_time = DateTimeField(null=True)
    status = CharField(constraints=[SQL("DEFAULT 'created'")])
    status_updated_time = DateTimeField(null=True)
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)
    job_middle_data_is_clear = IntegerField(constraints=[SQL("DEFAULT 0")])

    class Meta:
        db_table = 'job'
        indexes = (
            (('job_id', 'my_role'), True),
        )

    @staticmethod
    def getByParam(**kwargs):
        with DB.connection_context():
            filters = []
            for n, v in kwargs.items():
                attr_name = n
                if hasattr(Job, attr_name):
                    filters.append(operator.attrgetter(n)(Job) == v)
            if filters:
                jobs = Job.select().where(*filters)
                return [job for job in jobs]
            else:
                return []


# JobMember
class JobMember(ModelBase):
    created_by = CharField(null=True)
    created_time = DateTimeField(constraints=[SQL("DEFAULT CURRENT_TIMESTAMP")])
    data_set_id = CharField(null=True)
    flow_id = CharField()
    id = CharField(primary_key=True)
    job_id = CharField()
    job_role = CharField()
    member_id = CharField()
    project_id = CharField()
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)

    class Meta:
        db_table = "job_member"

    @staticmethod
    def getByParam(**kwargs):
        with DB.connection_context():
            filters = []
            for n, v in kwargs.items():
                attr_name = n
                if hasattr(JobMember, attr_name):
                    filters.append(operator.attrgetter(n)(JobMember) == v)
            if filters:
                jobMembers = JobMember.select().where(*filters)
                return [jobMember for jobMember in jobMembers]
            else:
                return []


# Task
class Task(ModelBase):
    created_by = CharField(null=True)
    created_time = DateTimeField(constraints=[SQL("DEFAULT CURRENT_TIMESTAMP")])
    deep = IntegerField(null=True)
    dependence_list = CharField(null=True)
    error_cause = TextField(null=True)
    finish_time = DateTimeField(null=True)
    flow_id = CharField()
    flow_node_id = CharField()
    id = CharField(primary_key=True)
    job_id = CharField()
    message = CharField(null=True)
    name = CharField()
    parent_task_id_list = CharField(null=True)
    pid = IntegerField(null=True)
    position = IntegerField(null=True)
    project_id = CharField(null=True)
    role = CharField(null=True)
    spend = IntegerField(null=True)
    start_time = DateTimeField(null=True)
    status = CharField()
    task_conf = TextField()
    task_id = CharField()
    task_type = CharField()
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)

    class Meta:
        table_name = 'task'

    @staticmethod
    def getByParam(**kwargs):
        with DB.connection_context():
            filters = []
            for n, v in kwargs.items():
                attr_name = n
                if hasattr(Task, attr_name):
                    filters.append(operator.attrgetter(n)(Task) == v)
            if filters:
                tasks = Task.select().where(*filters)
                return [task for task in tasks]
            else:
                return []


# TaskContext
class TaskContext(ModelBase):
    business_id = CharField()
    business_type = CharField()
    task_id = CharField()
    name = CharField()
    value = CharField()

    class Meta:
        db_table = "task_context"


class TaskResult(ModelBase):
    """
    Component result save
    """
    component_type = CharField()
    created_by = CharField(null=True)
    created_time = DateTimeField(constraints=[SQL("DEFAULT CURRENT_TIMESTAMP")], index=True)
    flow_id = CharField()
    flow_node_id = CharField()
    id = CharField(primary_key=True)
    job_id = CharField()
    name = CharField()
    project_id = CharField(null=True)
    result = TextField()
    role = CharField()
    serving_model = IntegerField(constraints=[SQL("DEFAULT 0")])
    task_id = CharField()
    type = CharField()
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)

    class Meta:
        table_name = 'task_result'
        indexes = (
            (('task_id', 'type', 'role'), True),
        )


class CurrentBestModel(ModelBase):
    job_id = CharField()
    role = CharField(max_length=50, index=True)
    task_id = CharField(max_length=100)
    member_id = CharField(max_length=50, index=True)
    component_name = TextField()
    iteration = IntegerField()
    model_meta = TextField()
    model_param = TextField()

    class Meta:
        db_table = "cur_best_model"


class ProviderModelParams(ModelBase):
    job_id = CharField()
    role = CharField(max_length=50, index=True)
    task_id = CharField(max_length=100)
    member_id = CharField(max_length=50, index=True)
    component_name = TextField()
    provider_member_id = CharField(max_length=50, index=True)
    provider_model_param = TextField()

    class Meta:
        db_table = "provider_model_params"


class GlobalConfigModel(ModelBase):
    id = CharField()
    group = CharField()
    name = CharField()
    value = CharField()

    class Meta:
        db_table = "global_config"


class Account(ModelBase):
    email = CharField()

    class Meta:
        db_table = "account"


class Project(ModelBase):
    created_by = CharField(null=True)
    created_time = DateTimeField(constraints=[SQL("DEFAULT CURRENT_TIMESTAMP")])
    finish_time = DateTimeField(null=True)
    id = CharField(primary_key=True)
    member_id = CharField()
    message = TextField(null=True)
    my_role = CharField()
    name = CharField()
    progress = IntegerField(constraints=[SQL("DEFAULT 0")])
    progress_updated_time = DateTimeField(null=True)
    project_desc = TextField(null=True)
    project_id = CharField(unique=True)
    project_status = CharField(constraints=[SQL("DEFAULT 'created'")])
    start_time = DateTimeField(null=True)
    status_updated_time = DateTimeField(null=True)
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)

    class Meta:
        table_name = 'project'


class ProjectFlow(ModelBase):
    """
    Project flow
    """
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)
    flow_id = CharField(null=True)
    flow_status = CharField()

    class Meta:
        table_name = 'project_flow'


class TaskProgress(ModelBase):
    """
    Task progress
    """
    created_by = CharField(null=True)
    created_time = DateTimeField()
    expect_end_time = DateTimeField(null=True)
    expect_work_amount = IntegerField(null=True)
    flow_id = CharField()
    flow_node_id = CharField()
    id = CharField(primary_key=True)
    job_id = CharField()
    progress = IntegerField(null=True)
    progress_rate = DecimalField(null=True)
    project_id = CharField(null=True)
    really_work_amount = IntegerField(null=True)
    role = CharField()
    spend = IntegerField(null=True)
    task_id = CharField()
    task_type = CharField()
    updated_by = CharField(null=True)
    updated_time = DateTimeField(null=True)
    pid_success = IntegerField(null=True)

    class Meta:
        table_name = 'task_progress'
        indexes = (
            (('task_id', 'role'), True),
        )


if int(work_mode) == 0:
    members = inspect.getmembers(sys.modules[__name__], inspect.isclass)
    table_objs = []
    for name, obj in members:
        if obj != ModelBase and issubclass(obj, ModelBase):
            table_objs.append(obj)
    sqlite_utils.create_table(table_objs, DB)

if __name__ == '__main__':
    pass
