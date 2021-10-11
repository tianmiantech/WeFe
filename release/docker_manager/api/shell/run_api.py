import os

from api.base.base_api import BaseApi
from api.base.dto.base_api_input import BaseApiInput
from api.base.dto.base_api_output import BaseApiOutput


class Input(BaseApiInput):
    command: str

    def check(self):
        super().required([self.command])


class Api(BaseApi):

    def run(self, input: Input):
        # 获取日志文件的目录和文件名
        f = os.popen(input.command)
        output = f.read()
        return BaseApiOutput.success({"output": output})
