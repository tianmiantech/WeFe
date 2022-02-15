from kernel.security.paillier import PaillierKeypair
from kernel.security.protol.spdz.communicator import Communicator
from kernel.security.protol.spdz.utils import NamingService
from kernel.security.protol.spdz.utils import naming


class SPDZ(object):
    __instance = None

    @classmethod
    def get_instance(cls) -> 'SPDZ':
        return cls.__instance

    @classmethod
    def set_instance(cls, instance):
        prev = cls.__instance
        cls.__instance = instance
        return prev

    @classmethod
    def has_instance(cls):
        return cls.__instance is not None

    def __init__(self, name="ss", q_field=2 << 60, local_party=None, all_parties=None, use_mix_rand=False):
        self.name_service = naming.NamingService(name)
        self._prev_name_service = None
        self._pre_instance = None

        self.communicator = Communicator(local_party, all_parties)

        self.party_idx = self.communicator.party_idx
        self.other_parties = self.communicator.other_parties
        if len(self.other_parties) > 1:
            raise EnvironmentError("support 2-party secret share only")
        self.public_key, self.private_key = PaillierKeypair.generate_keypair(1024)
        self.q_field = q_field
        self.use_mix_rand = use_mix_rand

    def __enter__(self):
        self._prev_name_service = NamingService.set_instance(self.name_service)
        self._pre_instance = self.set_instance(self)
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        NamingService.set_instance(self._pre_instance)

    def __reduce__(self):
        raise PermissionError("it's unsafe to transfer this")

    def partial_rescontruct(self):
        # todo: partial parties gets rescontructed tensor
        pass

    @classmethod
    def dot(cls, left, right, target_name=None):
        return left.dot(right, target_name)
