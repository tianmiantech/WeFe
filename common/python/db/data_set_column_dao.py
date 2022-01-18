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


from common.python.db.db_models import DB, DataSetColumn


class DataSetColumnDao(object):

    @staticmethod
    def list_by_data_set_id(data_set_id):
        with DB.connection_context():
            return DataSetColumn.select().where(
                DataSetColumn.data_set_id == data_set_id).order_by(
                DataSetColumn.index.asc())

    @staticmethod
    def batch_insert(data_set_column_list):
        with DB.atomic():
            for i in range(0, len(data_set_column_list), 1000):
                DataSetColumn.insert_many(data_set_column_list[i:i + 1000]).execute()

    @staticmethod
    def get(*query, **filters):
        with DB.connection_context():
            return DataSetColumn.get_or_none(*query, **filters)

    @staticmethod
    def save(model: DataSetColumn, force_insert=False):
        with DB.connection_context():
            model.save(force_insert=force_insert)
