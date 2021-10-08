from api.base.dto.base_api_output import BaseApiOutput


class BaseApi(object):

    def run(self, input: dict) -> BaseApiOutput:
        return BaseApiOutput.fail(-1, '此方法需要在子类中重新')