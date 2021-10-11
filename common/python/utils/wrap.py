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

class FederationWrapped(object):
    """
    A wrapper, wraps _DSource as Table
    """

    # noinspection PyProtectedMember
    def __init__(self, session_id, dsource_cls, table_cls):
        self.dsource_cls = dsource_cls
        self.table_cls = table_cls
        self.session_id = session_id

    def unboxed(self, obj):
        if hasattr(obj, "_fcs"):
            return obj
        elif isinstance(obj, self.table_cls):
            return obj.dsource()
        else:
            return obj

    def boxed(self, obj):
        if isinstance(obj, self.dsource_cls):
            return self.table_cls.from_dsource(dsource=obj, session_id=self.session_id)
        else:
            return obj
