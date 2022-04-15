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

import math
import ctypes
import datetime as dt
import numpy as np
import multiprocessing
from common.python import session
from scipy.sparse import csr_matrix
from common.python import RuntimeInstance
from common.python.calculation.acceleration.utils.aclr_utils import check_aclr_support
from common.python.common.exception.custom_exception import NotSupportTypeError
from kernel.base.instance import Instance
from ctypes import cdll, sizeof, c_buffer, cast, c_int32
from ctypes import c_char, c_char_p, c_void_p, c_uint32, c_double, c_int64, c_int, c_size_t, c_longlong
from kernel.security.paillier import PaillierEncryptedNumber

BATCH_SIZE = 20000
MIN_ADD_BATCH_SIZE = 1000


def table_dot_cpu(a_table, b_table):
    """
        accelerate function `_table_dot_func` in fixedpoint_table.py
    Args:
        a_table:
        b_table:

    Returns:

    """
    ret = None

    if a_table.shape[0] != b_table.shape[0]:
        raise ValueError("X's row count not equal Y's row count!")

    # in cpu
    for i in range(a_table.shape[0]):
        x = a_table[i]
        y = b_table[i]
        if ret is None:
            ret = np.tensordot(x, y, [[], []])
        else:
            ret += np.tensordot(x, y, [[], []])

    return ret


