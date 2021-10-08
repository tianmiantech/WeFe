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



import functools
import json

from google.protobuf import json_format

from common.python.common.consts import ModelType
from common.python.utils import log_utils
from kernel.components.binning.vertfeaturebinning.vert_binning_promoter import VertFeatureBinningPromoter
from kernel.components.binning.vertfeaturebinning.vert_binning_provider import VertFeatureBinningProvider
from kernel.components.featurecalculation.base import filter_factory
from kernel.components.featurecalculation.base.calculation_properties import CalculationProperties, \
    CompletedCalculationResults
from kernel.components.featurecalculation.param import FeatureCalculationParam
from kernel.model_base import ModelBase
from kernel.protobuf.generated import feature_calculation_param_pb2, feature_calculation_meta_pb2
from kernel.transfer.variables.transfer_class.vert_feature_calculation_transfer_variable import \
    VertFeatureCalculationTransferVariable
from kernel.utils import abnormal_detection
from kernel.utils import consts
from kernel.utils.data_util import get_header
from kernel.utils.io_check import assert_io_num_rows_equal

LOGGER = log_utils.get_logger()

MODEL_PARAM_NAME = 'FeatureCalculationParam'
MODEL_META_NAME = 'FeatureCalculationMeta'
MODEL_NAME = 'VertFeatureCalculation'


