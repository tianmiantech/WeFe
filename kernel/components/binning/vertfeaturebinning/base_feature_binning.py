#!/usr/bin/env python
# -*- coding: utf-8 -*-

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

import json
import re

from common.python.common.consts import DataSetSourceType
from common.python.utils import log_utils
from kernel.components.binning.core.base_binning import Binning
from kernel.components.binning.core.bin_inner_param import BinInnerParam
from kernel.components.binning.core.bucket_binning import BucketBinning
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.vertfeaturebinning.param import FeatureBinningParam
from kernel.model_base import ModelBase
from kernel.protobuf.generated import feature_binning_meta_pb2, feature_binning_param_pb2
from kernel.transfer.variables.transfer_class.vert_feature_binning_transfer_variable import \
    VertFeatureBinningTransferVariable
from kernel.utils import consts
from kernel.utils import data_util
from kernel.utils.data_util import get_header
from kernel.utils.io_check import assert_io_num_rows_equal

LOGGER = log_utils.get_logger()

MODEL_PARAM_NAME = 'FeatureBinningParam'
MODEL_META_NAME = 'FeatureBinningMeta'


class BaseVertFeatureBinning(ModelBase):
    """
    Do binning method through promoter and provider

    """

    def __init__(self):
        super(BaseVertFeatureBinning, self).__init__()
        self.transfer_variable = VertFeatureBinningTransferVariable()
        self.binning_obj: Binning = None
        self.binning_obj_list = []
        self.header = None
        self.schema = None
        self.provider_results = []
        self.provider_results_list = []
        self.transform_type = None
        self.model_save_to_storage = True
        self.save_dataset = True
        self.model_param = FeatureBinningParam()
        self.bin_inner_param = BinInnerParam()

        self.set_show_name("(Binning)")
        self.source_type = DataSetSourceType.BINNING

    def _init_model(self, params: FeatureBinningParam):
        self.model_param = params
        #
        # self.transform_type = self.model_param.transform_param.transform_type
        #
        # if self.model_param.method == consts.QUANTILE:
        #     self.binning_obj = QuantileBinning(self.model_param)
        # elif self.model_param.method == consts.BUCKET:
        #     self.binning_obj = BucketBinning(self.model_param)
        # elif self.model_param.method == consts.OPTIMAL:
        #     if self.role == consts.PROVIDER:
        #         self.model_param.bin_num = self.model_param.optimal_binning_param.init_bin_nums
        #         self.binning_obj = QuantileBinning(self.model_param)
        #     else:
        #         self.binning_obj = OptimalBinning(self.model_param)
        # else:
        #     # self.binning_obj = QuantileBinning(self.bin_param)
        #     raise ValueError("Binning method: {} is not supported yet".format(self.model_param.method))
        # LOGGER.debug("in _init_model, role: {}, local_member_id: {}".format(self.role, self.component_properties))
        # self.binning_obj.set_role_party(self.role, self.component_properties.local_member_id)

    def _setup_bin_inner_param(self, data_instances, params: FeatureBinningParam):
        # if self.schema is not None:
        #     return

        self.bin_inner_param = BinInnerParam()
        self.header = get_header(data_instances)
        LOGGER.debug("_setup_bin_inner_param, get header: {}".format(self.header))

        self.schema = data_instances.schema
        self.bin_inner_param.set_header(self.header)
        if params.bin_indexes == -1:
            self.bin_inner_param.set_bin_all()
        else:
            self.bin_inner_param.add_bin_indexes(params.bin_indexes)
            self.bin_inner_param.add_bin_names(params.bin_names)

        self.bin_inner_param.add_category_indexes(params.category_indexes)
        self.bin_inner_param.add_category_names(params.category_names)

        if params.transform_param.transform_cols == -1:
            self.bin_inner_param.set_transform_all()
        else:
            self.bin_inner_param.add_transform_bin_indexes(params.transform_param.transform_cols)
            self.bin_inner_param.add_transform_bin_names(params.transform_param.transform_names)
        # LOGGER.debug("After _setup_bin_inner_param: {}".format(self.bin_inner_param.__dict__))
        self.binning_obj.set_bin_inner_param(self.bin_inner_param)
        LOGGER.debug("After _setup_bin_inner_param, header: {}".format(self.header))

    @assert_io_num_rows_equal
    def transform(self, data_instances):
        self._setup_bin_inner_param(data_instances, self.model_param)
        data_instances = self.binning_obj.transform(data_instances, self.transform_type)
        self.set_schema(data_instances)
        self.data_output = self.binning_obj.convert_feature_to_woe(data_instances)

        return data_instances

    def _get_meta(self):
        # col_list = [str(x) for x in self.cols]

        transform_param = feature_binning_meta_pb2.TransformMeta(
            transform_cols=self.bin_inner_param.transform_bin_indexes,
            transform_type=self.model_param.transform_param.transform_type
        )

        meta_protobuf_obj = feature_binning_meta_pb2.FeatureBinningMeta(
            method=self.model_param.method,
            compress_thres=self.model_param.compress_thres,
            head_size=self.model_param.head_size,
            error=self.model_param.error,
            bin_num=int(self.model_param.bin_num),
            cols=self.bin_inner_param.bin_names,
            adjustment_factor=self.model_param.adjustment_factor,
            local_only=self.model_param.local_only,
            need_run=self.need_run,
            transform_param=transform_param
        )
        return meta_protobuf_obj

    def _get_param(self):
        binning_result_obj = self.binning_obj.bin_results.generated_pb()
        # binning_result_obj = self.bin_results.generated_pb()
        provider_results = [x.bin_results.generated_pb() for x in self.provider_results if
                            x.bin_results.all_cols_results]

        result_obj = feature_binning_param_pb2.FeatureBinningParam(binning_result=binning_result_obj,
                                                                   provider_results=provider_results,
                                                                   header=self.header)
        LOGGER.debug("json_result: {}".format(result_obj))
        return result_obj

    def load_model(self, model_dict):
        model_0 = list(model_dict.get('model'))[0]
        cols = model_0.get('Model_Meta').get('cols')
        Model_Param = model_0.get('Model_Param')
        binningResult = Model_Param.get('binningResult').get('binningResult')
        header = Model_Param.get('header')
        model_dict_str = json.dumps(model_dict)
        model_dict_str = self.hump2underline(model_dict_str)
        model_dict = json.loads(model_dict_str)
        LOGGER.debug("model_dict ===> {}".format(model_dict))

        for name, value in binningResult.items():
            binningResult[name] = json.loads(self.hump2underline(json.dumps(value)))

        model_meta = list(model_dict.get('model'))[0].get("model_meta")
        model_meta['cols'] = cols
        model_param = list(model_dict.get('model'))[0].get("model_param")
        binning_result = model_param.get('binning_result')
        binning_result['binning_result'] = binningResult
        model_param['binning_result'] = binning_result
        model_param['header'] = header
        LOGGER.debug(f"model_meta={model_meta}, model_param={model_param}")

        self.bin_inner_param = BinInnerParam()

        # assert isinstance(model_meta, feature_binning_meta_pb2.FeatureBinningMeta)
        # assert isinstance(model_param, feature_binning_param_pb2.FeatureBinningParam)

        self.header = list(model_param["header"])
        self.bin_inner_param.set_header(self.header)

        self.bin_inner_param.add_transform_bin_indexes(list(model_meta["transform_param"]["transform_cols"]))
        self.bin_inner_param.add_bin_names(list(model_meta["cols"]))
        self.transform_type = model_meta["transform_param"]["transform_type"]

        bin_method = str(model_meta["method"])
        if bin_method == consts.QUANTILE:
            self.binning_obj = QuantileBinning(params=model_meta)
        else:
            self.binning_obj = BucketBinning(params=model_meta)

        self.binning_obj.set_role_party(self.role, self.component_properties.local_member_id)
        self.binning_obj.set_bin_inner_param(self.bin_inner_param)
        self.binning_obj.bin_results.reconstruct2(model_param["binning_result"])

        self.provider_results = []
        LOGGER.debug(f"provider_results={model_param['provider_results']}")
        for host_pb in model_param["provider_results"]:
            LOGGER.debug("host_pb ===> {}".format(host_pb))
            binning_result = host_pb["binning_result"]
            if not binning_result:
                continue
            host_bin_obj = Binning()
            host_bin_obj.bin_results.reconstruct2(host_pb)
            self.provider_results.append(host_bin_obj)

    def export_model(self):
        if self.model_output is not None:
            return self.model_output

        meta_obj = self._get_meta()
        param_obj = self._get_param()
        result = {
            MODEL_META_NAME: meta_obj,
            MODEL_PARAM_NAME: param_obj
        }
        self.model_output = result
        # self.model_save_to_storage = True
        return result

    def output_data(self):
        return self.data_output

    def set_schema(self, data_instance):
        self.schema['header'] = self.header
        data_instance.schema = self.schema
        LOGGER.debug("After Binning, data_instance is : {}".format(data_instance))
        LOGGER.debug("After Binning, when setting schema, schema is : {}".format(data_instance.schema))

    def _abnormal_detection(self, data_instances):
        """
        Make sure input data_instances is valid.
        """
        data_util.empty_table_detection(data_instances)
        data_util.empty_feature_detection(data_instances)

    def get_indexes(self, bin_feature_names: list, data_instances):
        bin_indexes = []
        if len(bin_feature_names) == 0:
            return bin_indexes
        data_feature_names = data_instances.schema["header"]
        for bin_feature_name in bin_feature_names:
            index = data_feature_names.index(bin_feature_name)
            bin_indexes.append(index)
        return bin_indexes

    def hump2underline(self, hump_str):
        """
        Camel case string convert underscore form
        :param hump_str: Camel case string
        :return: underscore form
        """
        p = re.compile(r'([a-z]|\d)([A-Z])')
        sub = re.sub(p, r'\1_\2', hump_str).lower()
        return sub

    def _add_summary(self, split_points):
        summary = {}
        for k, v in split_points.items():
            summary[k] = list(v)
        self.set_summary({"split_points": summary})
        LOGGER.info(f'summary={summary}')
