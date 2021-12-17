import os
import typing
from pathlib import Path
from ruamel import yaml
import aiohttp
import asyncio
import json
from visualdl import LogReader
from visualfl.db.task_dao import TaskDao
import datetime
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


def load_from_file(path: typing.Union[str, Path]):
    """
    Loads conf content from json or yaml file. Used to read in parameter configuration
    Parameters
    ----------
    path: str, path to conf file, should be absolute path

    Returns
    -------
    dict, parameter configuration in dictionary format

    """
    if isinstance(path, str):
        path = Path(path)
    config = {}
    if path is not None:
        file_type = path.suffix
        with path.open("r") as f:
            if file_type in ['.yml', '.yaml'] :
                config.update(yaml.safe_load(f))
            elif file_type == ".json":
                config.update(json.load(f))
            else:
                raise ValueError(f"Cannot load conf from file type {file_type}")
    return config

def get_last_file(data_dir):
    list = os.listdir(data_dir)
    list.sort(key=lambda fn:os.path.getmtime(data_dir+'/'+fn))
    filepath = os.path.join(data_dir,list[-1])

    return filepath

def get_data_to_db(task_id,data_dir,tag,metric_name,component_name):

    try:
        file_path = get_last_file(data_dir)
        reader = LogReader(file_path=file_path)
        losslist = reader.get_data('scalar', tag)

        if len(losslist)>0:
            metric_result,train_loss,data = {},{},{}
            for loss in losslist:
                data[loss.id] = dict(value=loss.value, timestamp=loss.timestamp)
            train_loss.update(metric_name=metric_name)
            train_loss.update(data=data)
            metric_result.update(train_loss=train_loss)

            TaskDao.save_task_result(task_id=task_id,task_result=metric_result,component_name=component_name,type=metric_name)
    except Exception as e:
        logging.debug(f"training program begin {e}")



