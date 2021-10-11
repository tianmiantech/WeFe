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

from flow.service.gateway.gateway_service import GatewayService
from flow.web.utils.const import *


def all_status():
    """
    Check all of service status

    Returns
    -------
    { ServiceName ：Data }
    """
    res = {}
    res.update({ServiceType.BOARD: BoardStatusService.flow_board_status()})
    res.update({ServiceType.GATEWAY: GatewayStatusService.flow_gateway_status_json()})
    return res


class BoardStatusService:
    """
    Board Status
    """

    @staticmethod
    def flow_board_status():
        return get_success_json()


class GatewayStatusService:
    """
    Gateway Status
    """

    @staticmethod
    def flow_gateway_status():
        """
        check connectivity to gateway service
        """
        return GatewayService.alive()

    @staticmethod
    def flow_gateway_status_json():
        if GatewayStatusService.flow_gateway_status() is True:
            return get_success_json()


def get_success_json():
    return {
        JsonField.CODE: ServiceStatusCode.SUCCESS_CODE,
        JsonField.MESSAGE: ServiceStatusMessage.SUCCESS_MESSAGE
    }


if __name__ == '__main__':
    print(all_status())
