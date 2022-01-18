import os

from action.base_action import BaseAction
from util import database


class UpdateMysqlAction(BaseAction):

    def run(self):
        # 下载并解压文件
        zip_output_dir = self.download_and_unzip()

        # 将 zip_output_dir 目录中的所有 sql 文件按文件名排序后逐个执行
        files = os.listdir(zip_output_dir)
        print("解压后的文件列表：", files)
        files.sort()

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

        print("开始执行 sql：" + file_path)
        sql = self.extract_sql_list(file_path)
        # database.execute(sql)
        print("sql 执行成功：" + file_path)

    def extract_sql_list(self, file_path):

        with open(file_path, "r", encoding='utf-8') as f:
            lines = f.readlines()

        sql_lines = ""
        # 标记当前行是否处于多行注释中
        in_comment = False
        for line in lines:
            line = line.strip()

            # 跳过单行注释
            if line.startswith("--"):
                continue
            # 跳过多行注释
            if line.startswith("/*"):
                in_comment = True
                continue

            if line.endswith("*/"):
                in_comment = False
                continue

            if in_comment:
                continue

            sql_lines += " " + line

        database.execute_sql_list(sql_lines)
        return sql_lines
