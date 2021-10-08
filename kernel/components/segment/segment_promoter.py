# Copyright 2021 The WeFe Authors. All Rights Reserved.
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
from kernel.components.segment.segment_model import SegmentModelBase
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class SegmentPromoter(SegmentModelBase):

    def __init__(self):
        super().__init__()
        self.count = None
        self.role = consts.PROMOTER
        self.segment_data_output = None

    def fit(self, data_instances):
        LOGGER.info("start SegmentPromoter")

        segment_result = self.base_fit(data_instances)
        train_data = segment_result[0]
        test_data = segment_result[1]
        LOGGER.info("promoter:segment_result, train_data:{}, test_data:{}".format(train_data, test_data))

        # save train and test data
        self.save_metric_data(train_data, test_data)
        self.segment_data_output = [train_data, test_data]
        return [train_data, test_data]

    def output_data(self):
        return self.segment_data_output
