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

import asyncio
import base64
import json
import logging
import time
from urllib.parse import unquote

import aiohttp
from Crypto.Hash import SHA1
from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5 as PKCS1_v1_5_sign

from visualfl.db.task_dao import TaskDao


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
        dao.save_task_result(task_result=result, component_name=component_name, type=tag)
    except Exception as e:
        logging.error(f"task {task_id} save data to db error {e}")


def gen_sign(data, rsa_key_pri):
    private_key_obj = RSA.importKey(base64.b64decode(rsa_key_pri))
    msg_hash = SHA1.new(data.encode())
    signature = PKCS1_v1_5_sign.new(private_key_obj).sign(msg_hash)
    sign = base64.b64encode(signature).decode()
    return sign


def verify_sign(data, sign, rsa_pub_cus):
    public_key_obj = RSA.importKey(base64.b64decode(rsa_pub_cus))
    msg_hash = SHA1.new(data.encode())
    try:
        PKCS1_v1_5_sign.new(public_key_obj).verify(msg_hash, base64.b64decode(unquote(sign)))
        return True
    except (ValueError, TypeError):
        return False
