# Copyright 2021 Tianmian Tech. All Rights Reserved.
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

class MemberInfo(object):
    # 全局唯一，默认为uuid。
    member_id: str
    # 联邦成员名称
    member_name: str
    # 联邦成员邮箱
    member_email: str
    # 联邦成员电话
    member_mobile: str
    # 联邦成员网关访问地址
    member_gateway_uri: str
    # 是否允许对外公开数据集基础信息
    member_allow_public_data_set: bool
    # 私钥
    rsa_private_key: str
    # 公钥
    rsa_public_key: str
    # 成员头像
    member_logo: str
    # 成员隐身状态
    member_hidden: bool


class BoardConfigModel(object):
    intranet_base_uri: str


class GatewayConfigModel(object):
    intranet_base_uri: str
    ip_white_list: str


class MailServerModel(object):
    mail_host: str
    mail_port: int
    mail_username: str
    mail_password: str


class FunctionComputeConfig(object):
    max_cost_in_day: int
    max_cost_in_month: int


class SparkStandaloneConfig(object):
    executor_memory: str
    driver_max_result_size: str
    driver_memory: str
    hardware_acceleration: str
