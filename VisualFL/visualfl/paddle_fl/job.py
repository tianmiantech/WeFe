

import json
import os.path
import sys
from pathlib import Path
from typing import List

from visualfl.paddle_fl.abs.job import Job
from visualfl.paddle_fl.executor import ProcessExecutor
from visualfl.protobuf import job_pb2, cluster_pb2
from visualfl.utils.exception import VisualFLJobCompileException
from visualfl.protobuf import fl_job_pb2
from visualfl import __basedir__,__logs_dir__
JOB_TYPE = "paddle_fl"


class PaddleFLJob(Job):
    job_type = JOB_TYPE

    @classmethod
    def load(cls, job_id,task_id, role, member_id, config, algorithm_config,callback_url=None,is_infer=False,) -> "PaddleFLJob":
        job =  PaddleFLJob(job_id=job_id,task_id=task_id,role=role,member_id=member_id)
        if is_infer:
            job._init_infer_job(config,algorithm_config,callback_url)
        else:
            job._init_fl_job(config,algorithm_config,callback_url)
        return job

    def __init__(self,job_id,task_id,role,member_id):
        super().__init__(job_id=job_id)
        self._web_task_id = task_id
        self._role = role
        self._member_id = member_id

    def _init_fl_job(self,config, algorithm_config,callback_url=None):
        self._worker_num = config["worker_num"]
        self._local_worker_num = config["local_worker_num"]
        self._local_trainer_indexs = config["local_trainer_indexs"]
        self._program = algorithm_config["program"]
        self._trainer_entrypoint = f"visualfl.algorithm.{self._program}.fl_trainer"
        self._config_string = json.dumps(config)
        self._algorithm_config = json.dumps(algorithm_config)
        self._server_endpoint = config.get("server_endpoint",None)
        self._aggregator_endpoint = config.get("aggregator_endpoint",None)
        self._aggregator_assignee = config.get("aggregator_assignee",None)
        self._callback_url = callback_url

    def _init_infer_job(self, config, algorithm_config, callback_url=None):
        self._local_trainer_indexs = config["local_trainer_indexs"]
        self._program = algorithm_config["program"]
        # self._config_string = json.dumps(config)
        # self._algorithm_config = json.dumps(algorithm_config)
        self._callback_url = callback_url
        self._use_gpu = True if config["device"].lower() == 'gpu' else False
        self._output_dir = config["output_dir"]
        self._infer_dir = config["infer_dir"]
        cur_step = config["cur_step"]
        self._weights = Path(__logs_dir__).joinpath(
                f"jobs/{self.job_id}/trainer_{self._local_trainer_indexs[0]}/checkpoint/{cur_step}")
        self._algorithm_config_path = Path(__logs_dir__).joinpath(f"jobs/{self.job_id}/master/algorithm_config.json")
        if self._program == "paddle_detection":
            architecture = algorithm_config["architecture"]
            program_full_path = os.path.join(__basedir__, 'algorithm', 'paddle_detection')
            default_config_name = 'default_algorithm_config.yml'
            self._algorithm_config_path = os.path.join(program_full_path, "configs", architecture.lower(),
                                                    default_config_name)

    @property
    def resource_required(self):
        return cluster_pb2.TaskResourceRequire.REQ(num_endpoints=2)

    # noinspection PyAttributeOutsideInit
    def set_required_resource(self, response):
        self._server_endpoint = response.endpoints[0]
        self._aggregator_endpoint = response.endpoints[1]
        self._aggregator_assignee = response.worker_id

    @property
    def compile_path(self):
        return Path(__logs_dir__).joinpath(f"jobs/{self.job_id}/master")

    @property
    def infer_path(self):
        return Path(__logs_dir__).joinpath(f"jobs/{self.job_id}/infer")

    async def compile(self):
        executor = ProcessExecutor(self.compile_path)
        with self.compile_path.joinpath("algorithm_config.json").open("w") as f:
            f.write(self._algorithm_config)
        with self.compile_path.joinpath("config.json").open("w") as f:
            f.write(self._config_string)
        executable = sys.executable
        cmd = " ".join(
            [
                f"{executable} -m visualfl.algorithm.{self._program}.fl_master",
                f"--ps-endpoint {self._server_endpoint}",
                f"--algorithm-config algorithm_config.json",
                f"--config config.json",
                f">{executor.stdout} 2>{executor.stderr}",
            ]
        )
        returncode,pid = await executor.execute(cmd)
        if returncode != 0:
            raise VisualFLJobCompileException("compile error")

    async def infer(self):
        executor = ProcessExecutor(self.infer_path)
        executable = sys.executable
        cmd = " ".join(
            [
                f"{executable} -m visualfl.algorithm.{self._program}.infer",
                f"--job_id {self.job_id}",
                f"--task_id {self._web_task_id}",
                f"--use_gpu {self._use_gpu}",
                f"--weights {self._weights}",
                f"--infer_dir {self._infer_dir}",
                f"--output_dir {self._output_dir}",
                f"-c {self._algorithm_config_path}",
                f">{executor.stdout} 2>{executor.stderr}",
            ]
        )
        returncode, pid = await executor.execute(cmd)
        if returncode != 0:
            raise VisualFLJobCompileException("infer error")

    def generate_trainer_tasks(self) -> List[job_pb2.Task]:
        tasks = []
        for i ,v in enumerate(self._local_trainer_indexs):
            tasks.append(self._generate_trainer_task_pb(v))
        return tasks

    def generate_aggregator_tasks(self) -> List[job_pb2.Task]:
        return [
            self._generate_aggregator_task_pb(),
        ]

    def _generate_trainer_task_pb(self,i):
        task_pb = job_pb2.Task(
            job_id=self.job_id,
            task_id=f"trainer_{i}",
            web_task_id=self._web_task_id,
            task_type="fl_trainer")

        trainer_pb = fl_job_pb2.PaddleFLWorkerTask(
            scheduler_ep=self._aggregator_endpoint,
            trainer_id=i,
            trainer_ep=f"trainer_{i}",
            entrypoint=self._trainer_entrypoint,
            main_program=_load_program_bytes(
                self.compile_path.joinpath(f"compile/trainer{i}/trainer.main.program")
            ),
            startup_program=_load_program_bytes(
                self.compile_path.joinpath(
                    f"compile/trainer{i}/trainer.startup.program"
                )
            ),
            send_program=_load_program_bytes(
                self.compile_path.joinpath(f"compile/trainer{i}/trainer.send.program")
            ),
            recv_program=_load_program_bytes(
                self.compile_path.joinpath(f"compile/trainer{i}/trainer.recv.program")
            ),
            feed_names=_load_program_bytes(
                self.compile_path.joinpath(f"compile/trainer{i}/feed_names")
            ),
            target_names=_load_program_bytes(
                self.compile_path.joinpath(f"compile/trainer{i}/target_names")
            ),
            strategy=_load_program_bytes(
                self.compile_path.joinpath(f"compile/trainer{i}/strategy.pkl")
            ),
            feeds=_load_program_bytes(
                self.compile_path.joinpath(f"compile/trainer{1}/feeds.pkl")
            ),
            config_string=self._config_string,
            algorithm_config_string=self._algorithm_config,
        )
        task_pb.task.Pack(trainer_pb)
        return task_pb

    def _generate_aggregator_task_pb(self):
        scheduler_pb = fl_job_pb2.PaddleFLAggregatorTask(
            scheduler_ep=self._aggregator_endpoint,
        )
        scheduler_pb.main_program = _load_program_bytes(
            self.compile_path.joinpath(f"compile/server0/server.main.program")
        )
        scheduler_pb.startup_program = _load_program_bytes(
            self.compile_path.joinpath(f"compile/server0/server.startup.program")
        )
        scheduler_pb.config_string = self._config_string

        task_pb = job_pb2.Task(
            job_id=self.job_id,
            web_task_id=self._web_task_id,
            task_id=f"aggregator",
            task_type="fl_aggregator",
            assignee=self._aggregator_assignee,
        )
        task_pb.task.Pack(scheduler_pb)
        return task_pb


def _load_program_bytes(path: Path):
    with path.open("rb") as f:
        return f.read()
