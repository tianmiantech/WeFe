
import os
from pathlib import Path

import subprocess
import typer
import yaml
from visualfl_deploy import __BASE_NAME__

app = typer.Typer(help="services [start|stop] tools")
all_app = typer.Typer(help="[start|stop] all services")

coordinator_app = typer.Typer(help="[start|stop] coordinator service")
cluster_manager_app = typer.Typer(help="[start|stop] cluster manager service")
cluster_worker_app = typer.Typer(help="[start|stop] cluster worker service")
master_app = typer.Typer(help="[start|stop] master service")
app.add_typer(all_app, name="all")
app.add_typer(coordinator_app, name="coordinator")
app.add_typer(cluster_manager_app, name="cluster-manager")
app.add_typer(cluster_worker_app, name="cluster-worker")
app.add_typer(master_app, name="master")


@all_app.command(name="start", help="start all services")
def start_all(
    config: Path = typer.Argument(..., exists=True, file_okay=True, dir_okay=False)
):
    with config.open() as f:
        config_dict = yaml.safe_load(f)

    machines_map = {}
    for machine_config in config_dict["machines"]:
        machines_map[machine_config["name"]] = machine_config

    # start coordinator
    # coordinator_config = config_dict["coordinator"]
    # if coordinator_config.get("machine"):
    #     typer.echo(f"staring coordinator {coordinator_config['name']}")
    #     coordinator_machine = machines_map[coordinator_config["machine"]]
    #     status = start_coordinator(
    #         coordinator_machine["base_dir"],
    #         coordinator_config["port"],
    #     )
    #     coordinator_address = (
    #         f"{coordinator_machine['ip']}:{coordinator_config['port']}"
    #     )
    #     typer.echo(
    #         f"start coordinator {coordinator_config['name']} done, success: {status}\n"
    #     )
    # else:
    #     coordinator_address = f"{coordinator_config['ip']}:{coordinator_config['port']}"

    # start cluster
    cluster_address_map = {}
    for cluster_config in config_dict.get("clusters", []):
        cluster_name = cluster_config["name"]
        typer.echo(f"starting cluster {cluster_name}")
        manager_config = cluster_config["manager"]
        manager_machine = machines_map[manager_config["machine"]]
        status = start_cluster_manager(
            manager_machine["base_dir"],
            manager_config["port"],
        )
        typer.echo(f"start cluster {cluster_name} done, success: {status}\n")

        cluster_address = f"{manager_machine['ip']}:{manager_config['port']}"
        cluster_address_map[cluster_name] = cluster_address
        if status:
            typer.echo(f"starting cluster workers for cluster {cluster_name}")

            for worker_config in cluster_config.get("workers", []):
                typer.echo(f"starting worker {worker_config['name']}")
                worker_machine = machines_map[worker_config["machine"]]
                if "data_base_dir" in worker_machine:
                    data_base_dir = worker_machine["data_base_dir"]
                else:
                    data_base_dir = None
                status = start_cluster_worker(
                    machine_base_dir=worker_machine["base_dir"],
                    name=worker_config["name"],
                    local_ip=worker_machine["ip"],
                    port_start=int(worker_config["ports"].split("-")[0]),
                    port_end=int(worker_config["ports"].split("-")[1]),
                    max_tasks=worker_config["max_tasks"],
                    cluster_manager_address=cluster_address,
                    data_base_dir=data_base_dir,
                )
                typer.echo(
                    f"start worker {worker_config['name']} done, success: {status}\n"
                )

    # start master
    for master_config in config_dict.get("masters", []):
        typer.echo(f"starting master {master_config['name']}")
        master_machine = machines_map[master_config["machine"]]
        status = start_master(
            machine_base_dir=master_machine["base_dir"],
            submit_port=master_config["submit_port"],
            member_id=master_config["name"],
            cluster_manager_address=cluster_address_map[master_config["cluster"]],
        )
        typer.echo(f"start master {master_config['name']} done, success: {status}\n")

    typer.echo()


@all_app.command(name="stop", help="stop all services")
def stop_all(
    config: Path = typer.Argument(..., exists=True, file_okay=True, dir_okay=False)
):
    with config.open() as f:
        config_dict = yaml.safe_load(f)

    machines_map = {}
    for machine_config in config_dict["machines"]:
        machines_map[machine_config["name"]] = machine_config

    # stop coordinator
    # coordinator_config = config_dict["coordinator"]
    # if coordinator_config.get("machine"):
    #     coordinator_machine = machines_map[coordinator_config["machine"]]
    #     stop_coordinator(
    #         machine_base_dir=coordinator_machine["base_dir"],
    #         coordinator_port=coordinator_config["port"],
    #     )

    # stop cluster
    for cluster_config in config_dict.get("clusters", []):
        cluster_name = cluster_config["name"]
        typer.echo(f"stopping cluster {cluster_name}")
        manager_config = cluster_config["manager"]
        manager_machine = machines_map[manager_config["machine"]]

        for worker_config in cluster_config.get("workers", []):
            typer.echo(f"stopping worker {worker_config['name']}")
            worker_machine = machines_map[worker_config["machine"]]
            stop_cluster_worker(
                machine_base_dir=worker_machine["base_dir"],
                name=worker_config["name"],
            )
        stop_cluster_manager(
            machine_base_dir=manager_machine["base_dir"],
            manager_port=manager_config["port"],
        )

    # stop master
    for master_config in config_dict.get("masters", []):
        typer.echo(f"stopping master {master_config['name']}")
        master_machine = machines_map[master_config["machine"]]
        stop_master(
            machine_base_dir=master_machine["base_dir"],
            submit_port=master_config["submit_port"],
        )

    typer.echo("stop all")


