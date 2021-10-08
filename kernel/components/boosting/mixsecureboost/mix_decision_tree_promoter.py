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


import copy
import functools
from typing import List

from common.python import session
from common.python.utils import log_utils
from kernel.components.boosting import DecisionTree
from kernel.components.boosting import DecisionTreeClientAggregator
from kernel.components.boosting import HistogramBag
from kernel.components.boosting import Node
from kernel.components.boosting import SplitInfo
from kernel.components.boosting.core.subsample import goss_sampling
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import CriterionMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.security.encrypt import PaillierEncrypt, IterativeAffineEncrypt
from kernel.transfer.variables.transfer_class.mix_decision_tree_transfer_variable import MixDecisionTreeTransferVariable
from kernel.utils import consts
from kernel.utils.data_util import NoneType
from kernel.utils.io_check import assert_io_num_rows_equal

LOGGER = log_utils.get_logger()


class MixDecisionTreePromoter(DecisionTree):
    def __init__(self, tree_param):
        LOGGER.info("Mix decision tree promoter init!")
        super(MixDecisionTreePromoter, self).__init__(tree_param)

        self.grad_and_hess = None
        self.data_bin_with_node_dispatch = None
        self.node_dispatch = None
        self.infos = None
        self.encrypter = None
        self.encrypted_mode_calculator = None
        self.best_splitinfo_promoter = None
        self.tree_node_queue = None
        self.cur_split_nodes = None
        self.tree_node_num = 0
        self.split_maskdict = {}
        self.missing_dir_maskdict = {}
        self.transfer_inst = MixDecisionTreeTransferVariable()
        self.predict_weights = None
        self.provider_member_idlist = []
        self.sitename = consts.PROMOTER
        # self.feature_importance = {}

        self.complete_secure_tree = False
        self.compressor = None
        # goss subsample
        self.run_goss = False
        self.top_rate, self.other_rate = 0.2, 0.1  # goss sampling rate

        # cipher compressing
        self.cipher_encoder = None
        self.cipher_decompressor = None
        self.key_length = None
        self.round_decimal = 7
        self.max_sample_weight = 1

        # code version control
        self.new_ver = True
        self.epoch_idx = None
        self.tree_idx = None
        self.provider_sitename_list = []

    def report_init_status(self):

        LOGGER.info('reporting initialization status')
        LOGGER.info('using new version code {}'.format(self.new_ver))
        if self.complete_secure_tree:
            LOGGER.info('running complete secure')
        LOGGER.info('updated max sample weight is {}'.format(self.max_sample_weight))

        if self.deterministic:
            LOGGER.info('running on deterministic mode')

    def init(self, flowid, runtime_idx, data_bin, bin_split_points, bin_sparse_points, valid_features,
             grad_and_hess,
             encrypter, encrypted_mode_calculator,
             provider_member_idlist,
             epoch_idx,
             tree_idx,
             complete_secure=False,
             encrypt_key_length=None,
             max_sample_weight=1,
             mode="train"
             ):

        super(MixDecisionTreePromoter, self).init_variables(flowid, runtime_idx, data_bin, bin_split_points,
                                                            bin_sparse_points, valid_features)

        self.check_max_split_nodes()

        self.grad_and_hess = grad_and_hess
        self.encrypter = encrypter
        self.encrypted_mode_calculator = encrypted_mode_calculator
        self.complete_secure_tree = complete_secure
        self.provider_member_idlist = provider_member_idlist

        self.key_length = encrypt_key_length
        self.max_sample_weight = max_sample_weight

        self.epoch_idx = epoch_idx
        self.tree_idx = tree_idx

        # secure aggregator, class SecureBoostClientAggregator
        if mode == 'train':
            self.aggregator = DecisionTreeClientAggregator(verbose=False)
        else:
            self.aggregator = None

        self.report_init_status()

    def goss_sampling(self, ):
        new_g_h = goss_sampling(self.grad_and_hess, self.top_rate, self.other_rate)
        self.grad_and_hess = new_g_h

    def get_encrypt_type(self):

        if type(self.encrypter) == PaillierEncrypt:
            return consts.PAILLIER
        elif type(self.encrypter) == IterativeAffineEncrypt:
            return consts.ITERATIVEAFFINE
        else:
            raise ValueError('unknown encrypter type: {}'.format(type(self.encrypter)))

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

    def set_encrypter(self, encrypter):
        LOGGER.info("set encrypter")
        self.encrypter = encrypter

    def set_encrypted_mode_calculator(self, encrypted_mode_calculator):
        self.encrypted_mode_calculator = encrypted_mode_calculator

    def encrypt(self, val):
        return self.encrypter.encrypt(val)

    def decrypt(self, val):
        return self.encrypter.decrypt(val)

    def encode(self, etype="feature_idx", val=None, nid=None):
        if etype == "feature_idx":
            return val

        if etype == "feature_val":
            self.split_maskdict[nid] = val
            return None

        if etype == "missing_dir":
            self.missing_dir_maskdict[nid] = val
            return None

        raise TypeError("encode type %s is not support!" % (str(etype)))

    @staticmethod
    def decode(dtype="feature_idx", val=None, nid=None, split_maskdict=None, missing_dir_maskdict=None):
        if dtype == "feature_idx":
            return val

        if dtype == "feature_val":
            if nid in split_maskdict:
                return split_maskdict[nid]
            else:
                raise ValueError("decode val %s cause error, can't reconize it!" % (str(val)))

        if dtype == "missing_dir":
            if nid in missing_dir_maskdict:
                return missing_dir_maskdict[nid]
            else:
                raise ValueError("decode val %s cause error, can't reconize it!" % (str(val)))

        return TypeError("decode type %s is not support!" % (str(dtype)))

    def set_valid_features(self, valid_features=None):
        LOGGER.info("set valid features")
        self.valid_features = valid_features

    def process_and_sync_grad_and_hess(self, idx=-1):

        LOGGER.info('sedding g/h to provider')
        en_grad_hess = self.encrypted_mode_calculator.encrypt(self.grad_and_hess)

        self.transfer_inst.encrypted_grad_and_hess.remote(en_grad_hess,
                                                          role=consts.PROVIDER,
                                                          idx=idx)

        """
        federation.remote(obj=encrypted_grad_and_hess,
                          name=self.transfer_inst.encrypted_grad_and_hess.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.encrypted_grad_and_hess),
                          role=consts.PROVIDER,
                          idx=-1)
        """

    def encrypt_grad_and_hess(self):
        LOGGER.info("start to encrypt grad and hess")
        encrypted_grad_and_hess = self.encrypted_mode_calculator.encrypt(self.grad_and_hess)
        return encrypted_grad_and_hess

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

    def sync_tree_node_queue(self, tree_node_queue, dep=-1, idx=-1):
        LOGGER.info("send tree node queue of depth {}".format(dep))
        mask_tree_node_queue = copy.deepcopy(tree_node_queue)
        for i in range(len(mask_tree_node_queue)):
            mask_tree_node_queue[i] = Node(id=mask_tree_node_queue[i].id,
                                           parent_nodeid=mask_tree_node_queue[i].parent_nodeid,
                                           is_left_node=mask_tree_node_queue[i].is_left_node, )

        self.transfer_inst.tree_node_queue.remote(mask_tree_node_queue,
                                                  role=consts.PROVIDER,
                                                  idx=idx,
                                                  suffix=(dep,))

        """
        federation.remote(obj=mask_tree_node_queue,
                          name=self.transfer_inst.tree_node_queue.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.tree_node_queue, dep),
                          role=consts.PROVIDER,
                          idx=-1)
        """

    def sync_node_positions(self, dep):
        LOGGER.info("send node positions of depth {}".format(dep))
        self.transfer_inst.node_positions.remote(self.node_dispatch,
                                                 role=consts.PROVIDER,
                                                 idx=-1,
                                                 suffix=(dep,))
        """
        federation.remote(obj=self.node_dispatch,
                          name=self.transfer_inst.node_positions.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.node_positions, dep),
                          role=consts.PROVIDER,
                          idx=-1)
        """

    def sync_cur_layer_node_num(self, node_num, suffix):
        self.transfer_inst.cur_layer_node_num.remote(node_num, role=consts.ARBITER, idx=-1, suffix=suffix)

    def sync_provider_local_histograms(self, dep=-1, batch=-1, idx=-1):
        LOGGER.info("get provider local histograms of depth {}, batch {}".format(dep, batch))

        return self.transfer_inst.provider_local_histograms.get(idx=idx, suffix=(dep, batch,))

    def sync_encrypted_splitinfo_provider(self, dep=-1, batch=-1, idx=-1):
        LOGGER.info("get encrypted splitinfo of depth {}, batch {}".format(dep, batch))

        LOGGER.debug('provider idx is {}'.format(idx))
        encrypted_splitinfo_provider = self.transfer_inst.encrypted_splitinfo_provider.get(idx=idx,
                                                                                           suffix=(dep, batch,))
        ret = []
        if idx == -1:
            for obj in encrypted_splitinfo_provider:
                ret.append(obj.get_data())
        else:
            ret.append(encrypted_splitinfo_provider.get_data())

        return ret

    def sync_federated_best_splitinfo_provider(self, federated_best_splitinfo_provider, dep=-1, batch=-1, idx=-1):
        LOGGER.info("send federated best splitinfo of depth {}, batch {}".format(dep, batch))
        self.transfer_inst.federated_best_splitinfo_provider.remote(federated_best_splitinfo_provider,
                                                                    role=consts.PROVIDER,
                                                                    idx=idx,
                                                                    suffix=(dep, batch,))
        """
        federation.remote(obj=federated_best_splitinfo_provider,
                          name=self.transfer_inst.federated_best_splitinfo_provider.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.federated_best_splitinfo_provider,
                                                                     dep,
                                                                     batch),
                          role=consts.PROVIDER,
                          idx=idx)
        """

    def federated_find_split(self, dep=-1, batch=-1, idx=-1):
        LOGGER.info("federated find split of depth {}, batch {}".format(dep, batch))
        encrypted_splitinfo_provider = self.sync_encrypted_splitinfo_provider(dep, batch, idx)

        for i in range(len(encrypted_splitinfo_provider)):
            init_gain = self.min_impurity_split - consts.FLOAT_ZERO
            encrypted_init_gain = self.encrypter.encrypt(init_gain)
            best_splitinfo_provider = [[-1, encrypted_init_gain] for j in range(len(self.cur_split_nodes))]
            best_gains = [init_gain for j in range(len(self.cur_split_nodes))]
            max_nodes = max(len(encrypted_splitinfo_provider[i][j]) for j in range(len(self.cur_split_nodes)))
            for k in range(0, max_nodes, consts.MAX_FEDERATED_NODES):
                batch_splitinfo_provider = [encrypted_splitinfo[k: k + consts.MAX_FEDERATED_NODES] for
                                            encrypted_splitinfo
                                            in encrypted_splitinfo_provider[i]]
                encrypted_splitinfo_provider_table = session.parallelize(
                    zip(self.cur_split_nodes, batch_splitinfo_provider),
                    include_key=False,
                    partition=self.data_bin._partitions)

                _find_provider_split = functools.partial(find_provider_split, self.min_impurity_split,
                                                         self.encrypter.encrypt, self.encrypter.decrypt, self.splitter)

                splitinfos = encrypted_splitinfo_provider_table.mapValues(_find_provider_split).collect()
                for _, splitinfo in splitinfos:
                    if best_splitinfo_provider[_][0] == -1:
                        best_splitinfo_provider[_] = list(splitinfo[:2])
                        best_gains[_] = splitinfo[2]
                    elif splitinfo[0] != -1 and splitinfo[2] > best_gains[_]:
                        best_splitinfo_provider[_][0] = k + splitinfo[0]
                        best_splitinfo_provider[_][1] = splitinfo[1]
                        best_gains[_] = splitinfo[2]

            if idx != -1:
                self.sync_federated_best_splitinfo_provider(best_splitinfo_provider, dep, batch, idx)
                break

            self.sync_federated_best_splitinfo_provider(best_splitinfo_provider, dep, batch, i)

    def sync_final_split_provider(self, dep=-1, batch=-1, idx=-1):
        LOGGER.info("get provider final splitinfo of depth {}, batch {}".format(dep, batch))
        final_splitinfo_provider = self.transfer_inst.final_splitinfo_provider.get(idx=idx,
                                                                                   suffix=(dep, batch,))
        """
        final_splitinfo_provider = federation.get(name=self.transfer_inst.final_splitinfo_provider.name,
                                              tag=self.transfer_inst.generate_transferid(
                                                  self.transfer_inst.final_splitinfo_provider, dep, batch),
                                              idx=-1)
        """
        return final_splitinfo_provider if idx == -1 else [final_splitinfo_provider]

    def merge_splitinfo(self, splitinfo_promoter, splitinfo_provider, merge_provider_split_only=False,
                        need_decrypt=True):

        LOGGER.info("merge splitinfo, merge_provider_split_only is {}".format(merge_provider_split_only))

        if merge_provider_split_only:
            splitinfo_promoter = [None for i in range(len(splitinfo_provider[0]))]

        merge_infos = []
        for i in range(len(splitinfo_promoter)):
            splitinfo = [splitinfo_promoter[i]]
            for j in range(len(splitinfo_provider)):
                splitinfo.append(splitinfo_provider[j][i])

            merge_infos.append(splitinfo)

        splitinfo_promoter_provider_table = session.parallelize(merge_infos,
                                                                include_key=False,
                                                                partition=self.data_bin._partitions)
        _find_best_split_promoter_and_provider = functools.partial(find_best_split_promoter_and_provider,
                                                                   self.encrypter.decrypt, need_decrypt)
        best_splitinfo_table = splitinfo_promoter_provider_table.mapValues(_find_best_split_promoter_and_provider)

        best_splitinfos = [None for i in range(len(merge_infos))]
        for _, best_splitinfo in best_splitinfo_table.collect():
            best_splitinfos[_] = best_splitinfo

        return best_splitinfos

    def update_tree_node_queue(self, split_info_input):
        LOGGER.info("update tree node, splitlist length is {}, tree node queue size is".format(len(split_info_input),
                                                                                               len(self.tree_node_queue)))
        feature_num = self.bin_split_points.shape[0]
        split_info = []
        for node_split_info in split_info_input:
            if node_split_info.best_fid is None or node_split_info.best_fid < feature_num:
                node_split_info.sitename = self.sitename
            else:
                n = feature_num
                for (num, sitename) in self.provider_sitename_list:
                    n += num
                    if node_split_info.best_fid < n:
                        node_split_info.best_fid -= n-num
                        node_split_info.sitename = sitename
                        break
            split_info.append(node_split_info)

        new_tree_node_queue = []
        for i in range(len(self.tree_node_queue)):
            sum_grad = self.tree_node_queue[i].sum_grad
            sum_hess = self.tree_node_queue[i].sum_hess
            if split_info[i].best_fid is None or split_info[i].gain <= \
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

                new_tree_node_queue.append(left_node)
                new_tree_node_queue.append(right_node)

                self.tree_node_queue[i].fid = split_info[i].best_fid
                self.tree_node_queue[i].bid = split_info[i].best_bid
                self.tree_node_queue[i].missing_dir = split_info[i].missing_dir
                self.tree_node_queue[i].sitename = split_info[i].sitename

                self.update_feature_importance(split_info[i])

            self.tree_.append(self.tree_node_queue[i])

        self.tree_node_queue = new_tree_node_queue

    @staticmethod
    def dispatch_node(value, tree_=None, decoder=None, sitename=consts.PROMOTER,
                      split_maskdict=None, bin_sparse_points=None,
                      use_missing=False, zero_as_missing=False,
                      missing_dir_maskdict=None):
        unleaf_state, nodeid = value[1]

        if tree_[nodeid].is_leaf is True:
            return tree_[nodeid].weight
        else:
            if tree_[nodeid].sitename == sitename:
                fid = tree_[nodeid].fid
                bid = tree_[nodeid].bid
                if not use_missing:
                    if value[0].features.get_data(fid, bin_sparse_points[fid]) <= bid:
                        return 1, tree_[nodeid].left_nodeid
                    else:
                        return 1, tree_[nodeid].right_nodeid
                else:
                    missing_dir = tree_[nodeid].missing_dir

                    missing_val = False
                    if zero_as_missing:
                        if value[0].features.get_data(fid, None) is None or \
                                value[0].features.get_data(fid) == NoneType():
                            missing_val = True
                    elif use_missing and value[0].features.get_data(fid) == NoneType():
                        missing_val = True

                    if missing_val:
                        if missing_dir == 1:
                            return 1, tree_[nodeid].right_nodeid
                        else:
                            return 1, tree_[nodeid].left_nodeid
                    else:
                        LOGGER.debug("fid is {}, bid is {}, sitename is {}".format(fid, bid, sitename))
                        if value[0].features.get_data(fid, bin_sparse_points[fid]) <= bid:
                            return 1, tree_[nodeid].left_nodeid
                        else:
                            return 1, tree_[nodeid].right_nodeid
            else:
                return (1, tree_[nodeid].fid, tree_[nodeid].bid, tree_[nodeid].sitename,
                        nodeid, tree_[nodeid].left_nodeid, tree_[nodeid].right_nodeid)

    def sync_dispatch_node_provider(self, dispatch_to_provider_data, dep=-1, idx=-1):

        LOGGER.info("send node to provider to dispath, depth is {}".format(dep))
        self.transfer_inst.dispatch_node_provider.remote(dispatch_to_provider_data,
                                                         role=consts.PROVIDER,
                                                         idx=idx,
                                                         suffix=(dep,))
        LOGGER.info("get provider dispatch result, depth is {}".format(dep))
        ret = self.transfer_inst.dispatch_node_provider_result.get(idx=idx, suffix=(dep,))
        return ret if idx == -1 else [ret]

    def redispatch_node(self, dep, reach_max_depth=False):

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

        if self.sample_weights is None:
            self.sample_weights = leaf
        else:
            self.sample_weights = self.sample_weights.union(leaf)

        if reach_max_depth:  # if reach max_depth only update weight samples
            return

        dispatch_promoter_result = dispatch_promoter_result.subtractByKey(leaf)
        dispatch_node_provider_result = self.sync_dispatch_node_provider(dispatch_to_provider_result, dep)

        self.node_dispatch = None
        for idx in range(len(dispatch_node_provider_result)):
            if self.node_dispatch is None:
                self.node_dispatch = dispatch_node_provider_result[idx]
            else:
                self.node_dispatch = self.node_dispatch.join(dispatch_node_provider_result[idx],
                                                             lambda unleaf_state_nodeid1, unleaf_state_nodeid2:
                                                             unleaf_state_nodeid1 if len(
                                                                 unleaf_state_nodeid1) == 2 else unleaf_state_nodeid2,
                                                             need_send=True)

        self.node_dispatch = self.node_dispatch.union(dispatch_promoter_result, need_send=True)

    def sync_local_node_histogram(self, acc_histogram: List[HistogramBag], suffix):
        # sending local histogram
        self.aggregator.send_histogram(acc_histogram, suffix=suffix)
        LOGGER.debug('local histogram sent at layer {}'.format(suffix[0]))

    def sync_best_splits(self, suffix) -> List[SplitInfo]:

        best_splits = self.transfer_inst.best_split_points.get(idx=0, suffix=suffix)
        return best_splits

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

    def convert_bin_to_val(self):
        """
        convert current bid in tree nodes to real value
        """
        for node in self.tree_:
            if not node.is_leaf and node.sitename == self.sitename:
                node.bid = self.bin_split_points[node.fid][node.bid]

    def initialize_root_node(self, ):
        root_sum_grad, root_sum_hess = self.get_grad_hess_sum(self.grad_and_hess)
        root_node = Node(id=0, sitename=self.sitename, sum_grad=root_sum_grad, sum_hess=root_sum_hess,
                         weight=self.splitter.node_weight(root_sum_grad, root_sum_hess))
        return root_node

    @staticmethod
    def get_node_sample_weights(node_dispatch, tree_node: List[Node]):
        """
        get samples' weights which correspond to its node assignment
        """
        func = functools.partial(lambda inst, nodes: nodes[inst[1]].weight, nodes=tree_node)
        return node_dispatch.mapValues(func)

    def fit(self):
        LOGGER.info("begin to fit promoter decision tree")
        self.process_and_sync_grad_and_hess()

        g_sum, h_sum = self.get_grad_hess_sum(self.grad_and_hess)
        # get aggregated root info
        self.aggregator.send_local_root_node_info(g_sum, h_sum, suffix=('root_node_sync1', self.epoch_idx))
        g_h_dict = self.aggregator.get_aggregated_root_info(suffix=('root_node_sync2', self.epoch_idx))
        global_g_sum, global_h_sum = g_h_dict['g_sum'], g_h_dict['h_sum']

        root_node = Node(id=0, sitename=consts.PROMOTER, sum_grad=global_g_sum, sum_hess=global_h_sum,
                         weight=self.splitter.node_weight(global_g_sum, global_h_sum))

        self.tree_node_queue = [root_node]

        self.dispatch_all_node_to_root()

        for dep in range(self.max_depth):

            if dep + 1 == self.max_depth:

                for node in self.tree_node_queue:
                    node.is_leaf = True
                    self.tree_.append(node)
                rest_sample_weights = self.get_node_sample_weights(self.node_dispatch, self.tree_)
                if self.sample_weights is None:
                    self.sample_weights = rest_sample_weights
                else:
                    self.sample_weights = self.sample_weights.union(rest_sample_weights)

                # stop fitting
                break

            LOGGER.info("start to fit depth {}, tree node queue size is {}".format(dep, len(self.tree_node_queue)))

            self.sync_tree_node_queue(self.tree_node_queue, dep)
            if len(self.tree_node_queue) == 0:
                break

            self.sync_node_positions(dep)
            self.update_instances_node_positions()

            self.sync_cur_layer_node_num(len(self.tree_node_queue), suffix=(dep, self.epoch_idx, self.tree_idx))

            split_info, agg_histograms = [], []
            for batch_idx, i in enumerate(range(0, len(self.tree_node_queue), self.max_split_nodes)):
                self.cur_split_nodes = self.tree_node_queue[i: i + self.max_split_nodes]

                histograms = self.get_histograms(node_map=self.get_node_map(self.cur_split_nodes), dep=dep,
                                                 batch_idx=batch_idx)

                LOGGER.debug('federated finding best splits for batch{} at layer {}'.format(batch_idx, dep))
                self.sync_local_node_histogram(histograms, suffix=(batch_idx, dep, self.epoch_idx, self.tree_idx))

                agg_histograms += histograms

            split_info = self.sync_best_splits(suffix=(dep, self.epoch_idx))
            LOGGER.debug('got best splits from arbiter')

            self.update_tree_node_queue(split_info)
            self.redispatch_node(dep)

        self.convert_bin_to_val()
        self.round_leaf_val()
        self.sync_tree()
        LOGGER.info("tree node num is %d" % len(self.tree_))
        LOGGER.info("end to fit promoter decision tree")

    def get_computing_node_dispatch(self):
        if self.run_goss:
            node_dispatch = self.node_dispatch.join(self.grad_and_hess, lambda x1, x2: x1, need_send=True)
        else:
            node_dispatch = self.node_dispatch
        return node_dispatch

    def get_provider_sitename(self, provider_idx):
        provider_member_id = self.provider_member_idlist[provider_idx]
        provider_sitename = ":".join([consts.PROVIDER, str(provider_member_id)])
        return provider_sitename

    def get_histograms(self, node_map, dep, batch_idx):
        LOGGER.info("start to get node histograms")

        promoter_local_histograms = self.get_local_histograms(dep, self.data_bin_with_node_dispatch, self.grad_and_hess,
                                                              None, self.cur_split_nodes, node_map, ret='tensor',
                                                              hist_sub=False)

        acc_histograms = promoter_local_histograms
        provider_local_histograms = self.sync_provider_local_histograms(dep, batch_idx)
        for provider_local_histogram, site_name in provider_local_histograms:
            feature_num = len(provider_local_histogram[0])
            if dep == 0:
                self.provider_sitename_list.append((feature_num, site_name))
            for node_hist_list in provider_local_histogram:
                for i, hist in enumerate(node_hist_list):
                    if len(hist) > 0:
                        for x in hist:
                            x[0] = 0 if x[0] == 0 else self.encrypter.decrypt(x[0])
                            x[1] = 0 if x[1] == 0 else self.encrypter.decrypt(x[1])

            for node, acc_histogram in enumerate(acc_histograms):
                acc_histograms[node].extend(provider_local_histogram[node])

        hist_bags = []
        for hist_list in acc_histograms:
            hist_bags.append(HistogramBag(hist_list))

        left_nodes = []
        for node in self.cur_split_nodes:
            # if node.is_left_node or node.id == 0:
            left_nodes.append(node)

        # set histogram id and parent histogram id
        for node, hist_bag in zip(left_nodes, hist_bags):
            # LOGGER.debug('node id {}, node parent id {}, cur tree {}'.format(node.id, node.parent_nodeid, len(tree)))
            hist_bag.hid = node.id
            hist_bag.p_hid = node.parent_nodeid

        return hist_bags

    @staticmethod
    def traverse_tree(predict_state, data_inst, tree_=None,
                      decoder=None, sitename=consts.PROMOTER, split_maskdict=None,
                      use_missing=None, zero_as_missing=None, missing_dir_maskdict=None, return_leaf_id=False):

        nid, tag = predict_state

        while tree_[nid].sitename == sitename:
            if tree_[nid].is_leaf is True:
                return tree_[nid].weight if not return_leaf_id else nid

            fid = tree_[nid].fid
            bid = tree_[nid].bid
            missing_dir = tree_[nid].missing_dir
            # fid = decoder("feature_idx", tree_[nid].fid, split_maskdict=split_maskdict)
            # bid = decoder("feature_val", tree_[nid].bid, nid, split_maskdict=split_maskdict)
            # if use_missing:
            #     missing_dir = decoder("missing_dir", 1, nid, missing_dir_maskdict=missing_dir_maskdict)
            # else:
            #     missing_dir = 1

            if use_missing and zero_as_missing:
                # missing_dir = decoder("missing_dir", 1, nid, missing_dir_maskdict=missing_dir_maskdict)
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
                                              decoder=self.decode,
                                              sitename=self.sitename,
                                              split_maskdict=self.split_maskdict,
                                              use_missing=self.use_missing,
                                              zero_as_missing=self.zero_as_missing,
                                              missing_dir_maskdict=self.missing_dir_maskdict)
            predict_data = predict_data.join(data_inst,
                                             traverse_tree)  # list[(data_index,(nid,1)) or (data_index,weight)]
            predict_leaf = predict_data.filter(
                lambda key, value: isinstance(value, tuple) is False)  # list(data_index,weight)
            if predict_result is None:
                predict_result = predict_leaf
            else:
                predict_result = predict_result.union(predict_leaf)

            predict_data = predict_data.subtractByKey(predict_leaf)

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

        model_param.split_maskdict.update(self.split_maskdict)
        model_param.missing_dir_maskdict.update(self.missing_dir_maskdict)

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

        self.split_maskdict = dict(model_param.split_maskdict)
        self.missing_dir_maskdict = dict(model_param.missing_dir_maskdict)

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

    def assign_instance_to_leaves_and_update_weights(self):
        # re-assign samples to leaf nodes and update weights
        self.update_tree_node_queue([], True)
        self.update_instances_node_positions()
        self.redispatch_node(self.max_depth, reach_max_depth=True)


