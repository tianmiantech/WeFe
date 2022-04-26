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

# Copyright 2019 The FATE Authors. All Rights Reserved.
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
import functools
import operator

import numpy as np
import ctypes
from common.python.calculation.acceleration.utils.aclr_utils import check_aclr_support
from common.python.calculation.acceleration import aclr

from common.python.calculation.acceleration import aclr
from common.python.session import is_table
from common.python.utils import log_utils
from common.python.utils.member import Member
from kernel.security.fixedpoint import FixedPointEndec
from kernel.security.protol.spdz.beaver_triples import beaver_triplets
from kernel.security.protol.spdz.tensor import fixedpoint_numpy
from kernel.security.protol.spdz.tensor.base import TensorBase
from kernel.security.protol.spdz.utils import NamingService
from kernel.security.protol.spdz.utils.random_utils import urand_tensor
from common.python.calculation.acceleration.operator import cal
from kernel.security.paillier import PaillierEncryptedNumber
from common.python.common import consts
from common.python.utils import conf_utils
# from kernel.security.protol.spdz.utils.random_utils import urand_tensor
from kernel.security.protol.spdz.utils import urand_tensor

LOGGER = log_utils.get_logger()


def get_gpu_batch(algo_type='paillier_table_dot'):
    return int(aclr.gpu_device_info('max_array_size', algo_type) / conf_utils.get_comm_config(
        consts.COMM_CONF_KEY_GPU_INSTANCE, 1))

def _table_binary_op(x, y, op):
    return x.join(y, lambda a, b: op(a, b))


def _table_binary_mod_op(x, y, q_field, op):
    return x.join(y, lambda a, b: op(a, b) % q_field)


def _table_scalar_op(x, d, op):
    return x.mapValues(lambda a: op(a, d))


