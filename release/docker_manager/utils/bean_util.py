class BeanUtil:

    @staticmethod
    def dict_to_model(obj: object, **data):
        """
        将 dict 转为对象

        使用示例：
        dic = {'name': 'python', 'price': 10}
        model = Model()
        BeanUtil.dict_to_model(model, **dic)
        """

        obj.__dict__.update(data)
        return obj
