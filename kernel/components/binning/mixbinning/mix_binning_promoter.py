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

import copy
import functools

from common.python.utils import log_utils
from kernel.components.binning.horzfeaturebinning import virtual_summary_binning
from kernel.components.binning.horzfeaturebinning.param import HorzFeatureBinningParam
from kernel.components.binning.mixbinning.virtual_summary_binning_base import VirtualSummaryBinningBase
from kernel.components.binning.vertfeaturebinning.base_feature_binning import BaseVertFeatureBinning
from kernel.security import PaillierEncrypt
from kernel.security.paillier import PaillierEncryptedNumber
from kernel.transfer.variables.transfer_class.mix_feature_binning_transfer_variable import MixBinningTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class MixBinningPromoter(BaseVertFeatureBinning):
    def __init__(self):
        super(MixBinningPromoter, self).__init__()
        self.binning_obj = None
        self.role = consts.PROMOTER
        self.transfer_variable = MixBinningTransferVariable()
        self.model_param = HorzFeatureBinningParam()

    def _init_model(self, model_param: HorzFeatureBinningParam):
        self.model_param = model_param
        self.binning_obj = virtual_summary_binning.Client(self.model_param)

    def fit(self, data_instances):
        LOGGER.debug('promote binning start')
        self._abnormal_detection(data_instances)
        self._setup_bin_inner_param(data_instances, self.model_param)
        self.binning_obj.set_bin_inner_param(self.bin_inner_param)
        self.binning_obj.set_transfer_variable(self.transfer_variable)
        split_points = self.binning_obj.fit_split_points(data_instances)
        self.binning_obj.set_role_party(self.role, self.member_id)
        LOGGER.debug(f'split_points={split_points}')
        self.binning_obj.cal_local_iv(data_instances, is_horz=True)

        self._process_provider_binning_result(data_instances)

        self.transform(data_instances)
        self._add_summary(split_points)
        return self.data_output

    def _process_provider_binning_result(self, data_instances):
        cipher = PaillierEncrypt()
        cipher.generate_key()
        self._send_encrypt_label(data_instances, cipher)
        encrypted_bin_sum_infos = self.transfer_variable.encrypted_bin_sum.get(idx=-1)

        for provider_idx, encrypted_bin_sum in enumerate(encrypted_bin_sum_infos):
            LOGGER.debug(f'encrypted_bin_sum={encrypted_bin_sum}')
            result_counts = self.__decrypt_bin_sum(encrypted_bin_sum, cipher)
            LOGGER.debug(f'bin_sum={result_counts}')
            self.transfer_variable.bin_sum.remote(result_counts, member_id_list=[
                self.component_properties.provider_member_idlist[provider_idx]])

        provider_bin_bucket_info_list = self.transfer_variable.provider_bin_bucket_info.get(idx=-1)
        print(f'provider_bin_bucket_info_list={provider_bin_bucket_info_list}')
        for idx, provider_bin_bucket_info in enumerate(provider_bin_bucket_info_list):
            LOGGER.debug(f'provider_bin_bucket_info={provider_bin_bucket_info}')
            provider_member_id = self.component_properties.provider_member_idlist[idx]
            provider_model_params = copy.deepcopy(self.model_param)
            provider_model_params.bin_num = provider_bin_bucket_info["bin_num"]
            provider_model_params.method = provider_bin_bucket_info["bin_method"]
            provider_binning_obj = VirtualSummaryBinningBase(provider_model_params)
            provider_bin_bucket = provider_bin_bucket_info['bin_bucket']
            provider_binning_obj.cal_iv_woe(provider_bin_bucket, self.model_param.adjustment_factor)
            provider_binning_obj.set_role_party(role=consts.PROVIDER, member_id=provider_member_id)
            self.provider_results.append(provider_binning_obj)

    def _send_encrypt_label(self, data_instances, cipher):
        label_table = data_instances.mapValues(lambda x: x.label)

        f = functools.partial(self.encrypt, cipher=cipher)
        encrypted_label_table = label_table.mapValues(f, need_send=True)
        self.transfer_variable.encrypted_label.remote(encrypted_label_table, suffix=(self.member_id,),
                                                      role=consts.PROVIDER, idx=-1)

    @staticmethod
    def encrypt(x, cipher):
        return cipher.encrypt(x), cipher.encrypt(1 - x)

    @staticmethod
    def __decrypt_bin_sum(encrypted_bin_sum, cipher):
        decrypted_list = {}
        for col_name, count_list in encrypted_bin_sum.items():
            new_list = []
            for event_count, non_event_count in count_list:
                if isinstance(event_count, PaillierEncryptedNumber):
                    event_count = cipher.decrypt(event_count)
                if isinstance(non_event_count, PaillierEncryptedNumber):
                    non_event_count = cipher.decrypt(non_event_count)
                new_list.append((event_count, non_event_count))
            decrypted_list[col_name] = new_list
        return decrypted_list
