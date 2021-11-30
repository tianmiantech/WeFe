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


import hashlib

from common.python import RuntimeInstance
from common.python import session

BATCH_SIZE = 20000


def _hash(value):
    return hashlib.sha256(bytes(str(value), encoding='utf-8')).hexdigest()


def _generate_batch_data_iter(data_instance, r, p, is_hash):
    batch_data = []
    index = 0
    for k, v in data_instance.collect():
        batch_data.append(k)
        index += 1
        if index == BATCH_SIZE:
            yield batch_data, r, p, is_hash
            index = 0
            batch_data = []
    if index > 0:
        yield batch_data, r, p, is_hash


def _each_batch_encrypt(data_tuple):
    aclr_client = RuntimeInstance.get_alcr_ins()
    data = data_tuple[0]
    r = data_tuple[1]
    p = data_tuple[2]
    is_hash = data_tuple[3]

    if is_hash:
        cal_data = [(int(_hash(k), 16), r, p) for k in data]
    else:
        cal_data = [(k, r, p) for k in data]

    gpu_result = aclr_client.powm_base(cal_data)
    return [(gpu_result[i], data[i]) for i in range(len(cal_data))]


def _dh_encrypt_id(data_instance, r, p, is_hash):
    import multiprocessing
    process_count = multiprocessing.cpu_count()
    with multiprocessing.Pool(processes=process_count) as pool:
        result_iter = pool.map(_each_batch_encrypt, _generate_batch_data_iter(data_instance, r, p, is_hash))
        for item_result in result_iter:
            for item in item_result:
                yield item


def dh_encrypt_id(data_instance, r, p, is_hash):
    return session.parallelize(data=_dh_encrypt_id(data_instance, r, p, is_hash),
                               include_key=True,
                               partition=data_instance.get_partitions())
