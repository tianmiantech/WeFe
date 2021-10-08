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

import json
import os
import typing
from pathlib import Path

from ruamel import yaml

from common.python import session
from common.python.utils import file_utils


def merge_dict(dict1, dict2):
    merge_ret = {}
    keyset = dict1.keys() | dict2.keys()
    for key in keyset:
        if key in dict1 and key in dict2:
            val1 = dict1.get(key)
            val2 = dict2.get(key)
            assert type(val1).__name__ == type(val2).__name__
            if isinstance(val1, dict):
                merge_ret[key] = merge_dict(val1, val2)
            else:
                merge_ret[key] = val2
        elif key in dict1:
            merge_ret[key] = dict1.get(key)
        else:
            merge_ret[key] = dict2.get(key)

    return merge_ret


def extract_explicit_parameter(func):
    def wrapper(*args, **kwargs):
        explict_kwargs = {"explict_parameters": kwargs}

        return func(*args, **explict_kwargs)

    return wrapper


def load_job_config(path):
    config = JobConfig.load(path)
    return config


def remove_wefe_process():
    import shutil
    rootpath = os.path.abspath('../../../../data/LMDB/wefe_process')
    if os.path.exists(rootpath):
        shutil.rmtree(rootpath)


class Parties(object):
    def __init__(self, parties):
        self.provider = parties.get("provider", None)
        self.promoter = parties.get("promoter", None)
        self.arbiter = parties.get("arbiter", None)


class JobConfig(object):
    def __init__(self, config):
        self.parties = Parties(config.get("parties", {}))
        self.backend = config.get("backend", 0)
        self.work_mode = config.get("work_mode", 0)
        self.db_type = config.get("db_type", "LMDB")
        # self.data_base_dir = config.get("data_base_dir", "")
        self.data_base_dir = os.path.join(file_utils.get_project_base_directory(), "kernel/examples")

    @staticmethod
    def load(path: typing.Union[str, Path]):
        conf = JobConfig.load_from_file(path)
        return JobConfig(conf)

    @staticmethod
    def load_from_file(path: typing.Union[str, Path]):
        """
        Loads conf content from json or yaml file. Used to read in parameter configuration
        Parameters
        ----------
        path: str, path to conf file, should be absolute path

        Returns
        -------
        dict, parameter configuration in dictionary format

        """
        if isinstance(path, str):
            path = Path(path)
        config = {}
        if path is not None:
            file_type = path.suffix
            with path.open("r") as f:
                if file_type == ".yaml":
                    config.update(yaml.safe_load(f))
                elif file_type == ".json":
                    config.update(json.load(f))
                else:
                    raise ValueError(f"Cannot load conf from file type {file_type}")
        return config


class Upload(object):
    def __init__(self, backend, work_mode, db_type):
        self.MAX_PARTITION_NUM = 1024
        self.MAX_BYTES = 1024 * 1024 * 8
        session.init(mode=work_mode, backend=backend, db_type=db_type)

    def save_data_table(self, input_file, dst_table_name, dst_table_namespace, partition, head=True):
        count = self.get_count(input_file)
        with open(input_file, 'r') as fin:
            lines_count = 0
            if head is True:
                data_head = fin.readline()
                count -= 1
                self.save_data_header(data_head, dst_table_name, dst_table_namespace)
            while True:
                data = list()
                print("read data")
                lines = fin.readlines(self.MAX_BYTES)
                if lines:
                    for line in lines:
                        values = line.replace("\n", "").replace("\t", ",").split(",")
                        data.append((values[0], self.list_to_str(values[1:])))
                    lines_count += len(data)
                    data_table = session.save_data(data, name=dst_table_name, namespace=dst_table_namespace,
                                                   partition=partition)
                else:
                    break

            return data_table.count()

    def save_data_header(self, header_source, dst_table_name, dst_table_namespace):
        header_source_item = header_source.split(',')
        session.save_data_table_meta(
            {'header': ','.join(header_source_item[1:]).strip(), 'sid': header_source_item[0]},
            dst_table_name,
            dst_table_namespace)

    @staticmethod
    def prevent_repeat_upload(table_name, namespace):
        data_table = session.get_data_table(table_name, namespace)
        if not data_table.take():
            print("{}".format("Not Repeat"))
        else:
            session.cleanup(table_name, namespace)
            session.cleanup(table_name + ".meta", namespace)
            print("{}".format("Clean the Repeat Data"))

    def get_count(self, input_file):
        with open(input_file, 'r', encoding='utf-8') as fp:
            count = 0
            for line in fp:
                count += 1
        return count

    def list_to_str(self, input_list):
        return ','.join(list(map(str, input_list)))
