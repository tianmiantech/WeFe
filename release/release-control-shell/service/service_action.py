import os
import shutil
import urllib.request
import zipfile

from dto.service_action_info import ServiceActionInfo


class BaseServiceAction:
    # 行为相关配置
    action_info: ServiceActionInfo

    # 升级程序在升级过程中使用的工作目录
    # 用于存放临时文件和日志
    workspace: str

    def __init__(self, action_info=None, workspace=None) -> None:
        super().__init__()
        self.action_info = action_info
        self.workspace = workspace

    def download_and_unzip(self):
        """
        下载并解压文件
        """

        # 下载 zip 文件
        zip_file_path = self.download()

        # 解压文件
        zip_output_dir = os.path.dirname(zip_file_path)
        self.unzip(zip_file_path, zip_output_dir)

        return os.path.join(
            zip_output_dir,
            "package"
        )

    def download(self):
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
            "package.zip"
        )

        url = self.action_info.file_download_address
        urllib.request.urlretrieve(url, full_path)
        return full_path

    def unzip(self, zip_file_path, output_dir):
        """
        解压 zip 文件至指定目录
        """
        zip_file = zipfile.ZipFile(zip_file_path)
        zip_list = zip_file.namelist()

        for f in zip_list:
            zip_file.extract(f, output_dir)

        zip_file.close()

    def replace_files(self, source_dir, target_dir):
        """
        拷贝 source 文件夹内的所有文件至 target 目录
        当有文件冲突时进行替换
        """

        # 如不存在目标目录则创建
        if not os.path.exists(target_dir):
            os.makedirs(target_dir)

        # 获取文件夹中文件和目录列表
        files = os.listdir(source_dir)
        for f in files:
            # 判断是否是文件夹
            if os.path.isdir(source_dir + '/' + f):
                # 递归调用本函数
                self.replace_files(source_dir + '/' + f, target_dir + '/' + f)
            else:
                # 替换文件
                shutil.copy(source_dir + '/' + f, target_dir + '/' + f)
