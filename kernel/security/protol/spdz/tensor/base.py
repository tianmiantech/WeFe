import abc

from kernel.security.protol.spdz.utils import NamingService


class TensorBase(object):
    __array_ufunc__ = None

    def __init__(self, q_field, tensor_name: str = None):
        self.q_field = q_field
        self.tensor_name = NamingService.get_instance().next() if tensor_name is None else tensor_name

    @classmethod
    def get_spdz(cls):
        from kernel.security.protol.spdz import SPDZ
        return SPDZ.get_instance()

    @abc.abstractmethod
    def dot(self, other, target_name=None):
        pass
