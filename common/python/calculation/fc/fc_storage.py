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

import datetime
import json
from typing import Iterable

from aliyunsdkcore.client import AcsClient
from aliyunsdksts.request.v20150401.AssumeRoleRequest import AssumeRoleRequest

from common.python.common import consts
from common.python.utils import conf_utils


class FCStorage(object):
    """
    FC Storage
    """

    _OTS = consts.STORAGETYPE.OTS
    _OSS = consts.STORAGETYPE.OSS
    storage = None
    TIME_FORMAT = '%Y-%m-%d %H:%M:%S'

    def __init__(self, namespace, name, partitions=1, cloud_store_temp_auth=None):

        self._partitions = partitions
        self._name = name
        self._namespace = namespace
        self._cloud_store_temp_auth = cloud_store_temp_auth
        self._storage_type = self.get_fc_storage_type()
        self._instance_name = None
        self._bucket_name = None

        if self._OTS == self._storage_type:
            from common.python.storage.impl.ots_storage import OTS
            self.storage = OTS(namespace=namespace, name=name, partitions=partitions,
                               cloud_store_temp_auth=cloud_store_temp_auth)
            if self._cloud_store_temp_auth:
                self._instance_name = self.get_temp_storage_name()
            else:
                self._instance_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OTS_INSTANCE_NAME)

        elif self._OSS == self._storage_type:
            from common.python.storage.impl.oss_storage import OssStorage
            self.storage = OssStorage(namespace=namespace, name=name, partitions=partitions,
                                      cloud_store_temp_auth=cloud_store_temp_auth)
            if self._cloud_store_temp_auth:
                self._bucket_name = self.get_temp_storage_name()
            else:
                self._bucket_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OSS_BUCKET_NAME)
        else:
            raise NotImplementedError(f'not supported {self._storage_type} fc storage')

    def get_fc_storage_type(self):
        if self._cloud_store_temp_auth:
            if consts.STORAGETYPE.OTS in self._cloud_store_temp_auth['temp_auth_end_point'] or 'tablestore' in \
                    self._cloud_store_temp_auth['temp_auth_end_point']:
                return consts.STORAGETYPE.OTS
            elif consts.STORAGETYPE.OSS in self._cloud_store_temp_auth['temp_auth_end_point']:
                return consts.STORAGETYPE.OSS
        else:
            return conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_STORAGE_TYPE)

    def get_temp_storage_name(self):
        if self._storage_type == consts.STORAGETYPE.OTS:
            return self._cloud_store_temp_auth['instance_name']
        elif self._storage_type == consts.STORAGETYPE.OSS:
            return self._cloud_store_temp_auth['temp_auth_bucket_name']

    def to_dict(self):
        return {'name': self._name,
                'namespace': self._namespace,
                'partitions': self._partitions,
                'storage_type': self._storage_type,
                'create_time': datetime.datetime.now().strftime(self.TIME_FORMAT),
                'instance_name': self._instance_name,
                'bucket_name': self._bucket_name}

    def get_cloud_store_temp_auth(self):
        return self._cloud_store_temp_auth

    def create_cloud_store_temp_auth(self):

        # TODO：check whether the sts_token expires, if it does not expire, it will not be regenerated
        access_key_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID)
        access_key_secret = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_KEY_SECRET)
        role_arn = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_ROLE_ARN)
        region_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_REGION)
        temp_auth_internal_end_point = conf_utils.get_comm_config(
            consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_INTERNAL_END_POINT)
        temp_auth_end_point = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_END_POINT)
        role_session_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_ROLE_SESSION_NAME)
        temp_auth_duration_seconds = conf_utils.get_comm_config(
            consts.COMM_CONF_KEY_FC_CLOUD_STORE_TEMP_AUTH_DURATION_SECONDS)

        # build an Alibaba Cloud client to initiate requests.
        client = AcsClient(access_key_id, access_key_secret, region_id)
        request = AssumeRoleRequest()
        # set args
        request.set_RoleArn(role_arn)
        request.set_RoleSessionName(role_session_name)
        request.set_DurationSeconds(temp_auth_duration_seconds)
        response = client.do_action_with_exception(request)
        result = json.loads(str(response, encoding='utf-8'))
        temp_access_key_id = result.get('Credentials').get('AccessKeyId')
        temp_access_key_secret = result.get('Credentials').get('AccessKeySecret')
        sts_token = result.get('Credentials').get('SecurityToken')

        cloud_store_temp_auth = {
            "temp_access_key_id": temp_access_key_id,
            "temp_access_key_secret": temp_access_key_secret,
            "sts_token": sts_token,
            "temp_auth_internal_end_point": temp_auth_internal_end_point,
            "temp_auth_end_point": temp_auth_end_point
        }

        # when fc cloud storage is ots, with instance instance
        if self._storage_type == consts.STORAGETYPE.OTS:
            instance_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OTS_INSTANCE_NAME)
            cloud_store_temp_auth["instance_name"] = instance_name
        elif self._storage_type == consts.STORAGETYPE.OSS:
            bucket_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OSS_BUCKET_NAME)
            cloud_store_temp_auth["temp_auth_bucket_name"] = bucket_name

        return cloud_store_temp_auth

    @classmethod
    def from_fcs_info(cls, info: dict):
        storage_type = info.get('storage_type')
        if storage_type in [cls._OTS, cls._OSS]:
            # If more than 20 hours, the cache will be invalid
            create_time = datetime.datetime.strptime(info.get('create_time'), FCStorage.TIME_FORMAT)
            seconds = (datetime.datetime.now() - create_time).seconds
            if seconds > 3600 * 20:
                return None
            if storage_type == cls._OTS and info.get("instance_name") != conf_utils.get_comm_config(
                    consts.COMM_CONF_KEY_FC_OTS_INSTANCE_NAME):
                return None
            if storage_type == cls._OSS and info.get("bucket_name") != conf_utils.get_comm_config(
                    consts.COMM_CONF_KEY_FC_OSS_BUCKET_NAME):
                return None
            return FCStorage(info.get('namespace'), info.get('name'), info.get('partitions'))

    def get_partitions(self):
        return self._partitions

    def get_name(self):
        return self._name

    def get_namespace(self):
        return self._namespace

    def get_storage_type(self):
        return self._storage_type

    def put_all(self, data: Iterable, debug_info=None):
        return self.storage.put_all(data, debug_info=debug_info)

    def collect(self, partition=None, debug_info=None):
        return self.storage.collect(partition=partition, debug_info=debug_info)

    def take(self, n=1, partition=None):
        return self.storage.take(n, partition=partition)

    def first(self, partition=None):
        return self.storage.first(partition=partition)

    def count(self, partition=None):
        return self.storage.count(partition=partition)

    def put(self, k, v):
        return self.storage.put(k, v)


if __name__ == '__main__':
    pass
