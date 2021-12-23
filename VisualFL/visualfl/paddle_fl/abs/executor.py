
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
