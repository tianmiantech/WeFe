import os

from action.base_action import BaseAction
from util import database


class UpdateMysqlAction(BaseAction):

    def run(self):
        # 备份
        backup_output_path = os.path.join(
            self.workspace,
            database.db_info.database + ".sql"
        )
        print("开始备份数据库至：" + backup_output_path)
        database.execute_file(backup_output_path)
        print("数据库整库备份成功：" + backup_output_path)
        print()

        # 下载并解压文件
        zip_output_dir = self.download_and_unzip()

        # 将 zip_output_dir 目录中的所有 sql 文件按文件名排序后逐个执行
        files = os.listdir(zip_output_dir)
        print("解压后的文件列表：", files)
        files.sort()

        # 逐个执行sql文件
        for file_name in files:
            file_path = os.path.join(
                zip_output_dir,
                file_name
            )
            self.execute_sql_file(file_path)

    def execute_sql_file(self, file_path):
        if not os.path.isfile(file_path):
            print("非 sql 文件，跳过：" + file_path)
            return

        if not file_path.endswith(".sql"):
            print("非 sql 文件，跳过：" + file_path)
            return

        print("开始执行 sql 文件：" + file_path)
        database.execute_file(file_path)
        print("sql 文件执行成功：" + file_path)
        print()
