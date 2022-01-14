from service.base_action import BaseAction


class UpdateMysqlAction(BaseAction):

    def run(self):
        # 下载并解压文件
        zip_output_dir = self.download_and_unzip()

        # 将 zip_output_dir 目录中的所有 sql 文件按文件名排序后逐个执行
