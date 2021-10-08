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
from kernel.utils.paillier_tensor import PaillierTensor
from kernel.utils.random_number_generator import RandomNumberGenerator


class TestRandomNumberGenerator(unittest.TestCase):
    def setUp(self):
        session.init("test_random_number" + str(random.random()), 0)
        self.rng_gen = RandomNumberGenerator()

    def test_generate_random_number(self):
        data = np.ones((1, 3))
        random_data = self.rng_gen.generate_random_number(data.shape)

        self.assertTrue(random_data.shape == data.shape)

    def test_fast_generate_random_number(self):
        data = np.ones((1000, 100))

        random_data = self.rng_gen.fast_generate_random_number(data.shape)
        self.assertTrue(isinstance(random_data, PaillierTensor))
        self.assertTrue(tuple(random_data.shape) == tuple(data.shape))

    def tearDown(self):
        session.stop()


if __name__ == '__main__':
    unittest.main()
