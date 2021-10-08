# Copyright 2021 The WeFe Authors. All Rights Reserved.
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

from common.python.utils import log_utils
from kernel.components.intersection.intersect import DhIntersect
from kernel.security.diffie_hellman import DiffieHellman
from kernel.transfer.variables.transfer_class.dh_intersect_transfer_variable import DhIntersectTransferVariable
from kernel.utils import consts, abnormal_detection

LOGGER = log_utils.get_logger()


class DhIntersectionPromoter(DhIntersect):
    def __init__(self, intersect_params):
        super().__init__(intersect_params)
        self.random_bit = intersect_params.random_bit
        self.p = None
        self.r = None
        self.transfer_variable = DhIntersectTransferVariable()

    @staticmethod
    def map_raw_id_to_encrypt_id(raw_id_data, encrypt_id_data):
        encrypt_id_data_exchange_kv = encrypt_id_data.map(lambda k, v: (v, k))
        encrypt_raw_id = raw_id_data.join(encrypt_id_data_exchange_kv, lambda r, e: (e, r))
        encrypt_common_id = encrypt_raw_id.map(lambda k, v: (v[0], v[1]))

        return encrypt_common_id

    def promoter_id_process(self, k, key, p, is_hash=False):
        if is_hash:
            return DiffieHellman.encrypt(int(self.hash(k), 16), key, p), k
        else:
            return DiffieHellman.encrypt(k, key, p), k

    def run(self, data_instances):
        LOGGER.info("Start dh intersection")
        abnormal_detection.empty_table_detection(data_instances)
        public_keys = self.transfer_variable.dh_mod.get(-1)
        LOGGER.info("Get dh mod:{} from Provider".format(public_keys))
        self.p = [int(public_key["p"]) for public_key in public_keys]
        self.r = [random.SystemRandom().getrandbits(self.random_bit) for i in range(len(self.p))]

        # (promoter_eid, id)
        promoter_id_list = [data_instances.map(lambda k, v: self.promoter_id_process(k, self.r[i], self.p[i], True))
                            for i in range(len(self.r))]
        for i, promoter_ids in enumerate(promoter_id_list):
            promoter_ids_provider = promoter_ids.mapValues(lambda v: 1, need_send=True)
            self.transfer_variable.intersect_promoter_ids.remote(promoter_ids_provider,
                                                                 role=consts.PROVIDER,
                                                                 idx=i)
            LOGGER.info("Remote promoter_ids to Provider {}".format(i))

        # (provider_eid, 1)
        provider_id_list = self.transfer_variable.intersect_provider_ids_process.get(-1)

        # (promoter_eeid, promoter_eid)
        encrypt_promoter_id_list = self.transfer_variable.intersect_promoter_ids_process.get(-1)

        # (provider_eeid, provider_eid)
        encrypt_provider_id_list = [ids.map(lambda k, v: self.promoter_id_process(k, self.r[i], self.p[i])) for i, ids
                                    in enumerate(provider_id_list)]
        # (intersect_eeid, (promoter_eid, provider_eid))
        encrypt_intersect_id_list = [
            encrypt_promoter_id_list[i].join(encrypt_provider_id_list[i], lambda pm_eid, pv_eid: (pm_eid, pv_eid))
            for i
            in range(len(self.p))]

        # (promoter_eid, provider_eid)
        intersect_id_list = [ids.map(lambda k, v: (v[0], v[1])) for ids in encrypt_intersect_id_list]

        member_count = len(self.provider_member_id_list)
        if member_count > 1:
            # (pm_eid,id)
            raw_intersect_id_list = [ids.join(promoter_id_list[i], lambda pv_eid, id: id) for i, ids in
                                     enumerate(intersect_id_list)]
            # (id, pm_eid)
            raw_intersect_id_list = [ids.map(lambda pm_eid, id: (id, pm_eid)) for ids in raw_intersect_id_list]
            # (id, 1)
            common_intersect_ids = self.get_common_intersection(raw_intersect_id_list)
            # (id, new_id)
            intersect_ids = self.generate_id_nums(common_intersect_ids, has_encrypt_key=False)

            for i, provider_member_id in enumerate(self.provider_member_id_list):
                # (id, (pm_eid, new_id)
                intersect_eid_nid = intersect_ids.join(raw_intersect_id_list[i], lambda nid, pm_eid: (pm_eid, nid))
                # (pm_eid, new_id)
                intersect_eid_nid = intersect_eid_nid.map(lambda k, v: (v[0], v[1]))
                # (pm_eid, (pv_eid, new_id))
                intersect_provider_eid_nid = intersect_eid_nid.join(intersect_id_list[i],
                                                                    lambda nid, pv_eid: (pv_eid, nid))
                # (pv_eid, new_id)
                remote_intersect_id = intersect_provider_eid_nid.map(lambda k, v: (v[0], v[1]), need_send=True)
                self.transfer_variable.intersect_ids.remote(remote_intersect_id,
                                                            role=consts.PROVIDER,
                                                            idx=i)
                LOGGER.info("Remote intersect ids to Provider {}!".format(provider_member_id))

            pass
        else:
            intersect_ids = intersect_id_list[0]
            new_intersect_ids = self.generate_id_nums(intersect_ids, has_encrypt_key=True)
            remote_intersect_id = new_intersect_ids.map(lambda k, v: (v[0], v[1]), need_send=True)
            self.transfer_variable.intersect_ids.remote(remote_intersect_id,
                                                        role=consts.PROVIDER,
                                                        idx=0)
            intersect_ids = new_intersect_ids.join(promoter_ids, lambda new_id, id: (id, new_id[1]))
            intersect_ids = intersect_ids.map(lambda k, v: (v[0], v[1]))

        LOGGER.info("Finish intersect_ids computing")

        intersect_ids = self._get_value_from_data(intersect_ids, data_instances)
        LOGGER.info("intersect_ids count {}".format(intersect_ids.count()))
        return intersect_ids
