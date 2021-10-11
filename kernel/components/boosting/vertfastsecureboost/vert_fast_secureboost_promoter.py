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
from typing import List

import numpy as np

from common.python.utils import log_utils
from kernel.components.boosting.param import VertFastSecureBoostParam
from kernel.components.boosting.vertfastsecureboost import vert_fast_secureboost_plan as plan
from kernel.components.boosting.vertfastsecureboost.vert_fast_decision_tree_promoter import VertFastDecisionTreePromoter
from kernel.components.boosting.vertsecureboost.vert_secureboosting_promoter import VertSecureBoostingPromoter
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class VertFastSecureBoostingTreePromoter(VertSecureBoostingPromoter):

    def __init__(self):
        super(VertFastSecureBoostingTreePromoter, self).__init__()

        self.tree_num_per_member = 1
        self.promoter_depth = 0
        self.provider_depth = 0
        self.work_mode = consts.MIX_TREE
        self.tree_plan = []
        self.model_param = VertFastSecureBoostParam()
        self.model_name = 'VertFastSecureBoost'

    def _init_model(self, param: VertFastSecureBoostParam):
        super(VertFastSecureBoostingTreePromoter, self)._init_model(param)
        LOGGER.debug('loss func is {}'.format(param.objective_param.objective))
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

    def check_provider_number(self, tree_type):
        provider_num = len(self.component_properties.provider_member_idlist)
        LOGGER.info('provider number is {}'.format(provider_num))
        if tree_type == plan.tree_type_dict['layered_tree']:
            assert provider_num == 1, 'only 1 provider party is allowed in layered mode'

    def fit(self, data_inst, validate_data=None):
        LOGGER.info("begin to train fast secureboosting promoter model")
        self.gen_feature_fid_mapping(data_inst.schema)
        self.validation_strategy = self.init_validation_strategy(data_inst, validate_data)
        data_inst = self.data_alignment(data_inst)
        self.convert_feature_to_bin(data_inst)
        self.set_y()
        self.init_predict_score()
        self.generate_encrypter()

        self.sync_tree_dim()

        cur_best_model = self.tracker.get_training_best_model()
        bestIteration = 0
        if cur_best_model is not None:
            model_param = cur_best_model["Model_Param"]
            bestIteration = model_param['treeNum']
            self.set_model_param(model_param)
        self.sync_begin_iter(bestIteration)
        for i in range(bestIteration, self.num_trees):
            self.compute_grad_and_hess(data_inst)
            for tidx in range(self.tree_dim):

                model = self.fit_a_booster(i, tidx)
                tree_meta, tree_param = model.get_model()
                self.trees_.append(tree_param)
                if self.tree_meta is None:
                    self.tree_meta = tree_meta
                self.update_predict_score(new_f=model.predict_weights, dim=tidx)
                # self.update_feature_importance(model.get_feature_importance())

            loss = self.compute_loss()
            self.history_loss.append(loss)
            LOGGER.info("round {} loss is {}".format(i, loss))

            LOGGER.debug("type of loss is {}".format(type(loss).__name__))

            metric_meta = {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS'}
            self.callback_metric(metric_name='loss',
                                 metric_namespace='train',
                                 metric_meta=metric_meta,
                                 metric_data=(i, loss))

            if self.validation_strategy:
                self.validation_strategy.validate(self, i)
                if self.validation_strategy.need_stop():
                    LOGGER.debug('early stopping triggered')
                    break

            if self.n_iter_no_change is True:
                if self.check_convergence(loss):
                    self.sync_stop_flag(True, i)
                    break
                else:
                    self.sync_stop_flag(False, i)

            self.tracker.save_training_best_model(self.export_model())
            self.tracker.add_task_progress(1)

        LOGGER.debug("history loss is {}".format(min(self.history_loss)))
        self.callback_metric("loss",
                             "train",
                             {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS'},
                             metric_data=("Best", min(self.history_loss)))

        if self.validation_strategy and self.validation_strategy.has_saved_best_model():
            self.load_model(self.validation_strategy.cur_best_model)

        LOGGER.info("end to train fast secureboosting promoter model")

    def fit_a_booster(self, epoch_idx: int, booster_dim: int):

        # prepare tree plan
        tree_type, target_host_id = self.get_tree_plan(epoch_idx)
        LOGGER.info('tree work mode is {}'.format(tree_type))
        self.check_provider_number(tree_type)

        g_h = self.get_grad_and_hess(booster_dim)

        tree = VertFastDecisionTreePromoter(tree_param=self.tree_param)
        tree.init(flowid=self.generate_flowid(epoch_idx, booster_dim),
                  data_bin=self.data_bin, bin_split_points=self.bin_split_points,
                  bin_sparse_points=self.bin_sparse_points,
                  grad_and_hess=g_h,
                  encrypter=self.encrypter, encrypted_mode_calculator=self.encrypted_calculator,
                  valid_features=self.sample_valid_features(),
                  provider_member_idlist=self.component_properties.provider_member_idlist,
                  runtime_idx=self.component_properties.local_member_id,
                  goss_subsample=self.enable_goss,
                  top_rate=self.top_rate, other_rate=self.other_rate,
                  complete_secure=True if (epoch_idx == 0 and self.complete_secure) else False,
                  cipher_compressing=self.round_decimal is not None,
                  round_decimal=self.round_decimal,
                  encrypt_key_length=self.encrypt_param.key_length,
                  max_sample_weight=self.max_sample_weight,
                  new_ver=self.new_ver
                  )
        tree.set_tree_work_mode(tree_type, target_host_id)
        tree.set_layered_depth(self.promoter_depth, self.provider_depth)
        tree.fit()
        self.update_feature_importance(tree.get_feature_importance())
        return tree

    @staticmethod
    def traverse_promoter_local_trees(node_pos, sample, trees: List[VertFastDecisionTreePromoter]):

        """
        in mix mode, a sample can reach leaf directly
        """

        for t_idx, tree in enumerate(trees):

            cur_node_idx = node_pos[t_idx]
            if not tree.use_promoter_feat_only_predict_mode:
                continue
            rs, reach_leaf = VertSecureBoostingPromoter.traverse_a_tree(tree, sample, cur_node_idx)
            node_pos[t_idx] = rs

        return node_pos

    @staticmethod
    def merge_leaf_pos(pos1, pos2):
        return pos1 + pos2

    # this func will be called by super class's predict()
    def boosting_fast_predict(self, data_inst, trees: List[VertFastDecisionTreePromoter], predict_cache=None):

        LOGGER.info('fast sbt running predict')

        if self.work_mode == consts.MIX_TREE:

            LOGGER.info('running mix mode predict')

            tree_num = len(trees)
            node_pos = data_inst.mapValues(lambda x: np.zeros(tree_num, dtype=np.int64))

            # traverse local trees
            traverse_func = functools.partial(self.traverse_promoter_local_trees, trees=trees)
            promoter_leaf_pos = node_pos.join(data_inst, traverse_func)
            # get leaf node from other provider parties
            provider_leaf_pos_list = self.transfer_variable.provider_predict_data.get(idx=-1)

            for provider_leaf_pos in provider_leaf_pos_list:
                promoter_leaf_pos = promoter_leaf_pos.join(provider_leaf_pos, self.merge_leaf_pos)

            predict_result = self.get_predict_scores(leaf_pos=promoter_leaf_pos, learning_rate=self.learning_rate,
                                                     init_score=self.init_score, trees=trees,
                                                     multi_class_num=self.tree_dim, predict_cache=predict_cache)

            return predict_result

        else:

            LOGGER.debug('running layered mode predict')
            return super(VertFastSecureBoostingTreePromoter, self).boosting_fast_predict(data_inst, trees,
                                                                                         predict_cache)

    def load_booster(self, model_meta, model_param, epoch_idx, booster_idx):

        tree = VertFastDecisionTreePromoter(self.tree_param)
        tree.load_model(model_meta, model_param)
        tree.set_flowid(self.generate_flowid(epoch_idx, booster_idx))
        tree.set_runtime_idx(self.component_properties.local_member_id)
        tree.set_provider_member_idlist(self.component_properties.provider_member_idlist)

        tree_type, target_provider_id = self.get_tree_plan(epoch_idx)
        tree.set_tree_work_mode(tree_type, target_provider_id)

        if self.tree_plan[epoch_idx][0] == plan.tree_type_dict['promoter_feat_only']:
            LOGGER.debug('tree of epoch {} is promoter only'.format(epoch_idx))
            tree.use_promoter_feat_only_predict_mode()

        return tree

    def get_model_meta(self):

        _, model_meta = super(VertFastSecureBoostingTreePromoter, self).get_model_meta()
        meta_name = "VertFastSecureBoostingTreePromoterMeta"
        model_meta.work_mode = self.work_mode

        return meta_name, model_meta

    def get_model_param(self):

        _, model_param = super(VertFastSecureBoostingTreePromoter, self).get_model_param()
        param_name = "VertFastSecureBoostingTreePromoterParam"
        model_param.tree_plan.extend(plan.encode_plan(self.tree_plan))
        model_param.model_name = consts.VERT_FAST_SBT_MIX if self.work_mode == consts.MIX_TREE else \
            consts.VERT_FAST_SBT_LAYERED

        return param_name, model_param

    def set_model_meta(self, model_meta):
        super(VertFastSecureBoostingTreePromoter, self).set_model_meta(model_meta)
        self.work_mode = model_meta.work_mode

    def set_model_param(self, model_param):
        super(VertFastSecureBoostingTreePromoter, self).set_model_param(model_param)
        self.tree_plan = plan.decode_plan(model_param.tree_plan)
