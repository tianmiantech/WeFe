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
from visualfl_deploy import __BASE_NAME__

app = typer.Typer(help="services [start|stop] tools")
all_app = typer.Typer(help="[start|stop] all services")

cluster_manager_app = typer.Typer(help="[start|stop] cluster manager service")
cluster_worker_app = typer.Typer(help="[start|stop] cluster worker service")
master_app = typer.Typer(help="[start|stop] master service")
app.add_typer(all_app, name="all")
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

    # start cluster
    cluster_address_map = {}
    for cluster_config in config_dict.get("clusters", []):
        cluster_name = cluster_config["name"]
        typer.echo(f"starting cluster {cluster_name}")
        manager_config = cluster_config["manager"]
        manager_machine = machines_map[manager_config["machine"]]
        status = start_cluster_manager(
            manager_machine["ssh_string"],
            manager_machine["base_dir"],
            manager_config["port"],
        )
        typer.echo(f"start cluster {cluster_name} done, success: {status}\n")

        cluster_address = f"{manager_machine['ip']}:{manager_config['port']}"
        cluster_address_map[cluster_name] = cluster_address

        # if status:
        typer.echo(f"starting cluster workers for cluster {cluster_name}")
        for worker_config in cluster_config.get("workers", []):
            typer.echo(f"starting worker {worker_config['name']}")
            worker_machine = machines_map[worker_config["machine"]]
            if "data_base_dir" in worker_machine:
                data_base_dir = worker_machine["data_base_dir"]
            else:
                data_base_dir = None
            status = start_cluster_worker(
                machine_ssh=worker_machine["ssh_string"],
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
            machine_ssh=master_machine["ssh_string"],
            machine_base_dir=master_machine["base_dir"],
            submit_port=master_config["submit_port"],
            member_id=master_config["name"],
            cluster_manager_address=cluster_address_map[master_config["cluster"]],
            local=master_config.get("local",False)
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
                machine_ssh=worker_machine["ssh_string"],
                machine_base_dir=worker_machine["base_dir"],
                name=worker_config["name"],
            )
        stop_cluster_manager(
            machine_ssh=manager_machine["ssh_string"],
            machine_base_dir=manager_machine["base_dir"],
            manager_port=manager_config["port"],
        )

    # stop master
    for master_config in config_dict.get("masters", []):
        typer.echo(f"stopping master {master_config['name']}")
        master_machine = machines_map[master_config["machine"]]
        stop_master(
            machine_ssh=master_machine["ssh_string"],
            machine_base_dir=master_machine["base_dir"],
            submit_port=master_config["submit_port"],
        )

    typer.echo("stop all")


@cluster_manager_app.command(name="start", help="start cluster manager")
def start_cluster_manager(
    machine_ssh: str = typer.Argument(..., help="machine ssh string: user@host:port"),
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    manager_port: int = typer.Argument(
        ..., help="port number for cluster manager to serve"
    ),
):
    with fabric.Connection(machine_ssh) as c:
        with c.cd(machine_base_dir):
            if c.run(
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/cluster_manager.sh start {manager_port}",
                warn=True,
            ).failed:
                typer.echo(f"failed: can't start cluster manager")
                return False
            else:
                typer.echo(
                    f"{machine_ssh}:{machine_base_dir} started cluster manager: port={manager_port}"
                )
                return True


@cluster_manager_app.command(name="stop", help="stop cluster manager")
def stop_cluster_manager(
    machine_ssh: str = typer.Argument(..., help="machine ssh string: user@host:port"),
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    manager_port: int = typer.Argument(
        ..., help="port number for cluster manager to serve"
    ),
):
    with fabric.Connection(machine_ssh) as c:
        with c.cd(machine_base_dir):
            if c.run(
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/cluster_manager.sh stop {manager_port}",
                warn=True,
            ).failed:
                typer.echo(f"failed: can't stop cluster manager")
            else:
                typer.echo(
                    f"success: {machine_ssh}:{machine_base_dir} stop cluster manager: port={manager_port}"
                )


@cluster_worker_app.command(name="start", help="start cluster worker")
def start_cluster_worker(
    machine_ssh: str = typer.Argument(..., help="machine ssh string: user@host:port"),
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
    with fabric.Connection(machine_ssh) as c:
        with c.cd(machine_base_dir):
            if c.run(
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/cluster_worker.sh start "
                f"{name} {local_ip} {port_start} {port_end} {max_tasks} {cluster_manager_address} {data_base_dir}",
                warn=True,
            ).failed:
                typer.echo(f"failed: can't start cluster worker named {name}")
                return False
            else:
                typer.echo(
                    f"{machine_ssh}:{machine_base_dir} started cluster worker: name={name}"
                )
                return True


@cluster_worker_app.command(name="stop", help="stop cluster worker")
def stop_cluster_worker(
    machine_ssh: str = typer.Argument(..., help="machine ssh string: user@host:port"),
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    name: str = typer.Argument(..., help="worker name"),
):
    with fabric.Connection(machine_ssh) as c:
        with c.cd(machine_base_dir):
            if c.run(
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/cluster_worker.sh stop {name}",
                warn=True,
            ).failed:
                typer.echo(f"failed: can't stop cluster worker")
            else:
                typer.echo(
                    f"success: {machine_ssh}:{machine_base_dir} stop cluster worker: name={name}"
                )


@master_app.command(name="start", help="start master")
def start_master(
    machine_ssh: str = typer.Argument(..., help="machine ssh string: user@host:port"),
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    submit_port: int = typer.Argument(..., help="submit port"),
    member_id: str = typer.Argument(..., help="party id"),
    cluster_manager_address: str = typer.Argument(..., help="cluster manager address"),
    local: bool = typer.Argument(..., help="is local template"),
):
    with fabric.Connection(machine_ssh) as c:
        with c.cd(machine_base_dir):
            if c.run(
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/master.sh start "
                f"{submit_port} {member_id} {cluster_manager_address} {local}",
                warn=True,
            ).failed:
                typer.echo(f"failed: can't start master at port {submit_port}")
                return False
            else:
                typer.echo(
                    f"{machine_ssh}:{machine_base_dir} started master: port={submit_port}"
                )
                return True


@master_app.command(name="stop", help="stop master")
def stop_master(
    machine_ssh: str = typer.Argument(..., help="machine ssh string: user@host:port"),
    machine_base_dir: str = typer.Argument(..., help="deployed base name"),
    submit_port: int = typer.Argument(..., help="submit port"),
):
    with fabric.Connection(machine_ssh) as c:
        with c.cd(machine_base_dir):
            if c.run(
                f"PYTHON_EXECUTABLE={os.path.join(machine_base_dir, 'venv/bin/python')} "
                f"{__BASE_NAME__}/script/master.sh stop {submit_port}",
                warn=True,
            ).failed:
                typer.echo(f"failed: can't stop master")
            else:
                typer.echo(
                    f"success: {machine_ssh}:{machine_base_dir} stop master: port={submit_port}"
                )


if __name__ == '__main__':
    start_all()