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

from qcloud_cos import CosConfig
from qcloud_cos import CosS3Client
import sys

from common.python import RuntimeInstance
from common.python.common import consts
from common.python.protobuf.pyproto import intermediate_data_pb2
from common.python.storage.fc_storage import FCStorage
from common.python.utils import log_utils, conf_utils, network_utils
from common.python.utils.core_utils import deserialize, serialize


LOGGER = log_utils.get_logger()

class CosBucket(CosS3Client):
    """
    Cos Bucket
    """

    def __init__(self, conf, retry=1, session=None, bucket_name=None):
        super().__init__(conf, retry, session)  # 使用super() 调用父类对象
        self._bucket_name = bucket_name

    def get_bucket_name(self):
        return self._bucket_name

class CosStorage(FCStorage):
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
        self._bucket_client = bucket_client or self._get_bucket_client()
        self._use_multiprocess = True

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

    def _get_bucket_client(self):
        if self._cloud_store_temp_auth is None or self._cloud_store_temp_auth == "":
            # 1. 设置用户属性, 包括 secret_id, secret_key, region等。Appid 已在CosConfig中移除，请在参数 Bucket 中带上 Appid。Bucket 由 BucketName-Appid 组成
            secret_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_SCF_ACCESS_KEY_ID)# 替换为用户的 SecretId，请登录访问管理控制台进行查看和管理，https://console.cloud.tencent.com/cam/capi
            secret_key = conf_utils.get_comm_config(consts.COMM_CONF_KEY_SCF_KEY_SECRET)# 替换为用户的 SecretKey，请登录访问管理控制台进行查看和管理，https://console.cloud.tencent.com/cam/capi
            region = conf_utils.get_comm_config(consts.COMM_CONF_KEY_SCF_REGION)# 替换为用户的 region，已创建桶归属的region可以在控制台查看，https://console.cloud.tencent.com/cos5/bucket
            # COS支持的所有region列表参见https://cloud.tencent.com/document/product/436/6224
            token = None  # 如果使用永久密钥不需要填入token，如果使用临时密钥需要填入，临时密钥生成和使用指引参见https://cloud.tencent.com/document/product/436/14048
            scheme = 'https'  # 指定使用 http/https 协议来访问 COS，默认为 https，可不填
            config = CosConfig(Region=region, SecretId=secret_id, SecretKey=secret_key, Token=token, Scheme=scheme)
            bucket = CosBucket(config, bucket_name=conf_utils.get_comm_config(consts.COMM_CONF_KEY_SCF_COS_BUCKET_NAME))
        else:
            # Initialize with temporary authorization
            temp_auth = self._cloud_store_temp_auth
            temp_access_key_id = temp_auth.get("temp_access_key_id")
            temp_access_key_secret = temp_auth.get("temp_access_key_secret")
            temp_auth_bucket_name = temp_auth.get("temp_auth_bucket_name")
            sts_token = temp_auth.get("sts_token")
            region = temp_auth.get("temp_auth_internal_end_point")
            scheme = 'https'

            config = CosConfig(Region=region, SecretId=temp_access_key_id, SecretKey=temp_access_key_secret, Token=sts_token, Scheme=scheme)
            bucket = CosBucket(config, bucket_name=temp_auth_bucket_name)
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
        each_file_data_count：amount of data per cos file. -1=intelligent judgment
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
                yield p, batch_data[p], self._namespace, self._name, self._partitions, self._bucket_client
                batch_data[p] = []
                batch_data_count_stat[p] = 0

        for p in range(self._partitions):
            if batch_data[p]:
                yield p, batch_data[p], self._namespace, self._name, self._partitions, self._bucket_client

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
        self._bucket_client.put_object(Bucket=self._bucket_client.get_bucket_name(), Body=object_val, Key=object_key)

    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000, debug_info=None):
        batch_data = self._generate_each_batch_data(kv_list, use_serialize=use_serialize)

        if self._is_fc_env():
            for item_param_list in batch_data:
                self.write_partition_data(item_param_list[0], item_param_list[1])
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
            object_stream = self._bucket_client.get_object(Bucket=self._bucket_client.get_bucket_name(), Key=obj_key)
            obj = object_stream['Body'].get_raw_stream().read()

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
            response = self._bucket_client.list_objects(Bucket=self._bucket_client.get_bucket_name(), Prefix=prefix)
            if 'Contents' in response:
                for obj in response['Contents']:
                    if obj['Key'] == prefix:
                        continue
                    for item in self.read_each_object(obj['Key'], only_key=only_key):
                        yield item

            # for obj in oss2.ObjectIterator(self._bucket, prefix=prefix):
            #     for item in self.read_each_object(obj.key, only_key=only_key):
            #         yield item

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
            # for obj in oss2.ObjectIterator(self._bucket, prefix=prefix):
            #     object_key_list[self._get_partition_by_file_path(obj.key)].append(obj.key)

            response = self._bucket_client.list_objects(Bucket=self._bucket_client.get_bucket_name(), Prefix=prefix)
            if 'Contents' in response:
                for obj in response['Contents']:
                    if obj['Key'] == prefix:
                        continue
                    object_key_list[self._get_partition_by_file_path(obj['Key'])].append(obj['Key'])

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
        LOGGER.info("++++++")
        LOGGER.info(self._bucket_client.get_bucket_name())
        response = self._bucket_client.list_objects(Bucket=self._bucket_client.get_bucket_name(),Prefix=prefix)
        if 'Contents' in response:
            for obj in response['Contents']:
                if obj['Key'] == prefix:
                    continue
                cnt += self._get_data_count_in_file(obj['Key'])
        return cnt

    def each_partition_count(self):
        prefix = self._get_file_dir() + "/"
        partition_count = dict([(i, 0) for i in range(self._partitions)])
        # for obj in oss2.ObjectIterator(self._bucket, prefix=prefix):
        #     p = int(obj.key.replace("\\", "/").split("/")[2])
        #     cnt = self._get_data_count_in_file(obj.key)
        #     partition_count[p] = partition_count[p] + cnt

        response = self._bucket_client.list_objects(Bucket=self._bucket_client.get_bucket_name(), Prefix=prefix)
        if 'Contents' in response:
            for obj in response['Contents']:
                if obj['Key'] == prefix:
                    continue
                p = int(obj['Key'].replace("\\", "/").split("/")[2])
                cnt = self._get_data_count_in_file(obj['Key'])
                partition_count[p] = partition_count[p] + cnt
        return partition_count

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
    ins = CosStorage(param_list[2], param_list[3], param_list[4])
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
    ins = CosStorage(param_list[1], param_list[2], param_list[3], cloud_store_temp_auth=param_list[4])
    return list(ins.read_each_object(param_list[0], only_key=param_list[5]))


if __name__ == '__main__':
    secret_id = conf_utils.get_comm_config(
        consts.COMM_CONF_KEY_SCF_ACCESS_KEY_ID)  # 替换为用户的 SecretId，请登录访问管理控制台进行查看和管理，https://console.cloud.tencent.com/cam/capi
    print(secret_id)
    # pass
    cos_ins = CosStorage("wefe_process", "9acb8c55420d44daa076418dbad767ad_provider_DataIO_16195987735668056_provider_d27a9d1a3a7c46019d38684a530a86ca_78caf0cc-d199-11ec-8c5d-0242ac1b0004", partitions=1)
    print(cos_ins.count())
    cos_ins.put("k2", "v2")
    print(list(cos_ins.collect()))
    print(cos_ins.get())
