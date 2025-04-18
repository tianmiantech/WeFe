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

from abc import ABC
from abc import abstractmethod
from kernel.security import PaillierEncrypt, IterativeAffineEncrypt
from kernel.transfer.variables.transfer_class.cipher_compressor_transfer_variable \
    import CipherCompressorTransferVariable
from common.python.utils import log_utils

LOGGER = log_utils.get_logger()


def get_horz_encryption_max_int(encrypter):
    if type(encrypter) == PaillierEncrypt:
        max_pos_int = encrypter.public_key.max_int
        min_neg_int = -max_pos_int
    elif type(encrypter) == IterativeAffineEncrypt:
        n_array = encrypter.key.n_array
        allowed_max_int = n_array[0]
        max_pos_int = int(allowed_max_int * 0.9) - 1  # the other 0.1 part is for negative num
        min_neg_int = (max_pos_int - allowed_max_int) + 1
    else:
        raise ValueError('unknown encryption type')

    return max_pos_int, min_neg_int


def cipher_compress_advisor(encrypter, plaintext_bit_len):
    max_pos_int, min_neg_int = get_horz_encryption_max_int(encrypter)
    max_bit_len = max_pos_int.bit_length()
    capacity = max_bit_len // plaintext_bit_len
    return capacity


class CipherPackage(ABC):

    @abstractmethod
    def add(self, obj):
        pass

    @abstractmethod
    def unpack(self, decrypter):
        pass

    @abstractmethod
    def has_space(self):
        pass


class PackingCipherTensor(object):
    """
    A naive realization of cipher tensor
    """

    def __init__(self, ciphers):

        if type(ciphers) == list:
            if len(ciphers) == 1:
                self.ciphers = ciphers[0]
            else:
                self.ciphers = ciphers
            self.dim = len(ciphers)
        else:
            self.ciphers = ciphers
            self.dim = 1

    # def __len__(self):
    #     return self.dim

    def __add__(self, other):

        new_cipher_list = []
        if type(other) == PackingCipherTensor:
            assert self.dim == other.dim

            if self.dim == 1:
                return PackingCipherTensor(self.ciphers + other.ciphers)
            for c1, c2 in zip(self.ciphers, other.ciphers):
                new_cipher_list.append(c1 + c2)
            return PackingCipherTensor(ciphers=new_cipher_list)
        else:
            # scalar / single en num
            if self.dim == 1:
                return PackingCipherTensor(self.ciphers + other)
            for c in self.ciphers:
                new_cipher_list.append(c + other)
            return PackingCipherTensor(ciphers=new_cipher_list)

    def __radd__(self, other):
        return self.__add__(other)

    def __sub__(self, other):
        return self + other * -1

    def __rsub__(self, other):
        return other + (self * -1)

    def __mul__(self, other):

        if self.dim == 1:
            return PackingCipherTensor(self.ciphers * other)
        new_cipher_list = []
        for c in self.ciphers:
            new_cipher_list.append(c * other)
        return PackingCipherTensor(new_cipher_list)

    def __rmul__(self, other):
        return self.__mul__(other)

    def __truediv__(self, other):
        return self.__mul__(1 / other)

    def __repr__(self):
        return "[" + self.ciphers.__repr__() + "], dim {}".format(self.dim)


