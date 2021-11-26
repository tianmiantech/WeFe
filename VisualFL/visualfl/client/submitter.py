

import urllib.parse
from pathlib import Path

import click
import aiohttp
import asyncio
import json

import yaml

from visualfl import extensions


@click.group()
def cli():
    ...


def post(endpoint, path, json_data):
    async def post_co():
        url = urllib.parse.urljoin(f"http://{endpoint}", path)
        async with aiohttp.ClientSession() as session:
            async with session.post(
                url, json=json_data
            ) as resp:
                print(resp.status)
                print(json.dumps(await resp.json(), indent=2))
                resp.raise_for_status()

    loop = asyncio.get_event_loop()
    loop.run_until_complete(post_co())


@cli.command()
@click.option(
    "--config",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--endpoint",
    type=str,
    required=True,
)
def submit(endpoint, config):

    base = Path(config)
    with base.open("r") as f:
        config_json = yaml.load(f, yaml.Loader)
    job_id = config_json.get("job_id")
    job_type = config_json.get("job_type")
    role = config_json.get("role")
    member_id = config_json.get("member_id")
    job_config = config_json.get("job_config")

    algorithm_config_path = base.parent.joinpath(
        config_json.get("algorithm_config")
    ).absolute()
    with algorithm_config_path.open("r") as f:
        algorithm_config_string = f.read()

    extensions.get_job_schema_validator(job_type).validate(job_config)
    post(
        endpoint,
        "submit",
        dict(
            job_id=job_id,
            job_type=job_type,
            role=role,
            member_id=member_id,
            job_config=job_config,
            algorithm_config=algorithm_config_string,
        ),
    )



if __name__ == "__main__":
    cli()
