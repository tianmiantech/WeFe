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

import logging
import math
import sys
from collections import defaultdict

import numpy as np
import pandas as pd
from sklearn.metrics import accuracy_score
from sklearn.metrics import confusion_matrix
from sklearn.metrics import explained_variance_score
from sklearn.metrics import mean_absolute_error
from sklearn.metrics import mean_squared_error
from sklearn.metrics import mean_squared_log_error
from sklearn.metrics import median_absolute_error
from sklearn.metrics import precision_score
from sklearn.metrics import r2_score
from sklearn.metrics import recall_score
from sklearn.metrics import roc_auc_score
from sklearn.metrics import roc_curve

from common.python.utils import log_utils
from kernel.components.evaluation.param import EvaluateParam
from kernel.model_base import ModelBase
from kernel.utils import consts
from kernel.components.feature.featurepsi.vertfeaturepsi.base_psi import VertFeaturePSIBase

LOGGER = log_utils.get_logger()


class PerformanceRecorder():
    """
    This class record performance(single value metrics during the training process)
    """

    def __init__(self):

        # all of them are single value metrics
        self.allowed_metric = [consts.AUC,
                               consts.EXPLAINED_VARIANCE,
                               consts.MEAN_ABSOLUTE_ERROR,
                               consts.MEAN_SQUARED_ERROR,
                               consts.MEAN_SQUARED_LOG_ERROR,
                               consts.MEDIAN_ABSOLUTE_ERROR,
                               consts.R2_SCORE,
                               consts.ROOT_MEAN_SQUARED_ERROR,
                               consts.PRECISION,
                               consts.RECALL,
                               consts.ACCURACY,
                               consts.KS
                               ]

        self.larger_is_better = [consts.AUC,
                                 consts.R2_SCORE,
                                 consts.PRECISION,
                                 consts.RECALL,
                                 consts.EXPLAINED_VARIANCE,
                                 consts.ACCURACY,
                                 consts.KS
                                 ]

        self.smaller_is_better = [consts.ROOT_MEAN_SQUARED_ERROR,
                                  consts.MEAN_ABSOLUTE_ERROR,
                                  consts.MEAN_SQUARED_ERROR,
                                  consts.MEAN_SQUARED_LOG_ERROR]

        self.cur_best_performance = {}

        self.no_improvement_round = {}  # record no improvement round of all metrics

    def has_improved(self, val: float, metric: str, cur_best: dict):

        if metric not in cur_best:
            return True

        if metric in self.larger_is_better and val > cur_best[metric]:
            return True

        elif metric in self.smaller_is_better and val < cur_best[metric]:
            return True

        return False

    def update(self, eval_dict: dict):
        """

        Parameters
        ----------
        eval_dict dict, {metric_name:metric_val}, e.g. {'auc':0.99}

        Returns stop flag, if should stop return True, else False
        -------
        """
        if len(eval_dict) == 0:
            return

        for metric in eval_dict:
            if metric not in self.allowed_metric:
                continue
            if self.has_improved(eval_dict[metric], metric, self.cur_best_performance):
                self.cur_best_performance[metric] = eval_dict[metric]
                self.no_improvement_round[metric] = 0
            else:
                self.no_improvement_round[metric] += 1


