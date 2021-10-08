import numpy as np

from common.python.session import is_table
from kernel.security.protol.spdz.communicator import Communicator
from kernel.security.protol.spdz.utils.random_utils import rand_tensor, urand_tensor


def encrypt_tensor(tensor, public_key, need_send=False):
    encrypted_zero = public_key.encrypt(0)
    if isinstance(tensor, np.ndarray):
        return np.vectorize(lambda e: encrypted_zero + e)(tensor)
    elif is_table(tensor):
        return tensor.mapValues(lambda x: np.vectorize(lambda e: encrypted_zero + e)(x), need_send=need_send)
    else:
        raise NotImplementedError(f"type={type(tensor)}")


def decrypt_tensor(tensor, private_key, otypes):
    if isinstance(tensor, np.ndarray):
        return np.vectorize(private_key.decrypt, otypes)(tensor)
    elif is_table(tensor):
        return tensor.mapValues(lambda x: np.vectorize(private_key.decrypt, otypes)(x))
    else:
        raise NotImplementedError(f"type={type(tensor)}")


def beaver_triplets(a_tensor, b_tensor, dot, q_field, he_key_pair, communicator: Communicator, name):
    """
    generate beaver triplets
    Parameters
    ----------
    a_tensor: matrix
    b_tensor: matrix
    dot: a and b calculate method
    q_field:  prime finite fields
    he_key_pair: secret key
    communicator: communicator object
    name: tag

    Returns
    -------
        random matrix a , b, c = a * b
    """
    public_key, private_key = he_key_pair
    a = rand_tensor(q_field, a_tensor)
    b = rand_tensor(q_field, b_tensor)

    def _cross(self_index, other_index):
        _c = dot(a, b)
        encrypted_a = encrypt_tensor(a, public_key, need_send=True)
        communicator.remote_encrypted_tensor(encrypted=encrypted_a, tag=f"{name}_a_{self_index}")
        r = urand_tensor(q_field, _c)
        _p, (ea,) = communicator.get_encrypted_tensors(tag=f"{name}_a_{other_index}")
        eab = dot(ea, b)
        eab += r
        _c -= r
        communicator.remote_encrypted_cross_tensor(encrypted=eab,
                                                   parties=_p,
                                                   tag=f"{name}_cross_a_{other_index}_b_{self_index}")
        crosses = communicator.get_encrypted_cross_tensors(tag=f"{name}_cross_a_{self_index}_b_{other_index}")
        for eab in crosses:
            _c += decrypt_tensor(eab, private_key, [object])

        return _c

    c = _cross(communicator.party_idx, 1 - communicator.party_idx)

    return a, b, c % q_field
