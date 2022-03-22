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

import time

import gmpy2
import gpu_lib

from common.python.calculation.acceleration.abc.aclr_abc import ACLR_ABC
from common.python.calculation.acceleration.utils.aclr_utils import to_bytes
from common.python.common.exception.custom_exception import GPUCalcError
from common.python.calculation.acceleration.utils.aclr_consts import SupportBits
from common.python.utils import log_utils

LOGGER = log_utils.get_logger()


class ACLR_GPU(ACLR_ABC):
    """
    GPU acceleration
    """

    def __init__(self, check_gpu_result=False):
        self.check_gpu_result = check_gpu_result

    def powm(self, batch_param_4_gpu: list, batch_param_4_local: list, bits, after_func=None):
        """
        Large-number modular exponentiation in bulk

        Parameters
        ----------
        batch_param_4_gpu:list
            [(int x,int p,int m)]

        batch_param_4_local:list
            [(paillierEncryptNumber,exponent)]

        Returns
        -------

        """

        batch_param = []
        for item in batch_param_4_gpu:
            batch_param.append((to_bytes(item[0],bits), to_bytes(item[1],bits), to_bytes(item[2],bits)))

        total_result = []
        start = time.time()

        if batch_param:
            if SupportBits.BITS_1024==bits:
                gpu_result = gpu_lib.powm_1024(batch_param, len(batch_param))
            elif SupportBits.BITS_2048==bits:
                gpu_result = gpu_lib.powm_2048(batch_param, len(batch_param))
            LOGGER.debug(f"gpu powm_{bits} cal complete,batch_size{len(batch_param)}, time:{time.time() - start}")

            for i in range(len(gpu_result)):
                # return paillier instance
                biginteger = int.from_bytes(gpu_result[i], "little")

                if self.check_gpu_result:
                    cpu_result = gmpy2.powmod(batch_param_4_gpu[i][0], batch_param_4_gpu[i][1], batch_param_4_gpu[i][2])
                    if cpu_result != biginteger:
                        raise GPUCalcError(calc=f'powm_{bits}', param=batch_param_4_gpu[i],
                                           result=(biginteger, cpu_result))
                if after_func:
                    total_result.append(after_func(batch_param_4_local[i], biginteger))
                else:
                    total_result.append(batch_param_4_local[i][0].gpu_mul_after(biginteger, batch_param_4_local[i][1],
                                                                                new_instance=True))

        return total_result

    def mulm(self, batch_param_4_gpu: list, batch_param_4_local: list, bits):
        """
        Large-number modular exponentiation in bulk

        Parameters
        ----------
        batch_param_4_gpu：list
            [(int x,int y,int m)]
        batch_param_4_local:list
            [(paillierEncryptNumber,encode_exponent)]

        Returns
        -------

        """
        batch_param = []
        for item in batch_param_4_gpu:
            batch_param.append((to_bytes(item[0],bits), to_bytes(item[1],bits), to_bytes(item[2],bits)))

        total_result = []
        start = time.time()

        if batch_param:
            if SupportBits.BITS_1024==bits:
                gpu_result = gpu_lib.mulm_1024(batch_param, len(batch_param))
            elif SupportBits.BITS_2048==bits:
                gpu_result = gpu_lib.mulm_2048(batch_param, len(batch_param))

            LOGGER.debug(f"gpu mulm_{bits} cal complete,batch_size{len(batch_param)}, time:{time.time() - start}")
            for i in range(len(gpu_result)):

                biginteger = int.from_bytes(gpu_result[i], "little")
                if self.check_gpu_result:
                    cpu_result = int(batch_param_4_gpu[i][0] * batch_param_4_gpu[i][1] % batch_param_4_gpu[i][2])
                    if cpu_result != biginteger:
                        raise GPUCalcError(calc=f'mulm_{bits}', param=batch_param_4_gpu[i],
                                           result=(biginteger, cpu_result))
                total_result.append(
                    batch_param_4_local[i][0].gpu_add_after(biginteger, batch_param_4_local[i][1], new_instance=True))

        return total_result

    def powm_base(self, batch_param_4_gpu: list, bits):
        batch_param = []
        for item in batch_param_4_gpu:
            batch_param.append((to_bytes(item[0], bits), to_bytes(item[1], bits), to_bytes(item[2], bits)))

        total_result = []
        start = time.time()

        if batch_param:
            if SupportBits.BITS_1024==bits:
                gpu_result = gpu_lib.powm_1024(batch_param, len(batch_param))
            elif SupportBits.BITS_2048==bits:
                gpu_result = gpu_lib.powm_2048(batch_param, len(batch_param))
            LOGGER.debug(f"gpu powm_{bits} cal complete,batch_size{len(batch_param)}, time:{time.time() - start}")
            for i in range(len(gpu_result)):
                biginteger = int.from_bytes(gpu_result[i], "little")

                if self.check_gpu_result:
                    cpu_result = gmpy2.powmod(batch_param_4_gpu[i][0], batch_param_4_gpu[i][1], batch_param_4_gpu[i][2])
                    if cpu_result != biginteger:
                        raise GPUCalcError(calc=f'powm_{bits}', param=batch_param_4_gpu[i],
                                           result=(biginteger, cpu_result))

                total_result.append(biginteger)

        return total_result
