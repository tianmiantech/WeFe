import os

from action.base_action import BaseAction
from util import database


class UpdateMysqlAction(BaseAction):

    def run(self):
        # 备份数据库
        # mysqldump --column-statistics=0 -h10.1.0.120 -P3306 -uwefe -p'ou0sqsTPN!gG' --databases wefe_board_3 > wefe_board_3.sql
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

        print("开始执行 sql 文件：" + file_path)
        sql_list = self.extract_sql_list(file_path)
        database.execute_sql_list(sql_list)
        print("sql 文件执行成功：" + file_path)
        print()
        print()
        print()

    def extract_sql_list(self, file_path):
        """
        从 sql 文件中过滤掉注释和空行，抽取出所有的 sql 语句。
        """

        with open(file_path, "r", encoding='utf-8') as f:
            lines = f.readlines()

        all_sql = ""
        # 标记当前行是否处于多行注释中
        in_comment = False
        for line in lines:
            line = line.strip()

            # 跳过空行
            if len(line) == 0:
                continue

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

            all_sql += " " + line

        return all_sql.split(";")
