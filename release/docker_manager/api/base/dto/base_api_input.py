class BaseApiInput:

    def check(self):
        """
        检查参数是否有效，子类请重写此方法，当参数不合法时，请直接抛出异常。
        """

    @staticmethod
    def required(params):
        """
        确认传入的参数不为空
        """
        for item in params:
            if item is None or item == '':
                raise Exception("api param can not empty value")
