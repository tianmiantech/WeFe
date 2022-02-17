# Copyright 2021 The WeFe Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import shutil
import urllib.request
import zipfile

from dto.action_info import ActionInfo


class BaseAction:
    # 行为相关配置
    action_info: ActionInfo

    # 升级程序在升级过程中使用的工作目录
    # 用于存放临时文件和日志
    workspace: str

    def __init__(self, action_info=None, workspace=None) -> None:
        super().__init__()
        self.action_info = action_info
        self.workspace = workspace

    def run(self):
        """
        请在子类中重写此方法
        """
        pass

    def download_and_unzip(self):
        """
        下载并解压文件
        """

        # 下载 zip 文件
        zip_file_path = self.download()

        # 解压文件
        zip_output_dir = os.path.dirname(zip_file_path)
        self.unzip(zip_file_path, zip_output_dir)

        zip_output_dir = os.path.join(
            zip_output_dir,
            "package"
        )

        # 检查解压出来的内容是否在 package 目录下
        if not os.path.exists(zip_output_dir):
            raise Exception("压缩包不满足要求，在发布压缩包时请将文件放在名为 package 的文件夹下并压缩 package 文件夹。")

        return zip_output_dir

    def download(self):
        """
        下载文件到指定路径

        @param filename
        下载后的文件名
        """
        print("******************************************************")
        print("开始下载更新需要的资源...")
        output_dir = os.path.join(
            self.workspace,
            "download"
        )

        print("资源保存目录：" + output_dir)

        if not os.path.exists(output_dir):
            os.makedirs(output_dir)
            print("创建目录：" + output_dir)

        full_path = os.path.join(
            output_dir,
            "package.zip"
        )
        print("资源保存路径：" + full_path)

        # 这里为了方便本地测试
        # 如果 url 是 http 开头，则下载，否则视为本地文件，执行拷贝。
        url = self.action_info.file_download_url
        print("资源下载地址：" + url)
        print("开始下载...")
        if url.startswith("http"):
            urllib.request.urlretrieve(url, full_path)
        else:
            shutil.copy(url, full_path)

        print("下载成功！")

        return full_path

    def unzip(self, zip_file_path, output_dir):
        """
        解压 zip 文件至指定目录
        """
        print("******************************************************")
        print("开始解压：" + zip_file_path)
        zip_file = zipfile.ZipFile(zip_file_path)
        zip_list = zip_file.namelist()

        for f in zip_list:
            zip_file.extract(f, output_dir)

        zip_file.close()
        print("已解压至：" + output_dir)

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
