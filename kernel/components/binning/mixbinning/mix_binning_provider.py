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


import functools

from common.python.utils import log_utils
from kernel.components.binning.horzfeaturebinning.param import HorzFeatureBinningParam
from kernel.components.binning.mixbinning.virtual_summary_binning_client import VirtualSummaryBinningClient
from kernel.components.binning.mixbinning.virtual_summary_binning_server import VirtualSummaryBinningServer
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.transfer.variables.transfer_class.mix_feature_binning_transfer_variable import MixBinningTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MixBinningProvider(BaseVertFeatureBinning):
    def __init__(self):
        super(MixBinningProvider, self).__init__()
        self.transfer_variable = MixBinningTransferVariable()
        self.model_param = HorzFeatureBinningParam()
        self.binning_obj = None

    def _init_model(self, model_param: HorzFeatureBinningParam):
        self.model_param = model_param
        if self.provider_master:
            self.binning_obj = VirtualSummaryBinningServer(self.model_param)
        else:
            self.binning_obj = VirtualSummaryBinningClient(self.model_param)
        self.binning_obj.set_transfer_variable(self.transfer_variable)
        self.binning_obj.set_provider_param(self.provider_other_inner_id, self.provider_master_inner_id,
                                            self.provider_inner_id, self.member_id, self.mix_promoter_member_id)

    def fit(self, data_instances):
        self._setup_bin_inner_param(data_instances, self.model_param)
        self.binning_obj.set_bin_inner_param(self.bin_inner_param)
        self.binning_obj.set_transfer_variable(self.transfer_variable)
        split_points = self.binning_obj.fit_split_points(data_instances)
        self.binning_obj.set_role_party(self.role, self.member_id)
        LOGGER.debug(f'split_points={split_points}')
        encrypted_label = self._get_encrypted_label()
        LOGGER.debug(f'encrypted_label={encrypted_label.first()}')
        self._calc_bin_bucket(encrypted_label, split_points, data_instances)

        bin_bucket = None
        if self.provider_master:
            bin_sum_list = self.transfer_variable.bin_sum.get(idx=-1)
            for bin_sum in bin_sum_list:
                LOGGER.debug(f'bin_sum={bin_sum}')
                if bin_bucket is None:
                    bin_bucket = bin_sum
                else:
                    bin_bucket = self.binning_obj.aggregate_partition_label(bin_bucket, bin_sum)
                pass
            for provider_inner_id in self.provider_other_inner_id:
                suffix = (self.provider_inner_id, provider_inner_id)
                self.transfer_variable.merge_bin_sum.remote(bin_bucket, suffix=suffix,
                                                            member_id_list=[self.member_id])
        else:
            bin_bucket = self.transfer_variable.merge_bin_sum.get(
                suffix=(self.provider_master_inner_id, self.provider_inner_id),
                member_id_list=[self.member_id])[0]
        LOGGER.debug(f'bin_bucket={bin_bucket}')
        new_bin_bucket = {}
        for feature_name, bucket in bin_bucket.items():
            new_bin_bucket[self.binning_obj.bin_inner_param.decode_col_name(feature_name)] = bucket
        LOGGER.debug(f'new_bin_bucket={new_bin_bucket}')
        self.binning_obj.cal_iv_woe(new_bin_bucket, self.model_param.adjustment_factor)

        bin_bucket_info = {'bin_method': consts.QUANTILE, 'bin_num': self.model_param.bin_num, 'bin_bucket': bin_bucket,
                           'encoded_split_points_result': {}}
        LOGGER.debug(f'bin_bucket_info={bin_bucket_info}')
        self.transfer_variable.provider_bin_bucket_info.remote(bin_bucket_info,
                                                               member_id_list=[self.mix_promoter_member_id])
        self.transform(data_instances)
        self._add_summary(split_points)
        return self.data_output

    def _get_encrypted_label(self):
        return self.transfer_variable.encrypted_label.get(suffix=(self.mix_promoter_member_id,),
                                                          member_id_list=[self.mix_promoter_member_id])[0]

    def _calc_bin_bucket(self, encrypted_label, split_points, data_instances):
        encrypted_bin_sum = self._sync_init_bucket(encrypted_label, data_instances, split_points)
        LOGGER.debug(f'encrypted_bin_sum={encrypted_bin_sum}')
        self.transfer_variable.encrypted_bin_sum.remote(encrypted_bin_sum, member_id_list=[self.mix_promoter_member_id])

    def _sync_init_bucket(self, encrypted_label_table, data_instances, split_points):
        data_bin_table = self.binning_obj.get_data_bin(data_instances, split_points)

        encrypted_bin_sum = self.__static_encrypted_bin_label(data_bin_table, encrypted_label_table,
                                                              self.bin_inner_param.bin_cols_map, split_points)

        encrypted_bin_sum = self.bin_inner_param.encode_col_name_dict(encrypted_bin_sum)
        return encrypted_bin_sum

    def __static_encrypted_bin_label(self, data_bin_table, encrypted_label, cols_dict, split_points):
        data_bin_with_label = data_bin_table.join(encrypted_label, lambda x, y: (x, y))
        f = functools.partial(self.binning_obj.add_label_in_partition,
                              split_points=split_points,
                              cols_dict=cols_dict)
        result_sum = data_bin_with_label.mapPartitions(f)
        encrypted_bin_sum = result_sum.reduce(self.binning_obj.aggregate_partition_label)
        return encrypted_bin_sum
