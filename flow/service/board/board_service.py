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

from typing import List

import requests
from requests import Response

from common.python.db.global_config_dao import GlobalConfigDao
from common.python.utils.core_utils import current_timestamp
from common.python.utils.log_utils import LoggerFactory
from flow.service.board.board_output import JobProgressOutput
from flow.utils.bean_util import BeanUtil

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

        if not data:
            return []
        output = []

        for item in data:
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
        start_time = current_timestamp()
        BoardService.LOG.info(
            "board request url:{}, {}".format(url, str(data))
        )

        try:
            response: Response = requests.post(url, json=data)
        except Exception as e:
            BoardService.LOG.error(
                "board response fail url:{}, {}".format(url, repr(e))
            )
            return None

        spend = current_timestamp() - start_time

        # http error
        if response.status_code < 200 or response.status_code > 299:
            BoardService.LOG.error(
                "board response fail({}ms) url:{}, {}, {}".format(spend, url, response.status_code, response.text)
            )
            return None

        # board service error
        root = response.json()
        code = root.get("code")
        message = root.get("message")
        data = root.get("data")

        if code != 0:
            BoardService.LOG.error(
                "board response fail({}ms) url:{}, {}, {}".format(spend, url, message, response.text)
            )
            return None
        else:
            BoardService.LOG.info(
                "board response success({}ms) url:{}, {}".format(spend, url, response.text)
            )
            return data
