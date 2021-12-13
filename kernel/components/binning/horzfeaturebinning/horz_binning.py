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

from common.python.utils import log_utils
from kernel.components.binning.horzfeaturebinning import virtual_summary_binning, recursive_query_binning
from kernel.components.binning.horzfeaturebinning.param import HorzFeatureBinningParam
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.transfer.variables.transfer_class.horz_feature_binning_transfer_variable import HorzBinningTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class HorzBinningArbiter(BaseVertFeatureBinning):
    def __init__(self):
        super().__init__()
        self.binning_obj = None
        self.transfer_variable = HorzBinningTransferVariable()
        self.model_param = HorzFeatureBinningParam()

    def _init_model(self, model_param):
        self.model_param = model_param
        if self.model_param.method == consts.VIRTUAL_SUMMARY:
            self.binning_obj = virtual_summary_binning.Server(self.model_param)
        elif self.model_param.method == consts.RECURSIVE_QUERY:
            self.binning_obj = recursive_query_binning.Server(self.model_param)
        else:
            raise ValueError(f"Method: {self.model_param.method} cannot be recognized")

    def fit(self, *args):
        self.binning_obj.set_transfer_variable(self.transfer_variable)
        self.binning_obj.fit_split_points()

    def transform(self, data_instances):
        pass


class HorzBinningClient(BaseVertFeatureBinning):
    def __init__(self):
        super().__init__()
        self.binning_obj = None
        self.transfer_variable = HorzBinningTransferVariable()
        self.model_param = HorzFeatureBinningParam()

    def _init_model(self, model_param: HorzFeatureBinningParam):
        self.model_param = model_param
        if self.model_param.method == consts.VIRTUAL_SUMMARY:
            self.binning_obj = virtual_summary_binning.Client(self.model_param)
        elif self.model_param.method == consts.RECURSIVE_QUERY:
            self.binning_obj = recursive_query_binning.Client(role=self.component_properties.role,
                                                              params=self.model_param
                                                              )
        else:
            raise ValueError(f"Method: {self.model_param.method} cannot be recognized")

    def fit(self, data_instances):
        self._setup_bin_inner_param(data_instances, self.model_param)
        self.binning_obj.set_bin_inner_param(self.bin_inner_param)
        self.binning_obj.set_transfer_variable(self.transfer_variable)
        self.binning_obj.set_role_party(self.role, self.member_id)
        split_points = self.binning_obj.fit_split_points(data_instances)
        self.binning_obj.cal_local_iv(data_instances, is_horz=True)
        self.transform(data_instances)
        self._add_summary(split_points)
        return self.data_output
