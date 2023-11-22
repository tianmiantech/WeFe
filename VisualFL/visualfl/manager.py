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

import asyncio
import time
from typing import Optional, MutableMapping, AsyncGenerator, Tuple

import grpc

from visualfl.protobuf import cluster_pb2, cluster_pb2_grpc, job_pb2
from visualfl.utils.logger import Logger, pretty_pb


class ClusterManager(Logger, cluster_pb2_grpc.ClusterManagerServicer):
    def __init__(
        self,
        port: int,
        host: str = None,
    ):
        """
        init cluster manager instance
        Args:
            port:
            host:
        """
        self._host = "[::]" if host is None else host
        self._port = port
        self._alive_workers: MutableMapping[str, _WorkerDescription] = {}
        self._tasks_status = {}
        self._max_heartbeat_delay = 5

        self._server: Optional[grpc.aio.Server] = None

    def has_worker(self, worker_id) -> bool:
        """
        check worker `worker_id` alive(enrolled)
        Args:
            worker_id:

        Returns:

        """
        return worker_id in self._alive_workers

    def add_worker(self, worker_id, worker_ip, max_tasks, port_start, port_end):
        """
        add worker to manager
        Args:
            worker_id:
            worker_ip:
            max_tasks:
            port_start:
            port_end:

        Returns:

        """
        worker = _WorkerDescription(
            worker_id=worker_id,
            worker_ip=worker_ip,
            max_tasks=max_tasks,
            max_delay=self._max_heartbeat_delay,
            port_start=port_start,
            port_end=port_end,
        )
        self._alive_workers[worker_id] = worker

        async def _healthy_watcher():
            try:
                while True:
                    await asyncio.sleep(self._max_heartbeat_delay)
                    if worker_id not in self._alive_workers:
                        self.error(f"worker:{worker_id} not found")
                        break

                    if worker.is_asystole():
                        self.error(f"heartbeat from worker:{worker_id} loss")
                        break
            finally:
                self.remove_worker(worker_id)

        asyncio.create_task(_healthy_watcher())
        return worker

    def remove_worker(self, worker_id):
        """
        remove worker from manager
        Args:
            worker_id:
        """
        if worker_id not in self._alive_workers:
            return
        del self._alive_workers[worker_id]

    async def Enroll(
        self,
        request: cluster_pb2.Enroll.REQ,
        context: grpc.aio.ServicerContext,
    ) -> AsyncGenerator[cluster_pb2.Enroll.REP, None]:
        """
          rpc server impl: process tasker enroll request

        Args:
            request:
            context:

        Returns:

        """
        self.debug(f"cluster worker enroll request: {pretty_pb(request)}")
        if self.has_worker(request.worker_id):
            yield cluster_pb2.Enroll.REP(status=cluster_pb2.Enroll.ALREADY_ENROLL)
            return

        worker = self.add_worker(
            request.worker_id,
            request.worker_ip,
            request.max_tasks,
            request.port_start,
            request.port_end,
        )
        self.debug(f"cluster worker enroll success: worker: {request.worker_id}")
        yield cluster_pb2.Enroll.REP(status=cluster_pb2.Enroll.ENROLL_SUCCESS)

        while self.has_worker(request.worker_id):
            try:
                task = await worker.wait_next_task(timeout=5)
            except asyncio.TimeoutError:
                continue

            self.debug(
                f"task ready: job_id={task.job_id}, task_id={task.task_id}, task_type={task.task_type}"
            )
            rep = cluster_pb2.Enroll.REP(
                status=cluster_pb2.Enroll.TASK_READY, task=task
            )
            self.debug(
                f"response task({task.task_id}, {task.task_type}) to worker {request.worker_id}"
            )
            yield rep

        self.remove_worker(request.worker_id)

    async def UpdateTaskStatus(
        self, request: cluster_pb2.UpdateStatus.REQ, context: grpc.aio.ServicerContext
    ) -> cluster_pb2.UpdateStatus.REP:
        """
        process task status update request
        Args:
            request:
            context:

        Returns:

        """
        if request.worker_id not in self._alive_workers:
            return cluster_pb2.UpdateStatus.REP(status=cluster_pb2.UpdateStatus.FAILED)
        await self._alive_workers[request.worker_id].update_heartbeat()

        if not request.task_id:
            return cluster_pb2.UpdateStatus.REP(status=cluster_pb2.UpdateStatus.SUCCESS)

        if not request.task_id not in self._tasks_status:
            return cluster_pb2.UpdateStatus.REP(status=cluster_pb2.UpdateStatus.FAILED)

        self.debug(f"update task status: {request.task_id} to {request.task_status}")
        self._tasks_status[request.task_id] = request.task_status
        return cluster_pb2.UpdateStatus.REP(status=cluster_pb2.UpdateStatus.SUCCESS)

    async def TaskSubmit(
        self, request: cluster_pb2.TaskSubmit.REQ, context: grpc.aio.ServicerContext
    ) -> cluster_pb2.TaskSubmit.REP:
        """
        process task submit request
        Args:
            request:
            context:

        Returns:

        """
        try:
            task = request.task
            if not task.assignee:
                worker, _ = await self.dispatch()
                await worker.put_task(task=task)
            else:
                await self._alive_workers[task.assignee].put_task(task=task)
            return cluster_pb2.TaskSubmit.REP(status=cluster_pb2.TaskSubmit.SUCCESS)
        except Exception as e:
            self.exception(f"handle task submit failed: {e}")
            return cluster_pb2.TaskSubmit.REP(status=cluster_pb2.TaskSubmit.FAILED)

    async def TaskResourceRequire(self, request, context):
        """
        process task resource acquired request
        Args:
            request:
            context:

        Returns:

        """
        worker, endpoints = await self.dispatch(
            resource={"endpoints": request.num_endpoints}
        )
        if worker is None:
            return cluster_pb2.TaskResourceRequire.REP(
                status=cluster_pb2.TaskResourceRequire.FAILED
            )

        response = cluster_pb2.TaskResourceRequire.REP(
            status=cluster_pb2.TaskResourceRequire.SUCCESS, worker_id=worker.worker_id
        )
        for endpoint in endpoints:
            response.endpoints.append(endpoint)
        return response

    async def start(self):
        """
        start cluster manager service
        Returns:

        """
        self.info(f"starting cluster manager at port: {self._port}")
        self._server = grpc.aio.server(
            options=[
                ("grpc.max_send_message_length", 512 * 1024 * 1024),
                ("grpc.max_receive_message_length", 512 * 1024 * 1024),
            ],
        )
        cluster_pb2_grpc.add_ClusterManagerServicer_to_server(self, self._server)
        self._server.add_insecure_port(f"{self._host}:{self._port}")
        await self._server.start()
        self.info(f"cluster manager started at port: {self._port}")

    async def stop(self):
        """
        stop cluster manager service
        """
        await self._server.stop(1)

    async def dispatch(
        self, resource: dict = None
    ) -> Tuple[Optional["_WorkerDescription"], list]:
        """
        dispatch tasks to worker
        Args:
            resource:

        Returns:

        """
        if resource is None:
            resource = {}
        if not resource:
            for k, v in self._alive_workers.items():
                if v.has_task_capacity():
                    v.task_task_capacity()
                    return v, []
        elif "endpoints" in resource:
            num_endpoints = resource["endpoints"]
            for k, v in self._alive_workers.items():
                if v.has_num_valid_endpoints(num_endpoints) and v.has_task_capacity():
                    v.task_task_capacity()
                    endpoints = v.take_endpoints(num_endpoints)
                    return v, endpoints
        return None, []


