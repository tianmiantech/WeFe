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


from __future__ import annotations
import os
import signal
import asyncio
import json
import random
import sys
from pathlib import Path
import logging

import click
import grpc

from visualfl.paddle_fl.executor import ProcessExecutor
from visualfl.utils.exception import VisualFLWorkerException
from visualfl.protobuf import scheduler_pb2_grpc, scheduler_pb2


class Scheduler(scheduler_pb2_grpc.SchedulerServicer):
    def __init__(
        self,
        job_id:str,
        scheduler_ep: str,
        worker_num: int,
        sample_num: int,
        max_iter: int,
        startup_program,
        main_program,
    ):
        """
        init scheduler
        """
        self._scheduler_ep = scheduler_ep
        self._worker_num = worker_num
        self._sample_num = sample_num
        self._max_iter = max_iter

        self._grpc_port = int(self._scheduler_ep.split(":")[-1])
        self._grpc_server = None

        self._inited_workers = {}
        self._ready = asyncio.Event()

        self._current_step = 0
        self._candidate = set()
        self._wait_next = asyncio.Event()

        self._stop_event = asyncio.Event()

        self._max_delay = 300

        self._fl_server_watcher = FLServerWatched(
            job_id=job_id,main_program=main_program, startup_program=startup_program
        )

    async def start(self):
        logging.info(f"starting scheduler gRPC server")
        self._grpc_server = grpc.aio.server()
        scheduler_pb2_grpc.add_SchedulerServicer_to_server(self, self._grpc_server)
        self._grpc_server.add_insecure_port(f"[::]:{self._grpc_port}")
        await self._grpc_server.start()
        logging.info(f"scheduler gRPC server started at port {self._grpc_port}")

        # start server
        await self._fl_server_watcher.start()

        # async def _healthy_watcher():
        #     while True:
        #         await asyncio.sleep(self._max_delay)
        #         if len(self._inited_workers) == 0:
        #             self._stop_event.set()
        #             logging.debug(f"no workers init")
        #             break
        #
        # asyncio.create_task(_healthy_watcher())

    async def stop(self):
        logging.info(f"stopping gRPC server gracefully")
        await self._grpc_server.stop(1)
        logging.info(f"gRPC server stopped")

        await self._fl_server_watcher.stop()

    async def wait_for_termination(self):
        await self._stop_event.wait()
        await asyncio.sleep(2)

    async def Init(self, request, context):
        if self._ready.is_set() or request.name in self._inited_workers:
            return scheduler_pb2.Init.REP(status=scheduler_pb2.Init.REJECT)

        self._inited_workers[request.name] = 0
        self._check_init_status()
        logging.debug(f"init: {request.name}")
        return scheduler_pb2.Init.REP(status=scheduler_pb2.Init.INIT)

    def _check_init_status(self):
        if len(self._inited_workers) == self._worker_num:
            logging.debug(f"init done")
            self._select_candidate()
            logging.debug(f"selected: {self._candidate}")
            self._ready.set()

    def _check_finish_status(self):
        if len(self._candidate) == 0:
            logging.debug(f"all worker done, {self._current_step}/{self._max_iter}")
            if self._max_iter == self._current_step:
                self._stop_event.set()
                return

            self._current_step += 1
            self._select_candidate()
            self._wait_next.set()
            self._wait_next = asyncio.Event()

    def _select_candidate(self):
        self._candidate.clear()
        logging.debug(
            f"starting candidate selection from {self._inited_workers}, k={self._sample_num}"
        )
        self._candidate.update(
            random.sample(list(self._inited_workers.keys()), k=self._sample_num)
        )
        logging.debug(f"candidate selected: {self._candidate}")

    async def WorkerJoin(self, request, context):
        logging.debug(f"worker joining: {request.name}")
        if request.name not in self._inited_workers:
            return scheduler_pb2.WorkerJoin.REP(status=scheduler_pb2.WorkerJoin.REJECT)
        await self._ready.wait()

        if request.step < self._current_step:
            return scheduler_pb2.WorkerJoin.REP(status=scheduler_pb2.WorkerJoin.REJECT)

        if request.step == self._current_step:
            if request.name not in self._candidate:
                return scheduler_pb2.WorkerJoin.REP(
                    status=scheduler_pb2.WorkerJoin.NOT_SELECTED
                )
            return scheduler_pb2.WorkerJoin.REP(status=scheduler_pb2.WorkerJoin.ACCEPT)

        if request.step == self._current_step + 1:
            if self._max_iter == self._current_step:
                return scheduler_pb2.WorkerJoin.REP(
                    status=scheduler_pb2.WorkerJoin.REJECT
                )
            await self._wait_next.wait()
            if request.name not in self._candidate:
                return scheduler_pb2.WorkerJoin.REP(
                    status=scheduler_pb2.WorkerJoin.NOT_SELECTED
                )
            return scheduler_pb2.WorkerJoin.REP(status=scheduler_pb2.WorkerJoin.ACCEPT)

        return scheduler_pb2.WorkerJoin.REP(status=scheduler_pb2.WorkerJoin.REJECT)

    async def WorkerFinish(self, request, context):
        if request.name not in self._candidate:
            return scheduler_pb2.WorkerFinish.REP(
                status=scheduler_pb2.WorkerFinish.REJECT
            )
        self._candidate.remove(request.name)
        self._check_finish_status()
        return scheduler_pb2.WorkerFinish.REP(status=scheduler_pb2.WorkerFinish.DONE)


