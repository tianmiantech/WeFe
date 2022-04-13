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

import pymysql


class DataBaseInfo:
    host: str
    port: int
    user: str
    password: str
    database: str

    def __init__(self, host: str, port: int, user: str, password: str, database: str):
        self.host = host
        self.port = port
        self.user = user
        self.password = password
        self.database = database


db_info = DataBaseInfo(
    host='localhost',
    port=3306,
    user='root',
    password="",
    database="wefe_upgrade_test01"
)


def execute_file(file_path: str):
    command = "mysql -h{host} -P{port} -u{user} " \
        .format(
        host=db_info.host,
        port=db_info.port,
        user=db_info.user
    )

    if len(db_info.password) > 0:
        command += " -p'{password}' ".format(password=db_info.password)

    command += "< '{file_path}' ".format(file_path=file_path)

    print(command)


def backup_database(output_file_path: str):
    command = "mysqldump -h{host} -P{port} -u{user} " \
        .format(
        host=db_info.host,
        port=db_info.port,
        user=db_info.user
    )

    if len(db_info.password) > 0:
        command += " -p'{password}' ".format(password=db_info.password)

    command += " --column-statistics=0 " \
               "--set-gtid-purged=OFF " \
               "--databases {database} " \
               "> '{output_file_path}' " \
        .format(
        database=db_info.database,
        output_file_path=output_file_path
    )

    print(command)


def create_connection():
    return pymysql.connect(host=db_info.host,
                           port=db_info.port,
                           user=db_info.user,
                           password=db_info.password,
                           database=db_info.database)


def select_one(sql: str):
    connection = create_connection()
    cursor = connection.cursor()
    cursor.execute(sql)
    data = cursor.fetchone()
    connection.close()
    return data


def select_list(sql: str):
    connection = create_connection()
    cursor = connection.cursor()
    cursor.execute(sql)
    data = cursor.fetchall()
    connection.close()
    return data


def execute(sql: str):
    connection = create_connection()
    cursor = connection.cursor()
    data = cursor.execute(sql)
    connection.commit()
    connection.close()
    return data


def execute_sql_list(sql_list: list):
    print('开始事务...')
    connection = create_connection()
    cursor = connection.cursor()

    current_sql = ""
    try:
        for sql in sql_list:
            # 跳过空行
            if len(sql) == 0:
                continue

            current_sql = sql
            cursor.execute(sql)
            print('sql 执行成功：', current_sql)
    except Exception as e:
        print('sql 执行失败：', current_sql)
        print('事务执行失败，正在回滚...')
        connection.rollback()
        print('事务已回滚！')
        raise e
    else:
        connection.commit()
    finally:
        connection.close()


if __name__ == '__main__':
    execute_file("test.sql")
