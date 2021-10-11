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

from common.python.db.global_config_dao import GlobalConfigDao
from common.python.utils.log_utils import LoggerFactory
import requests
from requests import Response
from common.python.utils.core_utils import current_timestamp

BOARD_BASE_URL = GlobalConfigDao.getBoardConfig().intranet_base_uri


class VisualFLService:
    LOG = LoggerFactory.get_logger("VisualFLService")

    @staticmethod
    def request(name, params):
        """
        资源申请
        params = {
            'job_id': '',
            'task_id': '',
            'job_type': '默认paddle_fl',
            'role': '',
            'member_id': '',
            'callback_url': '回调地址'
        }"""
        if name == 'apply':
            VisualFLService._request("/apply", params)
        elif name == 'submit':
            VisualFLService._request("/submit", params)

    @staticmethod
    def _request(api, data):
        """
        向 board 服务发送请求
        """
        url = BOARD_BASE_URL + api

        # 发送请求
        start_time = current_timestamp()
        VisualFLService.LOG.info(
            "visualfl request url:{}, {}".format(url, str(data))
        )

        try:
            response: Response = requests.post(url, json=data)
        except Exception as e:
            VisualFLService.LOG.error(
                "visualfl response fail url:{}, {}".format(url, repr(e))
            )
            return None

        spend = current_timestamp() - start_time

        # http 异常
        if response.status_code < 200 or response.status_code > 299:
            VisualFLService.LOG.error(
                "visualfl response fail({}ms) url:{}, {}, {}".format(spend, url, response.status_code, response.text)
            )
            return None

        # 业务异常
        root = response.json()
        code = root.get("code")
        message = root.get("message")
        data = root.get("data")

        if code != 0:
            VisualFLService.LOG.error(
                "visualfl response fail({}ms) url:{}, {}, {}".format(spend, url, message, response.text)
            )
            return None
        else:
            VisualFLService.LOG.info(
                "visualfl response success({}ms) url:{}, {}".format(spend, url, response.text)
            )
            return data
