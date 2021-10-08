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

from common.python import session
from common.python.utils.store_type import DBTypes

DB_TYPE = DBTypes.CLICKHOUSE
BACKEND = 1


def deal_predict_data(t_list):
    """
    Deal predict data

    Parameters
    ----------
    t_list

    Returns
    -------

    """
    for item_kv in t_list:
        yield [item_kv[0]] + item_kv[1]


def deal_data_set_list(data_set_list):
    """
    Instance struct to feature list

    Parameters
    ----------
    data_set_list

    Returns
    -------

    """
    for item_kv in data_set_list:
        yield [item_kv[0]] + item_kv[1].to_csv().split(',')


def export_predict_data(namespace, name, save_name=None):
    """
    Export predict data

    Parameters
    ----------
    namespace
    name
    save_name

    Returns
    -------

    """
    session.init(job_id="export_data", db_type=DB_TYPE, backend=BACKEND)
    t = session.table(name, namespace=namespace)
    first = t.first()
    print(first)

    if not first:
        print("no data")
        return

    t_list = t.collect()
    data = deal_predict_data(t_list)

    import pandas as pd
    from common.python.utils import file_utils
    import os

    cols = ['id', 'label', 'predict_result', 'predict_score', 'predict_type', 'type']

    df = pd.DataFrame(data=data, columns=cols)
    save_dir = os.path.join(file_utils.get_project_base_directory(), "data", "export")
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)
    save_path = os.path.join(save_dir, (save_name or name) + ".csv")
    df.to_csv(save_path, index=True, index_label="index")


def export_data_set(namespace, name):
    """
    Export data set

    Parameters
    ----------
    namespace
    name

    Returns
    -------

    """
    session.init(job_id="export_data", db_type=DB_TYPE, backend=BACKEND)
    t = session.table(name, namespace=namespace)
    first = t.first()
    print(first)

    if not first:
        print("no data")
        return

    meta = session.get_data_table_metas(name, namespace)
    print(meta)

    data_set_list = t.collect()
    data = deal_data_set_list(data_set_list)

    import pandas as pd
    from common.python.utils import file_utils
    import os

    # cols = ['id'] + meta['header'].split(',')
    # {'sid': 'id', 'header': ['x0', 'x1', 'x2', 'x3', 'x4', 'x5', 'x6', 'x7', 'x8', 'x9'],
    # 'schema': {'header': ['x0', 'x1', 'x2', 'x3', 'x4', 'x5', 'x6', 'x7', 'x8', 'x9'],
    # 'sid_name': 'id', 'label_name': 'y', 'with_label': True}}

    schema = meta['schema']
    cols = [schema['sid_name']] + \
           ([schema['label_name']] if schema.get('with_label', False) else []) + \
           schema['header']

    # cols = None

    df = pd.DataFrame(data=data, columns=cols)
    save_dir = os.path.join(file_utils.get_project_base_directory(), "data", "export")
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)
    save_path = os.path.join(save_dir, name + ".csv")
    df.to_csv(save_path, index=False)


if __name__ == '__main__':
    pass
    # export predict data
    # export_predict_data("wefe_data", "xxx")

    # export data set
    # export_data_set("wefe_data",
    #                 "xxx")
