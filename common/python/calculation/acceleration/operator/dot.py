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

import numpy as np
from scipy.sparse import csr_matrix

from common.python import RuntimeInstance
from common.python.calculation.acceleration.utils.aclr_utils import check_aclr_support
from common.python.common.exception.custom_exception import NotSupportTypeError
from kernel.base.instance import Instance
from kernel.security.paillier import PaillierEncryptedNumber

BATCH_SIZE = 20000


def table_dot(it):
    """
    table dot

    accelerate function `_table_dot_func` in fixedpoint_table.py

    Parameters
    ----------
    it:list
        [(key,([x.],[y.]))]

    Returns
    -------

    """
    ret = None
    batch_x = []
    batch_y = []
    current_batch_count = 0

    if not check_aclr_support():
        # in cpu
        for _, (x, y) in it:
            if ret is None:
                ret = np.tensordot(x, y, [[], []])
            else:
                ret += np.tensordot(x, y, [[], []])

        return ret

    # in gpu
    for _, (x, y) in it:

        if not batch_x or current_batch_count < BATCH_SIZE:
            batch_x.append(x)
            batch_y.append(y)
            current_batch_count = current_batch_count + len(x) * len(y)
            if current_batch_count >= BATCH_SIZE:
                batch_result = _gpu_tensordot_with_paillier_4batch(batch_x, batch_y)

                for item_batch in batch_result:
                    if ret is None:
                        ret = item_batch
                    else:
                        ret += item_batch

                batch_x, batch_y, current_batch_count = [], [], 0

    if batch_x:
        batch_result = _gpu_tensordot_with_paillier_4batch(batch_x, batch_y)

        for item_batch in batch_result:
            if ret is None:
                ret = item_batch
            else:
                ret = ret + item_batch

    return ret


def dot(value, w):
    """

    dot

    accelerate function `dot` in base_operator.py

    Parameters
    ----------
    value
    w

    Returns
    -------

    """
    if isinstance(value, Instance):
        X = value.features
    else:
        X = value

    # dot(a, b)[i, j, k, m] = sum(a[i, j, :] * b[k, :, m])
    # One-dimension dot, which is the inner product of these two arrays

    # At present, only the case of np.ndim(X) == 2 and np.ndim(w) == 1 is processed,
    # the others will be processed in the future
    if np.ndim(X) == np.ndim(w) == 1:
        return _one_dimension_dot(X, w)
    elif np.ndim(X) == 2 and np.ndim(w) == 1:

        if isinstance(X, csr_matrix):
            res = []
            for x in X:
                res.append(_one_dimension_dot(x, w))
            res = np.array(res)
        else:
            # GPU acceleration is used here, w is ciphertext, X is plaintext
            res = []
            batch_w = []
            batch_x = []

            # Record the length of each x,
            # in order to restore the calculation result of the corresponding number according to the length
            x_length_to_restore = []
            batch_result = []
            result_array = []

            for x in X:
                x_length_to_restore.append(len(x))
                for j in range(len(x)):
                    batch_w.append(w[j])
                    batch_x.append(x[j])
                    if len(batch_w) >= BATCH_SIZE:
                        # submit to gpu calc
                        batch_result.extend(_gpu_powm_batch(batch_w, batch_x))
                        batch_w = []
                        batch_x = []
                        _restore_batch_result_2_array(x_length_to_restore, batch_result, result_array)
                        _result_array_reduce_add(result_array)

            # submit residue to gpu
            if len(batch_w) > 0:
                batch_result.extend(_gpu_powm_batch(batch_w, batch_x))
                _restore_batch_result_2_array(x_length_to_restore, batch_result, result_array)
                _result_array_reduce_add(result_array)

            # Submit the remaining batches that are not enough to use CPU calculation and return the result
            for item_result_array in result_array:
                item_result = 0
                for item in item_result_array:
                    item_result += item
                res.append(item_result)

            res = np.array(res)

    else:
        res = np.dot(X, w)

    return res


def _restore_batch_result_2_array(x_length_to_restore: list, batch_result: list, result_array: list):
    """
    Restore the flattened GPU operation results back to the multi-dimensional array structure

    Parameters
    ----------
    x_length_to_restore
    batch_result
    result_array

    Returns
    -------

    """
    while len(x_length_to_restore) > 0:
        if len(batch_result) >= x_length_to_restore[0]:
            result_array.append(batch_result[0:x_length_to_restore[0]])
            del batch_result[0:x_length_to_restore[0]]
            x_length_to_restore.pop(0)
        else:
            break


def _dot_list_to_restore(x_length_to_restore: list, res: list, batch_result: list):
    """
    restore the result of dot

    Parameters
    ----------
    x_length_to_restore:list
        Record the length of each x, in order to restore the calculation result of the corresponding number
        according to the length

    res:list
        the final result

    batch_result:list
        GPU batch calculation results

    Returns
    -------

    """
    while len(x_length_to_restore) > 0:
        if len(batch_result) >= x_length_to_restore[0]:
            item_result = 0
            for i in range(x_length_to_restore[0]):
                item_result += batch_result[i]
            res.append(item_result)
            del batch_result[0:x_length_to_restore[0]]
            x_length_to_restore.pop(0)
        else:
            break