class BaseVertFeatureCalculation(ModelBase):
    def __init__(self):
        super(BaseVertFeatureCalculation, self).__init__()
        self.transfer_variable = VertFeatureCalculationTransferVariable()

        self.curt_calculate_properties = CalculationProperties()
        self.completed_calculation_result = CompletedCalculationResults()

        self.schema = None
        self.header = None
        self.party_name = 'Base'
        # Possible previous model
        self.binning_model = None
        self.static_obj = None
        self.model_param = FeatureCalculationParam()
        self.meta_dicts = {}

    def _init_model(self, params):
        self.model_param = params
        # self.cols_index = params.calculate_cols
        self.filter_methods = params.filter_methods
        # self.local_only = params.local_only

    def _init_calculate_params(self, data_instances):
        if self.schema is None:
            self.schema = data_instances.schema

        if self.header is not None:
            return
        self.schema = data_instances.schema
        header = get_header(data_instances)
        self.header = header
        self.curt_calculate_properties.set_header(header)
        self.curt_calculate_properties.set_last_left_col_indexes([x for x in range(len(header))])
        if self.model_param.calculate_col_indexes == -1:
            self.curt_calculate_properties.set_calculate_all_cols()
        else:
            self.curt_calculate_properties.add_calculate_col_indexes(self.model_param.calculate_col_indexes)
        self.curt_calculate_properties.add_calculate_col_names(self.model_param.calculate_names)
        self.completed_calculation_result.set_header(header)
        self.completed_calculation_result.set_calculate_col_names(self.curt_calculate_properties.calculate_col_names)
        self.completed_calculation_result.set_all_left_col_indexes(self.curt_calculate_properties.all_left_col_indexes)

    def _get_meta(self):
        self.meta_dicts['filter_methods'] = self.filter_methods
        self.meta_dicts['cols'] = self.completed_calculation_result.get_calculate_col_names()
        self.meta_dicts['need_run'] = self.need_run
        meta_protobuf_obj = feature_calculation_meta_pb2.FeatureCalculationMeta(**self.meta_dicts)
        return meta_protobuf_obj

    def _get_param(self):
        LOGGER.debug("curt_calculate_properties.left_col_name: {}, completed_calculation_result: {}".format(
            self.curt_calculate_properties.left_col_names, self.completed_calculation_result.all_left_col_names
        ))
        LOGGER.debug("Length of left cols: {}".format(len(self.completed_calculation_result.all_left_col_names)))
        # left_cols = {x: True for x in self.curt_calculate_properties.left_col_names}
        left_cols = {x: True for x in self.completed_calculation_result.all_left_col_names}
        final_left_cols = feature_calculation_param_pb2.LeftCols(
            original_cols=self.completed_calculation_result.get_calculate_col_names(),
            left_cols=left_cols
        )

        result_obj = feature_calculation_param_pb2.FeatureCalculationParam(
            results=self.completed_calculation_result.filter_results,
            col_names=self.completed_calculation_result.get_sorted_col_names(),
        )

        result_obj_list = []
        result_obj_dic = {}
        result = json_format.MessageToJson(result_obj)
        result_obj_dic["role"] = self.role
        result_obj_dic["member_id"] = self.member_id
        result_obj_dic["results"] = json.loads(result)["results"]
        LOGGER.debug("json_result: {}".format(result_obj_dic))
        result_obj_list.append(result_obj_dic)
        if self.role == consts.PROVIDER:
            print(VertFeatureCalculationTransferVariable().provider_calculate_results.remote(result_obj_dic,
                                                                                             role=consts.PROMOTER,
                                                                                             idx=0))
        elif self.role == consts.PROMOTER:
            provider_result_obj_dics = VertFeatureCalculationTransferVariable().provider_calculate_results.get(idx=-1)
            for provider_result_obj in provider_result_obj_dics:
                result_obj_list.append(provider_result_obj)

        calculate_results_list = []
        for result_obj in result_obj_list:
            role = result_obj["role"]
            member_id = str(result_obj["member_id"])
            new_results = []
            results = result_obj["results"]

            for result in results:
                filter_name = result["filterName"]
                feature_values = result["featureValues"]
                feature_values = dict(sorted(feature_values.items(), key=lambda e: e[1], reverse=True))
                cols = []
                values = []
                for key in feature_values:
                    cols.append(key)
                    values.append(feature_values[key])
                new_result = feature_calculation_param_pb2.FeatureCalculationValueResultParam(
                    filter_name=filter_name,
                    cols=cols,
                    values=values,
                )

                new_results.append(new_result)

            new_result_obj = feature_calculation_param_pb2.FeatureCalculationResultParam(
                role=role,
                member_id=member_id,
                results=new_results
            )
            calculate_results_list.append(new_result_obj)

        results = feature_calculation_param_pb2.FeatureCalculationResultsParam(
            calculate_results=calculate_results_list
        )
        return results

    def save_data(self):
        return self.data_output

    def export_model(self):
        LOGGER.debug("Model output is : {}".format(self.model_output))
        if self.model_output is not None:
            LOGGER.debug("model output is already exist, return directly")
            return self.model_output

        meta_obj = self._get_meta()
        param_obj = self._get_param()
        result = {
            MODEL_META_NAME: meta_obj,
            MODEL_PARAM_NAME: param_obj
        }
        self.model_output = result
        return result

    def load_model(self, model_dict):

        if ModelType.TRAIN_MODEL in model_dict.get("model", {}):
            # self._parse_need_run(model_dict, MODEL_META_NAME)
            LOGGER.debug("Feature calculation need run: {}".format(self.need_run))
            if not self.need_run:
                return
            model_param = list(model_dict.get('model').values())[0].get(MODEL_PARAM_NAME)
            model_meta = list(model_dict.get('model').values())[0].get(MODEL_META_NAME)

            self.model_output = {
                MODEL_META_NAME: model_meta,
                MODEL_PARAM_NAME: model_param
            }

            header = list(model_param.header)
            # self.schema = {'header': header}
            self.header = header
            self.curt_calculate_properties.set_header(header)
            self.completed_calculation_result.set_header(header)
            self.curt_calculate_properties.set_last_left_col_indexes([x for x in range(len(header))])
            self.curt_calculate_properties.add_calculate_col_names(header)

            final_left_cols_names = dict(model_param.final_left_cols.left_cols)
            LOGGER.debug("final_left_cols_names: {}".format(final_left_cols_names))
            for col_name, _ in final_left_cols_names.items():
                self.curt_calculate_properties.add_left_col_name(col_name)
            self.completed_calculation_result.add_filter_results(filter_name='conclusion',
                                                                 calculate_properties=self.curt_calculate_properties)
            self.update_curt_calculate_param()
            LOGGER.debug("After load model, completed_calculation_result.all_left_col_indexes: {}".format(
                self.completed_calculation_result.all_left_col_indexes))

        if ModelType.BINNING_MODEL in model_dict.get("model", {}):

            LOGGER.debug("Has binning_model, model_dict: {}".format(model_dict))
            if self.role == consts.PROMOTER:
                self.binning_model = VertFeatureBinningPromoter()
            else:
                self.binning_model = VertFeatureBinningProvider()

            # binning = model_dict['model'][ModelType.BINNING_MODEL]
            # Model_Param = binning[0]['Model_Param']
            # newProviderResults = []
            # if 'providerResults' in Model_Param.keys():
            #     providerResults = Model_Param['providerResults']
            #     for providerResult in providerResults:
            #         binningResult = providerResult['binningResult']
            #         if binningResult:
            #             newProviderResults.append(providerResults)
            #     Model_Param['providerResults'] = newProviderResults
            # binning[0]['Model_Param'] = Model_Param
            new_model_dict = {'model': model_dict['model'][ModelType.BINNING_MODEL]}
            LOGGER.debug(f'model={new_model_dict}')
            self.binning_model.load_model(new_model_dict)

    @staticmethod
    def calculate_cols(instance, left_col_idx):
        instance.features = instance.features[left_col_idx]
        return instance

    def _transfer_data(self, data_instances):

        before_one_data = data_instances.first()
        f = functools.partial(self.calculate_cols,
                              left_col_idx=self.completed_calculation_result.all_left_col_indexes)

        new_data = data_instances.mapValues(f)

        LOGGER.debug("When transfering, all left_col_names: {}".format(
            self.completed_calculation_result.all_left_col_names
        ))
        new_data = self.set_schema(new_data, self.completed_calculation_result.all_left_col_names)

        one_data = new_data.first()[1]
        LOGGER.debug(
            "In feature calculation transform, Before transform: {}, length: {} After transform: {}, length: {}".format(
                before_one_data[1].features, len(before_one_data[1].features),
                one_data.features, len(one_data.features)))

        return new_data

    def _abnormal_detection(self, data_instances):
        """
        Make sure input data_instances is valid.
        """
        abnormal_detection.empty_table_detection(data_instances)
        abnormal_detection.empty_feature_detection(data_instances)

    def set_schema(self, data_instance, header=None):
        if header is None:
            self.schema["header"] = self.curt_calculate_properties.header
        else:
            self.schema["header"] = header
        data_instance.schema = self.schema
        return data_instance

    def update_curt_calculate_param(self):
        new_calculate_properties = CalculationProperties()
        new_calculate_properties.set_header(self.curt_calculate_properties.header)
        new_calculate_properties.set_last_left_col_indexes(self.curt_calculate_properties.all_left_col_indexes)
        new_calculate_properties.add_calculate_col_names(self.curt_calculate_properties.left_col_names)
        LOGGER.debug("In update_curt_calculate_param, header: {}, cols_map: {},"
                     "last_left_col_indexes: {}, calculate_col_names: {}".format(
            new_calculate_properties.header,
            new_calculate_properties.col_name_maps,
            new_calculate_properties.last_left_col_indexes,
            new_calculate_properties.calculate_col_names
        ))
        self.curt_calculate_properties = new_calculate_properties

    def _filter(self, data_instances, method, suffix):
        this_filter = filter_factory.get_filter(filter_name=method, model_param=self.model_param, role=self.role)
        this_filter.set_calculation_properties(self.curt_calculate_properties)
        this_filter.set_statics_obj(self.static_obj)
        this_filter.set_binning_obj(self.binning_model)
        this_filter.set_transfer_variable(self.transfer_variable)
        self.curt_calculate_properties = this_filter.fit(data_instances, suffix).calculation_properties
        provider_calculate_properties = getattr(this_filter, 'provider_calculation_properties', None)
        LOGGER.debug("method: {}, provider_calculate_properties: {}".format(
            method, provider_calculate_properties))

        self.completed_calculation_result.add_filter_results(filter_name=method,
                                                             calculate_properties=self.curt_calculate_properties,
                                                             provider_calculate_properties=provider_calculate_properties)
        LOGGER.debug("method: {}, calculation_cols: {}, left_cols: {}".format(
            method, self.curt_calculate_properties.calculate_col_names, self.curt_calculate_properties.left_col_names))
        self.update_curt_calculate_param()
        LOGGER.debug("After updated, method: {}, calculation_cols: {}, left_cols: {}".format(
            method, self.curt_calculate_properties.calculate_col_names, self.curt_calculate_properties.left_col_names))
        self.meta_dicts = this_filter.get_meta_obj(self.meta_dicts)

    def fit(self, data_instances):
        LOGGER.info("Start Vert Calculation Fit and transform.")
        self._abnormal_detection(data_instances)
        self._init_calculate_params(data_instances)

        if len(self.curt_calculate_properties.calculate_col_indexes) == 0:
            LOGGER.warning("None of columns has been set to calculat")
        else:
            for filter_idx, method in enumerate(self.filter_methods):
                self._filter(data_instances, method, suffix=str(filter_idx))

        new_data = self._transfer_data(data_instances)
        LOGGER.info("Finish Vert Calculation Fit and transform.")
        return new_data

    @assert_io_num_rows_equal
    def transform(self, data_instances):
        self._abnormal_detection(data_instances)
        self._init_calculate_params(data_instances)
        new_data = self._transfer_data(data_instances)
        return new_data