class FLServerWatched(object):
    """
    use scheduler to start and kill fl_server
    """

    def __init__(self, job_id,main_program, startup_program):
        self.job_id = job_id
        self._main_program = main_program
        self._startup_program = startup_program
        self.sub_pid = None

    async def start(self):
        executor = ProcessExecutor(Path("."))
        python_executable = sys.executable
        cmd = " ".join(
            [
                f"{python_executable} -m visualfl.paddle_fl.scheduler.fl_server",
                f"--job-id={self.job_id}",
                f"--startup-program={self._startup_program}",
                f"--main-program={self._main_program}",
                f">{executor.stdout} 2>{executor.stderr} &",
            ]
        )
        returncode,pid = await executor.execute(cmd)
        if returncode != 0:
            raise VisualFLWorkerException(
                f"execute task {cmd} failed, return code: {returncode}"
            )
        self.sub_pid = pid

    async def stop(self):
        if self.sub_pid is not None:
            try:
                os.kill(int(self.sub_pid)+1,signal.SIGKILL)
            except ProcessLookupError as e:
                logging.debug(f"kill {self.sub_pid} ProcessLookupError {e}")


@click.command()
@click.option("--job-id", type=str, required=True)
@click.option("--scheduler-ep", type=str, required=True)
@click.option(
    "--main-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--startup-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--config",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
def fl_scheduler(
    job_id,
    scheduler_ep,
    startup_program,
    main_program,
    config,
):
    logging.basicConfig(
        filename="aggregator.log",
        filemode="w",
        format="%(asctime)s %(name)s:%(levelname)s:%(message)s",
        datefmt="%d-%M-%Y %H:%M:%S",
        level=logging.DEBUG,
    )
    with open(config) as f:
        config_dict = json.load(f)
    max_iter = config_dict["max_iter"]
    worker_num = config_dict["worker_num"]

    loop = asyncio.get_event_loop()
    scheduler = Scheduler(
        job_id=job_id,
        scheduler_ep=scheduler_ep,
        worker_num=worker_num,
        sample_num=worker_num,
        max_iter=max_iter,
        startup_program=startup_program,
        main_program=main_program,
    )
    loop.run_until_complete(scheduler.start())

    try:
        loop.run_until_complete(scheduler.wait_for_termination())
    finally:
        loop.run_until_complete(scheduler.stop())
        loop.close()


if __name__ == "__main__":
    fl_scheduler()
