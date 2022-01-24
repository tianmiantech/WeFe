from action.base_action import BaseAction


class UpdateMysqlAction(BaseAction):

    def run(self):
        # 下载并解压文件
        zip_output_dir = self.download_and_unzip()

        # 将 zip_output_dir 目录中的所有 sql 文件按文件名排序后逐个执行
        path = "/Users/zane/Code/WeLab/Wefe/documents/数据库设计/board/wefe_board.sql";
        text = self.read_text(path)

    def read_text(self, path):
        with open(path, "r") as f:
            return f.read()
