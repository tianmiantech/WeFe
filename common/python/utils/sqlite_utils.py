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

import os

from playhouse.pool import PooledSqliteDatabase

from common.python.utils import file_utils


def _get_data_dir():
    data_dir = os.path.join(file_utils.get_project_base_directory(), 'data/SQLite')
    file_utils.get_project_base_directory()
    if not os.path.exists(data_dir):
        os.makedirs(data_dir)
    return data_dir


def get_database():
    return _get_data_dir() + "/wefe_board.db"


def get_sqlite_db():
    sqlite_database = get_database()
    db = PooledSqliteDatabase(database=sqlite_database)
    return db


def create_table(tables, data_base):
    data_base.create_tables(tables)
