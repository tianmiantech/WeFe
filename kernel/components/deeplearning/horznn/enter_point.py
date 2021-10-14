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

from common.python.session import is_table
from common.python.utils import log_utils
from kernel.components.deeplearning.horznn._consts import _extract_meta, _extract_param
from kernel.components.deeplearning.horznn.horz_nn_param import HorzNNParam
from kernel.model_base import ModelBase
from kernel.transfer.framework.horz.blocks.base import HorzTransferBase
from kernel.transfer.framework.horz.blocks.has_converged import HasConvergedTransVar
from kernel.transfer.framework.horz.blocks.loss_scatter import LossScatterTransVar
from kernel.transfer.framework.horz.blocks.secure_aggregator import SecureAggregatorTransVar
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class HorzNNBase(ModelBase):
    def __init__(self, trans_var):
        super().__init__()
        self.model_param = HorzNNParam()
        self.transfer_variable = trans_var
        self._api_version = 0

    def _init_model(self, param):
        self.param = param
        self.set_version(param.api_version)
        self.tracker.init_task_progress(param.max_iter)

    def is_version_0(self):
        return self._api_version == 0

    def set_version(self, version):
        self._api_version = version


class HorzNNServer(HorzNNBase):
    def __init__(self, trans_var):
        super().__init__(trans_var=trans_var)

    def _init_model(self, param: HorzNNParam):
        super()._init_model(param)
        if self.is_version_0():
            from kernel.components.deeplearning.horznn import _version_0

            _version_0.server_init_model(self, param)

    def callback_loss(self, iter_num, loss):
        metric_meta = {'abscissa_name': 'iters', 'ordinate_name': 'loss', 'metric_type': 'LOSS', 'pair_type': ''}
        self.callback_metric(metric_name='loss',
                             metric_namespace='train',
                             metric_meta=metric_meta,
                             metric_data=(iter_num, loss))

    def fit(self, data_inst,*args):
        if self.is_version_0():
            from kernel.components.deeplearning.horznn import _version_0

            _version_0.server_fit(self=self, data_inst=data_inst)

        else:
            from kernel.components.deeplearning.horznn._torch import build_aggregator

            aggregator = build_aggregator(self.param)
            aggregator.dataset_align()
            aggregator.fit(self.callback_loss)


class HorzNNClient(HorzNNBase):
    def __init__(self, trans_var):
        super().__init__(trans_var=trans_var)
        self._trainer = ...

    def _init_model(self, param: HorzNNParam):
        super()._init_model(param)
        if self.is_version_0():
            from kernel.components.deeplearning.horznn import _version_0

            _version_0.client_init_model(self, param)

    def fit(self, data, *args):
        if self.is_version_0():
            from kernel.components.deeplearning.horznn import _version_0

            _version_0.client_fit(self=self, data_inst=data)
        else:
            from kernel.components.deeplearning.horznn._torch import build_trainer

            self._trainer, dataloader = build_trainer(
                param=self.param,
                data=data,
            )
            self._trainer.fit(dataloader)
            self.set_summary(self._trainer.summary())
            # save model to local filesystem
            self._trainer.save_checkpoint()

    def predict(self, data):

        if self.is_version_0():
            from kernel.components.deeplearning.horznn import _version_0

            results = _version_0.client_predict(self=self, data_inst=data)
            return results

        else:
            from kernel.components.deeplearning.horznn._torch import make_predict_dataset

            dataset = make_predict_dataset(data=data, trainer=self._trainer)
            predict_tbl, classes = self._trainer.predict(
                dataset=dataset,
                batch_size=self.param.batch_size,
            )
            data_instances = data if is_table(data) else dataset.as_data_instance()
            results = self.predict_score_to_output(
                data_instances,
                predict_tbl,
                classes=classes,
                threshold=self.param.predict_param.threshold,
            )
            return results

    def export_model(self):
        if self.is_version_0():
            from kernel.components.deeplearning.horznn import _version_0

            return _version_0.client_export_model(self=self)

        else:
            return self._trainer.export_model(self.param)

    def load_model(self, model_dict):
        model_dict = list(model_dict["model"].values())[0]
        model_obj = _extract_param(model_dict)
        meta_obj = _extract_meta(model_dict)

        # compatibility
        if not hasattr(model_obj, "api_version"):
            self.set_version(0)
        else:
            self.set_version(model_obj.api_version)

        if self.is_version_0():
            from kernel.components.deeplearning.horznn import _version_0

            _version_0.client_load_model(
                self=self, meta_obj=meta_obj, model_obj=model_obj
            )
        else:
            from kernel.components.deeplearning.horznn._torch import PyTorchFederatedTrainer

            self._trainer = PyTorchFederatedTrainer.load_model(
                model_obj=model_obj, meta_obj=meta_obj, param=self.param
            )
            self._init_model(self.model_param)


# server: Arbiter, clients: Promoter and providers
class HorzNNDefaultTransVar(HorzTransferBase):
    def __init__(
            self, server=(consts.ARBITER,), clients=(consts.PROMOTER, consts.PROVIDER), prefix=None
    ):
        super().__init__(server=server, clients=clients, prefix=prefix)
        self.secure_aggregator_trans_var = SecureAggregatorTransVar(
            server=server, clients=clients, prefix=self.prefix
        )
        self.loss_scatter_trans_var = LossScatterTransVar(
            server=server, clients=clients, prefix=self.prefix
        )
        self.has_converged_trans_var = HasConvergedTransVar(
            server=server, clients=clients, prefix=self.prefix
        )


class HorzNNDefaultClient(HorzNNClient):
    def __init__(self):
        super().__init__(trans_var=HorzNNDefaultTransVar())


class HorzNNDefaultServer(HorzNNServer):
    def __init__(self):
        super().__init__(trans_var=HorzNNDefaultTransVar())
