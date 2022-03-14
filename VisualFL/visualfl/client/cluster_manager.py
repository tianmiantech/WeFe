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



import asyncio

import click


@click.command(name="start-manager")
@click.option("--port", type=int, required=True, help="cluster manager address")
def start_manager(port):
    """
    start manager
    """
    from visualfl.utils import logger

    logger.set_logger("manager")
    from visualfl.manager import ClusterManager

    loop = asyncio.get_event_loop()
    manager = ClusterManager(
        port=port,
    )
    try:
        loop.run_until_complete(manager.start())
        click.echo(f"cluster manager start")
        loop.run_forever()
    except KeyboardInterrupt:
        click.echo("keyboard interrupted")

    finally:
        loop.run_until_complete(manager.stop())
        click.echo(f"cluster manager server stop")
        loop.close()


if __name__ == "__main__":
    start_manager()
