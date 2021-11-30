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

import random

import gmpy2

POWMOD_GMP_SIZE = pow(2, 64)


def powmod(a, b, c):
    """
    return int: (a ** b) % c
    """

    if a == 1:
        return 1

    if max(a, b, c) < POWMOD_GMP_SIZE:
        return pow(a, b, c)

    else:
        return int(gmpy2.powmod(a, b, c))


def crt_coefficient(p, q):
    """
    return crt coefficient
    """
    tq = gmpy2.invert(p, q)
    tp = gmpy2.invert(q, p)
    return tp * q, tq * p


def powmod_crt(x, d, n, p, q, cp, cq):
    """
    return int: (a ** b) % n
    """

    rp = gmpy2.powmod(x, d % (p - 1), p)
    rq = gmpy2.powmod(x, d % (q - 1), q)
    return int((rp * cp + rq * cq) % n)


def invert(a, b):
    """return int: x, where a * x == 1 mod b
    """
    x = int(gmpy2.invert(a, b))

    if x == 0:
        raise ZeroDivisionError('invert(a, b) no inverse exists')

    return x


def getprimeover(n):
    """return a random n-bit prime number
    """
    r = gmpy2.mpz(random.SystemRandom().getrandbits(n))
    r = gmpy2.bit_set(r, n - 1)

    return int(gmpy2.next_prime(r))


def isqrt(n):
    """ return the integer square root of N """

    return int(gmpy2.isqrt(n))
