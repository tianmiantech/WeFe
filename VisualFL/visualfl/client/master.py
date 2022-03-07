

import asyncio

import click


@click.command()
@click.option("--member-id", type=str, required=True, help="member id")
@click.option("--submitter-port", type=int, required=True, help="submitter port")
@click.option(
    "--cluster-address", type=str, required=True, help="cluster manager address"
)
@click.option(
    "--local", type=bool, required=False, help="is local template"
)
def start_master(member_id, submitter_port, cluster_address,local=False):
    """
    start master
    """
    from visualfl.utils import logger

    logger.set_logger(f"master-{member_id}")
    from visualfl.master import Master

    loop = asyncio.get_event_loop()
    master = Master(
        member_id=member_id,
        cluster_address=cluster_address,
        rest_port=submitter_port,
        local=local
    )
    try:
        loop.run_until_complete(master.start())
        click.echo(f"master started")
        loop.run_forever()
    except KeyboardInterrupt:
        click.echo("keyboard interrupted")
    finally:
        loop.run_until_complete(master.stop())
        click.echo(f"master stop")
        loop.close()


if __name__ == "__main__":
    start_master()
