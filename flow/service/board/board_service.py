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
from typing import List

import requests
from requests import Response

from common.python.db.db_models import GlobalSetting
from common.python.db.global_config_dao import GlobalConfigDao
from common.python.utils.core_utils import current_timestamp
from common.python.utils.log_utils import LoggerFactory
from flow.service.board.board_output import JobProgressOutput
from flow.utils.bean_util import BeanUtil
from flow.web.utils.const import JsonField
import base64
import json
from Crypto.Hash import SHA1
from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5 as PKCS1_v1_5_sign

BOARD_BASE_URL = GlobalConfigDao.getBoardConfig().intranet_base_uri


class BoardService:
    LOG = LoggerFactory.get_logger("BoardService")

    @staticmethod
    def get_job_progress(job_id) -> List[JobProgressOutput]:
        """
        get job progress

        Parameters
        ----------
        job_id: str

        Returns
        -------
        The JobProgressOutput entity
        """
        params = {"job_id": job_id}
        data = BoardService.request("/flow/job/get_progress", params)

        if data[JsonField.SUCCESS] is False:
            return []
        output = []

        for item in data[JsonField.DATA]:
            output.append(
                BeanUtil.dict_to_model(JobProgressOutput(), **item)
            )

        return output

    @staticmethod
    def on_job_finished(job_id):
        """
        After the task ends, call the board interface to notify the update flow statistics.

        Parameters
        ----------
        job_id: str

        """
        data = {
            "job_id": job_id
        }
        BoardService.request("/project/job/finished", data)

    @staticmethod
    def request(api, data):
        """
        Send http request to board service.

        Parameters
        ----------
        api: str
        data: dict

        Returns
        -------
        The data of json response
        """
        url = BOARD_BASE_URL + api
        # send request
        sign = BoardService.gen_sign(json.dumps(data, separators=(',', ':')), GlobalSetting.get_rsa_private_key())
        req = {
            "data": data,
            "sign": sign
        }
        BoardService.LOG.info(
            "board request url:{}, {}".format(url, str(req))
        )
        start_time = current_timestamp()
        spend = 0
        try:
            response: Response = requests.post(url, json=req)
            spend = current_timestamp() - start_time
        except Exception as e:
            spend = current_timestamp() - start_time
            mess = "board response fail url:{}, {}".format(url, repr(e))
            BoardService.LOG.error(mess)
            return {
                JsonField.SUCCESS: False,
                JsonField.MESSAGE: e,
                JsonField.SPEND: spend
            }

        # http error
        if response.status_code < 200 or response.status_code > 299:
            mess = "board response fail({}ms) url:{}, {}, {}".format(spend, url, response.status_code, response.text)
            BoardService.LOG.error(mess)
            return {
                JsonField.SUCCESS: False,
                JsonField.MESSAGE: response.reason,
                JsonField.SPEND: spend
            }

        root = response.json()
        code = root.get("code")
        message = root.get("message")
        data = root.get("data")

        if code != 0:
            mess = "board response fail({}ms) url:{}, {}, {}".format(spend, url, message, response.text)
            BoardService.LOG.error(mess)
            return {
                JsonField.SUCCESS: False,
                JsonField.MESSAGE: message,
                JsonField.SPEND: spend
            }
        else:
            mess = "board response success({}ms) url:{}, {}".format(spend, url, response.text)
            BoardService.LOG.info(mess)
            return {
                JsonField.SUCCESS: True,
                JsonField.MESSAGE: response.text,
                JsonField.SPEND: spend,
                JsonField.DATA: data
            }

    @staticmethod
    def gen_sign(data_str, rsa_key_pri):
        private_key_obj = RSA.importKey(base64.b64decode(rsa_key_pri))
        msg_hash = SHA1.new(data_str.encode())
        signature = PKCS1_v1_5_sign.new(private_key_obj).sign(msg_hash)
        sign = base64.b64encode(signature).decode()
        return sign
