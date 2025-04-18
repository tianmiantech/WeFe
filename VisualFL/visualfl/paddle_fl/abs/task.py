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


import abc

from visualfl.paddle_fl.abs.executor import Executor
from visualfl.protobuf import job_pb2


class Task(metaclass=abc.ABCMeta):
    """
    A abstract Task class.
    """

    task_type: str

    def __init__(self, job_id, task_id,web_task_id):
        self.job_id = job_id
        self.task_id = task_id
        self.web_task_id = web_task_id

    @abc.abstractmethod
    async def exec(self, executor: Executor) -> int:
        ...

    def __str__(self):
        return f"{self.__class__.__name__}[{self.__dict__}]"

    @classmethod
    @abc.abstractmethod
    def deserialize(cls, pb: job_pb2.Task) -> "Task":
        ...
