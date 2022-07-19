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


import hashlib
import ctypes
import numpy as np
import datetime as dt
from ctypes import cdll, sizeof, c_buffer, cast, c_int32
from ctypes import c_char, c_char_p, c_void_p, c_uint32, c_double, c_int64, c_int, c_size_t, c_longlong
from common.python import RuntimeInstance
from common.python import session

BATCH_SIZE = 10000


def _hash(value):
    return hashlib.sha256(bytes(str(value), encoding='utf-8')).hexdigest()


def _generate_batch_data_iter(data_instance, r, p, is_hash, bits):
    batch_data = []
    index = 0
    for k, v in data_instance.collect():
        batch_data.append(k)
        index += 1
        if index == BATCH_SIZE:
            yield batch_data, r, p, is_hash, bits
            index = 0
            batch_data = []
    if index > 0:
        yield batch_data, r, p, is_hash, bits


def _each_batch_encrypt(data_tuple):
    aclr_client = RuntimeInstance.get_alcr_ins()
    data = data_tuple[0]
    r = data_tuple[1]
    p = data_tuple[2]
    is_hash = data_tuple[3]
    bits = data_tuple[4]

    if is_hash:
        cal_data = [(int(_hash(k), 16), r, p) for k in data]
    else:
        cal_data = [(k, r, p) for k in data]

    gpu_result = aclr_client.powm_base(cal_data, bits)
    return [(gpu_result[i], data[i]) for i in range(len(cal_data))]


def _dh_encrypt_id(data_instance, r, p, is_hash, bits):
    import multiprocessing
    process_count = multiprocessing.cpu_count()
    with multiprocessing.Pool(processes=process_count) as pool:
        result_iter = pool.imap_unordered(_each_batch_encrypt,
                                          _generate_batch_data_iter(data_instance, r, p, is_hash, bits))
        for item_result in result_iter:
            for item in item_result:
                yield item


def dh_encrypt_id(data_instance, r, p, is_hash, bits):
    return session.parallelize(data=_gpu_dh_encrypt_id(data_instance, r, p, is_hash, bits),
                               include_key=True,
                               partition=data_instance.get_partitions())


def _gpu_dh_encrypt_id(data_instance, r, p, is_hash, bits):
    k_array = []
    id_str_list = []
    datas = list(data_instance.collect())
    rs = []
    for id_str, _ in datas:
        if is_hash:
            eid = _hash(id_str)
            k_array.append(int(eid, 16))
        else:
            # print(f'id_str: {id_str}')
            k_array.append(id_str)
        id_str_list.append(id_str)
    eid = gpu_diffiehellman_encrypt_decrypt(k_array, r, p)

    for i in range(len(id_str_list)):
        rs.append((eid[i], id_str_list[i]))

    return rs


def gpu_diffiehellman_encrypt_decrypt(k_array, r, p):
    MAX_COUNT = 20000000
    INT64_TYPE = 1
    FLOAT_TYPE = 2
    PEN_BASE = 16

    CHAR_BYTE = sizeof(c_char)
    U_INT32_BYTE = sizeof(c_uint32)
    DOUBLE_BYTE = sizeof(c_double)
    INT64_BYTE = sizeof(c_int64)

    CIPHER_BITS = 2048
    PLAIN_BITS = 2048
    BYTE_LEN = 8
    CIPHER_BYTE = 256
    PLAIN_BYTE = 256
    device_type = 1

    # if (not isinstance(k_array[0], int)) and (not isinstance(k_array[0], mpz)):
    #    raise TypeError("k_array[0] should be int or mpz, \
    #                     not: %s" % type(k_array[0]))

    # if not isinstance(k_array[0], int):
    # raise TypeError("k_array[0] should be int, \
    # not: %s" % type(k_array[0]))

    if not isinstance(r, int):
        raise TypeError("r should be int, \
                         not: %s" % type(r))

    if not isinstance(p, int):
        raise TypeError("p should be int, \
                         not: %s" % type(p))

    if isinstance(k_array, list):
        array_element_count = len(k_array)
    else:
        array_element_count = k_array.shape[0]
    if array_element_count < 1:
        raise ValueError("k_array's element count < 1")

    if array_element_count > MAX_COUNT:
        raise ValueError("Total input element count = %i , too large ！[ > %i ]" % (array_element_count, MAX_COUNT))

    r_bytes = r.to_bytes(CIPHER_BYTE, 'little')
    p_bytes = p.to_bytes(CIPHER_BYTE, 'little')

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    timebegin = dt.datetime.now()
    # fill the structure
    k_array_structure = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if k_array_structure is None:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    ii = 0
    for i in range(array_element_count):  # y ** x mod nsquare
        # x
        gpu_lib.GPU_H_C_Memcpy(c_void_p(k_array_structure + ii),
                               r_bytes,
                               c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        # x_exponent(skip)
        ii = ii + INT64_BYTE

        # y
        gpu_lib.GPU_H_C_Memcpy(c_void_p(k_array_structure + ii),
                               k_array[i].to_bytes(
                                   CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        # y_exponent (skip)
        ii = ii + INT64_BYTE

        # g (skip)
        ii = ii + CIPHER_BYTE

        # n  (skip)
        ii = ii + CIPHER_BYTE

        # nsquare (put p)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(k_array_structure + ii),
                               p_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # max_int (skip)
        ii = ii + CIPHER_BYTE

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(
        " fill GPU structure, cost time:  %f " %
        (costtime))
    print(f' ')

    timebegin = dt.datetime.now()
    ret = gpu_lib.GPU_H_DiffieHellman_EncryptDecrypt(
        1,
        1,
        c_void_p(k_array_structure),
        c_size_t(array_element_count)
    )
    if 0 != ret:
        gpu_lib.GPU_H_C_Free(c_void_p(k_array_structure))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(
        " gpu_lib.GPU_H_DiffieHellman_EncryptDecrypt, cost time:  %f " %
        (costtime))
    print(f' ')

    timebegin = dt.datetime.now()

    # parse GPU encrypt result
    out_int_array = []
    element_len = CIPHER_BYTE * 6 + INT64_BYTE * 2
    for i in range(array_element_count):
        iii = i * element_len
        out_string = ctypes.string_at(k_array_structure + iii, CIPHER_BYTE)
        out_int = int.from_bytes(out_string, 'little')
        out_int_array.append(out_int)

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(
        " parse GPU result, cost time:  %f " %
        (costtime))
    print(f' ')

    # free memory
    gpu_lib.GPU_H_C_Free(c_void_p(k_array_structure))

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(" GPU encrypt, total cost time:  %f " % (gpu_cost_time))
    print(f' ')

    return np.array(out_int_array)
