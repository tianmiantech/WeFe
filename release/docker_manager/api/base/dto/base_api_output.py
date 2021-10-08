from api.base.content_type import ContentType


class BaseApiOutput:
    """api 执行结果状态码"""
    code = 0

    message = ''
    data = {}
    """执行耗时（ms）"""
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
