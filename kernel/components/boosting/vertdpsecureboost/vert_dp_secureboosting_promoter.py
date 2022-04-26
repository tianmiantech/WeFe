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
from operator import itemgetter
from typing import List

import numpy as np
from numpy import random

from common.python.utils import log_utils
from kernel.components.binning.core.quantile_binning import QuantileBinning
from kernel.components.binning.vertfeaturebinning.param import FeatureBinningParam
from kernel.components.boosting import BoostingTree
from kernel.components.boosting.vertdpsecureboost.vert_dp_decision_tree_promoter import VertDPDecisionTreePromoter
from kernel.components.boosting.core.predict_cache import PredictDataCache
from kernel.components.boosting.param import VertDPSecureBoostParam
from kernel.components.evaluation.param import EvaluateParam
from kernel.optimizer.convergence import converge_func_factory
from kernel.optimizer.loss import FairLoss
from kernel.optimizer.loss import HuberLoss
from kernel.optimizer.loss import LeastAbsoluteErrorLoss
from kernel.optimizer.loss import LogCoshLoss
from kernel.optimizer.loss import SigmoidBinaryCrossEntropyLoss
from kernel.optimizer.loss import SoftmaxCrossEntropyLoss
from kernel.optimizer.loss import TweedieLoss
from kernel.optimizer.loss.regression_loss import LeastSquaredErrorLoss
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import BoostingTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta, CriterionMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import ObjectiveMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import QuantileMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import BoostingTreeModelParam
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import FeatureImportanceInfo
from kernel.transfer.variables.transfer_class.vert_dp_secure_boost_transfer_variable import \
    VertDPSecureBoostingTransferVariable
from kernel.utils import consts
from kernel.utils.anonymous_generator import generate_anonymous
from kernel.utils.data_util import NoneType, with_weight, get_max_sample_weight
from kernel.utils.io_check import assert_io_num_rows_equal
from kernel.utils.label_checker import ClassifyLabelChecker
from kernel.utils.label_checker import RegressionLabelChecker

LOGGER = log_utils.get_logger()


