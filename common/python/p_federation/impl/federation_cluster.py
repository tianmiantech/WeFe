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

import json
import time
import typing
import uuid
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Union, Tuple

import grpc

from common.python import RuntimeInstance, Backend
from common.python import session, Federation
from common.python.calculation.fc.fc_source import FCSource
from common.python.calculation.fc.fc_storage import FCStorage
from common.python.common import consts
from common.python.common.consts import NAMESPACE
from common.python.common.consts import TransferAction
from common.python.common.exception.custom_exception import GatewayWhiteListError, GatewayConnectError
from common.python.p_session.base_impl.db_runtime import DBRuntime
from common.python.protobuf.pyproto import gateway_meta_pb2, basic_meta_pb2, gateway_service_pb2_grpc, storage_basic_pb2
from common.python.protobuf.pyproto.gateway_meta_pb2 import TransferMeta
# noinspection PyProtectedMember
from common.python.p_session.base_impl.data_source import _DSource
from common.python.utils import conf_utils
from common.python.utils.clean import Rubbish
from common.python.utils.core_utils import serialize, deserialize
from common.python.utils.log_utils import get_logger
from common.python.utils.member import Member
from common.python.utils.splitable import maybe_split_object
from common.python.db.global_config_dao import GlobalConfigDao

OBJECT_STORAGE_NAME = "__federation__"

ERROR_STATES = [gateway_meta_pb2.CANCELLED, gateway_meta_pb2.ERROR]
REMOTE_FRAGMENT_OBJECT_USE_D_SOURCE = False

_remote_tag_histories = set()
_get_tag_histories = set()
LOGGER = get_logger()


def _await_receive(receive_func, transfer_meta):
    resp_meta = receive_func(transfer_meta)
    while resp_meta.transferStatus != gateway_meta_pb2.COMPLETE:
        if resp_meta.transferStatus in ERROR_STATES:
            raise IOError(
                "receive terminated, state: {}".format(basic_meta_pb2.TransferStatus.Name(resp_meta.transferStatus)))
        resp_meta = receive_func(resp_meta)
    return resp_meta


def _thread_receive(receive_func, name, tag, session_id, src, dst, backend):
    # full session
    session_id = f"{session_id}-{name}-{tag}-{src.role}-{src.member_id}-{dst.role}-{dst.member_id}"
    log_msg = f"src={src}, dst={dst}, name={name}, tag={tag}, session_id={session_id}"
    LOGGER.debug(f"[GET] start: {log_msg}")
    transfer_meta = TransferMeta(sessionId=session_id, tag=tag)

    recv_meta = _await_receive(receive_func, transfer_meta)
    storage_type = GlobalConfigDao.get_function_compute_config().cloud_provider
    transfer_action = recv_meta.extField

    if transfer_action == TransferAction.DSOURCE:

        if backend == Backend.FC:
            LOGGER.debug("remote is DSOURCE object, local is FCSource object")
            if conf_utils.get_comm_config(consts.COMM_CONF_CLOUD_PROVIDER) == consts.CLOUDPROVIDER.ALIYUN:
                LOGGER.debug("local storage type is OSS")
                source = _get_fcsource(recv_meta)
            elif conf_utils.get_comm_config(consts.COMM_CONF_CLOUD_PROVIDER) == consts.CLOUDPROVIDER.TENCENTCLOUD:
                LOGGER.debug("local storage type is COS")
                source = _get_fcsource(recv_meta)
            else:
                raise Exception(f'{storage_type} 暂不支持!')
            LOGGER.debug(
                f"[GET] table ready: src={src}, dst={dst}, name={name}, tag={tag}, session_id={session_id}")
            return source, source, None
        else:
            LOGGER.debug("remote is DSOURCE object, local is DSOURCE object too")
            LOGGER.debug(
                f"[GET] table ready: src={src}, dst={dst}, name={name}, tag={tag}, session_id={session_id}")
            source = _get_source(recv_meta)
            return source, source, None

    if transfer_action == TransferAction.DOBJECT:
        LOGGER.debug(f"[GET] object ready: {log_msg}")
        return deserialize(recv_meta.content.byteData), (None, None), None

    if transfer_action == TransferAction.FCSOURCE:
        if backend == Backend.FC:
            if storage_type == consts.STORAGETYPE.OTS:
                source = _get_fcsource(recv_meta)
                LOGGER.debug("remote storage type is OTS")
            elif conf_utils.get_comm_config(consts.COMM_CONF_CLOUD_PROVIDER) == consts.CLOUDPROVIDER.ALIYUN:
                source = _get_fcsource(recv_meta)
                LOGGER.debug("remote storage type is OSS")
            elif conf_utils.get_comm_config(consts.COMM_CONF_CLOUD_PROVIDER) == consts.CLOUDPROVIDER.TENCENTCLOUD:
                source = _get_fcsource(recv_meta)
                LOGGER.debug("remote storage type is COS")
            else:
                raise Exception(f'{storage_type} 暂不支持, 请检查配置是否正确!')

            LOGGER.debug("remote is FCSource object, local is FCSource object too")
            LOGGER.debug(f"[GET] FCSource ready: {log_msg}")
            return source, source, None
        else:
            LOGGER.debug("remote is FCSource object, local is DSource object")
            fcsource = _get_fcsource(recv_meta)
            LOGGER.debug("loading data from FCSource")
            args = json.loads(recv_meta.content.strData)
            LOGGER.debug(f"json.loads(recv_meta.content.strData) args: {args}")
            source = session.parallelize(fcsource.collect(), include_key=True, partition=args['partitions'])
            first_data = source.first()
            if first_data is None:
                LOGGER.warn('data is None')
            LOGGER.debug(f"[GET] FCSource ready: {log_msg}")
            return source, source, None
    else:
        raise IOError(f"unknown transfer type: {transfer_action}")


