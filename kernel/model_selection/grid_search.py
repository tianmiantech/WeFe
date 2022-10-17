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
import itertools

from common.python.utils import log_utils
from kernel.components.evaluation.evaluation import Evaluation
from kernel.model_selection import start_cross_validation
from kernel.model_selection.evaluate import evaluate
from kernel.model_selection.k_fold import KFold
from kernel.transfer.variables.transfer_class.grid_search_transfer_variable import GridSearchTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class GridSearch(object):
    def __init__(self):
        super(GridSearch, self).__init__()
        self.model_param = None
        self.params_list = {}

    def _init_model(self, param):
        self.model_param = param
        self.params_list = param.params_list
        self.mode = param.mode
        self.role = param.role

    def _sync_best_parameters(self, best_iter, best_parameters, flowid):

        transfer_variable = GridSearchTransferVariable()

        if self.role == consts.PROMOTER:
            transfer_variable.best_iter.remote(best_iter,
                                               role=consts.PROVIDER,
                                               idx=-1,
                                               suffix=(flowid,))
            transfer_variable.best_parameters.remote(best_parameters,
                                                     role=consts.PROVIDER,
                                                     idx=-1,
                                                     suffix=(flowid,))
            LOGGER.info("grid search remote {},{} to provider".format(best_iter, best_parameters))

        elif self.role == consts.PROVIDER:
            best_iter = transfer_variable.best_iter.get(idx=0,
                                                        suffix=(flowid,))
            best_parameters = transfer_variable.best_parameters.get(idx=0,
                                                                    suffix=(flowid,))
            LOGGER.info("grid search get {}, {} from promoter".format(best_iter, best_parameters))

        return best_iter, best_parameters

    def run(self, component_parameters, train_data, validate_data, original_model, need_cv, provider_do_evaluate):
        self._init_model(component_parameters)

        iter_names = list(self.params_list.keys())
        iter_values = [list(value) for value in itertools.product(*self.params_list.values())]

        best_score = -1.0
        best_parameters = {}
        final_output_data = None
        models = []
        best_iter = 0

        original_model.tracker.init_task_progress(len(iter_values))
        for i, params in enumerate(iter_values):
            LOGGER.info("enter grid search iter is: {}".format(i))
            model = copy.deepcopy(original_model)
            parameters = model.set_grid_search_params(iter_names, params)
            model.set_grid_search_iter(i)
            if need_cv:
                kflod_obj = KFold()
                cv_param = start_cross_validation.get_cv_param(model)
                output_data = kflod_obj.run(cv_param, train_data, model, provider_do_evaluate, i)
                score = kflod_obj.get_mean_score()
            else:
                score, output_data = self.single_run(model, train_data, validate_data, i, provider_do_evaluate)

            if score > best_score:
                best_iter = i
                best_score = score
                best_parameters = parameters
                final_output_data = output_data

            models.append(model)
            original_model.tracker.add_task_progress(1)

        best_iter, best_parameters = self._sync_best_parameters(best_iter, best_parameters, 'grid_search.best.0')
        best_model = models[best_iter]
        if need_cv:
            score, final_output_data = self.single_run(best_model, train_data, validate_data, 'final.0',
                                                       provider_do_evaluate, False)

        original_model.set_model_output(best_model.export_model())
        self.callback_best_parameters(original_model, best_iter, best_parameters, self.params_list)

        LOGGER.info("Best iter:{}".format(best_iter))
        LOGGER.info("Best score:{:.2f}".format(best_score))
        LOGGER.info("Best parameters:{}".format(best_parameters))
        return final_output_data

    def callback_best_parameters(self, model, best_iter, best_parameters, params_list):
        metric_meta = {'metric_type': 'grid_search'}
        best_results = []
        best_results.append(('best_iter', best_iter))
        best_results.append(('best_parameters', best_parameters))
        model.tracker.saveMetricData(metric_name='best_parameters',
                                     metric_namespace='train',
                                     metric_meta=metric_meta,
                                     kv=best_results)
        model.tracker.saveMetricData(metric_name='params_list',
                                     metric_namespace='train',
                                     metric_meta=metric_meta,
                                     kv=("params_list", params_list))

    def single_run(self, model, train_data, validate_data, flowid, provider_do_evaluate, need_eval=True):
        if train_data is None:
            return self._arbiter_run(model, flowid)

        LOGGER.info("single grid search iter is: {}".format(flowid))
        this_flowid = 'train.' + str(flowid)
        LOGGER.debug("In grid search, set_flowid flowid is : {}".format(this_flowid))
        model.set_flowid(this_flowid)
        model.fit(train_data, validate_data)

        this_flowid = 'predict_train.' + str(flowid)
        LOGGER.debug("In grid search, set_flowid flowid is : {}".format(this_flowid))
        model.set_flowid(this_flowid)
        train_pred_res = model.predict(train_data)

        if validate_data is not None:
            this_flowid = 'predict_validate.' + str(flowid)
            LOGGER.debug("In grid search, set_flowid flowid is : {}".format(this_flowid))
            model.set_flowid(this_flowid)
            test_pred_res = model.predict(validate_data)

        score = 0.0
        if self.role == consts.PROMOTER or provider_do_evaluate:
            grid_name = "_".join(['grid_search', str(flowid)])
            train_pred_res = train_pred_res.mapValues(lambda value: value + ['train'])
            train_pred_res = model.set_predict_data_schema(train_pred_res, train_data.schema)
            # LOGGER.debug(f"train_pred_res schema: {train_pred_res.schema}")
            if validate_data is not None:
                test_pred_res = test_pred_res.mapValues(lambda value: value + ['validate'])
                test_pred_res = model.set_predict_data_schema(test_pred_res, validate_data.schema)
                train_pred_res = train_pred_res.union(test_pred_res)

            if need_eval:
                score = evaluate(train_pred_res, grid_name, model)
        LOGGER.debug("Finish grid search iter : {}".format(flowid))

        return score, train_pred_res

    def _arbiter_run(self, model, flowid):
        LOGGER.info("grid search flowid is: {}".format(flowid))
        this_flowid = 'train.' + str(flowid)
        model.set_flowid(this_flowid)
        model.fit(None)

        this_flowid = 'predict_train.' + str(flowid)
        model.set_flowid(this_flowid)
        model.predict(None)

        this_flowid = 'predict_validate.' + str(flowid)
        model.set_flowid(this_flowid)
        model.predict(None)

        return 0.0, None