def _table_scalar_mod_op(x, d, q_field, op):
    return x.mapValues(lambda a: op(a, d) % q_field)


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
    if check_aclr_support() and isinstance(a_table.take()[0][1][0], PaillierEncryptedNumber):
        new_table = list(a_table.join(b_table, lambda x, y: [x, y]).collect())

        if a_table.count() > 0:
            a_tables = [x[1][0] for x in new_table]
            b_tables = [x[1][1] for x in new_table]
            if type(a_tables[0][0]) == int:
                # 此处如果用type== int 进行转换，程序会自动转为 int64，后续的table_dot 会溢出，随后报错
                a_new_tables = np.array(a_tables)
                b_new_tables = np.array(b_tables)
            else:
                a_new_tables = np.array(a_tables).astype(type(a_tables[0][0]))
                b_new_tables = np.array(b_tables).astype(type(b_tables[0][0]))

            # 获取 GPU 可执行批量数
            gpu_table_dot_batch = get_gpu_batch()
            LOGGER.debug(f'gpu_table_dot_batch: {gpu_table_dot_batch}')
            # 分批调用，防止 GPU 显存不足
            table_batch_a = [a_new_tables[i:i + gpu_table_dot_batch] for i in
                             range(0, len(a_new_tables), gpu_table_dot_batch)]
            table_batch_b = [b_new_tables[i:i + gpu_table_dot_batch] for i in
                             range(0, len(b_new_tables), gpu_table_dot_batch)]
            rs = None

            for i in range(len(table_batch_a)):
                i_rs = aclr.table_dot_gpu(table_batch_a[i], table_batch_b[i])
                if rs is None:
                    rs = i_rs
                else:
                    # 调用 矩阵相加
                    row, col = rs.shape
                    arr1 = rs.flatten('A')
                    arr2 = i_rs.flatten('A')
                    rs = cal.gpu_paillier_array_pen_add_pen(arr1, arr2).reshape(row, col)
            return rs
        else:
            return []
    else:
        rs = a_table.join(b_table, lambda x, y: [x, y]) \
            .applyPartitions(lambda it: _table_dot_func(it)) \
            .reduce(lambda x, y: x if y is None else y if x is None else x + y)
        return rs


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

        x_add_a = (self + a).reconstruct(f"{target_name}_confuse_x")
        y_add_b = (self + b).reconstruct(f"{target_name}_confuse_y")
        cross = c - table_dot_mod(a, y_add_b, self.q_field) - table_dot_mod(x_add_a, b, self.q_field)
        if spdz.party_idx == 0:
            cross += table_dot_mod(x_add_a, y_add_b, self.q_field)
        cross = cross % self.q_field
        cross = self.endec.truncate(cross, self.get_spdz().party_idx)
        share = fixedpoint_numpy.FixedPointTensor(cross, self.q_field, self.endec, target_name)
        return share

    def dot_local(self, other, target_name=None):
        def _vec_dot(x, y, party_idx, q_field, endec):
            ret = np.dot(x, y) % q_field
            ret = endec.truncate(ret, party_idx)
            if not isinstance(ret, np.ndarray):
                ret = np.array([ret])
            return ret

        if isinstance(other, FixedPointTensor) or isinstance(other, fixedpoint_numpy.FixedPointTensor):
            other = other.value

        if isinstance(other, np.ndarray):
            party_idx = self.get_spdz().party_idx
            f = functools.partial(_vec_dot, y=other,
                                  party_idx=party_idx,
                                  q_field=self.q_field,
                                  endec=self.endec)
            ret = self.value.mapValues(f)
            return self._boxed(ret, target_name)

        elif is_table(other):
            ret = table_dot_mod(self.value, other, self.q_field).reshape((1, -1))[0]
            ret = self.endec.truncate(ret, self.get_spdz().party_idx)
            return fixedpoint_numpy.FixedPointTensor(ret,
                                                     self.q_field,
                                                     self.endec,
                                                     target_name)
        else:
            raise ValueError(f"type={type(other)}")

    def reduce(self, func, **kwargs):
        ret = self.value.reduce(func)
        return fixedpoint_numpy.FixedPointTensor(ret,
                                                 self.q_field,
                                                 self.endec
                                                 )

    @property
    def shape(self):
        return self.value.count(), len(self.value.first()[1])

    @classmethod
    def from_source(cls, tensor_name, source, **kwargs):
        spdz = cls.get_spdz()
        q_field = kwargs['q_field'] if 'q_field' in kwargs else spdz.q_field
        if 'encoder' in kwargs:
            encoder = kwargs['encoder']
        else:
            base = kwargs['base'] if 'base' in kwargs else 10
            frac = kwargs['frac'] if 'frac' in kwargs else 6
            q_field = kwargs['q_field'] if 'q_field' in kwargs else spdz.q_field
            encoder = FixedPointEndec(field=q_field, base=base, precision_fractional=frac)
        max_rand = kwargs['max_rand'] if 'max_rand' in kwargs else q_field
        if is_table(source):
            source = encoder.encode(source)
            _pre = urand_tensor(max_rand, source, use_mix=spdz.use_mix_rand)
            LOGGER.info(f"send_share:{_pre.count()},source:{source.count()}")
            spdz.communicator.remote_share(share=_pre, tensor_name=tensor_name, party=spdz.other_parties[0])
            for _party in spdz.other_parties[1:]:
                r = urand_tensor(max_rand, source, use_mix=spdz.use_mix_rand)
                spdz.communicator.remote_share(share=_table_binary_mod_op(r, _pre, q_field, operator.sub),
                                               tensor_name=tensor_name, party=_party)
                _pre = r
            share = _table_binary_mod_op(source, _pre, q_field, operator.sub)
        elif isinstance(source, Member):
            share = spdz.communicator.get_share(tensor_name=tensor_name, party=source)[0]
            LOGGER.info(f"share:{share.count()}")
        else:
            raise ValueError(f"type={type(source)}")
        return FixedPointTensor(share, q_field, encoder, tensor_name)

    def get(self, tensor_name=None, broadcast=True):
        return self.endec.decode(self.reconstruct(tensor_name, broadcast))

    def reconstruct(self, tensor_name=None, broadcast=True):
        from kernel.security.protol.spdz import SPDZ
        spdz = SPDZ.get_instance()
        share_val = self.value
        name = tensor_name or self.tensor_name

        if name is None:
            raise ValueError("name not specified")

        # remote share to other parties
        if broadcast:
            spdz.communicator.broadcast_rescontruct_share(share_val, name)

        # get shares from other parties
        for other_share in spdz.communicator.get_rescontruct_shares(name):
            share_val = _table_binary_op(share_val, other_share, self.q_field, operator.add)

        return share_val

    def broadcast_reconstruct_share(self, tensor_name=None):
        from kernel.security.protol.spdz import SPDZ
        spdz = SPDZ.get_instance()
        share_val = self.value
        name = tensor_name or self.tensor_name
        if name is None:
            raise ValueError("name not specified")
        # remote share to other parties
        spdz.communicator.broadcast_rescontruct_share(share_val, name)

        return share_val

    def __str__(self):
        return f"tensor_name={self.tensor_name}, value={self.value}"

    def __repr__(self):
        return self.__str__()

    def as_name(self, tensor_name):
        return self._boxed(value=self.value, tensor_name=tensor_name)

    def __add__(self, other):
        if isinstance(other, PaillierFixedPointTensor):
            z_value = _table_binary_op(self.value, other.value, operator.add)
            return PaillierFixedPointTensor(z_value)
        elif isinstance(other, FixedPointTensor):
            z_value = _table_binary_mod_op(self.value, other.value, self.q_field, operator.add)
        elif is_table(other):
            z_value = _table_binary_mod_op(self.value, other, self.q_field, operator.add)
        else:
            z_value = _table_scalar_mod_op(self.value, other, self.q_field, operator.add)

        return self._boxed(z_value)

    def __radd__(self, other):
        return self.__add__(other)

    def __sub__(self, other):
        if isinstance(other, PaillierFixedPointTensor):
            z_value = _table_binary_op(self.value, other.value, operator.sub)
            return PaillierFixedPointTensor(z_value)
        elif isinstance(other, FixedPointTensor):
            z_value = _table_binary_mod_op(self.value, other.value, self.q_field, operator.sub)
        elif is_table(other):
            z_value = _table_binary_mod_op(self.value, other, self.q_field, operator.sub)
        else:
            z_value = _table_scalar_mod_op(self.value, other, self.q_field, operator.sub)

        return self._boxed(z_value)

    def __rsub__(self, other):
        if isinstance(other, (PaillierFixedPointTensor, FixedPointTensor)):
            return other - self
        elif is_table(other):
            z_value = _table_binary_mod_op(other, self.value, self.q_field, operator.sub)
        else:
            z_value = _table_scalar_mod_op(self.value, other, self.q_field, -1 * operator.sub)

        return self._boxed(z_value)

    def __mul__(self, other):
        if isinstance(other, FixedPointTensor):
            z_value = _table_binary_mod_op(self.value, other.value, self.q_field, operator.mul)
        elif isinstance(other, PaillierFixedPointTensor):
            z_value = _table_binary_op(self.value, other.value, operator.mul)
        else:
            z_value = _table_scalar_mod_op(self.value, other, self.q_field, operator.mul)
        z_value = self.endec.truncate(z_value, self.get_spdz().party_idx)
        return self._boxed(z_value)

    def __rmul__(self, other):
        return self.__mul__(other)

    def __mod__(self, other):
        if not isinstance(other, (int, np.integer)):
            raise NotImplementedError("__mod__ support integer only")
        return self._boxed(_table_scalar_op(self.value, other, operator.mod))

    def _boxed(self, value, tensor_name=None):
        return FixedPointTensor(value=value, q_field=self.q_field, endec=self.endec, tensor_name=tensor_name)