# gpu table dot
# X: int or float or PaillierEncryptedNumber matrix
# Y: int or float matrix
def gpu_paillier_table_dot(a_table, b_table, partitions):
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

    NULL = 0
    NULL_bytes = int(NULL).to_bytes(PLAIN_BYTE, 'little')
    NULL_void_pointer = ctypes.cast(c_char_p(NULL_bytes), ctypes.c_void_p)
    NULL_char_pointer = c_char_p(NULL_bytes)

    if isinstance(a_table[0][0], PaillierEncryptedNumber):
        public_key = a_table[0][0].public_key

        g_bytes = public_key.g.to_bytes(CIPHER_BYTE, 'little')
        n_bytes = public_key.n.to_bytes(CIPHER_BYTE, 'little')
        nsquare_bytes = public_key.nsquare.to_bytes(CIPHER_BYTE, 'little')
        max_int_bytes = public_key.max_int.to_bytes(CIPHER_BYTE, 'little')
    else:
        return table_dot_cpu(a_table, b_table)

    if a_table.shape[0] != b_table.shape[0]:
        raise ValueError("X's row count not equal Y's row count!")

    if isinstance(b_table[0][0], PaillierEncryptedNumber):
        raise ValueError("Y's element should not be PaillierEncryptedNumber!")

    matrix_row_count = a_table.shape[0]
    matrix_x_column_count = a_table.shape[1]
    matrix_y_column_count = b_table.shape[1]

    x_count = matrix_row_count * matrix_x_column_count

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_Paillier_Encode.restype = c_void_p
    gpu_lib.GPU_H_Paillier_TableDot_MatrixMultiply.restype = c_void_p

    # prepare X for GPU
    x_array = a_table.reshape(matrix_row_count * matrix_x_column_count)

    x_array_encoded = gpu_lib.GPU_H_C_Malloc(c_size_t(x_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    ii = 0
    for i in range(x_count):
        # skip x_sign
        ii = ii + INT64_BYTE

        # x
        gpu_lib.GPU_H_C_Memcpy(c_void_p(x_array_encoded + ii),
                               x_array[i].ciphertext().to_bytes(CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        # x_exponent
        gpu_lib.GPU_H_C_Memcpy(c_void_p(x_array_encoded + ii),
                               x_array[i].exponent.to_bytes(INT64_BYTE, 'little'),
                               c_size_t(INT64_BYTE))
        ii = ii + INT64_BYTE

        # g
        gpu_lib.GPU_H_C_Memcpy(c_void_p(x_array_encoded + ii),
                               g_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # n
        gpu_lib.GPU_H_C_Memcpy(c_void_p(x_array_encoded + ii),
                               n_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # nsquare
        gpu_lib.GPU_H_C_Memcpy(c_void_p(x_array_encoded + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # max_int
        gpu_lib.GPU_H_C_Memcpy(c_void_p(x_array_encoded + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # skip random
        ii = ii + CIPHER_BYTE

    # prepare Y for GPU
    y_array = b_table.reshape(matrix_row_count * matrix_y_column_count)
    y_array_size = y_array.size
    y_array_data = gpu_lib.GPU_H_C_Malloc(c_size_t(y_array_size * DOUBLE_BYTE))
    # all turn to 64 bit data type
    if y_array.dtype == 'int32':
        y_array_as_typed = y_array.astype(np.int64)
        y_array_ctypes = y_array_as_typed.ctypes.data_as(c_void_p)
        data_type = INT64_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(y_array_data), y_array_ctypes,
                               y_array_size * INT64_BYTE)
    elif y_array.dtype == 'int64':
        y_array_ctypes = y_array.ctypes.data_as(c_void_p)
        data_type = INT64_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(y_array_data), y_array_ctypes,
                               y_array_size * INT64_BYTE)
    elif y_array.dtype == 'float32':
        y_array_as_typed = y_array.astype(np.float64)
        y_array_ctypes = y_array_as_typed.ctypes.data_as(c_void_p)
        data_type = FLOAT_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(y_array_data), y_array_ctypes,
                               y_array_size * DOUBLE_BYTE)
    elif y_array.dtype == 'float64':
        y_array_ctypes = y_array.ctypes.data_as(c_void_p)
        data_type = FLOAT_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(y_array_data), y_array_ctypes,
                               y_array_size * DOUBLE_BYTE)
    else:
        raise PermissionError("Invalid Data Type of y_array")

    y_array_encoded = gpu_lib.GPU_H_Paillier_Encode(
        1,
        c_longlong(data_type),
        c_void_p(y_array_data),
        c_size_t(matrix_row_count * matrix_y_column_count),
        c_char_p(g_bytes),
        c_char_p(n_bytes),
        c_char_p(nsquare_bytes),
        c_char_p(max_int_bytes)
    )

    matrix_multiplied = gpu_lib.GPU_H_Paillier_TableDot_MatrixMultiply(
        1,
        c_void_p(x_array_encoded),
        c_void_p(y_array_encoded),
        c_size_t(matrix_row_count),
        c_size_t(matrix_x_column_count),
        c_size_t(matrix_y_column_count)
    )

    # malloc output reduce sum host memory
    out_sum_array = gpu_lib.GPU_H_C_Malloc(c_size_t(matrix_x_column_count * matrix_y_column_count * CIPHER_BYTE))
    out_exponent_array = gpu_lib.GPU_H_C_Malloc(c_size_t(matrix_x_column_count * matrix_y_column_count * INT64_BYTE))

    gpu_lib.GPU_H_Paillier_TableDot_SumUp(
        1,
        c_void_p(matrix_multiplied),
        c_size_t(matrix_row_count),
        c_size_t(matrix_x_column_count),
        c_size_t(matrix_y_column_count),
        c_char_p(out_sum_array),
        c_char_p(out_exponent_array)
    )

    # parse GPU reduce sum result
    out_sum_paillier_encrypted_number_array = np.empty(matrix_x_column_count * matrix_y_column_count,
                                                       dtype=PaillierEncryptedNumber)
    out_sum_pen_bytes = c_buffer(CIPHER_BYTE)
    out_exponent_bytes = c_buffer(INT64_BYTE)
    for i in range(matrix_x_column_count * matrix_y_column_count):
        gpu_lib.GPU_H_C_Memcpy(cast(out_sum_pen_bytes, c_void_p), c_char_p(out_sum_array + i * CIPHER_BYTE),
                               CIPHER_BYTE)
        out_sum_pen = int.from_bytes(out_sum_pen_bytes.raw, 'little')
        gpu_lib.GPU_H_C_Memcpy(cast(out_exponent_bytes, c_void_p), c_char_p(out_exponent_array + i * INT64_BYTE),
                               INT64_BYTE)
        exponent = int.from_bytes(out_exponent_bytes.raw, 'little')
        out_sum_paillier_encrypted_number_array[i] = PaillierEncryptedNumber(public_key, out_sum_pen, exponent)

    # free memory
    gpu_lib.GPU_H_C_Free(c_void_p(x_array_encoded))
    gpu_lib.GPU_H_C_Free(c_void_p(y_array_data))
    gpu_lib.GPU_H_C_Free(c_void_p(y_array_encoded))
    gpu_lib.GPU_H_C_Free(c_void_p(matrix_multiplied))
    gpu_lib.GPU_H_C_Free(c_void_p(out_sum_array))
    gpu_lib.GPU_H_C_Free(c_void_p(out_exponent_array))
    result = out_sum_paillier_encrypted_number_array.reshape(matrix_x_column_count, matrix_y_column_count)
    return session.parallelize(result.tolist(), partition=partitions)


# dot
# X: value(int or float) matrix
# w: PaillierEncryptedNumber array
def gpu_paillier_dot(X, w):
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

    matrix_row_count = X.shape[0]
    matrix_column_count = X.shape[1]
    array_element_count = matrix_row_count * matrix_column_count
    if array_element_count > MAX_COUNT:
        raise ValueError("Total input element count = %i , too large ！[ > %i ]" % (array_element_count, MAX_COUNT))

    value_matrix_array = X.reshape(array_element_count)
    w_count = w.shape[0]
    public_key = w[0].public_key

    g_bytes = public_key.g.to_bytes(CIPHER_BYTE, 'little')
    n_bytes = public_key.n.to_bytes(CIPHER_BYTE, 'little')
    nsquare_bytes = public_key.nsquare.to_bytes(CIPHER_BYTE, 'little')
    max_int_bytes = public_key.max_int.to_bytes(CIPHER_BYTE, 'little')

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_Paillier_Encode.restype = c_void_p
    gpu_lib.GPU_H_Paillier_MatrixElementWiseMultiplyColumn.restype = c_void_p
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    value_matrix_array_size = value_matrix_array.size
    value_matrix_array_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(value_matrix_array_size * DOUBLE_BYTE))
    if value_matrix_array_data is None:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # all turn to 64 bit data type
    if (value_matrix_array.dtype == 'int32'):
        value_matrix_array_astyped = value_matrix_array.astype(np.int64)
        value_matrix_array_ctypes = value_matrix_array_astyped.ctypes.data_as(
            c_void_p)
        data_type = INT64_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_matrix_array_data), value_matrix_array_ctypes,
                               value_matrix_array_size * INT64_BYTE)
    elif (value_matrix_array.dtype == 'int64'):
        value_matrix_array_ctypes = value_matrix_array.ctypes.data_as(c_void_p)
        data_type = INT64_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_matrix_array_data), value_matrix_array_ctypes,
                               value_matrix_array_size * INT64_BYTE)
    elif (value_matrix_array.dtype == 'float32'):
        value_matrix_array_astyped = value_matrix_array.astype(np.float64)
        value_matrix_array_ctypes = value_matrix_array_astyped.ctypes.data_as(
            c_void_p)
        data_type = FLOAT_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_matrix_array_data), value_matrix_array_ctypes,
                               value_matrix_array_size * DOUBLE_BYTE)
    elif (value_matrix_array.dtype == 'float64'):
        value_matrix_array_ctypes = value_matrix_array.ctypes.data_as(c_void_p)
        data_type = FLOAT_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_matrix_array_data), value_matrix_array_ctypes,
                               value_matrix_array_size * DOUBLE_BYTE)
    else:
        raise PermissionError("Invalid Data Type of value_matrix_array")

    timebegin = dt.datetime.now()
    value_matrix_array_encoded = gpu_lib.GPU_H_Paillier_Encode(
        1,
        c_longlong(data_type),
        c_void_p(None),
        c_void_p(None),
        c_void_p(value_matrix_array_data),
        c_size_t(array_element_count),
        c_char_p(g_bytes),
        c_char_p(n_bytes),
        c_char_p(nsquare_bytes),
        c_char_p(max_int_bytes)
    )
    if value_matrix_array_encoded is None:
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(" gpu_lib.GPU_H_Paillier_Encode matrix, cost time:  %f " % (costtime))
    print(f' ')

    value_column_encoded = gpu_lib.GPU_H_C_Malloc(
        c_size_t(w_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if value_column_encoded is None:
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_data))
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_encoded))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    ii = 0
    for i in range(w_count):
        # skip x_sign
        ii = ii + INT64_BYTE

        # x
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_column_encoded + ii),
                               w[i].ciphertext(False).to_bytes(
                                   CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        # x_exponent
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_column_encoded + ii),
                               w[i].exponent.to_bytes(INT64_BYTE, 'little'),
                               c_size_t(INT64_BYTE))
        ii = ii + INT64_BYTE

        # g
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_column_encoded + ii),
                               g_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # n
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_column_encoded + ii),
                               n_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # nsquare
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_column_encoded + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # max_int
        gpu_lib.GPU_H_C_Memcpy(c_void_p(value_column_encoded + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # skip random
        ii = ii + CIPHER_BYTE

    timebegin = dt.datetime.now()
    matrixMultiplied = gpu_lib.GPU_H_Paillier_MatrixElementWiseMultiplyColumn(
        1,
        c_void_p(value_matrix_array_encoded),
        c_void_p(value_column_encoded),
        c_size_t(matrix_row_count),
        c_size_t(matrix_column_count)
    )
    if matrixMultiplied is None:
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_data))
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_encoded))
        gpu_lib.GPU_H_C_Free(c_void_p(value_column_encoded))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(
        " gpu_lib.GPU_H_Paillier_MatrixElementWiseMultiplyColumn, cost time:  %f " %
        (costtime))
    print(f' ')

    # malloc output reduce sum host memory
    out_sum_array = gpu_lib.GPU_H_C_Malloc(
        c_size_t(matrix_row_count * CIPHER_BYTE))
    if out_sum_array is None:
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_data))
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_encoded))
        gpu_lib.GPU_H_C_Free(c_void_p(value_column_encoded))
        gpu_lib.GPU_H_C_Free(c_void_p(matrixMultiplied))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    out_exponent_array = gpu_lib.GPU_H_C_Malloc(
        c_size_t(matrix_row_count * INT64_BYTE))
    if out_exponent_array is None:
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_data))
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_encoded))
        gpu_lib.GPU_H_C_Free(c_void_p(value_column_encoded))
        gpu_lib.GPU_H_C_Free(c_void_p(matrixMultiplied))
        gpu_lib.GPU_H_C_Free(c_void_p(out_sum_array))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    timebegin = dt.datetime.now()
    ret = gpu_lib.GPU_H_Paillier_MatrixRowSumUp(
        1,
        c_void_p(matrixMultiplied),
        c_size_t(matrix_row_count),
        c_size_t(matrix_column_count),
        c_char_p(out_sum_array),
        c_char_p(out_exponent_array)
    )
    if 0 != ret:
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_data))
        gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_encoded))
        gpu_lib.GPU_H_C_Free(c_void_p(value_column_encoded))
        gpu_lib.GPU_H_C_Free(c_void_p(matrixMultiplied))
        gpu_lib.GPU_H_C_Free(c_void_p(out_sum_array))
        gpu_lib.GPU_H_C_Free(c_void_p(out_exponent_array))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(
        " gpu_lib.GPU_H_Paillier_MatrixRowSumUp, cost time:  %f " %
        (costtime))
    print(f' ')

    # parse GPU reduce sum result
    out_sum_paillier_encrypted_number_array = np.empty(
        matrix_row_count, dtype=PaillierEncryptedNumber)
    for i in range(matrix_row_count):
        x_string = ctypes.string_at(out_sum_array + i * CIPHER_BYTE, CIPHER_BYTE)
        x = int.from_bytes(x_string, 'little')

        x_exponent_string = ctypes.string_at(out_exponent_array + i * INT64_BYTE, INT64_BYTE)
        x_exponent = int.from_bytes(x_exponent_string, 'little')

        out_sum_paillier_encrypted_number_array[i] = PaillierEncryptedNumber(
            public_key, x, x_exponent)

    # free memory
    gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_data))
    gpu_lib.GPU_H_C_Free(c_void_p(value_matrix_array_encoded))
    gpu_lib.GPU_H_C_Free(c_void_p(value_column_encoded))
    gpu_lib.GPU_H_C_Free(c_void_p(matrixMultiplied))
    gpu_lib.GPU_H_C_Free(c_void_p(out_sum_array))
    gpu_lib.GPU_H_C_Free(c_void_p(out_exponent_array))

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(" GPU dot, total cost time:  %f " % (gpu_cost_time))
    print(f' ')

    return out_sum_paillier_encrypted_number_array


