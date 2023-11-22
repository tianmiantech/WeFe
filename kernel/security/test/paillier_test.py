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
import pickle
import time
import unittest

import numpy as np

from kernel.security.paillier import PaillierKeypair


class TestPaillierEncryptedNumber(unittest.TestCase):
    def setUp(self):
        self.public_key, self.private_key = PaillierKeypair.generate_keypair()

    def tearDown(self):
        unittest.TestCase.tearDown(self)

    def test_add(self):
        x_li = np.ones(100) * np.random.randint(100)
        y_li = np.ones(100) * np.random.randint(1000)
        z_li = np.ones(100) * np.random.rand()
        t_li = range(100)

        for i in range(x_li.shape[0]):
            x = x_li[i]
            y = y_li[i]
            z = z_li[i]
            t = t_li[i]
            en_x = self.public_key.encrypt(x)
            en_y = self.public_key.encrypt(y)
            en_z = self.public_key.encrypt(z)
            en_t = self.public_key.encrypt(t)

            en_res = en_x + en_y + en_z + en_t

            res = x + y + z + t

            de_en_res = self.private_key.decrypt(en_res)
            self.assertAlmostEqual(de_en_res, res)

    def test_mul(self):
        x_li = np.ones(100) * np.random.randint(100)
        y_li = np.ones(100) * np.random.randint(1000) * -1
        z_li = np.ones(100) * np.random.rand()
        t_li = range(100)

        for i in range(x_li.shape[0]):
            x = x_li[i]
            y = y_li[i]
            z = z_li[i]
            t = t_li[i]
            en_x = self.public_key.encrypt(x)

            en_res = (en_x * y + z) * t

            res = (x * y + z) * t

            de_en_res = self.private_key.decrypt(en_res)
            self.assertAlmostEqual(de_en_res, res)

        x = 9
        en_x = self.public_key.encrypt(x)

        for i in range(100):
            en_x = en_x + 5000 - 0.2
            x = x + 5000 - 0.2
            de_en_x = self.private_key.decrypt(en_x)
            self.assertAlmostEqual(de_en_x, x)

    def time_consume_start(self):
        self.start_time = time.time()

    def print_time_consume(self, print_info="", reset=True):
        print(f"{print_info}total time:{time.time() - self.start_time}")
        self.start_time = time.time()

    def test_add_time(self):
        repeat_time = 50000
        lengths = [1024, 2048, 4096]
        x = np.random.randint(1000000)
        y = np.random.randint(1000000)
        check_result = False

        for length in lengths:
            print(f'current n_length:{length}')
            public_key, private_key = PaillierKeypair.generate_keypair(n_length=length)
            enc_x = public_key.encrypt(x)
            enc_y = public_key.encrypt(y)

            self.time_consume_start()
            for i in range(repeat_time):
                result = enc_x + enc_y
                if check_result:
                    if x + y == private_key.decrypt(result):
                        print(f"check {length} result ok")

            self.print_time_consume(f"paillier add test, length:{length}, repeat:{repeat_time} ")

            self.time_consume_start()
            for i in range(repeat_time):
                result = x + y
            self.print_time_consume(f'plaintext add test, repeat:{repeat_time} ')

    def test_mul_time(self):
        repeat_time = 50000
        lengths = [1024, 2048, 4096]
        x = np.random.randint(1000000)
        y = np.random.randint(1000000)
        check_result = False

        for length in lengths:
            print(f'current n_length:{length}')
            public_key, private_key = PaillierKeypair.generate_keypair(n_length=length)
            enc_x = public_key.encrypt(x)

            self.time_consume_start()
            for i in range(repeat_time):
                result = enc_x * y
                if check_result:
                    if x * y == private_key.decrypt(result):
                        print(f"check {length} result ok")

            self.print_time_consume(f"paillier add test, length:{length}, repeat:{repeat_time} ")

            self.time_consume_start()
            for i in range(repeat_time):
                result = x * y
            self.print_time_consume(f'plaintext add test, repeat:{repeat_time} ')

    def test_encrypt_space(self):

        data = np.ones(1000) * np.random.randint(1000000)
        n_lengths = [1024, 2048, 4096]

        for length in n_lengths:
            public_key, private_key = PaillierKeypair.generate_keypair(n_length=length)
            data_result = np.asarray(list(map(public_key.encrypt, data)))
            pickle_result = pickle.dumps(data_result)
            print(f"n_length:{length},data_count:{len(data)}, src_size:{len(pickle.dumps(data))}, encrypted_size:{len(pickle_result)}")

        # repeat_time = 50000
        # lengths = [1024, 2048, 4096]
        # x = np.random.randint(1000000)
        # y = np.random.randint(1000000)
        # check_result = False
        #
        # for length in lengths:
        #     print(f'current n_length:{length}')
        #     public_key, private_key = PaillierKeypair.generate_keypair(n_length=length)
        #     enc_x = public_key.encrypt(x)
        #
        #     enc_x_dump_result = pickle.dumps(enc_x)
        #     enc_x_length = len(enc_x_dump_result) * repeat_time
        #
        #     x_dump_result = pickle.dumps(x)
        #     x_length = len(x_dump_result) * repeat_time
        #
        #     print(f'n_length:{length}, enc_x_length:{enc_x_length}, x_length:{x_length}')

    def test_value(self):
        x = 100
        y = 200
        z = 100

        public_key, private_key = PaillierKeypair.generate_keypair(n_length=1024)
        enc_x = public_key.encrypt(x)
        enc_z = public_key.encrypt(z)
        enc_xy = enc_x * y
        gpu_mul_before = enc_x.gpu_mul_before(y)
        import gmpy2
        to_cal = gpu_mul_before[0]
        gpu_result = gmpy2.powmod(to_cal[0], to_cal[1], to_cal[2])
        r = enc_x.gpu_mul_after(int(gpu_result), gpu_mul_before[1])
        add_before = r.gpu_add_before(enc_z)
        to_cal_2 = add_before[0]
        mulm_result = to_cal_2[0] * to_cal_2[1] % to_cal_2[2]
        rr = r.gpu_add_after(mulm_result, to_cal_2[1])
        rr

    def test_del(self):
        total = 1000000
        pop_size = 20
        data = []
        self.time_consume_start()
        for i in range(total):
            data.append(i)
        self.print_time_consume("append total ")

        result_array = []
        del_times = len(data) // pop_size
        for i in range(del_times):
            result_array.append(data[i * pop_size:(i + 1) * pop_size])
        del data[0:del_times * pop_size]
        print(data)
        # while len(data) > 0:
        #     if len(data) >= pop_size:
        #         result_array.append(data[0:pop_size])
        #         del data[0:pop_size]
        #     else:
        #         break

        self.print_time_consume("del ")


if __name__ == '__main__':
    unittest.main()
