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

from visualfl.utils import consts
from visualfl.utils.conf_utils import get_comm_config, get_env_config


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

if __name__ == '__main__':
    pass
