import logging
import pickle
from typing import Optional

import grpc
from paddle import fluid

from visualfl.utils.logger import Logger
from visualfl.protobuf import scheduler_pb2_grpc, scheduler_pb2
from paddle_fl.paddle_fl.core.master.fl_job import FLJobBase


class TrainerSchedulerAgent(Logger):
    def __init__(self, worker_name, scheduler_ep):
        self._worker_name = worker_name
        self._scheduler_ep = scheduler_ep

        self._channel: Optional[grpc.Channel] = None
        self._stub: Optional[scheduler_pb2_grpc.SchedulerStub] = None

    def start_channel(self):
        self._channel = grpc.insecure_channel(self._scheduler_ep)
        self._stub = scheduler_pb2_grpc.SchedulerStub(self._channel)

        self.debug(f"waiting channel ready")
        future = grpc.channel_ready_future(self._channel)
        future.result()
        self.debug(f"channel ready")
        return self

    def init_worker(self):
        self.debug(f"start to init")
        self._stub.Init(scheduler_pb2.Init.REQ(name=self._worker_name))
        self.debug(f"init success")

    def join(self, step: int):
        self.debug("start to join")
        response = self._stub.WorkerJoin(
            scheduler_pb2.WorkerJoin.REQ(name=self._worker_name, step=step)
        )
        self.debug(f"join success: {response.status}")
        return response.status == scheduler_pb2.WorkerJoin.ACCEPT

    def finish(self):
        self.debug("start to finish")
        status = self._stub.WorkerFinish(
            scheduler_pb2.WorkerFinish.REQ(name=self._worker_name)
        )
        self.debug(f"finish success: {status}")
        return status == scheduler_pb2.WorkerFinish.DONE

    def close(self):
        self._channel.close()


class FedAvgTrainer(FLJobBase):
    def __init__(self, scheduler_ep, trainer_ep):
        self._logger = logging.getLogger("FLTrainer")
        super(FedAvgTrainer, self).__init__()
        self._scheduler_ep = scheduler_ep
        self._trainer_ep = trainer_ep

        self.scheduler_agent: Optional[TrainerSchedulerAgent] = None
        self.exe: Optional[fluid.Executor] = None
        self.cur_step = 0

    def start(self, place):
        self.scheduler_agent = TrainerSchedulerAgent(
            scheduler_ep=self._scheduler_ep, worker_name=self._trainer_ep
        )
        self.scheduler_agent.start_channel()
        self.scheduler_agent.init_worker()

        self.exe = fluid.Executor(place)
        self.exe.run(self._startup_program)

    def load_job(
        self,
        startup_program: str,
        main_program: str,
        send_program: str,
        recv_program: str,
        feed_names: str,
        target_names: str,
        strategy: str,
    ):
        self._startup_program = self._load_program(startup_program)
        self._main_program = self._load_program(main_program)
        self._send_program = self._load_program(send_program)
        self._recv_program = self._load_program(recv_program)

        self._step = self._load_strategy(strategy)._inner_step
        self._feed_names = self._load_str_list(feed_names)
        self._target_names = self._load_str_list(target_names)

    def load_feed_list(self, feeds_path):
        data = []
        with open(feeds_path, "rb") as f:
            num = pickle.load(f)
            for _ in range(num):
                data.append(fluid.data(**pickle.load(f)))
        return data

    @staticmethod
    def _load_strategy(input_file):

        return pickle.load(open(input_file, "rb"))

    def reset(self):
        self.cur_step = 0

    def run_with_epoch(self, reader, feeder, fetch, num_epoch):
        self._logger.debug("begin to run recv program")
        self.exe.run(self._recv_program)
        self._logger.debug("recv done")
        epoch = 0
        for i in range(num_epoch):
            for data in reader():
                acc = self.exe.run(
                    self._main_program, feed=feeder.feed(data), fetch_list=fetch
                )
                print(f"acc: {acc}")
            self.cur_step += 1
            epoch += 1
        self._logger.debug("begin to run send program")
        self.exe.run(self._send_program)

    def run(self, feed, fetch):
        self._logger.debug(
            f"begin to run FedAvgTrainer, cur_step={self.cur_step}, inner_step={self._step}"
        )
        if self.cur_step % self._step == 0:
            self._logger.debug("run recv program start")
            self.exe.run(self._recv_program)
            self._logger.debug("run recv program done")

        self._logger.debug("run main program start")
        loss = self.exe.run(self._main_program, feed=feed, fetch_list=fetch)
        self._logger.debug("run main program done")

        if self.cur_step % self._step == 0:
            self._logger.debug("run send program start")
            self.exe.run(self._send_program)
            self._logger.debug("run send program done")
        self.cur_step += 1
        return loss

    def save_model(self, model_path):
        fluid.io.save_inference_model(
            dirname=model_path,
            feeded_var_names=self._feed_names,
            target_vars=[
                self._main_program.global_block().var(fetch_var_name)
                for fetch_var_name in self._target_names
            ],
            executor=self.exe,
            main_program=self._main_program,
        )
