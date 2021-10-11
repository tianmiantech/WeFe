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

from flow.web.api.base.base_api import BaseApi
from flow.web.api.base.dto.base_api_input import BaseApiInput
from flow.web.api.base.dto.base_api_output import BaseApiOutput
from flow.web.service.log_service import LogService


class Input(BaseApiInput):
    job_id: str

    def check(self):
        super().required([self.job_id])


class Api(BaseApi):

    def run(self, input: Input):
        # Get the directory and file name of the log file
        dir, file = LogService.download_log(input.job_id)
        return BaseApiOutput.file(dir, file)