@coordinator_app.command(name="start", help="start coordinator")
def start_coordinator(
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    coordinator_port: int = typer.Argument(
        ..., help="port number for coordinator to serve"
    ),
):
        try:
            subprocess.run(f"cd {machine_base_dir}", shell=True)
            subprocess.run(
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/coordinator.sh start {coordinator_port}",
                shell=True,check=True)
        except Exception:
            typer.echo(f"failed: can't start coordinator")
            return False
        else:
            typer.echo(
                f"{machine_base_dir} started coordinator: port={coordinator_port}"
            )
            return True


@coordinator_app.command(name="stop", help="stop coordinator")
def stop_coordinator(
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    coordinator_port: int = typer.Argument(
        ..., help="port number for coordinator to serve"
    ),
):
        try:
            subprocess.run(f"cd {machine_base_dir};"
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/coordinator.sh stop {coordinator_port}",
                shell=True,check=True)
        except Exception:
            typer.echo(f"failed: can't stop coordinator")
        else:
            typer.echo(
                f"success: {machine_base_dir} stop coordinator: port={coordinator_port}"
            )


@cluster_manager_app.command(name="start", help="start cluster manager")
def start_cluster_manager(
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    manager_port: int = typer.Argument(
        ..., help="port number for cluster manager to serve"
    ),
):
    try:
        subprocess.run(f"cd {machine_base_dir};"
            f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
            f"{__BASE_NAME__}/script/cluster_manager.sh start {manager_port}",
            shell=True,check=True)
    except Exception:
            typer.echo(f"failed: can't start cluster manager")
            return False
    else:
        typer.echo(
            f"{machine_base_dir} started cluster manager: port={manager_port}"
        )
        return True


@cluster_manager_app.command(name="stop", help="stop cluster manager")
def stop_cluster_manager(
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    manager_port: int = typer.Argument(
        ..., help="port number for cluster manager to serve"
    ),
):
    try:
        subprocess.run(f"cd {machine_base_dir};"
            f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
            f"{__BASE_NAME__}/script/cluster_manager.sh stop {manager_port}",
            shell=True,check=True)
    except Exception:
        typer.echo(f"failed: can't stop cluster manager")
    else:
        typer.echo(
            f"success: {machine_base_dir} stop cluster manager: port={manager_port}"
        )


@cluster_worker_app.command(name="start", help="start cluster worker")
def start_cluster_worker(
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    name: str = typer.Argument(..., help="worker name"),
    local_ip: str = typer.Argument(..., help="local ip"),
    port_start: int = typer.Argument(..., help="port start"),
    port_end: int = typer.Argument(..., help="port start"),
    max_tasks: int = typer.Argument(..., help="num of maximum parallel tasks"),
    cluster_manager_address=typer.Argument(..., help="cluster manager address"),
    data_base_dir: str = typer.Option(None, "--data-dir", help="data dir"),
):
    if data_base_dir is None or isinstance(data_base_dir, typer.params.OptionInfo):
        data_base_dir = os.path.join(machine_base_dir, __BASE_NAME__, "data")
        try:
            subprocess.run(f"cd {machine_base_dir};"
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/cluster_worker.sh start "
                f"{name} {local_ip} {port_start} {port_end} {max_tasks} {cluster_manager_address} {data_base_dir}",
                shell=True,check=True)
        except Exception:
                typer.echo(f"failed: can't start cluster worker named {name}")
                return False
        else:
            typer.echo(
                f"{machine_base_dir} started cluster worker: name={name}"
            )
            return True


@cluster_worker_app.command(name="stop", help="stop cluster worker")
def stop_cluster_worker(
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    name: str = typer.Argument(..., help="worker name"),
):
    try:
        subprocess.run(f"cd {machine_base_dir};"
            f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
            f"{__BASE_NAME__}/script/cluster_worker.sh stop {name}",
            shell=True,check=True)
    except Exception:
        typer.echo(f"failed: can't stop cluster worker")
    else:
        typer.echo(
            f"success: {machine_base_dir} stop cluster worker: name={name}"
        )


@master_app.command(name="start", help="start master")
def start_master(
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    submit_port: int = typer.Argument(..., help="submit port"),
    member_id: str = typer.Argument(..., help="member id"),
    cluster_manager_address: str = typer.Argument(..., help="cluster manager address"),
):
    try:
        subprocess.run(f"cd {machine_base_dir};"
            f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
            f"{__BASE_NAME__}/script/master.sh start "
            f"{submit_port} {member_id} {cluster_manager_address}",
            shell=True,check=True)
    except Exception:
        typer.echo(f"failed: can't start master at port {submit_port}")
        return False
    else:
        typer.echo(
            f"{machine_base_dir} started master: port={submit_port}"
        )
        return True


@master_app.command(name="stop", help="stop master")
def stop_master(
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    submit_port: int = typer.Argument(..., help="submit port"),
):
    try:
        subprocess.run(f"cd {machine_base_dir};"
            f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
            f"{__BASE_NAME__}/script/master.sh stop {submit_port}",
            shell=True,check=True)
    except Exception:
        typer.echo(f"failed: can't stop master")
    else:
        typer.echo(
            f"success: {machine_base_dir} stop master: port={submit_port}"
            )


if __name__ == '__main__':
    start_all()