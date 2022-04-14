import numpy as np
import datetime as dt
import ctypes
from ctypes import cdll, sizeof, c_buffer, cast, c_int32
from ctypes import c_char, c_char_p, c_void_p, c_uint32, c_double, c_int64, c_int, c_size_t, c_longlong

from numba import jit

from kernel.security.paillier import PaillierEncryptedNumber


def copy_data(matrix_row_count, matrix_column_count, W, gpu_lib,
              pen_matrix_array, one_bytes, zero_bytes, CIPHER_BYTE, INT64_BYTE,
              g_bytes, n_bytes, nsquare_bytes, max_int_bytes):
    ii = 0
    for i in range(matrix_row_count):
        for j in range(matrix_column_count):
            if W[i][j] is None:
                # x
                if j < 5:
                    timebegin = dt.datetime.now()
                gpu_lib.GPU_H_C_Memcpy(c_void_p(pen_matrix_array + ii),
                                       one_bytes,
                                       c_size_t(CIPHER_BYTE))
                if j < 5:
                    timeover = dt.datetime.now()
                    costtime = (timeover - timebegin).total_seconds()
                    print(" GPU_H_C_Memcpy x <is None>, cost time:  %f " % (costtime))
                ii = ii + CIPHER_BYTE

                # x_exponent
                gpu_lib.GPU_H_C_Memcpy(c_void_p(pen_matrix_array + ii),
                                       zero_bytes,
                                       c_size_t(INT64_BYTE))
                ii = ii + INT64_BYTE
            else:
                # x
                # if j < 5:
                #    timebegin = dt.datetime.now()
                x_bytes = W[i][j].ciphertext(False).to_bytes(
                    CIPHER_BYTE, 'little')
                # if j < 5:
                #    timeover = dt.datetime.now()
                #    costtime = (timeover - timebegin).total_seconds()
                # print(f"ciphertext:{W[i][j].ciphertext()}")
                #    print(" W[i][j].ciphertext().to_bytes, cost time:  %f " % (costtime))

                # if j < 5:
                #    timebegin = dt.datetime.now()
                gpu_lib.GPU_H_C_Memcpy(c_void_p(pen_matrix_array + ii),
                                       x_bytes,
                                       c_size_t(CIPHER_BYTE))
                # if j < 5:
                #    timeover = dt.datetime.now()
                #    costtime = (timeover - timebegin).total_seconds()
                #    print(" GPU_H_C_Memcpy x, cost time:  %f " % (costtime))

                ii = ii + CIPHER_BYTE

                # x_exponent
                gpu_lib.GPU_H_C_Memcpy(c_void_p(pen_matrix_array + ii),
                                       W[i][j].exponent.to_bytes(
                                           INT64_BYTE, 'little'),
                                       c_size_t(INT64_BYTE))
                ii = ii + INT64_BYTE

            # y (skip)
            ii = ii + CIPHER_BYTE

            # y_exponent (skip)
            ii = ii + INT64_BYTE

            # g
            gpu_lib.GPU_H_C_Memcpy(c_void_p(pen_matrix_array + ii),
                                   g_bytes,
                                   CIPHER_BYTE)
            ii = ii + CIPHER_BYTE

            # n
            gpu_lib.GPU_H_C_Memcpy(c_void_p(pen_matrix_array + ii),
                                   n_bytes,
                                   CIPHER_BYTE)
            ii = ii + CIPHER_BYTE

            # nsquare
            gpu_lib.GPU_H_C_Memcpy(c_void_p(pen_matrix_array + ii),
                                   nsquare_bytes,
                                   CIPHER_BYTE)
            ii = ii + CIPHER_BYTE

            # max_int
            if j < 1:
                timebegin = dt.datetime.now()
            gpu_lib.GPU_H_C_Memcpy(c_void_p(pen_matrix_array + ii),
                                   max_int_bytes,
                                   CIPHER_BYTE)

            if j < 1:
                timeover = dt.datetime.now()
                costtime = (timeover - timebegin).total_seconds()
                print(" GPU_H_C_Memcpy max_int, cost time:  %f " % (costtime))

            ii = ii + CIPHER_BYTE