def table_dot(it, bits):
    """
    table dot

    accelerate function `_table_dot_func` in fixedpoint_table.py

    Parameters
    ----------
    it:list
        [(key,([x.],[y.]))]

    Returns
    -------

    """
    ret = None
    batch_x = []
    batch_y = []
    current_batch_count = 0

    if not check_aclr_support():
        # in cpu
        for _, (x, y) in it:
            if ret is None:
                ret = np.tensordot(x, y, [[], []])
            else:
                ret += np.tensordot(x, y, [[], []])

        return ret

    # in gpu
    for _, (x, y) in it:

        if not batch_x or current_batch_count < BATCH_SIZE:
            batch_x.append(x)
            batch_y.append(y)
            current_batch_count = current_batch_count + len(x) * len(y)
            if current_batch_count >= BATCH_SIZE:
                batch_result = _gpu_tensordot_with_paillier_4batch(batch_x, batch_y, bits)

                for item_batch in batch_result:
                    if ret is None:
                        ret = item_batch
                    else:
                        ret += item_batch

                batch_x, batch_y, current_batch_count = [], [], 0

    if batch_x:
        batch_result = _gpu_tensordot_with_paillier_4batch(batch_x, batch_y, bits)

        for item_batch in batch_result:
            if ret is None:
                ret = item_batch
            else:
                ret = ret + item_batch

    return ret


