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


import copy
import functools

from common.python import session
from common.python.utils import log_utils
from kernel.components.boosting import DecisionTree
from kernel.components.boosting import Node
from kernel.components.boosting.core.splitinfo_cipher_compressor import PromoterGradHessEncoder, \
    PromoterSplitInfoDecompressor
from kernel.components.boosting.core.subsample import goss_sampling
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import CriterionMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.security.encrypt import PaillierEncrypt, IterativeAffineEncrypt
from kernel.transfer.variables.transfer_class.vert_decision_tree_transfer_variable import \
    VertDecisionTreeTransferVariable
from kernel.utils import consts
from kernel.utils.data_util import NoneType
from kernel.utils.io_check import assert_io_num_rows_equal

LOGGER = log_utils.get_logger()


class VertDecisionTreePromoter(DecisionTree):
    def __init__(self, tree_param):
        LOGGER.info("vert decision tree promoter init!")
        super(VertDecisionTreePromoter, self).__init__(tree_param)

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
        self.transfer_inst = VertDecisionTreeTransferVariable()
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
        self.run_cipher_compressing = False
        self.key_length = None
        self.round_decimal = 7
        self.max_sample_weight = 1

        # code version control
        self.new_ver = True

    def report_init_status(self):

        LOGGER.info('reporting initialization status')
        LOGGER.info('using new version code {}'.format(self.new_ver))
        if self.complete_secure_tree:
            LOGGER.info('running complete secure')
        if self.run_goss:
            LOGGER.info('run goss is {}, top rate is {}, other rate is {}'.format(self.run_goss, self.top_rate,
                                                                                  self.other_rate))
            LOGGER.info('sampled g_h count is {}, total sample num is {}'.format(self.grad_and_hess.count(),
                                                                                 self.data_bin.count()))
        if self.run_cipher_compressing:
            LOGGER.info('running cipher compressing')
            LOGGER.info('round decimal is {}'.format(self.round_decimal))
        LOGGER.info('updated max sample weight is {}'.format(self.max_sample_weight))

        if self.deterministic:
            LOGGER.info('running on deterministic mode')

    def init(self, flowid, runtime_idx, data_bin, bin_split_points, bin_sparse_points, valid_features,
             grad_and_hess,
             encrypter, encrypted_mode_calculator,
             provider_member_idlist,
             complete_secure=False,
             goss_subsample=False,
             top_rate=0.1,
             other_rate=0.2,
             cipher_compressing=False,
             encrypt_key_length=None,
             round_decimal=7,
             max_sample_weight=1,
             new_ver=True):

        super(VertDecisionTreePromoter, self).init_variables(flowid, runtime_idx, data_bin, bin_split_points,
                                                             bin_sparse_points, valid_features)

        self.check_max_split_nodes()

        self.grad_and_hess = grad_and_hess
        self.encrypter = encrypter
        self.encrypted_mode_calculator = encrypted_mode_calculator
        self.complete_secure_tree = complete_secure
        self.provider_member_idlist = provider_member_idlist

        self.run_goss = goss_subsample
        self.top_rate = top_rate
        self.other_rate = other_rate

        self.run_cipher_compressing = cipher_compressing
        self.key_length = encrypt_key_length
        self.round_decimal = round_decimal
        self.max_sample_weight = max_sample_weight

        if self.run_goss:
            self.goss_sampling()
            self.max_sample_weight = self.max_sample_weight * ((1 - top_rate) / other_rate)

        if self.run_cipher_compressing:
            self.init_compressor()

        self.new_ver = new_ver

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

    def init_compressor(self):
        self.cipher_encoder = PromoterGradHessEncoder(self.encrypter, self.encrypted_mode_calculator,
                                                      task_type=consts.CLASSIFICATION,
                                                      round_decimal=self.round_decimal,
                                                      max_sample_weights=self.max_sample_weight)

        self.cipher_decompressor = PromoterSplitInfoDecompressor(self.encrypter, task_type=consts.CLASSIFICATION,
                                                                 max_sample_weight=self.max_sample_weight)

        max_capacity_int = self.encrypter.public_key.max_int
        para = {'max_capacity_int': max_capacity_int, 'en_type': self.get_encrypt_type(),
                'max_sample_weight': self.max_sample_weight}

        self.transfer_inst.cipher_compressor_para.remote(para, idx=-1)

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

        if self.run_cipher_compressing:
            LOGGER.info('sending encoded g/h to provider')
            en_grad_hess = self.cipher_encoder.encode_g_h_and_encrypt(self.grad_and_hess)
        else:
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

    # def find_provider_split(self, value):
    #     """
    #     find_provider_split
    #     :param value:
    #     :return:
    #     """
    #     cur_split_node, encrypted_splitinfo_provider = value
    #     sum_grad = cur_split_node.sum_grad
    #     sum_hess = cur_split_node.sum_hess
    #     best_gain = self.min_impurity_split - consts.FLOAT_ZERO
    #     best_idx = -1
    #
    #     for i in range(len(encrypted_splitinfo_provider)):
    #         sum_grad_l, sum_hess_l = encrypted_splitinfo_provider[i]
    #         sum_grad_l = self.decrypt(sum_grad_l)
    #         sum_hess_l = self.decrypt(sum_hess_l)
    #         sum_grad_r = sum_grad - sum_grad_l
    #         sum_hess_r = sum_hess - sum_hess_l
    #         gain = self.splitter.split_gain(sum_grad, sum_hess, sum_grad_l,
    #                                         sum_hess_l, sum_grad_r, sum_hess_r)
    #
    #         if gain > self.min_impurity_split and gain > best_gain:
    #             best_gain = gain
    #             best_idx = i
    #
    #     encrypted_best_gain = self.encrypt(best_gain)
    #     return best_idx, encrypted_best_gain, best_gain

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

    # def find_best_split_promoter_and_provider(self, splitinfo_promoter_provider):
    #     best_gain_provider = self.decrypt(splitinfo_promoter_provider[1].gain)
    #     best_gain_provider_idx = 1
    #     for i in range(1, len(splitinfo_promoter_provider)):
    #         gain_provider_i = self.decrypt(splitinfo_promoter_provider[i].gain)
    #         if best_gain_provider < gain_provider_i:
    #             best_gain_provider = gain_provider_i
    #             best_gain_provider_idx = i
    #
    #     if splitinfo_promoter_provider[0].gain >= best_gain_provider - consts.FLOAT_ZERO:
    #         best_splitinfo = splitinfo_promoter_provider[0]
    #     else:
    #         best_splitinfo = splitinfo_promoter_provider[best_gain_provider_idx]
    #         best_splitinfo.sum_grad = self.decrypt(best_splitinfo.sum_grad)
    #         best_splitinfo.sum_hess = self.decrypt(best_splitinfo.sum_hess)
    #         best_splitinfo.gain = best_gain_provider
    #
    #     return best_splitinfo

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
        # best_splitinfos = [best_splitinfo[1] for best_splitinfo in best_splitinfo_table.collect()]

        return best_splitinfos

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

                new_tree_node_queue.append(left_node)
                new_tree_node_queue.append(right_node)

                self.tree_node_queue[i].sitename = split_info[i].sitename
                if self.tree_node_queue[i].sitename == self.sitename:
                    self.tree_node_queue[i].fid = self.encode("feature_idx", split_info[i].best_fid)
                    self.tree_node_queue[i].bid = self.encode("feature_val", split_info[i].best_bid,
                                                              self.tree_node_queue[i].id)
                    self.tree_node_queue[i].missing_dir = self.encode("missing_dir",
                                                                      split_info[i].missing_dir,
                                                                      self.tree_node_queue[i].id)
                else:
                    self.tree_node_queue[i].fid = split_info[i].best_fid
                    self.tree_node_queue[i].bid = split_info[i].best_bid

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
                fid = decoder("feature_idx", tree_[nodeid].fid, split_maskdict=split_maskdict)
                bid = decoder("feature_val", tree_[nodeid].bid, nodeid, split_maskdict=split_maskdict)
                if not use_missing:
                    if value[0].features.get_data(fid, bin_sparse_points[fid]) <= bid:
                        return 1, tree_[nodeid].left_nodeid
                    else:
                        return 1, tree_[nodeid].right_nodeid
                else:
                    missing_dir = decoder("missing_dir", tree_[nodeid].missing_dir, nodeid,
                                          missing_dir_maskdict=missing_dir_maskdict)

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
                                                                 unleaf_state_nodeid1) == 2 else unleaf_state_nodeid2)

        self.node_dispatch = self.node_dispatch.union(dispatch_promoter_result, need_send=True)

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
        LOGGER.info("convert tree node bins to real value")
        for i in range(len(self.tree_)):
            if self.tree_[i].is_leaf is True:
                continue
            if self.tree_[i].sitename == self.sitename:
                fid = self.decode("feature_idx", self.tree_[i].fid, split_maskdict=self.split_maskdict)
                bid = self.decode("feature_val", self.tree_[i].bid, self.tree_[i].id, self.split_maskdict)
                real_splitval = self.encode("feature_val", self.bin_split_points[fid][bid], self.tree_[i].id)
                self.tree_[i].bid = real_splitval

    def initialize_root_node(self, ):
        root_sum_grad, root_sum_hess = self.get_grad_hess_sum(self.grad_and_hess)
        root_node = Node(id=0, sitename=self.sitename, sum_grad=root_sum_grad, sum_hess=root_sum_hess,
                         weight=self.splitter.node_weight(root_sum_grad, root_sum_hess))
        return root_node

    def fit(self):
        LOGGER.info("begin to fit promoter decision tree")
        self.process_and_sync_grad_and_hess()

        root_node = self.initialize_root_node()

        self.tree_node_queue = [root_node]

        self.dispatch_all_node_to_root()

        for dep in range(self.max_depth):
            LOGGER.info("start to fit depth {}, tree node queue size is {}".format(dep, len(self.tree_node_queue)))

            self.sync_tree_node_queue(self.tree_node_queue, dep)
            if len(self.tree_node_queue) == 0:
                break

            self.sync_node_positions(dep)
            self.update_instances_node_positions()

            split_info = []
            for batch_idx, i in enumerate(range(0, len(self.tree_node_queue), self.max_split_nodes)):
                self.cur_split_nodes = self.tree_node_queue[i: i + self.max_split_nodes]

                # cur_splitinfos = self.compute_best_splits(self.get_node_map(self.cur_split_nodes), dep, batch_idx, )
                node_map = self.get_node_map(self.cur_split_nodes)

                if self.new_ver:
                    cur_splitinfos = self.compute_best_splits2(node_map, dep, batch_idx)
                else:
                    cur_splitinfos = self.compute_best_splits(node_map, dep, batch_idx)

                split_info.extend(cur_splitinfos)

            max_depth_reach = True if dep + 1 == self.max_depth else False
            self.update_tree_node_queue(split_info, max_depth_reach)
            self.redispatch_node(dep)

        self.convert_bin_to_real()
        self.round_leaf_val()
        self.sync_tree()
        LOGGER.info("tree node num is %d" % len(self.tree_))
        LOGGER.info("end to fit promoter decision tree")

    def get_computing_node_dispatch(self):
        if self.run_goss:
            node_dispatch = self.node_dispatch.join(self.grad_and_hess, lambda x1, x2: x1)
        else:
            node_dispatch = self.node_dispatch
        return node_dispatch

    def get_provider_sitename(self, provider_idx):
        provider_member_id = self.provider_member_idlist[provider_idx]
        provider_sitename = ":".join([consts.PROVIDER, str(provider_member_id)])
        return provider_sitename

    def compute_best_splits2(self, node_map, dep, batch_idx):

        LOGGER.info('solving node batch {}, node num is {}'.format(batch_idx, len(self.cur_split_nodes)))
        node_dispatch = self.get_computing_node_dispatch()
        node_sample_count = self.count_node_sample_num(node_dispatch, node_map)
        LOGGER.debug('sample count is {}'.format(node_sample_count))
        acc_histograms = self.get_local_histograms(dep, self.data_bin_with_node_dispatch, self.grad_and_hess,
                                                   node_sample_count, self.cur_split_nodes, node_map, ret='tensor',
                                                   hist_sub=True)
        best_split_info_promoter = self.splitter.find_split(acc_histograms, self.valid_features,
                                                            self.data_bin._partitions, self.sitename,
                                                            self.use_missing, self.zero_as_missing)

        if self.complete_secure_tree:
            return best_split_info_promoter
        provider_split_info_tables = self.transfer_inst.encrypted_splitinfo_provider.get(idx=-1,
                                                                                         suffix=(dep, batch_idx))

        best_splits_of_all_providers = []

        if self.run_cipher_compressing:
            self.cipher_decompressor.renew_decompressor(node_map)
        cipher_decompressor = self.cipher_decompressor if self.run_cipher_compressing else None

        for provider_idx, split_info_table in enumerate(provider_split_info_tables):
            provider_split_info = self.splitter.find_provider_best_split_info(split_info_table,
                                                                              self.get_provider_sitename(provider_idx),
                                                                              self.encrypter,
                                                                              cipher_decompressor=cipher_decompressor)
            split_info_list = [None for i in range(len(provider_split_info))]
            for key in provider_split_info:
                split_info_list[node_map[key]] = provider_split_info[key]
            return_split_info = copy.deepcopy(split_info_list)
            for split_info in return_split_info:
                split_info.sum_grad, split_info.sum_hess, split_info.gain = None, None, None
            self.transfer_inst.federated_best_splitinfo_provider.remote(return_split_info,
                                                                        suffix=(dep, batch_idx), idx=provider_idx,
                                                                        role=consts.PROVIDER)
            best_splits_of_all_providers.append(split_info_list)

        # get encoded split-info from providers
        final_provider_split_info = self.sync_final_split_provider(dep, batch_idx)
        for masked_split_info, encoded_split_info in zip(best_splits_of_all_providers, final_provider_split_info):
            for s1, s2 in zip(masked_split_info, encoded_split_info):
                s2.gain = s1.gain
                s2.sum_grad = s1.sum_grad
                s2.sum_hess = s1.sum_hess

        final_best_splits = self.merge_splitinfo(best_split_info_promoter, final_provider_split_info,
                                                 need_decrypt=False)

        return final_best_splits

    def compute_best_splits(self, node_map, dep, batch_idx):

        acc_histograms = self.get_local_histograms(dep, self.data_bin_with_node_dispatch, self.grad_and_hess,
                                                   None, self.cur_split_nodes, node_map, ret='tensor',
                                                   hist_sub=False)

        best_split_info_promoter = self.splitter.find_split(acc_histograms, self.valid_features,
                                                            self.data_bin._partitions, self.sitename,
                                                            self.use_missing, self.zero_as_missing)
        LOGGER.debug('computing local splits done')

        if self.complete_secure_tree:
            return best_split_info_promoter

        self.federated_find_split(dep, batch_idx)
        provider_split_info = self.sync_final_split_provider(dep, batch_idx)

        # compare provider best split points with promoter split points
        cur_best_split = self.merge_splitinfo(splitinfo_promoter=best_split_info_promoter,
                                              splitinfo_provider=provider_split_info)

        return cur_best_split

    @staticmethod
    def traverse_tree(predict_state, data_inst, tree_=None,
                      decoder=None, sitename=consts.PROMOTER, split_maskdict=None,
                      use_missing=None, zero_as_missing=None, missing_dir_maskdict=None, return_leaf_id=False):

        nid, tag = predict_state

        while tree_[nid].sitename == sitename:
            if tree_[nid].is_leaf is True:
                return tree_[nid].weight if not return_leaf_id else nid

            fid = decoder("feature_idx", tree_[nid].fid, split_maskdict=split_maskdict)
            bid = decoder("feature_val", tree_[nid].bid, nid, split_maskdict=split_maskdict)
            if use_missing:
                missing_dir = decoder("missing_dir", 1, nid, missing_dir_maskdict=missing_dir_maskdict)
            else:
                missing_dir = 1

            if use_missing and zero_as_missing:
                missing_dir = decoder("missing_dir", 1, nid, missing_dir_maskdict=missing_dir_maskdict)
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

