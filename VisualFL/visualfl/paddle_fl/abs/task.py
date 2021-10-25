
import abc

from visualfl.paddle_fl.abs.executor import Executor
from visualfl.protobuf import job_pb2


class Task(metaclass=abc.ABCMeta):
    """
    A abstract Task class.
    """

    task_type: str

    def __init__(self, job_id, task_id):
        self.job_id = job_id
        self.task_id = task_id

    @abc.abstractmethod
    async def exec(self, executor: Executor) -> int:
        ...

    def __str__(self):
        return f"{self.__class__.__name__}[{self.__dict__}]"

    @classmethod
    @abc.abstractmethod
    def deserialize(cls, pb: job_pb2.Task) -> "Task":
        ...
