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
from operator import itemgetter
from typing import List

import numpy as np
from numpy import random

from common.python.utils import log_utils
from kernel.components.binning.horzfeaturebinning.horz_split_points import HorzFeatureBinningClient
from kernel.components.boosting import BoostingTree
from kernel.components.boosting import HorzDecisionTreeClient
from kernel.components.boosting import SecureBoostClientAggregator
from kernel.components.evaluation.param import EvaluateParam
from kernel.model_selection.k_fold import KFold
from kernel.optimizer.loss import FairLoss
from kernel.optimizer.loss import HuberLoss
from kernel.optimizer.loss import LeastAbsoluteErrorLoss
from kernel.optimizer.loss import LeastSquaredErrorLoss
from kernel.optimizer.loss import LogCoshLoss
from kernel.optimizer.loss import SigmoidBinaryCrossEntropyLoss
from kernel.optimizer.loss import SoftmaxCrossEntropyLoss
from kernel.optimizer.loss import TweedieLoss
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import BoostingTreeModelMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import DecisionTreeModelMeta, CriterionMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import ObjectiveMeta
from kernel.protobuf.generated.boosting_tree_model_meta_pb2 import QuantileMeta
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import BoostingTreeModelParam
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import DecisionTreeModelParam
from kernel.protobuf.generated.boosting_tree_model_param_pb2 import FeatureImportanceInfo
from kernel.transfer.variables.transfer_class.horz_secure_boost_transfer_variable import \
    HorzSecureBoostingTransferVariable
from kernel.utils import consts
from kernel.utils.data_util import NoneType
from kernel.utils.label_checker import ClassifyLabelChecker, RegressionLabelChecker
from kernel.model_selection import start_cross_validation
from kernel.model_selection.grid_search import GridSearch

LOGGER = log_utils.get_logger()