def dot(value, w, bits):
    """

    dot

    accelerate function `dot` in base_operator.py

    Parameters
    ----------
    value
    w
    bits: 1024 or 2048

    Returns
    -------


    """
    if isinstance(value, Instance):
        X = value.features
    else:
        X = value

    # dot(a, b)[i, j, k, m] = sum(a[i, j, :] * b[k, :, m])
    # One-dimension dot, which is the inner product of these two arrays

    # At present, only the case of np.ndim(X) == 2 and np.ndim(w) == 1 is processed,
    # the others will be processed in the future
    if np.ndim(X) == np.ndim(w) == 1:
        return _one_dimension_dot(X, w)
    elif np.ndim(X) == 2 and np.ndim(w) == 1:

        if isinstance(X, csr_matrix):
            res = []
            for x in X:
                res.append(_one_dimension_dot(x, w))
            res = np.array(res)
        else:
            # GPU acceleration is used here, w is ciphertext, X is plaintext
            process_count = multiprocessing.cpu_count()
            process_count = int(process_count / 2) or 1
            pool = multiprocessing.Pool(processes=process_count)
            each_size = math.ceil(len(X) / process_count)
            process_list = []
            for i in range(process_count):
                x = X[i * each_size:(i + 1) * each_size]
                if len(x) > 0:
                    process_list.append(pool.apply_async(_gpu_dot_4_batch, args=(x, w, bits)))
            pool.close()
            pool.join()

            res = []
            for item_process in process_list:
                item_result = item_process.get()
                res.extend(item_result)

            res = np.array(res)
    else:
        res = np.dot(X, w)

    return res


