import json

from flask import send_from_directory, make_response

from api.base.content_type import ContentType
from api.base.dto.base_api_output import BaseApiOutput


def file_response(output):
    """
    响应文件
    """
    directory = output.data["directory"]
    filename = output.data["filename"]

    response = make_response(send_from_directory(directory, filename, as_attachment=True))

    # 对文件名进行 latin-1 编码是为了解决文件名包含中文时报错的问题
    response.headers["Content-Disposition"] = "attachment; filename={}".format(filename.encode().decode('latin-1'))
    return response


def json_response(output):
    """
    响应 json
    """
    json_str = json.dumps(output, default=lambda obj: obj.__dict__)
    return json_str, 200, {"Content-Type": "application/json"}


response_map = {
    ContentType.Json: json_response,
    ContentType.File: file_response,
}


def build(output: BaseApiOutput):
    # 根据 output 的 content type 执行不同的响应动作
    response = response_map.get(output.content_type)
    if response is None:
        return "未指定响应处理器类型：" + str(output.content_type) + "，请在 response_map 中增加映射。"
    output.__delattr__('content_type')
    return response(output)
