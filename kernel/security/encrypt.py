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
import hashlib
import time
from collections import Iterable

import numpy as np
from Cryptodome import Random
from Cryptodome.PublicKey import RSA

from kernel.base.instance import Instance
from kernel.security import gmpy_math
from kernel.security.affine import AffineCipher
from kernel.security.iterative_affine import IterativeAffineCipher
from kernel.security.paillier import PaillierEncryptedNumber
from kernel.security.paillier import PaillierKeypair
from kernel.security.random import RandomPads
from kernel.security.paillier import PaillierEncryptedNumber
from common.python import session
from ctypes import cdll, sizeof, c_buffer, cast, c_int32
from ctypes import c_char, c_char_p, c_void_p, c_uint32, c_double, c_int64, c_int, c_size_t, c_longlong
import ctypes
import datetime as dt

_TORCH_VALID = False
try:
    import torch

    _TORCH_VALID = True
except ImportError:
    pass


class Encrypt(object):
    def __init__(self):
        self.public_key = None
        self.privacy_key = None

    def generate_key(self, n_length=0):
        pass

    def set_public_key(self, public_key):
        pass

    def get_public_key(self):
        pass

    def set_privacy_key(self, privacy_key):
        pass

    def get_privacy_key(self):
        pass

    def encrypt(self, value):
        pass

    def decrypt(self, value):
        pass

    def raw_encrypt(self, value):
        pass

    def raw_decrypt(self, value):
        pass

    def encrypt_list(self, values):
        result = [self.encrypt(msg) for msg in values]
        return result

    def decrypt_list(self, values):
        result = [self.decrypt(msg) for msg in values]
        return result

    def distribute_decrypt(self, X):
        decrypt_table = X.mapValues(lambda x: self.recursive_decrypt(x))
        return decrypt_table

    def distribute_encrypt(self, X):
        encrypt_table = X.mapValues(lambda x: self.recursive_encrypt(x))
        return encrypt_table

    def _recursive_func(self, obj, func):
        if isinstance(obj, np.ndarray):
            if len(obj.shape) == 1:
                return np.reshape([func(val) for val in obj], obj.shape)
            else:
                return np.reshape(
                    [self._recursive_func(o, func) for o in obj], obj.shape
                )
        elif isinstance(obj, Iterable):
            return type(obj)(
                self._recursive_func(o, func) if isinstance(o, Iterable) else func(o)
                for o in obj
            )
        else:
            return func(obj)

    def recursive_encrypt(self, X):
        return self._recursive_func(X, self.encrypt)

    def recursive_decrypt(self, X):
        return self._recursive_func(X, self.decrypt)

    def recursive_raw_encrypt(self, X):
        return self._recursive_func(X, self.raw_encrypt)

    def recursive_raw_decrypt(self, X):
        return self._recursive_func(X, self.raw_decrypt)


class RsaEncrypt(Encrypt):
    def __init__(self):
        super(RsaEncrypt, self).__init__()
        self.e = None
        self.d = None
        self.n = None
        self.p = None
        self.q = None

    def generate_key(self, rsa_bit=1024):
        random_generator = Random.new().read
        rsa = RSA.generate(rsa_bit, random_generator)
        self.e = rsa.e
        self.d = rsa.d
        self.n = rsa.n
        self.p = rsa.p
        self.q = rsa.q

    def get_key_pair(self):
        return self.e, self.d, self.n, self.p, self.q

    def set_public_key(self, public_key):
        self.e = public_key["e"]
        self.n = public_key["n"]

    def get_public_key(self):
        return self.e, self.n

    def set_privacy_key(self, privacy_key):
        self.d = privacy_key["d"]
        self.n = privacy_key["n"]

    def get_privacy_key(self):
        return self.d, self.n

    def encrypt(self, value):
        if self.e is not None and self.n is not None and self.p is not None and self.q is not None:
            cp, cq = gmpy_math.crt_coefficient(self.p, self.q)
            return gmpy_math.powmod_crt(value, self.e, self.n, self.p, self.q, cp, cq)
        if self.e is not None and self.n is not None:
            return gmpy_math.powmod(value, self.e, self.n)
        else:
            return None

    def decrypt(self, value):
        if self.d is not None and self.n is not None:
            return gmpy_math.powmod(value, self.d, self.n)
        else:
            return None


