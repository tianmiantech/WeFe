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



from common.python.utils import log_utils
from kernel.model_selection import MiniBatch
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class Promoter(object):
    def __init__(self):
        self.mini_batch_obj = None
        self.finish_sycn = False
        self.batch_nums = None

    def register_batch_generator(self, transfer_variables):
        self.batch_data_info_transfer = transfer_variables.batch_info.disable_auto_clean()
        self.batch_data_index_transfer = transfer_variables.batch_data_index.disable_auto_clean()

    def initialize_batch_generator(self, data_instances, batch_size, suffix=tuple()):
        self.mini_batch_obj = MiniBatch(data_instances, batch_size=batch_size)
        self.batch_nums = self.mini_batch_obj.batch_nums
        batch_info = {"batch_size": batch_size, "batch_num": self.batch_nums}
        self.remote_batch_info(batch_info, suffix)
        index_generator = self.mini_batch_obj.mini_batch_data_generator(result='index')
        batch_index = 0
        for batch_data_index in index_generator:
            batch_suffix = suffix + (batch_index,)
            self.remote_batch_index(batch_data_index, batch_suffix)
            batch_index += 1

    def generate_batch_data(self):
        data_generator = self.mini_batch_obj.mini_batch_data_generator(result='data')
        for batch_data in data_generator:
            yield batch_data

    def remote_batch_info(self, batch_info, suffix=tuple()):
        self.batch_data_info_transfer.remote(obj=batch_info,
                                             role=consts.PROVIDER,
                                             suffix=suffix)

    def remote_batch_index(self, batch_index, suffix=tuple()):
        self.batch_data_index_transfer.remote(obj=batch_index,
                                              role=consts.PROVIDER,
                                              suffix=suffix)


class Provider(object):
    def __init__(self):
        self.finish_sycn = False
        self.batch_data_insts = []
        self.batch_nums = None

    def register_batch_generator(self, transfer_variables):
        self.batch_data_info_transfer = transfer_variables.batch_info.disable_auto_clean()
        self.batch_data_index_transfer = transfer_variables.batch_data_index.disable_auto_clean()

    def initialize_batch_generator(self, data_instances, suffix=tuple(), member_id_list=None):
        batch_info = self.get_batch_info(suffix, member_id_list)
        self.batch_nums = batch_info.get('batch_num')
        for batch_index in range(self.batch_nums):
            batch_suffix = suffix + (batch_index,)
            batch_data_index = self.get_batch_index(suffix=batch_suffix, member_id_list=member_id_list)
            batch_data_inst = batch_data_index.join(data_instances, lambda g, d: d)
            self.batch_data_insts.append(batch_data_inst)

    def generate_batch_data(self):
        batch_index = 0
        for batch_data_inst in self.batch_data_insts:
            LOGGER.info("batch_num: {}, batch_data_inst size:{}".format(
                batch_index, batch_data_inst.count()))
            yield batch_data_inst
            batch_index += 1

    def get_batch_info(self, suffix=tuple(), member_id_list=None):
        LOGGER.debug("In sync_batch_info, suffix is :{}".format(suffix))
        batch_info = self.batch_data_info_transfer.get(idx=0,
                                                       suffix=suffix, member_id_list=member_id_list)
        batch_size = batch_info.get('batch_size')
        if batch_size < consts.MIN_BATCH_SIZE and batch_size != -1:
            raise ValueError(
                "Batch size get from promoter should not less than {}, except -1, batch_size is {}".format(
                    consts.MIN_BATCH_SIZE, batch_size))
        return batch_info

    def get_batch_index(self, suffix=tuple(), member_id_list=None):
        batch_index = self.batch_data_index_transfer.get(idx=0,
                                                         suffix=suffix, member_id_list=member_id_list)
        return batch_index
