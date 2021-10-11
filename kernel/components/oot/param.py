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




from kernel.base.params.base_param import BaseParam
from kernel.utils import consts


class OotParam(BaseParam):
    """
    OOT Param

    mode: mode, vert: vertical, horz: horizontal, mix: mix
    feature_columns：Selected feature column, blank means to select all feature columns
    with_label：with label
    label_name：label name
    label_type：label type
    sub_component_name_list: sub component name list
    sub_component_task_config_dick:
        The task configuration parameter list of the sub-component,
        key: the name of the sub-component,
        value: the configuration of the sub-component (new fields will be added on the original basis)
    """

    def __init__(self, mode=consts.VERT, feature_columns='', with_label=False, label_name='y',
                 label_type='int', flow_node_id='', sub_component_name_list=[], sub_component_task_config_dick={}):
        self.mode = mode
        self.feature_columns = feature_columns
        self.with_label = with_label
        self.label_name = label_name
        self.label_type = label_type
        self.flow_node_id = flow_node_id
        self.sub_component_name_list = sub_component_name_list
        self.sub_component_task_config_dick = sub_component_task_config_dick

    def check(self):
        descr = "oot param's"
        self.check_valid_value(self.mode, descr, [consts.VERT, consts.HORZ])
        self.check_boolean(self.with_label, descr)
        self.check_string(self.label_name, descr)

        return True