def _gpu_dot_4_batch(X, w, bits):
    res = []
    batch_w = []
    batch_x = []

    # Record the length of each x,
    # in order to restore the calculation result of the corresponding number according to the length
    x_shape_to_restore = []
    batch_result = []
    result_array = []

    for x in X:
        x_shape_to_restore.append(len(x))
        for j in range(len(x)):
            batch_w.append(w[j])
            batch_x.append(x[j])
            if len(batch_w) >= BATCH_SIZE:
                # submit to gpu calc
                batch_result.extend(_gpu_powm_batch(batch_w, batch_x, bits))
                batch_w = []
                batch_x = []
                # _restore_batch_result_2_array(x_length_to_restore, batch_result, result_array)
                # _result_array_reduce_add(result_array)

    # submit residue to gpu
    if len(batch_w) > 0:
        batch_result.extend(_gpu_powm_batch(batch_w, batch_x, bits))
    _restore_batch_result_2_array(x_shape_to_restore, batch_result, result_array)
    _result_array_reduce_add(result_array, bits)

    # Submit the remaining batches that are not enough to use CPU calculation and return the result
    for item_result_array in result_array:
        item_result = 0
        for item in item_result_array:
            item_result += item
        res.append(item_result)

    return res
    # return np.array(res)


def _restore_batch_result_2_array(x_length_to_restore: list, batch_result: list, result_array: list):
    """
    Restore the flattened GPU operation results back to the multi-dimensional array structure

    Parameters
    ----------
    x_length_to_restore
    batch_result
    result_array

    Returns
    -------

    """
    # scheme 1
    # while len(x_length_to_restore) > 0:
    #     if len(batch_result) >= x_length_to_restore[0]:
    #         result_array.append(batch_result[0:x_length_to_restore[0]])
    #         del batch_result[0:x_length_to_restore[0]]
    #         x_length_to_restore.pop(0)
    #     else:
    #         break

    # scheme 2
    if len(x_length_to_restore) > 0:
        each_size = x_length_to_restore[0]
        times = len(batch_result) // each_size
        if times > 0:
            for i in range(times):
                result_array.append(batch_result[i * each_size:(i + 1) * each_size])
            del batch_result[0:each_size * times]
            del x_length_to_restore[0:times]


