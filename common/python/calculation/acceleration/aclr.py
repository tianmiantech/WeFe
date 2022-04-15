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

from common.python.calculation.acceleration.operator import dot as operator_dot
from common.python.calculation.acceleration.operator import encrypt


def table_dot(it, bits=2048):
    """
    table dot

    Speed up the method of _table_dot_func in fixedpoint_table.py

    Parameters
    ----------
    it:list
        [(key,([x.],[y.]))]
    bits:
    Returns
    -------

    """
    return operator_dot.table_dot(it, bits)


def table_dot_gpu(X, Y, partitions):
    return operator_dot.gpu_paillier_table_dot(X, Y, partitions)


def dot(value, w, bits=2048):
    """
    dot

    Speed up the method of dot in base_operator.py

    Parameters
    ----------
    value
    w
    bits

    Returns
    -------

    """
    # return operator_dot.dot(value, w, bits)
    return operator_dot.gpu_paillier_dot(value, w)


def dh_encrypt_id(data_instance, r, p, is_hash=False, bits=2048):
    """
    encrypt id for dh
    :param data_instance:
    :param r:
    :param p:
    :param is_hash:
    :param bits:
    :return:

    """
    return encrypt.dh_encrypt_id(data_instance, r, p, is_hash, bits=bits)
