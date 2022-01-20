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
from kernel.components.lr.param import VertLogisticParam
from kernel.components.lr.vertlr.vert_lr_promoter import VertLRPromoter
from kernel.components.lr.vertlr.vert_lr_provider import VertLRProvider
from kernel.components.lr.vertsshelr.vert_lr_promoter import VertLRPromoter as VertSSHELRPromoter
from kernel.components.lr.vertsshelr.vert_lr_provider import VertLRProvider as VertSSHELRProvider
from kernel.model_base import ModelBase
from kernel.utils import consts, LOGGER


class VertLRStart(ModelBase):
    def __init__(self):
        super(VertLRStart, self).__init__()
        self.lr_obj = None
        self.model_param = VertLogisticParam()

    def run(self, component_parameters=None, args=None):
        self._init_runtime_parameters(component_parameters)
        lr_method = self.model_param.method
        LOGGER.debug(f'lr_method={lr_method}')
        if self.role == consts.PROMOTER:
            if lr_method == consts.SSHE_LR:
                self.lr_obj = VertSSHELRPromoter()
            else:
                self.lr_obj = VertLRPromoter()
        else:
            if lr_method == consts.SSHE_LR:
                self.lr_obj = VertSSHELRProvider()
            else:
                self.lr_obj = VertLRProvider()

        self.lr_obj.set_tracker(self.tracker)
        self.lr_obj.run(component_parameters, args)

        self.is_serving_model = self.lr_obj.is_serving_model
        self.show_name = self.lr_obj.show_name
        self.source_type = self.lr_obj.source_type

    def export_model(self):
        return self.lr_obj.export_model()

    def output_data(self):
        return self.lr_obj.output_data()

    def status_sync(self, task_config):
        return self.lr_obj.status_sync(task_config)
