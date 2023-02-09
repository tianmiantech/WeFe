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

from common.python.utils.gmssl import sm2


def sign_with_sm3(data, private_key, public_key):
    data_bytes = bytes(data, encoding="utf-8")
    sm2_crypt = sm2.CryptSM2(private_key=private_key,
                             public_key=public_key, mode=1, asn1=True)
    return sm2_crypt.sign_with_sm3(data_bytes)


def verify_with_sm3(data, public_key, sign):
    data_bytes = bytes(data, encoding="utf-8")
    sm2_crypt = sm2.CryptSM2(
        private_key=None, public_key=public_key, mode=1, asn1=True)
    return sm2_crypt.verify_with_sm3(sign, data_bytes)
