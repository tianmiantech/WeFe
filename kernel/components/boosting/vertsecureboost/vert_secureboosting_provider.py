#!/usr/bin/env python    
# -*- coding: utf-8 -*- 

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



import copy
import functools
from typing import List

import numpy as np
from numpy import random
from scipy import sparse as sp

from common.python.utils import log_utils
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.vertfeaturebinning.param import FeatureBinningParam
from kernel.components.boosting import BoostingTree
from kernel.components.boosting import VertDecisionTreeProvider
from kernel.components.boosting.param import VertSecureBoostParam
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import BoostingTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta, CriterionMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import QuantileMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import BoostingTreeModelParam
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.transfer.variables.transfer_class.vert_secure_boost_transfer_variable import \
    VertSecureBoostingTransferVariable
from kernel.utils import consts
from kernel.utils.anonymous_generator import generate_anonymous
from kernel.utils.data_util import NoneType
from kernel.utils.io_check import assert_io_num_rows_equal

LOGGER = log_utils.get_logger()


class VertSecureBoostingProvider(BoostingTree):
    def __init__(self):
        super(VertSecureBoostingProvider, self).__init__()

        self.transfer_variable = VertSecureBoostingTransferVariable()
        # self.flowid = 0
        self.tree_dim = None
        self.feature_num = None
        self.trees_ = []
        self.tree_meta = None
        self.bin_split_points = None
        self.bin_sparse_points = None
        self.data_bin = None
        self.role = consts.PROVIDER
        self.model_save_to_storage = True
        self.model_param = VertSecureBoostParam()
        # for fast hist
        self.sparse_opt_para = False
        self.run_sparse_opt = False
        self.has_transformed_data = False
        self.data_bin_dense = None

        self.complete_secure = False
        self.enable_goss = False

        self.cipher_compressing = False
        self.new_ver = True

    def _init_model(self, param: VertSecureBoostParam):

        super(VertSecureBoostingProvider, self)._init_model(param)
        self.complete_secure = param.complete_secure
        self.sparse_opt_para = param.sparse_optimization
        self.enable_goss = param.run_goss
        self.cipher_compressing = param.cipher_compress
        self.new_ver = param.new_ver

    def convert_feature_to_bin(self, data_instance):
        LOGGER.info("convert feature to bins")
        param_obj = FeatureBinningParam(bin_num=self.bin_num)
        if self.use_missing:
            binning_obj = QuantileBinning(param_obj, abnormal_list=[NoneType()])
        else:
            binning_obj = QuantileBinning(param_obj)

        binning_obj.fit_split_points(data_instance)
        self.data_bin, self.bin_split_points, self.bin_sparse_points = binning_obj.convert_feature_to_bin(data_instance)

    def sample_valid_features(self):
        LOGGER.info("sample valid features")
        if self.feature_num is None:
            self.feature_num = self.bin_split_points.shape[0]

        choose_feature = random.choice(range(0, self.feature_num), \
                                       max(1, int(self.subsample_feature_rate * self.feature_num)), replace=False)

        valid_features = [False for i in range(self.feature_num)]
        for fid in choose_feature:
            valid_features[fid] = True
        return valid_features

    def generate_flowid(self, round_num, tree_num):
        LOGGER.info("generate flowid, flowid {}".format(self.flowid))
        return ".".join(map(str, [self.flowid, round_num, tree_num]))

    def sync_tree_dim(self):
        LOGGER.info("sync tree dim from promoter")
        self.tree_dim = self.transfer_variable.tree_dim.get(idx=0)
        LOGGER.info("tree dim is %d" % (self.tree_dim))

    def sync_predict_start_round(self):
        return self.transfer_variable.predict_start_round.get(idx=0)

    def sync_begin_iter(self):
        return self.transfer_variable.begin_iter.get(idx=0)

    def sync_stop_flag(self, num_round):
        LOGGER.info("sync stop flag from promoter, boosting round is {}".format(num_round))
        stop_flag = self.transfer_variable.stop_flag.get(idx=0,
                                                         suffix=(num_round,))
        return stop_flag

    def sync_predict_stop_flag(self, num_round):
        LOGGER.info("get predict stop flag from promoter, boosting round is {}".format(num_round))
        stop_flag = self.transfer_variable.predict_stop_flag.get(idx=0,
                                                                 suffix=(num_round,))
        return stop_flag

    def sync_provider_predict_data(self, predict_data, comm_round):
        LOGGER.info("sync provider predict data {}".format(predict_data))
        self.transfer_variable.provider_predict_data.remote(predict_data,
                                                            role=consts.PROMOTER,
                                                            idx=-1,
                                                            suffix=(comm_round,))

    @staticmethod
    def sparse_to_array(data, feature_sparse_point_array, use_missing, zero_as_missing):
        new_data = copy.deepcopy(data)
        new_feature_sparse_point_array = copy.deepcopy(feature_sparse_point_array)
        for k, v in data.features.get_all_data():
            if v == NoneType():
                value = -1
            else:
                value = v
            new_feature_sparse_point_array[k] = value

        # as most sparse point is bin-0
        # when mark it as a missing value (-1), offset it to make it sparse
        if not use_missing or (use_missing and not zero_as_missing):
            offset = 0
        else:
            offset = 1
        new_data.features = sp.csc_matrix(np.array(new_feature_sparse_point_array) + offset)
        return new_data

    def check_run_sp_opt(self):
        # if run fast hist, generate dense d_dtable and set related variables
        self.run_sparse_opt = (self.encrypt_param.method.lower() == consts.ITERATIVEAFFINE.lower()) and \
                              self.sparse_opt_para

        if self.run_sparse_opt:
            LOGGER.info('provider is running fast histogram mode')

        # for fast hist computation, data preparation
        if self.run_sparse_opt and not self.has_transformed_data:
            # start data transformation for fast histogram mode
            if not self.use_missing or (self.use_missing and not self.zero_as_missing):
                feature_sparse_point_array = [self.bin_sparse_points[i] for i in range(len(self.bin_sparse_points))]
            else:
                feature_sparse_point_array = [-1 for i in range(len(self.bin_sparse_points))]
            sparse_to_array = functools.partial(
                VertSecureBoostingProvider.sparse_to_array,
                feature_sparse_point_array=feature_sparse_point_array,
                use_missing=self.use_missing,
                zero_as_missing=self.zero_as_missing
            )
            self.data_bin_dense = self.data_bin.mapValues(sparse_to_array)

            self.has_transformed_data = True

    def fit(self, data_inst, validate_data=None):

        LOGGER.info("begin to train secureboosting provider model")
        self.gen_feature_fid_mapping(data_inst.schema)
        LOGGER.debug("schema is {}".format(data_inst.schema))
        data_inst = self.data_alignment(data_inst)
        self.convert_feature_to_bin(data_inst)
        self.sync_tree_dim()

        self.validation_strategy = self.init_validation_strategy(data_inst, validate_data)

        cur_best_model = self.tracker.get_training_best_model()
        bestIteration = 0
        if cur_best_model is not None:
            model_param = cur_best_model["Model_Param"]
            self.set_model_param(model_param)
            bestIteration = self.sync_begin_iter()
            self.tracker.set_task_progress(bestIteration)
        for epoch_idx in range(bestIteration, self.num_trees):
            # n_tree = []
            for tidx in range(self.tree_dim):

                model = self.fit_a_booster(epoch_idx, tidx)

                tree_meta, tree_param = model.get_model()
                self.trees_.append(tree_param)
                if self.tree_meta is None:
                    self.tree_meta = tree_meta

            if self.validation_strategy:
                LOGGER.debug('provider running validation')
                self.validation_strategy.validate(self, epoch_idx)
                if self.validation_strategy.need_stop():
                    LOGGER.debug('early stopping triggered')
                    break

            if self.n_iter_no_change is True:
                stop_flag = self.sync_stop_flag(epoch_idx)
                if stop_flag:
                    break

            self.tracker.save_training_best_model(self.export_model())
            self.tracker.add_task_progress(1)

        if self.validation_strategy and self.validation_strategy.has_saved_best_model():
            self.load_model(self.validation_strategy.cur_best_model)

        LOGGER.info("end to train secureboosting provider model")

    def fit_a_booster(self, epoch_idx: int, booster_dim: int):

        # self.check_run_sp_opt()
        tree = VertDecisionTreeProvider(tree_param=self.tree_param)
        tree.init(flowid=self.generate_flowid(epoch_idx, booster_dim),
                  valid_features=self.sample_valid_features(),
                  data_bin=self.data_bin, bin_split_points=self.bin_split_points,
                  bin_sparse_points=self.bin_sparse_points,
                  run_sprase_opt=self.run_sparse_opt,
                  data_bin_dense=self.data_bin_dense,
                  runtime_idx=self.component_properties.local_member_id,
                  goss_subsample=self.enable_goss,
                  bin_num=self.bin_num,
                  complete_secure=True if (self.complete_secure and epoch_idx == 0) else False,
                  cipher_compressing=self.cipher_compressing,
                  new_ver=self.new_ver
                  )

        tree.fit()
        return tree

    def load_booster(self, model_meta, model_param, epoch_idx, booster_idx):
        tree = VertDecisionTreeProvider(self.tree_param)
        tree.load_model(model_meta, model_param)
        tree.set_flowid(self.generate_flowid(epoch_idx, booster_idx))
        tree.set_runtime_idx(self.component_properties.local_member_id)
        return tree

    @staticmethod
    def traverse_a_tree(tree: VertDecisionTreeProvider, sample, cur_node_idx):

        nid, _ = tree.traverse_tree(predict_state=(cur_node_idx, -1), data_inst=sample,
                                    decoder=tree.decode, split_maskdict=tree.split_maskdict,
                                    missing_dir_maskdict=tree.missing_dir_maskdict, sitename=tree.sitename,
                                    tree_=tree.tree_, zero_as_missing=tree.zero_as_missing,
                                    use_missing=tree.use_missing)

        return nid, _

    @staticmethod
    def traverse_trees(leaf_pos, sample, trees: List[VertDecisionTreeProvider]):

        for t_idx, tree in enumerate(trees):

            cur_node_idx = leaf_pos['node_pos'][t_idx]
            # idx is set as -1 when a sample reaches leaf
            if cur_node_idx == -1:
                continue
            nid, _ = VertSecureBoostingProvider.traverse_a_tree(tree, sample, cur_node_idx)
            leaf_pos['node_pos'][t_idx] = nid

        return leaf_pos

    def boosting_fast_predict(self, data_inst, trees: List[VertDecisionTreeProvider]):

        comm_round = 0

        traverse_func = functools.partial(self.traverse_trees, trees=trees)

        while True:

            LOGGER.debug('cur predict round is {}'.format(comm_round))

            stop_flag = self.sync_predict_stop_flag(comm_round)
            if stop_flag:
                break

            promoter_node_pos = self.transfer_variable.promoter_predict_data.get(idx=0, suffix=(comm_round,))
            provider_node_pos = promoter_node_pos.join(data_inst, traverse_func, need_send=True)
            self.sync_provider_predict_data(provider_node_pos, comm_round)
            comm_round += 1

    @assert_io_num_rows_equal
    def predict(self, data_inst):

        LOGGER.info('running prediction')

        processed_data = self.data_alignment(data_inst)

        predict_start_round = self.sync_predict_start_round()

        rounds = len(self.trees_) // self.tree_dim
        trees = []
        for idx in range(predict_start_round, rounds):
            for booster_idx in range(self.tree_dim):
                tree = self.load_booster(self.tree_meta,
                                         self.trees_[idx * self.tree_dim + booster_idx],
                                         idx, booster_idx)
                trees.append(tree)

        if len(trees) == 0:
            LOGGER.info('no tree for predicting, prediction done')
            return

        self.boosting_fast_predict(processed_data, trees=trees)

    def get_model_meta(self):
        model_meta = BoostingTreeModelMeta()
        model_meta.tree_meta.CopyFrom(self.tree_meta)
        model_meta.num_trees = self.num_trees
        model_meta.quantile_meta.CopyFrom(QuantileMeta(bin_num=self.bin_num))

        meta_name = "VertSecureBoostingTreeProviderMeta"

        return meta_name, model_meta

    def set_model_meta(self, model_meta):
        if type(model_meta) is dict:
            tree_meta = model_meta.get("treeMeta")
            self.tree_meta = DecisionTreeModelMeta()
            self.tree_meta.max_depth = tree_meta.get("maxDepth")
            self.tree_meta.min_sample_split = tree_meta.get("minSampleSplit")
            self.tree_meta.min_impurity_split = tree_meta.get("minImpuritySplit")
            self.tree_meta.min_leaf_node = tree_meta.get("minLeafNode")
            if tree_meta.get("criterionMeta"):
                self.tree_meta.criterion_meta.CopyFrom(
                    CriterionMeta(criterion_method=tree_meta.get("criterionMeta").get("criterionMethod"),
                                  criterion_param=list(tree_meta.get("criterionMeta").get("criterionParam"))))
            self.tree_meta.use_missing = tree_meta.get("useMissing")
            self.tree_meta.zero_as_missing = tree_meta.get("zeroAsMissing")

            self.num_trees = model_meta.get("numTrees")
            self.bin_num = model_meta.get("quantileMeta").get("binNum")
        else:
            self.tree_meta = model_meta.tree_meta
            self.num_trees = model_meta.num_trees
            self.bin_num = model_meta.quantile_meta.bin_num

    def get_model_param(self):
        model_param = BoostingTreeModelParam()
        model_param.tree_num = len(list(self.trees_))
        model_param.tree_dim = self.tree_dim
        model_param.trees_.extend(self.trees_)

        anonymous_name_mapping = {}
        member_id = self.component_properties.local_member_id
        for fid, name in self.feature_name_fid_mapping.items():
            anonymous_name_mapping[generate_anonymous(fid, role=consts.PROVIDER, party_id=member_id, )] = name

        model_param.anonymous_name_mapping.update(anonymous_name_mapping)
        model_param.feature_name_fid_mapping.update(self.feature_name_fid_mapping)
        model_param.model_name = consts.VERT_SBT

        model_param.best_iteration = -1 if self.validation_strategy is None else self.validation_strategy.best_iteration

        param_name = "VertSecureBoostingTreeProviderParam"

        return param_name, model_param

    def set_model_param(self, model_param):
        if type(model_param) is dict:
            for tree in list(model_param.get("trees")):
                tree_param = DecisionTreeModelParam()
                for node in tree['tree']:
                    tree_param.tree_.add(id=node['id'],
                                             sitename=node['sitename'],
                                             fid=node['fid'],
                                             bid=node['bid'],
                                             weight=node['weight'],
                                             is_leaf=node['isLeaf'],
                                             left_nodeid=node['leftNodeid'],
                                             right_nodeid=node['rightNodeid'],
                                             missing_dir=node['missingDir'])
                splitMaskdict = dict([int(b), v] for b, v in tree['splitMaskdict'].items())
                missingDirMaskdict = dict([int(b), v] for b, v in tree['missingDirMaskdict'].items())
                tree_param.split_maskdict.update(splitMaskdict)
                tree_param.missing_dir_maskdict.update(missingDirMaskdict)
                self.trees_.append(tree_param)
            # self.trees_ = list(model_param.get("trees"))
            self.tree_dim = model_param.get("treeDim")
            featureNameFidMapping = dict([int(b), v] for b, v in model_param['featureNameFidMapping'].items())
            self.feature_name_fid_mapping.update(featureNameFidMapping)
        else:
            self.trees_ = list(model_param.trees_)
            self.tree_dim = model_param.tree_dim
            self.feature_name_fid_mapping.update(model_param.feature_name_fid_mapping)

    def export_model(self):
        if self.need_cv:
            return None

        meta_name, meta_protobuf = self.get_model_meta()
        param_name, param_protobuf = self.get_model_param()

        return {meta_name: meta_protobuf, param_name: param_protobuf}

    def load_model(self, model_dict):
        LOGGER.info("load model")
        model_param = None
        model_meta = None
        for _, value in model_dict["model"].items():
            for model in value:
                if type(model) == str:
                    if model.endswith("Meta"):
                        model_meta = value[model]
                    if model.endswith("Param"):
                        model_param = value[model]
                else:
                    for obj in model.items():
                        key = obj[0]
                        if key.endswith("Meta"):
                            model_meta = obj[1]
                        if key.endswith("Param"):
                            model_param = obj[1]

        self.set_model_meta(model_meta)
        self.set_model_param(model_param)
