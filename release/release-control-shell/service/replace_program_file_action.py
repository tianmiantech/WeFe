import os.path

from service.service_action import BaseServiceAction


class ReplaceProgramFileAction(BaseServiceAction):
    """
    替换程序文件
    """

    def run(self):
        download_path = self.download(self.action_info.service + ".zip")

        target = os.path.join(
            self.workspace,
            self.action_info.service + ".jar"
        )
        self.replace_file(download_path, target)
        pass