class NormalCipherPackage(CipherPackage):

    def __init__(self, padding_length, max_capacity):

        self._padding_num = 2 ** padding_length
        self.max_capacity = max_capacity
        self._cipher_text = None
        self._capacity_left = max_capacity
        self._has_space = True

    def add(self, cipher_text):

        if self._capacity_left == 0:
            raise ValueError('cipher number exceeds package max capacity')

        if self._cipher_text is None:
            self._cipher_text = cipher_text
        else:
            self._cipher_text = self._cipher_text * self._padding_num
            self._cipher_text = self._cipher_text + cipher_text

        self._capacity_left -= 1
        if self._capacity_left == 0:
            self._has_space = False

    def unpack(self, decrypter):

        if type(decrypter) == PaillierEncrypt:
            # LOGGER.debug(f"cipher_text: {self._cipher_text}")
            compressed_plain_text = decrypter.privacy_key.raw_decrypt(self._cipher_text.ciphertext())
        elif type(decrypter) == IterativeAffineEncrypt:
            compressed_plain_text = decrypter.key.raw_decrypt(self._cipher_text)
        else:
            raise ValueError('unknown decrypter: {}'.format(type(decrypter)))

        if self.cur_cipher_contained() == 1:
            return [compressed_plain_text]

        unpack_result = []
        bit_len = (self._padding_num - 1).bit_length()
        for i in range(self.cur_cipher_contained()):
            num = (compressed_plain_text & (self._padding_num - 1))
            compressed_plain_text = compressed_plain_text >> bit_len
            unpack_result.insert(0, num)

        return unpack_result

    def has_space(self):
        return self._has_space

    def cur_cipher_contained(self):
        return self.max_capacity - self._capacity_left

    def retrieve(self):
        return self._cipher_text


class PackingCipherTensorPackage(CipherPackage):
    """
    A naive realization of compressible tensor(only compress last dimension because previous ciphers have
    no space for compressing)
    """

    def __init__(self, padding_length, max_capcity):
        self.cached_list = []
        self.compressed_cipher = []
        self.compressed_dim = -1
        self.not_compress_len = None
        self.normal_package = NormalCipherPackage(padding_length, max_capcity)

    def add(self, obj: PackingCipherTensor):

        if self.normal_package.has_space():
            if obj.dim == 1:
                self.normal_package.add(obj.ciphers)
            else:
                self.cached_list.extend(obj.ciphers[:-1])
                self.not_compress_len = len(obj.ciphers[:-1])
                self.normal_package.add(obj.ciphers[-1])
        else:
            raise ValueError('have no space for compressing')

    def unpack(self, decrypter):

        compressed_part = self.normal_package.unpack(decrypter)
        de_rs = []
        if len(self.cached_list) != 0:
            de_rs = decrypter.recursive_raw_decrypt(self.cached_list)

        if len(de_rs) == 0:
            return [[i] for i in compressed_part]
        else:
            rs = []
            idx_0, idx_1 = 0, 0
            while idx_0 < len(self.cached_list):
                rs.append(de_rs[idx_0: idx_0 + self.not_compress_len] + [compressed_part[idx_1]])
                idx_0 += self.not_compress_len
                idx_1 += 1
            return rs

    def has_space(self):
        return self.normal_package.has_space()


class CipherCompressorProvider(object):

    def __init__(self, package_class=PackingCipherTensorPackage, sync_para=True):

        """
        Parameters
        ----------
        package_class type of compressed packages
        """

        self._package_class = package_class
        self._padding_length, self._capacity = None, None
        if sync_para:
            self.transfer_var = CipherCompressorTransferVariable()
            # received from Provider
            self._padding_length, self._capacity = self.transfer_var.compress_para.get(idx=0)
            LOGGER.debug("received parameter from promoter is {} {}".format(self._padding_length, self._capacity))

    def compress(self, encrypted_obj_list):

        rs = []
        encrypted_obj_list = list(encrypted_obj_list)
        cur_package = self._package_class(self._padding_length, self._capacity)
        for c in encrypted_obj_list:
            if not cur_package.has_space():
                rs.append(cur_package)
                cur_package = self._package_class(self._padding_length, self._capacity)
            cur_package.add(c)

        rs.append(cur_package)
        return rs

    def compress_dtable(self, table):
        rs = table.mapValues(self.compress)
        return rs


if __name__ == '__main__':
    a = PackingCipherTensor([1, 2, 3, 4])
    b = PackingCipherTensor([2, 3, 4, 5])
    c = PackingCipherTensor(124)
    d = PackingCipherTensor([114514])
