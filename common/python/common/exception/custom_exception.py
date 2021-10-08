# Copyright 2021 The WeFe Authors. All Rights Reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from common.python.common.exception.base_exception import CustomBaseException

"""
exception definition：
1000-1099：common exception definition.
1100-1199：gateway related exception definition
1200-1299：the algorithm related exception definition
1300-1399：function computing related exception definition
1400-1499：GPU computing related exception definition
"""


#########################################
#
# 1000-1099：common exception definition
#
#########################################

class CommonCustomError(CustomBaseException):
    def __init__(self, message, **kwargs):
        """
        Define common custom exception

        Parameters
        ----------
        message：str
            show message for user
        kwargs
        """
        code = 1000
        self.init(code, message, kwargs)


class DataSetEmptyError(CustomBaseException):
    def __init__(self, code=1001, message='数据集为空，请检查。', **kwargs):
        self.init(code, message, kwargs)


class ParameterError(CustomBaseException):
    def __init__(self, code=1002, message='参数错误，请检查。', **kwargs):
        self.init(code, message, kwargs)


class PickleError(CustomBaseException):
    def __init__(self, code=1003, message='数据序列化异常，请联系技术人员。', **kwargs):
        self.init(code, message, kwargs)


class FeatureEmptyError(CustomBaseException):
    def __init__(self, code=1004, message='数据集的特征为空，请检查。', **kwargs):
        self.init(code, message, kwargs)


class NoneTypeError(CustomBaseException):
    def __init__(self, code=1005, message='NoneType异常，请检查数据集是否有空值。', **kwargs):
        self.init(code, message, kwargs)


class CustomTypeError(CustomBaseException):
    def __init__(self, code=1006, message='Type异常，请检查数据集的格式是否符合要求。', **kwargs):
        self.init(code, message, kwargs)


class NaNTypeError(CustomBaseException):
    def __init__(self, code=1007, message='NaN 异常，请检查数据集是否有空值。', **kwargs):
        self.init(code, message, kwargs)


class SparkError(CustomBaseException):
    def __init__(self, code=1008, message='Task执行异常，详情请下载日志并查看。', **kwargs):
        self.init(code, message, kwargs)


class HasNoLabelError(CustomBaseException):
    def __init__(self, code=1009, message='数据集不含标签 label，请检查数据集。', **kwargs):
        self.init(code, message, kwargs)


#########################################
#
# 1100-1199：gateway related exception definition
#
#########################################

class GatewayWhiteListError(CustomBaseException):
    def __init__(self, code=1101, message='gateway连接异常，请检查白名单配置信息。', **kwargs):
        self.init(code, message, kwargs)


class GatewayConnectError(CustomBaseException):
    def __init__(self, code=1102, message='gateway连接异常，请检查配置信息。', **kwargs):
        self.init(code, message, kwargs)


#########################################
#
# 1300-1399：function computing related exception definition
#
#########################################

class FCCommonError(CustomBaseException):
    def __init__(self, code=1300, message='函数计算异常', **kwargs):
        self.init(code, message, kwargs)


class OTSError(CustomBaseException):
    def __init__(self, code=1301, message='函数计算的中间存储模块OTS异常', **kwargs):
        self.init(code, message, kwargs)


class OSSError(CustomBaseException):
    def __init__(self, code=1302, message='函数计算的中间存储模块OSS异常', **kwargs):
        self.init(code, message, kwargs)


#########################################
#
# 1400-1499：GPU computing related exception definition
#
#########################################
class GPUCalcError(CustomBaseException):
    def __init__(self, code=1401, message='GPU计算异常', **kwargs):
        self.init(code, message, kwargs)


class NotSupportTypeError(CustomBaseException):
    def __init__(self, code=1402, message='不支持此类型做加速运算', **kwargs):
        self.init(code, message, kwargs)