class Evaluation(ModelBase):

    def __init__(self):
        super().__init__()
        self.model_param = EvaluateParam()
        self.eval_results = defaultdict(list)
        self.bins_result = defaultdict(list)
        self.save_single_value_metric_list = [consts.AUC,
                                              consts.EXPLAINED_VARIANCE,
                                              consts.MEAN_ABSOLUTE_ERROR,
                                              consts.MEAN_SQUARED_ERROR,
                                              consts.MEAN_SQUARED_LOG_ERROR,
                                              consts.MEDIAN_ABSOLUTE_ERROR,
                                              consts.R2_SCORE,
                                              consts.ROOT_MEAN_SQUARED_ERROR]
        self.save_curve_metric_list = [consts.KS, consts.ROC, consts.LIFT, consts.GAIN, consts.PRECISION, consts.RECALL,
                                       consts.ACCURACY]

        self.metrics = None
        self.round_num = 6

        self.validate_metric = {}
        self.train_metric = {}
        self.base_psi = VertFeaturePSIBase()

    def _init_model(self, model):
        self.model_param = model
        self.eval_type = self.model_param.eval_type
        self.pos_label = self.model_param.pos_label
        self.metrics = model.metrics

    def _run_data(self, data_sets=None, stage=None):
        if not self.need_run:
            return

        data = {}
        for data_key in data_sets:
            if data_sets[data_key].get("data", None):
                data[data_key] = data_sets[data_key]["data"]

        if stage == "fit":
            self.data_output = self.fit(data)
        else:
            LOGGER.warning("Evaluation has not transform, return")

    def split_data_with_type(self, data: list) -> dict:

        split_result = defaultdict(list)
        for value in data:
            if len(value[1]) > 5:
                mode = value[1][5]
            else:
                mode = value[1][4]
            split_result[mode].append(value)

        return split_result

    def evaluate_metircs(self, mode: str, data: list) -> dict:
        labels = []
        pred_scores = []
        pred_labels = []

        for d in data:
            labels.append(d[1][0])
            pred_labels.append(d[1][1])
            pred_scores.append(d[1][2])

        if self.eval_type == consts.BINARY or self.eval_type == consts.REGRESSION:
            if self.pos_label and self.eval_type == consts.BINARY:
                new_labels = []
                for label in labels:
                    if self.pos_label == label:
                        new_labels.append(1)
                    else:
                        new_labels.append(0)
                labels = new_labels

            pred_results = pred_scores
        else:
            pred_results = pred_labels

        eval_result = defaultdict(list)

        metrics = self.metrics

        for eval_metric in metrics:
            res = getattr(self, eval_metric)(labels, pred_results)
            if res is not None:
                try:
                    if math.isinf(res):
                        res = float(-9999999)
                        LOGGER.info("res is inf, set to {}".format(res))
                except:
                    pass

                eval_result[eval_metric].append(mode)
                eval_result[eval_metric].append(res)

        return eval_result

    def fit(self, data, return_result=False):
        if len(data) <= 0:
            return

        self.eval_results.clear()
        for (key, eval_data) in data.items():
            eval_data_local = list(eval_data.collect())
            split_data_with_label = self.split_data_with_type(eval_data_local)
            for mode, data in split_data_with_label.items():
                eval_result = self.evaluate_metircs(mode, data)
                self.eval_results[key].append(eval_result)
            scored_result = self.cal_scord_card_bin(eval_data_local)
            self.eval_results[key].append(scored_result)
            pred_psi = self.evaluate_psi(split_data_with_label)
            if pred_psi:
                self.eval_results[key].append(pred_psi)
        return self.callback_metric_data(return_single_val_metrics=return_result)

    def __save_bin_score_value(self,metric_name, metric_namespace, metric_meta, kv, need_value):
        if kv:
            self.tracker.saveProbBinsResult(metric_name, metric_namespace, metric_meta, kv, need_value)

    def __save_single_value(self, result, metric_name, metric_namespace, eval_name):
        self.tracker.saveMetricData(metric_name, metric_namespace, {'metric_type': 'EVALUATION_SUMMARY'},
                                    [(eval_name, np.round(result, self.round_num))])

    def __save_curve_metric(self, x_axis_list, y_axis_list, metric_name, metric_namespace, metric_type,
                            abscissa_name=None, ordinate_name=None,
                            curve_name=None, best=None, pair_type=None, thresholds=None):
        extra_metas = {}
        metric_type = "_".join([metric_type, "EVALUATION"])
        key_list = ["metric_type", "abscissa_name", "ordinate_name", "curve_name", "best", "pair_type", "thresholds"]
        for key in key_list:
            value = locals()[key]
            if value:
                if key == "thresholds":
                    value = np.round(value, self.round_num).tolist()
                extra_metas[key] = value

        points = []
        for i, value in enumerate(x_axis_list):
            if isinstance(value, float):
                value = np.round(value, self.round_num)
            points.append((value, np.round(y_axis_list[i], self.round_num)))
        points.sort(key=lambda x: x[0])

        self.tracker.saveMetricData(metric_name, metric_namespace, metric_meta=extra_metas, kv=points)

    def eval_results_score(self):
        score_result = {}
        for (data_type, eval_res_list) in self.eval_results.items():
            for eval_res in eval_res_list:
                collect_dict = {}
                for (metric, metric_res) in eval_res.items():
                    metric_namespace = metric_res[0]
                    if metric == consts.R2_SCORE or metric == consts.ACCURACY or metric == consts.AUC:
                        collect_dict[metric] = metric_res[1]
                    elif metric == consts.KS:
                        best_ks, fpr, tpr, thresholds, cuts = metric_res[1]
                        collect_dict[metric] = best_ks

                score_result[metric_namespace] = collect_dict

        return score_result

    def __filt_override_unit_ordinate_coordinate(self, x_sets, y_sets):
        max_y_dict = {}
        for idx, x_value in enumerate(x_sets):
            if x_value not in max_y_dict:
                max_y_dict[x_value] = {"max_y": y_sets[idx], "idx": idx}
            else:
                max_y = max_y_dict[x_value]["max_y"]
                if max_y < y_sets[idx]:
                    max_y_dict[x_value] = {"max_y": y_sets[idx], "idx": idx}

        x = []
        y = []
        idx_list = []
        for key, value in max_y_dict.items():
            x.append(key)
            y.append(value["max_y"])
            idx_list.append(value["idx"])

        return x, y, idx_list

    def __save_roc(self, data_type, metric_name, metric_namespace, metric_res):
        fpr, tpr, thresholds, _ = metric_res

        # set roc edge value
        fpr.append(1.0)
        tpr.append(1.0)

        fpr, tpr, idx_list = self.__filt_override_unit_ordinate_coordinate(fpr, tpr)
        edge_idx = idx_list[-1]
        if edge_idx == len(thresholds):
            idx_list = idx_list[:-1]
        thresholds = [thresholds[idx] for idx in idx_list]

        self.__save_curve_metric(fpr, tpr, metric_name=metric_name, metric_namespace=metric_namespace,
                                 metric_type="ROC", abscissa_name="fpr", ordinate_name="tpr",
                                 curve_name=data_type, thresholds=thresholds)

    def __save_topn(self, metric_name, metric_namespace, metric_res):
        metric_type = "TOPN"
        metric_type = "_".join([metric_type, "EVALUATION"])
        key_list = ["name", "cut_off", "TP", "total", "acc", "recall"]
        res_list = []
        for mectric in metric_res:
            metric_dict = dict(zip(key_list, mectric))
            res_list.append(metric_dict)

        self.tracker.save_metric_data_to_task_result(metric_name, metric_namespace,
                                                     metric_meta={"metric_type": metric_type}, kv={"topn": res_list},
                                                     need_value=False)

    def __save_psi(self, metric_name, metric_namespace, metric_res):
        metric_type = "_".join(["PSI", "EVALUATION"])
        self.tracker.save_metric_data_to_task_result(metric_name, metric_namespace,
                                                     metric_meta={"metric_type": metric_type}, kv=metric_res,
                                                     need_value=False)

    def __save_scored(self, metric_name, metric_namespace, metric_res):
        metric_type = "_".join(["SCORED", "EVALUATION"])
        self.__save_bin_score_value(metric_name=metric_name, metric_namespace=metric_namespace,
                                    metric_meta={"metric_type": metric_type}, kv=metric_res,
                                    need_value=False)

    def callback_metric_data(self, return_single_val_metrics=False):

        """
        Parameters
        ----------
        return_single_val_metrics if True return single_val_metrics

        Returns None or return_result dict
        -------
        """

        collect_dict = {}
        LOGGER.debug('callback metric called')

        for (data_type, eval_res_list) in self.eval_results.items():

            precision_recall = {}
            for eval_res in eval_res_list:
                for (metric, metric_res) in eval_res.items():
                    metric_namespace = metric_res[0]

                    if metric_namespace == 'validate':
                        collect_dict = self.validate_metric
                    elif metric_namespace == 'train':
                        collect_dict = self.train_metric

                    metric_name = '_'.join([data_type, metric])

                    if metric in self.save_single_value_metric_list:
                        self.__save_single_value(metric_res[1], metric_name=data_type, metric_namespace=metric_namespace
                                                 , eval_name=metric)
                        collect_dict[metric] = metric_res[1]

                    elif metric == consts.KS:
                        best_ks, fpr, tpr, thresholds, cuts = metric_res[1]
                        self.__save_single_value(best_ks, metric_name=data_type,
                                                 metric_namespace=metric_namespace,
                                                 eval_name=metric)
                        collect_dict[metric] = best_ks

                        metric_name_fpr = '_'.join([metric_name, "fpr"])
                        curve_name_fpr = "_".join([data_type, "fpr"])
                        self.__save_curve_metric(cuts, fpr, metric_name=metric_name_fpr,
                                                 metric_namespace=metric_namespace,
                                                 metric_type=metric.upper(), abscissa_name="",
                                                 curve_name=curve_name_fpr, pair_type=data_type, thresholds=thresholds)

                        metric_name_tpr = '_'.join([metric_name, "tpr"])
                        curve_name_tpr = "_".join([data_type, "tpr"])
                        self.__save_curve_metric(cuts, tpr, metric_name_tpr, metric_namespace, metric.upper(),
                                                 abscissa_name="",
                                                 curve_name=curve_name_tpr, pair_type=data_type, thresholds=thresholds)

                    elif metric == consts.ROC:
                        self.__save_roc(data_type, metric_name, metric_namespace, metric_res[1])

                    elif metric == consts.TOPN:
                        self.__save_topn(metric_name, metric_namespace, metric_res[1])

                    elif metric in [consts.ACCURACY, consts.LIFT, consts.GAIN]:
                        if self.eval_type == consts.MULTY and metric == consts.ACCURACY:
                            self.__save_single_value(metric_res[1], metric_name=data_type,
                                                     metric_namespace=metric_namespace,
                                                     eval_name=metric)
                            collect_dict[metric] = metric_res[1]
                            continue

                        score, cuts, thresholds = metric_res[1]

                        if metric in [consts.LIFT, consts.GAIN]:
                            score = [float(s[1]) for s in score]
                            cuts = [float(c[1]) for c in cuts]
                            cuts, score, idx_list = self.__filt_override_unit_ordinate_coordinate(cuts, score)
                            thresholds = [thresholds[idx] for idx in idx_list]

                            score.append(1.0)
                            cuts.append(1.0)
                            thresholds.append(0.0)

                        self.__save_curve_metric(cuts, score, metric_name=metric_name,
                                                 metric_namespace=metric_namespace,
                                                 metric_type=metric.upper(), abscissa_name="",
                                                 curve_name=data_type, thresholds=thresholds)

                    elif metric in [consts.PRECISION, consts.RECALL]:
                        precision_recall[metric] = metric_res
                        if len(precision_recall) < 2:
                            continue

                        precision_res = precision_recall.get(consts.PRECISION)
                        recall_res = precision_recall.get(consts.RECALL)

                        if precision_res[0] != recall_res[0]:
                            LOGGER.warning(
                                "precision mode:{} is not equal to recall mode:{}".format(precision_res[0],
                                                                                          recall_res[0]))
                            continue

                        metric_namespace = precision_res[0]
                        metric_name_precision = '_'.join([data_type, "precision"])
                        metric_name_recall = '_'.join([data_type, "recall"])

                        pos_precision_score = precision_res[1][0]
                        precision_cuts = precision_res[1][1]
                        if len(precision_res[1]) >= 3:
                            precision_thresholds = precision_res[1][2]
                        else:
                            precision_thresholds = None

                        pos_recall_score = recall_res[1][0]
                        recall_cuts = recall_res[1][1]

                        if len(recall_res[1]) >= 3:
                            recall_thresholds = recall_res[1][2]
                        else:
                            recall_thresholds = None

                        precision_curve_name = data_type
                        recall_curve_name = data_type
                        if self.eval_type == consts.BINARY:
                            pos_precision_score = [score[1] for score in pos_precision_score]
                            pos_recall_score = [score[1] for score in pos_recall_score]

                            pos_recall_score, pos_precision_score, idx_list = self.__filt_override_unit_ordinate_coordinate(
                                pos_recall_score, pos_precision_score)

                            precision_cuts = [precision_cuts[idx] for idx in idx_list]
                            recall_cuts = [recall_cuts[idx] for idx in idx_list]

                            edge_idx = idx_list[-1]
                            if edge_idx == len(precision_thresholds) - 1:
                                idx_list = idx_list[:-1]
                            precision_thresholds = [precision_thresholds[idx] for idx in idx_list]
                            recall_thresholds = [recall_thresholds[idx] for idx in idx_list]

                        elif self.eval_type == consts.MULTY:
                            average_precision = float(np.array(pos_precision_score).mean())
                            average_recall = float(np.array(pos_recall_score).mean())
                            self.__save_single_value(average_precision, metric_name=data_type,
                                                     metric_namespace=metric_namespace,
                                                     eval_name="precision")
                            self.__save_single_value(average_recall, metric_name=data_type,
                                                     metric_namespace=metric_namespace,
                                                     eval_name="recall")
                            collect_dict[consts.PRECISION] = average_precision
                            collect_dict[consts.RECALL] = average_recall

                            precision_curve_name = metric_name_precision
                            recall_curve_name = metric_name_recall

                        self.__save_curve_metric(precision_cuts, pos_precision_score, metric_name_precision,
                                                 metric_namespace,
                                                 "_".join([consts.PRECISION.upper(), self.eval_type.upper()]),
                                                 abscissa_name="", ordinate_name="Precision",
                                                 curve_name=precision_curve_name,
                                                 pair_type=data_type, thresholds=precision_thresholds)

                        self.__save_curve_metric(recall_cuts, pos_recall_score, metric_name_recall, metric_namespace,
                                                 "_".join([consts.RECALL.upper(), self.eval_type.upper()]),
                                                 abscissa_name="", ordinate_name="Recall", curve_name=recall_curve_name,
                                                 pair_type=data_type, thresholds=recall_thresholds)

                    elif metric == consts.SCORED:
                        self.__save_scored(metric_name, metric_namespace, metric_res[1])

                    elif metric == consts.PSI:
                        self.__save_psi(metric_name, metric_namespace, metric_res[1])

                    else:
                        LOGGER.warning("Unknown metric:{}".format(metric))

        if return_single_val_metrics:
            if len(self.validate_metric) != 0:
                LOGGER.debug("return validate metric")
                LOGGER.debug('validate metric is {}'.format(self.validate_metric))
                return self.validate_metric
            else:
                LOGGER.debug("validate metric is empty, return train metric")
                LOGGER.debug('train metric is {}'.format(self.train_metric))
                return self.train_metric

    def __filt_threshold(self, thresholds, step):
        cuts = list(map(float, np.arange(0, 1, step)))
        size = len(list(thresholds))
        thresholds.sort(reverse=True)
        index_list = [int(size * cut) for cut in cuts]
        new_thresholds = [thresholds[idx] for idx in index_list]

        return new_thresholds, cuts

    def topn(self, labels, pred_scores):
        '''
        columns=['TOP N', 'CutOff', 'real', 'num', 'accuracy', 'recall rate']
        '''
        if self.eval_type != consts.BINARY:
            logging.warning("topn is just suppose Binary Classification! return None as results")
            return None

        index_map = {
            '95%': 'TOP 5%',
            '90%': 'TOP 10%',
            '85%': 'TOP 15%',
            '80%': 'TOP 20%',
            '70%': 'TOP 30%',
            '50%': 'TOP 50%'
        }
        df = pd.DataFrame()
        df['label'] = labels
        df['score'] = pred_scores
        bad_num = df[df.label == 1].shape[0]
        desc = df.score.describe(percentiles=[0.7, 0.8, 0.85, 0.9, 0.95])
        desc = desc[4:10]
        res = []
        for index in desc.index:
            dd = df[df.score >= desc[index]]
            num = dd.shape[0]
            real = dd[dd.label == 1].shape[0]
            if num <= 0:
                continue
            res.append((
                index_map[index],
                '%.4f' % desc[index],
                # desc[index],
                real,
                num,
                '%.2f%%' % (real * 100 / num),
                '%.2f%%' % (real * 100 / bad_num)))
        res.reverse()
        return res

    def auc(self, labels, pred_scores):
        """
        Compute AUC for binary classification.

        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: value list. The predict results of model. It should be corresponding to labels each data.

        Returns
        ----------
        float
            The AUC
        """
        if self.eval_type == consts.BINARY:
            return roc_auc_score(labels, pred_scores)
        else:
            logging.warning("auc is just suppose Binary Classification! return None as results")
            return None

    def explained_variance(self, labels, pred_scores):
        """
        Compute explain variance
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: value list. The predict results of model. It should be corresponding to labels each data.

        Returns
        ----------
        float
            The explain variance
        """
        return explained_variance_score(labels, pred_scores)

    def mean_absolute_error(self, labels, pred_scores):
        """
        Compute mean absolute error
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        float
            A non-negative floating point.
        """
        return mean_absolute_error(labels, pred_scores)

    def mean_squared_error(self, labels, pred_scores):
        """
        Compute mean square error
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        float
            A non-negative floating point value
        """
        return mean_squared_error(labels, pred_scores)

    def mean_squared_log_error(self, labels, pred_scores):
        """
        Compute mean squared logarithmic error
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        float
            A non-negative floating point value
        """
        return mean_squared_log_error(labels, pred_scores)

    def median_absolute_error(self, labels, pred_scores):
        """
        Compute median absolute error
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        float
            A positive floating point value
        """
        return median_absolute_error(labels, pred_scores)

    def r2_score(self, labels, pred_scores):
        """
        Compute R^2 (coefficient of determination) score
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        float
            The R^2 score
        """
        return r2_score(labels, pred_scores)

    def root_mean_squared_error(self, labels, pred_scores):
        """
        Compute the root of mean square error
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Return
        ----------
        float
            A positive floating point value
        """
        return np.sqrt(mean_squared_error(labels, pred_scores))

    def roc(self, labels, pred_scores):
        if self.eval_type == consts.BINARY:
            fpr, tpr, thresholds = roc_curve(np.array(labels), np.array(pred_scores), drop_intermediate=1)
            fpr, tpr, thresholds = list(map(float, fpr)), list(map(float, tpr)), list(map(float, thresholds))

            filt_thresholds, cuts = self.__filt_threshold(thresholds=thresholds, step=0.01)
            new_thresholds = []
            new_tpr = []
            new_fpr = []
            for threshold in filt_thresholds:
                index = thresholds.index(threshold)
                new_tpr.append(tpr[index])
                new_fpr.append(fpr[index])
                new_thresholds.append(threshold)

            fpr = new_fpr
            tpr = new_tpr
            thresholds = new_thresholds
            return fpr, tpr, thresholds, cuts
        else:
            logging.warning("roc_curve is just suppose Binary Classification! return None as results")
            fpr, tpr, thresholds, cuts = None, None, None, None

            return fpr, tpr, thresholds, cuts

    def ks(self, labels, pred_scores):
        """
        Compute Kolmogorov-Smirnov
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        max_ks_interval: float max value of each tpr - fpt
        fpr:
        """
        score_label_list = []
        for i, label in enumerate(labels):
            score_label_list.append((pred_scores[i], label))

        score_label_list.sort(key=lambda x: x[0], reverse=True)
        cuts = [c / 100 for c in range(100)]
        data_size = len(pred_scores)
        indexs = [int(data_size * cut) for cut in cuts]
        score_threshold = [score_label_list[idx][0] for idx in indexs]

        fpr = []
        tpr = []
        ks = []
        for i, index in enumerate(indexs):
            positive = 0
            positive_recall = 0
            negative = 0
            false_positive = 0
            for score_label in score_label_list:
                pre_score = score_label[0]
                label = score_label[1]
                if label == self.pos_label:
                    positive += 1

                    if pre_score > score_threshold[i]:
                        positive_recall += 1

                if label == 0:
                    negative += 1
                    if pre_score > score_threshold[i]:
                        false_positive += 1

            if positive == 0 or negative == 0:
                raise ValueError("all labels are positive or negative, please check your data!")

            _tpr = positive_recall / positive
            _fpr = false_positive / negative
            _ks = _tpr - _fpr
            tpr.append(_tpr)
            fpr.append(_fpr)
            ks.append(_ks)

        fpr.append(1.0)
        tpr.append(1.0)
        cuts.append(1.0)

        return max(ks), fpr, tpr, score_threshold, cuts

    def lift(self, labels, pred_scores):
        """
        Compute lift of binary classification.
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        float
            The lift
        """
        if self.eval_type == consts.BINARY:
            thresholds = list(set(pred_scores))
            thresholds, cuts = self.__filt_threshold(thresholds, 0.01)
            lift_operator = Lift()
            lift_y, lift_x = lift_operator.compute(labels, pred_scores, thresholds=thresholds)
            return lift_y, lift_x, thresholds
        else:
            logging.warning("lift is just suppose Binary Classification! return None as results")
            return None

    def gain(self, labels, pred_scores):
        """
        Compute gain of binary classification.
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        float
            The gain
        """

        if self.eval_type == consts.BINARY:
            thresholds = list(set(pred_scores))
            thresholds, cuts = self.__filt_threshold(thresholds, 0.01)
            gain_operator = Gain()
            gain_x, gain_y = gain_operator.compute(labels, pred_scores, thresholds=thresholds)
            return gain_y, gain_x, thresholds
        else:
            logging.warning("gain is just suppose Binary Classification! return None as results")
            return None

    def precision(self, labels, pred_scores):
        """
        Compute the precision
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        dict
            The key is threshold and the value is another dic, which key is label in parameter labels, and value is the label's precision.
        """
        if self.eval_type == consts.BINARY:
            thresholds = list(set(pred_scores))
            thresholds, cuts = self.__filt_threshold(thresholds, 0.01)

            # set for recall edge value
            thresholds.append(min(thresholds) - 0.001)
            cuts.append(1)

            precision_operator = BiClassPrecision()
            precision_res, thresholds = precision_operator.compute(labels, pred_scores, thresholds)
            return precision_res, cuts, thresholds
        elif self.eval_type == consts.MULTY:
            precision_operator = MultiClassPrecision()
            return precision_operator.compute(labels, pred_scores)
        else:
            logging.warning("error:can not find classification type:{}".format(self.eval_type))

    def recall(self, labels, pred_scores):
        """
        Compute the recall
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        Returns
        ----------
        dict
            The key is threshold and the value is another dic, which key is label in parameter labels, and value is the label's recall.
        """
        if self.eval_type == consts.BINARY:
            thresholds = list(set(pred_scores))
            thresholds, cuts = self.__filt_threshold(thresholds, 0.01)

            # set for recall edge value
            thresholds.append(min(thresholds) - 0.001)
            cuts.append(1)

            recall_operator = BiClassRecall()
            recall_res, thresholds = recall_operator.compute(labels, pred_scores, thresholds)
            return recall_res, cuts, thresholds
        elif self.eval_type == consts.MULTY:
            recall_operator = MultiClassRecall()
            return recall_operator.compute(labels, pred_scores)
        else:
            logging.warning("error:can not find classification type:{}".format(self.eval_type))

    def accuracy(self, labels, pred_scores, normalize=True):
        """
        Compute the accuracy
        Parameters
        ----------
        labels: value list. The labels of data set.
        pred_scores: pred_scores: value list. The predict results of model. It should be corresponding to labels each data.
        normalize: bool. If true, return the fraction of correctly classified samples, else returns the number of correctly classified samples
        Returns
        ----------
        dict
            the key is threshold and the value is the accuracy of this threshold.
        """

        if self.eval_type == consts.BINARY:
            thresholds = list(set(pred_scores))
            thresholds, cuts = self.__filt_threshold(thresholds, 0.01)
            acc_operator = BiClassAccuracy()
            acc_res, thresholds = acc_operator.compute(labels, pred_scores, thresholds, normalize)
            return acc_res, cuts, thresholds
        elif self.eval_type == consts.MULTY:
            acc_operator = MultiClassAccuracy()
            return acc_operator.compute(labels, pred_scores, normalize)
        else:
            logging.warning("error:can not find classification type:".format(self.eval_type))

    def cal_scord_card_bin(self, eval_data_local):
        score_result = self.tracker.get_score_result()
        bins_result = None
        if score_result is not None and len(eval_data_local[0][1])>=6:
            a_score, b_score= score_result['a_score'], score_result['b_score']
            linear_scores = [data[1][4] for data in eval_data_local]
            sample_scores = [a_score + b_score * linear_score for linear_score in linear_scores]
            bins_result  = self.to_binning(sample_scores)
        else:
            classes = len(set([d[1][0] for d in eval_data_local]))
            if classes < 3 and self.model_param.prob_need_to_bin:
                sample_pro_result_list = []
                for index, sample_pro_result in enumerate(eval_data_local):
                    sample_pro_result_list.append(sample_pro_result[1][2])
                bins_result = self.to_binning(sample_pro_result_list)
        scored = defaultdict(list)
        scored['scored'] = ['train_validate', bins_result]
        return scored

    def evaluate_psi(self, data):
        pred_result = defaultdict(list)
        for mode, mode_data in data.items():
            label= []
            pred_scores = []
            for d in mode_data:
                pred_scores.append(d[1][2])
                label.append(d[1][0])
            self.get_classify(label)
            pred_result[mode] = pred_scores
        train_pred_score = pred_result.get('train')
        eval_pred_score = pred_result.get('validate')
        if eval_pred_score and train_pred_score and self.component_parameters and \
                self.component_parameters.get('module')=='Evaluation':
            train_bin_values, train_split_point = self.get_bin_result(train_pred_score)
            LOGGER.debug('train_bin_values and train_split_point'.format(train_bin_values, train_split_point))
            train_bin_results = self.cal_bin_rate(train_bin_values)
            eval_bin_values = pd.cut(eval_pred_score, train_split_point)
            eval_bin_results = self.cal_bin_rate(eval_bin_values)
            train_bin_results, eval_bin_results =  self.base_psi.check_bin_result(train_bin_results, eval_bin_results)
            bin_psi_list, feature_psi = self.base_psi.cal_psi(train_bin_results.get('count_rate'),
                                                              eval_bin_results.get('count_rate'))

            train_bin_results['bin_psi'] = bin_psi_list
            train_bin_results['feature_psi'] = feature_psi
            psi_values = defaultdict(list)
            train_test_result = {
                'train_pred_label_static': train_bin_results,
                'test_pred_label_static': eval_bin_results,
                'split_point': list(train_split_point),
                'bin_cal_results': bin_psi_list,
                'pred_label_psi': feature_psi
            }
            psi_values['psi'] = ['train_validate', train_test_result]
            return psi_values
        return None

    def to_binning(self, to_bin_data):
            data_count = len(to_bin_data)
            bin_values, split_point = self.get_bin_result(to_bin_data)
            bin_values_counts = bin_values.value_counts()
            bin_result ={}
            staitic_count = sum([ bin_values_counts[i] for i in range(len(bin_values_counts))])
            if staitic_count == data_count:
                for i in range(len(bin_values_counts)):
                    per_bin_result = { "count" : int(bin_values_counts[i]),
                             "count_rate": float(bin_values_counts[i] / data_count) }
                    bin_result[str(np.round(split_point[i+1], 4))] = per_bin_result
            else:
                return ValueError("Staitic_count and count are not the same, check the binning statistics!")
            scores_distribution ={
                "bin_method": self.model_param.bin_method,
                "bin_result": bin_result,
                "max": max(to_bin_data),
                "min": min(to_bin_data)}
            return scores_distribution

    def get_bin_result(self, to_bin_values):
        bin_values, split_point= None, None
        if self.model_param.bin_method == consts.CUSTOM:
            LOGGER.debug('split_points is {}'.format(self.model_param.split_points))
            bin_values, split_point = pd.cut(to_bin_values, self.model_param.split_points,
                                                         retbins=True)
        elif self.model_param.bin_method == consts.BUCKET:
            bin_values, split_point = pd.cut(to_bin_values, bins=self.model_param.bin_num,
                                                         retbins=True)
        elif self.model_param.bin_method == consts.QUANTILE:
            bin_values, split_point = pd.qcut(np.array(to_bin_values, dtype=float),self.model_param.bin_num,
                                              duplicates='drop',retbins= True)

        return bin_values, split_point

    def get_classify(self, label):
        label_class = set(label)
        if len(label_class) > 2:
            return

    @staticmethod
    def cal_bin_rate(bin_values):
        per_bin_result = {}
        count_list = []
        count_rate_list = []
        data_count = len(bin_values)
        bin_values_counts = bin_values.value_counts()
        for i in range(len(bin_values_counts)):
            count_list.append(int(bin_values_counts[i]))
            count_rate_list.append(float(bin_values_counts[i] / data_count))
        per_bin_result['count'] = count_list
        per_bin_result["count_rate"] = count_rate_list
        per_bin_result['total_count'] = sum(count_list)
        return per_bin_result

    @staticmethod
    def extract_data(data: dict):
        return data