class VertDPSecureBoostingPromoter(BoostingTree):
    def __init__(self):
        super(VertDPSecureBoostingPromoter, self).__init__()

        self.convegence = None
        self.y = None
        self.F = None
        self.predict_F = None
        self.data_bin = None
        self.loss = None
        self.init_score = None
        self.classes_dict = {}
        self.classes_ = []
        self.num_classes = 0
        self.classify_target = "binary"
        self.feature_num = None
        self.encrypter = None
        self.grad_and_hess = None
        self.tree_dim = 1
        self.tree_meta = None
        self.trees_ = []
        self.history_loss = []
        self.bin_split_points = None
        self.bin_sparse_points = None
        self.encrypted_mode_calculator = None
        self.predict_data_cache = PredictDataCache()

        self.feature_importances_ = {}
        self.role = consts.PROMOTER
        self.model_param = VertDPSecureBoostParam()
        self.transfer_variable = VertDPSecureBoostingTransferVariable()
        self.model_save_to_storage = True
        self.max_sample_weight = 1
        self.max_sample_weight_computed = False

        self.enable_goss = False  # GOSS
        self.top_rate = None
        self.other_rate = None
        self.epsilon = None
        self.feature_sitenames = []

    def _init_model(self, param: VertDPSecureBoostParam):

        super(VertDPSecureBoostingPromoter, self)._init_model(param)
        self.enable_goss = param.run_goss
        self.top_rate = param.top_rate
        self.other_rate = param.other_rate
        self.epsilon = param.epsilon

    def set_loss(self, objective_param):
        loss_type = objective_param.objective
        params = objective_param.params
        LOGGER.info("set objective, objective is {}".format(loss_type))
        if self.task_type == consts.CLASSIFICATION:
            if loss_type == "cross_entropy":
                if self.num_classes == 2:
                    self.loss = SigmoidBinaryCrossEntropyLoss()
                else:
                    self.loss = SoftmaxCrossEntropyLoss()
            else:
                raise NotImplementedError("objective %s not supported yet" % (loss_type))
        elif self.task_type == consts.REGRESSION:
            if loss_type == "lse":
                self.loss = LeastSquaredErrorLoss()
            elif loss_type == "lae":
                self.loss = LeastAbsoluteErrorLoss()
            elif loss_type == "huber":
                self.loss = HuberLoss(params[0])
            elif loss_type == "fair":
                self.loss = FairLoss(params[0])
            elif loss_type == "tweedie":
                self.loss = TweedieLoss(params[0])
            elif loss_type == "log_cosh":
                self.loss = LogCoshLoss()
            else:
                raise NotImplementedError("objective %s not supported yet" % (loss_type))
        else:
            raise NotImplementedError("objective %s not supported yet" % (loss_type))

    def convert_feature_to_bin(self, data_instance):
        LOGGER.info("convert feature to bins")
        param_obj = FeatureBinningParam(bin_num=self.bin_num)

        if self.use_missing:
            binning_obj = QuantileBinning(param_obj, abnormal_list=[NoneType()])
        else:
            binning_obj = QuantileBinning(param_obj)

        binning_obj.fit_split_points(data_instance)
        self.data_bin, self.bin_split_points, self.bin_sparse_points = binning_obj.convert_feature_to_bin(data_instance)
        LOGGER.info("convert feature to bins over")

        privider_data_bins = self.sync_provider_data_bin_with_dp()
        privider_split_points = self.sync_provider_bin_split_points()
        promoter_sitename = ":".join([consts.PROMOTER, str(self.component_properties.local_member_id,)])
        feature_num = self.bin_split_points.shape[0]
        self.feature_sitenames = [(feature_num,promoter_sitename)]
        for data_bin in privider_data_bins:
            self.data_bin = binning_obj.merge_data_bins(self.data_bin, data_bin)
        for bin_shape, bin_sparse_points, sitename in privider_split_points:
            self.bin_split_points = np.zeros((self.bin_split_points.shape[0]+bin_shape[0],self.bin_split_points.shape[1]))
            for k,v in bin_sparse_points.items():
                self.bin_sparse_points[k+feature_num] = v
            feature_num += bin_shape[0]
            self.feature_sitenames.append((feature_num,sitename))

    def set_y(self):
        LOGGER.info("set label from data and check label")
        self.y = self.data_bin.mapValues(lambda instance: instance.label)
        self.check_label()

    def generate_flowid(self, round_num, tree_num):
        LOGGER.info("generate flowid, flowid {}".format(self.flowid))
        return ".".join(map(str, [self.flowid, round_num, tree_num]))

    def check_label(self):
        LOGGER.info("check label")
        if self.task_type == consts.CLASSIFICATION:
            self.num_classes, self.classes_ = ClassifyLabelChecker.validate_label(self.data_bin)
            if self.num_classes > 2:
                self.classify_target = "multinomial"
                self.tree_dim = self.num_classes

            range_from_zero = True
            for _class in self.classes_:
                try:
                    if _class >= 0 and _class < self.num_classes and isinstance(_class, int):
                        continue
                    else:
                        range_from_zero = False
                        break
                except:
                    range_from_zero = False

            self.classes_ = sorted(self.classes_)
            if not range_from_zero:
                class_mapping = dict(zip(self.classes_, range(self.num_classes)))
                self.y = self.y.mapValues(lambda _class: class_mapping[_class])

        else:
            RegressionLabelChecker.validate_label(self.data_bin)

        self.set_loss(self.objective_param)

    @staticmethod
    def accumulate_f(f_val, new_f_val, lr=0.1, idx=0):
        f_val_copy = copy.deepcopy(f_val)
        f_val_copy[idx] += lr * new_f_val
        return f_val_copy

    def update_feature_importance(self, tree_feature_importance):
        for fid in tree_feature_importance:
            if fid not in self.feature_importances_:
                self.feature_importances_[fid] = tree_feature_importance[fid]
            else:
                self.feature_importances_[fid] += tree_feature_importance[fid]
        LOGGER.debug('cur feature importance {}'.format(self.feature_importances_))

    def init_predict_score(self):
        if self.tree_dim > 1:
            self.F, self.init_score = self.loss.initialize(self.y, self.tree_dim)
        else:
            self.F, self.init_score = self.loss.initialize(self.y)

    def update_predict_score(self, new_f=None, dim=0):
        func = functools.partial(self.accumulate_f, lr=self.learning_rate, idx=dim)
        self.F = self.F.join(new_f, func)

    def process_sample_weights(self, grad_and_hess, data_with_sample_weight=None):

        # add sample weights to gradient and hessian
        if data_with_sample_weight is not None:
            if with_weight(data_with_sample_weight):
                LOGGER.info('weighted sample detected, multiply g/h by weights')
                grad_and_hess = grad_and_hess.join(data_with_sample_weight,
                                                   lambda v1, v2: (v1[0] * v2.weight, v1[1] * v2.weight))
                if not self.max_sample_weight_computed:
                    self.max_sample_weight = get_max_sample_weight(data_with_sample_weight)
                    LOGGER.info('max sample weight is {}'.format(self.max_sample_weight))
                    self.max_sample_weight_computed = True

        return grad_and_hess

    def compute_grad_and_hess(self, data_with_sample_weight=None):
        LOGGER.info("compute grad and hess")
        loss_method = self.loss
        if self.task_type == consts.CLASSIFICATION:
            self.grad_and_hess = self.y.join(self.F, lambda y, f_val: \
                (loss_method.compute_grad(y, loss_method.predict(f_val)), \
                 loss_method.compute_hess(y, loss_method.predict(f_val))))
        else:
            self.grad_and_hess = self.y.join(self.F, lambda y, f_val:
            (loss_method.compute_grad(y, f_val),
             loss_method.compute_hess(y, f_val)))

        self.grad_and_hess = self.process_sample_weights(self.grad_and_hess, data_with_sample_weight)

    def compute_loss(self):
        LOGGER.info("compute loss")
        if self.task_type == consts.CLASSIFICATION:
            loss_method = self.loss
            y_predict = self.F.mapValues(lambda val: loss_method.predict(val))
            loss = loss_method.compute_loss(self.y, y_predict)
        elif self.task_type == consts.REGRESSION:
            if self.objective_param.objective in ["lse", "lae", "logcosh", "tweedie", "log_cosh", "huber"]:
                loss_method = self.loss
                loss = loss_method.compute_loss(self.y, self.F)
            else:
                loss_method = self.loss
                y_predict = self.F.mapValues(lambda val: loss_method.predict(val))
                loss = loss_method.compute_loss(self.y, y_predict)

        return float(loss)

    def get_grad_and_hess(self, tree_idx):
        LOGGER.info("get grad and hess of tree {}".format(tree_idx))
        grad_and_hess_subtree = self.grad_and_hess.mapValues(
            lambda grad_and_hess: (grad_and_hess[0][tree_idx], grad_and_hess[1][tree_idx]))
        return grad_and_hess_subtree

    def check_convergence(self, loss):
        LOGGER.info("check convergence")
        if self.convegence is None:
            self.convegence = converge_func_factory("diff", self.tol)

        return self.convegence.is_converge(loss)

    def sample_valid_features(self):
        LOGGER.info("sample valid features")
        if self.feature_num is None:
            self.feature_num = self.bin_split_points.shape[0]

        choose_feature = random.choice(range(0, self.feature_num), \
                                       max(1, int(self.subsample_feature_rate * self.feature_num)), replace=False)

        valid_features = [False for i in range(self.feature_num)]
        for fid in choose_feature:
            valid_features[fid] = True
        return valid_features

    def sync_tree_dim(self):
        LOGGER.info("sync tree dim to provider")

        self.transfer_variable.tree_dim.remote(self.tree_dim,
                                               role=consts.PROVIDER,
                                               idx=-1)

    def sync_begin_iter(self, begin_iter):
        LOGGER.info("sync begin iter")

        self.transfer_variable.begin_iter.remote(begin_iter,
                                                 role=consts.PROVIDER,
                                                 idx=-1)

    def sync_stop_flag(self, stop_flag, num_round):
        LOGGER.info("sync stop flag to provider, boosting round is {}".format(num_round))

        self.transfer_variable.stop_flag.remote(stop_flag,
                                                role=consts.PROVIDER,
                                                idx=-1,
                                                suffix=(num_round,))

    def sync_predict_start_round(self, num_round):
        LOGGER.info("sync predict start round {}".format(num_round))
        self.transfer_variable.predict_start_round.remote(num_round,
                                                          role=consts.PROVIDER,
                                                          idx=-1)

    def sync_predict_stop_flag(self, stop_flag, comm_round):
        LOGGER.info("sync predict stop flag {}".format(stop_flag))
        self.transfer_variable.predict_stop_flag.remote(stop_flag,
                                                        role=consts.PROVIDER,
                                                        idx=-1,
                                                        suffix=(comm_round,))

    def sync_promoter_predict_data(self, predict_data, comm_round):
        LOGGER.info("sync promoter predict data {}".format(predict_data))
        self.transfer_variable.promoter_predict_data.remote(predict_data,
                                                            role=consts.PROVIDER,
                                                            idx=-1,
                                                            suffix=(comm_round,))

    def sync_provider_data_bin_with_dp(self):
        LOGGER.info("get provider bin data with dp")
        return self.transfer_variable.data_bin_with_dp.get(idx=-1)

    def sync_provider_bin_split_points(self):
        LOGGER.info("get provider bin data with dp")
        return self.transfer_variable.bin_split_points.get(idx=-1)

    def load_booster(self, model_meta, model_param, epoch_idx, booster_idx):
        tree = VertDPDecisionTreePromoter(self.tree_param)
        tree.load_model(model_meta, model_param)
        tree.set_flowid(self.generate_flowid(epoch_idx, booster_idx))
        tree.set_runtime_idx(self.component_properties.local_member_id)
        tree.set_provider_member_idlist(self.component_properties.provider_member_idlist)
        return tree

    @staticmethod
    def generate_leaf_pos_dict(x, tree_num):
        """
        x: just occupy the first parameter position
        return: a numpy array record sample pos, and a counter counting how many trees reach a leaf node
        """
        node_pos = np.zeros(tree_num, dtype=np.int64) + 0
        reach_leaf_node = np.zeros(tree_num, dtype=np.bool)
        return {'node_pos': node_pos, 'reach_leaf_node': reach_leaf_node}

    @staticmethod
    def traverse_a_tree(tree: VertDPDecisionTreePromoter, sample, cur_node_idx):

        reach_leaf = False
        # only need nid here, predict state is not needed
        rs = tree.traverse_tree(tree_=tree.tree_, data_inst=sample, predict_state=(cur_node_idx, -1),
                                sitename=tree.sitename, use_missing=tree.use_missing,
                                return_leaf_id=True)

        if not isinstance(rs, tuple):
            reach_leaf = True
            leaf_id = rs
            return leaf_id, reach_leaf
        else:
            cur_node_idx = rs[0]
            return cur_node_idx, reach_leaf

    @staticmethod
    def save_leaf_pos_helper(v1, v2):

        reach_leaf_idx = v2['reach_leaf_node']
        select_idx = reach_leaf_idx & (
                v2['node_pos'] != -1)  # reach leaf and are not recorded( if recorded idx is -1)
        v1[select_idx] = v2['node_pos'][select_idx]
        return v1

    @staticmethod
    def traverse_trees(node_pos, sample, trees: List[VertDPDecisionTreePromoter]):

        if node_pos['reach_leaf_node'].all():
            return node_pos

        for t_idx, tree in enumerate(trees):

            cur_node_idx = node_pos['node_pos'][t_idx]

            # reach leaf
            if cur_node_idx == -1:
                continue

            rs, reach_leaf = VertDPSecureBoostingPromoter.traverse_a_tree(tree, sample, cur_node_idx)

            if reach_leaf:
                node_pos['reach_leaf_node'][t_idx] = True

            node_pos['node_pos'][t_idx] = rs

        return node_pos

    @staticmethod
    def merge_predict_pos(node_pos1, node_pos2):

        pos_arr1 = node_pos1['node_pos']
        pos_arr2 = node_pos2['node_pos']
        stack_arr = np.stack([pos_arr1, pos_arr2])
        node_pos1['node_pos'] = np.max(stack_arr, axis=0)
        return node_pos1

    @staticmethod
    def mask_leaf_pos(v):

        reach_leaf_idx = v['reach_leaf_node']
        v['node_pos'][reach_leaf_idx] = -1
        return v

    @staticmethod
    def add_y_hat(leaf_pos, init_score, learning_rate, trees: List[VertDPDecisionTreePromoter], multi_class_num=None):

        # finally node pos will hold weights
        weights = []
        for leaf_idx, tree in zip(leaf_pos, trees):
            weights.append(tree.tree_[leaf_idx].weight)
        weights = np.array(weights)
        if multi_class_num > 2:
            weights = weights.reshape((-1, multi_class_num))
        return np.sum(weights * learning_rate, axis=0) + init_score

    @staticmethod
    def get_predict_scores(leaf_pos, learning_rate, init_score, trees: List[VertDPDecisionTreePromoter]
                           , multi_class_num=-1, predict_cache=None):

        if predict_cache:
            init_score = 0  # prevent init_score re-add

        predict_func = functools.partial(VertDPSecureBoostingPromoter.add_y_hat,
                                         learning_rate=learning_rate, init_score=init_score, trees=trees,
                                         multi_class_num=multi_class_num)
        predict_result = leaf_pos.mapValues(predict_func)

        if predict_cache:
            predict_result = predict_result.join(predict_cache, lambda v1, v2: v1 + v2)

        return predict_result

    def save_leaf_pos_and_mask_leaf_pos(self, node_pos_tb, final_leaf_pos):

        # save leaf pos
        saved_leaf_pos = final_leaf_pos.join(node_pos_tb, self.save_leaf_pos_helper)
        rest_part = final_leaf_pos.subtractByKey(saved_leaf_pos)
        final_leaf_pos = saved_leaf_pos.union(rest_part)
        # mask leaf pos
        node_pos_tb = node_pos_tb.mapValues(self.mask_leaf_pos)

        return node_pos_tb, final_leaf_pos

    def fit_a_booster(self, epoch_idx: int, booster_dim: int):

        g_h = self.get_grad_and_hess(booster_dim)

        tree = VertDPDecisionTreePromoter(tree_param=self.tree_param)
        tree.init(flowid=self.generate_flowid(epoch_idx, booster_dim),
                  data_bin=self.data_bin, bin_split_points=self.bin_split_points,
                  bin_sparse_points=self.bin_sparse_points,
                  valid_features = self.sample_valid_features(),
                  grad_and_hess=g_h,
                  provider_member_idlist=self.component_properties.provider_member_idlist,
                  task_type=self.task_type,
                  runtime_idx=self.component_properties.local_member_id,
                  feature_sitenames = self.feature_sitenames,
                  goss_subsample=self.enable_goss,
                  top_rate=self.top_rate, other_rate=self.other_rate,
                  max_sample_weight=self.max_sample_weight,
                  )

        tree.fit()

        self.update_feature_importance(tree.get_feature_importance())

        return tree

    def fit(self, data_inst, validate_data=None):
        LOGGER.info("begin to train secureboosting promoter model")
        self.gen_feature_fid_mapping(data_inst.schema)
        self.validation_strategy = self.init_validation_strategy(data_inst, validate_data)
        data_inst = self.data_alignment(data_inst)
        self.convert_feature_to_bin(data_inst)
        self.set_y()
        self.init_predict_score()

        self.sync_tree_dim()

        cur_best_model = self.tracker.get_training_best_model()
        bestIteration = 0
        if cur_best_model is not None:
            model_param = cur_best_model["Model_Param"]
            bestIteration = model_param['treeNum']
            self.set_model_param(model_param)
            self.sync_begin_iter(bestIteration)
            self.tracker.set_task_progress(bestIteration)
        for epoch_idx in range(bestIteration, self.num_trees):
            self.compute_grad_and_hess(data_inst)
            for tidx in range(self.tree_dim):

                model = self.fit_a_booster(epoch_idx, tidx)

                tree_meta, tree_param = model.get_model()
                self.trees_.append(tree_param)
                if self.tree_meta is None:
                    self.tree_meta = tree_meta
                # update predict score
                cur_sample_weights = model.get_sample_weights()
                self.update_predict_score(cur_sample_weights, dim=tidx)

            loss = self.compute_loss()
            self.history_loss.append(loss)
            LOGGER.info("round {} loss is {}".format(epoch_idx, loss))
            LOGGER.debug("type of loss is {}".format(type(loss).__name__))

            metric_meta = {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS'}
            self.callback_metric(metric_name='loss',
                                 metric_namespace='train',
                                 metric_meta=metric_meta,
                                 metric_data=(epoch_idx, loss))

            if self.validation_strategy:
                self.validation_strategy.validate(self, epoch_idx)
                if self.validation_strategy.need_stop():
                    LOGGER.debug('early stopping triggered')
                    break

            if self.n_iter_no_change is True:
                if self.check_convergence(loss):
                    self.sync_stop_flag(True, epoch_idx)
                    break
                else:
                    self.sync_stop_flag(False, epoch_idx)

            self.tracker.save_training_best_model(self.export_model())
            self.tracker.add_task_progress(1)

        LOGGER.debug("history loss is {}".format(min(self.history_loss)))
        self.callback_metric("loss",
                             "train",
                             {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS'},
                             metric_data=("Best", min(self.history_loss)))

        if self.validation_strategy and self.validation_strategy.has_saved_best_model():
            self.load_model(self.validation_strategy.cur_best_model)

        LOGGER.info("end to train secureboosting promoter model")

    def score_to_predict_result(self, data_inst, y_hat):
        """
        given binary/multi-class/regression prediction scores, outputs result in standard format
        """
        predicts = None
        if self.task_type == consts.CLASSIFICATION:
            loss_method = self.loss
            if self.num_classes == 2:
                predicts = y_hat.mapValues(lambda f: float(loss_method.predict(f)))
            else:
                predicts = y_hat.mapValues(lambda f: loss_method.predict(f).tolist())

        elif self.task_type == consts.REGRESSION:
            if self.objective_param.objective in ["lse", "lae", "huber", "log_cosh", "fair", "tweedie"]:
                predicts = y_hat
            else:
                raise NotImplementedError("objective {} not supprted yet".format(self.objective_param.objective))

        if self.task_type == consts.CLASSIFICATION:

            predict_result = self.predict_score_to_output(data_inst, predict_score=predicts, classes=self.classes_,
                                                          threshold=self.predict_param.threshold)

        elif self.task_type == consts.REGRESSION:
            predict_result = data_inst.join(predicts, lambda inst, pred: [inst.label, float(pred), float(pred),
                                                                          {"label": float(pred)}])

        else:
            raise NotImplementedError("task type {} not supported yet".format(self.task_type))
        return predict_result

    def boosting_fast_predict(self, data_inst, trees: List[VertDPDecisionTreePromoter], predict_cache=None):

        tree_num = len(trees)
        generate_func = functools.partial(self.generate_leaf_pos_dict, tree_num=tree_num)
        node_pos_tb = data_inst.mapValues(
            generate_func)  # record node pos   <class 'tuple'>: ('0', {'node_pos': array([0, 0, 0]), 'reach_leaf_node': array([False, False, False])})
        final_leaf_pos = data_inst.mapValues(lambda x: np.zeros(tree_num, dtype=np.int64) - 1)  # record final leaf pos
        traverse_func = functools.partial(self.traverse_trees, trees=trees)
        comm_round = 0

        while True:

            LOGGER.info('cur predict round is {}'.format(comm_round))

            node_pos_tb = node_pos_tb.join(data_inst, traverse_func)
            node_pos_tb, final_leaf_pos = self.save_leaf_pos_and_mask_leaf_pos(node_pos_tb, final_leaf_pos)

            # remove sample that reaches leaves of all trees
            reach_leaf_samples = node_pos_tb.filter(lambda key, value: value['reach_leaf_node'].all())
            node_pos_tb = node_pos_tb.subtractByKey(reach_leaf_samples, need_send=True)

            if node_pos_tb.count() == 0:
                self.sync_predict_stop_flag(True, comm_round)
                break

            self.sync_predict_stop_flag(False, comm_round)
            self.sync_promoter_predict_data(node_pos_tb, comm_round)

            provider_pos_tbs = self.transfer_variable.provider_predict_data.get(idx=-1, suffix=(comm_round,))

            for provider_pos_tb in provider_pos_tbs:
                node_pos_tb = node_pos_tb.join(provider_pos_tb, self.merge_predict_pos)

            comm_round += 1

        LOGGER.info('federated prediction process done')

        predict_result = self.get_predict_scores(leaf_pos=final_leaf_pos, learning_rate=self.learning_rate,
                                                 init_score=self.init_score, trees=trees,
                                                 multi_class_num=self.tree_dim, predict_cache=predict_cache)

        return predict_result

    @assert_io_num_rows_equal
    def predict(self, data_inst):

        LOGGER.info('running prediction')
        cache_dataset_key = self.predict_data_cache.get_data_key(data_inst)

        processed_data = self.data_alignment(data_inst)

        last_round = self.predict_data_cache.predict_data_last_round(cache_dataset_key)

        self.sync_predict_start_round(last_round + 1)

        rounds = len(self.trees_) // self.tree_dim
        trees = []
        for idx in range(last_round + 1, rounds):
            for booster_idx in range(self.tree_dim):
                tree = self.load_booster(self.tree_meta,
                                         self.trees_[idx * self.tree_dim + booster_idx],
                                         idx, booster_idx)
                trees.append(tree)

        predict_cache = None
        if last_round != -1:
            predict_cache = self.predict_data_cache.predict_data_at(cache_dataset_key, last_round)
            LOGGER.info('load predict cache of round {}'.format(last_round))

        predict_rs = self.boosting_fast_predict(processed_data, trees=trees, predict_cache=predict_cache)
        # self.predict_data_cache.add_data(cache_dataset_key, predict_rs)

        return self.score_to_predict_result(data_inst, predict_rs)

    def get_feature_importance(self):
        return self.feature_importances_

    def get_model_meta(self):
        model_meta = BoostingTreeModelMeta()
        model_meta.tree_meta.CopyFrom(self.tree_meta)
        model_meta.learning_rate = self.learning_rate
        model_meta.num_trees = self.num_trees
        model_meta.quantile_meta.CopyFrom(QuantileMeta(bin_num=self.bin_num))
        model_meta.objective_meta.CopyFrom(ObjectiveMeta(objective=self.objective_param.objective,
                                                         param=self.objective_param.params))
        model_meta.task_type = self.task_type
        # model_meta.tree_dim = self.tree_dim
        model_meta.n_iter_no_change = self.n_iter_no_change
        model_meta.tol = self.tol
        # model_meta.num_classes = self.num_classes
        # model_meta.classes_.extend(map(str, self.classes_))
        # model_meta.need_run = self.need_run
        meta_name = "VertDPSecureBoostingTreePromoterMeta"

        return meta_name, model_meta

    def set_model_meta(self, model_meta):
        if type(model_meta) is dict:
            tree_meta = model_meta.get("treeMeta")
            self.tree_meta = DecisionTreeModelMeta()
            self.tree_meta.max_depth = tree_meta.get("maxDepth")
            self.tree_meta.min_sample_split = tree_meta.get("minSampleSplit")
            self.tree_meta.min_impurity_split = tree_meta.get("minImpuritySplit")
            self.tree_meta.min_leaf_node = tree_meta.get("minLeafNode")
            if tree_meta.get("criterionMeta"):
                self.tree_meta.criterion_meta.CopyFrom(
                    CriterionMeta(criterion_method=tree_meta.get("criterionMeta").get("criterionMethod"),
                                  criterion_param=list(tree_meta.get("criterionMeta").get("criterionParam"))))
            self.tree_meta.use_missing = tree_meta.get("useMissing")
            self.tree_meta.zero_as_missing = tree_meta.get("zeroAsMissing")

            self.learning_rate = model_meta.get("learningRate")
            self.num_trees = model_meta.get("numTrees")
            self.bin_num = model_meta.get("quantileMeta").get("binNum")
            self.objective_param.objective = model_meta.get("objectiveMeta").get("objective")
            self.objective_param.params = list(model_meta.get("objectiveMeta").get("param"))
            self.task_type = model_meta.get("taskType")
            self.n_iter_no_change = model_meta.get("nIterNoChange")
            self.tol = model_meta.get("tol")
        else:
            self.tree_meta = model_meta.tree_meta
            self.learning_rate = model_meta.learning_rate
            self.num_trees = model_meta.num_trees
            self.bin_num = model_meta.quantile_meta.bin_num
            self.objective_param.objective = model_meta.objective_meta.objective
            self.objective_param.params = list(model_meta.objective_meta.param)
            self.task_type = model_meta.task_type
            # self.tree_dim = model_meta.tree_dim
            # self.num_classes = model_meta.num_classes
            self.n_iter_no_change = model_meta.n_iter_no_change
            self.tol = model_meta.tol
            # self.classes_ = list(model_meta.classes_)

            # self.set_loss(self.objective_param)

    def get_model_param(self):
        model_param = BoostingTreeModelParam()
        model_param.tree_num = len(list(self.trees_))
        model_param.tree_dim = self.tree_dim
        model_param.trees_.extend(self.trees_)
        model_param.init_score.extend(self.init_score)
        model_param.losses.extend(self.history_loss)
        model_param.classes_.extend(map(str, self.classes_))
        model_param.num_classes = self.num_classes

        model_param.best_iteration = -1 if self.validation_strategy is None else self.validation_strategy.best_iteration

        feature_importances = list(self.get_feature_importance().items())
        feature_importances = sorted(feature_importances, key=itemgetter(1), reverse=True)
        feature_importance_param = []
        for (sitename, fid), importance in feature_importances:
            if consts.PROMOTER in sitename:
                fullname = self.feature_name_fid_mapping[fid]
            else:
                role_name, party_id = sitename.split(':')
                fullname = generate_anonymous(fid=fid, party_id=party_id, role=role_name)

            feature_importance_param.append(FeatureImportanceInfo(sitename=sitename,
                                                                  fid=fid,
                                                                  importance=importance.importance,
                                                                  fullname=fullname,
                                                                  importance2=importance.importance_2,
                                                                  main=importance.main_type
                                                                  ))
        model_param.feature_importances.extend(feature_importance_param)
        # LOGGER.debug('feat importance param {}'.format(feature_importance_param))
        model_param.feature_name_fid_mapping.update(self.feature_name_fid_mapping)

        param_name = "VertDPSecureBoostingTreePromoterParam"

        return param_name, model_param

    def set_model_param(self, model_param):
        if type(model_param) is dict:
            for tree in list(model_param.get("trees")):
                tree_param = DecisionTreeModelParam()
                for node in tree['tree']:
                    tree_param.tree_.add(id=node['id'],
                                             sitename=node['sitename'],
                                             fid=node['fid'],
                                             bid=node['bid'],
                                             weight=node['weight'],
                                             is_leaf=node['isLeaf'],
                                             left_nodeid=node['leftNodeid'],
                                             right_nodeid=node['rightNodeid'],
                                             missing_dir=node['missingDir'])
                self.trees_.append(tree_param)

            self.tree_dim = model_param.get("treeDim")
            self.init_score = np.array(list(model_param.get("initScore")))
            self.history_loss = list(model_param.get("losses"))
            self.classes_ = list(map(int, model_param.get("classes")))
            self.num_classes = model_param.get("numClasses")
            featureNameFidMapping = dict([int(b), v] for b, v in model_param['featureNameFidMapping'].items())
            self.feature_name_fid_mapping.update(featureNameFidMapping)
        else:
            self.trees_ = list(model_param.trees_)
            self.init_score = np.array(list(model_param.init_score))
            self.history_loss = list(model_param.losses)
            self.classes_ = list(map(int, model_param.classes_))
            self.tree_dim = model_param.tree_dim
            self.num_classes = model_param.num_classes
            self.feature_name_fid_mapping.update(model_param.feature_name_fid_mapping)

    def get_metrics_param(self):
        if self.task_type == consts.CLASSIFICATION:
            if self.num_classes == 2:
                return EvaluateParam(eval_type="binary",
                                     pos_label=self.classes_[1], metrics=self.metrics)
            else:
                return EvaluateParam(eval_type="multi", metrics=self.metrics)
        else:
            return EvaluateParam(eval_type="regression", metrics=self.metrics)

    def export_model(self):

        if self.need_cv:
            return None

        meta_name, meta_protobuf = self.get_model_meta()
        param_name, param_protobuf = self.get_model_param()

        return {meta_name: meta_protobuf, param_name: param_protobuf}

    def load_model(self, model_dict):
        model_param = None
        model_meta = None
        for _, value in model_dict["model"].items():
            for model in value:
                if type(model) == str:
                    if model.endswith("Meta"):
                        model_meta = value[model]
                    if model.endswith("Param"):
                        model_param = value[model]
                else:
                    for obj in model.items():
                        key = obj[0]
                        if key.endswith("Meta"):
                            model_meta = obj[1]
                        if key.endswith("Param"):
                            model_param = obj[1]

        LOGGER.info("load model")

        self.set_model_meta(model_meta)
        self.set_model_param(model_param)
        self.set_loss(self.objective_param)
