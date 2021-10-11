import os
import shutil
import urllib.request

from dto.service_action_info import ServiceActionInfo


class BaseServiceAction:
    config: ServiceActionInfo
    wefe_dir: str

    def __init__(self, config, wefe_dir) -> None:
        super().__init__()
        self.config = config
        self.wefe_dir = wefe_dir

    def download(self, filename):
        """
        下载文件到指定路径
        """
        download_path = os.path.join(
            self.wefe_dir,
            "download",
            filename
        )

        url = self.config.file_download_address
        urllib.request.urlretrieve(url, download_path)
        return download_path

    def replace_file(self, source, target):
        """
        替换文件
        """
        if os.path.exists(target):
            os.remove(target)

        shutil.copyfile(source, target)