class Lift(object):
    """
    Compute lift
    """

    def __predict_value_to_one_hot(self, pred_value, threshold):
        one_hot = []
        for value in pred_value:
            if value > threshold:
                one_hot.append(1)
            else:
                one_hot.append(0)

        return one_hot

    def __compute_lift(self, labels, pred_scores_one_hot, pos_label="1"):
        tn, fp, fn, tp = confusion_matrix(labels, pred_scores_one_hot).ravel()

        if pos_label == '0':
            tp, tn = tn, tp
            fp, fn = fn, fp

        labels_num = len(labels)
        if labels_num == 0:
            lift_x = 1
            denominator = 1
        else:
            lift_x = (tp + fp) / labels_num
            denominator = (tp + fn) / labels_num

        if tp + fp == 0:
            numerator = 1
        else:
            numerator = tp / (tp + fp)

        if denominator == 0:
            lift_y = sys.float_info.max
        else:
            lift_y = numerator / denominator

        return lift_x, lift_y

    def compute(self, labels, pred_scores, thresholds=None):
        lifts_x = []
        lifts_y = []

        for threshold in thresholds:
            pred_scores_one_hot = self.__predict_value_to_one_hot(pred_scores, threshold)
            label_type = ['0', '1']
            lift_x_type = []
            lift_y_type = []
            for lt in label_type:
                lift_x, lift_y = self.__compute_lift(labels, pred_scores_one_hot, pos_label=lt)
                lift_x_type.append(lift_x)
                lift_y_type.append(lift_y)
            lifts_x.append(lift_x_type)
            lifts_y.append(lift_y_type)

        return lifts_y, lifts_x


