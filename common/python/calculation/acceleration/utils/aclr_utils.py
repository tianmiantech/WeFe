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

from common.python.common import consts
from common.python.utils import conf_utils

BITS = 2048


def to_32bit_binary_array(value: int):
    binary_str = bin(value)[2:]
    if len(binary_str) < BITS:
        binary_str = '0' * (BITS - len(binary_str)) + binary_str
    return binary_str


def to_32bit_int_array(value: int):
    binary_str = to_32bit_binary_array(value)
    print(binary_str)
    result = []
    step = 32
    for i in range(0, len(binary_str), step):
        item_binary = binary_str[i:i + step]
        result.append(int(item_binary, 2))
    return result


def from_32bit_to_bigint(values: list):
    result = ''
    for item in values:
        item_result = bin(item)[2:]
        if len(item_result) < 32:
            item_result = '0' * (32 - len(item_result)) + item_result
        result += item_result
    return int(result, 2)


def to_bytes(value: int):
    return value.to_bytes(BITS // 8, "little")


def check_aclr_support():
    aclr_type = conf_utils.get_comm_config(consts.COMM_CONF_KEY_ACCELERATION, "")
    return aclr_type in [consts.AccelerationType.GPU]


def gpu_support():
    aclr_type = conf_utils.get_comm_config(consts.COMM_CONF_KEY_ACCELERATION, "")
    return aclr_type == consts.AccelerationType.GPU
