# Copyright 2021 Tianmian Tech. All Rights Reserved.
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


from pathlib import Path
from typing import Any, Union

from google.protobuf import text_format
from loguru import logger

from visualfl import __logs_dir__

__BASE_LOGGER = None


def set_logger(filename="unnamed"):
    log_dir = Path(__logs_dir__)
    if not log_dir.exists():
        log_dir.mkdir(exist_ok=True)

    log_format = (
        "<red>[{extra[base]}]</red>"
        "<green>{time:YYYY-MM-DD HH:mm:ss.SSS}</green> | "
        "<level>{level: <8}</level> | "
        "<cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan>:<level>{message}</level>"
    )
    config = {
        "handlers": [
            # dict(sink=sys.stdout, format=log_format, level="DEBUG"),
            dict(
                sink=f"{log_dir.joinpath(filename)}.log",
                format=log_format,
                level="DEBUG",
            ),
        ],
        "extra": {"base": "unknown"},
    }
    logger.configure(**config)
    global __BASE_LOGGER
    __BASE_LOGGER = logger


set_logger()


class Logger(object):

    _logger = None

    @classmethod
    def get_logger(cls, lazy=False):
        if cls._logger is None:
            cls._logger = logger.bind(base=cls.__name__).opt(depth=1)
        if lazy:
            return cls._logger.opt(lazy=True, depth=1)
        return cls._logger

    @classmethod
    def log(
        cls,
        __level: Union[int, str],
        __message: str,
        *args: Any,
        lazy=False,
        **kwargs: Any,
    ):
        cls.get_logger(lazy=lazy).log(__level, __message, *args, **kwargs)

    @classmethod
    def trace(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger(lazy=False).trace(__message, *args, **kwargs)

    @classmethod
    def trace_lazy(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger(lazy=True).trace(__message, *args, **kwargs)

    @classmethod
    def debug(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger().debug(__message, *args, **kwargs)

    @classmethod
    def debug_lazy(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger(lazy=True).debug(__message, *args, **kwargs)

    @classmethod
    def info(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger().info(__message, *args, **kwargs)

    @classmethod
    def info_lazy(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger(lazy=True).info(__message, *args, **kwargs)

    @classmethod
    def warning(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger().warning(__message, *args, **kwargs)

    @classmethod
    def error(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger().error(__message, *args, **kwargs)

    @classmethod
    def critical(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger().critical(__message, *args, **kwargs)

    @classmethod
    def exception(cls, __message: str, *args: Any, **kwargs: Any):
        cls.get_logger().exception(__message, *args, **kwargs)


def pretty_pb(pb):
    return text_format.MessageToString(pb, as_one_line=True)
