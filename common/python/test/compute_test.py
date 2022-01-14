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
import uuid
from typing import Union

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

    def __init__(self, backend_list=[Backend.LOCAL, Backend.SPARK], data_size=2,
                 db_type: Union[str, list] = DBTypes.LMDB):
        self.backend_list = backend_list
        self.current_backend = None
        self.current_db_type = None
        self.result = {}
        self.prefix = '^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^   '
        self.data_size = data_size
        self.db_type = db_type
        self.init_param_list = []
        for i in range(len(backend_list)):
            init_param = {"backend": backend_list[i], "db_type": db_type[i] if isinstance(db_type, list) else db_type}
            self.init_param_list.append(init_param)

    def main(self):
        self.map()
        self.map_values()
        self.map_partition()
        self.map_partition2()
        self.reduce()
        self.join()

    def reset_data(self, backend, multi_dataset=1, diff_count=0, db_type=None):
        RuntimeInstance.SESSION = None
        session.init(job_id="computer-test", db_type=db_type, backend=backend, options={
            "fc_partition": FunctionConfig.FC_DEFAULT_PARTITION,
            "features_count": 10})
        self.current_backend = backend
        self.current_db_type = db_type

        dataset_list = []
        cur_diff = 0
        for j in range(multi_dataset):
            p = j + 2
            datatable = session.table(str(uuid.uuid1()), "wefe_process", partition=p)
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
        print(f'{self.prefix}{self.show_current_backend()} {self.current_db_type} call {action_name}:{data}')

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
        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).map(
                lambda x, y: (x, y + 1))
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def map_values(self):
        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).mapValues(lambda v: v + 1)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def map_partition(self):
        def _func(data_list):
            deal_result = []
            for item in data_list:
                deal_result.append((f'mp:{item[0]}', f'mp:{item[1]}'))
            return deal_result

        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).mapPartitions(_func)
            self.record_data(datatable, get_function_name(), True)
        self.check_result_and_clear()

    def map_partition2(self):
        def _func(data_list: list):
            deal_result = []
            for item in data_list:
                deal_result.append((f'mp:{item[0]}', f'mp:{item[1]}'))
            return deal_result

        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).mapPartitions2(
                lambda data_list: [(f'mp:{x[0]}', f'mp:{x[1]}') for x in data_list])
            self.record_data(datatable, get_function_name(), only_record_value=True)
        self.check_result_and_clear()

    def reduce(self):
        def _add(x, y):
            return x + y

        def _key_func(k):
            return k % 2

        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).reduce(_add, _key_func)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def join(self):
        def _deal_values(v1, v2):
            return f"v1:{v1},v2:{v2}"

        for init_param in self.init_param_list:
            datatable_list = self.reset_data(init_param["backend"], db_type=init_param["db_type"], multi_dataset=2,
                                             diff_count=1)
            datatable = datatable_list[0].join(datatable_list[1], _deal_values)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def filter(self):
        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).filter(lambda x, y: x > 5)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def subtract_by_key(self):
        for init_param in self.init_param_list:
            datatable_list = self.reset_data(init_param["backend"], db_type=init_param["db_type"], multi_dataset=2,
                                             diff_count=1)
            datatable = datatable_list[0].subtractByKey(datatable_list[1])
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def glom(self):
        """
            Combine the data of each partition into a list
        Returns
        -------

        """
        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).glom()
            self.record_data(datatable, get_function_name(), only_record_value=True)
        self.check_result_and_clear()

    def flat_map(self):
        """
            The list returned after func is executed for each data is tied
        Returns
        -------

        """

        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).flatMap(
                lambda x, y: [(x, y + 1)])
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def sample(self):
        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).sample(0.3, 100)
            print(datatable.count())
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def union(self):
        for init_param in self.init_param_list:
            datatable_list = self.reset_data(init_param["backend"], db_type=init_param["db_type"], multi_dataset=2,
                                             diff_count=1)
            datatable = datatable_list[0].union(datatable_list[1])
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def count(self):
        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).count()
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def em(self):
        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).em()
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def apply_partitions(self):
        def f(it):
            r = []
            for k, v in it:
                r.append((v, v ** 2, v ** 3))
            return r

        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).applyPartitions(f)
            self.record_data(datatable, get_function_name())
        self.check_result_and_clear()

    def map_reduce_partitions(self):
        def _map_func(data_list):
            for k, v in data_list:
                yield k, v + 1

        def _reduce_func(x, y):
            return x + y

        for init_param in self.init_param_list:
            datatable = self.reset_data(init_param["backend"], db_type=init_param["db_type"]).mapReducePartitions(
                _map_func, _reduce_func)
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
    ct = ComputerTest(backend_list=[Backend.LOCAL, Backend.LOCAL], data_size=100,
                      db_type=[DBTypes.LMDB, DBTypes.LOCAL_FS])
    ct.map()
    ct.map_values()
    ct.apply_partitions()
    ct.map_partition()
    ct.map_partition2()
    ct.reduce()
    ct.join()
    ct.glom()
    ct.map_reduce_partitions()
    ct.sample()
    ct.filter()
    ct.union()
    ct.subtract_by_key()
    ct.flat_map()
