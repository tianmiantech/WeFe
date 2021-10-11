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

from common.python.common.consts import DataSetSourceType
from common.python.common.exception.custom_exception import CommonCustomError
from common.python.utils import log_utils
from kernel.components.intersection.dh.dh_intersect_promoter import DhIntersectionPromoter
from kernel.components.intersection.dh.dh_intersect_provider import DhIntersectionProvider
from kernel.components.intersection.dhkey.dh_key_intersect_promoter import DhKeyIntersectionPromoter
from kernel.components.intersection.dhkey.dh_key_intersect_provider import DhKeyIntersectionProvider
from kernel.components.intersection.param import IntersectParam
from kernel.components.intersection.repeat_id_process import RepeatedIDIntersect
from kernel.model_base import ModelBase
from kernel.transfer.variables.transfer_class.dh_intersect_transfer_variable import DhIntersectTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class IntersectModelBase(ModelBase):
    def __init__(self):
        super().__init__()
        self.intersection_obj = None
        self.intersect_num = -1
        self.intersect_rate = -1
        self.intersect_ids = None
        self.metric_name = "intersection"
        self.metric_namespace = "train"
        self.metric_type = "INTERSECTION"
        self.model_param = IntersectParam()
        self.role = None

        self.promoter_member_id = None
        self.provider_member_id = None
        self.provider_member_id_list = None

        self.set_show_name("(Data Intersect)")
        self.source_type = DataSetSourceType.INTERSECT
        self.transfer_variable = DhIntersectTransferVariable()

    def __init_intersect_method(self):
        LOGGER.info("Using {} intersection, role is {}".format(self.model_param.intersect_method, self.role))
        self.provider_member_id_list = self.component_properties.provider_member_idlist
        self.promoter_member_id = self.component_properties.promoter_member_id
        if self.role == consts.PROVIDER:
            self.provider_member_id = self.component_properties.local_member_id

        if self.model_param.intersect_method == "dh_key":
            if self.role == consts.PROVIDER:
                self.intersection_obj = DhKeyIntersectionProvider(self.model_param)
                self.intersection_obj.provider_member_id = self.provider_member_id
            elif self.role == consts.PROMOTER:
                self.intersection_obj = DhKeyIntersectionPromoter(self.model_param)
                self.intersection_obj.promoter_member_id = self.promoter_member_id
            else:
                raise ValueError("role {} is not support".format(self.role))
        elif self.model_param.intersect_method == 'dh':
            if self.role == consts.PROVIDER:
                self.intersection_obj = DhIntersectionProvider(self.model_param)
                self.intersection_obj.provider_member_id = self.provider_member_id
            elif self.role == consts.PROMOTER:
                self.intersection_obj = DhIntersectionPromoter(self.model_param)
                self.intersection_obj.promoter_member_id = self.promoter_member_id
            else:
                raise ValueError("role {} is not support".format(self.role))
        else:
            raise ValueError("intersect_method {} is not support yet".format(self.model_param.intersect_method))

        self.intersection_obj.promoter_member_id = self.promoter_member_id
        self.intersection_obj.provider_member_id_list = self.provider_member_id_list

    def fit(self, data):
        self.__init_intersect_method()

        if self.model_param.repeated_id_process:
            if self.model_param.intersect_cache_param.use_cache is True and self.model_param.intersect_method == consts.RSA:
                raise ValueError("Not support cache module while repeated id process.")

            if len(self.provider_member_id_list) > 1 and self.model_param.repeated_id_owner != consts.PROMOTER:
                raise ValueError("While multi-provider, repeated_id_owner should be promoter.")

            proc_obj = RepeatedIDIntersect(repeated_id_owner=self.model_param.repeated_id_owner, role=self.role)
            data = proc_obj.run(data=data)

        self.intersect_ids = self.intersection_obj.run(data)
        LOGGER.info("Finish intersection")

        if self.intersect_ids:
            self.intersect_num = self.intersect_ids.count()
            self.intersect_rate = self.intersect_num * 1.0 / data.count()

        metric_data = [("count", data.count()), ("intersect_count", self.intersect_num),
                       ("intersect_rate", self.intersect_rate)]
        print(f'metric_data: {metric_data}')
        self.tracker.saveMetricData(self.metric_name, self.metric_namespace, None, metric_data)

        if metric_data[1][1] == 0:
            raise CommonCustomError(message="Data Intersect result is zero")

    def output_data(self):
        return self.intersect_ids


class IntersectProvider(IntersectModelBase):
    def __init__(self):
        super().__init__()
        self.role = consts.PROVIDER


class IntersectPromoter(IntersectModelBase):
    def __init__(self):
        super().__init__()
        self.role = consts.PROMOTER
