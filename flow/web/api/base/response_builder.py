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

import json

from flask import send_from_directory, make_response

from flow.web.api.base.content_type import ContentType
from flow.web.api.base.dto.base_api_output import BaseApiOutput


def file_response(output):
    """
    response is file
    """
    directory = output.data["directory"]
    filename = output.data["filename"]

    response = make_response(send_from_directory(directory, filename, as_attachment=True))

    # The latin-1 encoding of the file name is to solve
    # the problem of error reporting when the file name contains Chinese.
    response.headers["Content-Disposition"] = "attachment; filename={}".format(filename.encode().decode('latin-1'))
    return response


def json_response(output):
    """
    response is json
    """
    json_str = json.dumps(output, default=lambda obj: obj.__dict__)
    return json_str, 200, {"Content-Type": "application/json"}


response_map = {
    ContentType.Json: json_response,
    ContentType.File: file_response,
}


def build(output: BaseApiOutput):
    # Perform different response actions according to the content type of the output.
    response = response_map.get(output.content_type)
    if response is None:
        return "未指定响应处理器类型：" + str(output.content_type) + "，请在 response_map 中增加映射。"
    output.__delattr__('content_type')
    return response(output)
