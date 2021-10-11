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

from base64 import b64decode, b64encode

from Crypto.Hash import SHA1
from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5


def sign(data, private_key):
    key_bytes = bytes(private_key, encoding="utf-8")
    key_bytes = b64decode(key_bytes)
    key = RSA.importKey(key_bytes)
    hash_value = SHA1.new(bytes(data, encoding="utf-8"))
    signer = PKCS1_v1_5.new(key)
    signature = signer.sign(hash_value)
    return b64encode(signature)


def verify(data, public_key, sign):
    key_bytes = bytes(public_key, encoding="utf-8")
    key_bytes = b64decode(key_bytes)
    key = RSA.importKey(key_bytes)
    hash_value = SHA1.new(bytes(data, encoding="utf-8"))
    verifier = PKCS1_v1_5.new(key)
    return verifier.verify(hash_value, b64decode(sign))
