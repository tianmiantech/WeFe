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

# Copyright 2019 The FATE Authors. All Rights Reserved.
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

import copy

from common.python.utils import log_utils

LOGGER = log_utils.get_logger()


class Component(object):
    __instance = {}

    def __init__(self, *args, **kwargs):
        LOGGER.debug(f"kwargs: {kwargs}")
        if "name" in kwargs:
            self._component_name = kwargs["name"]
        self.__member_instance = {}
        self._component_parameter_keywords = set(kwargs.keys())
        self._role_parameter_keywords = set()
        self._module_name = None
        self._param_name = None
        self._component_param = {}
        self._data_type = None

    def __new__(cls, *args, **kwargs):
        if cls.__name__.lower() not in cls.__instance:
            cls.__instance[cls.__name__.lower()] = 0

        new_cls = object.__new__(cls)
        new_cls.set_name(cls.__instance[cls.__name__.lower()])
        cls.__instance[cls.__name__.lower()] += 1

        return new_cls

    def set_name(self, idx):
        self._component_name = self.__class__.__name__.lower() + "_" + str(idx)
        LOGGER.debug(f"enter set name func {self._component_name}")

    def reset_name(self, name):
        self._component_name = name

    def get_member_instance(self, role="promoter", member_id=None) -> 'Component':
        if role not in ["promoter", "provider", "arbiter"]:
            raise ValueError("Role should be one of promoter/provider/arbiter")

        if member_id is not None:
            if isinstance(member_id, list):
                for _id in member_id:
                    if not isinstance(_id, int) or _id <= 0:
                        raise ValueError("member id should be positive integer")
            elif not isinstance(member_id, int) or member_id <= 0:
                raise ValueError("member id should be positive integer")

        if role not in self.__member_instance:
            self.__member_instance[role] = {}
            self.__member_instance[role]["member"] = {}

        member_key = member_id

        if isinstance(member_id, list):
            member_key = "|".join(map(str, member_id))

        if member_key not in self.__member_instance[role]["member"]:
            self.__member_instance[role]["member"][member_key] = None

        if not self.__member_instance[role]["member"][member_key]:
            member_instance = copy.deepcopy(self)
            self._decrease_instance_count()

            self.__member_instance[role]["member"][member_key] = member_instance
            LOGGER.debug(f"enter init")

        return self.__member_instance[role]["member"][member_key]

    @classmethod
    def _decrease_instance_count(cls):
        cls.__instance[cls.__name__.lower()] -= 1
        LOGGER.debug(f"decrease instance count")

    @property
    def name(self):
        return self._component_name

    @property
    def module(self):
        return self._module_name

    @property
    def param_name(self):
        return self._param_name

    def component_param(self, **kwargs):
        new_kwargs = copy.deepcopy(kwargs)
        for attr in self.__dict__:
            if attr in new_kwargs:
                setattr(self, attr, new_kwargs[attr])
                self._component_param[attr] = new_kwargs[attr]
                del new_kwargs[attr]

        for attr in new_kwargs:
            LOGGER.warning(f"key {attr}, value {new_kwargs[attr]} not use")

        self._role_parameter_keywords |= set(kwargs.keys())

    def get_component_param(self):
        return self._component_param

    def get_common_param_conf(self):
        """
        exclude_attr = ["_component_name", "__member_instance",
                        "_component_parameter_keywords", "_role_parameter_keywords"]
        """

        common_param_conf = {}
        for attr in self.__dict__:
            if attr.startswith("_"):
                continue

            if attr in self._role_parameter_keywords:
                continue

            if attr not in self._component_parameter_keywords:
                continue

            common_param_conf[attr] = getattr(self, attr)

        return common_param_conf

    def get_role_param_conf(self, roles=None):
        role_param_conf = {}

        if not self.__member_instance:
            return role_param_conf

        for role in self.__member_instance:
            role_param_conf[role] = {}
            if None in self.__member_instance[role]["member"]:
                role_all_member_conf = self.__member_instance[role]["member"][None].get_component_param()
                if "all" not in role_param_conf:
                    role_param_conf[role]["all"] = {}
                    role_param_conf[role]["all"][self._component_name] = role_all_member_conf

            valid_memberids = roles.get(role)
            for member_id in self.__member_instance[role]["member"]:
                if not member_id:
                    continue

                # if isinstance(member_id, int):
                #     member_key = str(valid_memberids.index(member_id))
                # else:
                #     member_list = list(map(int, member_id.split("|", -1)))
                #     member_key = "|".join(map(str, [valid_memberids.index(member) for member in member_list]))

                member_inst = self.__member_instance[role]["member"][member_id]

                # if member_key not in role_param_conf:
                #     role_param_conf[role][member_key] = {}

                role_param_conf[role][self._component_name] = member_inst.get_component_param()

        # print ("role_param_conf {}".format(role_param_conf))
        LOGGER.debug(f"role_param_conf {role_param_conf}")
        return role_param_conf

    @classmethod
    def erase_component_base_param(cls, **kwargs):
        new_kwargs = copy.deepcopy(kwargs)
        if "name" in new_kwargs:
            del new_kwargs["name"]

        return new_kwargs

    def get_config(self, **kwargs):
        """need to implement"""

        roles = kwargs["roles"]

        common_param_conf = self.get_common_param_conf()
        role_param_conf = self.get_role_param_conf(roles)

        conf = {}
        if common_param_conf:
            conf['common'] = {self._component_name: common_param_conf}

        if role_param_conf:
            conf["role"] = role_param_conf

        return conf

    def _get_all_member_instance(self):
        return self.__member_instance


class PlaceHolder(object):
    pass
