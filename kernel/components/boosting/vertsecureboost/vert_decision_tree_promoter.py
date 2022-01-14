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

from common.python import session
from common.python.utils import log_utils
from kernel.components.boosting import DecisionTree
from kernel.components.boosting import Node
from kernel.components.boosting.core.subsample import goss_sampling
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import CriterionMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.security.encrypt import PaillierEncrypt
from kernel.transfer.variables.transfer_class.vert_decision_tree_transfer_variable import \
    VertDecisionTreeTransferVariable
from kernel.utils import consts
from kernel.utils.data_util import NoneType
from kernel.utils.io_check import assert_io_num_rows_equal
from kernel.base.statics import MultivariateStatisticalSummary
from kernel.components.boosting.core.g_h_optim import GHPacker
LOGGER = log_utils.get_logger()


class VertDecisionTreePromoter(DecisionTree):

    def __init__(self, tree_param):
        super(VertDecisionTreePromoter, self).__init__(tree_param)

        self.encrypter = None
        self.encrypted_mode_calculator = None
        self.transfer_inst = VertDecisionTreeTransferVariable()

        self.sitename = consts.PROMOTER  # will be modified in self.set_runtime_idx()
        self.complete_secure_tree = False
        self.split_maskdict = {}
        self.missing_dir_maskdict = {}
        self.provider_member_idlist = []
        self.compressor = None

        # goss subsample
        self.run_goss = False
        self.top_rate, self.other_rate = 0.2, 0.1  # goss sampling rate

        # cipher compressing
        self.task_type = None
        self.run_cipher_compressing = True
        self.packer = None
        self.max_sample_weight = 1

        # code version control
        self.new_ver = True

    """
    Node Encode/ Decode
    """

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
                raise ValueError("decode val %s cause error, can't recognize it!" % (str(val)))

        if dtype == "missing_dir":
            if nid in missing_dir_maskdict:
                return missing_dir_maskdict[nid]
            else:
                raise ValueError("decode val %s cause error, can't recognize it!" % (str(val)))

        return TypeError("decode type %s is not support!" % (str(dtype)))

    """
    Setting
    """

    def set_provider_member_idlist(self, id_list):
        self.provider_member_idlist = id_list

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
        LOGGER.info('updated max sample weight is {}'.format(self.max_sample_weight))

        if self.deterministic:
            LOGGER.info('running on deterministic mode')

    def init(self, flowid, runtime_idx, data_bin, bin_split_points, bin_sparse_points, valid_features,
             grad_and_hess,
             encrypter, encrypted_mode_calculator,
             provider_member_idlist,
             task_type,
             complete_secure=False,
             goss_subsample=False,
             top_rate=0.1,
             other_rate=0.2,
             cipher_compressing=False,
             max_sample_weight=1,
             new_ver=True):

        super(VertDecisionTreePromoter, self).init_data_and_variable(flowid, runtime_idx, data_bin, bin_split_points,
                                                                    bin_sparse_points, valid_features,grad_and_hess)

        self.check_max_split_nodes()
        self.encrypter = encrypter
        self.encrypted_mode_calculator = encrypted_mode_calculator
        self.complete_secure_tree = complete_secure
        self.provider_member_idlist = provider_member_idlist

        self.run_goss = goss_subsample
        self.top_rate = top_rate
        self.other_rate = other_rate

        self.run_cipher_compressing = cipher_compressing
        self.max_sample_weight = max_sample_weight

        self.task_type = task_type

        # initializing goss settings
        if self.run_goss:
            self.encrypted_mode_calculator.align_to_input_data = False

            if self.encrypted_mode_calculator.mode != 'strict':
                if self.encrypted_mode_calculator.enc_zeros is None:
                    self.encrypted_mode_calculator.init_enc_zero(self.grad_and_hess,
                                                                 raw_en=self.run_cipher_compressing, exponent=0)
                    LOGGER.info('fast/balance encrypt mode, initialize enc zeros for goss sampling')

            self.goss_sampling()
            self.max_sample_weight = self.max_sample_weight * ((1 - top_rate) / other_rate)

        self.new_ver = new_ver
        self.report_init_status()

    """
    Encrypt/ Decrypt
    """

    def encrypt(self, val):
        return self.encrypter.encrypt(val)

    def decrypt(self, val):
        return self.encrypter.decrypt(val)

    def get_encrypt_type(self):

        if type(self.encrypter) == PaillierEncrypt:
            return consts.PAILLIER
        else:
            raise ValueError('unknown encrypter type: {}'.format(type(self.encrypter)))

    """
    Node Splitting
    """

    def get_provider_sitename(self, provider_idx):
        provider_member_id = self.provider_member_idlist[provider_idx]
        provider_sitename = ":".join([consts.PROVIDER, str(provider_member_id)])
        return provider_sitename

    def find_provider_split(self, value):

        cur_split_node, encrypted_splitinfo_provider = value
        sum_grad = cur_split_node.sum_grad
        sum_hess = cur_split_node.sum_hess

        best_gain = self.min_impurity_split - consts.FLOAT_ZERO
        best_idx = -1

        perform_recorder = {}
        gains = []

        for i in range(len(encrypted_splitinfo_provider)):
            sum_grad_l, sum_hess_l = encrypted_splitinfo_provider[i]
            sum_grad_l = self.decrypt(sum_grad_l)
            sum_hess_l = self.decrypt(sum_hess_l)
            sum_grad_r = sum_grad - sum_grad_l
            sum_hess_r = sum_hess - sum_hess_l
            gain = self.splitter.split_gain(sum_grad, sum_hess, sum_grad_l,
                                            sum_hess_l, sum_grad_r, sum_hess_r)

            perform_recorder[i] = gain
            gains.append(gain)

            if gain > self.min_impurity_split and gain > best_gain + consts.FLOAT_ZERO:
                best_gain = gain
                best_idx = i

        encrypted_best_gain = self.encrypt(best_gain)
        return best_idx, encrypted_best_gain, best_gain

    def find_best_split_promoter_and_provider(self, splitinfo_promoter_provider, need_decrypt=True):

        best_gain_provider = self.decrypt(splitinfo_promoter_provider[1].gain) if need_decrypt else splitinfo_promoter_provider[1].gain
        best_gain_provider_idx = 1
        for i in range(1, len(splitinfo_promoter_provider)):
            gain_provider_i = self.decrypt(splitinfo_promoter_provider[i].gain) if need_decrypt else splitinfo_promoter_provider[i].gain
            if best_gain_provider < gain_provider_i - consts.FLOAT_ZERO:
                best_gain_provider = gain_provider_i
                best_gain_provider_idx = i

        # if merge_provider_split_only is True, promoter hists is None
        if splitinfo_promoter_provider[0] is not None and \
                splitinfo_promoter_provider[0].gain >= best_gain_provider - consts.FLOAT_ZERO:
            best_splitinfo = splitinfo_promoter_provider[0]
        else:
            best_splitinfo = splitinfo_promoter_provider[best_gain_provider_idx]

            # when this node can not be further split, provider sum_grad and sum_hess is not an encrypted number but 0
            # so need type checking here
            if need_decrypt:
                best_splitinfo.sum_grad = self.decrypt(best_splitinfo.sum_grad) \
                    if type(best_splitinfo.sum_grad) != int else best_splitinfo.sum_grad
                best_splitinfo.sum_hess = self.decrypt(best_splitinfo.sum_hess) \
                    if type(best_splitinfo.sum_hess) != int else best_splitinfo.sum_hess
                best_splitinfo.gain = best_gain_provider

        return best_splitinfo

    def merge_splitinfo(self, splitinfo_promoter, splitinfo_provider, merge_provider_split_only=False, need_decrypt=True):

        LOGGER.info("merging splitinfo, merge_provider_split_only is {}".format(merge_provider_split_only))

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

        find_split_func = functools.partial(self.find_best_split_promoter_and_provider, need_decrypt=need_decrypt)
        best_splitinfo_table = splitinfo_promoter_provider_table.mapValues(find_split_func)

        best_splitinfos = [None for i in range(len(merge_infos))]
        for _, best_splitinfo in best_splitinfo_table.collect():
            best_splitinfos[_] = best_splitinfo

        return best_splitinfos

    def federated_find_split(self, dep=-1, batch=-1, idx=-1):

        LOGGER.info("federated find split of depth {}, batch {}".format(dep, batch))
        # get flatten split points from providers
        # [split points from provider 1, split point from provider 2, .... so on] ↓
        encrypted_splitinfo_provider = self.sync_encrypted_splitinfo_provider(dep, batch, idx=idx)

        for provider_idx in range(len(encrypted_splitinfo_provider)):

            LOGGER.debug('provider sitename is {}'.format(self.get_provider_sitename(provider_idx)))

            init_gain = self.min_impurity_split - consts.FLOAT_ZERO
            encrypted_init_gain = self.encrypter.encrypt(init_gain)
            # init encrypted gain for every nodes in cur layer
            best_splitinfo_provider = [[-1, encrypted_init_gain] for j in range(len(self.cur_to_split_nodes))]
            # init best gain for every nodes in cur layer
            best_gains = [init_gain for j in range(len(self.cur_to_split_nodes))]
            # max split points to compute at a time, to control memory consumption
            max_nodes = max(len(encrypted_splitinfo_provider[provider_idx][j]) for j in range(len(self.cur_to_split_nodes)))
            # batch split point finding for every cur to split nodes
            for k in range(0, max_nodes, consts.MAX_SPLITINFO_TO_COMPUTE):
                batch_splitinfo_provider = [encrypted_splitinfo[k: k + consts.MAX_SPLITINFO_TO_COMPUTE] for encrypted_splitinfo
                                        in encrypted_splitinfo_provider[provider_idx]]

                encrypted_splitinfo_provider_table = session.parallelize(zip(self.cur_to_split_nodes, batch_splitinfo_provider),
                                                                     include_key=False,
                                                                     partition=self.data_bin._partitions)

                splitinfos = encrypted_splitinfo_provider_table.mapValues(self.find_provider_split).collect()

                # update best splitinfo and gain for every cur to split nodes
                for node_idx, splitinfo in splitinfos:

                    if best_splitinfo_provider[node_idx][0] == -1:
                        best_splitinfo_provider[node_idx] = list(splitinfo[:2])
                        best_gains[node_idx] = splitinfo[2]
                    elif splitinfo[0] != -1 and splitinfo[2] > best_gains[node_idx] + consts.FLOAT_ZERO:
                        best_splitinfo_provider[node_idx][0] = k + splitinfo[0]
                        best_splitinfo_provider[node_idx][1] = splitinfo[1]
                        best_gains[node_idx] = splitinfo[2]

            if idx != -1:
                self.sync_federated_best_splitinfo_provider(best_splitinfo_provider, dep, batch, idx)
                break

            self.sync_federated_best_splitinfo_provider(best_splitinfo_provider, dep, batch, provider_idx)

    def get_computing_inst2node_idx(self):
        if self.run_goss:
            inst2node_idx = self.inst2node_idx.join(self.grad_and_hess, lambda x1, x2: x1)
        else:
            inst2node_idx = self.inst2node_idx
        return inst2node_idx

    def compute_best_splits2(self, cur_to_split_nodes, node_map, dep, batch_idx):

        LOGGER.info('solving node batch {}, node num is {}'.format(batch_idx, len(cur_to_split_nodes)))
        inst2node_idx = self.get_computing_inst2node_idx()
        node_sample_count = self.count_node_sample_num(inst2node_idx, node_map)
        LOGGER.debug('sample count is {}'.format(node_sample_count))
        acc_histograms = self.get_local_histograms(dep, self.data_with_node_assignments, self.grad_and_hess,
                                                   node_sample_count, cur_to_split_nodes, node_map, ret='tensor',
                                                   hist_sub=True)

        best_split_info_promoter = self.splitter.find_split(acc_histograms, self.valid_features,
                                                         self.data_bin._partitions, self.sitename,
                                                         self.use_missing, self.zero_as_missing)

        if self.complete_secure_tree:
            return best_split_info_promoter

        provider_split_info_tables = self.transfer_inst.encrypted_splitinfo_provider.get(idx=-1, suffix=(dep, batch_idx))
        best_splits_of_all_providers = []

        for provider_idx, split_info_table in enumerate(provider_split_info_tables):

            provider_split_info = self.splitter.find_provider_best_split_info(split_info_table, self.get_provider_sitename(provider_idx),
                                                                      self.encrypter,
                                                                      gh_packer=self.packer)
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

        final_best_splits = self.merge_splitinfo(best_split_info_promoter, final_provider_split_info, need_decrypt=False)

        return final_best_splits

    def compute_best_splits(self, cur_to_split_nodes, node_map, dep, batch_idx):

        acc_histograms = self.get_local_histograms(dep, self.data_with_node_assignments, self.grad_and_hess,
                                                   None, cur_to_split_nodes, node_map, ret='tensor',
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
                                              splitinfo_provider=provider_split_info,
                                              merge_provider_split_only=False)

        return cur_best_split

    """
    Federation Functions
    """

    def init_packer_and_sync_gh(self, idx=-1):

        if self.run_cipher_compressing:

            g_min, g_max = None, None
            if self.task_type == consts.REGRESSION:
                self.grad_and_hess.schema = {'header': ['g', 'h']}
                statistics = MultivariateStatisticalSummary(self.grad_and_hess, -1)
                g_min = statistics.get_min()['g']
                g_max = statistics.get_max()['g']

            self.packer = GHPacker(sample_num=self.grad_and_hess.count(),
                                   task_type=self.task_type,
                                   max_sample_weight=self.max_sample_weight,
                                   en_calculator=self.encrypted_mode_calculator,
                                   g_min=g_min,
                                   g_max=g_max)
            en_grad_hess = self.packer.pack_and_encrypt(self.grad_and_hess)

        else:
            en_grad_hess = self.encrypted_mode_calculator.encrypt(self.grad_and_hess)

        LOGGER.info('sending g/h to provider')
        self.transfer_inst.encrypted_grad_and_hess.remote(en_grad_hess,
                                                          role=consts.PROVIDER,
                                                          idx=idx)

    def sync_cur_to_split_nodes(self, cur_to_split_node, dep=-1, idx=-1):

        LOGGER.info("send tree node queue of depth {}".format(dep))
        mask_tree_node_queue = copy.deepcopy(cur_to_split_node)
        for i in range(len(mask_tree_node_queue)):
            mask_tree_node_queue[i] = Node(id=mask_tree_node_queue[i].id,
                                           parent_nodeid=mask_tree_node_queue[i].parent_nodeid,
                                           is_left_node=mask_tree_node_queue[i].is_left_node,)

        self.transfer_inst.tree_node_queue.remote(mask_tree_node_queue,
                                                  role=consts.PROVIDER,
                                                  idx=idx,
                                                  suffix=(dep,))

    def sync_node_positions(self, dep, idx=-1):

        LOGGER.info("send node positions of depth {}".format(dep))
        self.transfer_inst.node_positions.remote(self.inst2node_idx,
                                                 role=consts.PROVIDER,
                                                 idx=idx,
                                                 suffix=(dep,))

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

    def sync_final_split_provider(self, dep=-1, batch=-1, idx=-1):
        LOGGER.info("get provider final splitinfo of depth {}, batch {}".format(dep, batch))
        final_splitinfo_provider = self.transfer_inst.final_splitinfo_provider.get(idx=idx,
                                                                           suffix=(dep, batch,))
        return final_splitinfo_provider if idx == -1 else [final_splitinfo_provider]

    def sync_dispatch_node_provider(self, dispatch_promoter_data, dep=-1, idx=-1):

        LOGGER.info("send node to provider to dispatch, depth is {}".format(dep))
        self.transfer_inst.dispatch_node_provider.remote(dispatch_promoter_data,
                                                     role=consts.PROVIDER,
                                                     idx=idx,
                                                     suffix=(dep,))
        LOGGER.info("get provider dispatch result, depth is {}".format(dep))
        ret = self.transfer_inst.dispatch_node_provider_result.get(idx=idx, suffix=(dep,))
        return ret if idx == -1 else [ret]

    def sync_tree(self, idx=-1):
        LOGGER.info("sync tree to provider")
        tree_nodes = self.remove_sensitive_info()
        self.transfer_inst.tree.remote(tree_nodes,
                                       role=consts.PROVIDER,
                                       idx=idx)

    def sync_predict_finish_tag(self, finish_tag, send_times):
        LOGGER.info("send the {}-th predict finish tag {} to provider".format(finish_tag, send_times))

        self.transfer_inst.predict_finish_tag.remote(finish_tag,
                                                     role=consts.PROVIDER,
                                                     idx=-1,
                                                     suffix=(send_times,))

    def sync_predict_data(self, predict_data, send_times):
        LOGGER.info("send predict data to provider, sending times is {}".format(send_times))
        self.transfer_inst.predict_data.remote(predict_data,
                                               role=consts.PROVIDER,
                                               idx=-1,
                                               suffix=(send_times,))

    def sync_data_predicted_by_provider(self, send_times):
        LOGGER.info("get predicted data by provider, recv times is {}".format(send_times))
        predict_data = self.transfer_inst.predict_data_by_provider.get(idx=-1,
                                                                   suffix=(send_times,))
        return predict_data

    """
    Pre-porcess / Post-Process
    """

    def goss_sampling(self,):
        new_g_h = goss_sampling(self.grad_and_hess, self.top_rate, self.other_rate)
        self.grad_and_hess = new_g_h

    def remove_sensitive_info(self):
        """
        provider is not allowed to get weights/g/h
        """
        new_tree_ = copy.deepcopy(self.tree_node)
        for node in new_tree_:
            node.weight = None
            node.sum_grad = None
            node.sum_hess = None

        return new_tree_

    def initialize_root_node(self,):
        LOGGER.info('initializing root node')
        root_sum_grad, root_sum_hess = self.get_grad_hess_sum(self.grad_and_hess)
        root_node = Node(id=0, sitename=self.sitename, sum_grad=root_sum_grad, sum_hess=root_sum_hess,
                         weight=self.splitter.node_weight(root_sum_grad, root_sum_hess))
        return root_node

    def convert_bin_to_real(self):
        LOGGER.info("convert tree node bins to real value")
        for i in range(len(self.tree_node)):
            if self.tree_node[i].is_leaf is True:
                continue
            if self.tree_node[i].sitename == self.sitename:
                fid = self.decode("feature_idx", self.tree_node[i].fid, split_maskdict=self.split_maskdict)
                bid = self.decode("feature_val", self.tree_node[i].bid, self.tree_node[i].id, self.split_maskdict)
                real_split_val = self.encode("feature_val", self.bin_split_points[fid][bid], self.tree_node[i].id)
                self.tree_node[i].bid = real_split_val

    """
    Tree Updating
    """

    def update_tree(self, split_info, reach_max_depth):

        LOGGER.info("update tree node, splitlist length is {}, tree node queue size is".format(
            len(split_info), len(self.cur_layer_nodes)))
        new_tree_node_queue = []
        for i in range(len(self.cur_layer_nodes)):
            sum_grad = self.cur_layer_nodes[i].sum_grad
            sum_hess = self.cur_layer_nodes[i].sum_hess
            if reach_max_depth or split_info[i].gain <= \
                    self.min_impurity_split + consts.FLOAT_ZERO:  # if reach max_depth, only convert nodes to leaves
                self.cur_layer_nodes[i].is_leaf = True
            else:
                pid = self.cur_layer_nodes[i].id
                self.cur_layer_nodes[i].left_nodeid = self.tree_node_num + 1
                self.cur_layer_nodes[i].right_nodeid = self.tree_node_num + 2
                self.tree_node_num += 2

                left_node = Node(id=self.cur_layer_nodes[i].left_nodeid,
                                 sitename=self.sitename,
                                 sum_grad=split_info[i].sum_grad,
                                 sum_hess=split_info[i].sum_hess,
                                 weight=self.splitter.node_weight(split_info[i].sum_grad, split_info[i].sum_hess),
                                 is_left_node=True,
                                 parent_nodeid=pid)

                right_node = Node(id=self.cur_layer_nodes[i].right_nodeid,
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

                self.cur_layer_nodes[i].sitename = split_info[i].sitename
                if self.cur_layer_nodes[i].sitename == self.sitename:
                    self.cur_layer_nodes[i].fid = self.encode("feature_idx", split_info[i].best_fid)
                    self.cur_layer_nodes[i].bid = self.encode("feature_val", split_info[i].best_bid,
                                                              self.cur_layer_nodes[i].id)
                    self.cur_layer_nodes[i].missing_dir = self.encode("missing_dir",
                                                                      split_info[i].missing_dir,
                                                                      self.cur_layer_nodes[i].id)
                else:
                    self.cur_layer_nodes[i].fid = split_info[i].best_fid
                    self.cur_layer_nodes[i].bid = split_info[i].best_bid

                self.update_feature_importance(split_info[i])

            self.tree_node.append(self.cur_layer_nodes[i])

        self.cur_layer_nodes = new_tree_node_queue

    @staticmethod
    def assign_an_instance(value, tree_=None, decoder=None, sitename=consts.PROMOTER,
                           split_maskdict=None, bin_sparse_points=None,
                           use_missing=False, zero_as_missing=False,
                           missing_dir_maskdict=None):

        unleaf_state, nodeid = value[1]

        if tree_[nodeid].is_leaf is True:
            return tree_[nodeid].id
        else:
            if tree_[nodeid].sitename == sitename:

                next_layer_nid = VertDecisionTreePromoter.go_next_layer(tree_[nodeid], value[0], use_missing,
                                                                       zero_as_missing, bin_sparse_points, split_maskdict,
                                                                       missing_dir_maskdict, decoder)
                return 1, next_layer_nid

            else:
                return (1, tree_[nodeid].fid, tree_[nodeid].bid, tree_[nodeid].sitename,
                        nodeid, tree_[nodeid].left_nodeid, tree_[nodeid].right_nodeid)

    def assign_instances_to_new_node(self, dep, reach_max_depth=False):

        LOGGER.info("redispatch node of depth {}".format(dep))
        dispatch_node_method = functools.partial(self.assign_an_instance,
                                                 tree_=self.tree_node,
                                                 decoder=self.decode,
                                                 sitename=self.sitename,
                                                 split_maskdict=self.split_maskdict,
                                                 bin_sparse_points=self.bin_sparse_points,
                                                 use_missing=self.use_missing,
                                                 zero_as_missing=self.zero_as_missing,
                                                 missing_dir_maskdict=self.missing_dir_maskdict)

        dispatch_promoter_result = self.data_with_node_assignments.mapValues(dispatch_node_method)
        LOGGER.info("remask dispatch node result of depth {}".format(dep))

        dispatch_to_provider_result = dispatch_promoter_result.filter(
            lambda key, value: isinstance(value, tuple) and len(value) > 2)

        dispatch_promoter_result = dispatch_promoter_result.subtractByKey(dispatch_to_provider_result)
        leaf = dispatch_promoter_result.filter(lambda key, value: isinstance(value, tuple) is False)

        if self.sample_leaf_pos is None:
            self.sample_leaf_pos = leaf
        else:
            self.sample_leaf_pos = self.sample_leaf_pos.union(leaf)

        if reach_max_depth:  # if reach max_depth only update weight samples
            return

        dispatch_promoter_result = dispatch_promoter_result.subtractByKey(leaf)
        dispatch_node_provider_result = self.sync_dispatch_node_provider(dispatch_to_provider_result, dep)

        self.inst2node_idx = None
        for idx in range(len(dispatch_node_provider_result)):
            if self.inst2node_idx is None:
                self.inst2node_idx = dispatch_node_provider_result[idx]
            else:
                self.inst2node_idx = self.inst2node_idx.join(dispatch_node_provider_result[idx],
                                                             lambda unleaf_state_nodeid1, unleaf_state_nodeid2:
                                                             unleaf_state_nodeid1 if len(
                                                             unleaf_state_nodeid1) == 2 else unleaf_state_nodeid2)

        self.inst2node_idx = self.inst2node_idx.union(dispatch_promoter_result)

    def assign_instance_to_leaves_and_update_weights(self):
        # re-assign samples to leaf nodes and update weights
        self.update_tree([], True)
        self.update_instances_node_positions()
        self.assign_instances_to_new_node(self.max_depth, reach_max_depth=True)

    def update_instances_node_positions(self):
        self.data_with_node_assignments = self.data_bin.join(self.inst2node_idx, lambda data_inst, dispatch_info: (
            data_inst, dispatch_info))

    """
    Fit & Predict
    """

    def fit(self):

        LOGGER.info('fitting a promoter decision tree')

        self.init_packer_and_sync_gh()
        root_node = self.initialize_root_node()
        self.cur_layer_nodes = [root_node]
        self.inst2node_idx = self.assign_instance_to_root_node(self.data_bin, root_node_id=root_node.id)

        for dep in range(self.max_depth):

            LOGGER.info('At dep {}, cur layer has {} nodes'.format(dep, len(self.cur_layer_nodes)))

            self.sync_cur_to_split_nodes(self.cur_layer_nodes, dep)

            if len(self.cur_layer_nodes) == 0:
                break

            self.sync_node_positions(dep)
            self.update_instances_node_positions()

            split_info = []
            for batch_idx, i in enumerate(range(0, len(self.cur_layer_nodes), self.max_split_nodes)):

                self.cur_to_split_nodes = self.cur_layer_nodes[i: i + self.max_split_nodes]
                node_map = self.get_node_map(self.cur_to_split_nodes)

                if self.new_ver:
                    cur_splitinfos = self.compute_best_splits2(self.cur_to_split_nodes, node_map, dep, batch_idx)
                else:
                    cur_splitinfos = self.compute_best_splits(self.cur_to_split_nodes, node_map, dep, batch_idx)

                split_info.extend(cur_splitinfos)

            self.update_tree(split_info, False)
            self.assign_instances_to_new_node(dep)

        if self.cur_layer_nodes:
            self.assign_instance_to_leaves_and_update_weights()

        self.convert_bin_to_real()
        self.round_leaf_val()
        self.sync_tree()
        self.sample_weights_post_process()
        LOGGER.info("fitting promoter decision tree done")


    @staticmethod
    def traverse_tree(predict_state, data_inst, tree_=None,
                      decoder=None, sitename=consts.PROMOTER, split_maskdict=None,
                      use_missing=None, zero_as_missing=None, missing_dir_maskdict=None, return_leaf_id=False):

        nid, tag = predict_state

        while tree_[nid].sitename == sitename:

            if tree_[nid].is_leaf is True:
                return tree_[nid].weight if not return_leaf_id else nid

            nid = DecisionTree.go_next_layer(tree_[nid], data_inst, use_missing, zero_as_missing,
                                             None, split_maskdict, missing_dir_maskdict, decoder)

        return nid, 1

    def predict(self, data_inst):

        LOGGER.info("start to predict!")
        predict_data = data_inst.mapValues(lambda inst: (0, 1))
        site_provider_send_times = 0
        predict_result = None

        while True:
            traverse_tree = functools.partial(self.traverse_tree,
                                              tree_=self.tree_node,
                                              decoder=self.decode,
                                              sitename=self.sitename,
                                              split_maskdict=self.split_maskdict,
                                              use_missing=self.use_missing,
                                              zero_as_missing=self.zero_as_missing,
                                              missing_dir_maskdict=self.missing_dir_maskdict,
                                              return_leaf_id=False)

            predict_data = predict_data.join(data_inst, traverse_tree)
            predict_leaf = predict_data.filter(lambda key, value: isinstance(value, tuple) is False)
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

            predict_data_provider = self.sync_data_predicted_by_provider(site_provider_send_times)
            for i in range(len(predict_data_provider)):
                predict_data = predict_data.join(predict_data_provider[i],
                                                 lambda state1_nodeid1, state2_nodeid2:
                                                 state1_nodeid1 if state1_nodeid1[
                                                                       1] == 0 else state2_nodeid2)

            site_provider_send_times += 1

        LOGGER.info("predict finish!")
        return predict_result

    """
    Tree output
    """

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
        for node in self.tree_node:
            model_param.tree_.add(id=node.id,
                                      sitename=node.sitename,
                                      fid=node.fid,
                                      bid=node.bid,
                                      weight=node.weight,
                                      is_leaf=node.is_leaf,
                                      left_nodeid=node.left_nodeid,
                                      right_nodeid=node.right_nodeid,
                                      missing_dir=node.missing_dir)

        model_param.split_maskdict.update(self.split_maskdict)
        model_param.missing_dir_maskdict.update(self.missing_dir_maskdict)
        model_param.leaf_count.update(self.leaf_count)
        return model_param

    def set_model_param(self, model_param):
        self.tree_node = []
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

            self.tree_node.append(_node)

        self.split_maskdict = dict(model_param.split_maskdict)
        self.missing_dir_maskdict = dict(model_param.missing_dir_maskdict)

