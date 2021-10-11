from werkzeug.routing import BaseConverter

"""
自定义 flask 路由
使用该路由能实现所有路径均被路由到一个 Controller
"""


class RegexRouterConverter(BaseConverter):
    regex = r'.*'  # 支持正则匹配