def _fragment_tag(tag, idx):
    return f"{tag}.__frag__{idx}"


def _get_source(transfer_content):
    object_data = transfer_content.content.strData
    object_data_json = json.loads(object_data)
    name = object_data_json['dst_name']
    namespace = object_data_json['dst_namespace']
    partitions = object_data_json['partitions']
    db_type = DBRuntime.get_instance().get_storage_session().get_db_type()
    return _DSource(object_data_json['type'], name=name, namespace=namespace, partitions=partitions, db_type=db_type)


def _get_fcsource(transfer_content):
    object_data = transfer_content.content.strData
    object_data_json = json.loads(object_data)
    name = object_data_json['fc_name']
    namespace = object_data_json['fc_namespace']
    fc_partitions = object_data_json['fc_partitions']

    cloud_store_temp_auth = None
    if 'cloud_store_temp_auth' in object_data_json:
        cloud_store_temp_auth = object_data_json['cloud_store_temp_auth']
    if cloud_store_temp_auth is None:
        fcs = FCStorage(namespace, name, fc_partitions)
    else:
        fcs = FCStorage(namespace, name, fc_partitions, cloud_store_temp_auth)

        if consts.STORAGETYPE.OTS in cloud_store_temp_auth['temp_auth_end_point'] or 'tablestore' in \
                cloud_store_temp_auth['temp_auth_end_point']:
            fcs._instance_name = object_data_json['cloud_store_temp_auth']['instance_name']
        elif consts.STORAGETYPE.OSS in cloud_store_temp_auth['temp_auth_end_point'] \
                or consts.STORAGETYPE.COS in cloud_store_temp_auth['temp_auth_end_point']:
            fcs._bucket_name = object_data_json['cloud_store_temp_auth']['temp_auth_bucket_name']

    session_id = RuntimeInstance.SESSION.get_session_id()
    return FCSource(session_id, namespace, name, fc_partitions, fcs)


# noinspection PyProtectedMember
def _get_storage_locator(source):
    return storage_basic_pb2.StorageLocator(type=source._type,
                                            namespace=source._namespace,
                                            name=source._name,
                                            fragment=source._partitions)


def _create_source(name, namespace, persistent=True):
    return DBRuntime.get_instance().table(name=name, namespace=namespace, persistent=persistent)


def _create_fragment_obj_source(namespace, persistent=True):
    dbRuntime = DBRuntime.get_instance()
    name = dbRuntime.generateUniqueId()
    return DBRuntime.get_instance().table(name=name, namespace=namespace, persistent=persistent)


_cache_remote_obj_storage_source = {}
_cache_get_obj_storage_source = {}


def _get_obj_storage_source_name(src, dst, session_id):
    return f"{session_id}_{OBJECT_STORAGE_NAME}.{src.role}-{src.member_id}-{dst.role}-{dst.member_id}"


def _fill_cache(parties, local, session_id):
    for party in parties:
        _cache_remote_obj_storage_source[party] = _create_source(_get_obj_storage_source_name(local, party, session_id),
                                                                 NAMESPACE.PROCESS)


