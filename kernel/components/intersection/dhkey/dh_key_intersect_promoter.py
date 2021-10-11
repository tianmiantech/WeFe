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

from common.python.utils import log_utils
from kernel.components.intersection.intersect import DhIntersect
from kernel.security.diffie_hellman import DiffieHellman
from kernel.transfer.variables.transfer_class.dh_key_intersect_transfer_variable import DhKeyIntersectTransferVariable
from kernel.utils import consts, abnormal_detection

LOGGER = log_utils.get_logger()


class DhKeyIntersectionPromoter(DhIntersect):
    def __init__(self, intersect_params):
        super().__init__(intersect_params)

        self.random_bit = intersect_params.random_bit

        self.g = None
        self.p = None
        self.provider_key = None
        self.r = None
        self.key = None
        self.provider_count = None
        self.count = None
        self.initiative = False
        self.transfer_variable = DhKeyIntersectTransferVariable()

    def map_raw_id_to_encrypt_id(self, raw_id_data, encrypt_id_data):
        encrypt_id_data_exchange_kv = encrypt_id_data.map(lambda k, v: (v, k))
        encrypt_raw_id = raw_id_data.join(encrypt_id_data_exchange_kv, lambda r, e: (e, r))
        encrypt_common_id = encrypt_raw_id.map(lambda k, v: (v[0], v[1]), need_send=True)

        return encrypt_common_id

    @staticmethod
    def promoter_id_process(sid, key):
        promoter_encrypt_ids = DhKeyIntersectionPromoter.hash(
            gmpy2.mpz(int(DhKeyIntersectionPromoter.hash(sid), 16) * key))
        return promoter_encrypt_ids, sid

    def run(self, data_instances):
        LOGGER.info("Start dh intersection")
        abnormal_detection.empty_table_detection(data_instances)
        self.count = data_instances.count()
        public_keys = self.transfer_variable.dh_pubkey.get(-1)
        LOGGER.info("Get dh public_key:{} from Provider".format(public_keys))
        self.g = [int(public_key["g"]) for public_key in public_keys]
        self.p = [int(public_key["p"]) for public_key in public_keys]
        self.provider_count = [int(public_key['count']) for public_key in public_keys]
        self.initiative = any(i > self.count for i in self.provider_count)
        self.provider_key = [int(public_key["r"]) for public_key in public_keys]
        self.r = [random.SystemRandom().getrandbits(128) for i in range(len(self.g))]
        encrypt_r = [DiffieHellman.encrypt(self.g[i], self.r[i], self.p[i]) for i in range(len(self.g))]
        for i, promoter_r in enumerate(encrypt_r):
            promoter_key = {"r": promoter_r, "promoter_initiative": self.initiative}
            self.transfer_variable.promoter_key.remote(promoter_key,
                                                       role=consts.PROVIDER,
                                                       idx=i)
            LOGGER.info("Remote promoter_key to Provider {}".format(i))

        self.key = [DiffieHellman.decrypt(self.provider_key[i], self.r[i], self.p[i]) for i in
                    range(len(self.provider_key))]

        # table(promoter_encrypt_ids, sid)
        promoter_id_process_list = [data_instances.map(lambda k, v: self.promoter_id_process(k, self.key[i])) for i in
                                    range(len(self.key))]

        # send encrypt_id
        if self.initiative:
            for i, promoter_id in enumerate(promoter_id_process_list):
                remote_promoter_id = promoter_id.mapValues(lambda v: 1, need_send=True)
                self.transfer_variable.intersect_promoter_ids_process.remote(remote_promoter_id,
                                                                             role=consts.PROVIDER,
                                                                             idx=i)

        # table(provider_encrypt_ids, 1)
        provider_ids_process_list = self.transfer_variable.intersect_provider_ids_process.get(idx=-1)
        LOGGER.info("Get provider_ids_process")

        # intersect table(promoter_encrypt_ids, sid)
        encrypt_intersect_ids = [v.join(provider_ids_process_list[i], lambda sid, h: sid) for i, v in
                                 enumerate(promoter_id_process_list)]

        if len(self.provider_member_id_list) > 1:
            raw_intersect_ids = [e.map(lambda k, v: (v, 1)) for e in encrypt_intersect_ids]
            intersect_ids = self.get_common_intersection(raw_intersect_ids)
            intersect_ids = self.generate_id_nums(intersect_ids, has_encrypt_key=False)

            # send intersect id
            for i, provider_member_id in enumerate(self.provider_member_id_list):
                remote_intersect_id = self.map_raw_id_to_encrypt_id(intersect_ids, encrypt_intersect_ids[i])
                self.transfer_variable.intersect_ids.remote(remote_intersect_id,
                                                            role=consts.PROVIDER,
                                                            idx=i)
                LOGGER.info("Remote intersect ids to Provider {}!".format(provider_member_id))
        else:
            intersect_ids = encrypt_intersect_ids[0]
            new_intersect_ids = self.generate_id_nums(intersect_ids, has_encrypt_key=True)
            remote_intersect_id = new_intersect_ids.mapValues(lambda v: v[1], need_send=True)
            self.transfer_variable.intersect_ids.remote(remote_intersect_id,
                                                        role=consts.PROVIDER,
                                                        idx=0)
            intersect_ids = new_intersect_ids.map(lambda k, v: (v[0], v[1]))
        LOGGER.info("Finish intersect_ids computing")

        intersect_ids = self._get_value_from_data(intersect_ids, data_instances)
        # LOGGER.info("intersect_ids count {}".format(intersect_ids.count()))
        return intersect_ids
