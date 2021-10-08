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
from kernel.transfer.variables.transfer_class.horz_label_encoder_transfer_variable import \
    HorzLabelEncoderTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class HorzLabelEncoderClient(object):

    def __init__(self):
        self.transvar = HorzLabelEncoderTransferVariable()

    def label_alignment(self, class_set):
        LOGGER.info('start horz label alignments')
        self.transvar.local_labels.remote(class_set, role=consts.ARBITER, suffix=('label_align',))
        new_label_mapping = self.transvar.label_mapping.get(idx=0, suffix=('label_mapping',))
        reverse_mapping = {v: k for k, v in new_label_mapping.items()}
        new_classes_index = [new_label_mapping[k] for k in new_label_mapping]
        new_classes_index = sorted(new_classes_index)
        aligned_labels = [reverse_mapping[i] for i in new_classes_index]
        return aligned_labels, new_label_mapping


class HorzLabelEncoderArbiter(object):

    def __init__(self):
        self.transvar = HorzLabelEncoderTransferVariable()

    def label_alignment(self):
        LOGGER.info('start horz label alignments')
        labels = self.transvar.local_labels.get(idx=-1, suffix=('label_align',))
        label_set = set()
        for local_label in labels:
            label_set.update(local_label)
        global_label = list(label_set)
        global_label = sorted(global_label)
        label_mapping = {v: k for k, v in enumerate(global_label)}
        self.transvar.label_mapping.remote(label_mapping, idx=-1, suffix=('label_mapping',))
        return label_mapping
