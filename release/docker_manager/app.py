from flask import Flask, request

from api.base import response_builder
from api.base.api_executer import ApiExecutor
from api.base.regex_router_converter import RegexRouterConverter

app = Flask(__name__)

# 这里是定义数据类型的名字，并注册到 url_map 中
app.url_map.converters['regex'] = RegexRouterConverter


# 使用自定义的路由规则
@app.route('/<regex:path>', methods=['GET', 'POST'])
def execute_api(path):
    output = ApiExecutor.execute(request)
    return response_builder.build(output)


if __name__ == '__main__':
    # from waitress import serve
    # serve(app, host="0.0.0.0", port=5000)
    app.run(host="0.0.0.0", port=5000)
