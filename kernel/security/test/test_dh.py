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

import time

from kernel.security.diffie_hellman import DiffieHellman


def test_dh():
    start = time.clock()
    p, g = DiffieHellman.key_pair()
    r1 = DiffieHellman.generate_secret(p)
    gr1 = DiffieHellman.encrypt(g, r1, p)

    r2 = DiffieHellman.generate_secret(p)
    gr2 = DiffieHellman.encrypt(g, r2, p)

    s1 = DiffieHellman.decrypt(gr2, r1, p)
    s2 = DiffieHellman.decrypt(gr1, r2, p)
    assert s1 == s2
    end = time.clock()
    print("cost:{}".format(end - start))
