
import os
import shutil

import typer
from visualfl_deploy import __template__

app = typer.Typer(help="template tools")


@app.command(name="cluster")
def generate():
    """
    generate template
    """
    shutil.copy(os.path.join(__template__, "template.yaml"), os.getcwd())


@app.command(name="standalone")
def standalone_template():
    """
    generate template for standalone deploy
    """
    shutil.copy(os.path.join(__template__, "standalone_template.yaml"), os.getcwd())