class _WorkerDescription(object):
    def __init__(
        self, worker_id, worker_ip, max_tasks, max_delay, port_start, port_end
    ):
        self.worker_id = worker_id
        self.worker_ip = worker_ip
        self._port_start = port_start
        self._port_end = port_end
        self._max_tasks = max_tasks
        self._max_delay = max_delay
        self._last_heartbeat = time.time()
        self._task_queue: asyncio.Queue[job_pb2.Task] = asyncio.Queue()
        self._port_used = [False] * (
            self._port_end - self._port_start
        )  # todo: use memory-friendly data structure
        self.num_port_remind = self._port_end - self._port_start
        self.num_task_remind = self._max_tasks
        self._current_pos = 0

    def has_num_valid_endpoints(self, num):
        return self.num_task_remind >= num

    def has_task_capacity(self):
        return self.num_task_remind > 0

    def task_task_capacity(self):
        self.num_task_remind -= 1

    def take_endpoints(self, num):
        endpoints = []
        for i in range(num):
            endpoints.append(self._next_valid_endpoint())
        return endpoints

    def _next_valid_endpoint(self):
        for i in range(self._port_end - self._port_start):
            index = (i + self._current_pos) % (self._port_end - self._port_start)
            if not self._port_used[index]:
                self._current_pos = index + 1
                return f"{self.worker_ip}:{self._port_start + index}"
        raise Exception(f"no endpoint left")

    async def put_task(self, task: job_pb2.Task):
        return await self._task_queue.put(task)

    async def update_heartbeat(self):
        t = time.time()
        self._last_heartbeat = t

    def is_asystole(self):
        t = time.time()
        return t - self._last_heartbeat > self._max_delay

    async def wait_next_task(self, timeout):
        return await asyncio.wait_for(self._task_queue.get(), timeout=timeout)
