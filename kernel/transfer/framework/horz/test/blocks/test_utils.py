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
import unittest
import uuid
from multiprocessing import Pool

from kernel.utils import consts


class TestBlocks(unittest.TestCase):

    def clean_tables(self):
        from common.python import session
        session.init(job_id=self.job_id)
        try:
            session.cleanup("*", self.job_id, True)
        except EnvironmentError:
            pass
        try:
            session.cleanup("*", self.job_id, False)
        except EnvironmentError:
            pass

    def setUp(self) -> None:
        self.job_id = str(uuid.uuid1())

    def tearDown(self) -> None:
        self.clean_tables()

    @staticmethod
    def init_session_and_federation(job_id, role, member_id, member_id_map):
        from common.python import session
        from common.python import federation

        session.init(job_id)
        federation.init(job_id=job_id,
                        runtime_conf={"local": {"role": role, "member_id": member_id}, "role": member_id_map})

    @staticmethod
    def apply_func(func, job_id, role, num_hosts, ind, *args):
        member_id_map = dict(host=[9999 + i for i in range(num_hosts)], promoter=[9999], arbiter=[9999])
        member_id = 9999
        if role == consts.PROVIDER:
            member_id = 9999 + ind
        TestBlocks.init_session_and_federation(job_id, role, member_id, member_id_map)
        return func(job_id, role, ind, *args)

    @staticmethod
    def run_test(func, job_id, num_hosts, *args):
        pool = Pool(num_hosts + 2)
        tasks = []
        for role, ind in [(consts.ARBITER, 0), (consts.PROMOTER, 0)] + [(consts.PROVIDER, i) for i in range(num_hosts)]:
            tasks.append(
                pool.apply_async(func=TestBlocks.apply_func,
                                 args=(func, job_id, role, num_hosts, ind, *args))
            )
        pool.close()
        left = [i for i in range(len(tasks))]
        while left:
            time.sleep(0.01)
            tmp = []
            for i in left:
                if tasks[i].ready():
                    tasks[i] = tasks[i].get()
                else:
                    tmp.append(i)
            left = tmp
        return tasks[0], tasks[1], tasks[2:]
