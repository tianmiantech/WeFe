from service.service_action import BaseServiceAction


class ReplaceProgramFileAction(BaseServiceAction):
    """
    替换程序文件
    """

    def run(self):
        # 下载并解压文件
        zip_output_dir = self.download_and_unzip()

        # 将 zip_output_dir 目录中的所有文件替换到程序目录
        self.replace_files(zip_output_dir, self.action_info.program_dir)
        pass
