# coding=utf-8

import os

# 全部配置项
CONFIG_MAP: dict = {}


class Keys:
    DB_MYSQL_HOST = "db.mysql.host"
    DB_MYSQL_PORT = "db.mysql.port"
    DB_MYSQL_DATABASE = "db.mysql.database"
    DB_MYSQL_USERNAME = "db.mysql.username"
    DB_MYSQL_PASSWORD = "db.mysql.password"


def get(key):
    """
    获取指定配置项
    """
    return CONFIG_MAP[key]


def load(path):
    """
    加载 config.properties
    """
    if not os.path.exists(path):
        return

    with open(path, encoding="utf8") as fp:
        lines = fp.readlines()
        for line in lines:
            line = line.strip()
            if not line or line.strip().startswith("#"):
                continue

            array = line.split('=')
            key = array[0]
            value = array[1]
            CONFIG_MAP[key] = value
