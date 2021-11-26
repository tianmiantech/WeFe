
from __future__ import annotations

import os
import asyncio
import enum
import json
import traceback
from datetime import datetime
from typing import Optional, MutableMapping, List
import yaml
import attr
import grpc
from aiohttp import web

from visualfl import __basedir__
from visualfl import extensions
from visualfl.paddle_fl.abs.job import Job
from visualfl.protobuf import (
    job_pb2,
    cluster_pb2,
    cluster_pb2_grpc
)
from visualfl.utils.exception import VisualFLExtensionException
from visualfl.utils.logger import Logger



class _JobStatus(enum.Enum):
    """
    query job status
    """

    NOTFOUND = "not_found"
    APPLYING = "applying"
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
        return route_table

    async def _restful_submit(self, request: web.Request) -> web.Response:
        """
        handle submit request
        Args:
            request:

        Returns:

        """
        try:
            data = await request.json()
        except json.JSONDecodeError as e:
            return web.json_response(data={}, status=400, reason=str(e))

        try:
            job_id = data["job_id"]
            role = data["role"]
            member_id = data["member_id"]
            job_type = data["job_type"]
            config = data["env"]
            data_set = data["data_set"]
            download_url = data_set["download_url"]
            algorithm_config = data.get("algorithm_config", None)
            program = algorithm_config["program"]
            if program is "paddle_detection":
                program_full_path = os.path.join(__basedir__, 'visualfl', 'algorithm', program)
                default_config_name = 'default_algorithm_config.yaml'
                architecture = algorithm_config["architecture"]
                default_algorithm_config = os.path.join(program_full_path,"configs",architecture,default_config_name)
                with open(default_algorithm_config) as f:
                    default_algorithm_dict = yaml.load(f)
                default_algorithm_dict["download_url"] = download_url
                default_algorithm_dict["max_iters"] = algorithm_config["max_iters"]
                default_algorithm_dict["inner_step"] = algorithm_config["inner_step"]
                default_algorithm_dict["num_classes"] = algorithm_config["num_classes"]
                default_algorithm_dict["LearningRate"]["base_lr"] = algorithm_config["base_lr"]
                default_algorithm_dict["TrainReader"]["inputs_def"]["image_shape"] = algorithm_config["image_shape"]
                default_algorithm_dict["TrainReader"]["batch_size"] = algorithm_config["batch_size"]

        except KeyError:
            return web.json_response(
                data=dict(exception=traceback.format_exc()), status=400
            )

        # noinspection PyBroadException
        try:
            loader = extensions.get_job_class(job_type)
            validator = extensions.get_job_schema_validator(job_type)
            if loader is None:
                raise VisualFLExtensionException(f"job type {job_type} not supported")
            validator.validate(config)
            job = loader.load(
                job_id=job_id, role=role,member_id=member_id,config=config, algorithm_config=default_algorithm_dict
            )

        except Exception:
            # self.logger.exception("[submit]catch exception")
            reason = traceback.format_exc()
            return web.json_response(data=dict(exception=reason), status=400)

        self.shared_status.job_status[job_id] = _JobStatus.WAITING
        await self.shared_status.job_queue.put(job)

        return web.json_response(
            data={"job_id": job_id},
        )

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
            return web.json_response(data={}, status=400, reason=str(e))

        job_id = data.get("job_id", None)
        if job_id is None:
            return web.json_response(data={}, status=400, reason="required `job_id`")

        if job_id not in self.shared_status.job_status:
            return web.json_response(
                data=dict(job_id=job_id, status=str(_JobStatus.NOTFOUND)),
                status=404,
            )

        return web.json_response(
            data=dict(job_id=job_id, status=str(self.shared_status.job_status[job_id])),
        )


    async def _restful_apply(self, request: web.Request) -> web.Response:
        """
        handle query request

        Args:
            request:

        Returns:

        """
        try:
            data = await request.json()
        except json.JSONDecodeError as e:
            return web.json_response(data={}, status=400, reason=str(e))

        try:
            job_id = data["job_id"]
            job_type = data["job_type"]
            role = data["role"]
            member_id = data["member_id"]
            callback_url = data["callback_url"]
            job_config = data["job_config"]
            algorithm_config = data["algorithm_config"]
        except KeyError:
            return web.json_response(
                data=dict(exception=traceback.format_exc()), status=400
            )

        try:
            # loader = extensions.get_job_class(job_type)
            # # validator = extensions.get_job_schema_validator(job_type)
            # if loader is None:
            #     raise VisualFLExtensionException(f"job type {job_type} not supported")
            # job = loader(job_id=job_id,role=role,member_id=member_id,callback_url=callback_url)

            loader = extensions.get_job_class(job_type)
            validator = extensions.get_job_schema_validator(job_type)
            if loader is None:
                raise VisualFLExtensionException(f"job type {job_type} not supported")
            validator.validate(job_config)
            job = loader.load(
                job_id=job_id, role=role, member_id=member_id, config=job_config, algorithm_config=algorithm_config,callback_url=callback_url
            )

        except Exception:
            # self.logger.exception("[submit]catch exception")
            reason = traceback.format_exc()
            return web.json_response(data=dict(exception=reason), status=400)

        self.shared_status.job_status[job_id] = _JobStatus.APPLYING
        await self.shared_status.apply_queue.put(job)

        return web.json_response(
            data={"job_id": job_id},
        )


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
        standalone: bool = False
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
        self.standalone = standalone


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

                        json_data = {"job_id": job.job_id,
                                     "server_endpoint": job._server_endpoint,
                                     "aggregator_endpoint": job._aggregator_endpoint,
                                     "aggregator_assignee": job._aggregator_assignee
                                     }
                        self.debug(f"json data: {json_data}")
                        import requests
                        requests.post(job._callback_url, json=json_data)

            except Exception as e:
                self.exception(f"run jobs failed: {e}")

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

                if self.standalone:
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
                            f"send local task: {task.task_id} with task type: {task.task_type} to cluster"
                        )
                        await self.shared_status.cluster_task_queue.put(task)

                    await asyncio.sleep(5)
                else:
                    await job.compile()

                for task in job.generate_trainer_tasks():
                    self.debug(
                        f"send local task: {task.task_id} with task type: {task.task_type} to cluster"
                    )
                    await self.shared_status.cluster_task_queue.put(task)

            except Exception as e:
                self.exception(f"run jobs failed: {e}")

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

    async def stop(self):
        """
        stop master
        """
        # await self._coordinator.stop_coordinator_channel(grace=1)
        await self._rest_site.stop_rest_site()
        await self._cluster.stop_cluster_channel(grace=1)
