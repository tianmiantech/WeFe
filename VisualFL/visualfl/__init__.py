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


import os

__version__ = "1.0"
__basedir__ = os.path.dirname(os.path.abspath(__file__))
__logs_dir__ = os.path.abspath(os.path.join(__basedir__, os.path.pardir, "logs"))
__config_path__ = os.path.abspath(os.path.join(__basedir__, os.path.pardir, "config.properties"))
# __fl_job_config_dir__ = os.path.abspath(os.path.join(__basedir__, os.path.pardir, "visualfl/fl_job_config"))
__data_dir__ = os.path.abspath(os.path.join(__basedir__, os.path.pardir, "data"))

VISUALFL_DATA_BASE_ENV = "VISUALFL_DATA_BASE_ENV"


def get_data_dir():
    if VISUALFL_DATA_BASE_ENV in os.environ and os.path.exists(
        os.environ.get(VISUALFL_DATA_BASE_ENV)
    ):
        return os.path.abspath(os.environ.get(VISUALFL_DATA_BASE_ENV))
    else:
        return __data_dir__


if __name__ == '__main__':
    print(get_data_dir())