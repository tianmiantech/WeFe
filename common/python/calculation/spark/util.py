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


from typing import Iterable
from pyspark.rdd import RDD
from pyspark import SparkContext
from pyspark.serializers import BatchedSerializer


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
        from common.python.p_session.base_impl.db_runtime import DBRuntime
        DBRuntime(storage_session)


class WefeSparkContext(SparkContext):

    def __init__(self):
        self.context = super().getOrCreate()

    def parallelize(self, c: Iterable, numSlices: int, data_count: int):
        c_size = data_count

        batchSize = max(1, min(c_size // numSlices,
                        self.context._batchSize or 1024))
        serializer = BatchedSerializer(
            self.context._unbatched_serializer, batchSize)

        def reader_func(temp_filename):
            return self.context._jvm.PythonRDD.readRDDFromFile(self.context._jsc, temp_filename, numSlices)

        def createRDDServer():
            return self.context._jvm.PythonParallelizeServer(self.context._jsc.sc(), numSlices)

        jrdd = self.context._serialize_to_jvm(
            c, serializer, reader_func, createRDDServer)
        return RDD(jrdd, self.context, serializer)
