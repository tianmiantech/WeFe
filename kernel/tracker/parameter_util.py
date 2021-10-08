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

#

import builtins
import copy
import importlib
import json
import os

from common.python.common.consts import MemberRole


class ParameterUtil(object):
    @staticmethod
    def override_parameter(submit_dict=None, module=None,
                           module_alias=None, role=None, member_id=None,
                           component_full_path=None, component_rel_path=None):

        _module_setting_path = os.path.join(component_full_path, "setting.json")
        _module_setting = None
        with open(_module_setting_path, "r") as fin:
            _module_setting = json.loads(fin.read())

        if not _module_setting:
            raise Exception("{} is not set in setting_conf ".format(module))

        # module_path = os.path.join('kernel', 'components', module.lower())
        module_path = component_rel_path
        param_class_path = _module_setting["param_class"]
        param_class = param_class_path.split("/", -1)[-1]

        param_module_path = ".".join(
            os.path.relpath(os.path.join(module_path, param_class_path)).replace('\\', '/').split("/", -1)[
            :-1]).replace(".py", "")
        param_module = importlib.import_module(param_module_path)
        param_obj = getattr(param_module, param_class)()
        default_runtime_dict = ParameterUtil.change_object_to_dict(param_obj)

        # default_runtime_conf_suf = _module_setting["default_runtime_conf"]
        default_runtime_conf_name = 'default_runtime.json'
        try:
            with open(os.path.join(component_full_path, default_runtime_conf_name), "r") as fin:
                default_runtime_dict = ParameterUtil.merge_parameters(default_runtime_dict, json.loads(fin.read()),
                                                                      param_obj)
        except:
            raise Exception("default runtime conf should be a json file")

        if not submit_dict:
            raise ValueError("submit conf does exist or format is wrong")

        _support_rols = _module_setting["role"].keys()
        _role_setting = None
        for _rolelist in _support_rols:
            if role in _rolelist.split("|"):
                _role_setting = _module_setting["role"].get(_rolelist)

        _code_path = os.path.join(module_path, _role_setting.get('program'))

        # merge
        runtime_dict = {param_class: copy.deepcopy(default_runtime_dict)}
        for key, value in submit_dict.items():
            if key not in [param_class]:
                runtime_dict[key] = value

        # if param_class in submit_dict:
        #     common_parameters = submit_dict[param_class]
        #     merge_dict = ParameterUtil.merge_parameters(runtime_dict[param_class], common_parameters, param_obj)
        #     runtime_dict[param_class] = merge_dict

        runtime_dict['local'] = submit_dict.get('local', {})
        my_local = {
            "role": role, "member_id": member_id
        }
        runtime_dict['local'].update(my_local)
        runtime_dict['CodePath'] = _code_path
        runtime_dict['module'] = module
        runtime_dict['role'] = ParameterUtil.generate_role_json(runtime_dict["task"]["members"])
        if 'mix_promoter_member_id' in runtime_dict["task"]:
            runtime_dict['mix_promoter_member_id'] = runtime_dict["task"]["mix_promoter_member_id"]

        runtime_dict[param_class] = ParameterUtil.merge_user_default_params(runtime_dict.get(param_class, {}),
                                                                            runtime_dict.get("params", {}))
        return runtime_dict

    @staticmethod
    def merge_parameters(runtime_dict, role_parameters, param_obj, idx=-1):
        param_variables = param_obj.__dict__
        for key, val_list in role_parameters.items():
            if key not in param_variables:
                continue

            attr = getattr(param_obj, key)
            if type(attr).__name__ in dir(builtins) or not attr:
                if idx != -1:
                    if len(val_list) <= idx:
                        continue
                    runtime_dict[key] = val_list[idx]
                else:
                    runtime_dict[key] = val_list
            else:
                if key not in runtime_dict:
                    runtime_dict[key] = {}
                runtime_dict[key] = ParameterUtil.merge_parameters(runtime_dict.get(key), val_list, attr, idx)

        return runtime_dict

    @staticmethod
    def get_args_input(submit_dict, module="args"):
        if "role_parameters" not in submit_dict:
            return {}

        roles = submit_dict["role_parameters"].keys()
        if not roles:
            return {}

        args_input = {}

        for role in roles:
            if not submit_dict["role_parameters"][role].get(module):
                continue

            args_parameters = submit_dict["role_parameters"][role].get(module)
            args_input[role] = []

            if "data" in args_parameters:
                dataset = args_parameters.get("data")
                for data_key in dataset:
                    datalist = dataset[data_key]
                    for i in range(len(datalist)):
                        value = datalist[i];
                        if len(args_input[role]) <= i:
                            args_input[role].append({module:
                                                         {"data":
                                                              {}
                                                          }
                                                     })

                        args_input[role][i][module]["data"][data_key] = value

        return args_input

    @staticmethod
    def change_object_to_dict(obj):
        ret_dict = {}

        variable_dict = obj.__dict__
        for variable in variable_dict:
            attr = getattr(obj, variable)
            if attr and type(attr).__name__ not in dir(builtins):
                ret_dict[variable] = ParameterUtil.change_object_to_dict(attr)
            else:
                ret_dict[variable] = attr

        return ret_dict

    @staticmethod
    def get_param_class_name(setting_conf_prefix, module):
        _module_setting_path = os.path.join(setting_conf_prefix, module + ".json")
        _module_setting = None
        with open(_module_setting_path, "r") as fin:
            _module_setting = json.loads(fin.read())

        param_class_path = _module_setting["param_class"]
        param_class = param_class_path.split("/", -1)[-1]

        return param_class

    @staticmethod
    def generate_role_json(members_json):
        roles_dict = {
            MemberRole.PROVIDER: [],
            MemberRole.PROMOTER: [],
            MemberRole.ARBITER: []
        }
        for member in members_json:
            if member["member_role"] == MemberRole.PROVIDER:
                roles_dict[MemberRole.PROVIDER].append(member["member_id"])
            elif member["member_role"] == MemberRole.PROMOTER:
                roles_dict[MemberRole.PROMOTER].append(member["member_id"])
            elif member["member_role"] == MemberRole.ARBITER:
                roles_dict[MemberRole.ARBITER].append(member["member_id"])
            else:
                raise ValueError("The Member Role is Wrong!")
        return roles_dict

    @staticmethod
    def merge_user_default_params(runtime_dict, params):
        for key, val_list in params.items():
            if type(val_list) is not dict:
                runtime_dict[key] = val_list
            else:
                if key not in runtime_dict:
                    runtime_dict[key] = {}
                runtime_dict[key] = ParameterUtil.merge_user_default_params(runtime_dict[key], val_list)
        return runtime_dict
