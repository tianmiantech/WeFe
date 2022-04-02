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

# Copyright (c) 2020 The FedVision Authors. All Rights Reserved.
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

from __future__ import annotations

import os,sys
import asyncio
import random
import subprocess

import aiofiles
import enum
import json
from pathlib import Path
import traceback
from datetime import datetime
from typing import Optional, MutableMapping, List
import attr
import grpc
from aiohttp import web
from visualfl import extensions
from visualfl.paddle_fl.abs.job import Job
from visualfl.protobuf import (
    job_pb2,
    cluster_pb2,
    cluster_pb2_grpc
)
from visualfl.utils.exception import VisualFLExtensionException
from visualfl.utils.logger import Logger
from visualfl.utils.tools import *
from visualfl.utils.consts import TaskStatus
from visualfl.utils import data_loader
from visualfl.paddle_fl.executor import ProcessExecutor
from visualfl import __basedir__,__logs_dir__
from visualfl.utils.consts import ComponentName,TaskResultType

class _JobStatus(enum.Enum):
    """
    query job status
    """

    NOTFOUND = "not_found"
    APPLYING = "applying"
    INFERING = "infering"
    WAITING = "waiting"
    PROPOSAL = "proposal"
    RUNNING = "running"
    FAILED = "failed"
    SUCCESS = "success"


@attr.s
class _SharedStatus(object):
    """
    status shared by ...
    """

    member_id = attr.ib(type=str)
    job_types = attr.ib(type=List[str], default=["paddle_fl"])

    def __attrs_post_init__(self):
        self.job_status: MutableMapping[str, _JobStatus] = {}
        self.cluster_task_queue: asyncio.Queue[job_pb2.Task] = asyncio.Queue()
        self.job_queue: asyncio.Queue[Job] = asyncio.Queue()
        self.apply_queue: asyncio.Queue[Job] = asyncio.Queue()
        self.infer_queue: asyncio.Queue[Job] = asyncio.Queue()
        self.job_counter = 0

    def generate_job_id(self):
        """
        generate unique job id
        """
        self.job_counter += 1
        return f"{self.member_id}-{datetime.now().strftime('%Y%m%d%H%M%S')}-{self.job_counter}"


@attr.s
class ProposalAcceptRule(Logger):
    shared_status = attr.ib(type=_SharedStatus)

    async def accept(self, job_type):
        # accept all
        return job_type in self.shared_status.job_types


