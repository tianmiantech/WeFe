
import abc
from typing import List

from visualfl.protobuf import job_pb2


class Job(metaclass=abc.ABCMeta):

    job_type: str

    def __init__(self, job_id: str):
        self.job_id = job_id

    @property
    def resource_required(self):
        return None

    def set_required_resource(self, response):
        ...

    async def compile(self):
        ...

    async def infer(self):
        ...

    @abc.abstractmethod
    def generate_aggregator_tasks(self) -> List[job_pb2.Task]:
        ...

    @abc.abstractmethod
    def generate_trainer_tasks(self) -> List[job_pb2.Task]:
        ...

    @classmethod
    @abc.abstractmethod
    def load(cls, job_id: str,role: str, member_id: str, config, algorithm_config) -> "Job":
        ...

    def generate_task_id(self, task_name):
        return f"{self.job_id}-task_{task_name}"
