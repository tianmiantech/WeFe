import ctypes
import numpy as np
import datetime as dt
import random
from ctypes import cdll, sizeof, c_buffer, cast, c_int32
from ctypes import c_char, c_char_p, c_void_p, c_uint32, c_double, c_int64, c_int, c_size_t, c_longlong
from kernel.security.paillier import PaillierEncryptedNumber


# paillier decrypt
# private_key: PaillierPrivateKey
# encrypted_number_array: PaillierEncryptedNumber array
def gpu_paillier_raw_decrypt(private_key, encrypted_number_array, be_secure=True):
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

    if not isinstance(encrypted_number_array[0], PaillierEncryptedNumber):
        raise TypeError("encrypted_number_array should be an PaillierEncryptedNumber array, \
                         not: %s" % type(encrypted_number_array[0]))

    if isinstance(encrypted_number_array, list):
        array_element_count = len(encrypted_number_array)
    else:
        array_element_count = encrypted_number_array.shape[0]
    if array_element_count > MAX_COUNT:
        raise ValueError("Total input element count = %i , too large ！[ > %i ]" % (array_element_count, MAX_COUNT))

    public_key = encrypted_number_array[0].public_key
    is_obfuscator = encrypted_number_array[0].get_obfuscator()

    n_bytes = public_key.n.to_bytes(PLAIN_BYTE, 'little')
    g_bytes = public_key.g.to_bytes(PLAIN_BYTE, 'little')
    nsquare_bytes = public_key.nsquare.to_bytes(PLAIN_BYTE, 'little')
    max_int_bytes = public_key.max_int.to_bytes(PLAIN_BYTE, 'little')

    p_bytes = private_key.p.to_bytes(PLAIN_BYTE, 'little')
    q_bytes = private_key.q.to_bytes(PLAIN_BYTE, 'little')
    psquare_bytes = private_key.psquare.to_bytes(PLAIN_BYTE, 'little')
    qsquare_bytes = private_key.qsquare.to_bytes(PLAIN_BYTE, 'little')
    q_inverse_bytes = private_key.q_inverse.to_bytes(PLAIN_BYTE, 'little')
    hp_bytes = private_key.hp.to_bytes(PLAIN_BYTE, 'little')
    hq_bytes = private_key.hq.to_bytes(PLAIN_BYTE, 'little')

    # GPU computing...
    gpu_lib = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    gpu_lib.GPU_H_C_Malloc.restype = c_void_p
    gpu_lib.GPU_H_Paillier_Encode.restype = c_void_p
    gpu_lib.GPU_H_C_GetError.restype = c_char_p

    gpu_time_begin = dt.datetime.now()

    cipher_value_array = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * CIPHER_BYTE))
    if cipher_value_array is None:
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    cipher_exponent_array = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * INT64_BYTE))
    if cipher_exponent_array is None:
        gpu_lib.GPU_H_C_Free(c_void_p(cipher_value_array))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    plain_array = gpu_lib.GPU_H_C_Malloc(
        c_size_t(array_element_count * PLAIN_BYTE))
    if plain_array is None:
        gpu_lib.GPU_H_C_Free(c_void_p(cipher_value_array))
        gpu_lib.GPU_H_C_Free(c_void_p(cipher_exponent_array))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    for i in range(array_element_count):
        gpu_lib.GPU_H_C_Memcpy(c_void_p(cipher_value_array + i * CIPHER_BYTE),
                               encrypted_number_array[i].ciphertext(False).to_bytes(
                                   CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))

        gpu_lib.GPU_H_C_Memcpy(c_void_p(cipher_exponent_array + i * INT64_BYTE),
                               encrypted_number_array[i].exponent.to_bytes(
                                   INT64_BYTE, 'little'),
                               c_size_t(INT64_BYTE))

    if be_secure and not is_obfuscator:
        do_apply_obfuscator = 1
        obfuscator = random.SystemRandom().randrange(1, public_key.n)
        obfuscator_bytes = obfuscator.to_bytes(CIPHER_BYTE, 'little')

        print(" do_apply_obfuscator = 1 ")
    else:
        do_apply_obfuscator = 0
        obfuscator_bytes = None

        print(" do_apply_obfuscator = 0 ")

    timebegin = dt.datetime.now()
    ret = gpu_lib.GPU_H_Paillier_RawDecrypt(
        1,
        c_char_p(obfuscator_bytes),
        c_char_p(cipher_value_array),
        c_char_p(cipher_exponent_array),
        array_element_count,
        c_char_p(g_bytes),
        c_char_p(n_bytes),
        c_char_p(nsquare_bytes),
        c_char_p(max_int_bytes),
        c_char_p(p_bytes),
        c_char_p(q_bytes),
        c_char_p(psquare_bytes),
        c_char_p(qsquare_bytes),
        c_char_p(q_inverse_bytes),
        c_char_p(hp_bytes),
        c_char_p(hq_bytes),
        c_char_p(plain_array)
    )
    if 0 != ret:
        gpu_lib.GPU_H_C_Free(c_void_p(cipher_value_array))
        gpu_lib.GPU_H_C_Free(c_void_p(cipher_exponent_array))
        gpu_lib.GPU_H_C_Free(c_void_p(plain_array))
        error_message = gpu_lib.GPU_H_C_GetError()
        raise ValueError("gpu_lib ERROR: " +
                         str(error_message, encoding='utf8'))

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(" gpu_lib.GPU_H_Paillier_RawDecrypt, cost time:  %f " % (costtime))
    print(f' ')

    plain_value_array = []
    for i in range(array_element_count):
        x_string = ctypes.string_at(plain_array + i * PLAIN_BYTE, CIPHER_BYTE)
        x = int.from_bytes(x_string, 'little')
        plain_value_array.append(x)

    plain_value_array = np.array(plain_value_array)

    gpu_lib.GPU_H_C_Free(c_void_p(cipher_value_array))
    gpu_lib.GPU_H_C_Free(c_void_p(cipher_exponent_array))
    gpu_lib.GPU_H_C_Free(c_void_p(plain_array))

    gpu_time_over = dt.datetime.now()
    gpu_cost_time = (gpu_time_over - gpu_time_begin).total_seconds()
    print(" GPU raw decrypt, total cost time:  %f " % (gpu_cost_time))
    print(f' ')

    return plain_value_array
