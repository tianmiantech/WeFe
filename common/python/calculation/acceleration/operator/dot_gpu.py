# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

import random
import gmpy2
import ctypes
import numpy as np
import datetime as dt

from ctypes import cdll, sizeof, c_buffer, cast, c_int32
from ctypes import c_char, c_char_p, c_void_p, c_uint32, c_double, c_int64, c_int, c_size_t, c_longlong

from kernel.security import gmpy_math
from kernel.security.fixedpoint import FixedPointNumber
from kernel.security.paillier import PaillierEncryptedNumber
from kernel.security.paillier import PaillierKeypair


############################## dot from Wefe ##############################

# X is value, w is PaillierEncryptedNumber
def _one_dimension_dot_WEFE(X, w):
    res = 0
    for i in range(len(X)):
        if np.fabs(X[i]) < 1e-5:
            continue
        # print("[Python] -------------- __mul__ [ %i ] -------------- " % (i))
        ji = w[i] * X[i]
        res += ji

        '''
        print("[Python] _one_dimension_dot_WEFE: i = %i " % (i))
        print("[Python] _one_dimension_dot_WEFE: X[i] = %i " % (X[i]))
        print("[Python] _one_dimension_dot_WEFE: w[i] = %i " % (w[i].ciphertext(False)))
        print("[Python] _one_dimension_dot_WEFE: w[i] * X[i] = %i " % (ji.ciphertext(False)))
        '''

    if res == 0:
        if isinstance(w[0], PaillierEncryptedNumber):
            res = 0 * w[0]

    return res


# X is value, w is PaillierEncryptedNumber
def _dot_WEFE(X, w):
    if np.ndim(X) == np.ndim(w) == 1:
        return _one_dimension_dot_WEFE(X, w)
    elif np.ndim(X) == 2 and np.ndim(w) == 1:
        res = []
        for x in X:
            row_dot = _one_dimension_dot_WEFE(x, w)
            res.append(row_dot)
            # print("[Python] _dot_WEFE: row_dot = %i " % (row_dot.ciphertext(False)))

        res = np.array(res)
        return res


def _dot_decrypted_WEFE(private_key, X, w):
    res = []

    timebegin = dt.datetime.now()

    W = _dot_WEFE(X, w)

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(f' ')
    print(" _dot_WEFE, cost time:  %f " % (costtime))
    print(f' ')

    timebegin = dt.datetime.now()
    for w in W:
        res.append(private_key.decrypt(w))

    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(f' ')
    print(" private_key.decrypt WEFE, cost time:  %f " % (costtime))
    print(f' ')

    res = np.array(res)
    return res


############################## multi, sum, dot test ##############################

# X is value, w is PaillierEncryptedNumber
def _one_dimension_ElementwiseMultiply_TEST(X, w):
    res = []
    for i in range(len(X)):
        if np.fabs(X[i]) < 1e-5:
            continue
        res.append(w[i] * X[i])

    res = np.array(res)
    return res


# W is PaillierEncryptedNumber
def _one_dimension_ReduceSum_TEST(W):
    res = 0
    for i in range(len(W)):
        res += W[i]

    return res


# X is value, w is PaillierEncryptedNumber
def _ElementwiseMultiply_TEST(X, w):
    if np.ndim(X) == np.ndim(w) == 1:
        return _one_dimension_ElementwiseMultiply_TEST(X, w)
    elif np.ndim(X) == 2 and np.ndim(w) == 1:
        res = []
        for x in X:
            res.append(_one_dimension_ElementwiseMultiply_TEST(x, w))

        res = np.array(res)
        return res


# W is PaillierEncryptedNumber
def _ReduceSum_TEST(W):
    if np.ndim(W) == 1:
        return _one_dimension_ReduceSum_TEST(W)
    elif np.ndim(W) == 2:
        res = []
        for w in W:
            res.append(_one_dimension_ReduceSum_TEST(w))

        res = np.array(res)
        return res


