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
from operator import itemgetter
from typing import List

import numpy as np

from common.python.utils import log_utils
from kernel.components.boosting.param import VertFastSecureBoostParam
from kernel.components.boosting.vertfastsecureboost import vert_fast_secureboost_plan as plan
from kernel.components.boosting.vertfastsecureboost.vert_fast_decision_tree_provider import VertFastDecisionTreeProvider
from kernel.components.boosting.vertsecureboost.vert_secureboosting_provider import VertSecureBoostingProvider
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import FeatureImportanceInfo
from kernel.utils import consts

LOGGER = log_utils.get_logger()


# make_readable_feature_importance = VertSecureBoostingPromoter.make_readable_feature_importance


class VertFastSecureBoostingTreeProvider(VertSecureBoostingProvider):

    def __init__(self):
        super(VertFastSecureBoostingTreeProvider, self).__init__()

        self.tree_num_per_member = 1
        self.promoter_depth = 0
        self.provider_depth = 0
        self.work_mode = consts.MIX_TREE
        self.tree_plan = []
        self.model_param = VertFastSecureBoostParam()
        self.model_name = 'VertFastSecureBoost'

        self.feature_importances_ = {}

    def _init_model(self, param: VertFastSecureBoostParam):
        super(VertFastSecureBoostingTreeProvider, self)._init_model(param)
        self.tree_num_per_member = param.tree_num_per_member
        self.work_mode = param.work_mode
        self.promoter_depth = param.promoter_depth
        self.provider_depth = param.provider_depth

    def get_tree_plan(self, idx):

        if len(self.tree_plan) == 0:
            self.tree_plan = plan.create_tree_plan(self.work_mode, k=self.tree_num_per_member, tree_num=self.num_trees,
                                                   provider_list=self.component_properties.provider_member_idlist)
            LOGGER.info('tree plan is {}'.format(self.tree_plan))

        return self.tree_plan[idx]

    def update_feature_importance(self, tree_feature_importance):
        for fid in tree_feature_importance:
            if fid not in self.feature_importances_:
                self.feature_importances_[fid] = tree_feature_importance[fid]
            else:
                self.feature_importances_[fid] += tree_feature_importance[fid]

    def check_provider_number(self, tree_type):
        provider_num = len(self.component_properties.provider_member_idlist)
        LOGGER.info('provider number is {}'.format(provider_num))
        if tree_type == plan.tree_type_dict['layered_tree']:
            assert provider_num == 1, 'only 1 provider party is allowed in layered mode'

    def fit(self, data_inst, validate_data=None):

        LOGGER.info("begin to train fast secureboosting provider model")
        self.gen_feature_fid_mapping(data_inst.schema)
        LOGGER.debug("schema is {}".format(data_inst.schema))
        data_inst = self.data_alignment(data_inst)
        self.convert_feature_to_bin(data_inst)
        self.sync_tree_dim()

        self.validation_strategy = self.init_validation_strategy(data_inst, validate_data)

        cur_best_model = self.tracker.get_training_best_model()
        if cur_best_model is not None:
            model_param = cur_best_model["Model_Param"]
            self.set_model_param(model_param)
        bestIteration = self.sync_begin_iter()
        for i in range(bestIteration, self.num_trees):
            # n_tree = []
            for tidx in range(self.tree_dim):
                model = self.fit_a_booster(i, tidx)
                tree_meta, tree_param = model.get_model()
                self.trees_.append(tree_param)
                if self.tree_meta is None:
                    self.tree_meta = tree_meta

            if self.validation_strategy:
                LOGGER.debug('provider running validation')
                self.validation_strategy.validate(self, i)
                if self.validation_strategy.need_stop():
                    LOGGER.debug('early stopping triggered')
                    break

            if self.n_iter_no_change is True:
                stop_flag = self.sync_stop_flag(i)
                if stop_flag:
                    break

            self.tracker.save_training_best_model(self.export_model())
            self.tracker.add_task_progress(1)

        if self.validation_strategy and self.validation_strategy.has_saved_best_model():
            self.load_model(self.validation_strategy.cur_best_model)

        LOGGER.info("end to train fast secureboosting provider model")

    def fit_a_booster(self, epoch_idx: int, booster_dim: int):

        tree_type, target_provider_id = self.get_tree_plan(epoch_idx)
        self.check_provider_number(tree_type)
        # self.check_run_sp_opt()
        tree = VertFastDecisionTreeProvider(tree_param=self.tree_param)
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
                  cipher_compressing=self.round_decimal is not None,
                  round_decimal=self.round_decimal,
                  new_ver=self.new_ver
                  )

        tree.set_tree_work_mode(tree_type, target_provider_id)
        tree.set_layered_depth(self.promoter_depth, self.provider_depth)
        tree.set_self_provider_id(self.component_properties.local_member_id)
        tree.set_provider_member_idlist(self.component_properties.provider_member_idlist)
        LOGGER.debug('tree work mode is {}'.format(tree_type))
        tree.fit()
        self.update_feature_importance(tree.get_feature_importance())
        return tree

    def load_booster(self, model_meta, model_param, epoch_idx, booster_idx):

        tree = VertFastDecisionTreeProvider(self.tree_param)
        tree.load_model(model_meta, model_param)
        tree.set_flowid(self.generate_flowid(epoch_idx, booster_idx))
        tree.set_runtime_idx(self.component_properties.local_member_id)

        tree_type, target_provider_id = self.get_tree_plan(epoch_idx)

        # target_provider_id and self_provider_id and target_provider_id are related to prediction
        tree.set_tree_work_mode(tree_type, target_provider_id)
        tree.set_self_provider_id(self.component_properties.local_member_id)

        if self.tree_plan[epoch_idx][0] == plan.tree_type_dict['promoter_feat_only']:
            tree.use_promoter_feat_only_predict_mode()

        return tree

    # def generate_summary(self) -> dict:
    #     summary = super(VertFastSecureBoostingTreeProvider, self).generate_summary()
    #     summary['feature_importance'] = make_readable_feature_importance(self.feature_name_fid_mapping,
    #                                                                      self.feature_importances_)
    #     return summary

    @staticmethod
    def traverse_provider_local_trees(node_pos, sample, trees: List[VertFastDecisionTreeProvider]):

        """
        in mix mode, a sample can reach leaf directly
        """

        for i in range(len(trees)):

            tree = trees[i]
            if len(tree.tree_node) == 0:  # this tree belongs to other party because it has no tree node
                continue
            leaf_id = tree.provider_local_traverse_tree(sample, tree.tree_node, use_missing=tree.use_missing,
                                                        zero_as_missing=tree.zero_as_missing)
            node_pos[i] = leaf_id

        return node_pos

    # this func will be called by super class's predict()
    def boosting_fast_predict(self, data_inst, trees: List[VertFastDecisionTreeProvider]):

        LOGGER.info('fast sbt running predict')

        if self.work_mode == consts.MIX_TREE:

            LOGGER.info('running mix mode predict')

            tree_num = len(trees)
            node_pos = data_inst.mapValues(lambda x: np.zeros(tree_num, dtype=np.int64))
            local_traverse_func = functools.partial(self.traverse_provider_local_trees, trees=trees)
            leaf_pos = node_pos.join(data_inst, local_traverse_func)
            self.transfer_variable.provider_predict_data.remote(leaf_pos, idx=0, role=consts.PROMOTER)

        else:

            LOGGER.info('running layered mode predict')

            super(VertFastSecureBoostingTreeProvider, self).boosting_fast_predict(data_inst, trees)

    def get_model_meta(self):

        _, model_meta = super(VertFastSecureBoostingTreeProvider, self).get_model_meta()
        meta_name = "VertFastSecureBoostingTreeProviderMeta"
        model_meta.work_mode = self.work_mode

        return meta_name, model_meta

    def get_model_param(self):

        _, model_param = super(VertFastSecureBoostingTreeProvider, self).get_model_param()
        param_name = "VertFastSecureBoostingTreeProviderParam"
        model_param.tree_plan.extend(plan.encode_plan(self.tree_plan))
        model_param.model_name = consts.VERT_FAST_SBT_MIX if self.work_mode == consts.MIX_TREE else \
            consts.VERT_FAST_SBT_LAYERED
        # in mix mode, provider can output feature importance
        feature_importances = list(self.feature_importances_.items())
        feature_importances = sorted(feature_importances, key=itemgetter(1), reverse=True)
        feature_importance_param = []
        for fid, importance in feature_importances:
            feature_importance_param.append(FeatureImportanceInfo(sitename=self.role,
                                                                  fid=fid,
                                                                  importance=importance.importance,
                                                                  fullname=self.feature_name_fid_mapping[fid]))
        model_param.feature_importances.extend(feature_importance_param)

        return param_name, model_param

    def set_model_meta(self, model_meta):
        super(VertFastSecureBoostingTreeProvider, self).set_model_meta(model_meta)
        self.work_mode = model_meta.work_mode

    def set_model_param(self, model_param):
        super(VertFastSecureBoostingTreeProvider, self).set_model_param(model_param)
        self.tree_plan = plan.decode_plan(model_param.tree_plan)
