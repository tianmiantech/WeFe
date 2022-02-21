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

import itertools
import multiprocessing
import os
import uuid
from collections.abc import Iterable

import oss2
from oss2.models import LifecycleRule, LifecycleExpiration, BucketLifecycle

from common.python import RuntimeInstance
from common.python.common import consts
from common.python.protobuf.pyproto import intermediate_data_pb2
from common.python.storage.fc_storage import FCStorage
from common.python.utils import log_utils, conf_utils, network_utils
from common.python.utils.core_utils import deserialize, serialize


LOGGER = log_utils.get_logger()


class OssStorage(FCStorage):
    """
    Oss Storage

    The intermediate storage layer of the function calculation
    """

    OBJECT_MAX_DATA_COUNT = 5000
    OBJECT_MIN_DATA_COUNT = 500
    OBJECT_FILE_MAX_SIZE = 1024 * 1024 * 4

    def __init__(self, namespace, name, partitions=1, bucket_client=None, cloud_store_temp_auth=None):
        self._namespace = namespace
        self._name = name
        self._partitions = partitions
        self.schema = {}
        self._cloud_store_temp_auth = cloud_store_temp_auth
        self._bucket = bucket_client or self._get_bucket()
        self._use_multiprocess = True
        self._bucket_name = None

    def init_life_cycle(self):
        """
        Set the life cycle of the oss file
        Only set it once at the time of initialization

        Returns
        -------

        """
        wefe_process_expire_rule = LifecycleRule('wefe_process_expire_rule', consts.NAMESPACE.PROCESS + "/",
                                                 status=LifecycleRule.ENABLED,
                                                 expiration=LifecycleExpiration(days=1))

        wefe_transfer_expire_rule = LifecycleRule('wefe_transfer_expire_rule', consts.NAMESPACE.TRANSFER + "/",
                                                  status=LifecycleRule.ENABLED,
                                                  expiration=LifecycleExpiration(days=1))

        lifecycle = BucketLifecycle([wefe_process_expire_rule, wefe_transfer_expire_rule])
        self._bucket.put_bucket_lifecycle(lifecycle)

    def _generate_file_path(self, partition, file_data_count=-1):
        """

        Generate file path

        Parameters
        ----------
        partition: partition index
        file_data_count: the amount of data contained in the file, -1 indicates unknown

        Returns
        -------

        """
        # return f'{self._namespace}/{self._name}/{partition}/{uuid.uuid1()}_cnt{file_data_count}'
        return self.generate_file_relative_path(self._namespace, self._name, partition, file_data_count)

    def _get_partition_by_file_path(self, object_key: str):
        p = int(object_key.split('/')[2])
        return p

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

    def _get_file_dir(self, partition=None):
        if partition is None:
            return f'{self._namespace}/{self._name}'
        else:
            return f'{self._namespace}/{self._name}/{partition}'

    def get_optimal_endpoint_from_temp_auth(self, temp_auth):
        """
        Get the best endpoint from temp auth

        first to use internal endpoint whether meet the requirements

        Parameters
        ----------
        temp_auth

        Returns
        -------

        """
        internal_end_point = temp_auth.get("temp_auth_internal_end_point")

        # if in fc environment, and then determines to use internal address whether in the same region
        if RuntimeInstance.BACKEND is not None and RuntimeInstance.BACKEND.is_fc():
            region = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_REGION)
            if region in internal_end_point:
                return internal_end_point
        else:
            # try connecting to see if can connect
            if network_utils.check_endpoint_is_connected(internal_end_point):
                return internal_end_point

        end_point = temp_auth.get("temp_auth_end_point")
        return end_point

    def _get_bucket(self):

        if self._cloud_store_temp_auth is None or self._cloud_store_temp_auth == "":
            end_point = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OSS_INTERNAL_ENDPOINT)
            access_key_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID)
            key_secret = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_KEY_SECRET)
            bucket_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OSS_BUCKET_NAME)

            auth = oss2.Auth(access_key_id, key_secret)
            bucket = oss2.Bucket(auth, end_point, bucket_name)

        else:
            # Initialize with temporary authorization
            temp_auth = self._cloud_store_temp_auth
            temp_access_key_id = temp_auth.get("temp_access_key_id")
            temp_access_key_secret = temp_auth.get("temp_access_key_secret")
            temp_auth_bucket_name = temp_auth.get("temp_auth_bucket_name")
            temp_access_end_point = self.get_optimal_endpoint_from_temp_auth(temp_auth)
            sts_token = temp_auth.get("sts_token")

            auth = oss2.StsAuth(temp_access_key_id,
                                temp_access_key_secret,
                                sts_token)

            bucket = oss2.Bucket(auth, temp_access_end_point, temp_auth_bucket_name)

        return bucket

    def _is_fc_env(self):
        env_dist = os.environ
        return int(env_dist.get('IN_FC_ENV') or 0) == 1

    def put(self, k, v, use_serialize=True):
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        p = self.hash_key_to_partition(k_bytes, self._partitions)
        self.write_partition_data(p, [(k, v)])
        return True

    def _generate_each_batch_data(self, kv_list: Iterable, each_file_data_count=-1, use_serialize=True):
        """

        Generate each batch data

        Parameters
        ----------
        kv_list
        each_file_data_count：amount of data per oss file. -1=intelligent judgment
        use_serialize：use serialize

        Returns
        -------
        [partition_index, data_list, namespace, name, partitions, bucket_instance]

        """
        batch_data = [[] for _ in range(self._partitions)]
        batch_data_count_stat = [0 for _ in range(self._partitions)]

        for k, v in kv_list:

            # Estimate the number of data rows in the file through the first row of data
            if each_file_data_count == -1:
                v_bytes = self.kv_to_bytes(v=v, use_serialize=use_serialize)
                each_file_data_count = int(self.OBJECT_FILE_MAX_SIZE / len(v_bytes))

                if each_file_data_count < self.OBJECT_MIN_DATA_COUNT:
                    each_file_data_count = self.OBJECT_MIN_DATA_COUNT
                elif each_file_data_count > self.OBJECT_MAX_DATA_COUNT:
                    each_file_data_count = self.OBJECT_MAX_DATA_COUNT

            k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
            p = self.hash_key_to_partition(k_bytes, self._partitions)
            batch_data[p].append((k, v))
            batch_data_count_stat[p] = batch_data_count_stat[p] + 1

            if batch_data_count_stat[p] == each_file_data_count:
                yield p, batch_data[p], self._namespace, self._name, self._partitions, self._bucket
                batch_data[p] = []
                batch_data_count_stat[p] = 0

        for p in range(self._partitions):
            if batch_data[p]:
                yield p, batch_data[p], self._namespace, self._name, self._partitions, self._bucket

    def write_partition_data(self, partition, partition_data: list):
        object_key = self._generate_file_path(partition, len(partition_data))

        # encapsulated into the protobuf structure
        intermediate_data = intermediate_data_pb2.IntermediateData()

        # for k, v in partition_data:
        #     k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v)
        #     item_data = intermediate_data.intermediateData.add()
        #     item_data.key = k_bytes
        #     item_data.value = v_bytes

        # use batch serialize data
        intermediate_data.dataFlag = consts.IntermediateDataFlag.BATCH_SERIALIZATION
        batch_serialize_data = intermediate_data.serializationData
        batch_serialize_data.value = serialize(partition_data)

        object_val = intermediate_data.SerializeToString()
        self._bucket.put_object(object_key, object_val)

    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000, debug_info=None):
        batch_data = self._generate_each_batch_data(kv_list, use_serialize=use_serialize)

        if self._is_fc_env():
            for item_param_list in batch_data:
                write_partition_data_4poolmap(item_param_list)
        else:
            with multiprocessing.Pool() as pool:
                pool.map(write_partition_data_4poolmap, batch_data)

    def put_if_absent(self, k, v, use_serialize=True):
        self.put(k, v, use_serialize)

    def get(self, k, use_serialize=True, maybe_large_value=False):
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        p = self.hash_key_to_partition(k_bytes, self._partitions)

        for item in self.collect(use_serialize=use_serialize, partition=p, dispersal=False):
            if item[0] == k:
                return item[1]

    def read_each_object(self, obj_key, only_key=False):
        """

        Read the data of each object file

        Parameters
        ----------
        obj_key:
        only_key:

        Returns
        -------
        [k,v] or [k]
        """
        if obj_key:
            object_stream = self._bucket.get_object(obj_key)
            obj = object_stream.read()

            intermediate_data = intermediate_data_pb2.IntermediateData()
            intermediate_data.ParseFromString(obj)

            if intermediate_data.dataFlag == consts.IntermediateDataFlag.BATCH_SERIALIZATION:
                serialization_data = intermediate_data.serializationData
                result_list = deserialize(serialization_data.value)
                for k, v in result_list:
                    if only_key:
                        yield k
                    else:
                        yield k, v
            else:
                for item_data in intermediate_data.intermediateData:
                    if only_key:
                        yield deserialize(item_data.key)
                    else:
                        yield deserialize(item_data.key), deserialize(item_data.value)

    def collect(self, min_chunk_size=0, use_serialize=True, partition=None, dispersal=True,
                debug_info=None, only_key=False) -> list:

        # get the data of the specified partition
        if partition is not None:
            prefix = self._get_file_dir(partition) + "/"
            for obj in oss2.ObjectIterator(self._bucket, prefix=prefix):
                for item in self.read_each_object(obj.key, only_key=only_key):
                    yield item

        # get all data
        else:

            # get all object files, and group by partition
            prefix = self._get_file_dir() + "/"
            object_key_list = []

            # init object name list
            for i in range(self._partitions):
                object_key_list.append([])

            # group by partition
            # [[obj_name1,obj_name2],[obj_name3,obj_nameN]]
            for obj in oss2.ObjectIterator(self._bucket, prefix=prefix):
                object_key_list[self._get_partition_by_file_path(obj.key)].append(obj.key)

            if self._is_fc_env():

                # Each group takes one element to generate a new batch
                for each_batch_object_index in itertools.zip_longest(*object_key_list):
                    param_list = [(item, self._namespace, self._name, self._partitions,
                                   self._cloud_store_temp_auth, only_key)
                                  for item in each_batch_object_index if item is not None]

                    result = []
                    for item_param_list in param_list:
                        result.append(get_object_data_4poolmap(item_param_list))

                    if not dispersal:
                        for item_result in result:
                            for item in item_result:
                                yield item
                    else:
                        # data scattered back
                        for each_zip_data in itertools.zip_longest(*result):
                            for item in each_zip_data:
                                if item is not None:
                                    yield item
            else:

                # Multi-process parallel processing
                with multiprocessing.Pool() as pool:

                    for each_batch_object_index in itertools.zip_longest(*object_key_list):
                        param_list = [(item, self._namespace, self._name, self._partitions,
                                       self._cloud_store_temp_auth, only_key)
                                      for item in each_batch_object_index if item is not None]

                        result = pool.map(get_object_data_4poolmap, param_list)

                        if not dispersal:
                            for item_result in result:
                                for kv in item_result:
                                    yield kv
                        else:
                            # data scattered back
                            for each_zip_data in itertools.zip_longest(*result):
                                for kv in each_zip_data:
                                    if kv is not None:
                                        yield kv

    def delete(self, k, use_serialize=True):
        pass

    def destroy(self):
        pass

    def count(self, partition=None):
        prefix = self._get_file_dir(partition) + "/"
        cnt = 0
        for obj in oss2.ObjectIterator(self._bucket, prefix=prefix):
            cnt += self._get_data_count_in_file(obj.key)
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
        pass


def write_partition_data_4poolmap(param_list):
    """
    Write partition data

    Parameters
    ----------
    param_list：(partition_index, data_list, namespace, name, partitions)

    Returns
    -------

    """
    ins = OssStorage(param_list[2], param_list[3], param_list[4])
    return ins.write_partition_data(param_list[0], param_list[1])


def get_object_data_4poolmap(param_list):
    """

    Get k,v data by object

    Parameters
    ----------
    param_list:(partition_index, data_list, namespace, name, partitions)

    Returns
    -------

    """
    ins = OssStorage(param_list[1], param_list[2], param_list[3], cloud_store_temp_auth=param_list[4])
    return list(ins.read_each_object(param_list[0], only_key=param_list[5]))


if __name__ == '__main__':
    pass
    # oss_ins = OssStorage("test", "20220125", partitions=10)
    # # oss_ins.put("k", "v")
    # print(list(oss_ins.collect()))