def gpu_paillier_matrix_row_sum_up(W, public_key, matrix_row_count, matrix_column_count):
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

    total_count = matrix_row_count * matrix_column_count
    # public_key = W[0][0].public_key
    print(
        "gpu_paillier_matrix_row_sum_up, row: %f,  column: %f" %
        (matrix_row_count, matrix_column_count))

    one = 1
    zero = 0
    one_bytes = one.to_bytes(CIPHER_BYTE, 'little')
    zero_bytes = zero.to_bytes(INT64_BYTE, 'little')

    g_bytes = public_key.g.to_bytes(CIPHER_BYTE, 'little')
    n_bytes = public_key.n.to_bytes(CIPHER_BYTE, 'little')
    nsquare_bytes = public_key.nsquare.to_bytes(CIPHER_BYTE, 'little')
    max_int_bytes = public_key.max_int.to_bytes(CIPHER_BYTE, 'little')

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_Paillier_MatrixElementWiseMultiplyColumn.restype = c_void_p
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    pen_matrix_array = gpu_lib.GPU_H_C_Malloc(
        c_size_t(total_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if pen_matrix_array is None:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    timebegin = dt.datetime.now()

    # read data
    for i in range(matrix_row_count):
        for j in range(matrix_column_count):
            W[i][j]

    copy_data(matrix_row_count, matrix_column_count, W, gpu_lib,
              pen_matrix_array, one_bytes, zero_bytes, CIPHER_BYTE, INT64_BYTE,
              g_bytes, n_bytes, nsquare_bytes, max_int_bytes)

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(
        " fill GPU structure, cost time:  %f " %
        (costtime))
    print(f' ')

    # malloc output reduce sum host memory
    out_sum_array = gpu_lib.GPU_H_C_Malloc(
        c_size_t(matrix_row_count * CIPHER_BYTE))
    if out_sum_array is None:
        gpu_lib.GPU_H_C_Free(c_void_p(pen_matrix_array))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    out_exponent_array = gpu_lib.GPU_H_C_Malloc(
        c_size_t(matrix_row_count * INT64_BYTE))
    if out_exponent_array is None:
        gpu_lib.GPU_H_C_Free(c_void_p(pen_matrix_array))
        gpu_lib.GPU_H_C_Free(c_void_p(out_sum_array))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    timebegin = dt.datetime.now()
    ret = gpu_lib.GPU_H_Paillier_MatrixRowSumUp(
        1,
        c_void_p(pen_matrix_array),
        c_size_t(matrix_row_count),
        c_size_t(matrix_column_count),
        c_char_p(out_sum_array),
        c_char_p(out_exponent_array)
    )
    if 0 != ret:
        gpu_lib.GPU_H_C_Free(c_void_p(pen_matrix_array))
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

    timebegin = dt.datetime.now()
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
    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(
        " parse GPU reduce sum result, cost time:  %f " %
        (costtime))
    print(f' ')

    # free memory
    gpu_lib.GPU_H_C_Free(c_void_p(pen_matrix_array))
    gpu_lib.GPU_H_C_Free(c_void_p(out_sum_array))
    gpu_lib.GPU_H_C_Free(c_void_p(out_exponent_array))

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(" GPU matrix row sum up, total cost time:  %f " % (gpu_cost_time))
    print(f' ')

    return out_sum_paillier_encrypted_number_array


def gpu_paillier_array_pen_add_pen(a_array, b_array):
    """
        add
    Args:
        a_array: PaillierEncryptedNumber array
        b_array: PaillierEncryptedNumber array

    Returns:

    """
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

    if a_array.shape[0] != b_array.shape[0]:
        raise TypeError("a_array's shape[0] not equal b_array's shape[0] \
                        : %s  %s" % a_array.shape[0], b_array.shape[0])

    if not isinstance(a_array[0], PaillierEncryptedNumber):
        raise TypeError("a_array should be an PaillierEncryptedNumber array, \
                         not: %s" % type(a_array[0]))

    if not isinstance(b_array[0], PaillierEncryptedNumber):
        raise TypeError("b_array should be an PaillierEncryptedNumber array, \
                         not: %s" % type(b_array[0]))

    if a_array[0].public_key != b_array[0].public_key:
        raise TypeError(
            "a_array[0]'s public_key not equal b_array[0]'s public_key")

    public_key = a_array[0].public_key
    array_element_count = a_array.shape[0]

    g_bytes = public_key.g.to_bytes(CIPHER_BYTE, 'little')
    n_bytes = public_key.n.to_bytes(CIPHER_BYTE, 'little')
    nsquare_bytes = public_key.nsquare.to_bytes(CIPHER_BYTE, 'little')
    max_int_bytes = public_key.max_int.to_bytes(CIPHER_BYTE, 'little')

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_Paillier_Encode.restype = c_void_p
    gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation.restype = c_void_p
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    a_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if a_data is None:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    b_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if b_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    ii = 0
    for i in range(array_element_count):
        # skip x_sign
        ii = ii + INT64_BYTE

        # x
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               a_array[i].ciphertext().to_bytes(
                                   CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               b_array[i].ciphertext().to_bytes(
                                   CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        # x_exponent
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               a_array[i].exponent.to_bytes(
                                   INT64_BYTE, 'little'),
                               c_size_t(INT64_BYTE))
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               b_array[i].exponent.to_bytes(
                                   INT64_BYTE, 'little'),
                               c_size_t(INT64_BYTE))
        ii = ii + INT64_BYTE

        # g
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               g_bytes,
                               CIPHER_BYTE)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               g_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # n
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               n_bytes,
                               CIPHER_BYTE)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               n_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # nsquare
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # max_int
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # skip random
        ii = ii + CIPHER_BYTE

    # a_data, b_data ok, do add
    gpu_result = gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation(
        1,
        1,  # 1: add
        c_void_p(a_data),
        c_void_p(b_data),
        c_size_t(array_element_count)
    )
    if gpu_result is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # parse GPU result
    out_paillier_encrypted_number_array = np.empty(
        array_element_count, dtype=PaillierEncryptedNumber)
    out_sum_pen_bytes = c_buffer(CIPHER_BYTE)
    out_exponent_bytes = c_buffer(INT64_BYTE)
    element_len = CIPHER_BYTE * 6 + INT64_BYTE * 2
    for i in range(array_element_count):
        iii = i * element_len

        gpu_lib.GPU_H_C_Memcpy(cast(out_sum_pen_bytes, c_void_p), c_char_p(gpu_result + iii),
                               CIPHER_BYTE)
        out_sum_pen = int.from_bytes(out_sum_pen_bytes.raw, 'little')
        iii = iii + CIPHER_BYTE

        gpu_lib.GPU_H_C_Memcpy(cast(out_exponent_bytes, c_void_p), c_char_p(gpu_result + iii),
                               INT64_BYTE)
        exponent = int.from_bytes(out_exponent_bytes.raw, 'little')

        out_paillier_encrypted_number_array[i] = PaillierEncryptedNumber(
            public_key, out_sum_pen, exponent)

    # free memory
    gpu_lib.GPU_H_C_Free(c_void_p(a_data))
    gpu_lib.GPU_H_C_Free(c_void_p(b_data))
    gpu_lib.GPU_H_C_Free(c_void_p(gpu_result))

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(f' ')
    print(
        " GPU gpu_paillier_array_pen_add_pen, total cost time:  %f " %
        (gpu_cost_time))
    print(f' ')

    return out_paillier_encrypted_number_array


