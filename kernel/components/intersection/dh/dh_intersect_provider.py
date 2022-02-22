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


import random

import gmpy2

from common.python import session
from common.python.calculation.acceleration.aclr import dh_encrypt_id
from common.python.calculation.acceleration.utils import aclr_utils
from common.python.utils import log_utils
from kernel.components.intersection.intersect import DhIntersect
from kernel.security.diffie_hellman import DiffieHellman
from kernel.transfer.variables.transfer_class.dh_intersect_transfer_variable import DhIntersectTransferVariable
from kernel.utils import consts, abnormal_detection

LOGGER = log_utils.get_logger()


class DhIntersectionProvider(DhIntersect):
    def __init__(self, intersect_params):
        super().__init__(intersect_params)
        self.transfer_variable = DhIntersectTransferVariable()
        self.count = 0
        self.p = None
        self.r = random.SystemRandom().getrandbits(128)

    def cal_provider_ids_process_pair(self, data_instances: session.table) -> session.table:
        return data_instances.map(
            lambda k, v: (
                self.hash(gmpy2.mpz(int(self.hash(k), 16) * self.key)), k)
        )

    def get_dh_key(self, dh_bit=2048):
        return DiffieHellman.key_pair(dh_bit)

    def encrypt_ids_process(self, k, is_hash=False):
        if is_hash:
            return DiffieHellman.encrypt(int(self.hash(k), 16), self.r, self.p), k
        else:
            return DiffieHellman.encrypt(k, self.r, self.p), k

    def run(self, data_instances):
        LOGGER.info("Start dh intersection")
        abnormal_detection.empty_table_detection(data_instances)
        _, self.p = self.get_dh_key()
        self.p = self.p + 1 if self.p % 2 == 0 else self.p         # force to odd
        LOGGER.info("Get dh key!")
        public_mod = {"p": self.p}

        self.transfer_variable.dh_mod.remote(public_mod,
                                             role=consts.PROMOTER,
                                             idx=0)
        LOGGER.info("Remote public mod to Promoter.")

        # (provider_eid, id)
        if aclr_utils.check_aclr_support():
            provider_ids = dh_encrypt_id(data_instances, int(self.r), int(self.p), True)
        else:
            provider_ids = data_instances.map(lambda k, v: self.encrypt_ids_process(k, True))
        raw_provider_ids = provider_ids.mapValues(lambda v: 1, need_send=True)
        self.transfer_variable.intersect_provider_ids_process.remote(raw_provider_ids,
                                                                     role=consts.PROMOTER,
                                                                     idx=0)

        promoter_ids = self.transfer_variable.intersect_promoter_ids.get(idx=0)

        if aclr_utils.check_aclr_support():
            encrypt_promoter_ids = dh_encrypt_id(promoter_ids, int(self.r), int(self.p))
        else:
            encrypt_promoter_ids = promoter_ids.map(lambda k, v: self.encrypt_ids_process(k), need_send=True)

        self.transfer_variable.intersect_promoter_ids_process.remote(encrypt_promoter_ids,
                                                                     role=consts.PROMOTER,
                                                                     idx=0)

        # recv intersect ids
        encrypt_intersect_ids = self.transfer_variable.intersect_ids.get(idx=0)
        intersect_ids_pair = encrypt_intersect_ids.join(provider_ids, lambda e, h: (h, e))
        intersect_ids = intersect_ids_pair.map(lambda k, v: (v[0], v[1]))
        LOGGER.info("Get intersect ids from Promoter")

        intersect_ids = self._get_value_from_data(intersect_ids, data_instances)

        return intersect_ids
