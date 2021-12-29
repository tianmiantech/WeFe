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

        data[step] = dict(value=value, timestamp=current_milli_time)
        result.update(data=data)
        dao.save_task_result(task_result=result,component_name=component_name,type=tag)
    except Exception as e:
        logging.error(f"task {task_id} save data to db error {e}")