class Gain(object):
    """
    Compute Gain
    """

    def __init__(self):
        pass

    def __predict_value_to_one_hot(self, pred_value, threshold):
        one_hot = []
        for value in pred_value:
            if value > threshold:
                one_hot.append(1)
            else:
                one_hot.append(0)

        return one_hot

    def __compute_gain(self, label, pred_scores_one_hot, pos_label="1"):
        tn, fp, fn, tp = confusion_matrix(label, pred_scores_one_hot).ravel()

        if pos_label == '0':
            tp, tn = tn, tp
            fp, fn = fn, fp

        num_label = len(label)
        if num_label == 0:
            gain_x = 1
        else:
            gain_x = float((tp + fp) / num_label)

        num_positives = tp + fn
        if num_positives == 0:
            gain_y = 1
        else:
            gain_y = float(tp / num_positives)

        return gain_x, gain_y

    def compute(self, labels, pred_scores, thresholds=None):
        gains_x = []
        gains_y = []

        for threshold in thresholds:
            pred_scores_one_hot = self.__predict_value_to_one_hot(pred_scores, threshold)
            label_type = ['0', '1']
            gain_x_type = []
            gain_y_type = []
            for lt in label_type:
                gain_x, gain_y = self.__compute_gain(labels, pred_scores_one_hot, pos_label=lt)
                gain_x_type.append(gain_x)
                gain_y_type.append(gain_y)
            gains_x.append(gain_x_type)
            gains_y.append(gain_y_type)

        return gains_x, gains_y


