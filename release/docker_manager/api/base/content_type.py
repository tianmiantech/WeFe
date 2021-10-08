from enum import Enum, unique

'''
枚举，API 的响应类型。
'''


@unique
class ContentType(Enum):
    __dict__ = None

    Json = 'application/json'
    Zip = 'application/zip'
    File = 'application/octet-stream'