class HorzSecureBoostingClient(BoostingTree):

    def __init__(self):
        super(HorzSecureBoostingClient, self).__init__()

        self.mode = consts.HORZ
        self.validation_strategy = None
        self.loss_fn = None
        self.cur_sample_weights = None
        self.y = None
        self.y_hat = None
        self.y_hat_predict = None
        self.feature_num = None
        self.num_classes = 2
        self.tree_dim = 1
        self.trees = []
        self.feature_importance = {}
        self.transfer_inst = HorzSecureBoostingTransferVariable()
        self.role = None
        self.data_bin = None
        self.bin_split_points = None
        self.bin_sparse_points = None
        self.init_score = None
        self.local_loss_history = []
        self.classes_ = []

        self.role = consts.PROMOTER

        # store learnt model param
        self.tree_meta = None
        self.learnt_tree_param = []

        self.aggregator = SecureBoostClientAggregator()

        # Since arbiter is not needed in oot mode, it will always wait for the data blocking value
        # when creating the HorzFeatureBinningClient object, so the object will not be created here
        self.binning_obj = None

    def set_loss_function(self, objective_param):
        loss_type = objective_param.objective
        params = objective_param.params
        LOGGER.info("set objective,  objective is {}".format(loss_type))
        if self.task_type == consts.CLASSIFICATION:
            if loss_type == "cross_entropy":
                if self.num_classes == 2:
                    self.loss_fn = SigmoidBinaryCrossEntropyLoss()
                else:
                    self.loss_fn = SoftmaxCrossEntropyLoss()
            else:
                raise NotImplementedError("objective %s not supported yet" % (loss_type))
        elif self.task_type == consts.REGRESSION:
            if loss_type == "lse":
                self.loss_fn = LeastSquaredErrorLoss()
            elif loss_type == "lae":
                self.loss_fn = LeastAbsoluteErrorLoss()
            elif loss_type == "huber":
                self.loss_fn = HuberLoss(params[0])
            elif loss_type == "fair":
                self.loss_fn = FairLoss(params[0])
            elif loss_type == "tweedie":
                self.loss_fn = TweedieLoss(params[0])
            elif loss_type == "log_cosh":
                self.loss_fn = LogCoshLoss()
            else:
                raise NotImplementedError("objective %s not supported yet" % loss_type)
        else:
            raise NotImplementedError("objective %s not supported yet" % loss_type)

    def federated_binning(self, data_instance):
        # In order to be compatible with oot mode, the object is not created when it is initialized,
        # so it can only be created after it is used somewhere
        if self.binning_obj is None:
            self.binning_obj = HorzFeatureBinningClient()
        if self.use_missing:
            binning_result = self.binning_obj.average_run(data_instances=data_instance,
                                                          bin_num=self.bin_num, abnormal_list=[NoneType()])
        else:
            binning_result = self.binning_obj.average_run(data_instances=data_instance,
                                                          bin_num=self.bin_num)

        return self.binning_obj.convert_feature_to_bin(data_instance, binning_result)

    def compute_local_grad_and_hess(self, y_hat):

        loss_method = self.loss_fn
        if self.task_type == consts.CLASSIFICATION:
            grad_and_hess = self.y.join(y_hat, lambda y, f_val: \
                (loss_method.compute_grad(y, loss_method.predict(f_val)), \
                 loss_method.compute_hess(y, loss_method.predict(f_val))))
        else:
            grad_and_hess = self.y.join(y_hat, lambda y, f_val:
            (loss_method.compute_grad(y, f_val),
             loss_method.compute_hess(y, f_val)))

        return grad_and_hess

    def compute_local_loss(self, y, y_hat):

        LOGGER.info('computing local loss')

        loss_method = self.loss_fn
        if self.objective_param.objective in ["lse", "lae", "logcosh", "tweedie", "log_cosh", "huber"]:
            # regression tasks
            y_predict = y_hat
        else:
            # classification tasks
            y_predict = y_hat.mapValues(lambda val: loss_method.predict(val))

        loss = loss_method.compute_loss(y, y_predict)

        return float(loss)

    @staticmethod
    def get_subtree_grad_and_hess(g_h, t_idx: int):
        """
        Args:
            g_h of g_h val
            t_idx: tree index
        Returns: grad and hess of sub tree
        """
        LOGGER.info("get grad and hess of tree {}".format(t_idx))
        grad_and_hess_subtree = g_h.mapValues(
            lambda grad_and_hess: (grad_and_hess[0][t_idx], grad_and_hess[1][t_idx]))
        return grad_and_hess_subtree

    def sample_valid_feature(self):

        if self.feature_num is None:
            self.feature_num = self.bin_split_points.shape[0]

        chosen_feature = random.choice(range(0, self.feature_num), \
                                       max(1, int(self.subsample_feature_rate * self.feature_num)), replace=False)
        valid_features = [False for i in range(self.feature_num)]
        for fid in chosen_feature:
            valid_features[fid] = True

        return valid_features

    @staticmethod
    def add_y_hat(f_val, new_f_val, lr=0.1, idx=0):
        f_val[idx] += lr * new_f_val
        return f_val

    def update_y_hat_val(self, new_val=None, mode='train', tree_idx=0):

        LOGGER.debug('update y_hat value,  current tree is {}'.format(tree_idx))
        add_func = functools.partial(self.add_y_hat, lr=self.learning_rate, idx=tree_idx)
        if mode == 'train':
            self.y_hat = self.y_hat.join(new_val, add_func)
        else:
            self.y_hat_predict = self.y_hat_predict.join(new_val, add_func)

    def update_feature_importance(self, tree_feature_importance):

        for fid in tree_feature_importance:
            if fid not in self.feature_importance:
                self.feature_importance[fid] = tree_feature_importance[fid]
            else:
                self.feature_importance[fid] += tree_feature_importance[fid]

    def sync_feature_num(self):
        self.transfer_inst.feature_number.remote(self.feature_num, role=consts.ARBITER, idx=-1, suffix=('feat_num',))

    def sync_local_loss(self, cur_loss: float, sample_num: int, suffix):
        data = {'cur_loss': cur_loss, 'sample_num': sample_num}
        self.transfer_inst.loss_status.remote(data, role=consts.ARBITER, idx=-1, suffix=suffix)
        LOGGER.debug('loss status sent')

    def sync_tree_dim(self, tree_dim: int):
        self.transfer_inst.tree_dim.remote(tree_dim, suffix=('tree_dim',))
        LOGGER.debug('tree dim sent')

    def sync_stop_flag(self, suffix) -> bool:
        flag = self.transfer_inst.stop_flag.get(idx=0, suffix=suffix)
        return flag

    def check_labels(self, data_inst, ) -> List[int]:

        LOGGER.debug('checking labels')

        classes_ = None
        if self.task_type == consts.CLASSIFICATION:
            num_classes, classes_ = ClassifyLabelChecker.validate_label(data_inst)
        else:
            RegressionLabelChecker.validate_label(data_inst)

        return classes_

    def generate_flowid(self, round_num, tree_num):
        LOGGER.info("generate flowid, flowid {}".format(self.flowid))
        return ".".join(map(str, [self.flowid, round_num, tree_num]))

    def label_alignment(self, labels: List[int]):
        self.transfer_inst.local_labels.remote(labels, suffix=('label_align',))

    def get_valid_features(self, epoch_idx, t_idx):
        valid_feature = self.transfer_inst.valid_features.get(idx=0, suffix=('valid_features', epoch_idx, t_idx))
        return valid_feature

    def callback_loss(self, iter_num, loss):
        metric_meta = {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS',
                       'pair_type': ''}
        self.callback_metric(metric_name='loss',
                             metric_namespace='train',
                             metric_meta=metric_meta,
                             metric_data=(iter_num, loss))

    def fit(self, data_inst, validate_data=None, ):

        # binning
        data_inst = self.data_alignment(data_inst)
        self.data_bin, self.bin_split_points, self.bin_sparse_points = self.federated_binning(data_inst)
        print(self.data_bin.first())
        # fid mapping
        self.gen_feature_fid_mapping(data_inst.schema)

        # set feature_num
        self.feature_num = self.bin_split_points.shape[0]

        # sync feature num
        self.sync_feature_num()

        # initialize validation strategy
        self.validation_strategy = self.init_validation_strategy(train_data=data_inst, validate_data=validate_data, )

        # check labels
        local_classes = self.check_labels(self.data_bin)  # [0,1]

        # sync label class and set y
        if self.task_type == consts.CLASSIFICATION:
            self.transfer_inst.local_labels.remote(local_classes, role=consts.ARBITER, suffix=('label_align',))
            new_label_mapping = self.transfer_inst.label_mapping.get(idx=0, suffix=('label_mapping',))  # {0: 0, 1: 1}
            self.classes_ = [new_label_mapping[k] for k in new_label_mapping]
            # set labels
            self.num_classes = len(new_label_mapping)
            LOGGER.debug('num_classes is {}'.format(self.num_classes))
            self.y = self.data_bin.mapValues(lambda instance: new_label_mapping[instance.label])
            # set tree dimension
            self.tree_dim = self.num_classes if self.num_classes > 2 else 1
        else:
            self.y = self.data_bin.mapValues(lambda instance: instance.label)
        # print(list(self.y.collect()))

        # set loss function
        self.set_loss_function(self.objective_param)

        # set y_hat_val
        self.y_hat, self.init_score = self.loss_fn.initialize(self.y) if self.tree_dim == 1 else \
            self.loss_fn.initialize(self.y, self.tree_dim)
        # print(list(self.y_hat.collect()))

        for epoch_idx in range(self.num_trees):

            g_h = self.compute_local_grad_and_hess(self.y_hat)
            # print(list(g_h.collect()))

            for t_idx in range(self.tree_dim):
                valid_features = self.get_valid_features(epoch_idx,
                                                         t_idx)  # <class 'list'>: [True, True, True, True, True, True, True, True, True, True, True, True, True, True, True, True, True, True, True, True, True, True, True]
                LOGGER.debug('valid features are {}'.format(valid_features))
                subtree_g_h = self.get_subtree_grad_and_hess(g_h, t_idx)
                flow_id = self.generate_flowid(epoch_idx, t_idx)
                new_tree = HorzDecisionTreeClient(self.tree_param, self.data_bin, self.bin_split_points,
                                                  self.bin_sparse_points, subtree_g_h, valid_feature=valid_features
                                                  , epoch_idx=epoch_idx, role=self.role, tree_idx=t_idx,
                                                  flow_id=flow_id, mode='train')
                new_tree.fit()

                # update y_hat_val
                self.update_y_hat_val(new_val=new_tree.sample_weights, mode='train', tree_idx=t_idx)
                self.trees.append(new_tree)
                self.tree_meta, new_tree_param = new_tree.get_model()
                self.learnt_tree_param.append(new_tree_param)
                self.update_feature_importance(new_tree.get_feature_importance())

            # sync loss status
            loss = self.compute_local_loss(self.y, self.y_hat)
            self.callback_loss(epoch_idx,loss)
            LOGGER.debug('local loss of epoch {} is {}'.format(epoch_idx, loss))

            self.local_loss_history.append(loss)
            self.aggregator.send_local_loss(loss, self.data_bin.count(), suffix=(epoch_idx,))

            # validate
            if self.validation_strategy:
                self.validation_strategy.validate(self, epoch_idx)

            # check stop flag if n_iter_no_change is True
            if self.n_iter_no_change:
                should_stop = self.aggregator.get_converge_status(suffix=(str(epoch_idx),))
                LOGGER.debug('got stop flag {}'.format(should_stop))
                if should_stop:
                    LOGGER.debug('stop triggered')
                    break

            self.tracker.add_task_progress(1, self.need_grid_search)

            LOGGER.debug('fitting tree {}/{}'.format(epoch_idx, self.num_trees))

        LOGGER.debug('fitting horz decision tree done')

    def predict(self, data_inst):

        to_predict_data = self.data_alignment(data_inst)

        init_score = self.init_score
        self.y_hat_predict = data_inst.mapValues(lambda x: init_score)

        round_num = len(self.learnt_tree_param) // self.tree_dim
        idx = 0
        for round_idx in range(round_num):
            for tree_idx in range(self.tree_dim):
                tree_inst = HorzDecisionTreeClient(tree_param=self.tree_param, mode='predict')
                tree_inst.load_model(model_meta=self.tree_meta, model_param=self.learnt_tree_param[idx])
                idx += 1
                predict_val = tree_inst.predict(to_predict_data)
                self.update_y_hat_val(predict_val, mode='predict', tree_idx=tree_idx)

        predict_result = None

        if self.task_type == consts.REGRESSION and \
                self.objective_param.objective in ["lse", "lae", "huber", "log_cosh", "fair", "tweedie"]:
            predict_result = to_predict_data.join(self.y_hat_predict,
                                                  lambda inst, pred: [inst.label, float(pred), float(pred),
                                                                      {"label": float(pred)}])

        elif self.task_type == consts.CLASSIFICATION:
            classes_ = self.classes_
            loss_func = self.loss_fn
            if self.num_classes == 2:
                predicts = self.y_hat_predict.mapValues(lambda f: float(loss_func.predict(f)))
                threshold = self.predict_param.threshold
                predict_result = to_predict_data.join(predicts, lambda inst, pred: [inst.label,
                                                                                    classes_[1] if pred > threshold else
                                                                                    classes_[0], pred,
                                                                                    {"0": 1 - pred, "1": pred}])
            else:
                predicts = self.y_hat_predict.mapValues(lambda f: loss_func.predict(f).tolist())
                predict_result = to_predict_data.join(predicts, lambda inst, preds: [inst.label, \
                                                                                     classes_[np.argmax(preds)],
                                                                                     np.max(preds), dict(
                        zip(map(str, classes_), preds))])

        return predict_result

    def get_feature_importance(self):
        return self.feature_importance

    def get_model_meta(self):
        model_meta = BoostingTreeModelMeta()
        model_meta.tree_meta.CopyFrom(self.tree_meta)
        model_meta.learning_rate = self.learning_rate
        model_meta.num_trees = self.num_trees
        model_meta.quantile_meta.CopyFrom(QuantileMeta(bin_num=self.bin_num))
        model_meta.objective_meta.CopyFrom(ObjectiveMeta(objective=self.objective_param.objective,
                                                         param=self.objective_param.params))
        model_meta.task_type = self.task_type
        model_meta.n_iter_no_change = self.n_iter_no_change
        model_meta.tol = self.tol

        meta_name = "HorzSecureBoostingTreePromoterMeta"

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
            self.n_iter_no_change = model_meta.n_iter_no_change
            self.tol = model_meta.tol

    def get_model_param(self):
        model_param = BoostingTreeModelParam()
        model_param.tree_num = len(list(self.learnt_tree_param))
        model_param.tree_dim = self.tree_dim
        model_param.trees_.extend(self.learnt_tree_param)
        model_param.init_score.extend(self.init_score)
        model_param.losses.extend(self.local_loss_history)
        model_param.classes_.extend(map(str, self.classes_))
        model_param.num_classes = self.num_classes
        model_param.best_iteration = -1

        feature_importance = list(self.get_feature_importance().items())
        feature_importance = sorted(feature_importance, key=itemgetter(1), reverse=True)
        feature_importance_param = []
        for fid, importance in feature_importance:
            feature_importance_param.append(FeatureImportanceInfo(fid=fid,
                                                                  fullname=self.feature_name_fid_mapping[fid],
                                                                  sitename=self.role,
                                                                  importance=importance.importance,
                                                                  importance2=importance.importance_2,
                                                                  main=importance.main_type
                                                                  ))
        model_param.feature_importances.extend(feature_importance_param)

        model_param.feature_name_fid_mapping.update(self.feature_name_fid_mapping)

        param_name = "HorzSecureBoostingTreePromoterParam"

        return param_name, model_param

    def get_cur_model(self):
        meta_name, meta_protobuf = self.get_model_meta()
        param_name, param_protobuf = self.get_model_param()
        return {meta_name: meta_protobuf,
                param_name: param_protobuf
                }

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
                splitMaskdict = dict([int(b), v] for b, v in tree['splitMaskdict'].items())
                missingDirMaskdict = dict([int(b), v] for b, v in tree['missingDirMaskdict'].items())
                tree_param.split_maskdict.update(splitMaskdict)
                tree_param.missing_dir_maskdict.update(missingDirMaskdict)
                self.trees.append(tree_param)
                self.learnt_tree_param.append(tree_param)

            # self.learnt_tree_param = list(model_param.get("trees"))
            self.tree_dim = model_param.get("treeDim")
            self.init_score = np.array(list(model_param.get("initScore")))
            self.history_loss = list(model_param.get("losses"))
            self.classes_ = list(map(int, model_param.get("classes")))
            self.num_classes = model_param.get("numClasses")
            featureNameFidMapping = dict([int(b), v] for b, v in model_param['featureNameFidMapping'].items())
            self.feature_name_fid_mapping.update(featureNameFidMapping)
        else:
            self.learnt_tree_param = list(model_param.trees_)
            self.init_score = np.array(list(model_param.init_score))
            self.local_loss_history = list(model_param.losses)
            self.classes_ = list(model_param.classes_)
            self.tree_dim = model_param.tree_dim
            self.num_classes = model_param.num_classes
            self.feature_name_fid_mapping.update(model_param.feature_name_fid_mapping)

    def get_metrics_param(self):
        if self.task_type == consts.CLASSIFICATION:
            if self.num_classes == 2:
                return EvaluateParam(eval_type="binary",
                                     pos_label=self.classes_[1])
            else:
                return EvaluateParam(eval_type="multi")
        else:
            return EvaluateParam(eval_type="regression")

    def export_model(self):
        if self.model_output is not None:
            return self.model_output

        if self.need_cv and not self.need_grid_search:
            return
        return self.get_cur_model()

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
        self.set_loss_function(self.objective_param)

    def cross_validation(self, data_instances):
        return start_cross_validation.run(self, data_instances, True)

    def grid_search(self, train_data, eval_data, need_cv=False):
        if not self.need_run:
            return train_data
        grid_obj = GridSearch()
        grid_search_param = self._get_grid_search_param()
        output_data = grid_obj.run(grid_search_param, train_data, eval_data, self, need_cv, True)
        return output_data