# W is PaillierEncryptedNumber
def _Decrypt_TEST(private_key, W):
    res = []
    for w in W:
        res.append(private_key.decrypt(w))

    res = np.array(res)
    return res



def DotGPU(X, w):
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


    matrixRowCount = X.shape[0]
    matrixColumnCount = X.shape[1]

    valueMatrixArray = X.reshape(matrixRowCount * matrixColumnCount)
    w_count = w.shape[0]
    public_key = w[0].public_key

    g_bytes = public_key.g.to_bytes(CIPHER_BYTE, 'little')
    n_bytes = public_key.n.to_bytes(CIPHER_BYTE, 'little')
    nsquare_bytes = public_key.nsquare.to_bytes(CIPHER_BYTE, 'little')
    max_int_bytes = public_key.max_int.to_bytes(CIPHER_BYTE, 'little')

    # GPU computing...
    GPU_LIB = cdll.LoadLibrary("/usr/lib/libgpuhomomorphism.so")
    GPU_LIB.GPU_H_C_Malloc.restype = c_void_p
    GPU_LIB.GPU_H_Paillier_Encode.restype = c_void_p
    GPU_LIB.GPU_H_Paillier_MatrixElementWiseMultiplyColumn.restype = c_void_p

    GPUtimebegin = dt.datetime.now()

    valueMatrixArray_size = valueMatrixArray.size
    valueMatrixArray_data = GPU_LIB.GPU_H_C_Malloc(c_size_t(valueMatrixArray_size * DOUBLE_BYTE))
    # all turn to 64 bit data type
    if (valueMatrixArray.dtype == 'int32'):
        valueMatrixArray_astyped = valueMatrixArray.astype(np.int64)
        valueMatrixArray_ctypes = valueMatrixArray_astyped.ctypes.data_as(c_void_p)
        data_type = INT64_TYPE
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueMatrixArray_data), valueMatrixArray_ctypes,
                               valueMatrixArray_size * INT64_BYTE)
    elif (valueMatrixArray.dtype == 'int64'):
        valueMatrixArray_ctypes = valueMatrixArray.ctypes.data_as(c_void_p)
        data_type = INT64_TYPE
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueMatrixArray_data), valueMatrixArray_ctypes,
                               valueMatrixArray_size * INT64_BYTE)
    elif (valueMatrixArray.dtype == 'float32'):
        valueMatrixArray_astyped = valueMatrixArray.astype(np.float64)
        valueMatrixArray_ctypes = valueMatrixArray_astyped.ctypes.data_as(c_void_p)
        data_type = FLOAT_TYPE
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueMatrixArray_data), valueMatrixArray_ctypes,
                               valueMatrixArray_size * DOUBLE_BYTE)
    elif (valueMatrixArray.dtype == 'float64'):
        valueMatrixArray_ctypes = valueMatrixArray.ctypes.data_as(c_void_p)
        data_type = FLOAT_TYPE
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueMatrixArray_data), valueMatrixArray_ctypes,
                               valueMatrixArray_size * DOUBLE_BYTE)
    else:
        raise PermissionError("Invalid Data Type of valueMatrixArray")

    timebegin = dt.datetime.now()
    valueMatrixArray_encoded = GPU_LIB.GPU_H_Paillier_Encode(
        1,
        c_longlong(data_type),
        c_void_p(valueMatrixArray_data),
        c_size_t(matrixRowCount * matrixColumnCount),
        c_char_p(g_bytes),
        c_char_p(n_bytes),
        c_char_p(nsquare_bytes),
        c_char_p(max_int_bytes)
    )
    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(" GPU_LIB.GPU_H_Paillier_Encode matrix, cost time:  %f " % (costtime))

    valueColumn_encoded = GPU_LIB.GPU_H_C_Malloc(c_size_t(w_count * ( 6 * CIPHER_BYTE + 2 * INT64_BYTE )))
    ii = 0
    for i in range(w_count):
        # skip x_sign
        ii = ii+INT64_BYTE

        #x
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueColumn_encoded + ii),
                               w[i].ciphertext().to_bytes(CIPHER_BYTE, 'little'),
                               c_size_t(CIPHER_BYTE))
        ii = ii + CIPHER_BYTE

        #x_exponent
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueColumn_encoded + ii),
                               w[i].exponent.to_bytes(INT64_BYTE, 'little'),
                               c_size_t(INT64_BYTE))
        ii = ii + INT64_BYTE

        #g
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueColumn_encoded + ii),
                               g_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        #n
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueColumn_encoded + ii),
                               n_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        #nsquare
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueColumn_encoded + ii),
                               nsquare_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        #max_int
        GPU_LIB.GPU_H_C_Memcpy(c_void_p(valueColumn_encoded + ii),
                               max_int_bytes,
                               CIPHER_BYTE)
        ii = ii + CIPHER_BYTE

        #skip random
        ii = ii + CIPHER_BYTE

    timebegin = dt.datetime.now()
    matrixMultiplied = GPU_LIB.GPU_H_Paillier_MatrixElementWiseMultiplyColumn(
        1,
        c_void_p(valueMatrixArray_encoded),
        c_void_p(valueColumn_encoded),
        c_size_t(matrixRowCount),
        c_size_t(matrixColumnCount)
    )
    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(" GPU_LIB.GPU_H_Paillier_MatrixElementWiseMultiplyColumn, cost time:  %f " % (costtime))

    # malloc output reduce sum host memory
    outSumArray = GPU_LIB.GPU_H_C_Malloc(c_size_t(matrixRowCount * CIPHER_BYTE))
    outExponentArray = GPU_LIB.GPU_H_C_Malloc(c_size_t(matrixRowCount * INT64_BYTE))

    timebegin = dt.datetime.now()
    GPU_LIB.GPU_H_Paillier_MatrixRowSumUp(
        1,
        c_void_p(matrixMultiplied),
        c_size_t(matrixRowCount),
        c_size_t(matrixColumnCount),
        c_char_p(outSumArray),
        c_char_p(outExponentArray)
    )
    timeover = dt.datetime.now()
    costtime = (timeover - timebegin).total_seconds()
    print(" GPU_LIB.GPU_H_Paillier_MatrixRowSumUp, cost time:  %f " % (costtime))

    # parse GPU reduce sum result
    out_sum_PaillierEncryptedNumber_array = np.empty(matrixRowCount, dtype=PaillierEncryptedNumber)
    out_sum_pen_bytes = c_buffer(CIPHER_BYTE)
    out_exponent_bytes = c_buffer(INT64_BYTE)
    for i in range(matrixRowCount):
        GPU_LIB.GPU_H_C_Memcpy(cast(out_sum_pen_bytes, c_void_p), c_char_p(outSumArray + i * CIPHER_BYTE),
                               CIPHER_BYTE)
        out_sum_pen = int.from_bytes(out_sum_pen_bytes.raw, 'little')
        # print(" test_GPUDot >>> , out_sum_pen:  %d " % (out_sum_pen))

        GPU_LIB.GPU_H_C_Memcpy(cast(out_exponent_bytes, c_void_p), c_char_p(outExponentArray + i * INT64_BYTE),
                               INT64_BYTE)
        exponent = int.from_bytes(out_exponent_bytes.raw, 'little')
        # print(" test_GPUDot >>> , exponent:  %d " % (exponent))
        out_sum_PaillierEncryptedNumber_array[i] = PaillierEncryptedNumber(public_key, out_sum_pen, exponent)

    # free memory
    GPU_LIB.GPU_H_C_Free(c_void_p(valueMatrixArray_data))
    GPU_LIB.GPU_H_C_Free(c_void_p(valueMatrixArray_encoded))
    GPU_LIB.GPU_H_C_Free(c_void_p(valueColumn_encoded))
    GPU_LIB.GPU_H_C_Free(c_void_p(matrixMultiplied))
    GPU_LIB.GPU_H_C_Free(c_void_p(outSumArray))
    GPU_LIB.GPU_H_C_Free(c_void_p(outExponentArray))

    GPUtimeover = dt.datetime.now()
    GPUcosttime = (GPUtimeover - GPUtimebegin).total_seconds()
    print(f' ')
    print(" GPU dot, total cost time:  %f " % (GPUcosttime))
    print(f' ')

    return out_sum_PaillierEncryptedNumber_array


