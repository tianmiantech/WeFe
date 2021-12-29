
import sys

from visualfl.paddle_fl.abs.executor import Executor
from visualfl.paddle_fl.abs.task import Task
from visualfl.protobuf import job_pb2
from visualfl.utils.exception import VisualFLWorkerException
from visualfl.protobuf import fl_job_pb2


class FLAggregator(Task):
    task_type = "fl_aggregator"

    def __init__(
        self,
        job_id,
        task_id,
        web_task_id,
        scheduler_ep,
        main_program,
        startup_program,
        config_string,
    ):
        super().__init__(job_id=job_id, task_id=task_id,web_task_id=web_task_id)
        self._scheduler_ep = scheduler_ep
        self._main_program = main_program
        self._startup_program = startup_program
        self._config_string = config_string

    @classmethod
    def deserialize(cls, pb: job_pb2.Task) -> "FLAggregator":
        if pb.task_type != cls.task_type:
            raise VisualFLWorkerException(
                f"try to deserialize task_type {pb.task_type} by {cls.task_type}"
            )
        scheduler_task_pb = fl_job_pb2.PaddleFLAggregatorTask()
        pb.task.Unpack(scheduler_task_pb)
        return FLAggregator(
            job_id=pb.job_id,
            task_id=pb.task_id,
            web_task_id=pb.web_task_id,
            scheduler_ep=scheduler_task_pb.scheduler_ep,
            startup_program=scheduler_task_pb.startup_program,
            main_program=scheduler_task_pb.main_program,
            config_string=scheduler_task_pb.config_string,
        )

    async def exec(self, executor: Executor):
        python_executable = sys.executable
        cmd = " ".join(
            [
                f"{python_executable} -m visualfl.paddle_fl.scheduler.fl_scheduler",
                f"--scheduler-ep={self._scheduler_ep}",
                f"--startup-program=startup_program",
                f"--main-program=main_program",
                f"--config=config.json",
                f">{executor.stdout} 2>{executor.stderr}",
            ]
        )
        with executor.working_dir.joinpath("main_program").open("wb") as f:
            f.write(self._main_program)
        with executor.working_dir.joinpath("startup_program").open("wb") as f:
            f.write(self._startup_program)
        with executor.working_dir.joinpath("config.json").open("w") as f:
            f.write(self._config_string)
        returncode,pid = await executor.execute(cmd)
        if returncode != 0:
            raise VisualFLWorkerException(
                f"execute task: {self.task_id} failed, return code: {returncode}"
            )
