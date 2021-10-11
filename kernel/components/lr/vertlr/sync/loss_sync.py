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



from kernel.utils import consts


class Promoter(object):
    def _register_loss_sync(self, provider_loss_regular_transfer):
        self.provider_loss_regular_transfer = provider_loss_regular_transfer

    def get_provider_loss_regular(self, suffix=tuple()):
        losses = self.provider_loss_regular_transfer.get(idx=-1, suffix=suffix)
        return losses


class Provider(object):
    def _register_loss_sync(self, provider_loss_regular_transfer):
        self.provider_loss_regular_transfer = provider_loss_regular_transfer

    def remote_loss_regular(self, loss_regular, suffix=tuple(), idx=-1, member_id_list=None):
        self.provider_loss_regular_transfer.remote(obj=loss_regular, role=consts.PROMOTER, idx=idx, suffix=suffix,
                                                   member_id_list=member_id_list)
