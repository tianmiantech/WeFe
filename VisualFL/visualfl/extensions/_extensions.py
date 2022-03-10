

import importlib
import json
from pathlib import Path
from typing import Optional, MutableMapping, Type

import jsonschema
import yaml

from visualfl.paddle_fl.job import PaddleFLJob as Job
from visualfl.paddle_fl.abs.task import Task
from visualfl.utils.exception import VisualFLExtensionException
from visualfl.utils.logger import Logger


class _ExtensionLoader(Logger):
    _job_classes: Optional[MutableMapping[str, Type[Job]]] = None
    _job_schema_validator: Optional[MutableMapping] = None
    _task_classes: Optional[MutableMapping[str, Type[Task]]] = None

    @classmethod
    def _load(cls):
        if cls._job_classes is not None:
            return cls

        cls._job_classes = {}
        cls._task_classes = {}
        cls._job_schema_validator = {}
        path = Path(__file__).parent.joinpath(
            "conf/extensions.yaml"
        )
        cls.trace(f"load extension configuration from {path}")
        with open(path) as f:
            try:
                extensions = yaml.safe_load(f)
            except yaml.YAMLError as e:
                raise VisualFLExtensionException("load extension failed") from e
            finally:
                cls.trace_lazy(
                    "extension configuration:\n{yaml_config}",
                    yaml_config=lambda: yaml.safe_dump(extensions, indent=2),
                )

            for extension_name, configs in extensions.items():
                cls.trace(f"loading extension: {extension_name}")

                cls.trace(f"loading job classes from extension {extension_name}")
                for extension_job in configs.get("jobs", []):
                    job_module_name, job_cls_name = extension_job["loader"].split(":")
                    module = importlib.import_module(job_module_name)
                    job_cls = getattr(module, job_cls_name)
                    if not issubclass(job_cls, Job):
                        raise VisualFLExtensionException(
                            f"JobLoader expected, {job_cls} found"
                        )
                    cls._job_classes[extension_job["name"]] = job_cls

                    if "schema" in extension_job:
                        with path.parent.joinpath(extension_job["schema"]).open() as g:
                            schema = json.load(g)
                            print(schema)
                            cls._job_schema_validator[
                                extension_job["name"]
                            ] = jsonschema.Draft7Validator(schema)
                    else:
                        cls._job_schema_validator[
                            extension_job["name"]
                        ] = jsonschema.Draft7Validator({})

                cls.trace(f"loading task classes from extension {extension_name}")
                for extension_task in configs.get("tasks", []):
                    loader_module, loader_cls = extension_task["loader"].split(":")
                    module = importlib.import_module(loader_module)
                    loader = getattr(module, loader_cls)
                    if not issubclass(loader, Task):
                        raise VisualFLExtensionException(
                            f"JobLoader expected, {loader} found"
                        )
                    cls._task_classes[extension_task["name"]] = loader
        cls.trace_lazy(
            "loading extensions done. job classes: {job_classes}, task classes: {task_classes}",
            job_classes=lambda: cls._job_classes,
            task_classes=lambda: cls._task_classes,
        )
        return cls

    @classmethod
    def _load_schema(cls):
        if cls._job_schema_validator is not None:
            return cls

        cls._job_schema_validator = {}
        path = Path(__file__).parent.joinpath(
            "conf/extensions.yaml"
        )
        cls.trace(f"load extension configuration from {path}")
        with open(path) as f:
            try:
                extensions = yaml.safe_load(f)
            except yaml.YAMLError as e:
                raise VisualFLExtensionException("load extension failed") from e
            finally:
                cls.trace_lazy(
                    "extension configuration:\n{yaml_config}",
                    yaml_config=lambda: yaml.safe_dump(extensions, indent=2),
                )

            for extension_name, configs in extensions.items():
                cls.trace(f"loading extension: {extension_name}")

                for extension_job in configs.get("jobs", []):
                    if "schema" in extension_job:
                        with path.parent.joinpath(extension_job["schema"]).open() as g:
                            schema = json.load(g)
                            cls._job_schema_validator[
                                extension_job["name"]
                            ] = jsonschema.Draft7Validator(schema)
                    else:
                        cls._job_schema_validator[
                            extension_job["name"]
                        ] = jsonschema.Draft7Validator({})
        return cls

    @classmethod
    def get_job_class(cls, name):
        return cls._load()._job_classes.get(name)

    @classmethod
    def get_task_class(cls, name):
        return cls._load()._task_classes.get(name)

    @classmethod
    def get_job_schema_validator(cls, name):
        return cls._load_schema()._job_schema_validator.get(name)


def get_job_class(name) -> Type[Job]:
    return _ExtensionLoader.get_job_class(name)


def get_task_class(name) -> Type[Task]:
    return _ExtensionLoader.get_task_class(name)


def get_job_schema_validator(name):
    return _ExtensionLoader.get_job_schema_validator(name)


if __name__ == '__main__':
    loader = get_job_class("paddle_fl")
