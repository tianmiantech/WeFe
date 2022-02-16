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

from common.python.utils import log_utils
from kernel.components.boosting.core.node import Node
from kernel.components.boosting.core.splitter import SplitInfo
from kernel.components.boosting.vertfastsecureboost import vert_fast_secureboost_plan as plan
from kernel.components.boosting.vertsecureboost.vert_decision_tree_provider import VertDecisionTreeProvider
from kernel.utils import consts
from kernel.utils.data_util import NoneType

LOGGER = log_utils.get_logger()

import numpy as np
import functools
import copy


class VertFastDecisionTreeProvider(VertDecisionTreeProvider):

    def __init__(self, tree_param):
        super(VertFastDecisionTreeProvider, self).__init__(tree_param)
        self.node_plan = []
        self.node_plan_idx = 0
        self.tree_type = None
        self.target_provider_id = -1
        self.promoter_depth = 0
        self.provider_depth = 0
        self.cur_dep = 0
        self.self_provider_id = -1
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

    def set_self_provider_id(self, self_provider_id):
        self.self_provider_id = self_provider_id

    """
    Node Plan
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

    """
    Provider local split computation
    """

    def get_provider_split_info(self, splitinfo_provider, federated_best_splitinfo_provider):

        final_splitinfos = []
        for i in range(len(splitinfo_provider)):
            best_idx, best_gain = federated_best_splitinfo_provider[i]
            if best_idx != -1:
                LOGGER.debug('sitename is {}, self.sitename is {}'
                             .format(splitinfo_provider[i][best_idx].sitename, self.sitename))
                assert splitinfo_provider[i][best_idx].sitename == self.sitename
                splitinfo = splitinfo_provider[i][best_idx]
                splitinfo.best_fid = splitinfo.best_fid
                assert splitinfo.best_fid is not None
                splitinfo.best_bid = splitinfo.best_bid
                splitinfo.missing_dir = splitinfo.missing_dir
                splitinfo.gain = best_gain
            else:
                splitinfo = SplitInfo(sitename=self.sitename, best_fid=-1, best_bid=-1, gain=best_gain)

            final_splitinfos.append(splitinfo)

        return final_splitinfos

    def compute_best_splits_with_node_plan(self, tree_action, target_provider_id,
                                           node_map: dict, dep: int, batch_idx: int,
                                           mode=consts.LAYERED_TREE):

        LOGGER.debug('node plan at dep {} is {}'.format(dep, (tree_action, target_provider_id)))

        if tree_action == plan.tree_actions['provider_only'] and target_provider_id == self.self_provider_id:

            data = self.data_bin_with_position
            if self.run_sparse_opt:
                data = self.data_bin_dense_with_position

            acc_histograms = self.get_local_histograms(dep, data, self.grad_and_hess,
                                                       None, self.cur_split_nodes, node_map, ret='tb',
                                                       hist_sub=False, sparse_opt=self.run_sparse_opt,
                                                       bin_num=self.bin_num)

            splitinfo_provider, encrypted_splitinfo_provider = self.splitter.find_split_provider(
                histograms=acc_histograms,
                node_map=node_map,
                use_missing=self.use_missing,
                zero_as_missing=self.zero_as_missing,
                valid_features=self.valid_features,
                sitename=self.sitename
            )

            self.sync_encrypted_splitinfo_provider(encrypted_splitinfo_provider, dep, batch_idx)
            federated_best_splitinfo_provider = self.sync_federated_best_splitinfo_provider(dep, batch_idx)

            if mode == consts.LAYERED_TREE:
                LOGGER.debug('sending split info to promoter')
                self.sync_final_splitinfo_provider(splitinfo_provider, federated_best_splitinfo_provider, dep,
                                                   batch_idx)
                LOGGER.debug('computing provider splits done')

            else:
                provider_split_info = self.get_provider_split_info(splitinfo_provider,
                                                                   federated_best_splitinfo_provider)
                return provider_split_info
        else:
            LOGGER.debug('skip best split computation')
            return None

    def compute_best_splits_with_node_plan2(self, tree_action, target_provider_id,
                                            node_map: dict, dep: int, batch: int,
                                            mode=consts.LAYERED_TREE):

        if tree_action == plan.tree_actions['provider_only'] and target_provider_id == self.self_provider_id:
            data = self.data_bin_with_position
            if self.run_sparse_opt:
                data = self.data_bin_dense_with_position

            node_dispatch = self.get_computing_node_dispatch()
            node_sample_count = self.count_node_sample_num(node_dispatch, node_map)
            LOGGER.debug('sample count is {}'.format(node_sample_count))
            acc_histograms = self.get_local_histograms(dep, data, self.grad_and_hess, node_sample_count,
                                                       self.cur_split_nodes, node_map, ret='tb',
                                                       sparse_opt=self.run_sparse_opt, hist_sub=True,
                                                       bin_num=self.bin_num)

            split_info_table = self.splitter.provider_prepare_split_points(histograms=acc_histograms,
                                                                           use_missing=self.use_missing,
                                                                           valid_features=self.valid_features,
                                                                           sitename=self.sitename,
                                                                           left_missing_dir=self.missing_dir_mask_left[
                                                                               dep],
                                                                           right_missing_dir=
                                                                           self.missing_dir_mask_right[dep],
                                                                           mask_id_mapping=self.fid_bid_random_mapping,
                                                                           batch_size=self.bin_num,
                                                                           cipher_compressor=self.cipher_compressor,
                                                                           shuffle_random_seed=np.abs(
                                                                               hash((dep, batch)))
                                                                           )

            # test split info encryption
            self.transfer_inst.encrypted_splitinfo_provider.remote(split_info_table,
                                                                   role=consts.PROMOTER,
                                                                   idx=-1,
                                                                   suffix=(dep, batch))

            best_split_info = self.transfer_inst.federated_best_splitinfo_provider.get(suffix=(dep, batch), idx=0)
            unmasked_split_info = self.unmask_split_info(best_split_info, self.inverse_fid_bid_random_mapping,
                                                         self.missing_dir_mask_left[dep],
                                                         self.missing_dir_mask_right[dep])

            if mode == consts.LAYERED_TREE:
                return_split_info = self.encode_split_info(unmasked_split_info)
                self.transfer_inst.final_splitinfo_provider.remote(return_split_info,
                                                                   role=consts.PROMOTER,
                                                                   idx=-1,
                                                                   suffix=(dep, batch,))
            elif mode == consts.SKIP_TREE:
                return unmasked_split_info
        else:
            LOGGER.debug('skip provider computation')
            return None

    """
    Provider Local Tree update
    """

    def update_provider_side_tree(self, split_info, reach_max_depth):

        LOGGER.info("update tree node, splitlist length is {}, tree node queue size is {}".format(
            len(split_info), len(self.tree_node_queue)))

        new_tree_node_queue = []
        for i in range(len(self.tree_node_queue)):

            sum_grad = self.tree_node_queue[i].sum_grad
            sum_hess = self.tree_node_queue[i].sum_hess

            # when provider node can not be further split, fid/bid is set to -1
            if reach_max_depth or split_info[i].best_fid == -1:
                self.tree_node_queue[i].is_leaf = True
            else:
                self.tree_node_queue[i].left_nodeid = self.tree_node_num + 1
                self.tree_node_queue[i].right_nodeid = self.tree_node_num + 2
                self.tree_node_num += 2

                left_node = Node(id=self.tree_node_queue[i].left_nodeid,
                                 sitename=self.sitename,
                                 sum_grad=split_info[i].sum_grad,
                                 sum_hess=split_info[i].sum_hess,
                                 parent_nodeid=self.cur_split_nodes[i].id
                                 )
                right_node = Node(id=self.tree_node_queue[i].right_nodeid,
                                  sitename=self.sitename,
                                  sum_grad=sum_grad - split_info[i].sum_grad,
                                  sum_hess=sum_hess - split_info[i].sum_hess,
                                  parent_nodeid=self.cur_split_nodes[i].id
                                  )

                new_tree_node_queue.append(left_node)
                new_tree_node_queue.append(right_node)

                self.tree_node_queue[i].sitename = split_info[i].sitename

                self.tree_node_queue[i].fid = split_info[i].best_fid
                self.tree_node_queue[i].bid = split_info[i].best_bid
                self.tree_node_queue[i].missing_dir = split_info[i].missing_dir

                if self.feature_importance_type == 'split':
                    self.update_feature_importance(split_info[i], record_site_name=False)

            self.tree_.append(self.tree_node_queue[i])

        self.tree_node_queue = new_tree_node_queue

    @staticmethod
    def provider_assign_an_instance(value, tree_, bin_sparse_points, use_missing, zero_as_missing, dense_format=False):

        unleaf_state, nodeid = value[1]

        if tree_[nodeid].is_leaf is True:
            return nodeid

        fid = tree_[nodeid].fid
        bid = tree_[nodeid].bid

        if not dense_format:

            next_layer_nid = VertFastDecisionTreeProvider.go_next_layer(tree_[nodeid], value[0], use_missing,
                                                                      zero_as_missing, bin_sparse_points)

            return 1, next_layer_nid

        else:
            # this branch is for fast histogram
            # will get scipy sparse matrix if using fast histogram
            if not use_missing:
                sample_feat = value[0].features[0, fid]  # value.features is a scipy sparse matrix
                return (1, tree_[nodeid].left_nodeid) if sample_feat <= bid else (1, tree_[nodeid].right_nodeid)
            else:
                missing_dir = tree_[nodeid].missing_dir
                sample_feat = value[0].features[0, fid]
                if zero_as_missing:  # zero_as_missing and use_missing, 0 and missing value are marked as -1
                    sample_feat -= 1  # remove offset
                if sample_feat == -1:
                    return (1, tree_[nodeid].right_nodeid) if missing_dir == 1 else (1, tree_[nodeid].left_nodeid)
                else:
                    return (1, tree_[nodeid].left_nodeid) if sample_feat <= bid else (1, tree_[nodeid].right_nodeid)

    def provider_local_assign_instances_to_new_node(self):

        assign_node_method = functools.partial(self.provider_assign_an_instance,
                                               tree_=self.tree_,
                                               bin_sparse_points=self.bin_sparse_points,
                                               use_missing=self.use_missing,
                                               zero_as_missing=self.zero_as_missing,
                                               dense_format=self.run_sparse_opt
                                               )

        if not self.run_sparse_opt:
            assign_result = self.data_bin_with_position.mapValues(assign_node_method)
        else:
            assign_result = self.data_bin_dense_with_position.mapValues(assign_node_method)
        leaf = assign_result.filter(lambda key, value: isinstance(value, tuple) is False)

        if self.sample_leaf_pos is None:
            self.sample_leaf_pos = leaf
        else:
            self.sample_leaf_pos = self.sample_leaf_pos.union(leaf)

        assign_result = assign_result.subtractByKey(leaf)

        return assign_result

    """
    Federation Functions
    """

    def sync_sample_leaf_pos(self, sample_leaf_pos):
        LOGGER.debug('final sample pos sent')
        self.transfer_inst.dispatch_node_provider_result.remote(sample_leaf_pos, idx=0,
                                                                suffix=('final sample pos',), role=consts.PROMOTER)

    def sync_leaf_nodes(self):
        leaves = []
        for node in self.tree_:
            if node.is_leaf:
                leaves.append(node)
        to_send_leaves = copy.deepcopy(leaves)
        self.transfer_inst.provider_leafs.remote(to_send_leaves)

    def sync_cur_layer_nodes(self, nodes, dep):
        # self.mask_node_id(copy.deepcopy(nodes))
        self.transfer_inst.provider_cur_to_split_node_num. \
            remote(nodes, idx=0, role=consts.PROMOTER, suffix=(dep,))

    """
    Pre/Post Process
    """

    def process_leaves_info(self):

        # remove g/h info and rename leaves

        for node in self.tree_:
            node.sum_grad = None
            node.sum_hess = None
            if node.is_leaf:
                node.sitename = consts.PROMOTER

    def mask_node_id(self, nodes):
        for n in nodes:
            n.id = -1
        return nodes

    def convert_bin_to_real(self):
        LOGGER.info("convert tree node bins to real value")
        for i in range(len(self.tree_)):
            if self.tree_[i].is_leaf is True:
                continue
            if self.tree_[i].sitename == self.sitename:
                fid = self.decode("feature_idx", self.tree_[i].fid, split_maskdict=self.split_maskdict)
                bid = self.decode("feature_val", self.tree_[i].bid, self.tree_[i].id, self.split_maskdict)
                real_splitval = self.encode("feature_val", self.bin_split_points[fid][bid], self.tree_[i].id)
                self.tree_[i].bid = real_splitval

    def convert_bin_to_real2(self):
        """
        convert current bid in tree nodes to real value
        """
        for node in self.tree_:
            if not node.is_leaf:
                node.bid = self.bin_split_points[node.fid][node.bid]

    """
    Mix Mode
    """

    def sync_en_g_sum_h_sum(self):

        gh_list = self.transfer_inst.encrypted_grad_and_hess.get(idx=0, suffix='ghsum')
        g_sum, h_sum = gh_list
        return g_sum, h_sum

    def skip_mode_fit(self):

        LOGGER.info('running skip mode')

        if self.tree_type == plan.tree_type_dict['promoter_feat_only']:
            LOGGER.debug('this tree uses promoter feature only, skip')
            return
        if self.self_provider_id != str(self.target_provider_id):
            LOGGER.debug('not selected provider, skip')
            return

        LOGGER.debug('use local provider feature to build tree')

        self.init_compressor_and_sync_gh()
        root_sum_grad, root_sum_hess = self.sync_en_g_sum_h_sum()
        self.node_dispatch = self.dispatch_all_node_to_root(self.data_bin, root_node_id=0)  # root node id is 0

        self.tree_node_queue = [Node(id=0, sitename=self.sitename, sum_grad=root_sum_grad, sum_hess=root_sum_hess, )]

        for dep in range(self.max_depth):

            tree_action, layer_target_provider_id = self.get_node_plan(dep)
            # for split point masking
            self.generate_split_point_masking_variable(dep)

            self.sync_cur_layer_nodes(self.tree_node_queue, dep)
            if len(self.tree_node_queue) == 0:
                break

            self.update_instances_node_positions()
            batch = 0
            split_info = []
            for i in range(0, len(self.tree_node_queue), self.max_split_nodes):
                self.cur_split_nodes = self.tree_node_queue[i: i + self.max_split_nodes]

                if self.new_ver:
                    batch_split_info = self.compute_best_splits_with_node_plan2(tree_action,
                                                                                str(layer_target_provider_id),
                                                                                node_map=self.get_node_map(
                                                                                    self.cur_split_nodes),
                                                                                dep=dep, batch=batch,
                                                                                mode=consts.SKIP_TREE)
                else:
                    batch_split_info = self.compute_best_splits_with_node_plan(tree_action,
                                                                               str(layer_target_provider_id),
                                                                               node_map=self.get_node_map(
                                                                                   self.cur_split_nodes),
                                                                               dep=dep, batch_idx=batch,
                                                                               mode=consts.SKIP_TREE)
                batch += 1
                split_info.extend(batch_split_info)

            self.update_provider_side_tree(split_info, reach_max_depth=False)
            self.node_dispatch = self.provider_local_assign_instances_to_new_node()

        if self.tree_node_queue:
            self.update_provider_side_tree([], reach_max_depth=True)  # mark final layer nodes as leaves
            self.update_instances_node_positions()  # update instances position
            self.provider_local_assign_instances_to_new_node()  # assign instances to final leaves

        self.convert_bin_to_real2()  # convert bin num to val
        self.sync_leaf_nodes()  # send leaf nodes to promoter
        self.process_leaves_info()  # remove encrypted g/h
        self.sync_sample_leaf_pos(self.sample_leaf_pos)  # sync sample final leaf positions

    @staticmethod
    def provider_local_traverse_tree(data_inst, tree_node, use_missing=True, zero_as_missing=True):

        nid = 0  # root node id
        while True:

            if tree_node[nid].is_leaf:
                return nid

            cur_node = tree_node[nid]
            fid, bid = cur_node.fid, cur_node.bid
            missing_dir = cur_node.missing_dir

            if use_missing and zero_as_missing:

                if data_inst.features.get_data(fid) == NoneType() or data_inst.features.get_data(fid, None) is None:

                    nid = tree_node[nid].right_nodeid if missing_dir == 1 else tree_node[nid].left_nodeid

                elif data_inst.features.get_data(fid) <= bid:
                    nid = tree_node[nid].left_nodeid
                else:
                    nid = tree_node[nid].right_nodeid

            elif data_inst.features.get_data(fid) == NoneType():

                nid = tree_node[nid].right_nodeid if missing_dir == 1 else tree_node[nid].left_nodeid

            elif data_inst.features.get_data(fid, 0) <= bid:
                nid = tree_node[nid].left_nodeid
            else:
                nid = tree_node[nid].right_nodeid

    def skip_mode_predict(self, data_inst):

        LOGGER.debug('running skip mode predict')

        if not self.use_promoter_feat_when_predict and str(self.target_provider_id) == self.self_provider_id:
            LOGGER.info('predicting using local nodes')
            traverse_tree = functools.partial(self.provider_local_traverse_tree,
                                              tree_node=self.tree_,
                                              use_missing=self.use_missing,
                                              zero_as_missing=self.zero_as_missing, )
            leaf_nodes = data_inst.mapValues(traverse_tree, need_send=True)
            LOGGER.debug('leaf nodes count is {}'.format(leaf_nodes.count()))
            self.sync_sample_leaf_pos(leaf_nodes)
        else:
            LOGGER.info('this tree belongs to other parties, skip prediction')

        # sync status
        _ = self.transfer_inst.sync_flag.get(idx=0)

    """
    Layered Mode
    """

    def layered_mode_fit(self):

        LOGGER.info('running layered mode')

        self.initialize_node_plan()

        self.init_compressor_and_sync_gh()

        for dep in range(self.max_depth):

            tree_action, layer_target_provider_id = self.get_node_plan(dep)
            # for split point masking
            self.generate_split_point_masking_variable(dep)

            self.sync_tree_node_queue(dep)
            if len(self.tree_node_queue) == 0:
                break

            if self.self_provider_id == str(layer_target_provider_id):
                self.node_dispatch = self.sync_node_positions(dep)
                self.update_instances_node_positions()

            batch = 0
            for i in range(0, len(self.tree_node_queue), self.max_split_nodes):
                self.cur_split_nodes = self.tree_node_queue[i: i + self.max_split_nodes]

                if self.new_ver:
                    self.compute_best_splits_with_node_plan2(tree_action, str(layer_target_provider_id),
                                                             node_map=self.get_node_map(self.cur_split_nodes),
                                                             dep=dep, batch=batch,
                                                             mode=consts.LAYERED_TREE)
                else:
                    self.compute_best_splits_with_node_plan(tree_action, str(layer_target_provider_id),
                                                            node_map=self.get_node_map(self.cur_split_nodes),
                                                            dep=dep, batch_idx=batch, )
                batch += 1

            if str(layer_target_provider_id) == self.self_provider_id:
                dispatch_node_provider = self.sync_dispatch_node_provider(dep)
                self.find_dispatch(dispatch_node_provider, dep)

        self.sync_tree()
        self.convert_bin_to_real()

    """
    Fit & Predict
    """

    def fit(self):

        LOGGER.info("begin to fit fast provider decision tree")

        self.initialize_node_plan()

        if self.tree_type == plan.tree_type_dict['promoter_feat_only'] or \
                self.tree_type == plan.tree_type_dict['provider_feat_only']:
            self.skip_mode_fit()
        else:
            self.layered_mode_fit()

        LOGGER.info("end to fit provider decision tree")

    def predict(self, data_inst):

        LOGGER.info("start to predict!")

        if self.tree_type == plan.tree_type_dict['promoter_feat_only'] or \
                self.tree_type == plan.tree_type_dict['provider_feat_only']:

            self.skip_mode_predict(data_inst)

        else:
            LOGGER.debug('running layered mode predict')
            super(VertFastDecisionTreeProvider, self).predict(data_inst)

        LOGGER.info('predict done')

    def get_model_meta(self):
        return super(VertFastDecisionTreeProvider, self).get_model_meta()

    def get_model_param(self):
        return super(VertFastDecisionTreeProvider, self).get_model_param()