def _result_array_reduce_add(result_array: list):
    """
    PaillierEncryptedNumber result add

    Parameters
    ----------
    result_array

    Returns
    -------

    """

    # The addition is performed in a loop until the batch condition is not met
    while True:

        vaild_pair_cnt = 0
        for item_array in result_array:
            vaild_pair_cnt += len(item_array) // 2

        # Determine whether the conditions for batch submission are met
        if vaild_pair_cnt >= BATCH_SIZE:

            # Store the Modular multiplication parameters that need to be provided to the gpu operation
            param_4_gpu = []

            # Store the original object and exponent parameters of the paillier,
            # and restore the object after the GPU calculation is completed
            param_4_local = []

            to_restore_size = []
            current_batch_size = 0

            for item_array in result_array:
                item_array_length = len(item_array)
                item_submit_count = 0
                if current_batch_size == BATCH_SIZE:
                    break

                for i in range(0, item_array_length, 2):
                    if i == item_array_length - 1:
                        break
                    param = item_array[i].gpu_add_before(item_array[i + 1])
                    param_4_gpu.append(param[0])
                    param_4_local.append((item_array[i], param[1]))
                    item_submit_count += 1
                    current_batch_size += 1
                    if current_batch_size == BATCH_SIZE:
                        break

                to_restore_size.append(item_submit_count)

            aclr_client = RuntimeInstance.get_alcr_ins()
            gpu_result = aclr_client.mulm(param_4_gpu, param_4_local)

            for idx in range(len(to_restore_size)):
                each_pair_size = to_restore_size[idx]
                # Remove objects that have been added
                del result_array[idx][0:each_pair_size * 2]
                # Combine the result of the addition into the original array.
                # Since the addition does not need to consider the order, it is directly `extended`
                result_array[idx].extend(gpu_result[0:each_pair_size])
                # Remove processed results from gpu_result
                del gpu_result[0:each_pair_size]

        else:
            break


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


def _gpu_powm_batch(w_batch: list, x_batch: list):
    """
    Do batch modular exponentiation operations on wx

    Parameters
    ----------
    w_batch:list
    x_batch:list

    Returns
    -------

    """
    first_w = w_batch[0]
    if isinstance(first_w, PaillierEncryptedNumber):
        param_4_gpu = []
        param_4_local = []

        for i in range(len(w_batch)):
            param = w_batch[i].gpu_mul_before(x_batch[i])
            param_4_gpu.append(param[0])
            param_4_local.append((w_batch[i], param[1]))

        aclr_client = RuntimeInstance.get_alcr_ins()
        return aclr_client.powm(param_4_gpu, param_4_local)
    else:
        raise NotSupportTypeError(w=first_w)


def _gpu_tensordot_with_paillier_4batch(x_batch: list, y_batch: list):
    """
    Batch submission of homomorphic multiplication operations

    Parameters
    ----------
    x_batch:list
        [[E(x)...],[E(x)...]]
    y_batch:list
        [[E(y)...],[E(y)...]]

    Returns
    -------

    """
    first_x_batch = x_batch[0]
    # first_y_batch = y_batch[0]

    if isinstance(first_x_batch[0], PaillierEncryptedNumber):
        batch_data_shape = []
        result = []
        batch_param_4_gpu = []
        batch_param_4_local = []
        aclr_client = RuntimeInstance.get_alcr_ins()

        for each_batch_index in range(len(x_batch)):

            x = x_batch[each_batch_index]
            y = y_batch[each_batch_index]

            x_length = x.shape[0]
            y_length = y.shape[0]
            batch_data_shape.append((x_length, y_length))

            for i in range(x_length):
                for j in range(y_length):
                    # param: (x,p,m),exponent
                    param = x[i].gpu_mul_before(y[j])
                    batch_param_4_gpu.append(param[0])
                    # batch_param_4_local: (PaillierEncryptedNumber), exponent
                    batch_param_4_local.append((x[i], param[1]))

        # Submit to GPU calculation
        if len(batch_param_4_gpu) > 0:
            result.extend(aclr_client.powm(batch_param_4_gpu, batch_param_4_local))

            while len(batch_data_shape) > 0:
                item_shape = batch_data_shape[0]
                shape_length = item_shape[0] * item_shape[1]
                if len(result) >= shape_length:
                    yield np.asarray(result[0:shape_length]).reshape(shape_length // y_length, y_length)
                    del result[0:shape_length]
                    batch_data_shape.pop(0)
                else:
                    break

    else:
        for i in range(len(x_batch)):
            yield np.tensordot(x_batch[i], y_batch[i], [[], []])


def test_range():
    a = [1, 2, 3, 4, 5, 6, 7]
    for i in range(0, len(a), 2):
        print(i)
        # print(a[i])


if __name__ == '__main__':
    test_range()
