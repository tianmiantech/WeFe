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

from common.python.calculation.acceleration.operator import dot as operator_dot
from common.python.calculation.acceleration.operator import encrypt
from ctypes import c_char, c_char_p, c_void_p, c_uint32, c_double, c_int64, c_int, c_size_t, c_longlong
from ctypes import cdll
import datetime as dt
import math


def gpu_device_info(info_type, algo_type):
    free_12g = 12163874816

    max_table_dot_12g = 93000 * 10 * 10  # limit about: 96000 * 13 * 14
    max_matrix_12g = 46000 * 100  # limit about: 48000 * 13 * 14
    max_array_12g = 9300000  # limit about: 96000 * 13 * 14
    max_array_two_12g = 4600000  # limit about: 48000 * 13 * 14
    max_array_three_12g = 3100000  # limit about: 32000 * 13 * 14
    # MAX_ARRAY_FOUR_12G = 2400000  # limit about: 24000 * 13 * 14

    device_type = 1

    if info_type != "max_array_size":
        raise ValueError("ERROR: not support, info_type = " + info_type)

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_GPU_Device_GetInfo.restype = c_size_t
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    free_space = gpu_lib.GPU_H_GPU_Device_GetInfo(
        device_type,
        100001
    )
    if 0 == free_space:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    if algo_type == "paillier_table_dot":
        max_array_size = math.floor(max_table_dot_12g * (free_space / free_12g))
    elif algo_type == "paillier_dot" or algo_type == "paillier_matrix_row_sum_up":
        max_array_size = math.floor(max_matrix_12g * (free_space / free_12g))
    elif algo_type == "diffiehellman_encrypt_decrypt":
        max_array_size = math.floor(max_array_12g * (free_space / free_12g))
    elif algo_type == "paillier_add_mul":
        max_array_size = math.floor(max_array_two_12g * (free_space / free_12g))
    elif algo_type == "paillier_sub" or algo_type == "paillier_encrypt_decrypt" or algo_type == "paillier_raw_encrypt_decrypt":
        max_array_size = math.floor(max_array_three_12g * (free_space / free_12g))
    else:
        raise ValueError("ERROR: not support, algo_type = " + algo_type)

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(" GPU Device GetInfo, total cost time:  %f " % (gpu_cost_time))
    print(f' ')

    print(
        " GPU Device GetInfo, free_space:  %i , %s : max_array_size = %i" %
        (free_space, algo_type, max_array_size))
    print(f' ')

    return max_array_size


def table_dot(it, bits=2048):
    """
    table dot

    Speed up the method of _table_dot_func in fixedpoint_table.py

    Parameters
    ----------
    it:list
        [(key,([x.],[y.]))]
    bits:
    Returns
    -------

    """
    return operator_dot.table_dot(it, bits)


def table_dot_gpu(X, Y):
    return operator_dot.gpu_paillier_table_dot(X, Y)


def dot(value, w, bits=2048):
    """
    dot

    Speed up the method of dot in base_operator.py

    Parameters
    ----------
    value
    w
    bits

    Returns
    -------

    """
    # return operator_dot.dot(value, w, bits)
    return operator_dot.gpu_paillier_dot(value, w)


def dh_encrypt_id(data_instance, r, p, is_hash=False, bits=2048):
    """
    encrypt id for dh
    :param data_instance:
    :param r:
    :param p:
    :param is_hash:
    :param bits:
    :return:

    """
    return encrypt.dh_encrypt_id(data_instance, r, p, is_hash, bits=bits)
