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


import sys
import time
import gmpy2

sys.path.append('./build')
import gpu_lib as gpu_cal

BITS = 2048


def to_32bit_binary_array(value: int):
    binary_str = bin(value)[2:]
    if len(binary_str) < BITS:
        binary_str = '0' * (BITS - len(binary_str)) + binary_str
    return binary_str


def to_32bit_int_array(value: int):
    binary_str = to_32bit_binary_array(value)
    # print(binary_str)
    result = []
    step = 32
    for i in range(0, len(binary_str), step):
        item_binary = binary_str[i:i + step]
        result.append(int(item_binary, 2))
    return result


def get_32bit_int_array(value: int):
    max_len = int(BITS / 32)
    result = [0] * max_len
    for i in range(max_len):
        if i > 0:
            value = value >> 32
        # result[max_len-i-1]=value & 0xffffffff
        result[i] = value & 0xffffffff

        # result.append(value & 0xffffffff)
    result.reverse()
    return result


def binary_array_to_pyint(value):
    pass


P = 689843881293174137
Q = 119723423039056370
M = 1490729983589926801


def generate_big_number():
    pass


def powmod(a, b, c):
    return int(gmpy2.powmod(a, b, c))


TOTAL = 50000
BATCH_SIZE = 50000


def gpu_cal_time():
    start = time.time()

    batch_param = []

    for i in range(TOTAL):
        p = get_32bit_int_array(P)
        q = get_32bit_int_array(Q)
        m = get_32bit_int_array(M)
        batch_param.append(gpu_cal.powmod_param_int(p, q, m))
        if len(batch_param) == BATCH_SIZE:
            print(f'param_time:{time.time() - start}')
    print(f'gpu consume time:{time.time() - start}')


def deal_item(p, q, m):
    a = get_32bit_int_array(p)
    b = get_32bit_int_array(q)
    c = get_32bit_int_array(m)
    return (a, b, c)


def gpu_param_test():
    start = time.time()
    batch_param = []

    for i in range(TOTAL):

        batch_param.append((str(P), str(Q), str(M)))
        if len(batch_param) == BATCH_SIZE:
            print(f'param_time:{time.time() - start}')

            # result = gpu_cal.add_test()
            result = gpu_cal.powm_param_test(batch_param, len(batch_param))
            print(f'gpu_result_len:{result}')
            batch_param = []
    if batch_param:
        result = gpu_cal.powm_2048(batch_param, len(batch_param))
        print(f'gpu_result_len:{len(result)}')
        batch_param = []

    print(f'gpu consume time:{time.time() - start}')


def gpu_param_test2():
    print("enter test2")
    # powm_param_test
    start = time.time()
    batch_param = []

    p = get_32bit_int_array(P)
    q = get_32bit_int_array(Q)
    m = get_32bit_int_array(M)

    for i in range(TOTAL):
        # pool.apply_async()

        # batch_param.append((str(P),str(Q),str(M)))
        # batch_param.append((P,Q,M))
        # batch_param.append((p,q,m))
        batch_param.extend(p)
        batch_param.extend(q)
        batch_param.extend(m)

        if len(batch_param) == BATCH_SIZE * 3 * 64:
            print(f'param_time:{time.time() - start}')

            # result = gpu_cal.add_test()
            result = gpu_cal.powm_param_test2(batch_param, len(batch_param))
            print(f'gpu_result_len:{result}')
            batch_param = []
    # if batch_param:
    #     result = gpu_cal.powm_2048(batch_param, len(batch_param))
    #     print(f'gpu_result_len:{len(result)}')
    #     batch_param = []

    print(f'gpu consume time:{time.time() - start}')


def gpu_param_test4():
    TOTAL = 1
    batch_param = []
    start = time.time()
    for i in range(TOTAL):
        p_bytes = P.to_bytes(256, "little")
        q_bytes = Q.to_bytes(256, "little")
        m_bytes = M.to_bytes(256, "little")
        batch_param.append((p_bytes, q_bytes, m_bytes))
        if i == 0:
            print("p", int.from_bytes(p_bytes[0:4], "little"))
            print("q", int.from_bytes(q_bytes[0:4], "little"))
            print("m", int.from_bytes(m_bytes[0:4], "little"))

    result = gpu_cal.powm_2048(batch_param, len(batch_param))
    print(result)
    print(int.from_bytes(result[0], "little"))
    print(f'gpu consume time4:{time.time() - start}')


def cpu_cal_time():
    start = time.time()
    for i in range(TOTAL):
        powmod(P, Q, M)
    print(f'cpu consume time:{time.time() - start}')


if __name__ == '__main__':
    start = time.time()
    gpu_param_test4()
