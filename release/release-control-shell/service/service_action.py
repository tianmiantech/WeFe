import os
import shutil
import urllib.request

from dto.service_action_info import ServiceActionInfo


class BaseServiceAction:
    # 行为相关配置
    action_info: ServiceActionInfo

    # 升级程序在升级过程中使用的工作目录
    # 用于存放临时文件和日志
    workspace: str

    def __init__(self, action_info, workspace) -> None:
        super().__init__()
        self.action_info = action_info
        self.workspace = workspace

    def download(self, filename):
        """
        下载文件到指定路径

        @param filename
        下载后的文件名
        """

        output_dir = os.path.join(
            self.workspace,
            "download"
        )

        if not os.path.exists(output_dir):
            os.makedirs(output_dir)

        full_path = os.path.join(
            output_dir,
            filename
        )

        url = self.action_info.file_download_address
        urllib.request.urlretrieve(url, full_path)
        return full_path

    def replace_file(self, source, target):
        """
        替换文件
        """
        if os.path.exists(target):
            os.remove(target)

        shutil.copyfile(source, target)
