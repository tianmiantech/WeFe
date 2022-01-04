
import asyncio
import subprocess
import os
from pathlib import Path
from typing import Optional

from visualfl.utils.logger import Logger
from visualfl import VISUALFL_DATA_BASE_ENV
from visualfl.paddle_fl.abs.executor import Executor


class ProcessExecutor(Executor,Logger):

    def __init__(self, working_dir: Path, data_dir=None):
        super().__init__(working_dir)
        self._data_dir = data_dir

    async def execute(self, cmd) -> Optional[int]:
        self.info(f"execute cmd {cmd} at {self.working_dir}")
        try:
            env = os.environ.copy()
            if self._data_dir is not None:
                env[VISUALFL_DATA_BASE_ENV] = self._data_dir
            sub = await asyncio.subprocess.create_subprocess_shell(
                cmd, shell=True, cwd=self.working_dir, env=env
            )
            await sub.communicate()
            return sub.returncode,sub.pid

        except Exception as e:
            self.error(e)

    def syncexecute(self, cmd) -> Optional[int]:
        self.info(f"execute cmd {cmd} at {self.working_dir}")
        try:
            env = os.environ.copy()
            if self._data_dir is not None:
                env[VISUALFL_DATA_BASE_ENV] = self._data_dir

            p = subprocess.Popen(cmd,shell=True, cwd=self.working_dir, env=env)
            p.communicate()
            return p.returncode,p.pid
        except Exception as e:
            self.error(e)


