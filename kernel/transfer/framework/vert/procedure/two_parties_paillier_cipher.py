#!/usr/bin/env python
# -*- coding: utf-8 -*-

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



from common.python.utils import log_utils
from kernel.security.encrypt import PaillierEncrypt
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class Promoter(object):
    def __init__(self):
        self._pubkey_transfer = None

    def gen_paillier_cipher_operator(self, transfer_variables, suffix=tuple()):
        self._pubkey_transfer = transfer_variables.paillier_pubkey
        cipher = PaillierEncrypt()
        cipher.generate_key()
        pub_key = cipher.get_public_key()
        self._pubkey_transfer.remote(obj=pub_key, role=consts.PROVIDER, idx=-1, suffix=suffix)
        return cipher


class Provider(object):
    def __init__(self):
        self._pubkey_transfer = None

    def gen_paillier_cipher_operator(self, transfer_variables, suffix=tuple()):
        self._pubkey_transfer = transfer_variables.paillier_pubkey
        pubkey = self._pubkey_transfer.get(idx=0, suffix=suffix)
        cipher = PaillierEncrypt()
        cipher.set_public_key(pubkey)
        return cipher
