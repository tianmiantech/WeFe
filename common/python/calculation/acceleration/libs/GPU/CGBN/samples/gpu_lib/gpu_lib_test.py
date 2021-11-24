import pickle
import time

import sys
import unittest

import gmpy2
import numpy as np

BITS = 2048


class TestGPULib(unittest.TestCase):
    def setUp(self):
        sys.path.append('./build')
        import gpu_lib
        self.gpu_lib = gpu_lib

    def tearDown(self):
        unittest.TestCase.tearDown(self)

    def test_mulm(self):
        x = np.random.randint(100000000)
        y = np.random.randint(100000000)
        m = np.random.randint(100000000)

        batch_param = []
        x_bytes = x.to_bytes(BITS // 8, "little")
        y_bytes = y.to_bytes(BITS // 8, "little")
        m_bytes = m.to_bytes(BITS // 8, "little")

        batch_param.append((x_bytes, y_bytes, m_bytes))
        gpu_result = self.gpu_lib.mulm_2048(batch_param, len(batch_param))

        for i in range(len(batch_param)):
            item_result = int.from_bytes(gpu_result[i], "little")
            self.assertAlmostEqual(item_result, x * y % m)

    def test_powm(self):
        x = np.random.randint(100000)
        y = np.random.randint(100000)
        m = np.random.randint(100000)
        m = m if m % 2 == 1 else m + 1

        batch_param = []
        x_bytes = x.to_bytes(BITS // 8, "little")
        y_bytes = y.to_bytes(BITS // 8, "little")
        m_bytes = m.to_bytes(BITS // 8, "little")

        batch_param.append((x_bytes, y_bytes, m_bytes))
        gpu_result = self.gpu_lib.powm_2048(batch_param, len(batch_param))

        for i in range(len(batch_param)):
            item_result = int.from_bytes(gpu_result[i], "little")
            self.assertAlmostEqual(item_result, gmpy2.powmod(x, y, m))


if __name__ == '__main__':
    unittest.main()
