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

import inspect
import time
import uuid

from common.python import session, Backend, RuntimeInstance
from common.python.common.consts import FunctionConfig
from common.python.utils import cloudpickle
from common.python.utils.store_type import DBTypes


def get_function_name():
    return inspect.stack()[1][3]


class ComputerTest(object):
    """
        compute method test
    """

    def __init__(self, backend_list=[Backend.LOCAL, Backend.SPARK], data_size=2):
        self.backend_list = backend_list
        self.current_backend = None
        self.result = {}
        self.prefix = '^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^   '
        self.data_size = data_size

    def main(self):
        # self.map()
        # self.map_values()
        # self.map_partition()
        # self.map_partition2()
        # self.reduce()
        # self.join()
        # self.union()
        # self.filter()
        # self.flat_map()
        # self.apply_partitions()
        self.map_reduce_partitions()


    def reset_data(self, backend, multi_dataset=1, diff_count=0):
        RuntimeInstance.SESSION = None
        session.init(job_id="computer-test", db_type=DBTypes.LMDB, backend=backend, options={
            # "fc_partition": FunctionConfig.FC_DEFAULT_PARTITION,
            "fc_partition": 2,
            "features_count": 10})
        self.current_backend = backend

        dataset_list = []
        cur_diff = 0
        for j in range(multi_dataset):
            datatable = session.table(str(uuid.uuid1()), "wefe_process")
            data_list = []
            for i in range(self.data_size - cur_diff):
                data_list.append((i, i))
            datatable.put_all(data_list)
            dataset_list.append(datatable)
            cur_diff += diff_count
        return dataset_list[0] if multi_dataset == 1 else dataset_list

    def show_current_backend(self):
        """
            Display the current computing backend
        Returns
        -------
            Backend
        """

        if self.current_backend == Backend.FC:
            return "FC"
        elif self.current_backend == Backend.SPARK:
            return "SPARK"
        return "LOCAL"

    def record_data(self, dsource, action_name, only_record_value=False):
        """
            save execution results
        Parameters
        ----------
        dsource
        action_name
        only_record_value

        Returns
        -------

        """
        if dsource is None:
            data = None
        elif isinstance(dsource, int) or isinstance(dsource, dict):
            data = dsource
        else:
            data = list(dsource.collect())
            data.sort()
        print(f'{self.prefix}{self.show_current_backend()} call {action_name}:{data}')

        if only_record_value:
            value_result = []
            for x in data:
                value_result += x[1]
            data = value_result

        if not self.result.get(action_name):
            self.result[action_name] = [data]
        else:
            self.result.get(action_name).append(data)

    def check_result_and_clear(self):
        """
            Compare execution results
        Returns
        -------

        """

        for action, result_list in self.result.items():
            if isinstance(result_list, list):
                if len(result_list) > 0 and not isinstance(result_list[0], dict):
                    result_list.sort()
                [x.sort() if isinstance(x, list) else x for x in result_list]
                for i in range(len(result_list)):
                    if i > 0:
                        if result_list[0] == result_list[i]:
                            print(f'{self.prefix}{action} check results: ok')
                        else:
                            print(
                                f'{self.prefix}{action} check results: fail, [0]:{result_list[0]},[i]:{result_list[i]}')
                            raise Exception("check fail")
            else:
                print('not support')
        self.result = dict()

    def map(self):
        for backend in self.backend_list:
            datatable = self.reset_data(backend).map(lambda x, y: (x, y + 1))
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def map_values(self):
        for backend in self.backend_list:
            datatable = self.reset_data(backend).mapValues(lambda v: v + 1)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def map_partition(self):
        def _func(data_list):
            deal_result = []
            for item in data_list:
                deal_result.append((f'mp:{item[0]}', f'mp:{item[1]}'))
            return deal_result

        for backend in self.backend_list:
            datatable = self.reset_data(backend).mapPartitions(_func)
            self.record_data(datatable, get_function_name(), True)
        self.check_result_and_clear()

    def map_partition2(self):
        def _func(data_list: list):
            deal_result = []
            for item in data_list:
                deal_result.append((f'mp:{item[0]}', f'mp:{item[1]}'))
            return deal_result

        for backend in self.backend_list:
            datatable = self.reset_data(backend).mapPartitions2(
                lambda data_list: [(f'mp:{x[0]}', f'mp:{x[1]}') for x in data_list])
            self.record_data(datatable, get_function_name(), only_record_value=True)
        self.check_result_and_clear()

    def reduce(self):
        def _add(x, y):
            return x + y

        def _key_func(k):
            return k % 2

        for backend in self.backend_list:
            datatable = self.reset_data(backend).reduce(_add, _key_func)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def join(self):
        def _deal_values(v1, v2):
            return f"v1:{v1},v2:{v2}"

        for backend in self.backend_list:
            datatable_list = self.reset_data(backend, multi_dataset=2, diff_count=1)
            datatable = datatable_list[0].join(datatable_list[1], _deal_values)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def filter(self):
        for backend in self.backend_list:
            datatable = self.reset_data(backend).filter(lambda x, y: x > 5)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def subtract_by_key(self):
        for backend in self.backend_list:
            datatable_list = self.reset_data(backend, multi_dataset=2, diff_count=1)
            datatable = datatable_list[0].subtractByKey(datatable_list[1])
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def glom(self):
        """
            Combine the data of each partition into a list
        Returns
        -------

        """
        for backend in self.backend_list:
            datatable = self.reset_data(backend).glom()
            self.record_data(datatable, get_function_name(), only_record_value=True)
        self.check_result_and_clear()

    def flat_map(self):
        """
            The list returned after func is executed for each data is tied
        Returns
        -------

        """

        for backend in self.backend_list:
            datatable = self.reset_data(backend).flatMap(lambda x, y: [(x, y + 1)])
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def sample(self):
        for backend in self.backend_list:
            datatable = self.reset_data(backend).sample(0.3, 100)
            print(datatable.count())
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def union(self):
        for backend in self.backend_list:
            datatable_list = self.reset_data(backend, multi_dataset=2, diff_count=1)
            datatable = datatable_list[0].union(datatable_list[1])
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def count(self):
        for backend in self.backend_list:
            datatable = self.reset_data(backend).count()
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def em(self):
        for backend in self.backend_list:
            datatable = self.reset_data(backend).em()
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def apply_partitions(self):
        def _func(data_list):
            deal_result = []
            for item in data_list:
                deal_result.append((f'mp:{item[0]}', f'mp:{item[1]}'))
            return deal_result

        for backend in self.backend_list:
            datatable = self.reset_data(backend).applyPartitions(_func)
            self.record_data(datatable, get_function_name(), True)
        self.check_result_and_clear()

    def map_reduce_partitions(self):
        def _map_func(data_list):
            for k, v in data_list:
                yield k, v + 1

        def _reduce_func(x, y):
            return x + y

        for backend in self.backend_list:
            datatable = self.reset_data(backend).mapReducePartitions(_map_func, _reduce_func)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()


def base64_test():
    import base64

    b_fun_a = cloudpickle.dumps(lambda x: x + 1)
    s = base64.b64encode(b_fun_a)
    print(s)
    print(b_fun_a.hex())
    print(base64.b85encode(b_fun_a))

    d_fun = cloudpickle.loads(base64.b64decode(s))
    print(d_fun(1))


def process_pool_test():
    RuntimeInstance.SESSION = None
    backend = Backend.FC
    session.init(job_id="process_pool_test", db_type=DBTypes.CLICKHOUSE, backend=backend, options={
        "fc_partition": 100})

    for j in range(10):
        datatable = session.table(str(uuid.uuid1()), "wefe_process")
        data_list = []
        for i in range(10000):
            data_list.append((i, i))
        datatable.put_all(data_list)
        new_dt = session.parallelize(
            datatable.collect(), include_key=True, partition=100)
        print(list(new_dt.collect()))


if __name__ == '__main__':
    ct = ComputerTest(backend_list=[Backend.LOCAL, Backend.FC])
    ct.main()
