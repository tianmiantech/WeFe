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

import aiohttp
import asyncio
import json
from visualfl.db.task_dao import TaskDao
import time
import logging

def post(url, json_data):
    async def post_co():
        async with aiohttp.ClientSession() as session:
            async with session.post(
                url, json=json_data
            ) as resp:
                print(resp.status)
                print(json.dumps(await resp.json(), indent=2))
                resp.raise_for_status()

    loop = asyncio.get_event_loop()
    loop.run_until_complete(post_co())

def save_data_to_db(task_id,tag,value,step,component_name):
    try:
        result,data = {},{}
        current_milli_time = int(round(time.time() * 1000))
        tag = "accuracy" if "accuracy" in tag else "loss"

        dao = TaskDao(task_id)
        model = dao.get_task_result(tag)
        if model:
            result = json.loads(model.result)
            data = result.get("data")

        data[int(step)] = dict(value=float(value), timestamp=current_milli_time)
        result.update(data=data)
        dao.save_task_result(task_result=result,component_name=component_name,type=tag)
    except Exception as e:
        logging.error(f"task {task_id} save data to db error {e}")
