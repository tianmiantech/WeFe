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

import os
import zipfile


def construct_status_return(response: dict, service_name, code, message):
    response[service_name] = {
        "code": code,
        "message": message
    }
    return response


def zip_util(start_dir, new_file_path):
    """
    zip compression tool

    Parameters
    ----------
    start_dir: str
        The source folder that needs to be compressed

    new_file_path: str
        file path after compression

    """

    z = zipfile.ZipFile(new_file_path, 'w', zipfile.ZIP_DEFLATED)
    for dir_path, dir_names, filenames in os.walk(start_dir):
        f_path = dir_path.replace(start_dir, '')
        f_path = f_path and f_path + os.sep or ''
        for filename in filenames:
            z.write(os.path.join(dir_path, filename), f_path + filename)
        # print('zip success')
    z.close()


if __name__ == '__main__':
    zip_util(
        "/Users/wingo.wen/PycharmProjects/Wefe/logs/4a22e0dcfa5b41c9b4c0173de372fad7",
        "/Users/wingo.wen/PycharmProjects/Wefe/logs/4a22e0dcfa5b41c9b4c0173de372fad7.zip"
    )