def gpu_paillier_array_pen_sub_pen(public_key, a_array, b_array):
    """
        sub
    Args:
        a_array: PaillierEncryptedNumber array
        b_array: PaillierEncryptedNumber array

    Returns:

    """
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

    if a_array.shape[0] != b_array.shape[0]:
        raise TypeError("a_array's shape[0] not equal b_array's shape[0] \
                        : %s  %s" % a_array.shape[0], b_array.shape[0])

    # if not isinstance(a_array[0], PaillierEncryptedNumber):
    #     raise TypeError("a_array should be an PaillierEncryptedNumber array, \
    #                      not: %s" % type(a_array[0]))

    # if not isinstance(b_array[0], PaillierEncryptedNumber):
    #     raise TypeError("b_array should be an PaillierEncryptedNumber array, \
    #                      not: %s" % type(b_array[0]))

    # if a_array[0].public_key != b_array[0].public_key:
    #     raise TypeError(
    #         "a_array[0]'s public_key not equal b_array[0]'s public_key")
    array_element_count = a_array.shape[0]

    zero = 0
    one = 1
    zero_bytes = zero.to_bytes(INT64_BYTE, 'little')
    one_bytes = one.to_bytes(CIPHER_BYTE, 'little')

    g_bytes = public_key.g.to_bytes(CIPHER_BYTE, 'little')
    n_bytes = public_key.n.to_bytes(CIPHER_BYTE, 'little')
    nsquare_bytes = public_key.nsquare.to_bytes(CIPHER_BYTE, 'little')
    max_int_bytes = public_key.max_int.to_bytes(CIPHER_BYTE, 'little')

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_Paillier_Encode.restype = c_void_p
    gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation.restype = c_void_p
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    a_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if a_data is None:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    b_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if b_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    ii = 0
    for i in range(array_element_count):
        # skip x_sign
        ii = ii + INT64_BYTE
        # print(f'a_array index: {i}, type: {type(a_array[i])}')
        # print(f'b_array index: {i}, type: {type(b_array[i])}')

        # x
        if 0 == a_array[i]:
            gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                                   one_bytes,
                                   c_size_t(CIPHER_BYTE))
        else:
            gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                                   a_array[i].ciphertext().to_bytes(
                                       CIPHER_BYTE, 'little'),
                                   c_size_t(CIPHER_BYTE))
        if 0 == b_array[i]:
            gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                                   one_bytes,
                                   c_size_t(CIPHER_BYTE))
        else:
            gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                                   b_array[i].ciphertext().to_bytes(
                                       CIPHER_BYTE, 'little'),
                                   c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        # x_exponent
        if 0 == a_array[i]:
            gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                                   zero_bytes,
                                   c_size_t(INT64_BYTE))
        else:
            gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                                   a_array[i].exponent.to_bytes(
                                       INT64_BYTE, 'little'),
                                   c_size_t(INT64_BYTE))

        if 0 == b_array[i]:
            gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                                   zero_bytes,
                                   c_size_t(INT64_BYTE))
        else:
            gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                                   b_array[i].exponent.to_bytes(
                                       INT64_BYTE, 'little'),
                                   c_size_t(INT64_BYTE))
        ii = ii + INT64_BYTE

        # g
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               g_bytes,
                               CIPHER_BYTE)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               g_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # n
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               n_bytes,
                               CIPHER_BYTE)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               n_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # nsquare
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # max_int
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_data + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # skip random
        ii = ii + CIPHER_BYTE

    # a_data, b_data ok, do sub
    neg_one_array = np.ones(array_element_count) * -1

    neg_one_value_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * DOUBLE_BYTE))
    if neg_one_value_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    neg_one_array_astyped = neg_one_array.astype(np.int64)
    neg_one_array_ctypes = neg_one_array_astyped.ctypes.data_as(c_void_p)
    data_type = INT64_TYPE
    gpu_lib.GPU_H_C_Memcpy(c_void_p(neg_one_value_data), neg_one_array_ctypes,
                           array_element_count * INT64_BYTE)

    neg_one_data = gpu_lib.GPU_H_Paillier_Encode(
        1,
        c_longlong(data_type),
        c_void_p(None),
        c_void_p(None),
        c_void_p(neg_one_value_data),
        c_size_t(array_element_count),
        c_char_p(g_bytes),
        c_char_p(n_bytes),
        c_char_p(nsquare_bytes),
        c_char_p(max_int_bytes)
    )
    if neg_one_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_data))
        gpu_lib.GPU_H_C_Free(c_void_p(neg_one_value_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    mul_neg_one_data = gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation(
        1,
        3,  # 3: mul
        c_void_p(b_data),
        c_void_p(neg_one_data),
        c_size_t(array_element_count)
    )
    if mul_neg_one_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_data))
        gpu_lib.GPU_H_C_Free(c_void_p(neg_one_value_data))
        gpu_lib.GPU_H_C_Free(c_void_p(neg_one_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    gpu_result = gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation(
        1,
        2,  # 2: sub
        c_void_p(a_data),
        c_void_p(mul_neg_one_data),
        c_size_t(array_element_count)
    )
    if gpu_result is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_data))
        gpu_lib.GPU_H_C_Free(c_void_p(neg_one_value_data))
        gpu_lib.GPU_H_C_Free(c_void_p(neg_one_data))
        gpu_lib.GPU_H_C_Free(c_void_p(mul_neg_one_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # parse GPU result
    out_paillier_encrypted_number_array = np.empty(
        array_element_count, dtype=PaillierEncryptedNumber)
    out_sum_pen_bytes = c_buffer(CIPHER_BYTE)
    out_exponent_bytes = c_buffer(INT64_BYTE)
    element_len = CIPHER_BYTE * 6 + INT64_BYTE * 2
    for i in range(array_element_count):
        iii = i * element_len

        gpu_lib.GPU_H_C_Memcpy(cast(out_sum_pen_bytes, c_void_p), c_char_p(gpu_result + iii),
                               CIPHER_BYTE)
        out_sum_pen = int.from_bytes(out_sum_pen_bytes.raw, 'little')
        iii = iii + CIPHER_BYTE

        gpu_lib.GPU_H_C_Memcpy(cast(out_exponent_bytes, c_void_p), c_char_p(gpu_result + iii),
                               INT64_BYTE)
        exponent = int.from_bytes(out_exponent_bytes.raw, 'little')

        out_paillier_encrypted_number_array[i] = PaillierEncryptedNumber(
            public_key, out_sum_pen, exponent)

    # free memory
    gpu_lib.GPU_H_C_Free(c_void_p(a_data))
    gpu_lib.GPU_H_C_Free(c_void_p(b_data))
    gpu_lib.GPU_H_C_Free(c_void_p(neg_one_value_data))
    gpu_lib.GPU_H_C_Free(c_void_p(neg_one_data))
    gpu_lib.GPU_H_C_Free(c_void_p(mul_neg_one_data))
    # gpu_lib.GPU_H_C_Free(c_void_p(gpu_result)) #gpu_result is same as
    # mul_neg_one_data,need not free

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(f' ')
    print(
        " GPU gpu_paillier_array_pen_sub_pen, total cost time:  %f " %
        (gpu_cost_time))
    print(f' ')

    return out_paillier_encrypted_number_array


def gpu_paillier_array_pen_mul_scalar(a_array, b_array):
    """
        mul
    Args:
        a_array: PaillierEncryptedNumber array
        b_array: int array or float array

    Returns:

    """
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

    if a_array.shape[0] != b_array.shape[0]:
        raise TypeError("a_array's shape[0] not equal b_array's shape[0] \
                        : %s  %s" % a_array.shape[0], b_array.shape[0])

    if not isinstance(a_array[0], PaillierEncryptedNumber):
        raise TypeError("a_array should be an PaillierEncryptedNumber array, \
                         not: %s" % type(a_array[0]))

    public_key = a_array[0].public_key
    array_element_count = a_array.shape[0]

    g_bytes = public_key.g.to_bytes(CIPHER_BYTE, 'little')
    n_bytes = public_key.n.to_bytes(CIPHER_BYTE, 'little')
    nsquare_bytes = public_key.nsquare.to_bytes(CIPHER_BYTE, 'little')
    max_int_bytes = public_key.max_int.to_bytes(CIPHER_BYTE, 'little')

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_Paillier_Encode.restype = c_void_p
    gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation.restype = c_void_p
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    a_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if a_data is None:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    ii = 0
    for i in range(array_element_count):
        # skip x_sign
        ii = ii + INT64_BYTE

        # x
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               a_array[i].ciphertext().to_bytes(
                                   CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        # x_exponent
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               a_array[i].exponent.to_bytes(
                                   INT64_BYTE, 'little'),
                               c_size_t(INT64_BYTE))
        ii = ii + INT64_BYTE

        # g
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               g_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # n
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               n_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # nsquare
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # max_int
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # skip random
        ii = ii + CIPHER_BYTE

    b_value_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * DOUBLE_BYTE))
    if b_value_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # all turn to 64 bit data type
    if (b_array.dtype == 'int32'):
        value_matrix_array_astyped = b_array.astype(np.int64)
        value_matrix_array_ctypes = value_matrix_array_astyped.ctypes.data_as(
            c_void_p)
        data_type = INT64_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_value_data), value_matrix_array_ctypes,
                               array_element_count * INT64_BYTE)
    elif (b_array.dtype == 'int64'):
        value_matrix_array_ctypes = b_array.ctypes.data_as(c_void_p)
        data_type = INT64_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_value_data), value_matrix_array_ctypes,
                               array_element_count * INT64_BYTE)
    elif (b_array.dtype == 'float32'):
        value_matrix_array_astyped = b_array.astype(np.float64)
        value_matrix_array_ctypes = value_matrix_array_astyped.ctypes.data_as(
            c_void_p)
        data_type = FLOAT_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_value_data), value_matrix_array_ctypes,
                               array_element_count * DOUBLE_BYTE)
    elif (b_array.dtype == 'float64'):
        value_matrix_array_ctypes = b_array.ctypes.data_as(c_void_p)
        data_type = FLOAT_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_value_data), value_matrix_array_ctypes,
                               array_element_count * DOUBLE_BYTE)
    else:
        raise PermissionError("Invalid Data Type of b_array")

    b_data = gpu_lib.GPU_H_Paillier_Encode(
        1,
        c_longlong(data_type),
        c_void_p(None),
        c_void_p(None),
        c_void_p(b_value_data),
        c_size_t(array_element_count),
        c_char_p(g_bytes),
        c_char_p(n_bytes),
        c_char_p(nsquare_bytes),
        c_char_p(max_int_bytes)
    )
    if b_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_value_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # a_data, b_data ok, do multiply
    gpu_result = gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation(
        1,
        3,  # 3: mul
        c_void_p(a_data),
        c_void_p(b_data),
        c_size_t(array_element_count)
    )
    if gpu_result is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_value_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # parse GPU result
    out_paillier_encrypted_number_array = np.empty(
        array_element_count, dtype=PaillierEncryptedNumber)
    out_sum_pen_bytes = c_buffer(CIPHER_BYTE)
    out_exponent_bytes = c_buffer(INT64_BYTE)
    element_len = CIPHER_BYTE * 6 + INT64_BYTE * 2
    for i in range(array_element_count):
        iii = i * element_len

        gpu_lib.GPU_H_C_Memcpy(cast(out_sum_pen_bytes, c_void_p), c_char_p(gpu_result + iii),
                               CIPHER_BYTE)
        out_sum_pen = int.from_bytes(out_sum_pen_bytes.raw, 'little')
        iii = iii + CIPHER_BYTE

        gpu_lib.GPU_H_C_Memcpy(cast(out_exponent_bytes, c_void_p), c_char_p(gpu_result + iii),
                               INT64_BYTE)
        exponent = int.from_bytes(out_exponent_bytes.raw, 'little')

        out_paillier_encrypted_number_array[i] = PaillierEncryptedNumber(
            public_key, out_sum_pen, exponent)

    # free memory
    gpu_lib.GPU_H_C_Free(c_void_p(a_data))
    gpu_lib.GPU_H_C_Free(c_void_p(b_value_data))
    gpu_lib.GPU_H_C_Free(c_void_p(b_data))
    gpu_lib.GPU_H_C_Free(c_void_p(gpu_result))

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(f' ')
    print(
        " GPU gpu_paillier_array_pen_mul_scalar, total cost time:  %f " %
        (gpu_cost_time))
    print(f' ')

    return out_paillier_encrypted_number_array


