import click
import asyncio

from visualfl import extensions
from visualfl.paddle_fl.executor import ProcessExecutor
from visualfl.utils.tools import load_from_file
from pathlib import Path
from visualfl.utils.exception import VisualFLExtensionException

@click.command()
@click.option("--job_id", type=str, required=True)
@click.option("--task_id", type=str, required=True)
@click.option("--task_type", type=str, required=True)
@click.option("--scheduler-ep", type=str, required=True)
@click.option("--trainer-id", type=int, required=False)
@click.option("--trainer-ep", type=str, required=False)
@click.option("--compile-config-path", type=str, required=True)

def run_task(job_id,task_id,task_type,scheduler_ep,trainer_id,trainer_ep,
             compile_config_path):
    task_class = extensions.get_task_class(task_type)
    if task_class is None:
        raise VisualFLExtensionException(
            f"task type {task_type} not found"
        )
    config_json = load_from_file(Path(compile_config_path).joinpath("config.json"))
    program = config_json.get("program")
    task = task_class.load(
        job_id=job_id,
        task_id=task_id,
        scheduler_ep=scheduler_ep,
        trainer_id=trainer_id,
        trainer_ep=trainer_ep,
        program=program,)
    executor = ProcessExecutor(Path(compile_config_path))

    loop = asyncio.get_event_loop()
    try:
        loop.run_until_complete(task.exec(executor))
    finally:
        loop.close()


if __name__ == '__main__':
    run_task()