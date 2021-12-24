import os
import aiohttp
import asyncio
import json
from visualdl import LogReader
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

def get_last_file(data_dir):
    list = os.listdir(data_dir)
    list.sort(key=lambda fn:os.path.getmtime(data_dir+'/'+fn))
    filepath = os.path.join(data_dir,list[-1])

    return filepath

def get_data_to_db(task_id,log_dir,tag,component_name):
    file_path = get_last_file(log_dir)
    reader = LogReader(file_path=file_path)
    tags = reader.get_tags()
    losslist = reader.get_data('scalar', 'accuracy_0.tmp_0')

    if len(losslist)>0:
        metric_result,train_loss,data = {},{},{}
        for loss in losslist:
            data[loss.id] = dict(value=loss.value, timestamp=loss.timestamp)
        train_loss.update(metric_name=tag)
        train_loss.update(data=data)
        metric_result.update(train_loss=train_loss)

        TaskDao(task_id).save_task_result(task_result=metric_result,component_name=component_name,type=tag)

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


