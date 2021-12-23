
from pathlib import Path

import click
from paddle import fluid


@click.command()
@click.option(
    "--main-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--startup-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
def fl_server(
    startup_program,
    main_program,
):
    def _load_job_from_file(path):
        with Path(path).open("rb") as f:
            return fluid.Program.parse_from_string(f.read())

    server_startup_program = _load_job_from_file(Path(startup_program))
    server_main_program = _load_job_from_file(Path(main_program))
    exe = fluid.Executor(fluid.CPUPlace())
    exe.run(server_startup_program)
    exe.run(server_main_program)



if __name__ == "__main__":
    fl_server()