class PaillierFixedPointTensor(TensorBase):
    __array_ufunc__ = None

    def __init__(self, value, tensor_name: str = None, cipher=None):
        super().__init__(q_field=None, tensor_name=tensor_name)
        self.value = value
        self.cipher = cipher

    def dot(self, other, target_name=None):
        def _vec_dot(x, y):
            ret = np.dot(x, y)
            if not isinstance(ret, np.ndarray):
                ret = np.array([ret])
            return ret

        if isinstance(other, (FixedPointTensor, fixedpoint_numpy.FixedPointTensor)):
            other = other.value

        if isinstance(other, np.ndarray):
            ret = self.value.mapValues(lambda x: _vec_dot(x, other))
            return self._boxed(ret, target_name)

        elif is_table(other):
            ret = table_dot(self.value, other).reshape((1, -1))[0]
            return fixedpoint_numpy.PaillierFixedPointTensor(ret, target_name)
        else:
            raise ValueError(f"type={type(other)}")

    def reduce(self, func, **kwargs):
        ret = self.value.reduce(func)
        return fixedpoint_numpy.PaillierFixedPointTensor(ret)

    def __str__(self):
        return f"tensor_name={self.tensor_name}, value={self.value}"

    def __repr__(self):
        return self.__str__()

    def __add__(self, other):
        if isinstance(other, (PaillierFixedPointTensor, FixedPointTensor)):
            return self._boxed(_table_binary_op(self.value, other.value, operator.add))
        elif is_table(other):
            return self._boxed(_table_binary_op(self.value, other, operator.add))
        else:
            return self._boxed(_table_scalar_op(self.value, other, operator.add))

    def __radd__(self, other):
        return self.__add__(other)

    def __sub__(self, other):
        if isinstance(other, (PaillierFixedPointTensor, FixedPointTensor)):
            return self._boxed(_table_binary_op(self.value, other.value, operator.sub))
        elif is_table(other):
            return self._boxed(_table_binary_op(self.value, other, operator.sub))
        else:
            return self._boxed(_table_scalar_op(self.value, other, operator.sub))

    def __rsub__(self, other):
        if isinstance(other, (PaillierFixedPointTensor, FixedPointTensor)):
            return self._boxed(_table_binary_op(other.value, self.value, operator.sub))
        elif is_table(other):
            return self._boxed(_table_binary_op(other, self.value, operator.sub))
        else:
            return self._boxed(_table_scalar_op(self.value, other, -1 * operator.sub))

    def __mul__(self, other):
        if isinstance(other, FixedPointTensor):
            z_value = _table_binary_op(self.value, other.value, operator.mul)
        elif is_table(other):
            z_value = _table_binary_op(self.value, other, operator.mul)
        else:
            z_value = _table_scalar_op(self.value, other, operator.mul)
        return self._boxed(z_value)

    def __rmul__(self, other):
        return self.__mul__(other)

    def _boxed(self, value, tensor_name=None):
        return PaillierFixedPointTensor(value=value, tensor_name=tensor_name)

    @classmethod
    def from_source(cls, tensor_name, source, **kwargs):
        spdz = cls.get_spdz()
        q_field = kwargs['q_field'] if 'q_field' in kwargs else spdz.q_field

        if 'encoder' in kwargs:
            encoder = kwargs['encoder']
        else:
            base = kwargs['base'] if 'base' in kwargs else 10
            frac = kwargs['frac'] if 'frac' in kwargs else 4
            encoder = FixedPointEndec(field=q_field, base=base, precision_fractional=frac)
        max_rand = kwargs['max_rand'] if 'max_rand' in kwargs else q_field
        if is_table(source):
            _pre = urand_tensor(max_rand, source, use_mix=spdz.use_mix_rand)

            share = _pre

            spdz.communicator.remote_share(share=_table_binary_op(source, encoder.decode(_pre), operator.sub),
                                           tensor_name=tensor_name, party=spdz.other_parties[-1])
            return FixedPointTensor(value=share,
                                    q_field=q_field,
                                    endec=encoder,
                                    tensor_name=tensor_name)

        elif isinstance(source, Member):
            share = spdz.communicator.get_share(tensor_name=tensor_name, party=source)[0]
            is_cipher_source = kwargs['is_cipher_source'] if 'is_cipher_source' in kwargs else True
            if is_cipher_source:
                cipher = kwargs.get("cipher")
                if cipher is None:
                    raise ValueError("Cipher is not provided")

                share = cipher.distribute_decrypt(share)
                share = encoder.encode(share)
            return FixedPointTensor(value=share,
                                    q_field=q_field,
                                    endec=encoder,
                                    tensor_name=tensor_name)
        else:
            raise ValueError(f"type={type(source)}")
