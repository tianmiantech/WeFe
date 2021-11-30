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

import numpy as np

from common.python.session import is_table
from common.python.utils.member import Member
from kernel.security.fixedpoint import FixedPointEndec
from kernel.security.protol.spdz.beaver_triples import beaver_triplets
from kernel.security.protol.spdz.tensor import fixedpoint_table
from kernel.security.protol.spdz.tensor.base import TensorBase
# from kernel.security.protol.spdz.tensor.fixedpoint_endec import FixedPointEndec
from kernel.security.protol.spdz.utils.random_utils import urand_tensor


class FixedPointTensor(TensorBase):
    __array_ufunc__ = None

    def __init__(self, value, q_field, endec, tensor_name: str = None):
        super().__init__(q_field, tensor_name)
        self.endec = endec
        self.value = value

    @property
    def shape(self):
        return self.value.shape

    def dot(self, other, target_name=None):
        return self.einsum(other, "ij,ik->jk", target_name)

    @classmethod
    def from_source(cls, tensor_name, source, **kwargs):
        spdz = cls.get_spdz()
        q_field = kwargs['q_field'] if 'q_field' in kwargs else spdz.q_field
        if 'encoder' in kwargs:
            encoder = kwargs['encoder']
        else:
            base = kwargs['base'] if 'base' in kwargs else 10
            frac = kwargs['frac'] if 'frac' in kwargs else 4
            encoder = FixedPointEndec(q_field, base, frac)
        if isinstance(source, np.ndarray):
            source = encoder.encode(source)
            _pre = urand_tensor(q_field, source, need_send=True)
            spdz.communicator.remote_share(share=_pre, tensor_name=tensor_name, party=spdz.other_parties[0])
            for _party in spdz.other_parties[1:]:
                r = urand_tensor(q_field, source)
                spdz.communicator.remote_share(share=(r - _pre) % q_field, tensor_name=tensor_name, party=_party)
                _pre = r
            return (source - _pre) % q_field
        elif isinstance(source, Member):
            share = spdz.communicator.get_share(tensor_name=tensor_name, party=source)[0]
        else:
            raise ValueError(f"type={type(source)}")
        return FixedPointTensor(share, spdz.q_field, encoder, tensor_name)

    def einsum(self, other: 'FixedPointTensor', einsum_expr, target_name=None):
        spdz = self.get_spdz()
        target_name = target_name or spdz.name_service.next()

        def _dot_func(_x, _y):
            return np.einsum(einsum_expr, _x, _y, optimize=True)

        a, b, c = beaver_triplets(a_tensor=self.value, b_tensor=other.value, dot=_dot_func,
                                  q_field=self.q_field, he_key_pair=(spdz.public_key, spdz.private_key),
                                  communicator=spdz.communicator, name=target_name)

        x_add_a = self._raw_add(a).rescontruct(f"{target_name}_confuse_x")
        y_add_b = other._raw_add(b).rescontruct(f"{target_name}_confuse_y")
        cross = c - _dot_func(a, y_add_b) - _dot_func(x_add_a, b)
        if spdz.party_idx == 0:
            cross += _dot_func(x_add_a, y_add_b)
        cross = cross % self.q_field
        cross = self.endec.truncate(cross, self.get_spdz().party_idx)
        share = self._boxed(cross)
        return share

    def get(self, tensor_name=None):
        """
        rescontruct and decode
        """
        return self.endec.decode(self.rescontruct(tensor_name))

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
            share_val += other_share
            share_val %= self.q_field
        return share_val

    def _boxed(self, value, tensor_name=None):
        return FixedPointTensor(value=value, q_field=self.q_field, endec=self.endec, tensor_name=tensor_name)

    def __str__(self):
        return f"{self.tensor_name}: {self.value}"

    def __repr__(self):
        return self.__str__()

    def _raw_add(self, other):
        z_value = (self.value + other) % self.q_field
        return self._boxed(z_value)

    def _raw_sub(self, other):
        z_value = (self.value - other) % self.q_field
        return self._boxed(z_value)

    def __add__(self, other):
        if isinstance(other, FixedPointTensor):
            return self._raw_add(other.value)
        z_value = (self.value + self.endec.encode(other / 2)) % self.q_field
        return self._boxed(z_value)

    def __radd__(self, other):
        z_value = (self.endec.encode(other / 2) + self.value) % self.q_field
        return self._boxed(z_value)

    def __sub__(self, other):
        if isinstance(other, FixedPointTensor):
            return self._raw_sub(other.value)
        z_value = (self.value - self.endec.encode(other / 2)) % self.q_field
        return self._boxed(z_value)

    def __rsub__(self, other):
        z_value = (self.endec.encode(other / 2) - self.value) % self.q_field
        return self._boxed(z_value)

    def __mul__(self, other):
        if not isinstance(other, (int, np.integer)):
            raise NotImplementedError("__mul__ support integer only")
        return self._boxed(self.value * other)

    def __rmul__(self, other):
        if not isinstance(other, (int, np.integer)):
            raise NotImplementedError("__rmul__ support integer only")
        return self._boxed(self.value * other)

    def __matmul__(self, other):
        return self.einsum(other, "ij,jk->ik")


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

        if isinstance(other, (FixedPointTensor, fixedpoint_table.FixedPointTensor)):
            other = other.value
        if isinstance(other, np.ndarray):
            ret = _vec_dot(self.value, other)
            return self._boxed(ret, target_name)
        elif is_table(other):
            f = functools.partial(_vec_dot, self.value)
            ret = other.mapValues(f)
            return fixedpoint_table.PaillierFixedPointTensor(value=ret,
                                                             tensor_name=target_name,
                                                             cipher=self.cipher)
        else:
            raise ValueError(f"type={type(other)}")

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

    def _raw_add(self, other):
        z_value = (self.value + other)
        return self._boxed(z_value)

    def _raw_sub(self, other):
        z_value = (self.value - other)
        return self._boxed(z_value)

    def __add__(self, other):
        if isinstance(other, (PaillierFixedPointTensor, FixedPointTensor)):
            return self._raw_add(other.value)
        else:
            return self._raw_add(other)

    def __radd__(self, other):
        return self.__add__(other)

    def __sub__(self, other):
        if isinstance(other, (PaillierFixedPointTensor, FixedPointTensor)):
            return self._raw_sub(other.value)
        else:
            return self._raw_sub(other)

    def __rsub__(self, other):
        if isinstance(other, (PaillierFixedPointTensor, FixedPointTensor)):
            z_value = other.value - self.value
        else:
            z_value = other - self.value
        return self._boxed(z_value)

    def __mul__(self, other):
        if isinstance(other, PaillierFixedPointTensor):
            raise NotImplementedError("__mul__ not support PaillierFixedPointTensor")
        elif isinstance(other, FixedPointTensor):
            return self._boxed(self.value * other.value)
        else:
            return self._boxed(self.value * other)

    def __rmul__(self, other):
        self.__mul__(other)

    def _boxed(self, value, tensor_name=None):
        return PaillierFixedPointTensor(value=value,
                                        tensor_name=tensor_name,
                                        cipher=self.cipher)

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

        if isinstance(source, np.ndarray):
            _pre = urand_tensor(q_field, source)

            share = _pre

            spdz.communicator.remote_share(share=source - encoder.decode(_pre),
                                           tensor_name=tensor_name,
                                           party=spdz.other_parties[-1])

            return FixedPointTensor(value=share,
                                    q_field=q_field,
                                    endec=encoder,
                                    tensor_name=tensor_name)

        elif isinstance(source, Member):
            share = spdz.communicator.get_share(tensor_name=tensor_name, party=source)[0]

            is_cipher_source = kwargs['is_cipher_source'] if 'is_cipher_source' in kwargs else True
            if is_cipher_source:
                cipher = kwargs['cipher']
                share = cipher.recursive_decrypt(share)
                share = encoder.encode(share)
            return FixedPointTensor(value=share,
                                    q_field=q_field,
                                    endec=encoder,
                                    tensor_name=tensor_name)
        else:
            raise ValueError(f"type={type(source)}")
