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

from common.python.utils.log_utils import get_log_root_path
from flow.web.util.util import zip_util


class LogService:

    @staticmethod
    def download_log(job_id):

        log_root_path = get_log_root_path()
        log_real_path = log_root_path + "/" + job_id
        after_zip_path = log_real_path + ".zip"

        if os.path.isdir(log_real_path) is False:
            raise AttributeError("未找到指定的日志目录")

        # Check whether the compressed file has been generated
        if os.path.exists(after_zip_path) is False:
            # Generate compressed files
            try:
                zip_util(log_real_path, after_zip_path)
            except IOError:
                raise IOError("文件压缩失败，请检查服务")

        return log_root_path, job_id + '.zip'