class PaillierEncrypt(Encrypt):
    def __init__(self):
        super(PaillierEncrypt, self).__init__()

    def generate_key(self, n_length=1024):
        self.public_key, self.privacy_key = PaillierKeypair.generate_keypair(
            n_length=n_length
        )

    def get_key_pair(self):
        return self.public_key, self.privacy_key

    def set_public_key(self, public_key):
        self.public_key = public_key

    def get_public_key(self):
        return self.public_key

    def set_privacy_key(self, privacy_key):
        self.privacy_key = privacy_key

    def get_privacy_key(self):
        return self.privacy_key

    def encrypt(self, value):
        if self.public_key is not None:
            return self.public_key.encrypt(value)
        else:
            return None

    def encrypt_list(self, values):

        if self.public_key is not None:
            partitions = values.get_partitions()
            datas = list(values.collect())
            if len(datas) > 0:
                _datas = [x[1][0] if isinstance(x[1], list) else x[1] for x in datas]
                values = np.array(_datas, dtype=type(_datas[0]))
            # start_time = time.time()
            result = self.public_key.encrypt_gpu(values)
            # print(f'gpu encrypt... 耗时：{time.time() - start_time}')

            return session.parallelize(result.tolist(), partition=partitions)
        else:
            return None

    def decrypt(self, value):
        if self.privacy_key is not None:
            return self.privacy_key.decrypt(value)
        else:
            return None

    def gpu_raw_encrypt(self, values, exponent=0):

        if self.public_key is not None:
            partitions = values.get_partitions()
            datas = list(values.collect())
            key_list = [x[0] for x in datas]
            if len(datas) > 0:
                datas = [x[1][0] if isinstance(x[1], list) else x[1] for x in datas]
                # values = np.array(_datas, dtype=type(_datas[0]))
            start_time = time.time()
            encrypt_list = self.public_key.gpu_paillier_raw_encrypt(datas)

            result = []
            for i in range(len(key_list)):
                result.append((key_list[i], [encrypt_list[i]]))
            print(f'gpu raw encrypt... 耗时：{time.time() - start_time}')

            return session.parallelize(result, include_key=True, partition=partitions)
        else:
            return None

    def raw_encrypt(self, plaintext, exponent=0):
        cipher_int = self.public_key.raw_encrypt(plaintext)
        paillier_num = PaillierEncryptedNumber(public_key=self.public_key, ciphertext=cipher_int, exponent=exponent)
        return paillier_num

    def raw_decrypt(self, ciphertext):
        return self.privacy_key.raw_decrypt(ciphertext.ciphertext())

    def recursive_raw_encrypt(self, X, exponent=0):
        raw_en_func = functools.partial(self.raw_encrypt, exponent=exponent)
        return self._recursive_func(X, raw_en_func)


class FakeEncrypt(Encrypt):

    def __init__(self):
        pass

    def encrypt(self, value):
        return value

    def decrypt(self, value):
        return value

    def raw_decrypt(self, value):
        return value

    def raw_encrypt(self, value):
        return value


class SymmetricEncrypt(Encrypt):

    def __init__(self):
        self.key = None

    def encrypt(self, plaintext):
        pass


class AffineEncrypt(SymmetricEncrypt):
    def __init__(self):
        super(AffineEncrypt, self).__init__()

    def generate_key(self, key_size=1024):
        self.key = AffineCipher.generate_keypair(key_size=key_size)

    def encrypt(self, plaintext):
        if self.key is not None:
            return self.key.encrypt(plaintext)
        else:
            return None

    def decrypt(self, ciphertext):
        if self.key is not None:
            return self.key.decrypt(ciphertext)
        else:
            return None