def find_best_split_promoter_and_provider(decrypt, need_decrypt, splitinfo_promoter_provider):
    best_gain_provider = decrypt(splitinfo_promoter_provider[1].gain) if need_decrypt else splitinfo_promoter_provider[
        1].gain
    best_gain_provider_idx = 1
    for i in range(1, len(splitinfo_promoter_provider)):
        gain_provider_i = decrypt(splitinfo_promoter_provider[i].gain) if need_decrypt else splitinfo_promoter_provider[
            i].gain
        if best_gain_provider < gain_provider_i - consts.FLOAT_ZERO:
            best_gain_provider = gain_provider_i
            best_gain_provider_idx = i

    # if merge_provider_split_only is True, promoter hists is None
    if splitinfo_promoter_provider[0] is not None and \
            splitinfo_promoter_provider[0].gain >= best_gain_provider - consts.FLOAT_ZERO:
        best_splitinfo = splitinfo_promoter_provider[0]
    else:
        best_splitinfo = splitinfo_promoter_provider[best_gain_provider_idx]
        LOGGER.debug('best split info is {}, {}'.format(best_splitinfo.sum_grad, best_splitinfo.sum_hess))

        # when this node can not be further split, provider sum_grad and sum_hess is not an encrypted number but 0
        # so need type checking here
        if need_decrypt:
            best_splitinfo.sum_grad = decrypt(best_splitinfo.sum_grad) \
                if type(best_splitinfo.sum_grad) != int else best_splitinfo.sum_grad
            best_splitinfo.sum_hess = decrypt(best_splitinfo.sum_hess) \
                if type(best_splitinfo.sum_hess) != int else best_splitinfo.sum_hess
            best_splitinfo.gain = best_gain_provider

    return best_splitinfo


