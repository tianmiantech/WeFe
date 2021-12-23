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
import json

from common.python.db.global_config_dao import GlobalConfigDao
from flow.service.board.board_service import BoardService
from flow.service.gateway.gateway_service import GatewayService
from flow.settings import GATEWAY_INTRANET_HOST, GATEWAY_INTRANET_PORT
from flow.web.utils.const import *
from datetime import datetime


class StatusService:

    @staticmethod
    def all_status():
        """
        Check all of service status

        Returns
        -------
        { ServiceName ：Data }
        """
        res = {
            "available": True,
            "list": [],
            "message": '',
            "errorServiceType": ''
        }
        res['list'].append(BoardStatusService.flow_board_status())
        res['list'].append(GatewayStatusService.flow_gateway_status_json())

        for item in res['list']:
            if not item[JsonField.SUCCESS]:
                res["available"] = False
                res["message"] = item[JsonField.MESSAGE]
                res["errorServiceType"] = item[JsonField.SERVICE]

        return res


class BoardStatusService:
    """
    Board Status
    """

    @staticmethod
    def flow_board_status():
        desc = '检查 flow 对 board 服务的访问是否正常'
        api = '/service/alive'
        data = {}
        resp = BoardService.request(api, data)
        return get_result_json(
            des=desc,
            mess=resp[1],
            service='BoardService',
            spend=resp[2],
            success=resp[0],
            value=GlobalConfigDao.getBoardConfig().intranet_base_uri
        )


class GatewayStatusService:
    """
    Gateway Status
    """

    @staticmethod
    def flow_gateway_status():
        """
        check connectivity to gateway service
        """
        start = datetime.now()
        check_info = GatewayService.alive()
        end = datetime.now()
        spend = round((end - start).total_seconds() * 1000)
        return check_info[1], spend

    @staticmethod
    def flow_gateway_status_json():
        desc = "检查 flow 对 gateway 服务的访问是否正常"
        res = GatewayStatusService.flow_gateway_status()
        if res[0]["code"] == 0:
            return get_result_json(
                des=desc,
                mess=ServiceStatusMessage.SUCCESS_MESSAGE,
                service=ServiceName.GATEWAY_SERVICE,
                spend=GatewayStatusService.flow_gateway_status()[1],
                success=True,
                value=GATEWAY_INTRANET_HOST + ':' + GATEWAY_INTRANET_PORT
            )
        else:
            return get_result_json(
                des=desc,
                mess=GatewayStatusService.flow_gateway_status()[0]["message"],
                service=ServiceName.GATEWAY_SERVICE,
                spend=GatewayStatusService.flow_gateway_status()[1],
                success=False,
                value=GATEWAY_INTRANET_HOST + ':' + GATEWAY_INTRANET_PORT
            )


def get_result_json(des, mess, service, spend, success, value):
    return {
        JsonField.DESC: des,
        JsonField.MESSAGE: mess,
        JsonField.SERVICE: service,
        JsonField.SPEND: spend,
        JsonField.SUCCESS: success,
        JsonField.VALUE: value
    }


if __name__ == '__main__':
    StatusService.all_status()