class PadsCipher(Encrypt):
    def __init__(self):
        super().__init__()
        self._uuid = None
        self._rands = None
        self._amplify_factor = 1

    def set_self_uuid(self, uuid):
        self._uuid = uuid

    def set_amplify_factor(self, factor):
        self._amplify_factor = factor

    def set_exchanged_keys(self, keys):
        self._seeds = {
            uid: v & 0xFFFFFFFF for uid, v in keys.items() if uid != self._uuid
        }
        self._rands = {
            uid: RandomPads(v & 0xFFFFFFFF)
            for uid, v in keys.items()
            if uid != self._uuid
        }

    def encrypt(self, value):
        if isinstance(value, np.ndarray):
            ret = value
            for uid, rand in self._rands.items():
                if uid > self._uuid:
                    ret = rand.add_rand_pads(ret, 1.0 * self._amplify_factor)
                else:
                    ret = rand.add_rand_pads(ret, -1.0 * self._amplify_factor)
            return ret

        if _TORCH_VALID and isinstance(value, torch.Tensor):
            ret = value.numpy()
            for uid, rand in self._rands.items():
                if uid > self._uuid:
                    ret = rand.add_rand_pads(ret, 1.0 * self._amplify_factor)
                else:
                    ret = rand.add_rand_pads(ret, -1.0 * self._amplify_factor)
            return torch.Tensor(ret)

        ret = value
        for uid, rand in self._rands.items():
            if uid > self._uuid:
                ret += rand.rand(1)[0] * self._amplify_factor
            else:
                ret -= rand.rand(1)[0] * self._amplify_factor
        return ret

    def encrypt_table(self, table):
        def _pad(key, value, seeds, amplify_factor):
            has_key = int(hashlib.md5(f"{key}".encode("utf-8")).hexdigest(), 16)
            # LOGGER.debug(f"hash_key: {has_key}")
            cur_seeds = {uid: has_key + seed for uid, seed in seeds.items()}
            # LOGGER.debug(f"cur_seeds: {cur_seeds}")
            rands = {uid: RandomPads(v & 0xFFFFFFFF) for uid, v in cur_seeds.items()}

            if isinstance(value, np.ndarray):
                ret = value
                for uid, rand in rands.items():
                    if uid > self._uuid:
                        ret = rand.add_rand_pads(ret, 1.0 * amplify_factor)
                    else:
                        ret = rand.add_rand_pads(ret, -1.0 * amplify_factor)
                return key, ret
            elif isinstance(value, Instance):
                ret = value.features
                for uid, rand in rands.items():
                    if uid > self._uuid:
                        ret = rand.add_rand_pads(ret, 1.0 * amplify_factor)
                    else:
                        ret = rand.add_rand_pads(ret, -1.0 * amplify_factor)
                value.features = ret
                return key, value
            else:
                ret = value
                for uid, rand in rands.items():
                    if uid > self._uuid:
                        ret += rand.rand(1)[0] * self._amplify_factor
                    else:
                        ret -= rand.rand(1)[0] * self._amplify_factor
                return key, ret

        f = functools.partial(
            _pad, seeds=self._seeds, amplify_factor=self._amplify_factor
        )
        return table.map(f)

    def decrypt(self, value):
        return value


class IterativeAffineEncrypt(SymmetricEncrypt):
    def __init__(self):
        super(IterativeAffineEncrypt, self).__init__()

    def generate_key(self, key_size=1024, key_round=5, encode_precision=2**100, randomized=False):
        self.key = IterativeAffineCipher.generate_keypair(
            key_size=key_size,
            key_round=key_round,
            encode_precision=encode_precision,
            randomized=randomized
        )

    def encrypt(self, plaintext):
        if self.key is not None:
            return self.key.encrypt(plaintext)
        else:
            return None

    def decrypt(self, ciphertext):
        if self.key is not None:
            return self.key.decrypt(ciphertext)
        else:
            return None

    def raw_encrypt(self, plaintext):
        return self.key.raw_encrypt(plaintext)

    def raw_decrypt(self, ciphertext):
        return self.key.raw_decrypt(ciphertext)
