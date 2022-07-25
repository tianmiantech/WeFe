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

from visualfl.db.db_models import DB, MemberInfo, GlobalConfigModel


class GlobalConfigDao:
    """
    GlobalConfig database access object
    """

    @staticmethod
    def getMemberInfo() -> MemberInfo:
        """
        Get MemberInfo from database.

        Returns:
            MemberInfo of GlobalConfig
        """
        return GlobalConfigDao.getModel("member_info", MemberInfo)

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
    def get_rsa_private_key():
        return GlobalConfigDao.getMemberInfo().rsa_private_key

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