def find_provider_split(min_impurity_split, encrypt, decrypt, splitter, value):
    """
    find_provider_split

    In order to solve the problem of function calculation serialization error, remove the self dependency

    :param min_impurity_split:
    :param encrypt:
    :param decrypt:
    :param splitter:
    :param value:
    :return:
    """
    cur_split_node, encrypted_splitinfo_provider = value
    sum_grad = cur_split_node.sum_grad
    sum_hess = cur_split_node.sum_hess
    best_gain = min_impurity_split - consts.FLOAT_ZERO
    best_idx = -1

    for i in range(len(encrypted_splitinfo_provider)):
        sum_grad_l, sum_hess_l = encrypted_splitinfo_provider[i]
        sum_grad_l = decrypt(sum_grad_l)
        sum_hess_l = decrypt(sum_hess_l)
        sum_grad_r = sum_grad - sum_grad_l
        sum_hess_r = sum_hess - sum_hess_l
        gain = splitter.split_gain(sum_grad, sum_hess, sum_grad_l,
                                   sum_hess_l, sum_grad_r, sum_hess_r)

        if gain > min_impurity_split and gain > best_gain:
            best_gain = gain
            best_idx = i

    encrypted_best_gain = encrypt(best_gain)
    return best_idx, encrypted_best_gain, best_gain

