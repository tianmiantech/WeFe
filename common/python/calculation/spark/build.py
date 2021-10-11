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

from common.python.calculation.spark.table import RDDSource
from common.python.calculation.spark.util import broadcast_storage_session
from common.python.p_session.base_impl import build as build1x


class Builder(build1x.Builder):
    _table_cls = RDDSource

    def __init__(self, session_id, work_mode, persistent_engine, db_type=None):
        super().__init__(session_id=session_id, work_mode=work_mode, persistent_engine=persistent_engine,
                         db_type=db_type)

    def build_session(self):
        from common.python.calculation.spark.session import WefeSessionImpl
        from common.python.p_session.base_impl.session import build_storage_session, build_db_runtime

        storage_session = build_storage_session(work_mode=self._work_mode, job_id=self._session_id,
                                                db_type=self._db_type)
        self._session_id = storage_session.get_session_id()
        broadcast_storage_session(work_mode=self._work_mode, storage_session=storage_session)
        db_runtime = build_db_runtime(work_mode=self._work_mode, storage_session=storage_session)
        return WefeSessionImpl(self._session_id, db_runtime, self._persistent_engine)