class RESTService(Logger):
    """
    service accept restful request from users
    """

    def __init__(self, shared_status: _SharedStatus, port: int, host: str = None):
        """
        init rest services instance
        Args:
            shared_status:
            port:
            host:
        """
        self.shared_status = shared_status
        self.port = port
        self.host = host

        self._site: Optional[web.TCPSite] = None

    async def start_rest_site(self):
        """
        start web service non-blocked
        """
        self.info(
            f" ing restful services at {':' if self.host is None else self.host}:{self.port}"
        )
        app = web.Application()
        app.add_routes(self._register_routes())
        runner = web.AppRunner(app, access_log=self.get_logger())
        await runner.setup()
        self._site = web.TCPSite(runner=runner, host=self.host, port=self.port)
        await self._site.start()
        self.info(
            f"restful services started at  {':' if self.host is None else self.host}:{self.port}"
        )

    async def stop_rest_site(self):
        """
        stop web service
        """
        if self._site is not None:
            await self._site.stop()

    def _register_routes(
        self, route_table: Optional[web.RouteTableDef] = None
    ) -> web.RouteTableDef:
        """
        register routes:

            1. submitter
            2. query
        Args:
            route_table: optional provide a `RouteTableDef` instance.

        Returns:

        """
        if route_table is None:
            route_table = web.RouteTableDef()
        route_table.post("/apply")(self._restful_apply)
        route_table.post("/submit")(self._restful_submit)
        route_table.post("/query")(self._restful_query)
        route_table.post("/infer")(self._restful_infer)
        route_table.post("/stop")(self._restful_stop)
        route_table.get("/serving_model/download")(self._restful_download)
        return route_table

    def web_response(self,code,message,job_id=None):
        return web.json_response(data=dict(code=code,message=message,job_id=job_id), status=code)

    async def _restful_download(self,request: web.Request) -> web.Response:

        def query_parse(req):
            obj = req.query_string
            queryitem = []
            if obj:
                query = req.query.items()
                for item in query:
                    queryitem.append(item)
                return dict(queryitem)
            else:
                return None

        async def export_serving_mode(job_id,task_id,serving_model_path):
            step = TaskDao(task_id).get_task_progress()
            algorithm_config_path = Path(__logs_dir__).joinpath(f"jobs/{job_id}/master/algorithm_config.json")
            config_path = Path(__logs_dir__).joinpath(f"jobs/{job_id}/master/config.json")
            with open(config_path) as f:
                config_json = json.load(f)
            with open(algorithm_config_path) as f:
                algorithm_config_json = json.load(f)
            local_trainer_indexs = config_json.get("local_trainer_indexs")
            weights = Path(__logs_dir__).joinpath(f"jobs/{job_id}/trainer_{local_trainer_indexs[0]}/checkpoint/{step}")
            program = algorithm_config_json.get("program")
            architecture = algorithm_config_json.get("architecture")
            if program == "paddle_detection":
                program_full_path = os.path.join(__basedir__, 'algorithm', 'paddle_detection')
                default_config_name = 'default_algorithm_config.yml'
                algorithm_config_path = os.path.join(program_full_path, "configs", architecture.lower(),
                                                     default_config_name)

            executor = ProcessExecutor(serving_model_path)
            executable = sys.executable
            cmd = " ".join(
                [
                    f"{executable} -m visualfl.algorithm.{program}.export_serving_model",
                    f"--task_id {task_id}",
                    f"-o weights={weights}",
                    f"--output_dir {serving_model_path}",
                    f"-c {algorithm_config_path}",
                    f">{executor.stdout} 2>{executor.stderr}",
                ]
            )
            returncode, pid = await executor.execute(cmd)
            if returncode != 0:
                raise Exception("export serving model error")

        query = query_parse(request)
        self.debug(f"export serving model request data: {query}")
        job_id = query.get("job_id")
        task_id = query.get("task_id")
        serving_model_path = Path(__logs_dir__).joinpath(f"jobs/{job_id}/serving_model")

        await export_serving_mode(job_id,task_id,serving_model_path)

        cfg_name = "default_algorithm_config"
        zip_file = os.path.join(serving_model_path, f"{cfg_name}.zip")
        data_loader.make_zip(os.path.join(serving_model_path, cfg_name),zip_file)

        if os.path.exists(zip_file):
            async with aiofiles.open(zip_file, 'rb') as f:
                content = await f.read()
            if content:
                response = web.Response(
                    content_type='application/octet-stream',
                    headers={'Content-Disposition': 'attachment;filename={}'.format(zip_file)},
                    body=content
                )
                return response

            else:
                return self.web_response(400, f"read file :{zip_file} error",job_id)
        else:
            return self.web_response(400, f"file path :{zip_file} not exists",job_id)

    async def _restful_submit(self, request: web.Request) -> web.Response:
        """
        handle submit request
        Args:
            request:

        Returns:

        """
        try:
            data = await request.json()
            self.debug(f"restful submit request data: {data}")
        except json.JSONDecodeError as e:
            return self.web_response(400,str(e))

        try:
            job_id = data["job_id"]
            task_id = data["task_id"]
            role = data["role"]
            member_id = data["member_id"]
            job_type = data["job_type"]
            config = data["env"]
            data_set = data["data_set"]
            download_url = data_set["download_url"]
            data_name = data_set["name"]
            algorithm_config = data.get("algorithm_config")
            program = algorithm_config["program"]
            config["max_iter"] = algorithm_config["max_iter"]
            algorithm_config["download_url"] = download_url
            algorithm_config["data_name"] = data_name

        except Exception:
            return self.web_response(400, traceback.format_exc(),job_id)


        # noinspection PyBroadException
        try:
            loader = extensions.get_job_class(job_type)
            validator = extensions.get_job_schema_validator(job_type)
            if loader is None:
                raise VisualFLExtensionException(f"job type {job_type} not supported")
            # validator.validate(config)
            job = loader.load(
                job_id=job_id, task_id=task_id,role=role,member_id=member_id,config=config, algorithm_config=algorithm_config
            )

        except Exception:
            # self.logger.exception("[submit]catch exception")
            reason = traceback.format_exc()
            return self.web_response(400, reason,job_id)


        self.shared_status.job_status[job_id] = _JobStatus.WAITING
        await self.shared_status.job_queue.put(job)

        return self.web_response(200, "success", job_id)

    async def _restful_stop(self, request: web.Request) -> web.Response:
        """
        handle query request

        Args:
            request:

        Returns:

        """
        try:
            data = await request.json()
        except json.JSONDecodeError as e:
            return self.web_response(400, str(e))

        job_id = data.get("job_id", None)
        if job_id is None:
            return self.web_response(400, "required `job_id`")

        try:
            for line in os.popen(f'ps -ef | grep fl_ | grep {job_id} | grep -v grep').readlines():
                pid = line.split()[1]
                subprocess.Popen(f"kill -9 {pid}", shell=True)
        except Exception:
            Logger.error(f"failed: can't stop job {job_id}")
            return self.web_response(400, f"failed: can't stop job {job_id}")

        return self.web_response(200, "stop job success", job_id)

    async def _restful_query(self, request: web.Request) -> web.Response:
        """
        handle query request

        Args:
            request:

        Returns:

        """
        try:
            data = await request.json()
        except json.JSONDecodeError as e:
            return self.web_response(400, str(e))

        job_id = data.get("job_id", None)
        if job_id is None:
            return self.web_response(400, "required `job_id`")

        if job_id not in self.shared_status.job_status:
            return self.web_response(404, "job_id not found",job_id)

        return web.json_response(
            data=dict(code=200,job_id=job_id,status=str(self.shared_status.job_status[job_id]),message="success"),
        )


    async def _restful_apply(self, request: web.Request) -> web.Response:
        """
        handle apply request

        Args:
            request:

        Returns:

        """
        try:
            data = await request.json()
            self.debug(f"restful apply request data: {data}")
        except json.JSONDecodeError as e:
            return self.web_response(400, str(e))

        try:
            job_id = data["job_id"]
            task_id = data["task_id"]
            role = data["role"]
            member_id = data["member_id"]
            job_type = data["job_type"]
            config = data["env"]
            callback_url = data["callback_url"]
            data_set = data["data_set"]
            download_url = data_set["download_url"]
            data_name = data_set["name"]
            algorithm_config = data.get("algorithm_config")
            config["max_iter"] = algorithm_config["max_iter"]
            algorithm_config["download_url"] = download_url
            algorithm_config["data_name"] = data_name

        except Exception:
            return self.web_response(400, traceback.format_exc(),job_id)

        try:
            loader = extensions.get_job_class(job_type)
            validator = extensions.get_job_schema_validator(job_type)
            if loader is None:
                raise VisualFLExtensionException(f"job type {job_type} not supported")
            # validator.validate(env_config)
            job = loader.load(
                job_id=job_id, task_id=task_id,role=role, member_id=member_id, config=config, algorithm_config=algorithm_config,callback_url=callback_url
            )

        except Exception:
            # self.logger.exception("[submit]catch exception")
            return self.web_response(400, traceback.format_exc(),job_id)

        self.shared_status.job_status[job_id] = _JobStatus.APPLYING
        await self.shared_status.apply_queue.put(job)

        return self.web_response(200, "success", job_id)

    async def _restful_infer(self, request: web.Request) -> web.Response:
        """
        handle infer request

        Args:
            request:

        Returns:

        """
        try:
            data = await request.json()
            self.debug(f"restful infer request data: {data}")
        except json.JSONDecodeError as e:
            return self.web_response(400, str(e))

        try:
            job_id = data["job_id"]
            task_id = data["task_id"]
            role = data["role"]
            member_id = data["member_id"]
            job_type = data["job_type"]
            config = data["env"]
            data_set = data["data_set"]
            download_url = data_set["download_url"]
            data_name = data_set["name"]
            config["download_url"] = download_url
            config["data_name"] = data_name
            algorithm_config = data.get("algorithm_config")
            cur_step = TaskDao(task_id).get_task_progress()
            input_dir = os.path.join(__logs_dir__,f"jobs/{job_id}/infer/input")
            infer_session_id = data_set.get("infer_session_id", '')
            infer_dir = data_loader.job_download(download_url, infer_session_id, input_dir)
            data_loader.extractImages(infer_dir)
            output_dir = os.path.join(__logs_dir__, f"jobs/{job_id}/infer/output/{os.path.basename(infer_dir)}")
            config["cur_step"] = cur_step
            config["infer_dir"] = infer_dir
            config["output_dir"] = output_dir

            task_result = {"infer_session_id": infer_session_id,"status": "wait_run"}
            program = algorithm_config["program"]
            componentName = ComponentName.DETECTION if program == "paddle_detection" else ComponentName.CLASSIFY
            TaskDao(task_id).save_task_result(task_result, componentName,type=TaskResultType.INFER)

        except Exception as e:
            self.exception(f"infer request download and process images error as {e} ")
            return self.web_response(400, traceback.format_exc(),job_id)

        try:
            loader = extensions.get_job_class(job_type)
            if loader is None:
                raise VisualFLExtensionException(f"job type {job_type} not supported")
            from visualfl.paddle_fl.job import PaddleFLJob
            job = loader.load(
                job_id=job_id, task_id=task_id, role=role, member_id=member_id, config=config,
                algorithm_config=algorithm_config, is_infer=True
            )

        except Exception:
            return self.web_response(400, traceback.format_exc(),job_id)

        self.shared_status.job_status[job_id] = _JobStatus.INFERING
        await self.shared_status.infer_queue.put(job)

        return self.web_response(200, "success", job_id)


