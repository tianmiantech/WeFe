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


import functools

import numpy as np

from common.python.utils import log_utils
from kernel.components.boosting import DecisionTree
from kernel.components.boosting import Node
from kernel.components.boosting import SplitInfo
from kernel.components.boosting.core.splitinfo_cipher_compressor import ProviderSplitInfoCompressor
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.transfer.variables.transfer_class.vert_decision_tree_transfer_variable import \
    VertDecisionTreeTransferVariable
from kernel.utils import consts
from kernel.utils.data_util import NoneType

LOGGER = log_utils.get_logger()


class VertDecisionTreeProvider(DecisionTree):
    def __init__(self, tree_param):
        LOGGER.info("vert decision tree promoter init!")
        super(VertDecisionTreeProvider, self).__init__(tree_param)

        self.data_bin_with_position = None
        self.grad_and_hess = None
        self.infos = None
        self.pubkey = None
        self.privakey = None
        self.tree_id = None
        self.encrypted_grad_and_hess = None
        self.transfer_inst = VertDecisionTreeTransferVariable()
        self.tree_node_queue = None
        self.cur_split_nodes = None
        self.missing_dir_mask_left = {}  # mask for left direction
        self.missing_dir_mask_right = {}  # mask for right direction
        self.split_maskdict = {}  # mask for split value
        self.missing_dir_maskdict = {}
        self.tree_node_num = 0
        self.sitename = consts.PROVIDER
        self.node_dispatch = None
        self.provider_member_idlist = []

        self.complete_secure_tree = False
        # For fast histogram
        self.run_sparse_opt = False
        self.bin_num = None
        self.data_bin_dense = None
        self.data_bin_dense_with_position = None

        # goss subsample
        self.run_goss = False

        # transfer variable
        self.transfer_inst = VertDecisionTreeTransferVariable()

        # cipher compressing
        self.cipher_compressor = None
        self.run_cipher_compressing = False
        self.key_length = None
        self.round_decimal = 7

        # code version control
        self.new_ver = True

    def init_compressor(self):
        para = self.transfer_inst.cipher_compressor_para.get(idx=0)
        max_sample_weight, max_capcity_int, en_type = para['max_sample_weight'], para['max_capacity_int'], para[
            'en_type']
        LOGGER.info(
            'got para from promoter: max sample weight {}; max capacity int {}; en type {}'.format(max_sample_weight,
                                                                                                   max_capcity_int,
                                                                                                   en_type))
        self.cipher_compressor = ProviderSplitInfoCompressor(max_capcity_int, en_type, consts.CLASSIFICATION,
                                                             round_decimal=self.round_decimal,
                                                             max_sample_weights=max_sample_weight)

    def report_init_status(self):

        LOGGER.info('reporting initialization status')
        LOGGER.info('using new version code {}'.format(self.new_ver))
        if self.run_sparse_opt:
            LOGGER.info('running sparse optimization')
        if self.complete_secure_tree:
            LOGGER.info('running complete secure')
        if self.run_goss:
            LOGGER.info('running goss')
        if self.run_cipher_compressing:
            LOGGER.info('running cipher compressing')
            LOGGER.info('round decimal is {}'.format(self.round_decimal))
        LOGGER.debug('bin num and feature num: {}/{}'.format(self.bin_num, self.feature_num))

    def init(self, flowid, runtime_idx, data_bin, bin_split_points, bin_sparse_points, data_bin_dense, bin_num,
             valid_features,
             complete_secure=False,
             goss_subsample=False,
             run_sprase_opt=False,
             cipher_compressing=False,
             round_decimal=7,
             new_ver=True):

        super(VertDecisionTreeProvider, self).init_variables(flowid, runtime_idx, data_bin, bin_split_points,
                                                             bin_sparse_points, valid_features)

        self.check_max_split_nodes()
        self.complete_secure_tree = complete_secure
        self.run_goss = goss_subsample
        self.run_sparse_opt = run_sprase_opt
        self.data_bin_dense = data_bin_dense
        self.bin_num = bin_num
        self.run_cipher_compressing = cipher_compressing
        self.round_decimal = round_decimal
        self.feature_num = self.bin_split_points.shape[0]

        if self.run_cipher_compressing:
            self.init_compressor()

        self.new_ver = new_ver

        self.report_init_status()

    def set_flowid(self, flowid=0):
        LOGGER.info("set flowid, flowid is {}".format(flowid))
        self.transfer_inst.set_flowid(flowid)

    def set_provider_member_idlist(self, provider_member_idlist):
        self.provider_member_idlist = provider_member_idlist

    # def set_runtime_idx(self, runtime_idx):
    #     self.runtime_idx = runtime_idx
    #     self.sitename = ":".join([consts.PROVIDER, str(self.runtime_idx)])

    def set_inputinfo(self, data_bin=None, grad_and_hess=None, bin_split_points=None, bin_sparse_points=None):
        LOGGER.info("set input info")
        self.data_bin = data_bin
        self.grad_and_hess = grad_and_hess
        self.bin_split_points = bin_split_points
        self.bin_sparse_points = bin_sparse_points
        # self.data_bin_dense = data_bin_dense

    def set_valid_features(self, valid_features=None):
        LOGGER.info("set valid features")
        self.valid_features = valid_features

    def activate_sparse_hist_opt(self):
        self.run_sparse_opt = True

    def set_dense_data_for_sparse_opt(self, data_bin_dense, bin_num):
        # a dense dtable and bin_num for fast hist computation
        self.data_bin_dense = data_bin_dense
        self.bin_num = bin_num

    def encode_split_info(self, split_info_list):

        final_split_info = []
        for i, split_info in enumerate(split_info_list):

            if split_info.best_fid != -1:
                LOGGER.debug('sitename is {}, self.sitename is {}'
                             .format(split_info.sitename, self.sitename))
                assert split_info.sitename == self.sitename
                split_info.best_fid = self.encode("feature_idx", split_info.best_fid)
                assert split_info.best_fid is not None
                split_info.best_bid = self.encode("feature_val", split_info.best_bid, self.cur_split_nodes[i].id)
                split_info.missing_dir = self.encode("missing_dir", split_info.missing_dir, self.cur_split_nodes[i].id)
                split_info.mask_id = None
            else:
                LOGGER.debug('this node can not be further split by provider feature: {}'.format(split_info))

            final_split_info.append(split_info)

        return final_split_info

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

    def sync_encrypted_grad_and_hess(self):
        LOGGER.info("get encrypted grad and hess")
        self.grad_and_hess = self.transfer_inst.encrypted_grad_and_hess.get(idx=0)
        """
        self.grad_and_hess = federation.get(name=self.transfer_inst.encrypted_grad_and_hess.name,
                                            tag=self.transfer_inst.generate_transferid(
                                                self.transfer_inst.encrypted_grad_and_hess),
                                            idx=0)
        """

    def sync_node_positions(self, dep=-1):
        LOGGER.info("get node positions of depth {}".format(dep))
        node_positions = self.transfer_inst.node_positions.get(idx=0,
                                                               suffix=(dep,))
        """
        node_positions = federation.get(name=self.transfer_inst.node_positions.name,
                                        tag=self.transfer_inst.generate_transferid(self.transfer_inst.node_positions,
                                                                                   dep),
                                        idx=0)
        """
        return node_positions

    def sync_tree_node_queue(self, dep=-1):
        LOGGER.info("get tree node queue of depth {}".format(dep))
        self.tree_node_queue = self.transfer_inst.tree_node_queue.get(idx=0,
                                                                      suffix=(dep,))
        """
        self.tree_node_queue = federation.get(name=self.transfer_inst.tree_node_queue.name,
                                              tag=self.transfer_inst.generate_transferid(
                                                  self.transfer_inst.tree_node_queue, dep),
                                              idx=0)
        """

    def sync_encrypted_splitinfo_provider(self, encrypted_splitinfo_provider, dep=-1, batch=-1):
        LOGGER.info("send encrypted splitinfo of depth {}, batch {}".format(dep, batch))

        self.transfer_inst.encrypted_splitinfo_provider.remote(encrypted_splitinfo_provider,
                                                               role=consts.PROMOTER,
                                                               idx=-1,
                                                               suffix=(dep, batch,))
        """
        self.transfer_inst.encrypted_splitinfo_provider.remote(encrypted_splitinfo_provider,
                                                           role=consts.PROMOTER,
                                                           idx=-1,
                                                           suffix=(dep, batch,))
        """

    def sync_federated_best_splitinfo_provider(self, dep=-1, batch=-1):
        LOGGER.info("get federated best splitinfo of depth {}, batch {}".format(dep, batch))
        federated_best_splitinfo_provider = self.transfer_inst.federated_best_splitinfo_provider.get(idx=0,
                                                                                                     suffix=(
                                                                                                         dep, batch,))
        """
        federated_best_splitinfo_provider = federation.get(name=self.transfer_inst.federated_best_splitinfo_provider.name,
                                                       tag=self.transfer_inst.generate_transferid(
                                                           self.transfer_inst.federated_best_splitinfo_provider, dep,
                                                           batch),
                                                       idx=0)
        """

        return federated_best_splitinfo_provider

    def sync_final_splitinfo_provider(self, splitinfo_provider, federated_best_splitinfo_provider, dep=-1, batch=-1):
        LOGGER.info("send provider final splitinfo of depth {}, batch {}".format(dep, batch))
        final_splitinfos = []
        for i in range(len(splitinfo_provider)):
            best_idx, best_gain = federated_best_splitinfo_provider[i]
            if best_idx != -1:
                assert splitinfo_provider[i][best_idx].sitename == self.sitename
                splitinfo = splitinfo_provider[i][best_idx]
                splitinfo.best_fid = self.encode("feature_idx", splitinfo.best_fid)
                assert splitinfo.best_fid is not None
                splitinfo.best_bid = self.encode("feature_val", splitinfo.best_bid, self.cur_split_nodes[i].id)
                splitinfo.missing_dir = self.encode("missing_dir", splitinfo.missing_dir, self.cur_split_nodes[i].id)
                splitinfo.gain = best_gain
            else:
                splitinfo = SplitInfo(sitename=self.sitename, best_fid=-1, best_bid=-1, gain=best_gain)

            final_splitinfos.append(splitinfo)

        self.transfer_inst.final_splitinfo_provider.remote(final_splitinfos,
                                                           role=consts.PROMOTER,
                                                           idx=-1,
                                                           suffix=(dep, batch,))

        """
        federation.remote(obj=final_splitinfos,
                          name=self.transfer_inst.final_splitinfo_provider.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.final_splitinfo_provider, dep,
                                                                     batch),
                          role=consts.PROMOTER,
                          idx=-1)
        """

    def sync_dispatch_node_provider(self, dep):
        LOGGER.info("get node from provider to dispath, depth is {}".format(dep))
        dispatch_node_provider = self.transfer_inst.dispatch_node_provider.get(idx=0,
                                                                               suffix=(dep,))
        """
        dispatch_node_provider = federation.get(name=self.transfer_inst.dispatch_node_provider.name,
                                            tag=self.transfer_inst.generate_transferid(
                                                self.transfer_inst.dispatch_node_provider, dep),
                                            idx=0)
        """
        return dispatch_node_provider

    @staticmethod
    def dispatch_node(value1, value2, sitename=None, decoder=None,
                      split_maskdict=None, bin_sparse_points=None,
                      use_missing=False, zero_as_missing=False,
                      missing_dir_maskdict=None):

        unleaf_state, fid, bid, node_sitename, nodeid, left_nodeid, right_nodeid = value1
        if node_sitename != sitename:
            return value1

        fid = decoder("feature_idx", fid, split_maskdict=split_maskdict)
        bid = decoder("feature_val", bid, nodeid, split_maskdict=split_maskdict)
        if not use_missing:
            if value2.features.get_data(fid, bin_sparse_points[fid]) <= bid:
                return unleaf_state, left_nodeid
            else:
                return unleaf_state, right_nodeid
        else:
            missing_dir = decoder("missing_dir", 1, nodeid,
                                  missing_dir_maskdict=missing_dir_maskdict)
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

    def sync_dispatch_node_provider_result(self, dispatch_node_provider_result, dep=-1):
        LOGGER.info("send provider dispatch result, depth is {}".format(dep))

        self.transfer_inst.dispatch_node_provider_result.remote(dispatch_node_provider_result,
                                                                role=consts.PROMOTER,
                                                                idx=-1,
                                                                suffix=(dep,))

        """
        federation.remote(obj=dispatch_node_provider_result,
                          name=self.transfer_inst.dispatch_node_provider_result.name,
                          tag=self.transfer_inst.generate_transferid(self.transfer_inst.dispatch_node_provider_result, dep),
                          role=consts.PROMOTER,
                          idx=-1)
        """

    def find_dispatch(self, dispatch_node_provider, dep=-1):
        LOGGER.info("start to find provider dispath of depth {}".format(dep))
        dispatch_node_method = functools.partial(self.dispatch_node,
                                                 sitename=self.sitename,
                                                 decoder=self.decode,
                                                 split_maskdict=self.split_maskdict,
                                                 bin_sparse_points=self.bin_sparse_points,
                                                 use_missing=self.use_missing,
                                                 zero_as_missing=self.zero_as_missing,
                                                 missing_dir_maskdict=self.missing_dir_maskdict)
        dispatch_node_provider_result = dispatch_node_provider.join(self.data_bin, dispatch_node_method, need_send=True)
        self.sync_dispatch_node_provider_result(dispatch_node_provider_result, dep)

    def sync_tree(self):
        LOGGER.info("sync tree from promoter")
        self.tree_ = self.transfer_inst.tree.get(idx=0)
        """
        self.tree_ = federation.get(name=self.transfer_inst.tree.name,
                                    tag=self.transfer_inst.generate_transferid(self.transfer_inst.tree),
                                    idx=0)
        """

    def remove_duplicated_split_nodes(self, split_nid_used):
        LOGGER.info("remove duplicated nodes from split mask dict")
        duplicated_nodes = set(self.split_maskdict.keys()) - set(split_nid_used)
        for nid in duplicated_nodes:
            del self.split_maskdict[nid]

    def convert_bin_to_real(self):
        LOGGER.info("convert tree node bins to real value")
        split_nid_used = []
        for i in range(len(self.tree_)):
            if self.tree_[i].is_leaf is True:
                continue

            if self.tree_[i].sitename == self.sitename:
                fid = self.decode("feature_idx", self.tree_[i].fid, split_maskdict=self.split_maskdict)
                bid = self.decode("feature_val", self.tree_[i].bid, self.tree_[i].id, self.split_maskdict)
                LOGGER.debug("shape of bin_split_points is {}".format(len(self.bin_split_points[fid])))
                real_splitval = self.encode("feature_val", self.bin_split_points[fid][bid], self.tree_[i].id)
                self.tree_[i].bid = real_splitval

                split_nid_used.append(self.tree_[i].id)

        self.remove_duplicated_split_nodes(split_nid_used)

    @staticmethod
    def traverse_tree(predict_state, data_inst, tree_=None,
                      decoder=None, split_maskdict=None, sitename=consts.PROVIDER,
                      use_missing=False, zero_as_missing=False,
                      missing_dir_maskdict=None):

        nid, _ = predict_state
        if tree_[nid].sitename != sitename:
            return predict_state

        while tree_[nid].sitename == sitename:
            fid = decoder("feature_idx", tree_[nid].fid, split_maskdict=split_maskdict)
            bid = decoder("feature_val", tree_[nid].bid, nid, split_maskdict)

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

    def update_instances_node_positions(self):

        # join data and node_dispatch to update current node positions of samples
        if self.run_sparse_opt:
            self.data_bin_dense_with_position = self.data_bin_dense.join(self.node_dispatch,
                                                                         lambda v1, v2: (v1, v2))
        else:
            self.data_bin_with_position = self.data_bin.join(self.node_dispatch, lambda v1, v2: (v1, v2))

    def get_computing_node_dispatch(self):
        if self.run_goss:
            node_dispatch = self.node_dispatch.join(self.grad_and_hess, lambda x1, x2: x1)
        else:
            node_dispatch = self.node_dispatch
        return node_dispatch

    def compute_best_splits2(self, node_map, dep, batch):

        LOGGER.info('solving node batch {}, node num is {}'.format(batch, len(self.cur_split_nodes)))
        if not self.complete_secure_tree:

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

            if self.run_cipher_compressing:
                self.cipher_compressor.renew_compressor(node_sample_count, node_map)
            cipher_compressor = self.cipher_compressor if self.run_cipher_compressing else None

            split_info_table = self.splitter.provider_prepare_split_points(histograms=acc_histograms,
                                                                           use_missing=self.use_missing,
                                                                           valid_features=self.valid_features,
                                                                           sitename=self.sitename,
                                                                           left_missing_dir=self.missing_dir_mask_left[
                                                                               dep],
                                                                           right_missing_dir=
                                                                           self.missing_dir_mask_right[
                                                                               dep],
                                                                           mask_id_mapping=self.fid_bid_random_mapping,
                                                                           batch_size=self.bin_num,
                                                                           cipher_compressor=cipher_compressor,
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
            return_split_info = self.encode_split_info(unmasked_split_info)
            self.transfer_inst.final_splitinfo_provider.remote(return_split_info,
                                                               role=consts.PROMOTER,
                                                               idx=-1,
                                                               suffix=(dep, batch,))
        else:
            LOGGER.debug('skip splits computation')

    def compute_best_splits(self, node_map: dict, dep: int, batch: int):

        if not self.complete_secure_tree:
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

            LOGGER.debug('sending en_splitinfo {}'.format(encrypted_splitinfo_provider))
            self.sync_encrypted_splitinfo_provider(encrypted_splitinfo_provider, dep, batch)
            federated_best_splitinfo_provider = self.sync_federated_best_splitinfo_provider(dep, batch)
            self.sync_final_splitinfo_provider(splitinfo_provider, federated_best_splitinfo_provider, dep, batch)
            LOGGER.debug('computing provider splits done')
        else:
            LOGGER.debug('skip splits computation')

    def generate_missing_dir(self, dep, left_num=3, right_num=3):
        """
        randomly generate missing dir mask
        """
        rn = np.random.choice(range(left_num + right_num), left_num + right_num, replace=False)
        left_dir = rn[0:left_num]
        right_dir = rn[left_num:]
        self.missing_dir_mask_left[dep] = left_dir
        self.missing_dir_mask_right[dep] = right_dir

    @staticmethod
    def generate_fid_bid_random_mapping(feature_num, bin_num):

        total_id_num = feature_num * bin_num

        mapping = {}
        idx = 0
        id_list = np.random.choice(range(total_id_num), total_id_num, replace=False)
        for fid in range(feature_num):
            for bid in range(bin_num):
                mapping[(fid, bid)] = int(id_list[idx])
                idx += 1

        return mapping

    def generate_split_point_masking_variable(self, dep):
        # for split point masking
        self.generate_missing_dir(dep, 5, 5)
        self.fid_bid_random_mapping = self.generate_fid_bid_random_mapping(self.feature_num, self.bin_num)
        self.inverse_fid_bid_random_mapping = {v: k for k, v in self.fid_bid_random_mapping.items()}

    def unmask_split_info(self, split_info_list, inverse_mask_id_mapping, left_missing_dir, right_missing_dir):

        for split_info in split_info_list:
            if split_info.mask_id is not None:
                fid, bid = inverse_mask_id_mapping[split_info.mask_id]
                split_info.best_fid, split_info.best_bid = fid, bid
                masked_missing_dir = split_info.missing_dir
                if masked_missing_dir in left_missing_dir:
                    split_info.missing_dir = -1
                elif masked_missing_dir in right_missing_dir:
                    split_info.missing_dir = 1

        return split_info_list

    def fit(self):
        LOGGER.info("begin to fit provider decision tree")
        self.sync_encrypted_grad_and_hess()

        for dep in range(self.max_depth):
            self.sync_tree_node_queue(dep)
            self.generate_split_point_masking_variable(dep)

            if len(self.tree_node_queue) == 0:
                break

            self.node_dispatch = self.sync_node_positions(dep)
            self.update_instances_node_positions()

            batch = 0
            for i in range(0, len(self.tree_node_queue), self.max_split_nodes):
                self.cur_split_nodes = self.tree_node_queue[i: i + self.max_split_nodes]
                if self.new_ver:
                    self.compute_best_splits2(node_map=self.get_node_map(self.cur_split_nodes),
                                              dep=dep, batch=batch)
                else:
                    self.compute_best_splits(node_map=self.get_node_map(self.cur_split_nodes), dep=dep, batch=batch)

                batch += 1

            dispatch_node_provider = self.sync_dispatch_node_provider(dep)
            self.find_dispatch(dispatch_node_provider, dep)

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
                                              decoder=self.decode,
                                              split_maskdict=self.split_maskdict,
                                              sitename=self.sitename,
                                              use_missing=self.use_missing,
                                              zero_as_missing=self.zero_as_missing,
                                              missing_dir_maskdict=self.missing_dir_maskdict)
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
