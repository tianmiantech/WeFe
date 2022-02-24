# Copyright (c) 2020 The FedVision Authors. All Rights Reserved.
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
import os
from pathlib import Path

import fabric
import typer
import yaml
from visualfl_deploy import __visualfl_tarball__, __BASE_NAME__

app = typer.Typer(help="deploy tools")


@app.command()
def deploy(
    config: Path = typer.Option(
        ...,
        file_okay=True,
        exists=True,
        dir_okay=False,
        readable=True,
        resolve_path=True,
    )
):
    with config.open() as f:
        config_dict = yaml.safe_load(f)
    machines = config_dict.get("machines", [])
    typer.echo(
        f"deploying {len(machines)} machines: {[machine['name'] for machine in machines]}"
    )
    with typer.progressbar(machines, length=len(machines)) as bar:
        for machine in bar:
            _maybe_create_python_venv(machine)
            _upload_code(machine)
            _install_deps(machine)
    typer.echo(f"deploy done")


def _upload_code(machine):
    tarfile = os.path.abspath(__visualfl_tarball__)
    base_dir = Path(machine["base_dir"])
    with fabric.Connection(machine["ssh_string"]) as c:
        c.run(f"mkdir -p {base_dir}")
        with c.cd(str(base_dir)):
            c.put(tarfile, f"{base_dir}")
            c.run(f"tar -xf {tarfile} -C {base_dir}")
            c.run(f"rm {os.path.join(base_dir, os.path.basename(tarfile))}")


def _maybe_create_python_venv(machine: dict):
    with fabric.Connection(machine["ssh_string"]) as c:
        version = c.run(
            f"{machine['python_for_venv_create']} "
            f"-c 'import sys; assert sys.version_info.major >= 3 and sys.version_info.minor >= 6'",
            warn=True,
        )
        if version.failed:
            raise RuntimeError(f"python executable {machine['python']} not valid")

        base_dir = Path(machine["base_dir"])
        c.run(f"mkdir -p {base_dir}")
        with c.cd(str(base_dir)):
            if c.run(
                f"test -f {base_dir.joinpath('venv/bin/python')}", warn=True
            ).failed:
                c.run(f"{machine['python_for_venv_create']} -m venv venv")


def _install_deps(machine):
    with fabric.Connection(machine["ssh_string"]) as c:
        base_dir = Path(machine["base_dir"])
        with c.cd(str(base_dir)):
            c.run(f"venv/bin/python -m pip install -U pip --quiet")
            c.run(
                f"venv/bin/python -m pip install -r {__BASE_NAME__}/requirements.txt --log deps_install.log --quiet"
            )
