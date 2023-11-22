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


from pathlib import Path

import click
from paddle import fluid


@click.command()
@click.option("--job-id", type=str, required=True)
@click.option(
    "--main-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--startup-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
def fl_server(
    job_id,
    startup_program,
    main_program,
):
    def _load_job_from_file(path):
        with Path(path).open("rb") as f:
            return fluid.Program.parse_from_string(f.read())

    server_startup_program = _load_job_from_file(Path(startup_program))
    server_main_program = _load_job_from_file(Path(main_program))
    exe = fluid.Executor(fluid.CPUPlace())
    exe.run(server_startup_program)
    exe.run(server_main_program)



if __name__ == "__main__":
    fl_server()
