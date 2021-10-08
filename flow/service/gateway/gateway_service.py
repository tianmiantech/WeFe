# Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import traceback

import grpc

from common.python.common.consts import GatewayTransferProcess
from common.python.db.global_config_dao import GlobalConfigDao
from common.python.protobuf.pyproto import gateway_meta_pb2
from common.python.protobuf.pyproto.gateway_meta_pb2 import TransferMeta
from common.python.utils import network_utils
from common.python.utils.core_utils import get_commit_id
from common.python.utils.log_utils import LoggerFactory
from flow.settings import MEMBER_ID, JOB_GRPC
from flow.web.utils.const import *


class GatewayService:
    logger = LoggerFactory.get_logger("GatewayService")

    @staticmethod
    def report_ip_to_white_list():
        """
        Report the local IP to the gateway ip whitelist
        """
        GatewayService.logger.info("开始上报IP地址到 gateway")

        try:
            # Intranet IP
            local_ip = network_utils.get_local_ip()
            # Change the last digit of the intranet ip to fuzzy match
            array = local_ip.split('.')
            array[len(array) - 1] = '*'
            local_ip = '.'.join(array)
            GlobalConfigDao.append_gateway_white_ip(local_ip, "flow 内网IP地址，由 flow 自主上报。")

            # Internet IP
            internet_ip = network_utils.get_internet_ip()
            GlobalConfigDao.append_gateway_white_ip(internet_ip, "flow 外网IP地址，由 flow 自主上报。")

            GatewayService.send_to_myself(GatewayTransferProcess.REFRESH_SYSTEM_CONFIG_CACHE_PROCESS)

            GatewayService.logger.info("上报IP地址完成.")
        except Exception as e:
            traceback.print_exc()
            GatewayService.logger.exception("上报IP地址异常:%s", e)

    @staticmethod
    def alive() -> bool:
        """
        Check the connectivity to the gateway
        """
        result = GatewayService.send_to_myself(GatewayTransferProcess.GATEWAY_ALIVE_PROCESS)
        return result[JsonField.CODE] == 0

    @staticmethod
    def send_to_myself(processor) -> dict:
        """
        Send messages to self gateway service
        """
        return GatewayService.send(MEMBER_ID, processor)

    @staticmethod
    def send(dst_member_id, processor, data="") -> dict:
        """
        Sent message to gateway service

        Parameters
        ----------
        dst_member_id: str
            Specify the member id to receive the message

        processor: str
            The processor defined by the gateway service, see the enumeration: GatewayTransferProcess

        data: str
            The data sent can be empty

        Returns
        -------
        The response from gateway service
        """

        if (not GatewayConfig.HOST) or (not GatewayConfig.PORT):
            return {
                JsonField.CODE: ServiceStatusCode.REMOTE_SERVICE_ERROR,
                JsonField.MESSAGE: ServiceStatusMessage.ADDRESS_IS_EMPTY
            }

        result = {
            JsonField.CODE: ServiceStatusCode.SUCCESS_CODE,
            JsonField.MESSAGE: ServiceStatusMessage.SUCCESS_MESSAGE
        }
        try:
            dst = gateway_meta_pb2.Member(memberId=dst_member_id)
            transfer_meta = TransferMeta(
                sessionId=get_commit_id(),
                dst=dst,
                content=gateway_meta_pb2.Content(objectData=data),
                taggedVariableName=None,
                processor=processor
            )
            JOB_GRPC.send(transfer_meta)
        except grpc.RpcError as error:
            # Catch exceptions based on custom protocol
            print(error.code())
            if str(error.code()) == GrpcStatusMessage.UNAVAILABLE:
                return {
                    JsonField.CODE: ServiceStatusCode.REMOTE_SERVICE_ERROR,
                    JsonField.MESSAGE: ServiceStatusMessage.REMOTE_SERVICE_ERROR_MESSAGE
                }
            elif str(error.code()) == GrpcStatusMessage.PERMISSION_DENIED:
                return {
                    JsonField.CODE: ServiceStatusCode.REMOTE_SERVICE_ERROR,
                    JsonField.MESSAGE: ServiceStatusMessage.WHITELIST_NOT_ADDED
                }
            else:
                return {
                    JsonField.CODE: ServiceStatusCode.REMOTE_SERVICE_ERROR,
                    JsonField.MESSAGE: str(error)
                }
        finally:
            GatewayService.logger.debug("[REMOTE] send result:%s", result["message"])

        return result
