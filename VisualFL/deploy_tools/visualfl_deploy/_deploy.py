
import os
from pathlib import Path
import subprocess
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
            _install_depends(machine)
    typer.echo(f"deploy done")

def _upload_code(machine):
    tarfile = os.path.abspath(__visualfl_tarball__)
    base_dir = Path(machine["base_dir"])
    subprocess.run(f"mkdir -p {base_dir};"
                   f"cd {str(base_dir)};"
                   f"tar -xf {tarfile} -C {base_dir}",shell=True)

def _maybe_create_python_venv(machine: dict):
    try:
       subprocess.run(f"{machine['python_for_venv_create']} "
            f"-c 'import sys; assert sys.version_info.major >= 3 and sys.version_info.minor >= 7'",
            shell=True,check=True)
    except Exception:
        raise RuntimeError(f"python executable {machine['python']} not valid")

    base_dir = Path(machine["base_dir"])
    subprocess.run(f"mkdir -p {base_dir}",shell=True)
    try:
        subprocess.run(f"test -f {base_dir.joinpath('venv/bin/python')}",shell=True, check=True)
    except Exception:
        subprocess.run(f"{machine['python_for_venv_create']} -m venv venv",shell=True)

def _install_depends(machine):
    base_dir = Path(machine["base_dir"])
    subprocess.run(f"cd {str(base_dir)};"
                   f"venv/bin/python -m pip install -U pip --quiet;"
                   f"venv/bin/python -m pip install -r {__BASE_NAME__}/requirements.txt --log depends_install.log --quiet",shell=True)