def test_DotGPU(compareCPU, inDataType, matrixRowCount, matrixColumnCount, floatStart, intStart):
    PAILLIER_KEY_BITS = 1024

    # print("test_GPUDot, matrix size: { %i x %i } ---------------------------------------------------- begin" % (matrixRowCount, matrixColumnCount))

    if 1 == inDataType:
        valueMatrixArray = np.arange(intStart, intStart + matrixRowCount * matrixColumnCount)
        X = valueMatrixArray.reshape(matrixRowCount, matrixColumnCount)
        #X = valueMatrix.reshape(matrixRowCount*matrixColumnCount)
        valueColumn = np.arange(intStart * 3, intStart * 3 + matrixColumnCount)
    else:
        valueMatrixArray = np.arange(floatStart, floatStart + matrixRowCount * matrixColumnCount)
        X = valueMatrixArray.reshape(matrixRowCount, matrixColumnCount)
        #X = valueMatrix.reshape(matrixRowCount * matrixColumnCount)
        valueColumn = np.arange(floatStart + intStart, floatStart + intStart + matrixColumnCount)

    public_key, private_key = PaillierKeypair.generate_keypair(PAILLIER_KEY_BITS)


    w = np.empty(matrixColumnCount, dtype=PaillierEncryptedNumber)  # pen: paillier encrypted number
    for i in range(matrixColumnCount):
        w[i] = public_key.encrypt(valueColumn[i], None)

    # penColumn_0_ciphertext = penColumn[0].ciphertext(False)
    # print("[Python] public_key.encrypt: penColumn[0].ciphertext = %i " % (penColumn_0_ciphertext))

    if compareCPU:
        plainDotWEFE = _dot_decrypted_WEFE(private_key, X, w)

    out_sum_PaillierEncryptedNumber_array = DotGPU(X, w)

    if compareCPU:

        # check GPU reduce sum result
        plainDotGPU = _Decrypt_TEST(private_key, out_sum_PaillierEncryptedNumber_array)

        eq = np.array_equal(plainDotGPU, plainDotWEFE)
    else:
        eq = True

    '''
    if(eq):
        equal = "True"
    else:
        equal = "False"

    print(f' ')
    print(f'test_GPUDot ---> : plainDotGPU == plainDotWEFE : '+equal)
    print(f' ')
    '''

    # print(f'test_GPUDot ---------------------------------------------------- over')

    return eq;


# Press the green button in the gutter to run the script.
if __name__ == '__main__':

    repeat_times = 5
    datatype = 2 #1:int, 2:float
    compareCPU = True;

    for i in range(repeat_times):
        matrixRowCount = 3#random.SystemRandom().randrange(1, 2000)
        matrixColumnCount = 5#random.SystemRandom().randrange(2, 80)
        floatStart = random.SystemRandom().randrange(-100, 100) + random.SystemRandom().randrange(-999999,
                                                                                                  999999) / 1000000
        intStart = random.SystemRandom().randrange(-50, 50)

        print("----------- test_GPUDot [%i] , matrix size: { %i x %i } , datatype: %i -----------" % (i,
                                                                                                 matrixRowCount,
                                                                                                 matrixColumnCount,datatype))
        print(f'floatStart = {floatStart}')
        print(f'intStart   = {intStart}')

        equal = test_DotGPU(compareCPU, datatype, matrixRowCount, matrixColumnCount, floatStart, intStart)
        if not equal:
            print(f' ')
            print(f'ERROR: GPU result NOT EQUAL python result !!!!!!')
            print(f' ')
            break
        else:
            print(f'Success ...')
            print(f' ')

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
