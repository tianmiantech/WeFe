# Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import json

from common.python.storage.impl.clickhouse_storage import ClickHouseStorage


class DataViewService:

    @staticmethod
    def data_view(name):
        from common.python import session, WorkMode, Backend
        session.init("data_preview", WorkMode.CLUSTER, Backend.SPARK)
        view_data = session.get_data_table(namespace="wefe_data", name=name)
        print(view_data.take(10))

    @staticmethod
    def instance_to_json(table_namespace, table_name):
        result = {"header": [], "list": []}
        storage_meta = ClickHouseStorage(_type=None, namespace=table_namespace, name=table_name + ".meta")
        storage_data = ClickHouseStorage(_type=None, namespace=table_namespace, name=table_name)
        my_str = storage_meta.get("schema")
        # Get the Header
        header_list = json.loads(my_str)["header"]
        # Judge Whether Have Label Y
        if len(storage_data.take()[0][1].to_csv().split(",")) == len(header_list) + 1:
            header_list.insert(0, "y")
        header_list.insert(0, json.loads(my_str)["sid_name"])
        for x in header_list:
            result["header"].append(x)
        print(result)
        # Get the Data List
        count = storage_data.count()
        max = 100
        for x in storage_data.take(count if count < max else max):
            each_cnid = x[0]
            each_list = (x[1].to_csv()).split(",")
            each_list.insert(0, each_cnid)
            result["list"].append(each_list)
        return result