def gpu_paillier_array_pen_add_scalar(a_array, b_array):
    """
        add
    Args:
        a_array: PaillierEncryptedNumber array
        b_array: int array or float array

    Returns:

    """
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

    if a_array.shape[0] != b_array.shape[0]:
        raise TypeError("a_array's shape[0] not equal b_array's shape[0] \
                        : %s  %s" % a_array.shape[0], b_array.shape[0])

    if not isinstance(a_array[0], PaillierEncryptedNumber):
        raise TypeError("a_array should be an PaillierEncryptedNumber array, \
                         not: %s" % type(a_array[0]))

    public_key = a_array[0].public_key
    array_element_count = a_array.shape[0]

    g_bytes = public_key.g.to_bytes(CIPHER_BYTE, 'little')
    n_bytes = public_key.n.to_bytes(CIPHER_BYTE, 'little')
    nsquare_bytes = public_key.nsquare.to_bytes(CIPHER_BYTE, 'little')
    max_int_bytes = public_key.max_int.to_bytes(CIPHER_BYTE, 'little')

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_Paillier_Encode.restype = c_void_p
    gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation.restype = c_void_p
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    a_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * (6 * CIPHER_BYTE + 2 * INT64_BYTE)))
    if a_data is None:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    ii = 0
    for i in range(array_element_count):
        # skip x_sign
        ii = ii + INT64_BYTE

        # x
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               a_array[i].ciphertext().to_bytes(
                                   CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        # x_exponent
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               a_array[i].exponent.to_bytes(
                                   INT64_BYTE, 'little'),
                               c_size_t(INT64_BYTE))
        ii = ii + INT64_BYTE

        # g
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               g_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # n
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               n_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # nsquare
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # max_int
        gpu_lib.GPU_H_C_Memcpy(c_void_p(a_data + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        # skip random
        ii = ii + CIPHER_BYTE

    b_value_data = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * DOUBLE_BYTE))
    if b_value_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # all turn to 64 bit data type
    if (b_array.dtype == 'int32'):
        value_matrix_array_astyped = b_array.astype(np.int64)
        value_matrix_array_ctypes = value_matrix_array_astyped.ctypes.data_as(
            c_void_p)
        data_type = INT64_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_value_data), value_matrix_array_ctypes,
                               array_element_count * INT64_BYTE)
    elif (b_array.dtype == 'int64'):
        value_matrix_array_ctypes = b_array.ctypes.data_as(c_void_p)
        data_type = INT64_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_value_data), value_matrix_array_ctypes,
                               array_element_count * INT64_BYTE)
    elif (b_array.dtype == 'float32'):
        value_matrix_array_astyped = b_array.astype(np.float64)
        value_matrix_array_ctypes = value_matrix_array_astyped.ctypes.data_as(
            c_void_p)
        data_type = FLOAT_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_value_data), value_matrix_array_ctypes,
                               array_element_count * DOUBLE_BYTE)
    elif (b_array.dtype == 'float64'):
        value_matrix_array_ctypes = b_array.ctypes.data_as(c_void_p)
        data_type = FLOAT_TYPE
        gpu_lib.GPU_H_C_Memcpy(c_void_p(b_value_data), value_matrix_array_ctypes,
                               array_element_count * DOUBLE_BYTE)
    else:
        raise PermissionError("Invalid Data Type of b_array")

    b_data = gpu_lib.GPU_H_Paillier_Encode(
        1,
        c_longlong(data_type),
        c_void_p(None),
        c_void_p(a_data),  # use Pen array element's exponent as max_exponent
        c_void_p(b_value_data),
        c_size_t(array_element_count),
        c_char_p(g_bytes),
        c_char_p(n_bytes),
        c_char_p(nsquare_bytes),
        c_char_p(max_int_bytes)
    )
    if b_data is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_value_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # a_data, b_data ok, do multiply
    gpu_result = gpu_lib.GPU_H_Paillier_ArrayTwoInputOperation(
        1,
        4,  # 4: add Fpn
        c_void_p(a_data),
        c_void_p(b_data),
        c_size_t(array_element_count)
    )
    if gpu_result is None:
        gpu_lib.GPU_H_C_Free(c_void_p(a_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_value_data))
        gpu_lib.GPU_H_C_Free(c_void_p(b_data))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    # _debug_print_pub_one_variable_instance(gpu_lib, b_data, array_element_count)

    # _debug_print_pub_two_variable_instance(gpu_lib, 1, gpu_result, array_element_count)

    # parse GPU result
    out_paillier_encrypted_number_array = np.empty(
        array_element_count, dtype=PaillierEncryptedNumber)
    out_sum_pen_bytes = c_buffer(CIPHER_BYTE)
    out_exponent_bytes = c_buffer(INT64_BYTE)
    element_len = CIPHER_BYTE * 6 + INT64_BYTE * 2
    for i in range(array_element_count):
        iii = i * element_len

        gpu_lib.GPU_H_C_Memcpy(cast(out_sum_pen_bytes, c_void_p), c_char_p(gpu_result + iii),
                               CIPHER_BYTE)
        out_sum_pen = int.from_bytes(out_sum_pen_bytes.raw, 'little')
        iii = iii + CIPHER_BYTE

        gpu_lib.GPU_H_C_Memcpy(cast(out_exponent_bytes, c_void_p), c_char_p(gpu_result + iii),
                               INT64_BYTE)
        exponent = int.from_bytes(out_exponent_bytes.raw, 'little')

        out_paillier_encrypted_number_array[i] = PaillierEncryptedNumber(
            public_key, out_sum_pen, exponent)

    # free memory
    gpu_lib.GPU_H_C_Free(c_void_p(a_data))
    gpu_lib.GPU_H_C_Free(c_void_p(b_value_data))
    gpu_lib.GPU_H_C_Free(c_void_p(b_data))
    gpu_lib.GPU_H_C_Free(c_void_p(gpu_result))

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(f' ')
    print(
        " GPU gpu_paillier_array_pen_mul_scalar, total cost time:  %f " %
        (gpu_cost_time))
    print(f' ')

    return out_paillier_encrypted_number_array
