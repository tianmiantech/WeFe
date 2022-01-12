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


from dto.service_action_info import ActionInfo
from service.base_action import BaseAction
from service.replace_program_file_action import ReplaceProgramFileAction
from service.update_mysql_action import UpdateMysqlAction
from util import object_util


class Main:

    @staticmethod
    def run(workspace, config_file_path, action_info_str):
        """
        wefe 程序升级行为的入口方法
        """
        action_info: ActionInfo = object_util.json_to_model(action_info_str, ActionInfo)

        # 根据参数实例化 action 对象
        action: BaseAction
        if action_info.service.index("mysql") >= 0:
            action = UpdateMysqlAction(action_info, workspace)
        else:
            action = ReplaceProgramFileAction(action_info, workspace)

        # 执行升级动作
        action.run()
