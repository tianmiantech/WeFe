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
from pathlib import Path


class Executor(object):
    def __init__(self, working_dir: Path):
        self._working_dir = working_dir
        self._working_dir.mkdir(parents=True, exist_ok=True)

    @abc.abstractmethod
    async def execute(self, cmd) -> int:
        ...

    @property
    def stderr(self):
        return "stderr"

    @property
    def stdout(self):
        return "stdout"

    @property
    def working_dir(self):
        return self._working_dir
