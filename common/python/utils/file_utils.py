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
from fnmatch import fnmatch

from cachetools import LRUCache
from cachetools import cached

PROJECT_BASE = None


def get_project_base_directory():
    global PROJECT_BASE
    if PROJECT_BASE is None:
        PROJECT_BASE = os.path.abspath(
            os.path.join(os.path.dirname(os.path.realpath(__file__)), os.pardir, os.pardir, os.pardir))
    return PROJECT_BASE


@cached(cache=LRUCache(maxsize=10))
def load_json_conf(conf_path):
    if os.path.isabs(conf_path):
        json_conf_path = conf_path
    else:
        json_conf_path = os.path.join(get_project_base_directory(), conf_path)
    try:
        with open(json_conf_path) as f:
            return json.load(f)
    except Exception:
        raise EnvironmentError("loading json file config from '{}' failed!".format(json_conf_path))


def dump_json_conf(config_data, conf_path):
    if os.path.isabs(conf_path):
        json_conf_path = conf_path
    else:
        json_conf_path = os.path.join(get_project_base_directory(), conf_path)
    try:
        with open(json_conf_path, "w") as f:
            json.dump(config_data, f, indent=4)
    except Exception:
        raise EnvironmentError("loading json file config from '{}' failed!".format(json_conf_path))


def load_yaml_conf(conf_path):
    from ruamel import yaml
    if not os.path.isabs(conf_path):
        conf_path = os.path.join(get_project_base_directory(), conf_path)
    try:
        with open(conf_path) as f:
            return yaml.safe_load(f)
    except Exception as e:
        raise EnvironmentError("loading yaml file config from {} failed:".format(conf_path), e)


def match_dir(dir_root, match_name):
    """

    Match the first matching directory

    Parameters
    ----------
    dir_root：str
        root directory

    match_name:str
        Directory name to be matched

    Returns
    -------
        full directory path
    """
    for root, dirs, files in os.walk(dir_root):
        if match_name in dirs:
            return os.path.join(root, match_name)
        for d in dirs:
            match_result = match_dir(os.path.join(root, d), match_name)
            if match_result:
                return match_result


def match_all_files(dir_root, match_file_patten):
    """
    Match all eligible files

    Parameters
    ----------
    dir_root
    match_file_patten

    Returns
    -------
        Full path of eligible files
    """
    for root, dirs, files in os.walk(dir_root):
        for f in files:
            if fnmatch(f, match_file_patten):
                yield os.path.join(root, f)

        for d in dirs:
            match_result = match_all_files(os.path.join(root, d), match_file_patten)
            for item in match_result:
                yield item


if __name__ == "__main__":
    print(get_project_base_directory())
    print(load_json_conf('kernel/transfer/varibles/definition/transfer_conf.json'))
