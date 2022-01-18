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

from common.python.common.exception.custom_exception import DataSetEmptyError, FeatureEmptyError
from kernel.utils import data_util


def empty_table_detection(data_instances):
    num_data = data_instances.count()
    if num_data == 0:
        table_name = data_instances.get_name()
        namespace = data_instances.get_namespace()
        raise DataSetEmptyError(namespace=namespace, name=table_name)


def empty_feature_detection(data_instances):
    is_empty_feature = data_util.is_empty_feature(data_instances)
    if is_empty_feature:
        table_name = data_instances.get_name()
        namespace = data_instances.get_namespace()
        raise FeatureEmptyError(namespace=namespace, name=table_name)


def empty_value_detection(value):
    if value is None:
        return True
    if isinstance(value, str):
        value = value.strip().lower()
        if value in ['', 'null', 'na', 'n/a']:
            return True
    else:
        if np.isnan(value):
            return True
    return False
