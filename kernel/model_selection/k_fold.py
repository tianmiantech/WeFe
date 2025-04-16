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

import numpy as np

from common.python import session
from common.python.utils import log_utils
from kernel.components.evaluation.evaluation import Evaluation
from kernel.model_selection.cross_validate import BaseCrossValidator
from kernel.model_selection.indices import collect_index
from kernel.transfer.variables.transfer_class.cross_validation_transfer_variable import CrossValidationTransferVariable
from kernel.utils import consts
from kernel.model_selection.evaluate import evaluate

LOGGER = log_utils.get_logger()


class KFold(BaseCrossValidator):
    def __init__(self):
        super(KFold, self).__init__()
        self.model_param = None
        self.n_splits = 1
        self.shuffle = True
        self.random_seed = 1
        self.output_fold_history = False
        self.fold_history = None
        self.history_value_type = "score"
        self.mean_score = 0.0

    def _init_model(self, param):
        self.model_param = param
        self.n_splits = param.n_splits
        self.mode = param.mode
        self.role = param.role
        self.shuffle = param.shuffle
        self.random_seed = param.random_seed
        # self.evaluate_param = param.evaluate_param
        # np.random.seed(self.random_seed)

    def split(self, data_inst):
        header = data_inst.schema.get('header')

        data_sids_iter, data_size = collect_index(data_inst)
        data_sids = []
        key_type = None
        for sid, _ in data_sids_iter:
            if key_type is None:
                key_type = type(sid)
            data_sids.append(sid)
        data_sids = np.array(data_sids)
        # if self.shuffle:
        #     np.random.shuffle(data_sids)

        from sklearn.model_selection import KFold as sk_KFold
        kf = sk_KFold(n_splits=self.n_splits, shuffle=self.shuffle, random_state=self.random_seed)

        n = 0
        for train, test in kf.split(data_sids):
            train_sids = data_sids[train]
            test_sids = data_sids[test]

            n += 1

            train_sids_table = [(key_type(x), 1) for x in train_sids]
            test_sids_table = [(key_type(x), 1) for x in test_sids]
            # print(train_sids_table)
            train_table = session.parallelize(train_sids_table,
                                              include_key=True,
                                              partition=data_inst._partitions)
            train_data = data_inst.join(train_table, lambda x, y: x)

            test_table = session.parallelize(test_sids_table,
                                             include_key=True,
                                             partition=data_inst._partitions)
            test_data = data_inst.join(test_table, lambda x, y: x)
            train_data.schema['header'] = header
            test_data.schema['header'] = header
            yield train_data, test_data

    @staticmethod
    def generate_new_id(id, fold_num, data_type):
        return f"{id}#fold{fold_num}#{data_type}"

    def transform_history_data(self, data, predict_data, fold_num, data_type):
        if self.history_value_type == "score":
            if predict_data is not None:
                history_data = predict_data.map(lambda k, v: (KFold.generate_new_id(k, fold_num, data_type), v))
                history_data.schema = copy.deepcopy(predict_data.schema)
            else:
                history_data = data.map(lambda k, v: (KFold.generate_new_id(k, fold_num, data_type), fold_num))
                schema = copy.deepcopy(data.schema)
                schema["header"] = ["fold_num"]
                history_data.schema = schema

        elif self.history_value_type == "instance":
            history_data = data.map(lambda k, v: (KFold.generate_new_id(k, fold_num, data_type), v))
            history_data.schema = copy.deepcopy(data.schema)
        else:
            raise ValueError(f"unknown history value type")
        return history_data

    def run(self, component_parameters, data_inst, original_model, provider_do_evaluate, grid_flowid=0):
        self._init_model(component_parameters)

        if data_inst is None:
            self._arbiter_run(original_model, grid_flowid)
            return
        total_data_count = data_inst.count()
        LOGGER.debug("data_inst count: {}".format(data_inst.count()))
        if self.mode == consts.HORZ or self.role == consts.PROMOTER:
            data_generator = self.split(data_inst)
        else:
            data_generator = [(data_inst, data_inst)] * self.n_splits
        fold_num = 0
        sum_score = 0.0
        for train_data, test_data in data_generator:
            model = copy.deepcopy(original_model)
            LOGGER.debug("In CV, set_flowid flowid is : {}".format(fold_num))
            model.set_flowid(f'grid_{grid_flowid}.{fold_num}')
            model.set_cv_fold(fold_num)

            LOGGER.info("KFold fold_num is: {}".format(fold_num))
            if self.mode == consts.VERT:
                train_data = self._align_data_index(train_data, model.flowid, consts.TRAIN_DATA)
                LOGGER.info("Train data Synchronized")
                test_data = self._align_data_index(test_data, model.flowid, consts.TEST_DATA)
                LOGGER.info("Test data Synchronized")
            LOGGER.debug("train_data count: {}".format(train_data.count()))
            if train_data.count() + test_data.count() != total_data_count:
                raise EnvironmentError("In cv fold: {}, train count: {}, test count: {}, original data count: {}."
                                       "Thus, 'train count + test count = total count' condition is not satisfied"
                                       .format(fold_num, train_data.count(), test_data.count(), total_data_count))
            this_flowid = f'train.grid_{grid_flowid}.' + str(fold_num)
            LOGGER.debug("In CV, set_flowid flowid is : {}".format(this_flowid))
            model.set_flowid(this_flowid)
            model.fit(train_data, test_data)

            this_flowid = f'predict_train.grid_{grid_flowid}.' + str(fold_num)
            LOGGER.debug("In CV, set_flowid flowid is : {}".format(this_flowid))
            model.set_flowid(this_flowid)
            train_pred_res = model.predict(train_data)

            this_flowid = f'predict_validate.grid_{grid_flowid}.' + str(fold_num)
            LOGGER.debug("In CV, set_flowid flowid is : {}".format(this_flowid))
            model.set_flowid(this_flowid)
            test_pred_res = model.predict(test_data)

            # if train_pred_res is not None:
            if self.role == consts.PROMOTER or provider_do_evaluate:
                fold_name = "_".join([f'grid.{grid_flowid}', 'fold', str(fold_num)])
                train_pred_res = train_pred_res.mapValues(lambda value: value + ['train'])
                train_pred_res = model.set_predict_data_schema(train_pred_res, train_data.schema)
                # LOGGER.debug(f"train_pred_res schema: {train_pred_res.schema}")
                test_pred_res = test_pred_res.mapValues(lambda value: value + ['validate'])
                test_pred_res = model.set_predict_data_schema(test_pred_res, test_data.schema)
                train_pred_res = train_pred_res.union(test_pred_res)
                score = evaluate(train_pred_res, fold_name, model)
                sum_score += score

            if self.output_fold_history:
                LOGGER.debug(f"generating fold history for fold {fold_num}")
                fold_train_data = self.transform_history_data(train_data, train_pred_res, fold_num, "train")
                fold_validate_data = self.transform_history_data(test_data, test_pred_res, fold_num, "validate")

                fold_history_data = fold_train_data.union(fold_validate_data)
                fold_history_data.schema = fold_train_data.schema
                if self.fold_history is None:
                    self.fold_history = fold_history_data
                else:
                    new_fold_history = self.fold_history.union(fold_history_data)
                    new_fold_history.schema = fold_history_data.schema
                    self.fold_history = new_fold_history

            fold_num += 1

        self.mean_score = sum_score / fold_num

        LOGGER.debug("Finish all fold running")
        if self.output_fold_history:
            LOGGER.debug(f"output data schema: {self.fold_history.schema}")
            return self.fold_history
        else:
            return data_inst

    def _arbiter_run(self, original_model, grid_flowid=0):
        for fold_num in range(self.n_splits):
            LOGGER.info("KFold flowid is: {}".format(fold_num))
            model = copy.deepcopy(original_model)
            this_flowid = f'train.grid_{grid_flowid}.' + str(fold_num)
            model.set_flowid(this_flowid)
            model.set_cv_fold(fold_num)
            model.fit(None)

            this_flowid = f'predict_train.grid_{grid_flowid}.' + str(fold_num)
            model.set_flowid(this_flowid)
            model.predict(None)

            this_flowid = f'predict_validate.grid_{grid_flowid}.' + str(fold_num)
            model.set_flowid(this_flowid)
            model.predict(None)

    def _align_data_index(self, data_instance, flowid, data_application=None):
        header = data_instance.schema.get('header')

        if data_application is None:
            # LOGGER.warning("not data_application!")
            # return
            raise ValueError("In _align_data_index, data_application should be provided.")

        transfer_variable = CrossValidationTransferVariable()
        if data_application == consts.TRAIN_DATA:
            transfer_id = transfer_variable.train_sid
        elif data_application == consts.TEST_DATA:
            transfer_id = transfer_variable.test_sid
        else:
            raise ValueError("In _align_data_index, data_application should be provided.")

        if self.role == consts.PROMOTER:
            data_sid = data_instance.mapValues(lambda v: 1)
            transfer_id.remote(data_sid,
                               role=consts.PROVIDER,
                               idx=-1,
                               suffix=(flowid,))
            LOGGER.info("remote {} to provider".format(data_application))
            return data_instance
        elif self.role == consts.PROVIDER:
            data_sid = transfer_id.get(idx=0,
                                       suffix=(flowid,))

            LOGGER.info("get {} from promoter".format(data_application))
            join_data_insts = data_sid.join(data_instance, lambda s, d: d)
            join_data_insts.schema['header'] = header
            return join_data_insts

    def get_mean_score(self):
        return self.mean_score
