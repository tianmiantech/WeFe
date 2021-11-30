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

import numpy as np
from google.protobuf import json_format

from common.python.utils import log_utils
from kernel.base.sparse_vector import SparseVector
from kernel.components.lr.lr_model_weight import LRModelWeights as LogisticRegressionWeights
from kernel.components.lr.one_vs_rest import one_vs_rest_factory
from kernel.components.lr.param import InitParam
from kernel.model_base import ModelBase
from kernel.model_selection import start_cross_validation
from kernel.optimizer.convergence import converge_func_factory
from kernel.optimizer.initialize import Initializer
from kernel.optimizer.optimizer import optimizer_factory
from kernel.protobuf.generated import lr_model_param_pb2
from kernel.utils import consts
from kernel.utils import data_util
from kernel.utils.base_operator import vec_dot
from kernel.utils.validation_strategy import ValidationStrategy

LOGGER = log_utils.get_logger()


class BaseLRModel(ModelBase):
    def __init__(self):
        super(BaseLRModel, self).__init__()
        # attribute:
        self.n_iter_ = 0
        self.classes_ = None
        self.feature_shape = None
        self.gradient_operator = None
        self.initializer = Initializer()
        self.transfer_variable = None
        self.loss_history = []
        self.is_converged = False
        self.header = None
        self.role = ''
        self.mode = ''
        self.schema = {}
        self.cipher_operator = None
        self.model_weights = None
        self.validation_freqs = None
        self.in_one_vs_rest = False
        self.init_param_obj = None

        self.model_name = 'LogisticRegression'
        self.model_param_name = 'LogisticRegressionParam'
        self.model_meta_name = 'LogisticRegressionMeta'

        # one_ve_rest parameter
        self.need_one_vs_rest = None
        self.one_vs_rest_classes = []
        self.one_vs_rest_obj = None

        self.validation_strategy = None
        self.is_serving_model = True

    def _init_model(self, params):
        self.model_param = params
        self.alpha = params.alpha
        self.init_param_obj = params.init_param
        # self.fit_intercept = self.init_param_obj.fit_intercept
        self.batch_size = params.batch_size
        self.max_iter = params.max_iter
        self.optimizer = optimizer_factory(params)
        self.converge_func = converge_func_factory(params.early_stop, params.tol)
        self.encrypted_calculator = None
        self.validation_freqs = params.validation_freqs
        self.validation_strategy = None
        self.early_stopping_rounds = params.early_stopping_rounds
        self.metrics = params.metrics
        self.use_first_metric_only = params.use_first_metric_only

        self.tracker.init_task_progress(params.max_iter)

        self.one_vs_rest_obj = one_vs_rest_factory(self, role=self.role, mode=self.mode, has_arbiter=True)

    def compute_wx(self, data_instances, coef_, intercept_=0):
        return data_instances.mapValues(lambda v: vec_dot(v.features, coef_) + intercept_, need_send=True)

    def get_single_model_param(self):
        weight_dict = {}
        LOGGER.debug("in get_single_model_param, model_weights: {}, coef: {}, header: {}".format(
            self.model_weights.unboxed, self.model_weights.coef_, self.header
        ))
        for idx, header_name in enumerate(self.header):
            coef_i = self.model_weights.coef_[idx]
            weight_dict[header_name] = coef_i

        result = {'iters': self.n_iter_,
                  'loss_history': self.loss_history,
                  'is_converged': self.is_converged,
                  'weight': weight_dict,
                  'intercept': self.model_weights.intercept_,
                  'header': self.header
                  }
        return result

    def parse_single_model_param(self, model_weights: LogisticRegressionWeights, header):
        weight_dict = {}
        LOGGER.debug("in get_single_model_param, model_weights: {}, coef: {}, header: {}".format(
            model_weights.unboxed, model_weights.coef_, header
        ))
        for idx, header_name in enumerate(header):
            coef_i = model_weights.coef_[idx]
            weight_dict[header_name] = coef_i

        result = {
            'weight': weight_dict,
            'intercept': model_weights.intercept_,
            'header': header
        }
        return result

    def parse_param(self, model_weights, header):
        LOGGER.debug("In get_param, header: {}".format(header))
        if header is None:
            param_protobuf_obj = lr_model_param_pb2.LRModelParam()
            return param_protobuf_obj
        if self.need_one_vs_rest:
            # one_vs_rest_class = list(map(str, self.one_vs_rest_obj.classes))
            one_vs_rest_result = self.one_vs_rest_obj.save(lr_model_param_pb2.SingleModel)
            single_result = {'header': header, 'need_one_vs_rest': True}
        else:
            one_vs_rest_result = None
            single_result = self.parse_single_model_param(model_weights, header)
            single_result['need_one_vs_rest'] = False
        single_result['one_vs_rest_result'] = one_vs_rest_result
        LOGGER.debug("in _get_param, single_result: {}".format(single_result))

        param_protobuf_obj = lr_model_param_pb2.LRModelParam(**single_result)
        json_result = json_format.MessageToJson(param_protobuf_obj)
        LOGGER.debug("json_result: {}".format(json_result))
        return param_protobuf_obj

    def _get_param(self):
        header = self.header
        LOGGER.debug("In get_param, header: {}".format(header))
        if header is None:
            param_protobuf_obj = lr_model_param_pb2.LRModelParam()
            return param_protobuf_obj
        if self.need_one_vs_rest:
            # one_vs_rest_class = list(map(str, self.one_vs_rest_obj.classes))
            one_vs_rest_result = self.one_vs_rest_obj.save(lr_model_param_pb2.SingleModel)
            single_result = {'header': header, 'need_one_vs_rest': True}
        else:
            one_vs_rest_result = None
            single_result = self.get_single_model_param()
            single_result['need_one_vs_rest'] = False
        single_result['one_vs_rest_result'] = one_vs_rest_result
        LOGGER.debug("in _get_param, single_result: {}".format(single_result))

        param_protobuf_obj = lr_model_param_pb2.LRModelParam(**single_result)
        json_result = json_format.MessageToJson(param_protobuf_obj)
        LOGGER.debug("json_result: {}".format(json_result))
        return param_protobuf_obj

    # if __name__ == '__main__':
    #     rs = {'iters': 30, 'loss_history': [], 'is_converged': True, 'weight': {'x0': -0.37593292862612715, 'x1': 0.051038257304800556, 'x2': 0.33922291754252504, 'x3': 0.2849122956560564, 'x4': 0.04835349028386403, 'x5': -0.1327609025895212, 'x6': 0.2486081899501936, 'x7': -0.4083127306751853, 'x8': -0.09128201671412293, 'x9': 0.2877323954765944, 'x10': -0.09912534070670494, 'x11': 0.008587187307896293, 'x12': 0.19415649701119336, 'x13': -0.2240128725396232, 'x14': -0.007320813873523055, 'x15': 0.10377277303709516, 'x16': 0.09214901146505608, 'x17': 0.1559231270728811, 'x18': -0.12093531048335666, 'x19': -0.20804639436497643}, 'intercept': 0.0, 'header': ['x0', 'x1', 'x2', 'x3', 'x4', 'x5', 'x6', 'x7', 'x8', 'x9', 'x10', 'x11', 'x12', 'x13', 'x14', 'x15', 'x16', 'x17', 'x18', 'x19'], 'need_one_vs_rest': False, 'one_vs_rest_result': None}
    #     param_protobuf_obj = lr_model_param_pb2.LRModelParam(**rs)
    #     json_result = json_format.MessageToJson(param_protobuf_obj)
    #     print(json_result)

    def load_model(self, model_dict):
        LOGGER.debug("Start Loading model")

        for _, value in model_dict["model"].items():
            for model in value:
                if type(model) == str:
                    if model.endswith("Meta"):
                        meta_obj = value[model]
                    if model.endswith("Param"):
                        result_obj = value[model]
                else:
                    for obj in model.items():
                        key = obj[0]
                        if key.endswith("Meta"):
                            meta_obj = obj[1]
                        if key.endswith("Param"):
                            result_obj = obj[1]

        LOGGER.info("load model")

        # result_obj = list(model_dict.get('model').values())[0].get(self.model_param_name)
        # meta_obj = list(model_dict.get('model').values())[0].get(self.model_meta_name)
        # self.fit_intercept = meta_obj.fit_intercept
        if self.init_param_obj is None:
            self.init_param_obj = InitParam()
        self.init_param_obj.fit_intercept = meta_obj["fitIntercept"] if type(
            meta_obj) is dict else meta_obj.fit_intercept
        self.header = list(result_obj["header"]) if type(result_obj) is dict else list(result_obj.header)
        # For vert-lr arbiter predict function
        if self.header is None:
            return

        need_one_vs_rest = result_obj["needOneVsRest"] if type(result_obj) is dict else result_obj.need_one_vs_rest
        LOGGER.debug("in _load_model need_one_vs_rest: {}".format(need_one_vs_rest))
        if need_one_vs_rest:
            one_vs_rest_result = result_obj["oneVsRestResult"] if type(
                result_obj) is dict else result_obj.one_vs_rest_result
            self.one_vs_rest_obj = one_vs_rest_factory(classifier=self, role=self.role,
                                                       mode=self.mode, has_arbiter=True)
            self.one_vs_rest_obj.load_model(one_vs_rest_result)
            self.need_one_vs_rest = True
        else:
            self.load_single_model(result_obj)
            self.need_one_vs_rest = False

    def load_single_model(self, single_model_obj):
        LOGGER.info("It's a binary task, start to load single model")
        feature_shape = len(self.header)
        tmp_vars = np.zeros(feature_shape)
        if type(single_model_obj) is dict:
            weight_dict = single_model_obj.get('weight')
        else:
            weight_dict = dict(single_model_obj.weight)

        for idx, header_name in enumerate(self.header):
            tmp_vars[idx] = weight_dict.get(header_name)

        if self.fit_intercept:
            intercept = single_model_obj.get("intercept") if type(single_model_obj) is dict \
                else single_model_obj.intercept
            tmp_vars = np.append(tmp_vars, intercept)
        self.model_weights = LogisticRegressionWeights(tmp_vars, fit_intercept=self.fit_intercept)
        return self

    def one_vs_rest_fit(self, train_data=None, validate_data=None):
        LOGGER.debug("Class num larger than 2, need to do one_vs_rest")
        self.one_vs_rest_obj.fit(data_instances=train_data, validate_data=validate_data)

    def get_features_shape(self, data_instances):
        if self.feature_shape is not None:
            return self.feature_shape
        return data_util.get_features_shape(data_instances)

    def set_header(self, header):
        self.header = header

    def get_header(self, data_instances):
        if self.header is not None:
            return self.header
        return data_instances.schema.get("header")

    def get_weight_intercept_dict(self, header):
        weight_dict = {}
        for idx, header_name in enumerate(header):
            coef_i = self.model_weights.coef_[idx]
            weight_dict[header_name] = coef_i
        intercept_ = self.model_weights.intercept_
        return weight_dict, intercept_

    @property
    def fit_intercept(self):
        return self.init_param_obj.fit_intercept

    def _get_meta(self):
        raise NotImplementedError("This method should be be called here")

    def export_model(self):
        meta_obj = self._get_meta()
        param_obj = self._get_param()
        result = {
            self.model_meta_name: meta_obj,
            self.model_param_name: param_obj
        }
        return result

    def callback_loss(self, iter_num, loss):
        metric_meta = {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS', 'pair_type': ''}
        self.callback_metric(metric_name='loss',
                             metric_namespace='train',
                             metric_meta=metric_meta,
                             metric_data=(iter_num, loss))

    def _abnormal_detection(self, data_instances):
        """
        Make sure input data_instances is valid.
        """
        data_util.empty_table_detection(data_instances)

    def init_validation_strategy(self, train_data=None, validate_data=None):
        # validation_strategy = ValidationStrategy(self.role, self.mode, self.validation_freqs)
        validation_strategy = ValidationStrategy(self.role, self.mode, self.validation_freqs,
                                                 self.early_stopping_rounds,
                                                 self.use_first_metric_only)
        if self.role != consts.ARBITER:
            validation_strategy.set_train_data(train_data)
            validation_strategy.set_validate_data(validate_data)

        return validation_strategy

    def cross_validation(self, data_instances):
        return start_cross_validation.run(self, data_instances)

    def _get_cv_param(self):
        self.model_param.cv_param.role = self.role
        self.model_param.cv_param.mode = self.mode
        return self.model_param.cv_param

    def set_schema(self, data_instance, header=None):
        if header is None:
            self.schema["header"] = self.header
        else:
            self.schema["header"] = header
        data_instance.schema = self.schema
        return data_instance

    def init_schema(self, data_instance):
        if data_instance is None:
            return
        self.schema = data_instance.schema
        self.header = self.schema.get('header')

    def check_abnormal_values(self, data_instances):

        if data_instances is None:
            return

        def _check_overflow(data_iter):
            for _, instant in data_iter:
                features = instant.features
                if isinstance(features, SparseVector):
                    sparse_data = features.get_all_data()
                    for k, v in sparse_data:
                        if np.abs(v) > consts.OVERFLOW_THRESHOLD:
                            return True
                else:
                    if np.max(np.abs(features)) > consts.OVERFLOW_THRESHOLD:
                        return True
            return False

        check_status = data_instances.applyPartitions(_check_overflow)
        is_overflow = check_status.reduce(lambda a, b: a or b)
        if is_overflow:
            raise OverflowError("The value range of features is too large for GLM, please have "
                                "a check for input data")
        LOGGER.info("Check for abnormal value passed")
