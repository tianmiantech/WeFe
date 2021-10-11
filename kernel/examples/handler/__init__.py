



from pathlib import Path

from ruamel import yaml

with Path(__file__).parent.parent.joinpath("config.yaml").resolve().open("r") as fin:
    __DEFAULT_CONFIG: dict = yaml.safe_load(fin)


def set_default_config(ip: str, port: int, log_directory: str):
    global __DEFAULT_CONFIG
    __DEFAULT_CONFIG.update(dict(ip=ip, port=port, log_directory=log_directory))


def get_default_config():
    return __DEFAULT_CONFIG
