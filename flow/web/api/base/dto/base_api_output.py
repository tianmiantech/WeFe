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

from flow.web.api.base.content_type import ContentType


class BaseApiOutput:
    # api execution result status code
    code = 0

    message = ''
    data = {}
    # Time consuming to execute (ms)
    spend = 0
    content_type = ContentType.Json

    @staticmethod
    def success(data):
        output = BaseApiOutput()
        output.content_type = ContentType.Json
        output.data = data
        output.code = 0

        return output

    @staticmethod
    def fail(code, message):
        output = BaseApiOutput()
        output.content_type = ContentType.Json
        output.message = message
        output.code = code

        return output

    @staticmethod
    def file(directory, filename):
        output = BaseApiOutput()
        output.content_type = ContentType.File
        output.data["directory"] = directory
        output.data["filename"] = filename

        return output
