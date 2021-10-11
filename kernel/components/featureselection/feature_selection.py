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
from common.python.db.db_models import GlobalSetting
from common.python.utils import log_utils
from kernel.base.instance import Instance
from kernel.model_base import ModelBase
from kernel.utils import data_util

LOGGER = log_utils.get_logger()


class FeatureSelection(ModelBase):

    def __init__(self):
        super(FeatureSelection, self).__init__()
        from kernel.components.featureselection.param import Params
        self.model_param = Params()
        self.new_instance = None
        self.save_dataset = True
        self.set_show_name("(特征筛选)")
        self.source_type = DataSetSourceType.FEATURESELECTION

    def _init_feature_list(self):
        self.members = self.model_param.members
        # self.features = self.model_param.features

    def _get_my_features(self):
        for item in self.members:
            if item["member_id"] == GlobalSetting.get_member_id():
                self.features = item["features"]

    def filter_data_instance(self, data_instance):
        if self.features is None:
            return data_instance
        if not isinstance(data_instance.first()[1], Instance):
            raise ValueError('data_instance value type is not str or Instance')
        # data_util.set_schema(data_instance, data_instance.get_metas())
        header = data_util.get_header(data_instance)
        if header is None:
            raise ValueError('data_instance header is empty')
        features_index = data_util.calc_need_feature_index(self.features, header)
        new_data_instance = data_instance.mapValues(lambda v: data_util.process_data_instance_value(v, features_index))
        schema = data_instance.schema
        schema['header'] = self.features
        data_util.set_schema(new_data_instance, schema)
        LOGGER.info("{}: {}".format("筛选后的数据集", new_data_instance.schema))
        self.new_instance = new_data_instance

    def fit(self, data_inst):
        self._init_feature_list()
        self._get_my_features()
        LOGGER.info("特征选择结果：" + str(self.features))
        if data_inst.first() is None:
            LOGGER.info("Empty")
            raise ValueError("选择的数据集为空！")
        else:
            self.filter_data_instance(data_inst)
        return self.new_instance

    def output_data(self):
        return self.new_instance
