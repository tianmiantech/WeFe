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


from common.python.utils import log_utils
from kernel.components.binning.horzfeaturebinning import virtual_summary_binning
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.transfer.framework.horz.procedure.table_aggregator import TableScatterTransVar
from kernel.transfer.variables.transfer_class.mix_feature_binning_transfer_variable import MixBinningTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MixBinningArbiter(BaseVertFeatureBinning):

    def __init__(self):
        super(MixBinningArbiter, self).__init__()
        self.role = consts.ARBITER
        self.binning_obj = None
        self.transfer_variable = MixBinningTransferVariable()

    def _init_model(self, model_param):
        self.model_param = model_param
        self.binning_obj = virtual_summary_binning.Server(self.model_param,
                                                          table_transVar=TableScatterTransVar(
                                                              clients=(consts.PROMOTER,)), clients=(consts.PROMOTER,))

    def fit(self, *args):
        self.binning_obj.set_transfer_variable(self.transfer_variable)
        self.binning_obj.fit_split_points()

    def transform(self, data_instances):
        pass
