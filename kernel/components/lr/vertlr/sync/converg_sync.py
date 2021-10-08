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

from common.python.utils import log_utils
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class Provider(object):

    def register_convergence(self, transfer_variables):
        self._is_stopped_transfer = transfer_variables.converge_flag

    def get_converge_info(self, suffix=tuple(), member_id_list=None):
        is_converged = self._is_stopped_transfer.get(idx=0, suffix=suffix, member_id_list=member_id_list)
        return is_converged


class Promoter(object):

    def register_convergence(self, transfer_variables):
        self._is_stopped_transfer = transfer_variables.converge_flag

    def sync_converge_info(self, is_converged, suffix=tuple()):
        self._is_stopped_transfer.remote(obj=is_converged, role=consts.PROVIDER, idx=-1, suffix=suffix)
