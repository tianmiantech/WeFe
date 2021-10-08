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

_STORAGE_CLIENT = "_storage_client"
RDD_ATTR_NAME = "_rdd"


# noinspection PyUnresolvedReferences
def get_storage_level():
    from pyspark import StorageLevel
    return StorageLevel.MEMORY_AND_DISK


def materialize(rdd):
    rdd.persist(get_storage_level())
    rdd.count()
    # rdd.mapPartitionsWithIndex(lambda ind, it: (1,)).collect()
    return rdd


# noinspection PyUnresolvedReferences
def broadcast_storage_session(work_mode, storage_session):
    import pickle
    pickled_client = pickle.dumps((work_mode.value, storage_session)).hex()
    from pyspark import SparkContext
    SparkContext.getOrCreate().setLocalProperty(_STORAGE_CLIENT, pickled_client)


# noinspection PyProtectedMember,PyUnresolvedReferences
def maybe_create_storage_client():
    """
    a tricky way to set storage client which may be used by spark tasks.
    WARM: This may be removed or adjusted in future!
    """
    import pickle
    from pyspark.taskcontext import TaskContext
    mode, storage_session = pickle.loads(bytes.fromhex(TaskContext.get().getLocalProperty(_STORAGE_CLIENT)))

    from common.python import _STORAGE_VERSION
    if _STORAGE_VERSION < 2:
        from common.python.storage.impl.dsource import DBRuntime
        DBRuntime(storage_session)
