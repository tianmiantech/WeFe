import operator

import numpy as np
import ctypes
from common.python.calculation.acceleration.utils.aclr_utils import check_aclr_support
from common.python.calculation.acceleration import aclr
from common.python.session import is_table
from common.python.utils import log_utils
from common.python.utils.member import Member
from kernel.security.protol.spdz.beaver_triples import beaver_triplets
from kernel.security.protol.spdz.tensor import fixedpoint_numpy
from kernel.security.protol.spdz.tensor.base import TensorBase
from kernel.security.protol.spdz.utils import NamingService
from kernel.security.protol.spdz.utils.random_utils import urand_tensor

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
    if check_aclr_support():
        partitions = a_table.get_partitions()
        if a_table.count() > 0:
            tables = [x[1] for x in a_table.collect()]
            new_tables = np.array(tables, dtype=type(tables[0][0]))
            return aclr.table_dot_gpu(new_tables, new_tables, partitions)
        else:
            return []
    else:
        return a_table.join(b_table, lambda x, y: [x, y]) \
            .applyPartitions(lambda it: _table_dot_func(it)) \
            .reduce(lambda x, y: x if y is None else y if x is None else x + y)


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
