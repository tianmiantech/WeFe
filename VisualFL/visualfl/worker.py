
from __future__ import annotations

import asyncio
from pathlib import Path
from typing import Optional, List, AsyncIterable

import grpc

from visualfl import __logs_dir__
from visualfl import extensions
from visualfl.paddle_fl.abs.task import Task
from visualfl.paddle_fl.executor import ProcessExecutor
from visualfl.protobuf import cluster_pb2_grpc, cluster_pb2
from visualfl.utils.exception import (
    VisualFLWorkerException,
    VisualFLExtensionException,
    VisualFLException,
)
from visualfl.utils.logger import Logger, pretty_pb
from visualfl.db.task_dao import TaskDao
from visualfl.utils.consts import TaskStatus


class ClusterWorker(Logger):
    def __init__(
        self,
        worker_id: str,
        worker_ip: str,
        max_tasks: int,
        port_start: int,
        port_end: int,
        manager_address: str,
        data_dir: str = None,
    ):
        """
        init cluster worker instance
        Args:
            worker_id:
            worker_ip:
            max_tasks:
            port_start:
            port_end:
            manager_address:
            data_dir:
        """
        self._task_queue: asyncio.Queue = asyncio.Queue()
        self._semaphore = asyncio.Semaphore(max_tasks)

        self._worker_id = worker_id
        self._worker_ip = worker_ip
        self._manager_address = manager_address
        self._max_tasks = max_tasks
        self._port_start = port_start
        self._port_end = port_end
        self._heartbeat_interval = 1
        self._data_dir = data_dir

        self._channel: Optional[grpc.Channel] = None
        self._stub: Optional[cluster_pb2_grpc.ClusterManagerStub] = None

        self._tasks: List[asyncio.Future] = []
        self._task_status: asyncio.Queue = asyncio.Queue()
        self._stop_event = asyncio.Event()

        self._asyncio_task_collection: Optional[List[asyncio.Task]] = None

    async def start(self):
        """
        start worker

        1. enroll to manager
        2. start heartbeat loop
        3. start task exec loop
        4. process tasks
        """
        self.info(f"starting worker {self._worker_id}")

        self.info(f"staring grpc channel to cluster manager")
        self._channel = grpc.aio.insecure_channel(
            self._manager_address,
            options=[
                ("grpc.max_send_message_length", 512 * 1024 * 1024),
                ("grpc.max_receive_message_length", 512 * 1024 * 1024),
            ],
        )
        self._stub = cluster_pb2_grpc.ClusterManagerStub(self._channel)

        self.info(f"sending enroll request to cluster manager")
        response_stream: AsyncIterable[cluster_pb2.Enroll.REP] = self._stub.Enroll(
            cluster_pb2.Enroll.REQ(
                worker_id=self._worker_id,
                worker_ip=self._worker_ip,
                max_tasks=self._max_tasks,
                port_start=self._port_start,
                port_end=self._port_end,
            )
        )
        first_response = True

        try:
            async for response in response_stream:

                if first_response:
                    if response.status == cluster_pb2.Enroll.ALREADY_ENROLL:
                        raise VisualFLWorkerException(
                            f"worker<{self._worker_id}> already enrolled, use new name or remove it from manager"
                        )

                    if response.status != cluster_pb2.Enroll.ENROLL_SUCCESS:
                        raise VisualFLWorkerException(
                            f"worker<{self._worker_id}>enroll failed with unknown status: {response.status}"
                        )
                    self.info(
                        f"worker<{self._worker_id}>success enrolled to cluster manager"
                    )

                    async def _co_update_status():
                        while True:
                            try:
                                request = await asyncio.wait_for(
                                    self._task_status.get(), self._heartbeat_interval
                                )
                            except asyncio.TimeoutError:
                                self.trace(
                                    "wait task status timeout. sending heartbeat request"
                                )
                                request = cluster_pb2.UpdateStatus.REQ(
                                    worker_id=self._worker_id
                                )

                            try:
                                update_response = await self._stub.UpdateTaskStatus(
                                    request
                                )
                            except grpc.aio.AioRpcError as _e:
                                self.error(f"can't send heartbeat to manager, {_e}")
                                self._stop_event.set()
                                return
                            if (
                                update_response.status
                                != cluster_pb2.UpdateStatus.SUCCESS
                            ):
                                self.error(
                                    f"update status failed, please check manager status"
                                )

                    self.info("starting heartbeat loop")
                    self._asyncio_task_collection = [
                        asyncio.create_task(_co_update_status()),
                    ]
                    self.info("heartbeat loop started")

                    self.info(f"starting task execute loop")
                    self._asyncio_task_collection.append(
                        asyncio.create_task(self._co_task_execute_loop())
                    )
                    self.info(f"task execute loop started")
                    first_response = False
                    continue

                # fetch tasks
                if response.status != cluster_pb2.Enroll.TASK_READY:
                    raise VisualFLWorkerException(
                        f"expect status {cluster_pb2.Enroll.TASK_READY}, got {response.status}"
                    )

                self.trace_lazy(
                    f"response <{{response}}> got", response=lambda: pretty_pb(response)
                )
                try:
                    task_id = response.task.task_id
                    task_type = response.task.task_type
                    task_class = extensions.get_task_class(task_type)
                    if task_class is None:
                        self.error(f"task type {task_type} not found")
                        raise VisualFLExtensionException(
                            f"task type {task_type} not found"
                        )
                    task = task_class.deserialize(response.task)
                    await self._task_queue.put(task)
                    TaskDao(task.web_task_id).update_task_status(TaskStatus.WAITRUN)
                    self.trace(f"put task in queue: task_id={task_id}")
                except VisualFLException as e:
                    self.error(f"preprocess fetched task failed: {e}")
                except Exception as e:
                    self.exception(e)
        except grpc.aio.AioRpcError as e:
            self.error(f"gRPC error: can't connect with cluster manager, {e}")
            self._stop_event.set()

    async def wait_for_termination(self):
        """
        block until stop event was set
        """
        await self._stop_event.wait()
        self.info(f"stop event set, stopping worker {self._worker_id}")

    async def stop(self):
        """
        stop worker
        """
        if self._channel is not None:
            await self._channel.close()
            self._channel = None

        self.info(f"canceling unfinished asyncio tasks")
        if self._asyncio_task_collection is not None:
            for task in self._asyncio_task_collection:
                if not task.done():
                    task.cancel()
                    self.trace(f"canceled task {task}")
            self.info(f"all unfinished asyncio tasks canceled")

    async def _task_exec_coroutine(self, _task: Task):
        try:
            self.info(
                f"start to exec task, job_id={_task.job_id}, task_id={_task.task_id}, task_type={_task.task_type}"
            )
            executor = ProcessExecutor(
                Path(__logs_dir__).joinpath(f"jobs/{_task.job_id}/{_task.task_id}"),
                data_dir=self._data_dir,
            )
            response = await _task.exec(executor)
            self.info(
                f"finish exec task, job_id={_task.job_id}, task_id={_task.task_id}"
            )

            self.trace(f"update task status")
            await self._task_status.put(
                cluster_pb2.UpdateStatus.REQ(
                    worker_id=self._worker_id,
                    job_id=_task.job_id,
                    task_id=_task.task_id,
                    task_status=cluster_pb2.UpdateStatus.TASK_FINISH,
                    exec_result=response,
                )
            )
            self.info(
                f"task status success updated to {cluster_pb2.UpdateStatus.TASK_FINISH}. "
                f"job_id={_task.job_id}, task_id={_task.task_id}"
            )

        except Exception as e:
            self.exception(e)
            await self._task_status.put(
                cluster_pb2.UpdateStatus.REQ(
                    worker_id=self._worker_id,
                    job_id=_task.job_id,
                    task_id=_task.task_id,
                    task_status=cluster_pb2.UpdateStatus.TASK_EXCEPTION,
                    exception=str(e),
                )
            )
            TaskDao(_task.web_task_id).update_task_status(TaskStatus.ERROR,str(e))
        finally:
            self._semaphore.release()
            self.trace_lazy(
                f"semaphore released, current: {{current}}",
                current=lambda: self._semaphore,
            )

    async def _co_task_execute_loop(self):

        # noinspection PyUnusedLocal

        while True:
            self.trace(f"acquiring semaphore")
            await self._semaphore.acquire()
            self.trace(f"acquired semaphore")
            self.trace(f"get from task queue")
            ready_task = await self._task_queue.get()
            self.trace_lazy(f"got {{task}} from task queue", task=lambda: ready_task)
            asyncio.create_task(self._task_exec_coroutine(ready_task))
            self.trace(f"asyncio task created to exec task")
