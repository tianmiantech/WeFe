import pickle
import random
import time

import sys
import unittest

import gmpy2
import numpy as np

BITS = 2048


def getprimeover(n):
    """return a random n-bit prime number
    """
    r = gmpy2.mpz(random.SystemRandom().getrandbits(n))
    r = gmpy2.bit_set(r, n - 1)

    return int(gmpy2.next_prime(r))


class TestGPULib(unittest.TestCase):
    def setUp(self):
        sys.path.append('./build')
        import gpu_lib
        self.gpu_lib = gpu_lib

    def tearDown(self):
        unittest.TestCase.tearDown(self)

    def test_mulm(self):
        for i in range(10000):
            print(i)
            x = np.random.randint(10000000)
            y = np.random.randint(10000000)
            m = np.random.randint(10000000)
            # m = m if m % 2 == 1 else m + 1

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
        m = np.random.randint(10000000)
        x = np.random.randint(m - 1)
        y = np.random.randint(m - 1)
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

    def test_powm2(self):
        n_length = 1024
        x = getprimeover(n_length // 2)
        y = getprimeover(n_length // 2)
        m = max(x, y)
        m = m if m % 2 == 1 else m + 1

        batch_param = []
        x_bytes = x.to_bytes(n_length // 8, "little")
        y_bytes = y.to_bytes(n_length // 8, "little")
        m_bytes = m.to_bytes(n_length // 8, "little")

        for i in range(500000):
            batch_param.append(x_bytes)

        start = time.time()
        gpu_result = self.gpu_lib.powm_1024_2(batch_param, y_bytes, m_bytes, len(batch_param), )
        print(f'powm_1024_2 consume time:{time.time() - start}')

        for i in range(len(batch_param)):
            item_result = int.from_bytes(gpu_result[i], "little")
            self.assertAlmostEqual(item_result, gmpy2.powmod(x, y, m))

    def test_bits(self):
        n_length = 1024
        x = getprimeover(n_length // 2)
        p = getprimeover(n_length // 2)
        m = max(x, p)
        m = m + 2 if m % 2 == 1 else m + 1

        total_size = 500000

        call_bits = [1024]
        for bit in call_bits:
            x_bytes = x.to_bytes(bit // 8, "little")
            p_bytes = p.to_bytes(bit // 8, "little")
            m_bytes = m.to_bytes(bit // 8, "little")

            batch_param = []
            for i in range(total_size):
                batch_param.append((x_bytes, p_bytes, m_bytes))

            if bit == 1024:
                start = time.time()
                gpu_result = self.gpu_lib.powm_1024(batch_param, len(batch_param))
                print(f'powm 1024 consume time:{time.time() - start}')
            elif bit == 2048:
                start = time.time()
                gpu_result = self.gpu_lib.powm_2048(batch_param, len(batch_param))
                print(f'powm 2048 consume time:{time.time() - start}')
            print(len(gpu_result))


if __name__ == '__main__':
    unittest.main()
