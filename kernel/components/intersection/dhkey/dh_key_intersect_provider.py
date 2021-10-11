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
from common.python.utils import log_utils
from kernel.components.intersection.intersect import DhIntersect
from kernel.security.diffie_hellman import DiffieHellman
from kernel.transfer.variables.transfer_class.dh_key_intersect_transfer_variable import DhKeyIntersectTransferVariable
from kernel.utils import consts, abnormal_detection

LOGGER = log_utils.get_logger()


class DhKeyIntersectionProvider(DhIntersect):
    def __init__(self, intersect_params):
        super().__init__(intersect_params)
        self.transfer_variable = DhKeyIntersectTransferVariable()

        self.p = None
        self.g = None
        self.r = random.SystemRandom().getrandbits(128)
        self.promoter_key = None
        self.count = None
        self.promoter_initiative = False
        self.key = None

    def cal_provider_ids_process_pair(self, data_instances: session.table) -> session.table:
        return data_instances.map(
            lambda k, v: (
                self.hash(gmpy2.mpz(int(self.hash(k), 16) * self.key)), k)
        )

    def get_dh_key(self, dh_bit=2048):
        return DiffieHellman.key_pair(dh_bit)

    def provider_ids_process(self, data_instances):
        return self.cal_provider_ids_process_pair(data_instances)

    def run(self, data_instances):
        LOGGER.info("Start dh intersection")
        abnormal_detection.empty_table_detection(data_instances)
        self.count = data_instances.count()
        self.p, self.g = self.get_dh_key()
        LOGGER.info("Get dh key!")
        encrypt_r = DiffieHellman.encrypt(self.g, self.r, self.p);
        public_key = {"p": self.p, "g": self.g, "r": encrypt_r, "count": self.count}

        self.transfer_variable.dh_pubkey.remote(public_key,
                                                role=consts.PROMOTER,
                                                idx=0)
        LOGGER.info("Remote public key to Promoter.")

        promoter_key = self.transfer_variable.promoter_key.get(idx=0)
        self.promoter_key = int(promoter_key["r"])
        self.promoter_initiative = bool(promoter_key['promoter_initiative'])

        self.key = DiffieHellman.decrypt(self.promoter_key, self.r, self.p)
        provider_ids_process_pair = self.provider_ids_process(data_instances)

        if self.promoter_initiative:
            promoter_ids = self.transfer_variable.intersect_promoter_ids_process.get(idx=0)
            provider_ids_process = promoter_ids.join(provider_ids_process_pair, lambda sid, v: 1, need_send=True)
        else:
            provider_ids_process = provider_ids_process_pair.mapValues(lambda v: 1, need_send=True)

        self.transfer_variable.intersect_provider_ids_process.remote(provider_ids_process,
                                                                     role=consts.PROMOTER,
                                                                     idx=0)
        LOGGER.info("Remote provider_ids_process to Promoter.")

        # recv intersect ids
        encrypt_intersect_ids = self.transfer_variable.intersect_ids.get(idx=0)
        intersect_ids_pair = encrypt_intersect_ids.join(provider_ids_process_pair, lambda e, h: (h, e))
        intersect_ids = intersect_ids_pair.map(lambda k, v: (v[0], v[1]))
        LOGGER.info("Get intersect ids from Promoter")

        intersect_ids = self._get_value_from_data(intersect_ids, data_instances)

        return intersect_ids
