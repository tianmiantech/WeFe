

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
