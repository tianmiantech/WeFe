# Copyright 2021 Tianmian Tech. All Rights Reserved.
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

import uuid

from common.python.db.db_models import DB, GlobalConfigModel
from common.python.dto.global_config import MailServerModel, GatewayConfigModel, MemberInfo, BoardConfigModel


class GlobalConfigDao:
    """
    GlobalConfig database access object
    """

    @staticmethod
    def append_gateway_white_ip(ip, comment=None):
        """
        append ip to gateway ip whitelist

        Args:
            ip: An ip address want append to whitelist
        """
        if ip == "127.0.0.1":
            return

        gateway_config = GlobalConfigDao.getGatewayConfig()
        if gateway_config is None:
            return

        # Parse out the current ip whitelist list
        before_ip_list = []
        if gateway_config.ip_white_list:
            for line in gateway_config.ip_white_list.split('\n'):
                line = line.strip()
                # Determine whether it is a comment line
                if len(line) == 0 or line.startswith('#'):
                    continue

                # If there is a comment at the end of the line, cut out ip.
                annotation_symbol_index = line.find('#')
                if annotation_symbol_index > -1:
                    line = line[0:annotation_symbol_index].strip()

                before_ip_list.append(line)

        # If ip is already in the whitelist, jump out.
        if ip in before_ip_list:
            return

        if comment:
            ip = "\n# {}\n{}\n".format(comment, ip)

        item = GlobalConfigDao.get("wefe_gateway", "ip_white_list")
        if item is None:
            item = GlobalConfigModel()
            item.id = str(uuid.uuid1())
            item.group = "wefe_gateway"
            item.name = "ip_white_list"

        item.value += ip

        with DB.connection_context():
            item.save()

    @staticmethod
    def getBoardConfig() -> BoardConfigModel:
        """
        Get BoardConfig from database.

        Returns:
            BoardConfig of GlobalConfig
        """
        return GlobalConfigDao.getModel("wefe_board", BoardConfigModel)

    @staticmethod
    def getMemberInfo() -> MemberInfo:
        """
        Get MemberInfo from database.

        Returns:
            MemberInfo of GlobalConfig
        """
        return GlobalConfigDao.getModel("member_info", MemberInfo)

    @staticmethod
    def getGatewayConfig() -> GatewayConfigModel:
        """
        Get GatewayConfigModel from database.

        Returns:
            GatewayConfigModel of GlobalConfig
        """
        return GlobalConfigDao.getModel("wefe_gateway", GatewayConfigModel)

    @staticmethod
    def getMailServerConfig() -> MailServerModel:
        """
        Get MailServerModel from database.

        Returns:
            MailServerModel of GlobalConfig
        """
        return GlobalConfigDao.getModel("mail_server", MailServerModel)

    @staticmethod
    def getModel(group, clazz):
        """
        Query configs and convert to the model

        Args:
            group: group of config item
            clazz: the model class

        Return:
            the model instance
        """
        dic = GlobalConfigDao.list(group)
        model = clazz()
        model.__dict__.update(dic)
        return model

    @staticmethod
    def get(group, name) -> GlobalConfigModel:
        """
        Get one config item from database.

        Args:
            group: group of config item
            name: name of config item

        Return:
            A config item
        """
        with DB.connection_context():
            return GlobalConfigModel.get_or_none(
                GlobalConfigModel.group == group,
                GlobalConfigModel.name == name
            )

    @staticmethod
    def list(group) -> dict:
        """
        List configs by group.

        Args:
            group: group of config item

        Returns:
            Dict of multi config items.
        """
        with DB.connection_context():
            items = GlobalConfigModel.select().where(GlobalConfigModel.group == group)
            result = {}
            for item in items:
                result[item.name] = item.value

            return result