class ClusterManagerConnect(Logger):
    """
    cluster manager client
    """

    def __init__(self, address, shared_status: _SharedStatus):
        """
        init cluster manager client
        Args:
            address:
            shared_status:
        """
        self.address = address
        self.shared_status = shared_status
        self._channel: Optional[grpc.aio.Channel] = None
        self._stub: Optional[cluster_pb2_grpc.ClusterManagerStub] = None

    async def  submit_tasks_to_cluster(self):
        """
        infinity loop to get task from queue and submit it to cluster
        """
        while True:
            task = await self.shared_status.cluster_task_queue.get()
            self.debug(
                f"task sending: task_id={task.task_id} task_type={task.task_type} to cluster"
            )
            await self._stub.TaskSubmit(cluster_pb2.TaskSubmit.REQ(task=task))
            self.debug(
                f"task sent: task_id={task.task_id} task_type={task.task_type} to cluster"
            )

    async def task_resource_require(
        self, request: cluster_pb2.TaskResourceRequire.REQ
    ) -> cluster_pb2.TaskResourceRequire.REP:
        """
        acquired resource from cluster(ports)
        Args:
            request:

        Returns:

        """
        response = await self._stub.TaskResourceRequire(request)
        return response

    async def start_cluster_channel(self):
        """
        start channel to cluster manager
        """
        self.info(f"start cluster channel to {self.address}")
        self._channel = grpc.aio.insecure_channel(
            self.address,
            options=[
                ("grpc.max_send_message_length", 512 * 1024 * 1024),
                ("grpc.max_receive_message_length", 512 * 1024 * 1024),
            ],
        )
        self._stub = cluster_pb2_grpc.ClusterManagerStub(self._channel)
        self.info(f"cluster channel started to {self.address}")

    async def cluster_channel_ready(self):
        """
        await until channel ready
        """
        return await self._channel.channel_ready()

    async def stop_cluster_channel(self, grace: Optional[float] = None):
        """
        stop channel to cluster manager
        Args:
            grace:

        Returns:

        """
        self.info(f"stopping cluster channel")
        await self._channel.close(grace)
        self.info(f"cluster channel started to {self.address}")


