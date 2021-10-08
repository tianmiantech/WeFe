# Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import random
import unittest

import numpy as np

from common.python import session
from kernel.utils import consts
from kernel.utils.paillier_tensor import PaillierTensor


class TestPaillierTensor(unittest.TestCase):
    def setUp(self):
        session.init("test_paillier_tensor" + str(random.random()), 0)
        self.data1 = np.ones((1000, 10))
        self.data2 = np.ones((1000, 10))
        self.paillier_tensor1 = PaillierTensor(ori_data=self.data1, partitions=10)
        self.paillier_tensor2 = PaillierTensor(ori_data=self.data2, partitions=10)

    def test_tensor_add(self):
        paillier_tensor = self.paillier_tensor1 + self.paillier_tensor2
        self.assertTrue(isinstance(paillier_tensor, PaillierTensor))
        self.assertTrue(paillier_tensor.shape == self.paillier_tensor1.shape)
        arr = paillier_tensor.numpy()
        self.assertTrue(abs(arr.sum() - 20000) < consts.FLOAT_ZERO)

    def test_ndarray_add(self):
        paillier_tensor = self.paillier_tensor1 + np.ones(10)
        self.assertTrue(isinstance(paillier_tensor, PaillierTensor))
        self.assertTrue(paillier_tensor.shape == self.paillier_tensor1.shape)
        arr = paillier_tensor.numpy()
        self.assertTrue(abs(arr.sum() - 20000) < consts.FLOAT_ZERO)

    def test_tensor_sub(self):
        paillier_tensor = self.paillier_tensor1 - self.paillier_tensor2
        self.assertTrue(isinstance(paillier_tensor, PaillierTensor))
        self.assertTrue(paillier_tensor.shape == self.paillier_tensor1.shape)

        arr = paillier_tensor.numpy()
        self.assertTrue(abs(arr.sum()) < consts.FLOAT_ZERO)

    def test_tensor_sub(self):
        paillier_tensor = self.paillier_tensor1 - np.ones(10)
        self.assertTrue(isinstance(paillier_tensor, PaillierTensor))
        self.assertTrue(paillier_tensor.shape == self.paillier_tensor1.shape)
        arr = paillier_tensor.numpy()
        self.assertTrue(abs(arr.sum()) < consts.FLOAT_ZERO)

    def test_constant_mul(self):
        paillier_tensor = self.paillier_tensor1 * 10
        self.assertTrue(isinstance(paillier_tensor, PaillierTensor))
        self.assertTrue(paillier_tensor.shape == self.paillier_tensor1.shape)
        arr = paillier_tensor.numpy()
        self.assertTrue(abs(arr.sum() - 100000) < consts.FLOAT_ZERO)

    def test_inverse(self):
        paillier_tensor = self.paillier_tensor2.T
        self.assertTrue(isinstance(paillier_tensor, PaillierTensor))
        self.assertTrue(paillier_tensor.shape == tuple([10, 1000]))

    def test_get_partition(self):
        self.assertTrue(self.paillier_tensor1.partitions == 10)

    def test_mean(self):
        self.assertTrue(abs(self.paillier_tensor1.mean() - 1.0) < consts.FLOAT_ZERO)

    def test_encrypt_and_decrypt(self):
        from kernel.security import PaillierEncrypt
        from kernel.security.encrypt_mode import EncryptModeCalculator
        encrypter = PaillierEncrypt()
        encrypter.generate_key(1024)

        encrypted_calculator = EncryptModeCalculator(encrypter, "fast")

        encrypter_tensor = self.paillier_tensor1.encrypt(encrypted_calculator)
        decrypted_tensor = encrypter_tensor.decrypt(encrypter)

        self.assertTrue(isinstance(encrypter_tensor, PaillierTensor))
        self.assertTrue(isinstance(decrypted_tensor, PaillierTensor))

        arr = decrypted_tensor.numpy()
        self.assertTrue(abs(arr.sum() - 10000) < consts.FLOAT_ZERO)

    def tearDown(self):
        session.stop()


if __name__ == '__main__':
    unittest.main()
