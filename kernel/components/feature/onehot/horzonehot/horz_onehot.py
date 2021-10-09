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

import functools
from collections import defaultdict

from common.python.utils import log_utils
from kernel.components.feature.onehot.horzonehot.param import HorzOneHotParam
from kernel.components.feature.onehot.onehot import OneHotEncoder, TransferPair
from kernel.transfer.variables.transfer_class.horz_onehot_transfer_variable import HorzOneHotTransferVariable
from kernel.utils import consts, abnormal_detection

LOGGER = log_utils.get_logger()


class HorzOneHotEncoder(OneHotEncoder):
    def __init__(self):
        super(HorzOneHotEncoder, self).__init__()
        self.model_name = 'HorzOneHotEncoder'
        self.model_param_name = 'HorzOneHotParam'
        self.model_meta_name = 'HorzOneHotMeta'
        self.model_param = HorzOneHotParam()

    def _init_model(self, params):
        super(HorzOneHotEncoder, self)._init_model(params)
        self.need_alignment = params.need_alignment
        self.transfer_variable = HorzOneHotTransferVariable()

    def _init_params(self, data_instances):
        if data_instances is None:
            return
        super(HorzOneHotEncoder, self)._init_params(data_instances)

    def combine_all_column_headers(self, promoter_columns, provider_columns):
        """ This is used when there is a need for aligment within the
        federated learning. The function would align the column headers from
        promoter and provider and send the new aligned headers back.

        Returns:
            Combine all the column headers from promoter and host
            if there is alignment is used
        """
        all_cols_dict = defaultdict(list)

        # Obtain all the promoter headers
        for promoter_cols in promoter_columns:
            for k, v in promoter_cols.items():
                all_cols_dict[k] = list(set(all_cols_dict[k] + v))

        # Obtain all the provider headers
        for provider_cols in provider_columns:
            for k, v in provider_cols.items():
                all_cols_dict[k] = list(set(all_cols_dict[k] + v))

        # Align all of them together
        combined_all_cols = {}
        for el in all_cols_dict.keys():
            combined_all_cols[el] = all_cols_dict[el]

        LOGGER.debug("{} combined cols: {}".format(self.role, combined_all_cols))

        return combined_all_cols

    def fit(self, data_instances):
        abnormal_detection.empty_table_detection(data_instances)

        self._init_params(data_instances)

        f1 = functools.partial(self.record_new_header,
                               inner_param=self.inner_param)

        self.col_maps = data_instances.applyPartitions(f1).reduce(self.merge_col_maps)
        LOGGER.debug("Before set_schema in fit, schema is : {}, header: {}".format(self.schema,
                                                                                   self.inner_param.header))

        col_maps = {}
        for col_name, pair_obj in self.col_maps.items():
            values = [x for x in pair_obj.values]
            col_maps[col_name] = values

        if self.need_alignment:
            aligned_col_maps = None
            if self.role == consts.PROVIDER:
                self.transfer_variable.provider_columns.remote(col_maps)
                aligned_columns = self.transfer_variable.aligned_columns.get(idx=-1)
                aligned_col_maps = aligned_columns[0]
            elif self.role == consts.PROMOTER:
                provider_columns = self.transfer_variable.provider_columns.get(idx=-1)
                combined_all_cols = self.combine_all_column_headers([col_maps], provider_columns)
                aligned_col_maps = combined_all_cols
                self.transfer_variable.aligned_columns.remote(combined_all_cols)

            self.col_maps = {}
            for col_name, value_list in aligned_col_maps.items():
                value_set = set([str(x) for x in value_list])
                if len(value_set) != len(value_list):
                    raise ValueError("Same values with different types have occurred among different parties")

                transfer_pair = TransferPair(col_name)
                for v in value_list:
                    transfer_pair.add_value(v)
                self.col_maps[col_name] = transfer_pair

        self._transform_schema()
        data_instances = self.transform(data_instances)
        LOGGER.debug("After transform in fit, schema is : {}, header: {}".format(self.schema,
                                                                                 self.inner_param.header))
        return data_instances