class BiClassPrecision(object):
    """
    Compute binary classification precision
    """

    def __init__(self):
        self.total_positives = 0

    def __predict_value_to_one_hot(self, pred_value, threshold):
        one_hot = []
        self.total_positives = 0
        for value in pred_value:
            if value > threshold:
                one_hot.append(1)
                self.total_positives += 1
            else:
                one_hot.append(0)

        return one_hot

    def compute(self, labels, pred_scores, thresholds):
        scores = []
        for threshold in thresholds:
            pred_scores_one_hot = self.__predict_value_to_one_hot(pred_scores, threshold)
            score = list(map(float, precision_score(labels, pred_scores_one_hot, average=None)))
            if self.total_positives == 0:
                score[1] = 1.0
            scores.append(score)

        return scores, thresholds


class MultiClassPrecision(object):
    """
    Compute multi-classification precision
    """

    def compute(self, labels, pred_scores):
        all_labels = list(set(labels).union(set(pred_scores)))
        all_labels.sort()
        return precision_score(labels, pred_scores, average=None), all_labels


class BiClassRecall(object):
    """
    Compute binary classification recall
    """

    def __predict_value_to_one_hot(self, pred_value, threshold):
        one_hot = []
        for value in pred_value:
            if value > threshold:
                one_hot.append(1)
            else:
                one_hot.append(0)

        return one_hot

    def compute(self, labels, pred_scores, thresholds):
        scores = []

        for threshold in thresholds:
            pred_scores_one_hot = self.__predict_value_to_one_hot(pred_scores, threshold)
            score = list(map(float, recall_score(labels, pred_scores_one_hot, average=None)))
            scores.append(score)

        return scores, thresholds