class FederationRuntime(Federation):

    def __init__(self, session_id, runtime_conf, host, port):
        super().__init__(session_id, runtime_conf)
        self.host = host
        self.port = port
        self.retry_time = 3

        self.__pool = ThreadPoolExecutor()

        # init object storage tables
        _fill_cache(self.all_parties, self.local_party, self._session_id)

    def _get_channel(self):
        return grpc.insecure_channel(target=f"{self.host}:{self.port}",
                                     options=[('grpc.max_send_message_length', -1),
                                              ('grpc.max_receive_message_length', -1)])

    def gateway_send(self, transfer_meta):
        last_ex = None
        for i in range(self.retry_time):
            try:
                with self._get_channel() as channel:
                    stub = gateway_service_pb2_grpc.TransferServiceStub(channel)
                    return stub.send(transfer_meta)
            except Exception as ex:
                last_ex = ex
                LOGGER.error(f"gateway_send_error,{ex}")

            time.sleep(1)

        if isinstance(last_ex, grpc.RpcError):
            if last_ex.code() == grpc.StatusCode.PERMISSION_DENIED:
                raise GatewayWhiteListError()
            elif last_ex.code() == grpc.StatusCode.UNAVAILABLE:
                raise GatewayConnectError(host=self.host, port=self.port)

        raise Exception("gateway发送异常，详细请查看日志")

    def gateway_recv(self, transfer_meta):
        last_ex = None
        for i in range(self.retry_time):
            try:
                with self._get_channel() as channel:
                    stub = gateway_service_pb2_grpc.TransferServiceStub(channel)
                    return stub.recv(transfer_meta)
            except Exception as ex:
                last_ex = ex
                LOGGER.error(f"gateway_recv_error,{ex}")

            time.sleep(1)

        if isinstance(last_ex, grpc.RpcError):
            if last_ex.code() == grpc.StatusCode.PERMISSION_DENIED:
                raise GatewayWhiteListError()
            elif last_ex.code() == grpc.StatusCode.UNAVAILABLE:
                raise GatewayConnectError(host=self.host, port=self.port)

        raise Exception("gateway接收异常，详细请查看日志")

    def remote(self, obj, name: str, tag: str, parties: Union[Member, list]):
        if obj is None:
            raise EnvironmentError(f"federation try to remote None to {parties} with name {name}, tag {tag}")

        if isinstance(parties, Member):
            parties = [parties]

        for party in parties:
            if (name, tag, party) not in _remote_tag_histories:
                # raise EnvironmentError(f"remote duplicate tag {(name, tag)}")
                _remote_tag_histories.add((name, tag, party))

        self._remote_side_auth(name=name, parties=parties)
        rubbish = Rubbish(name, tag)

        # if obj is a dsource, remote it
        if isinstance(obj, _DSource):
            obj.set_gc_disable()
            for index, party in enumerate(parties):
                print(f"dsource:{obj}")
                self._send(transfer_type=TransferAction.DSOURCE, name=name, tag=tag, dst_party=party, rubbish=rubbish,
                           source=obj, index=index)
            return rubbish
        # if obj is a fcsource, remote it
        elif isinstance(obj, FCSource):
            if obj._fcs:
                for index, party in enumerate(parties):
                    log_msg = f"src={self.local_party}, dst={party}, name={name}, tag={tag}, " \
                              f"session_id={self._session_id}"
                    LOGGER.debug(f"[REMOTE] sending FCSource: {log_msg}")

                    ret = self._send(transfer_type=TransferAction.FCSOURCE, name=name, tag=tag, dst_party=party,
                                     rubbish=None,
                                     source=obj, index=index)
                    LOGGER.debug(f"[REMOTE] send result:{ret}")
                return None
            else:
                obj.set_gc_disable()
                for index, party in enumerate(parties):
                    print(f"dsource:{obj}")
                    self._send(transfer_type=TransferAction.DSOURCE, name=name, tag=tag, dst_party=party,
                               rubbish=rubbish,
                               source=obj, index=index)
                return rubbish

        for index, party in enumerate(parties):
            log_msg = f"src={self.local_party}, dst={party}, name={name}, tag={tag}, session_id={self._session_id}"
            LOGGER.debug(f"[REMOTE] sending object: {log_msg}")
            obj_source = _cache_remote_obj_storage_source[party]

            # remote object or remote fragment header
            LOGGER.debug("[REMOTE] send result:")
            LOGGER.debug(
                self._send(transfer_type=TransferAction.DOBJECT, name=name, tag=tag, dst_party=party, rubbish=rubbish,
                           source=obj_source, obj=obj, index=index))
            # if not fragments:
            LOGGER.debug(f"[REMOTE] object done: {log_msg}")

        return rubbish

    def _receive_async(self, name: str, tag: str, parties: list) -> dict:
        self._get_side_auth(name=name, parties=parties)

        futures = {
            self.__pool.submit(_thread_receive, self.gateway_recv, name, tag,
                               self._session_id, party,
                               self.local_party, str(RuntimeInstance.BACKEND)): party for party in parties}
        return futures

    def async_get(self, name: str, tag: str, parties: list) -> typing.Generator:
        rubbish = Rubbish(name, tag)
        futures = self._receive_async(name, tag, parties)
        for future in as_completed(futures):
            party = futures[future]
            obj, head, frags = future.result()
            if isinstance(obj, _DSource):
                rubbish.add_table(obj)
                yield party, obj
            else:
                yield party, obj
        yield None, rubbish

    def get(self, name: str, tag: str, parties: Union[Member, list]) -> Tuple[list, Rubbish]:
        if isinstance(parties, Member):
            parties = [parties]

        for party in parties:
            if (name, tag, party) in _get_tag_histories:
                raise EnvironmentError(f"get duplicate tag {(name, tag)}")
            _remote_tag_histories.add((name, tag, party))

        rtn = {}
        rubbish = None
        for p, v in self.async_get(name, tag, parties):
            print(f"get_p:{p},v:{v}")
            if p is not None:
                if v is None:
                    raise EnvironmentError(f"federation get None from {p} with name {name}, tag {tag}")
                rtn[p] = v
            else:
                rubbish = v
        return [rtn[p] for p in parties], rubbish

    def _send(self, transfer_type, name: str, tag: str, dst_party: Member, rubbish, source, obj=None, index=None):
        content = gateway_meta_pb2.Content(byteData=serialize(obj))
        transfer_process = None

        if transfer_type == TransferAction.DOBJECT:
            transfer_process = consts.GatewayTransferProcess.MEMORY_PROCESS
        elif transfer_type == TransferAction.DSOURCE:
            transfer_process = consts.GatewayTransferProcess.DSOURCE_PROCESS

            # Todo 这里需要添加对存储类型的判断可能需要board修改代码
            dst_backend = RuntimeInstance.get_member_backend(dst_party.get_member_id())
            dst_fc_provider = RuntimeInstance.get_member_fc_provider(dst_party.get_member_id())

            if dst_backend == Backend.FC:
                if dst_fc_provider == "aliyun":
                    dst_storage_type = consts.STORAGETYPE.OSS
                elif dst_fc_provider == "tencentcloud":
                    dst_storage_type = consts.STORAGETYPE.COS
            else:
                dst_storage_type = consts.STORAGETYPE.CLICKHOUSE

            object_data = {
                "namespace": source.get_namespace(),
                "name": source.get_name(),
                "dst_namespace": source.get_namespace(),
                "dst_name": f"{source.get_name()}-dst-{index}-",
                "dst_storage_type": dst_storage_type,

                # The members have agreed well, and the intermediate data is stored in `wefe_process`,
                # When your own member is ck and the other member is ots,
                # own member needs to specify in advance the other member’s gateway to upload ots information
                # include fc_namespace,fc_name,fc_partitions
                "fc_namespace": consts.NAMESPACE.PROCESS,
                "fc_name": self._session_id + '_' + str(uuid.uuid1()),
                "fc_partitions": RuntimeInstance.get_fc_partition(),
                "type": source.get_type(),
                "partitions": source.get_partitions(),
                "in_place_computing": source.get_in_place_computing()
            }

            LOGGER.debug(f"[REMOTE] dsource info: {object_data}")
            content = gateway_meta_pb2.Content(strData=json.dumps(object_data))
            rubbish.add_table(source)
        elif transfer_type == TransferAction.FCSOURCE:
            transfer_process = consts.GatewayTransferProcess.MEMORY_PROCESS
            fcs = source.fcs()
            fcs_cloud_store_temp_auth = None

            # Read temporary authorization information
            if fcs._cloud_store_temp_auth is None:
                fcs_cloud_store_temp_auth = fcs.create_cloud_store_temp_auth()

            object_data = {
                "cloud_store_temp_auth": fcs_cloud_store_temp_auth,
                "fc_namespace": fcs.get_namespace(),
                "fc_name": fcs.get_name(),
                "partitions": fcs.get_partitions(),
                "fc_partitions": fcs.get_partitions()
            }

            LOGGER.debug(f"[REMOTE] fcsource info: {object_data}")
            content = gateway_meta_pb2.Content(strData=json.dumps(object_data))

        session_id = f"{self._session_id}-{name}-{tag}-{self.local_party.role}-{self.local_party.member_id}-" \
                     f"{dst_party.role}-{dst_party.member_id}"

        log_msg = f"src={self.local_party.role}, dst={dst_party.role}, name={name}, tag={tag}, session_id={session_id}"
        LOGGER.debug(f"[REMOTE] sending table: {log_msg}")

        member_id = dst_party.member_id
        dst = gateway_meta_pb2.Member(memberId=str(member_id))
        transfer_meta = TransferMeta(sessionId=session_id, tag=tag, dst=dst, content=content,
                                     taggedVariableName=None, processor=transfer_process,
                                     extField=transfer_type)
        LOGGER.debug(f"[REMOTE] table done: {log_msg}")
        ret = self.gateway_send(transfer_meta)
        return ret


if __name__ == '__main__':
    pass
