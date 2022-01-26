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
import zipfile

from action.base_action import BaseAction


class ReplaceProgramFileAction(BaseAction):
    """
    替换程序文件
    """

    def run(self):
        # 备份数据库
        backup_output_path = os.path.join(
            self.workspace,
            self.action_info.service + ".backup.zip"
        )
        print("开始备份应用程序至：" + backup_output_path)
        self.make_zip(self.action_info.program_dir, backup_output_path)
        print("应用程序备份成功：" + backup_output_path)
        print()

        # 下载并解压文件
        zip_output_dir = self.download_and_unzip()

        # 将 zip_output_dir 目录中的所有文件替换到程序目录
        self.replace_files(zip_output_dir, self.action_info.program_dir)

    def make_zip(self, source_dir, output_filename):
        zipf = zipfile.ZipFile(output_filename, 'w')
        pre_len = len(os.path.dirname(source_dir))
        for parent, dirnames, filenames in os.walk(source_dir):
            for filename in filenames:
                pathfile = os.path.join(parent, filename)
                # 相对路径
                arcname = pathfile[pre_len:].strip(os.path.sep)
                zipf.write(pathfile, arcname)
        zipf.close()
