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



import copy
import functools

from common.python.utils import log_utils
from kernel.components.boosting import DecisionTree
from kernel.components.boosting import Node
from kernel.components.boosting.core.subsample import goss_sampling
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import CriterionMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.transfer.variables.transfer_class.vert_dp_decision_tree_transfer_variable import \
    VertDPDecisionTreeTransferVariable
from kernel.utils import consts
from kernel.utils.data_util import NoneType
from kernel.utils.io_check import assert_io_num_rows_equal
LOGGER = log_utils.get_logger()


class VertDPDecisionTreePromoter(DecisionTree):
    def __init__(self, tree_param):
        LOGGER.info("vert decision tree promoter init!")
        super(VertDPDecisionTreePromoter, self).__init__(tree_param)

        self.grad_and_hess = None
        self.data_bin_with_node_dispatch = None
        self.node_dispatch = None
        self.infos = None
        self.best_splitinfo_promoter = None
        self.tree_node_queue = None
        self.cur_split_nodes = None
        self.tree_node_num = 0
        self.transfer_inst = VertDPDecisionTreeTransferVariable()
        self.predict_weights = None
        self.provider_member_idlist = []
        self.sitename = consts.PROMOTER
        # self.feature_importance = {}

        # goss subsample
        self.run_goss = False
        self.top_rate, self.other_rate = 0.2, 0.1  # goss sampling rate

        self.max_sample_weight = 1
        self.feature_sitenames = []

    def report_init_status(self):

        LOGGER.info('reporting initialization status')
        if self.run_goss:
            LOGGER.info('run goss is {}, top rate is {}, other rate is {}'.format(self.run_goss, self.top_rate,
                                                                                  self.other_rate))
            LOGGER.info('sampled g_h count is {}, total sample num is {}'.format(self.grad_and_hess.count(),
                                                                                 self.data_bin.count()))

    def init(self, flowid, runtime_idx, data_bin, bin_split_points, bin_sparse_points, valid_features,
             grad_and_hess,
             task_type,
             provider_member_idlist,
             feature_sitenames,
             goss_subsample=False,
             top_rate=0.1,
             other_rate=0.2,
             max_sample_weight=1):

        super(VertDPDecisionTreePromoter, self).init_variables(flowid, runtime_idx, data_bin, bin_split_points,
                                                             bin_sparse_points, valid_features)

        self.check_max_split_nodes()

        self.grad_and_hess = grad_and_hess
        self.provider_member_idlist = provider_member_idlist

        self.run_goss = goss_subsample
        self.top_rate = top_rate
        self.other_rate = other_rate

        self.max_sample_weight = max_sample_weight
        self.task_type = task_type
        self.feature_sitenames = feature_sitenames

        if self.run_goss:
            self.goss_sampling()
            self.max_sample_weight = self.max_sample_weight * ((1 - top_rate) / other_rate)

        self.report_init_status()

    def goss_sampling(self, ):
        new_g_h = goss_sampling(self.grad_and_hess, self.top_rate, self.other_rate)
        self.grad_and_hess = new_g_h

    def set_flowid(self, flowid=0):
        LOGGER.info("set flowid, flowid is {}".format(flowid))
        self.transfer_inst.set_flowid(flowid)

    def set_provider_member_idlist(self, provider_member_idlist):
        self.provider_member_idlist = provider_member_idlist

    def set_inputinfo(self, data_bin=None, grad_and_hess=None, bin_split_points=None, bin_sparse_points=None):
        LOGGER.info("set input info")
        self.data_bin = data_bin
        self.grad_and_hess = grad_and_hess
        self.bin_split_points = bin_split_points
        self.bin_sparse_points = bin_sparse_points

    def set_valid_features(self, valid_features=None):
        LOGGER.info("set valid features")
        self.valid_features = valid_features

    def get_grad_hess_sum(self, grad_and_hess_table):
        LOGGER.info("calculate the sum of grad and hess")
        grad, hess = grad_and_hess_table.reduce(
            lambda value1, value2: (value1[0] + value2[0], value1[1] + value2[1]))
        return grad, hess

    def dispatch_all_node_to_root(self, root_id=0):
        LOGGER.info("dispatch all node to root")
        self.node_dispatch = self.data_bin.mapValues(lambda data_inst: (1, root_id), need_send=True)

    def update_instances_node_positions(self):
        self.data_bin_with_node_dispatch = self.data_bin.join(self.node_dispatch,
                                                              lambda data_inst, dispatch_info: (
                                                                  data_inst, dispatch_info))

    def update_tree_node_queue(self, split_info, reach_max_depth):

        LOGGER.info("update tree node, splitlist length is {}, tree node queue size is".format(
            len(split_info), len(self.tree_node_queue)))
        new_tree_node_queue = []
        for i in range(len(self.tree_node_queue)):
            sum_grad = self.tree_node_queue[i].sum_grad
            sum_hess = self.tree_node_queue[i].sum_hess
            if reach_max_depth or split_info[i].gain <= \
                    self.min_impurity_split + consts.FLOAT_ZERO:  # if reach max_depth, only convert nodes to leaves
                self.tree_node_queue[i].is_leaf = True
            else:
                pid = self.tree_node_queue[i].id
                self.tree_node_queue[i].left_nodeid = self.tree_node_num + 1
                self.tree_node_queue[i].right_nodeid = self.tree_node_num + 2
                self.tree_node_num += 2

                left_node = Node(id=self.tree_node_queue[i].left_nodeid,
                                 sitename=self.sitename,
                                 sum_grad=split_info[i].sum_grad,
                                 sum_hess=split_info[i].sum_hess,
                                 weight=self.splitter.node_weight(split_info[i].sum_grad, split_info[i].sum_hess),
                                 is_left_node=True,
                                 parent_nodeid=pid)

                right_node = Node(id=self.tree_node_queue[i].right_nodeid,
                                  sitename=self.sitename,
                                  sum_grad=sum_grad - split_info[i].sum_grad,
                                  sum_hess=sum_hess - split_info[i].sum_hess,
                                  weight=self.splitter.node_weight(
                                      sum_grad - split_info[i].sum_grad,
                                      sum_hess - split_info[i].sum_hess),
                                  is_left_node=False,
                                  parent_nodeid=pid)

                LOGGER.debug('cwj node {}'.format(left_node))
                LOGGER.debug('cwj node {}'.format(right_node))
                LOGGER.debug('cwj gain {}'.format(split_info[i].gain))

                new_tree_node_queue.append(left_node)
                new_tree_node_queue.append(right_node)

                self.tree_node_queue[i].sitename = split_info[i].sitename
                self.tree_node_queue[i].fid = split_info[i].best_fid
                self.tree_node_queue[i].bid = split_info[i].best_bid
                self.tree_node_queue[i].missing_dir = split_info[i].missing_dir

                self.update_feature_importance(split_info[i])

            self.tree_.append(self.tree_node_queue[i])

        self.tree_node_queue = new_tree_node_queue

    @staticmethod
    def go_next_layer(node, data_inst, use_missing, zero_as_missing, bin_sparse_point=None,
                      return_node_id=True):

        fid, bid = node.fid, node.bid

        missing_dir = node.missing_dir
        zero_val = 0 if bin_sparse_point is None else bin_sparse_point[fid]
        go_left = DecisionTree.make_decision(data_inst, fid, bid, missing_dir, use_missing, zero_as_missing, zero_val)

        if not return_node_id:
            return go_left

        if go_left:
            return node.left_nodeid
        else:
            return node.right_nodeid

    @staticmethod
    def dispatch_node(value, tree_=None, bin_sparse_points=None,
                           use_missing=False, zero_as_missing=False):

        unleaf_state, nodeid = value[1]

        if tree_[nodeid].is_leaf is True:
            return tree_[nodeid].id
        else:
            next_layer_nid = VertDPDecisionTreePromoter.go_next_layer(tree_[nodeid], value[0], use_missing,
                                                                   zero_as_missing,bin_sparse_points)
            return 1, next_layer_nid


    def redispatch_node(self, dep, reach_max_depth=False):

        LOGGER.info("redispatch node of depth {}".format(dep))

        tree_nodes = copy.deepcopy(self.tree_)
        for node in tree_nodes:
            if node.fid is not None:
                index=0
                for feature_num, sitename in self.feature_sitenames:
                    if node.sitename==sitename:
                        node.fid += index
                        break
                    index += feature_num

        dispatch_node_method = functools.partial(self.dispatch_node,
                                                 tree_=tree_nodes,
                                                 bin_sparse_points=self.bin_sparse_points,
                                                 use_missing=self.use_missing,
                                                 zero_as_missing=self.zero_as_missing)


        dispatch_promoter_result = self.data_bin_with_node_dispatch.mapValues(dispatch_node_method)
        LOGGER.info("remask dispatch node result of depth {}".format(dep))

        leaf = dispatch_promoter_result.filter(lambda key, value: isinstance(value, tuple) is False)

        if self.sample_leaf_pos is None:
            self.sample_leaf_pos = leaf
        else:
            self.sample_leaf_pos = self.sample_leaf_pos.union(leaf)

        if reach_max_depth:  # if reach max_depth only update weight samples
            return

        self.node_dispatch = dispatch_promoter_result.subtractByKey(leaf)

    def sync_tree(self):
        LOGGER.info("sync tree to provider")

        self.transfer_inst.tree.remote(self.tree_,
                                       role=consts.PROVIDER,
                                       idx=-1)
        """
        federation.remote(obj=self.tree_,
                          name=self.transfer_inst.tree.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.tree),
                          role=consts.PROVIDER,
                          idx=-1)
        """

    def convert_bin_to_real(self):
        """
        convert current bid in tree nodes to real value
        """
        LOGGER.info("convert tree node bins to real value")
        for node in self.tree_:
            if not node.is_leaf and node.sitename == self.sitename:
                node.bid = self.bin_split_points[node.fid][node.bid]

    def initialize_root_node(self, ):
        root_sum_grad, root_sum_hess = self.get_grad_hess_sum(self.grad_and_hess)
        root_node = Node(id=0, sitename=self.sitename, sum_grad=root_sum_grad, sum_hess=root_sum_hess,
                         weight=self.splitter.node_weight(root_sum_grad, root_sum_hess))
        return root_node

    def fit(self):
        LOGGER.info("begin to fit dp promoter decision tree")

        root_node = self.initialize_root_node()

        self.tree_node_queue = [root_node]

        self.dispatch_all_node_to_root()

        for dep in range(self.max_depth):
            LOGGER.info("start to fit depth {}, tree node queue size is {}".format(dep, len(self.tree_node_queue)))

            if len(self.tree_node_queue) == 0:
                break

            self.update_instances_node_positions()

            split_info = []
            for batch_idx, i in enumerate(range(0, len(self.tree_node_queue), self.max_split_nodes)):
                self.cur_split_nodes = self.tree_node_queue[i: i + self.max_split_nodes]

                node_map = self.get_node_map(self.cur_split_nodes)

                cur_splitinfos = self.compute_best_splits(node_map, dep)

                split_info.extend(cur_splitinfos)

            max_depth_reach = True if dep + 1 == self.max_depth else False
            self.update_tree_node_queue(split_info, max_depth_reach)
            self.redispatch_node(dep)

        self.convert_bin_to_real()
        self.round_leaf_val()
        self.sync_tree()
        self.sample_weights_post_process()
        LOGGER.info("tree node num is %d" % len(self.tree_))
        LOGGER.info("end to fit promoter decision tree")

    def compute_best_splits(self, node_map, dep):

        acc_histograms = self.get_local_histograms(dep, self.data_bin_with_node_dispatch, self.grad_and_hess,
                                                   None, self.cur_split_nodes, node_map, ret='tensor',
                                                   hist_sub=False)

        best_split_info_promoter = self.splitter.find_split(acc_histograms, self.valid_features,
                                                            self.data_bin._partitions, self.sitename,
                                                            self.use_missing, self.zero_as_missing)


        for split_info in best_split_info_promoter:
            fid = split_info.best_fid
            if fid is not None:
                index = 0
                for feature_num ,sitename in self.feature_sitenames:
                    if fid < feature_num:
                        split_info.sitename = sitename
                        split_info.best_fid -= index
                        break
                    index += feature_num

        return best_split_info_promoter

    @staticmethod
    def traverse_tree(predict_state, data_inst, tree_=None,sitename=consts.PROMOTER,
                      use_missing=None, zero_as_missing=None,  return_leaf_id=False):

        nid, tag = predict_state

        while tree_[nid].sitename == sitename:
            if tree_[nid].is_leaf is True:
                return tree_[nid].weight if not return_leaf_id else nid

            fid = tree_[nid].fid
            bid = tree_[nid].bid
            missing_dir = tree_[nid].missing_dir

            if use_missing and zero_as_missing:
                if data_inst.features.get_data(fid) == NoneType() or data_inst.features.get_data(fid, None) is None:
                    if missing_dir == 1:
                        nid = tree_[nid].right_nodeid
                    else:
                        nid = tree_[nid].left_nodeid
                elif data_inst.features.get_data(fid) <= bid + consts.FLOAT_ZERO:
                    nid = tree_[nid].left_nodeid
                else:
                    nid = tree_[nid].right_nodeid
            elif data_inst.features.get_data(fid) == NoneType():
                if missing_dir == 1:
                    nid = tree_[nid].right_nodeid
                else:
                    nid = tree_[nid].left_nodeid
            elif data_inst.features.get_data(fid, 0) <= bid + consts.FLOAT_ZERO:
                nid = tree_[nid].left_nodeid
            else:
                nid = tree_[nid].right_nodeid

        return nid, 1

    def sync_predict_finish_tag(self, finish_tag, send_times):
        LOGGER.info("send the {}-th predict finish tag {} to provider".format(finish_tag, send_times))

        self.transfer_inst.predict_finish_tag.remote(finish_tag,
                                                     role=consts.PROVIDER,
                                                     idx=-1,
                                                     suffix=(send_times,))
        """
        federation.remote(obj=finish_tag,
                          name=self.transfer_inst.predict_finish_tag.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.predict_finish_tag, send_times),
                          role=consts.PROVIDER,
                          idx=-1)
        """

    def sync_predict_data(self, predict_data, send_times):
        LOGGER.info("send predict data to provider, sending times is {}".format(send_times))
        self.transfer_inst.predict_data.remote(predict_data,
                                               role=consts.PROVIDER,
                                               idx=-1,
                                               suffix=(send_times,))

        """
        federation.remote(obj=predict_data,
                          name=self.transfer_inst.predict_data.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.predict_data, send_times),
                          role=consts.PROVIDER,
                          idx=-1)
        """

    def sync_data_predicted_by_provider(self, send_times):
        LOGGER.info("get predicted data by provider, recv times is {}".format(send_times))
        predict_data = self.transfer_inst.predict_data_by_provider.get(idx=-1,
                                                                       suffix=(send_times,))
        """
        predict_data = federation.get(name=self.transfer_inst.predict_data_by_provider.name,
                                      tag=self.transfer_inst.generate_transferid(
                                          self.transfer_inst.predict_data_by_provider, send_times),
                                      idx=-1)
        """
        return predict_data

    @assert_io_num_rows_equal
    def predict(self, data_inst):
        LOGGER.info("start to predict!")
        predict_data = data_inst.mapValues(lambda data_inst: (0, 1))  # list(data_index,(0,1))
        site_provider_send_times = 0
        predict_result = None

        while True:
            traverse_tree = functools.partial(self.traverse_tree,
                                              tree_=self.tree_,
                                              sitename=self.sitename,
                                              use_missing=self.use_missing,
                                              zero_as_missing=self.zero_as_missing)
            predict_data = predict_data.join(data_inst,
                                             traverse_tree)  # list[(data_index,(nid,1)) or (data_index,weight)]
            predict_leaf = predict_data.filter(
                lambda key, value: isinstance(value, tuple) is False)  # list(data_index,weight)
            if predict_result is None:
                predict_result = predict_leaf
            else:
                predict_result = predict_result.union(predict_leaf)

            predict_data = predict_data.subtractByKey(predict_leaf, need_send=True)

            unleaf_node_count = predict_data.count()

            if unleaf_node_count == 0:
                self.sync_predict_finish_tag(True, site_provider_send_times)
                break

            self.sync_predict_finish_tag(False, site_provider_send_times)
            self.sync_predict_data(predict_data, site_provider_send_times)

            predict_data_provider = self.sync_data_predicted_by_provider(
                site_provider_send_times)  # list[(data_index,(nid,0))
            for i in range(len(predict_data_provider)):
                predict_data = predict_data.join(predict_data_provider[i],
                                                 lambda state1_nodeid1, state2_nodeid2:
                                                 state1_nodeid1 if state1_nodeid1[
                                                                       1] == 0 else state2_nodeid2)

            site_provider_send_times += 1

        LOGGER.info("predict finish!")
        return predict_result

    def get_model_meta(self):
        model_meta = DecisionTreeModelMeta()
        model_meta.criterion_meta.CopyFrom(CriterionMeta(criterion_method=self.criterion_method,
                                                         criterion_param=self.criterion_params))

        model_meta.max_depth = self.max_depth
        model_meta.min_sample_split = self.min_sample_split
        model_meta.min_impurity_split = self.min_impurity_split
        model_meta.min_leaf_node = self.min_leaf_node
        model_meta.use_missing = self.use_missing
        model_meta.zero_as_missing = self.zero_as_missing

        return model_meta

    def set_model_meta(self, model_meta):
        self.max_depth = model_meta.max_depth
        self.min_sample_split = model_meta.min_sample_split
        self.min_impurity_split = model_meta.min_impurity_split
        self.min_leaf_node = model_meta.min_leaf_node
        self.criterion_method = model_meta.criterion_meta.criterion_method
        self.criterion_params = list(model_meta.criterion_meta.criterion_param)
        self.use_missing = model_meta.use_missing
        self.zero_as_missing = model_meta.zero_as_missing

    def get_model_param(self):
        model_param = DecisionTreeModelParam()
        for node in self.tree_:
            model_param.tree_.add(id=node.id,
                                      sitename=node.sitename,
                                      fid=node.fid,
                                      bid=node.bid,
                                      weight=node.weight,
                                      is_leaf=node.is_leaf,
                                      left_nodeid=node.left_nodeid,
                                      right_nodeid=node.right_nodeid,
                                      missing_dir=node.missing_dir)
            LOGGER.debug("missing_dir is {}, sitename is {}, is_leaf is {}".format(node.missing_dir, node.sitename,
                                                                                   node.is_leaf))

        return model_param

    def set_model_param(self, model_param):
        self.tree_ = []
        for node_param in model_param.tree_:
            _node = Node(id=node_param.id,
                         sitename=node_param.sitename,
                         fid=node_param.fid,
                         bid=node_param.bid,
                         weight=node_param.weight,
                         is_leaf=node_param.is_leaf,
                         left_nodeid=node_param.left_nodeid,
                         right_nodeid=node_param.right_nodeid,
                         missing_dir=node_param.missing_dir)

            self.tree_.append(_node)

    def get_model(self):
        model_meta = self.get_model_meta()
        model_param = self.get_model_param()

        return model_meta, model_param

    def load_model(self, model_meta=None, model_param=None):
        LOGGER.info("load tree model")
        self.set_model_meta(model_meta)
        self.set_model_param(model_param)

    def get_feature_importance(self):
        return self.feature_importance