class MultiClassRecall(object):
    """
    Compute multi-classification recall
    """

    def compute(self, labels, pred_scores):
        all_labels = list(set(labels).union(set(pred_scores)))
        all_labels.sort()
        return recall_score(labels, pred_scores, average=None), all_labels


class BiClassAccuracy(object):
    """
    Compute binary classification accuracy
    """

    def __predict_value_to_one_hot(self, pred_value, threshold):
        one_hot = []
        for value in pred_value:
            if value > threshold:
                one_hot.append(1)
            else:
                one_hot.append(0)

        return one_hot

    def compute(self, labels, pred_scores, thresholds, normalize=True):
        scores = []
        for threshold in thresholds:
            pred_scores_one_hot = self.__predict_value_to_one_hot(pred_scores, threshold)
            score = accuracy_score(labels, pred_scores_one_hot, normalize)
            scores.append(score)

        return scores, thresholds


class MultiClassAccuracy(object):
    """
    Compute multi-classification accuracy
    """

    def compute(self, labels, pred_scores, normalize=True):
        return accuracy_score(labels, pred_scores, normalize)


class IC(object):
    """
    Compute Information Criterion with a given dTable and loss
        When k = 2, result is genuine AIC;
        when k = log(n), results is BIC, also called SBC, SIC, SBIC.
    """

    def compute(self, k, n, dfe, loss):
        aic_score = k * dfe + 2 * n * loss
        return aic_score


class IC_Approx(object):
    """
    Compute Information Criterion value with a given dTable and loss
        When k = 2, result is genuine AIC;
        when k = log(n), results is BIC, also called SBC, SIC, SBIC.
        Note that this formula for linear regression dismisses the constant term n * np.log(2 * np.pi) for sake of simplicity, so the absolute value of result will be small.
    """

    def compute(self, k, n, dfe, loss):
        aic_score = k * dfe + n * np.log(loss * 2)
        return aic_score
