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

from flow.web.api.base.base_api import BaseApi
from flow.web.api.base.dto.base_api_input import BaseApiInput
from flow.web.api.base.dto.base_api_output import BaseApiOutput
from flow.web.service.data_view_service import DataViewService


class Input(BaseApiInput):
    table_namespace: str
    table_name: str

    def check(self):
        super().required([self.table_namespace, self.table_name])


class Api(BaseApi):

    def run(self, input: Input):
        return BaseApiOutput.success(data=DataViewService.instance_to_json(input.table_namespace, input.table_name))
