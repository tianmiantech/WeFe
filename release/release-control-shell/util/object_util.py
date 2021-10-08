import json


def dict_to_model(model: object, **data):
    """
    将 dict 转为对象

    使用示例：
    dic = {'name': 'python', 'price': 10}
    model = Model()
    BeanUtil.dict_to_model(model, **dic)
    """

    model.__dict__.update(data)
    return model


def json_to_model(json_str: str, clazz):
    dic = json.loads(json_str)
    return dict_to_model(clazz(), **dic)


class TestModel:
    a: str
    b: int


if __name__ == '__main__':
    json_str = '{"a":"hello","b":100}'
    test_model: TestModel = json_to_model(json_str, TestModel)
    print(test_model.a)
    print(test_model.b)
