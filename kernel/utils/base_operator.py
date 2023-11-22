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

#
import time
from collections import Iterable

import numpy as np
from scipy.sparse import csr_matrix

from common.python.calculation.acceleration.aclr import dot as gpu_dot
from common.python.calculation.acceleration.utils.aclr_utils import check_aclr_support
from common.python.utils import log_utils
from kernel.base.instance import Instance
from kernel.base.sparse_vector import SparseVector
from kernel.security.paillier import PaillierEncryptedNumber, PaillierKeypair

LOGGER = log_utils.get_logger()


def _one_dimension_dot(X, w):
    res = 0
    # LOGGER.debug("_one_dimension_dot, len of w: {}, len of X: {}".format(len(w), len(X)))
    if isinstance(X, csr_matrix):
        for idx, value in zip(X.indices, X.data):
            res += value * w[idx]
    else:
        for i in range(len(X)):
            if np.fabs(X[i]) < 1e-5:
                continue
            res += w[i] * X[i]

    if res == 0:
        if isinstance(w[0], PaillierEncryptedNumber):
            res = 0 * w[0]
    return res


def cpu_dot(value, w):
    # LOGGER.debug(f'value:{value},w:{w}')

    if isinstance(value, Instance):
        X = value.features
    else:
        X = value

    # # dot(a, b)[i, j, k, m] = sum(a[i, j, :] * b[k, :, m])
    # # One-dimension dot, which is the inner product of these two arrays

    if np.ndim(X) == np.ndim(w) == 1:
        return _one_dimension_dot(X, w)
    elif np.ndim(X) == 2 and np.ndim(w) == 1:
        res = []
        for x in X:
            res.append(_one_dimension_dot(x, w))
        res = np.array(res)
    else:
        res = np.dot(X, w)

    return res


def dot(value, w):
    if isinstance(w[0], PaillierEncryptedNumber) and check_aclr_support():
        return gpu_dot(value, w)
    else:
        return cpu_dot(value, w)


def vec_dot(x, w):
    new_data = 0
    if isinstance(x, SparseVector):
        for idx, v in x.get_all_data():
            # if idx < len(w):
            new_data += v * w[idx]
    else:
        new_data = np.dot(x, w)
    return new_data


def reduce_add(x, y):
    if x is None and y is None:
        return None

    if x is None:
        return y

    if y is None:
        return x
    if not isinstance(x, Iterable):
        result = x + y
    elif isinstance(x, np.ndarray) and isinstance(y, np.ndarray):
        result = x + y
    else:
        result = []
        for idx, acc in enumerate(x):
            if acc is None:
                result.append(acc)
                continue
            result.append(acc + y[idx])
    return result


def norm(vector, p=2):
    """
    Get p-norm of this vector

    Parameters
    ----------
    vector : numpy array, Input vector
    p: int, p-norm

    """
    if p < 1:
        raise ValueError('p should larger or equal to 1 in p-norm')

    if type(vector).__name__ != 'ndarray':
        vector = np.array(vector)

    return np.linalg.norm(vector, p)


def dot_test():
    from kernel.security.paillier import PaillierKeypair
    public_key, private_key = PaillierKeypair.generate_keypair(n_length=1024)
    feature_count = 20
    row_count = 50000
    value = np.random.random_sample((feature_count, row_count))
    w = np.random.uniform(-1, 1, row_count)
    w = list(map(lambda x: public_key.encrypt(x), w))

    start = time.time()
    result = gpu_dot(value, w)
    print(f'gpu dot:{time.time() - start}')

    start = time.time()
    # result = cpu_dot(value, w)
    print(f'cpu dot:{time.time() - start}')

    # print(result)


if __name__ == '__main__':
    dot_test()
    # value = [[1, 2, 3], [4, 5, 6]]
    # w = [1, 2, 3]
    # print(dot(value, w))