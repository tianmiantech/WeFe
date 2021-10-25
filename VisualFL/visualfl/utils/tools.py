import json
import typing
from pathlib import Path
from ruamel import yaml

def load_from_file(path: typing.Union[str, Path]):
    """
    Loads conf content from json or yaml file. Used to read in parameter configuration
    Parameters
    ----------
    path: str, path to conf file, should be absolute path

    Returns
    -------
    dict, parameter configuration in dictionary format

    """
    if isinstance(path, str):
        path = Path(path)
    config = {}
    if path is not None:
        file_type = path.suffix
        with path.open("r") as f:
            if file_type in ['.yml', '.yaml'] :
                config.update(yaml.safe_load(f))
            elif file_type == ".json":
                config.update(json.load(f))
            else:
                raise ValueError(f"Cannot load conf from file type {file_type}")
    return config