def _dot_list_to_restore(x_length_to_restore: list, res: list, batch_result: list):
    """
    restore the result of dot

    Parameters
    ----------
    x_length_to_restore:list
        Record the length of each x, in order to restore the calculation result of the corresponding number
        according to the length

    res:list
        the final result

    batch_result:list
        GPU batch calculation results

    Returns
    -------

    """
    while len(x_length_to_restore) > 0:
        if len(batch_result) >= x_length_to_restore[0]:
            item_result = 0
            for i in range(x_length_to_restore[0]):
                item_result += batch_result[i]
            res.append(item_result)
            del batch_result[0:x_length_to_restore[0]]
            x_length_to_restore.pop(0)
        else:
            break


def _to_align_exponent(to_align_exponent_list, bits):
    param_4_gpu = []  # to call powm
    param_4_local = []  # (PaillierEncryptNumber, exponent, is_left)

    to_restore_index = []

    for i in range(len(to_align_exponent_list)):
        item_pair = to_align_exponent_list[i]
        left = item_pair[0]
        right = item_pair[1]

        if left.exponent < right.exponent:
            param = left.gpu_increase_exponent_before(right.exponent)
            param_4_gpu.append(param[0])
            param_4_local.append((left, param[1], True))
            to_restore_index.append(i)
        elif left.exponent > right.exponent:
            param = right.gpu_increase_exponent_before(left.exponent)
            param_4_gpu.append(param[0])
            param_4_local.append((right, param[1], False))
            to_restore_index.append(i)

    aclr_client = RuntimeInstance.get_alcr_ins()
    result = aclr_client.powm(param_4_gpu, param_4_local, bits,
                              lambda item_local, ciphertext: item_local[0].gpu_increase_exponent_after(ciphertext,
                                                                                                       item_local[1],
                                                                                                       False))
    for i in range(len(to_restore_index)):
        is_left = param_4_local[i][2]
        src_list_index = to_restore_index[i]
        item_pair = to_align_exponent_list[src_list_index]
        if is_left:
            item_pair = (result[i], item_pair[1])
        else:
            item_pair = (item_pair[0], result[i])
        to_align_exponent_list[src_list_index] = item_pair

    return to_align_exponent_list


def _result_array_reduce_add(result_array: list, bits):
    """
    PaillierEncryptedNumber result add

    Parameters
    ----------
    result_array

    Returns
    -------

    """

    # The addition is performed in a loop until the batch condition is not met
    while True:

        vaild_pair_cnt = 0
        for item_array in result_array:
            vaild_pair_cnt += len(item_array) // 2

        # Determine whether the conditions for batch submission are met
        if vaild_pair_cnt >= MIN_ADD_BATCH_SIZE:

            # Store the Modular multiplication parameters that need to be provided to the gpu operation
            param_4_gpu = []

            # Store the original object and exponent parameters of the paillier,
            # and restore the object after the GPU calculation is completed
            param_4_local = []

            to_restore_size = []
            current_batch_size = 0

            to_align_exponent_list = []

            for item_array in result_array:
                item_array_length = len(item_array)
                item_submit_count = 0
                if current_batch_size == BATCH_SIZE:
                    break

                for i in range(0, item_array_length, 2):
                    if i == item_array_length - 1:
                        break

                    to_align_exponent_list.append((item_array[i], item_array[i + 1]))
                    # param = item_array[i].gpu_add_before(item_array[i + 1])
                    # param_4_gpu.append(param[0])
                    # param_4_local.append((item_array[i], param[1]))
                    item_submit_count += 1
                    current_batch_size += 1
                    if current_batch_size == BATCH_SIZE:
                        break

                to_restore_size.append(item_submit_count)

            # first align exponent
            _to_align_exponent(to_align_exponent_list, bits)
            for item in to_align_exponent_list:
                param = item[0].gpu_add_before(item[1])
                param_4_gpu.append(param[0])
                param_4_local.append((item[0], param[1]))

            aclr_client = RuntimeInstance.get_alcr_ins()
            gpu_result = aclr_client.mulm(param_4_gpu, param_4_local, bits)

            for idx in range(len(to_restore_size)):
                each_pair_size = to_restore_size[idx]
                # Remove objects that have been added
                del result_array[idx][0:each_pair_size * 2]
                # Combine the result of the addition into the original array.
                # Since the addition does not need to consider the order, it is directly `extended`
                result_array[idx].extend(gpu_result[0:each_pair_size])
                # Remove processed results from gpu_result
                del gpu_result[0:each_pair_size]

        else:
            break


