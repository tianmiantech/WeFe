
from pathlib import Path

import click
import sys
import yaml

from visualfl import extensions
from visualfl.paddle_fl.executor import ProcessExecutor
from visualfl.utils.exception import VisualFLExtensionException,VisualFLJobCompileException

@click.group()
def cli():
    ...

@cli.command()
@click.option(
    "-c",
    "--config",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "-j",
    "--job_id",
    type=str,
    required=True,
)

def compile(job_id, config):

    base = Path(config)
    with base.open("r") as f:
        config_yaml = yaml.load(f, yaml.Loader)
    job_type = config_yaml.get("job_type")
    job_config = config_yaml.get("job_config")
    algorithm_config_path = base.parent.joinpath(
        config_yaml.get("algorithm_config")
    ).absolute()
    with algorithm_config_path.open("r") as f:
        algorithm_config_string = f.read()

    extensions.get_job_schema_validator(job_type).validate(job_config)

    loader = extensions.get_job_class(job_type)
    validator = extensions.get_job_schema_validator(job_type)
    if loader is None:
        raise VisualFLExtensionException(f"job type {job_type} not supported")

    validator.validate(job_config)
    job = loader.load(
        job_id=job_id, config=job_config, algorithm_config=algorithm_config_string
    )

    executor = ProcessExecutor(job.compile_path)
    with job.compile_path.joinpath("algorithm_config.yaml").open("w") as f:
        f.write(job._algorithm_config)
    with job.compile_path.joinpath("config.json").open("w") as f:
        f.write(job._config_string)
    executable = sys.executable
    cmd = " ".join(
        [
            f"{executable} -m visualfl.{job._program}.fl_master",
            f"--ps-endpoint {job._server_endpoint}",
            f"--algorithm-config algorithm_config.yaml",
            f"--config config.json",
            f">{executor.stdout} 2>{executor.stderr}",
        ]
    )
    returncode = executor.syncexecute(cmd)
    if returncode != 0:
        raise VisualFLJobCompileException("compile error")

if __name__ == '__main__':
    compile()