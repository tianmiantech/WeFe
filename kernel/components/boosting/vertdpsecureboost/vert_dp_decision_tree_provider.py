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


import functools

from common.python.utils import log_utils
from kernel.components.boosting import DecisionTree
from kernel.components.boosting import Node
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.transfer.variables.transfer_class.vert_dp_decision_tree_transfer_variable import \
    VertDPDecisionTreeTransferVariable
from kernel.utils import consts
from kernel.utils.data_util import NoneType

LOGGER = log_utils.get_logger()


class VertDPDecisionTreeeProvider(DecisionTree):
    def __init__(self, tree_param):
        LOGGER.info("vert dp decision tree promoter init!")
        super(VertDPDecisionTreeeProvider, self).__init__(tree_param)

        self.data_bin_with_position = None
        self.grad_and_hess = None
        self.infos = None
        self.pubkey = None
        self.privakey = None
        self.tree_id = None
        self.encrypted_grad_and_hess = None
        self.tree_node_queue = None
        self.cur_split_nodes = None
        self.tree_node_num = 0
        self.sitename = consts.PROVIDER
        self.node_dispatch = None
        self.provider_member_idlist = []

        # goss subsample
        self.run_goss = False

        # transfer variable
        self.transfer_inst = VertDPDecisionTreeTransferVariable()

    def report_init_status(self):

        LOGGER.info('reporting initialization status')
        if self.run_goss:
            LOGGER.info('running goss')
        LOGGER.debug('bin num and feature num: {}/{}'.format(self.bin_num, self.feature_num))

    def init(self, flowid, runtime_idx, data_bin, bin_split_points, bin_sparse_points, bin_num,
             valid_features,
             goss_subsample=False):

        super(VertDPDecisionTreeeProvider, self).init_variables(flowid, runtime_idx, data_bin, bin_split_points,
                                                             bin_sparse_points, valid_features)

        self.check_max_split_nodes()
        self.run_goss = goss_subsample
        self.bin_num = bin_num
        self.feature_num = self.bin_split_points.shape[0]

        self.report_init_status()

    def set_flowid(self, flowid=0):
        LOGGER.info("set flowid, flowid is {}".format(flowid))
        self.transfer_inst.set_flowid(flowid)

    def set_provider_member_idlist(self, provider_member_idlist):
        self.provider_member_idlist = provider_member_idlist

    @staticmethod
    def dispatch_node(value1, value2, sitename=None, bin_sparse_points=None,
                      use_missing=False, zero_as_missing=False,):

        unleaf_state, fid, bid, node_sitename, nodeid, left_nodeid, right_nodeid , missing_dir= value1
        if node_sitename != sitename:
            return value1

        if not use_missing:
            if value2.features.get_data(fid, bin_sparse_points[fid]) <= bid:
                return unleaf_state, left_nodeid
            else:
                return unleaf_state, right_nodeid
        else:

            missing_val = False
            if zero_as_missing:
                if value2.features.get_data(fid, None) is None or \
                        value2.features.get_data(fid) == NoneType():
                    missing_val = True
            elif use_missing and value2.features.get_data(fid) == NoneType():
                missing_val = True

            if missing_val:
                if missing_dir == 1:
                    return unleaf_state, right_nodeid
                else:
                    return unleaf_state, left_nodeid
            else:
                if value2.features.get_data(fid, bin_sparse_points[fid]) <= bid:
                    return unleaf_state, left_nodeid
                else:
                    return unleaf_state, right_nodeid

    def sync_tree(self):
        LOGGER.info("sync tree from promoter")
        self.tree_ = self.transfer_inst.tree.get(idx=0)
        """
        self.tree_ = federation.get(name=self.transfer_inst.tree.name,
                                    tag=self.transfer_inst.generate_transferid(self.transfer_inst.tree),
                                    idx=0)
        """


    def convert_bin_to_real(self):
        """
        convert current bid in tree nodes to real value
        """
        for node in self.tree_:
            if not node.is_leaf and node.sitename == self.sitename:
                node.bid = self.bin_split_points[node.fid][node.bid]

    @staticmethod
    def traverse_tree(predict_state, data_inst, tree_=None, sitename=consts.PROVIDER,
                      use_missing=False, zero_as_missing=False):

        nid, _ = predict_state
        if tree_[nid].sitename != sitename:
            return predict_state

        while tree_[nid].sitename == sitename:
            fid = tree_[nid].fid
            bid = tree_[nid].bid
            missing_dir = tree_[nid].missing_dir

            if use_missing and zero_as_missing:
                if data_inst.features.get_data(fid) == NoneType() or data_inst.features.get_data(fid, None) is None:
                    if missing_dir == 1:
                        nid = tree_[nid].right_nodeid
                    else:
                        nid = tree_[nid].left_nodeid
                elif data_inst.features.get_data(fid) <= bid:
                    nid = tree_[nid].left_nodeid
                else:
                    nid = tree_[nid].right_nodeid
            elif data_inst.features.get_data(fid) == NoneType():
                if missing_dir == 1:
                    nid = tree_[nid].right_nodeid
                else:
                    nid = tree_[nid].left_nodeid
            elif data_inst.features.get_data(fid, 0) <= bid:
                nid = tree_[nid].left_nodeid
            else:
                nid = tree_[nid].right_nodeid

        return nid, 0

    def sync_predict_finish_tag(self, recv_times):
        LOGGER.info("get the {}-th predict finish tag from promoter".format(recv_times))
        finish_tag = self.transfer_inst.predict_finish_tag.get(idx=0,
                                                               suffix=(recv_times,))
        """
        finish_tag = federation.get(name=self.transfer_inst.predict_finish_tag.name,
                                    tag=self.transfer_inst.generate_transferid(self.transfer_inst.predict_finish_tag,
                                                                               recv_times),
                                    idx=0)
        """

        return finish_tag

    def sync_predict_data(self, recv_times):
        LOGGER.info("srecv predict data to provider, recv times is {}".format(recv_times))
        predict_data = self.transfer_inst.predict_data.get(idx=0,
                                                           suffix=(recv_times,))
        """
        predict_data = federation.get(name=self.transfer_inst.predict_data.name,
                                      tag=self.transfer_inst.generate_transferid(self.transfer_inst.predict_data,
                                                                                 recv_times),
                                      idx=0)
        """

        return predict_data

    def sync_data_predicted_by_provider(self, predict_data, send_times):
        LOGGER.info("send predicted data by provider, send times is {}".format(send_times))

        self.transfer_inst.predict_data_by_provider.remote(predict_data,
                                                           role=consts.PROMOTER,
                                                           idx=0,
                                                           suffix=(send_times,))
        """
        federation.remote(obj=predict_data,
                          name=self.transfer_inst.predict_data_by_provider.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.predict_data_by_provider,
                                                                     send_times),
                          role=consts.PROMOTER,
                          idx=0)
        """

    def fit(self):
        LOGGER.info("begin to fit dp provider decision tree")

        self.sync_tree()
        self.convert_bin_to_real()

        LOGGER.info("end to fit provider decision tree")

    def predict(self, data_inst):
        LOGGER.info("start to predict!")
        site_promoter_send_times = 0
        while True:
            finish_tag = self.sync_predict_finish_tag(site_promoter_send_times)
            if finish_tag is True:
                break

            predict_data = self.sync_predict_data(site_promoter_send_times)

            traverse_tree = functools.partial(self.traverse_tree,
                                              tree_=self.tree_,
                                              sitename=self.sitename,
                                              use_missing=self.use_missing,
                                              zero_as_missing=self.zero_as_missing)
            predict_data = predict_data.join(data_inst, traverse_tree, need_send=True)

            self.sync_data_predicted_by_provider(predict_data, site_promoter_send_times)

            site_promoter_send_times += 1

        LOGGER.info("predict finish!")

    def get_model_meta(self):
        model_meta = DecisionTreeModelMeta()

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
