
import sys

from visualfl.paddle_fl.abs.executor import Executor
from visualfl.paddle_fl.abs.task import Task
from visualfl.protobuf import job_pb2
from visualfl.utils.exception import VisualFLWorkerException
from visualfl.protobuf import fl_job_pb2


class FLTrainer(Task):
    task_type = "fl_trainer"

    def __init__(
        self,
        job_id,
        task_id,
        web_task_id,
        scheduler_ep: str,
        trainer_id: int,
        trainer_ep: str,
        entrypoint,
        startup_program,
        main_program,
        send_program,
        recv_program,
        feed_names,
        target_names,
        strategy,
        feeds,
        config_string,
        algorithm_config_string,
    ):
        super().__init__(job_id=job_id, task_id=task_id,web_task_id=web_task_id)
        self._scheduler_ep = scheduler_ep
        self._trainer_id = trainer_id
        self._trainer_ep = trainer_ep
        self._entrypoint = entrypoint
        self._startup_program = startup_program
        self._main_program = main_program
        self._send_program = send_program
        self._recv_program = recv_program
        self._feed_names = feed_names
        self._target_names = target_names
        self._strategy = strategy
        self._feeds = feeds
        self._config_string = config_string
        self._algorithm_config_string = algorithm_config_string

    @classmethod
    def deserialize(cls, pb: job_pb2.Task) -> "FLTrainer":
        if pb.task_type != cls.task_type:
            raise VisualFLWorkerException(
                f"try to deserialize task_type {pb.task_type} by {cls.task_type}"
            )
        worker_task_pb = fl_job_pb2.PaddleFLWorkerTask()
        pb.task.Unpack(worker_task_pb)
        return FLTrainer(
            job_id=pb.job_id,
            task_id=pb.task_id,
            web_task_id=pb.web_task_id,
            scheduler_ep=worker_task_pb.scheduler_ep,
            trainer_id=worker_task_pb.trainer_id,
            trainer_ep=worker_task_pb.trainer_ep,
            entrypoint=worker_task_pb.entrypoint,
            startup_program=worker_task_pb.startup_program,
            main_program=worker_task_pb.main_program,
            send_program=worker_task_pb.send_program,
            recv_program=worker_task_pb.recv_program,
            feed_names=worker_task_pb.feed_names,
            target_names=worker_task_pb.target_names,
            strategy=worker_task_pb.strategy,
            feeds=worker_task_pb.feeds,
            config_string=worker_task_pb.config_string,
            algorithm_config_string=worker_task_pb.algorithm_config_string,
        )

    async def exec(self, executor: Executor):
        python_executable = sys.executable
        cmd = " ".join(
            [
                f"{python_executable} -m {self._entrypoint}",
                f"--job-id={self.job_id}",
                f"--task-id={self.web_task_id}",
                f"--scheduler-ep={self._scheduler_ep}",
                f"--trainer-id={self._trainer_id}",
                f"--trainer-ep={self._trainer_ep}",
                f"--startup-program=startup_program",
                f"--main-program=main_program",
                f"--send-program=send_program",
                f"--recv-program=recv_program",
                f"--feed-names=feed_names",
                f"--target-names=target_names",
                f"--feeds=feeds",
                f"--strategy=strategy",
                f"--config config.json",
                f"--algorithm-config algorithm_config.json"
                f">{executor.stdout} 2>{executor.stderr}",
            ]
        )
        with executor.working_dir.joinpath("main_program").open("wb") as f:
            f.write(self._main_program)
        with executor.working_dir.joinpath("startup_program").open("wb") as f:
            f.write(self._startup_program)
        with executor.working_dir.joinpath("send_program").open("wb") as f:
            f.write(self._send_program)
        with executor.working_dir.joinpath("recv_program").open("wb") as f:
            f.write(self._recv_program)
        with executor.working_dir.joinpath("feed_names").open("wb") as f:
            f.write(self._feed_names)
        with executor.working_dir.joinpath("target_names").open("wb") as f:
            f.write(self._target_names)
        with executor.working_dir.joinpath("strategy").open("wb") as f:
            f.write(self._strategy)
        with executor.working_dir.joinpath("feeds").open("wb") as f:
            f.write(self._feeds)
        with executor.working_dir.joinpath("config.json").open("w") as f:
            f.write(self._config_string)
        with executor.working_dir.joinpath("algorithm_config.json").open("w") as f:
            f.write(self._algorithm_config_string)
        returncode = await executor.execute(cmd)
        if returncode is None or returncode != 0:
            raise VisualFLWorkerException(
                f"execute task: {self.task_id} failed, return code: {returncode}"
            )
