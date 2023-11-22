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
import os
import shutil
from typing import Iterable

from common.python.common.consts import NAMESPACE
from common.python.protobuf.pyproto import intermediate_data_pb2
from common.python.storage.storage import Storage
from common.python.utils import file_utils
from common.python.utils.core_utils import deserialize


class LfsStorage(Storage):
    """
    Local fs storage

    Just used for local mode
    Data cannot be automatically deduplicated
    """

    def __init__(self, namespace, name, partitions=1, in_place_computing=False):
        self._namespace = namespace
        self._name = name
        self._partitions = partitions
        self._in_place_computing = in_place_computing
        self.data_dir = os.path.join(file_utils.get_project_base_directory(), 'data', 'LFS')

    def _get_file_full_dir(self, partition=None):
        if partition is None:
            relative_path = f'{self._namespace}/{self._name}'
        else:
            relative_path = f'{self._namespace}/{self._name}/{partition}'

        return os.path.join(self.data_dir, relative_path)

    def write_partition_bytes_data(self, partition, partition_bytes_data: list):
        object_relative_path = self.generate_file_relative_path(self._namespace,
                                                                self._name, partition,
                                                                len(partition_bytes_data))
        # print(f"object_relative_path:{object_relative_path},partition:{partition}")

        # encapsulated into the protobuf structure
        intermediate_data = intermediate_data_pb2.IntermediateData()
        for k_bytes, v_bytes in partition_bytes_data:
            # k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v)
            item_data = intermediate_data.intermediateData.add()
            item_data.key = k_bytes
            item_data.value = v_bytes
        object_val = intermediate_data.SerializeToString()

        file_full_path = os.path.join(self.data_dir, object_relative_path)
        with open(file_full_path, mode="wb") as fp:
            fp.write(object_val)

    def _generate_each_batch_iter(self, kv_list: Iterable, use_serialize, chunk_size: int):
        batch_data = [[] for _ in range(self._partitions)]
        batch_size_stat = [0 for _ in range(self._partitions)]

        for k, v in kv_list:
            k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v, use_serialize=use_serialize)
            p = self.hash_key_to_partition(k_bytes, self._partitions)
            batch_data[p].append((k_bytes, v_bytes))
            batch_size_stat[p] = batch_size_stat[p] + len(v_bytes)

            if batch_size_stat[p] >= chunk_size:
                yield p, batch_data[p], self._namespace, self._name, self._partitions
                batch_data[p] = []
                batch_size_stat[p] = 0

        for p in range(self._partitions):
            if batch_data[p]:
                # print(f"yield:p:{p},{batch_data[p]}")
                yield p, batch_data[p], self._namespace, self._name, self._partitions

    def _make_all_dir(self):
        for i in range(self._partitions):
            partition_dir = self._get_file_full_dir(partition=i)
            os.makedirs(partition_dir, exist_ok=True)

    def put(self, k, v, use_serialize=True):
        self.put_all([(k, v)], use_serialize=use_serialize)
        return True

    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=1024 * 1024 * 4):
        self._make_all_dir()
        batch_data_iter = self._generate_each_batch_iter(kv_list, use_serialize, chunk_size)
        for item_batch_wrap in batch_data_iter:
            partition = item_batch_wrap[0]
            kv_data = item_batch_wrap[1]
            self.write_partition_bytes_data(partition, kv_data)

    def put_if_absent(self, k, v, use_serialize=True):
        get_v = self.get(k)
        if get_v is None:
            self.put(k, v, use_serialize)

    def get(self, k, use_serialize=True, maybe_large_value=False):
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        p = self.hash_key_to_partition(k_bytes, self._partitions)

        for item in self.collect(use_serialize=use_serialize, partition=p):
            if item[0] == k:
                return item[1]

    def _get_all_files(self, file_dir):
        if os.path.exists(file_dir):
            list_files = os.listdir(file_dir)
            for item in list_files:
                item_path = os.path.join(file_dir, item)
                if os.path.isdir(item_path):
                    for in_file in self._get_all_files(item_path):
                        yield in_file
                elif os.path.isfile(item_path):
                    yield item_path

    def _read_file_data(self, file_path):
        if file_path:
            with open(file_path, mode="rb") as fp:
                obj_data = fp.read()
                intermediate_data = intermediate_data_pb2.IntermediateData()
                intermediate_data.ParseFromString(obj_data)
                for item_data in intermediate_data.intermediateData:
                    yield deserialize(item_data.key), deserialize(item_data.value)

    def _get_data_count_in_file(self, file_name: str):
        """
        Get data count in file

        Parameters
        ----------
        file_name

        Returns
        -------

        """
        rindex = file_name.rindex("_cnt")
        return int(file_name[rindex + 4:])

    def collect(self, min_chunk_size=0, use_serialize=True, partition=None) -> list:
        full_dir = self._get_file_full_dir(partition)
        all_files = self._get_all_files(full_dir)
        for each_file in all_files:
            for item in self._read_file_data(each_file):
                yield item

    def delete(self, k, use_serialize=True):
        pass

    def destroy(self):
        file_path = self._get_file_full_dir()
        if os.path.exists(file_path):
            shutil.rmtree(file_path)
            # for fileList in os.walk(file_path):
            #     for name in fileList[2]:
            #         # os.chmod(os.path.join(fileList[0], name), stat.S_IWRITE)
            #         os.remove(os.path.join(fileList[0], name))
            #         shutil.rmtree(file_path)

    def count(self):
        file_dir = self._get_file_full_dir()
        files = self._get_all_files(file_dir)

        cnt = 0
        for file in files:
            cnt += self._get_data_count_in_file(file)
        return cnt

    def take(self, n=1, keysOnly=False, use_serialize=True, partition=None):
        if n <= 0:
            n = 1
        i = 0
        it = self.collect(use_serialize=use_serialize, partition=partition)
        rtn = list()
        for item in it:
            if keysOnly:
                rtn.append(item[0])
            else:
                rtn.append(item)
            i += 1
            if i == n:
                break
        return rtn

    def first(self, keysOnly=False, use_serialize=True, partition=None):
        data_list = self.take(n=1, partition=partition)
        if data_list:
            return data_list[0]
        return None

    def save_as(self, name, namespace, partition=None, use_serialize=True, **kwargs):
        if partition is None:
            partition = self._partitions

        persistent = kwargs["persistent"]
        persistent_engine = kwargs["persistent_engine"]
        dsource = kwargs["dsource"]

        dsource.set_gc_disable()

        from common.python.p_session.base_impl.db_runtime import DBRuntime
        dup = DBRuntime.get_instance().table(name, namespace, partition,
                                             persistent=persistent, persistent_engine=persistent_engine)
        dup.put_all(self.collect(), use_serialize=use_serialize)
        dsource.set_gc_enable()
        return dup


if __name__ == '__main__':
    lfs = LfsStorage(NAMESPACE.PROCESS, "123", partitions=10)
    lfs.put("1", "2")
    print(lfs.get("1"))
    # for item in lfs.collect():
    #     print(item[0], item[1])
    # lfs.put_all([(i, i) for i in range(10)])