def _one_dimension_dot(X, w):
    res = 0
    # LOGGER.debug("_one_dimension_dot, len of w: {}, len of X: {}".format(len(w), len(X)))
    if isinstance(X, csr_matrix):
        for idx, value in zip(X.indices, X.data):
            res += value * w[idx]
    else:
        for i in range(len(X)):
            if np.fabs(X[i]) < 1e-5:
                continue
            res += w[i] * X[i]

    if res == 0:
        if isinstance(w[0], PaillierEncryptedNumber):
            res = 0 * w[0]
    return res


def _gpu_powm_batch(w_batch: list, x_batch: list, bits):
    """
    Do batch modular exponentiation operations on wx

    Parameters
    ----------
    w_batch:list
    x_batch:list

    Returns
    -------

    """
    first_w = w_batch[0]
    if isinstance(first_w, PaillierEncryptedNumber):
        param_4_gpu = []
        param_4_local = []

        for i in range(len(w_batch)):
            param = w_batch[i].gpu_mul_before(x_batch[i])
            param_4_gpu.append(param[0])
            param_4_local.append((w_batch[i], param[1]))

        aclr_client = RuntimeInstance.get_alcr_ins()
        return aclr_client.powm(param_4_gpu, param_4_local, bits)
    else:
        raise NotSupportTypeError(w=first_w)


def _gpu_tensordot_with_paillier_4batch(x_batch: list, y_batch: list, bits):
    """
    Batch submission of homomorphic multiplication operations

    Parameters
    ----------
    x_batch:list
        [[E(x)...],[E(x)...]]
    y_batch:list
        [[E(y)...],[E(y)...]]

    Returns
    -------

    """
    first_x_batch = x_batch[0]
    # first_y_batch = y_batch[0]

    if isinstance(first_x_batch[0], PaillierEncryptedNumber):
        batch_data_shape = []
        result = []
        batch_param_4_gpu = []
        batch_param_4_local = []
        aclr_client = RuntimeInstance.get_alcr_ins()

        for each_batch_index in range(len(x_batch)):

            x = x_batch[each_batch_index]
            y = y_batch[each_batch_index]

            x_length = x.shape[0]
            y_length = y.shape[0]
            batch_data_shape.append((x_length, y_length))

            for i in range(x_length):
                for j in range(y_length):
                    # param: (x,p,m),exponent
                    param = x[i].gpu_mul_before(y[j])
                    batch_param_4_gpu.append(param[0])
                    # batch_param_4_local: (PaillierEncryptedNumber), exponent
                    batch_param_4_local.append((x[i], param[1]))

        # Submit to GPU calculation
        if len(batch_param_4_gpu) > 0:
            result.extend(aclr_client.powm(batch_param_4_gpu, batch_param_4_local, bits))

            while len(batch_data_shape) > 0:
                item_shape = batch_data_shape[0]
                shape_length = item_shape[0] * item_shape[1]
                if len(result) >= shape_length:
                    yield np.asarray(result[0:shape_length]).reshape(shape_length // y_length, y_length)
                    del result[0:shape_length]
                    batch_data_shape.pop(0)
                else:
                    break

    else:
        for i in range(len(x_batch)):
            yield np.tensordot(x_batch[i], y_batch[i], [[], []])


if __name__ == '__main__':
    pass
