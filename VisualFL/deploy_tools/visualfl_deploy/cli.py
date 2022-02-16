
import typer
from visualfl_deploy import _deploy, _generate_template, _service

app = typer.Typer()

app.add_typer(_deploy.app, name="deploy")
app.add_typer(_service.app, name="services")
app.add_typer(_generate_template.app, name="template")

if __name__ == "__main__":
    app()
