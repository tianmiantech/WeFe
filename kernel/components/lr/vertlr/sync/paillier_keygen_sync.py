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

from kernel.security.encrypt import PaillierEncrypt
from kernel.utils import consts


class Promoter(object):
    # noinspection PyAttributeOutsideInit
    def register_paillier_keygen(self, transfer_variables):
        self._pubkey_transfer = transfer_variables.paillier_pubkey

    def paillier_keygen_and_broadcast(self, key_length, suffix=tuple()):
        cipher = PaillierEncrypt()
        cipher.generate_key(key_length)
        pub_key = cipher.get_public_key()
        self._pubkey_transfer.remote(obj=pub_key, role=consts.PROVIDER, idx=-1, suffix=suffix)
        return cipher


class Provider(object):
    # noinspection PyAttributeOutsideInit
    def register_paillier_keygen(self, transfer_variables):
        self._pubkey_transfer = transfer_variables.paillier_pubkey

    def gen_paillier_cipher(self, suffix=tuple(), member_id_list=None):
        pubkey = self._pubkey_transfer.get(idx=0, suffix=suffix, member_id_list=member_id_list)
        cipher = PaillierEncrypt()
        cipher.set_public_key(pubkey)
        return cipher