class Master(Logger):
    def __init__(
        self,
        member_id: str,
        cluster_address: str,
        rest_port: int,
        rest_host: str = None,
        local: bool = False
    ):
        """
          init master

        Args:
            member_id:
            rest_port:
            rest_host:
        """
        self.shared_status = _SharedStatus(member_id=member_id)
        self._rest_site = RESTService(
            shared_status=self.shared_status, port=rest_port, host=rest_host
        )
        self._cluster = ClusterManagerConnect(
            shared_status=self.shared_status, address=cluster_address
        )
        self.local = local

    def callback(self,job,status=None,message=None):
        json_data = dict(
            job_id=job.job_id,
            task_id = job._web_task_id,
            status=status,
            message=message,
            server_endpoint=job._server_endpoint,
            aggregator_endpoint=job._aggregator_endpoint,
            aggregator_assignee=job._aggregator_assignee
        )

        self.debug(f"callback url {job._callback_url} , json data is {json_data}")
        import requests
        r = requests.post(job._callback_url,json=json_data)
        self.debug(f"callback {job._callback_url} result: {r.text}")

    async def _infer_job_handler(self):
        """
        handle infer jobs.
        """
        async def _co_handler(job: Job):

            try:

                    await job.infer()
                    # self.callback(job)

            except Exception as e:
                self.exception(f"job infer failed: {e}")


        while True:
            infer_job = await self.shared_status.infer_queue.get()
            asyncio.create_task(_co_handler(infer_job))


    async def _apply_job_handler(self):
        """
        handle submitted jobs.
        """
        async def _co_handler(job: Job):

            try:
                if job.resource_required is not None:
                    response = await self._cluster.task_resource_require(
                        job.resource_required
                    )
                    if response.status != cluster_pb2.TaskResourceRequire.SUCCESS:
                        raise Exception(
                            "job failed due to no enough resource"
                        )  # todo: maybe wait some times and retry?
                    job.set_required_resource(response)

                    await job.compile()

                    self.shared_status.job_status[job.job_id] = _JobStatus.RUNNING
                    for task in job.generate_aggregator_tasks():
                        self.debug(
                            f"send local task: {task.task_id} with task type: {task.task_type} to cluster"
                        )
                        await self.shared_status.cluster_task_queue.put(task)

                        self.callback(job)

            except Exception as e:
                self.exception(f"run jobs failed: {e}")
                TaskDao(job._web_task_id).update_task_status(TaskStatus.ERROR, str(e))


        while True:
            apply_job = await self.shared_status.apply_queue.get()
            asyncio.create_task(_co_handler(apply_job))


    async def _submitted_job_handler(self):
        """
        handle submitted jobs.
        """
        async def _co_handler(job: Job):

            # todo: generalize this process
            # stick to paddle fl job now

            try:

                if self.local:
                    if job.resource_required is not None:
                        response = await self._cluster.task_resource_require(
                            job.resource_required
                        )
                        if response.status != cluster_pb2.TaskResourceRequire.SUCCESS:
                            raise Exception(
                                "job failed due to no enough resource"
                            )  # todo: maybe wait some times and retry?
                        job.set_required_resource(response)

                    # compile job
                    await job.compile()

                    self.shared_status.job_status[job.job_id] = _JobStatus.RUNNING
                    for task in job.generate_aggregator_tasks():
                        self.debug(
                            f"send aggregator task: {task.task_id} with task type: {task.task_type} to cluster"
                        )
                        await self.shared_status.cluster_task_queue.put(task)

                    await asyncio.sleep(5)
                else:
                    await job.compile()

                for task in job.generate_trainer_tasks():
                    self.debug(
                        f"send trainer task: {task.task_id} with task type: {task.task_type} to cluster"
                    )
                    await self.shared_status.cluster_task_queue.put(task)

            except Exception as e:
                self.exception(f"run jobs failed: {e}")
                TaskDao(job._web_task_id).update_task_status(status=TaskStatus.ERROR, message=str(e))

        while True:
            submitted_job = await self.shared_status.job_queue.get()
            asyncio.create_task(_co_handler(submitted_job))

    async def start(self):
        """
        start master:

            1. cluster manager to process tasks
            2. restful service to handler request from user
            3. coordinator to connect to `the world`

        """

        # connect to cluster
        await self._cluster.start_cluster_channel()
        while True:
            try:
                await asyncio.wait_for(self._cluster.cluster_channel_ready(), 5)
            except asyncio.TimeoutError:
                self.warning(f"cluster channel not ready, retry in 5 seconds")
            else:
                self.info(f"cluster channel ready!")
                break
        #  get task from queue and submit it to cluster
        asyncio.create_task(self._cluster.submit_tasks_to_cluster())

        # start rest site
        await self._rest_site.start_rest_site()


        #get job from apply_queue and require source
        asyncio.create_task(self._apply_job_handler())

        #get job from job_queue and send task to cluster by put it into a task queue
        asyncio.create_task(self._submitted_job_handler())

        # get job from infer_queue
        asyncio.create_task(self._infer_job_handler())

    async def stop(self):
        """
        stop master
        """
        # await self._coordinator.stop_coordinator_channel(grace=1)
        await self._rest_site.stop_rest_site()
        await self._cluster.stop_cluster_channel(grace=1)

