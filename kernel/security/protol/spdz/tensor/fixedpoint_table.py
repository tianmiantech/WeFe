import operator

import numpy as np
import ctypes

from common.python.calculation.acceleration import aclr
from common.python.session import is_table
from common.python.utils import log_utils
from common.python.utils.member import Member
from kernel.security.protol.spdz.beaver_triples import beaver_triplets
from kernel.security.protol.spdz.tensor import fixedpoint_numpy
from kernel.security.protol.spdz.tensor.base import TensorBase
from kernel.security.protol.spdz.utils import NamingService
from kernel.security.paillier import PaillierEncryptedNumber
from kernel.security.protol.spdz.utils.random_utils import urand_tensor
from ctypes import cdll, sizeof, c_buffer, cast, c_int32
from ctypes import c_char, c_char_p, c_void_p, c_uint32, c_double, c_int64, c_int, c_size_t, c_longlong
from common.python import session

LOGGER = log_utils.get_logger()


def _table_binary_op(x, y, q_field, op, need_send=False):
    return x.join(y, lambda a, b: op(a, b) % q_field, need_send=need_send)


def _table_scalar_op(x, d, op):
    return x.mapValues(lambda a: op(a, d))


def _table_dot_mod_func(it, q_field):
    ret = None
    for _, (x, y) in it:
        if ret is None:
            ret = np.tensordot(x, y, [[], []]) % q_field
        else:
            ret = (ret + np.tensordot(x, y, [[], []])) % q_field
    return ret


def _table_dot_func(it):
    return aclr.table_dot(it)


def table_dot(a_table, b_table):
    # source code
    return a_table.join(b_table, lambda x, y: [x, y]) \
        .applyPartitions(lambda it: _table_dot_func(it)) \
        .reduce(lambda x, y: x if y is None else y if x is None else x + y)


def table_dot_python(a_table, b_table):
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


def table_dot_gpu(a_table, b_table, partitions):
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
        return table_dot_python(a_table, b_table)

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


def table_dot_mod(a_table, b_table, q_field):
    return a_table.join(b_table, lambda x, y: [x, y]) \
        .applyPartitions(lambda it: _table_dot_mod_func(it, q_field)) \
        .reduce(lambda x, y: x if y is None else y if x is None else x + y)


class FixedPointTensor(TensorBase):
    """
    a table based tensor
    """
    __array_ufunc__ = None

    def __init__(self, value, q_field, endec, tensor_name: str = None):
        super().__init__(q_field, tensor_name)
        self.value = value
        self.endec = endec
        self.tensor_name = NamingService.get_instance().next() if tensor_name is None else tensor_name

    def dot(self, other: 'FixedPointTensor', target_name=None):
        spdz = self.get_spdz()
        if target_name is None:
            target_name = NamingService.get_instance().next()

        a, b, c = beaver_triplets(a_tensor=self.value, b_tensor=other.value, dot=table_dot,
                                  q_field=self.q_field, he_key_pair=(spdz.public_key, spdz.private_key),
                                  communicator=spdz.communicator, name=target_name)

        x_add_a = self.__add__(a, need_send=True).rescontruct(f"{target_name}_confuse_x")
        y_add_b = other.__add__(b, need_send=True).rescontruct(f"{target_name}_confuse_y")
        cross = c - table_dot_mod(a, y_add_b, self.q_field) - table_dot_mod(x_add_a, b, self.q_field)
        if spdz.party_idx == 0:
            cross += table_dot_mod(x_add_a, y_add_b, self.q_field)
        cross = cross % self.q_field
        cross = self.endec.truncate(cross, self.get_spdz().party_idx)
        share = fixedpoint_numpy.FixedPointTensor(cross, self.q_field, self.endec, target_name)
        return share

    @classmethod
    def from_source(cls, tensor_name, source, **kwargs):
        spdz = cls.get_spdz()
        if 'encoder' in kwargs:
            encoder = kwargs['encoder']
        else:
            base = kwargs['base'] if 'base' in kwargs else 10
            frac = kwargs['frac'] if 'frac' in kwargs else 6
            q_field = kwargs['q_field'] if 'q_field' in kwargs else spdz.q_field
            encoder = fixedpoint_numpy.FixedPointEndec(q_field, base, frac)
        if is_table(source):
            source = encoder.encode(source)
            _pre = urand_tensor(spdz.q_field, source, use_mix=spdz.use_mix_rand, need_send=True)
            LOGGER.info(f"send_share:{_pre.count()},source:{source.count()}")
            spdz.communicator.remote_share(share=_pre, tensor_name=tensor_name, party=spdz.other_parties[0])
            for _party in spdz.other_parties[1:]:
                r = urand_tensor(spdz.q_field, source, use_mix=spdz.use_mix_rand)
                spdz.communicator.remote_share(
                    share=_table_binary_op(r, _pre, spdz.q_field, operator.sub, need_send=True),
                    tensor_name=tensor_name, party=_party)
                _pre = r
            share = _table_binary_op(source, _pre, spdz.q_field, operator.sub)
        elif isinstance(source, Member):
            share = spdz.communicator.get_share(tensor_name=tensor_name, party=source)[0]
            LOGGER.info(f"share:{share.count()}")
        else:
            raise ValueError(f"type={type(source)}")
        return FixedPointTensor(share, spdz.q_field, encoder, tensor_name)

    def get(self, tensor_name=None):
        return self.rescontruct(tensor_name)

    def rescontruct(self, tensor_name=None):
        from kernel.security.protol.spdz import SPDZ
        spdz = SPDZ.get_instance()
        share_val = self.value
        name = tensor_name or self.tensor_name

        if name is None:
            raise ValueError("name not specified")

        # remote share to other parties
        spdz.communicator.broadcast_rescontruct_share(share_val, name)

        # get shares from other parties
        for other_share in spdz.communicator.get_rescontruct_shares(name):
            share_val = _table_binary_op(share_val, other_share, self.q_field, operator.add)
        return share_val

    def __str__(self):
        return f"{self.tensor_name}: {self.value}"

    def __repr__(self):
        return self.__str__()

    def as_name(self, tensor_name):
        return self._boxed(value=self.value, tensor_name=tensor_name)

    def __add__(self, other, need_send=False):
        if isinstance(other, FixedPointTensor):
            other = other.value
        z_value = _table_binary_op(self.value, other, self.q_field, operator.add, need_send=need_send)
        return self._boxed(z_value)

    def __sub__(self, other):
        z_value = _table_binary_op(self.value, other.value, self.q_field, operator.sub)
        return self._boxed(z_value)

    def __mul__(self, other):
        if not isinstance(other, (int, np.integer)):
            raise NotImplementedError("__mul__ support integer only")
        return self._boxed(_table_scalar_op(self.value, other, operator.mul))

    def __mod__(self, other):
        if not isinstance(other, (int, np.integer)):
            raise NotImplementedError("__mod__ support integer only")
        return self._boxed(_table_scalar_op(self.value, other, operator.mod))

    def _boxed(self, value, tensor_name=None):
        return FixedPointTensor(value=value, q_field=self.q_field, endec=self.endec, tensor_name=tensor_name)
