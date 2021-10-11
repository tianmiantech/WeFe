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

import random

from kernel.transfer.framework.horz.sync import loss_transfer_sync
from kernel.utils import consts
from .horz_test_sync_base import TestSyncBase


class LossTransferTest(TestSyncBase):

    @classmethod
    def call(cls, role, transfer_variable, ind, *args):
        host_list = args[0]

        if role == consts.ARBITER:
            return list(loss_transfer_sync.Arbiter()
                        .register_loss_transfer(transfer_variable.host_loss,
                                                transfer_variable.promoter_loss)
                        .get_losses(host_list))
        elif role == consts.PROVIDER:
            import random
            has_loss = ind in host_list
            loss = random.random()
            if has_loss:
                loss_transfer_sync.Provider() \
                    .register_loss_transfer(transfer_variable.host_loss) \
                    .send_loss(loss)
            return has_loss, loss
        else:
            import random
            return loss_transfer_sync.Promoter() \
                .register_loss_transfer(transfer_variable.promoter_loss) \
                .send_loss(random.random())

    def run_with_num_hosts(self, num_hosts):
        ratio = 0.3
        host_list = [i for i in range(num_hosts) if random.random() > ratio]
        arbiter, promoter, *hosts = self.run_results(num_hosts, host_list)
        self.assertEqual(promoter, arbiter[0])
        for i in range(len(host_list)):
            host_id = host_list[i]
            self.assertTrue(hosts[host_id][0])
            self.assertEqual(arbiter[i + 1], hosts[host_id][1])

    def test_host_1(self):
        self.run_with_num_hosts(1)

    def test_host_10(self):
        self.run_with_num_hosts(10)
