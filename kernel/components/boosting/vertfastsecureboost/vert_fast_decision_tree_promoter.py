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

from common.python.utils import log_utils
from kernel.components.boosting.core.node import Node
from kernel.components.boosting.vertfastsecureboost import vert_fast_secureboost_plan as plan
from kernel.components.boosting.vertsecureboost.vert_decision_tree_promoter import VertDecisionTreePromoter
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class VertFastDecisionTreePromoter(VertDecisionTreePromoter):

    def __init__(self, tree_param):
        super(VertFastDecisionTreePromoter, self).__init__(tree_param)
        self.node_plan = []
        self.node_plan_idx = 0
        self.tree_type = None
        self.target_provider_id = -1
        self.promoter_depth = 0
        self.provider_depth = 0
        self.cur_dep = 0
        self.use_promoter_feat_when_predict = False

    """
    Setting
    """

    def use_promoter_feat_only_predict_mode(self):
        self.use_promoter_feat_when_predict = True

    def set_tree_work_mode(self, tree_type, target_provider_id):
        self.tree_type, self.target_provider_id = tree_type, target_provider_id

    def set_layered_depth(self, promoter_depth, provider_depth):
        self.promoter_depth, self.provider_depth = promoter_depth, provider_depth

    """
    Tree Plan
    """

    def initialize_node_plan(self):
        if self.tree_type == plan.tree_type_dict['layered_tree']:
            self.node_plan = plan.create_layered_tree_node_plan(promoter_depth=self.promoter_depth,
                                                                provider_depth=self.provider_depth,
                                                                provider_list=self.provider_member_idlist)
            self.max_depth = len(self.node_plan)
            LOGGER.debug('max depth reset to {}, cur node plan is {}'.format(self.max_depth, self.node_plan))
        else:
            self.node_plan = plan.create_node_plan(self.tree_type, self.target_provider_id, self.max_depth)

    def get_node_plan(self, idx):
        return self.node_plan[idx]

    def provider_id_to_idx(self, provider_id):
        if provider_id == -1 or provider_id == '-1':
            return -1
        return self.provider_member_idlist.index(provider_id)

    def compute_best_splits_with_node_plan2(self, tree_action, target_provider_idx, node_map: dict,
                                            dep: int, batch_idx: int, mode=consts.SKIP_TREE):

        LOGGER.debug('node plan2 at dep {} is {}'.format(dep, (tree_action, target_provider_idx)))

        # In layered mode, promoter hist computation does not start from root node, so need to disable hist-sub
        hist_sub = True if mode == consts.SKIP_TREE else False

        if tree_action == plan.tree_actions['promoter_only']:
            node_dispatch = self.get_computing_node_dispatch()
            node_sample_count = self.count_node_sample_num(node_dispatch, node_map)
            LOGGER.debug('sample count is {}'.format(node_sample_count))
            acc_histograms = self.get_local_histograms(dep, self.data_bin_with_node_dispatch, self.grad_and_hess,
                                                       node_sample_count, self.cur_split_nodes, node_map, ret='tensor',
                                                       hist_sub=hist_sub)

            best_split_info_promoter = self.splitter.find_split(acc_histograms, self.valid_features,
                                                                self.data_bin._partitions, self.sitename,
                                                                self.use_missing, self.zero_as_missing)

            return best_split_info_promoter

        if tree_action == plan.tree_actions['provider_only']:

            split_info_table = self.transfer_inst.encrypted_splitinfo_provider.get(idx=target_provider_idx,
                                                                                   suffix=(dep, batch_idx))

            provider_split_info = self.splitter.find_provider_best_split_info(split_info_table,
                                                                              self.get_provider_sitename(target_provider_idx),
                                                                              self.encrypter,
                                                                              gh_packer=self.packer,)

            split_info_list = [None for i in range(len(provider_split_info))]
            for key in provider_split_info:
                split_info_list[node_map[key]] = provider_split_info[key]

            # MIX mode and Layered mode difference:
            if mode == consts.SKIP_TREE:
                for split_info in split_info_list:
                    split_info.sum_grad, split_info.sum_hess, split_info.gain = self.encrypt(split_info.sum_grad), \
                                                                                self.encrypt(split_info.sum_hess), \
                                                                                self.encrypt(split_info.gain)
                return_split_info = split_info_list
            else:
                return_split_info = copy.deepcopy(split_info_list)
                for split_info in return_split_info:
                    split_info.sum_grad, split_info.sum_hess, split_info.gain = None, None, None

            self.transfer_inst.federated_best_splitinfo_provider.remote(return_split_info,
                                                                        suffix=(dep, batch_idx),
                                                                        idx=target_provider_idx,
                                                                        role=consts.PROVIDER)

            if mode == consts.SKIP_TREE:
                return []
            elif mode == consts.LAYERED_TREE:

                final_provider_split_info = self.sync_final_split_provider(dep, batch_idx, idx=target_provider_idx)
                for s1, s2 in zip(split_info_list, final_provider_split_info[0]):
                    s2.gain = s1.gain
                    s2.sum_grad = s1.sum_grad
                    s2.sum_hess = s1.sum_hess

                cur_best_split = self.merge_splitinfo(splitinfo_promoter=[],
                                                      splitinfo_provider=final_provider_split_info,
                                                      merge_provider_split_only=True,
                                                      need_decrypt=False)
                return cur_best_split

    """
    Compute split point
    """

    def compute_best_splits_with_node_plan(self, tree_action, target_provider_idx, node_map: dict, dep: int,
                                           batch_idx: int, mode=consts.SKIP_TREE):

        LOGGER.debug('node plan at dep {} is {}'.format(dep, (tree_action, target_provider_idx)))

        cur_best_split = []

        if tree_action == plan.tree_actions['promoter_only']:
            acc_histograms = self.get_local_histograms(dep, self.data_bin_with_node_dispatch, self.grad_and_hess,
                                                       None, self.cur_split_nodes, node_map, ret='tensor',
                                                       hist_sub=False)

            cur_best_split = self.splitter.find_split(acc_histograms, self.valid_features,
                                                      self.data_bin._partitions, self.sitename,
                                                      self.use_missing, self.zero_as_missing)
            LOGGER.debug('computing local splits done')

        if tree_action == plan.tree_actions['provider_only']:

            self.federated_find_split(dep, batch_idx, idx=target_provider_idx)

            if mode == consts.LAYERED_TREE:
                provider_split_info = self.sync_final_split_provider(dep, batch_idx, idx=target_provider_idx)
                LOGGER.debug('get encrypted split value from provider')

                cur_best_split = self.merge_splitinfo(splitinfo_promoter=[],
                                                      splitinfo_provider=provider_split_info,
                                                      merge_provider_split_only=True)

        return cur_best_split

    """
    Tree update
    """

    def assign_instances_to_new_node_with_node_plan(self, dep, tree_action, mode=consts.SKIP_TREE, ):

        LOGGER.info("redispatch node of depth {}".format(dep))
        dispatch_node_method = functools.partial(self.dispatch_node,
                                                 tree_=self.tree_,
                                                 decoder=self.decode,
                                                 sitename=self.sitename,
                                                 split_maskdict=self.split_maskdict,
                                                 bin_sparse_points=self.bin_sparse_points,
                                                 use_missing=self.use_missing,
                                                 zero_as_missing=self.zero_as_missing,
                                                 missing_dir_maskdict=self.missing_dir_maskdict)

        dispatch_promoter_result = self.data_bin_with_node_dispatch.mapValues(dispatch_node_method)
        LOGGER.info("remask dispatch node result of depth {}".format(dep))

        dispatch_to_provider_result = dispatch_promoter_result.filter(
            lambda key, value: isinstance(value, tuple) and len(value) > 2, need_send=True)

        dispatch_promoter_result = dispatch_promoter_result.subtractByKey(dispatch_to_provider_result)
        leaf = dispatch_promoter_result.filter(lambda key, value: isinstance(value, tuple) is False)

        if self.sample_leaf_pos is None:
            self.sample_leaf_pos = leaf
        else:
            self.sample_leaf_pos = self.sample_leaf_pos.union(leaf)

        dispatch_promoter_result = dispatch_promoter_result.subtractByKey(leaf)

        if tree_action == plan.tree_actions['provider_only'] and mode == consts.LAYERED_TREE:
            dispatch_promoter_result = dispatch_promoter_result.subtractByKey(leaf)
            dispatch_node_provider_result = self.sync_dispatch_node_provider(dispatch_to_provider_result, dep)

            self.node_dispatch = None
            for idx in range(len(dispatch_node_provider_result)):
                if self.node_dispatch is None:
                    self.node_dispatch = dispatch_node_provider_result[idx]
                else:
                    self.node_dispatch = self.node_dispatch.join(dispatch_node_provider_result[idx],
                                                                 lambda unleaf_state_nodeid1,
                                                                        unleaf_state_nodeid2:
                                                                 unleaf_state_nodeid1 if len(
                                                                     unleaf_state_nodeid1) == 2 else
                                                                 unleaf_state_nodeid2)
            self.node_dispatch = self.node_dispatch.union(dispatch_promoter_result)
        else:
            LOGGER.debug('skip provider only node_dispatch computation')
            self.node_dispatch = dispatch_promoter_result

    """
    Layered Mode
    """

    def layered_mode_fit(self):

        LOGGER.info('running layered mode')

        self.initialize_node_plan()

        self.init_packer_and_sync_gh()

        root_node = self.initialize_root_node()
        self.tree_node_queue = [root_node]
        self.dispatch_all_node_to_root()

        for dep in range(self.max_depth):

            tree_action, layer_target_provider_id = self.get_node_plan(dep)
            provider_idx = self.provider_id_to_idx(layer_target_provider_id)

            self.sync_tree_node_queue(self.tree_node_queue, dep)

            if len(self.tree_node_queue) == 0:
                break

            if layer_target_provider_id != -1:
                self.sync_node_positions(dep)

            self.update_instances_node_positions()

            split_info = []
            for batch_idx, i in enumerate(range(0, len(self.tree_node_queue), self.max_split_nodes)):
                self.cur_split_nodes = self.tree_node_queue[i: i + self.max_split_nodes]
                if self.new_ver:
                    cur_splitinfos = self.compute_best_splits_with_node_plan2(tree_action, provider_idx,
                                                                              node_map=self.get_node_map(
                                                                                  self.cur_split_nodes),
                                                                              dep=dep, batch_idx=batch_idx,
                                                                              mode=consts.LAYERED_TREE)
                else:
                    cur_splitinfos = self.compute_best_splits_with_node_plan(tree_action, provider_idx, node_map=
                    self.get_node_map(self.cur_split_nodes),
                                                                             dep=dep, batch_idx=batch_idx,
                                                                             mode=consts.LAYERED_TREE)
                split_info.extend(cur_splitinfos)

            max_depth_reach = True if dep + 1 == self.max_depth else False
            self.update_tree_node_queue(split_info, max_depth_reach)

            self.assign_instances_to_new_node_with_node_plan(dep, tree_action, mode=consts.LAYERED_TREE, )

        if self.tree_node_queue:
            self.assign_instance_to_leaves_and_update_weights()

        self.convert_bin_to_real()
        self.round_leaf_val()
        self.sync_tree()
        self.sample_weights_post_process()

    """
    Mix Mode
    """


    def sync_en_g_sum_h_sum(self):
        root_sum_grad, root_sum_hess = self.get_grad_hess_sum(self.grad_and_hess)
        en_g, en_h = self.encrypt(root_sum_grad), self.encrypt(root_sum_hess)
        self.transfer_inst.encrypted_grad_and_hess.remote(idx=self.provider_id_to_idx(self.target_provider_id),
                                                          obj=[en_g, en_h], suffix='ghsum', role=consts.PROVIDER)

    def skip_mode_fit(self):

        LOGGER.info('running skip mode')

        self.initialize_node_plan()

        if self.tree_type != plan.tree_type_dict['promoter_feat_only']:
            self.init_packer_and_sync_gh(idx=self.provider_id_to_idx(self.target_provider_id))
            self.sync_en_g_sum_h_sum()
        else:
            root_node = self.initialize_root_node()
            self.tree_node_queue = [root_node]
            self.dispatch_all_node_to_root()

        for dep in range(self.max_depth):

            tree_action, layer_target_provider_id = self.get_node_plan(dep)
            provider_idx = self.provider_id_to_idx(layer_target_provider_id)

            # get cur_layer_node_num
            if self.tree_type == plan.tree_type_dict['provider_feat_only']:
                self.tree_node_queue = self.sync_provider_tree_node_queue(dep, provider_idx)
                LOGGER.debug('printing cur layer nodes')
                for n in self.tree_node_queue:
                    LOGGER.debug(n)

            if len(self.tree_node_queue) == 0:
                break

            if self.tree_type == plan.tree_type_dict['promoter_feat_only']:
                self.update_instances_node_positions()

            split_info = []
            for batch_idx, i in enumerate(range(0, len(self.tree_node_queue), self.max_split_nodes)):
                self.cur_split_nodes = self.tree_node_queue[i: i + self.max_split_nodes]

                if self.new_ver:
                    cur_splitinfos = self.compute_best_splits_with_node_plan2(tree_action, provider_idx,
                                                                              node_map=self.get_node_map(
                                                                                  self.cur_split_nodes),
                                                                              dep=dep, batch_idx=batch_idx,
                                                                              mode=consts.SKIP_TREE)
                else:
                    cur_splitinfos = self.compute_best_splits_with_node_plan(tree_action, provider_idx, node_map=
                    self.get_node_map(self.cur_split_nodes),
                                                                             dep=dep, batch_idx=batch_idx,
                                                                             mode=consts.SKIP_TREE)
                split_info.extend(cur_splitinfos)

            if self.tree_type == plan.tree_type_dict['promoter_feat_only']:
                self.update_tree_node_queue(split_info, False)
                self.assign_instances_to_new_node_with_node_plan(dep, tree_action, provider_idx)

        if self.tree_type == plan.tree_type_dict['provider_feat_only']:
            target_idx = self.provider_id_to_idx(self.get_node_plan(0)[1])  # get provider id
            leaves = self.sync_provider_leaf_nodes(target_idx)  # get leaves node from provider
            self.tree_ = self.handle_leaf_nodes(leaves)  # decrypt node info
            self.sample_leaf_pos = self.sync_sample_leaf_pos(idx=target_idx)  # get final sample leaf id from provider

            # checking sample number
            assert self.sample_leaf_pos.count() == self.data_bin.count(), 'numbers of sample positions failed to match, ' \
                                                                'sample leaf pos number:{}, instance number {}'. \
                format(self.sample_leaf_pos.count(), self.data_bin.count())

            # self.predict_weights = self.extract_sample_weights_from_node(sample_pos)  # extract leaf weights
        else:
            if self.tree_node_queue:
                self.assign_instance_to_leaves_and_update_weights()  # promoter local updates
            self.convert_bin_to_real()  # convert bin id to real value features

        self.round_leaf_val()
        self.sample_weights_post_process()

    def skip_mode_predict(self, data_inst):

        LOGGER.info("running skip mode predict")

        if self.use_promoter_feat_when_predict:
            LOGGER.debug('predicting using promoter local tree')
            predict_data = data_inst.mapValues(lambda inst: (0, 1))
            traverse_tree = functools.partial(self.traverse_tree,
                                              tree_=self.tree_,
                                              decoder=self.decode,
                                              sitename=self.sitename,
                                              split_maskdict=self.split_maskdict,
                                              use_missing=self.use_missing,
                                              zero_as_missing=self.zero_as_missing,
                                              missing_dir_maskdict=self.missing_dir_maskdict)
            predict_result = predict_data.join(data_inst, traverse_tree)
            LOGGER.debug('promoter_predict_inst_count is {}'.format(predict_result.count()))

        else:
            LOGGER.debug('predicting using provider local tree')
            leaf_node_info = self.sync_sample_leaf_pos(idx=self.provider_id_to_idx(self.target_provider_id))
            predict_result = self.extract_sample_weights_from_node(leaf_node_info)

        self.transfer_inst.sync_flag.remote(True, idx=-1)
        return predict_result

    """
    Federation Functions
    """

    def sync_sample_leaf_pos(self, idx):
        leaf_pos = self.transfer_inst.dispatch_node_provider_result.get(idx=idx, suffix=('final sample pos',))
        return leaf_pos

    def sync_provider_tree_node_queue(self, dep, provider_idx):
        nodes = self.transfer_inst.provider_cur_to_split_node_num.get(idx=provider_idx, suffix=(dep,))
        for n in nodes:
            n.sum_grad = self.decrypt(n.sum_grad)
            n.sum_hess = self.decrypt(n.sum_hess)
        return nodes

    def sync_provider_leaf_nodes(self, idx):
        return self.transfer_inst.provider_leafs.get(idx=idx)

    """
    Mix Functions
    """

    def handle_leaf_nodes(self, nodes):
        """
        decrypte hess and grad and return tree node list that only contains leaves
        """
        max_node_id = -1
        for n in nodes:
            n.sum_hess = self.decrypt(n.sum_hess)
            n.sum_grad = self.decrypt(n.sum_grad)
            n.weight = self.splitter.node_weight(n.sum_grad, n.sum_hess)
            n.sitename = self.sitename
            if n.id > max_node_id:
                max_node_id = n.id
        new_nodes = [Node() for i in range(max_node_id + 1)]
        for n in nodes:
            new_nodes[n.id] = n
        return new_nodes

    """
    Fit & Predict
    """

    def fit(self):

        LOGGER.info('fitting a vert decision tree')

        if self.tree_type == plan.tree_type_dict['provider_feat_only'] or \
                self.tree_type == plan.tree_type_dict['promoter_feat_only']:

            self.skip_mode_fit()

        elif self.tree_type == plan.tree_type_dict['layered_tree']:

            self.layered_mode_fit()

        LOGGER.info("end to fit promoter decision tree")

    def predict(self, data_inst):
        LOGGER.info("start to predict!")
        if self.tree_type == plan.tree_type_dict['promoter_feat_only'] or \
                self.tree_type == plan.tree_type_dict['provider_feat_only']:
            predict_res = self.skip_mode_predict(data_inst)
            LOGGER.debug('input result count {} , out count {}'.format(data_inst.count(), predict_res.count()))
            return predict_res
        else:
            LOGGER.debug('running layered mode predict')
            return super(VertFastDecisionTreePromoter, self).predict(data_inst)

    def get_model_meta(self):
        return super(VertFastDecisionTreePromoter, self).get_model_meta()

    def get_model_param(self):
        return super(VertFastDecisionTreePromoter, self).get_model_param()
