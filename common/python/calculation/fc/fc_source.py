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

import time
import uuid
from typing import Iterable

from common.python import RuntimeInstance
from common.python.calculation.fc.fc_storage import FCStorage
from common.python.calculation.spark import util
from common.python.common import consts
from common.python.common.consts import NAMESPACE
from common.python.table import Table
from common.python.utils import log_utils, conf_utils, cloudpickle
from common.python.utils.profile_util import log_elapsed
from common.python.utils.split import split_put, split_get

LOGGER = log_utils.get_logger("PROFILING")


class FCSource(Table):
    """

    fc：function computer
    fcs: function computer storage
    """

    FCS_ATTR = "_fcs"
    COLLECT_REDUCE_FC_NAME = "collectReduce"

    # noinspection PyProtectedMember
    @classmethod
    def from_dsource(cls, session_id: str, dsource):
        """
        Load data from storage to generate FCSource

        Parameters
        ----------
        session_id: session_id
        dsource: default storage, such as CK,LMDB

        Returns
        -------

        """
        namespace = dsource._namespace
        name = dsource._name
        partitions = dsource._partitions
        return FCSource(session_id=session_id, namespace=namespace, name=name, partitions=partitions, dsource=dsource)

    @classmethod
    def from_fcs(cls, fcs, job_id: str, namespace: str, name: str):
        """
        Load data from fc storage to generate FCSource

        Parameters
        ----------
        fcs: Intermediate storage combined with function calculations, such as oss, ots
        job_id: job id
        namespace: NAMESPACE
        name: name

        Returns
        -------

        """
        partitions = fcs.get_partitions()
        return FCSource(session_id=job_id, namespace=namespace, name=name, partitions=partitions, fcs=fcs)

    def __init__(self, session_id: str,
                 namespace: str,
                 name: str = None,
                 partitions: int = 1,
                 fcs=None,
                 dsource=None):
        """
        Constructor

        Parameters
        ----------
        session_id: session_id
        namespace: namespace
        name: name
        partitions:The default sharding number, in CK storage, has weakened this parameter
        fcs:Intermediate storage combined with function calculations, such as oss, ots
        dsource
        """

        self._valid_param_check(fcs, dsource, namespace, partitions)
        setattr(self, util.RDD_ATTR_NAME, fcs)
        self._fcs = fcs
        self._partitions = partitions
        self._dsource = dsource
        self.schema = {}
        self._name = name or str(uuid.uuid1())
        self._namespace = namespace
        self._session_id = session_id

    def get_name(self):
        return self._name

    def get_namespace(self):
        return self._namespace

    def __str__(self):
        return f"{self._namespace}, {self._name}, {self._dsource}"

    def __repr__(self):
        return f"{self._namespace}, {self._name}, {self._dsource}"

    def generate_name(self, name=None):
        return name or f"{self._session_id}_{str(uuid.uuid1())}"

    def _tmp_source_from_fcs(self, fcs, name=None):
        """
        tmp source, with namespace == job_id
        """
        name = self.generate_name(name)
        return FCSource(session_id=self._session_id,
                        namespace=NAMESPACE.PROCESS,
                        name=name,
                        partitions=fcs.get_partitions(),
                        fcs=fcs,
                        dsource=None)

    def __getstate__(self):
        state = dict(self.__dict__)
        if self.FCS_ATTR in state:
            del state[self.FCS_ATTR]
        return state

    @staticmethod
    def _valid_param_check(fcs, dsource, namespace, partitions):
        assert (fcs is not None) or (dsource is not None), "params fcs and storage are both None"
        assert namespace is not None, "namespace is None"
        assert partitions > 0, "invalid partitions={0}".format(partitions)

    def fcs(self):
        if hasattr(self, self.FCS_ATTR) and self._fcs is not None:
            return self._fcs

        if self._dsource is None:
            raise AssertionError("try create fcs from None storage")

        return self._fcs_from_dsource()

    def get_exist_fcs(self):
        """
        get exist fc storage
        Returns
        -------

        """
        if hasattr(self, self.FCS_ATTR) and self._fcs is not None:
            return self._fcs

    # noinspection PyProtectedMember,PyUnresolvedReferences
    # @log_elapsed
    def _fcs_from_dsource(self):
        """
        read data from dsource and written in fc storage
        :return:
        """
        start = time.time()
        storage_iterator = self._dsource.collect(use_serialize=True)
        data_count = self._dsource.count()
        if data_count <= 0:
            storage_iterator = []

        num_partition = self._dsource._partitions
        num_partition = self.get_fcs_partitions(num_partition, data_count)

        # Since fcs is used only for intermediate calculations, the namespace here requires wefe_process
        self._fcs = FCStorage(NAMESPACE.PROCESS, self.generate_name(), num_partition)
        self._fcs.put_all(storage_iterator)

        consume = time.time() - start
        LOGGER.debug(f"ck data upload to fcs take time:{consume}，amount:{data_count}，\
            partition:{num_partition}, info:{self._fcs.to_dict()}")

        return self._fcs

    def dsource(self):
        """
        fcs -> storage
        """
        if self._dsource:
            return self._dsource
        else:
            if not hasattr(self, self.FCS_ATTR) or self._fcs is None:
                raise AssertionError("try create dsource from None")
            return self._fcs_to_dsource()

    # noinspection PyUnusedLocal
    @log_elapsed
    def _fcs_to_dsource(self, **kwargs):
        self._dsource = self.save_as(name=self._name,
                                     namespace=self._namespace,
                                     partition=self._partitions,
                                     persistent=False)._dsource
        return self._dsource

    def get_partitions(self):
        return self._partitions

    @classmethod
    def get_fcs_partitions(cls, num_partition, data_count):
        """
        Get fc storage partition

        Parameters
        ----------
        num_partition
        data_count

        Returns
        -------

        """
        return RuntimeInstance.get_fc_partition()

    def _get_json_source(self, name, namespace, partitions, cloud_store_temp_auth=None):
        return {
            "name": name,
            "namespace": namespace,
            "partitions": partitions,
            "cloud_store_temp_auth": cloud_store_temp_auth
        }

    @staticmethod
    def pickle2hex(func):
        return cloudpickle.dumps(func).hex()

    def _get_fc_input_param(self, func=None, others=None, key_func=None, fraction=None, seed=None,
                            fc_name=None, execution_name=None, need_send=False, map_func=None, reduce_func=None,
                            unfold_result=False):
        """
        Builds input parameters for function

        Parameters
        ----------
        func: the python function to be executed
        others: other data source ，used for join,union
        key_func: key func ，used for reduce
        fraction: fraction，used for sample
        seed: seed，used for sample
        fc_name: function name
        execution_name: execution name
        need_send: whether the calculated results need to be transfer
        map_func: map function, used for mapReducePartitions
        reduce_func: reduce function, used for mapReducePartitions

        Returns
        -------

        """
        source = self._get_json_source(self.fcs().get_name(), self.fcs().get_namespace(), self.fcs().get_partitions(),
                                       self.fcs().get_cloud_store_temp_auth())

        dest = self._get_json_source(self.generate_name(), NAMESPACE.PROCESS, self.fcs().get_partitions())
        partition_list = [i for i in range(self.fcs().get_partitions())]
        param = {"partition_lists": [partition_list[i:i + 100] for i in range(0, len(partition_list), 100)],
                 "source": source, "dest": dest,
                 "fraction": fraction, "seed": seed,
                 "fc_name": fc_name, "execution_name": execution_name,
                 "single_call": fc_name == self.COLLECT_REDUCE_FC_NAME,
                 "unfold_result": unfold_result}

        if func:
            param["func"] = FCSource.pickle2hex(func)
        if others:
            others.fcs()
            param["others"] = self._get_json_source(others.fcs().get_name(), others.fcs().get_namespace(),
                                                    others.fcs().get_partitions(),
                                                    others.fcs().get_cloud_store_temp_auth())
        if key_func:
            param["key_func"] = FCSource.pickle2hex(key_func)

        if map_func:
            param["map_func"] = FCSource.pickle2hex(map_func)

        if reduce_func:
            param["reduce_func"] = FCSource.pickle2hex(reduce_func)

        return param

    def _reset_dest_name(self, fc_input_param):
        """
        Reset dest name

        When a function executes an error, write the OSS results to another directory

        Parameters
        ----------
        fc_input_param

        Returns
        -------

        """
        fc_input_param['dest']['name'] = self.generate_name()
        return fc_input_param

    def _get_rtn_fcs(self, input_param: dict):
        """
        Get return result

        Parameters
        ----------
        input_param

        Returns
        -------

        """
        dest = input_param.get("dest")
        return FCStorage(dest.get("namespace"), dest.get("name"), dest.get("partitions"))

    def _call_fc(self, fc_name, func=None, others=None, key_func=None, fraction=None, seed=None, **kwargs):
        need_send = False
        if "need_send" in kwargs:
            need_send = kwargs.get("need_send")

        map_func = None
        if "map_func" in kwargs:
            map_func = kwargs.get("map_func")

        reduce_func = None
        if "reduce_func" in kwargs:
            reduce_func = kwargs.get("reduce_func")

        # for collect reduce
        unfold_result = False
        if "unfold_result" in kwargs:
            unfold_result = kwargs.get("unfold_result")

        execution_name = 'wefe-' + str(uuid.uuid1())
        input_param = self._get_fc_input_param(fc_name=fc_name, func=func, others=others, key_func=key_func,
                                               fraction=fraction, seed=seed, execution_name=execution_name,
                                               need_send=need_send, map_func=map_func, reduce_func=reduce_func,
                                               unfold_result=unfold_result)

        start = time.time()
        from common.python.calculation.fc.fc_caller import FCCaller

        retry = 5
        exception = None
        last_sleep_time = 0.2
        caller = FCCaller(init_client=False)
        function_name = consts.FunctionIndexName.INDEX

        while retry > 0:

            try:
                # Because collecReduce requires more memory, so calls the high-performance function hpIndex directly
                # The premise of use of high-performance function machines:
                # Features over 500, using ultra-high-performance function 32g machines
                # Features over 100, using high-performance function 16g machine

                features_count = RuntimeInstance.get_features_count()

                if fc_name == self.COLLECT_REDUCE_FC_NAME:
                    if features_count >= 500:
                        function_name = consts.FunctionIndexName.SUPER_HIGH_PERFORMANCE_INDEX
                    elif features_count > 100:
                        function_name = consts.FunctionIndexName.HIGH_PERFORMANCE_INDEX

                caller.call_fc_by_go(input_param, function_name=function_name)
                consume = time.time() - start
                LOGGER.debug(
                    f"function`{fc_name}`time-consuming:{consume},execution_name:{execution_name},\
                    level:{int(consume)},param:{input_param if consume > 1 else None},function_name:{function_name}")
                return self._get_rtn_fcs(input_param)

            except Exception as ex:
                # if used oss ，reset dst name
                if conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_STORAGE_TYPE) == consts.STORAGETYPE.OSS:
                    input_param = self._reset_dest_name(input_param)

                LOGGER.error(f"function`{fc_name}`error,execution_name:{execution_name}", ex)
                exception = ex
                time.sleep(last_sleep_time)
                last_sleep_time = last_sleep_time * 2

                # In the case of high-performance machines, the number of retries is reduced，
                # TODO: Determine if you need to try again with exception information
                retry = retry - (1 if function_name == consts.FunctionIndexName.INDEX else 3)
        raise exception

    @log_elapsed
    def map(self, func, **kwargs):
        rtn_fcs = self._call_fc(fc_name='mapper', func=func, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def mapValues(self, func, **kwargs):
        rtn_fcs = self._call_fc(fc_name='mapValues', func=func, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def mapPartitions(self, func, **kwargs):
        rtn_fcs = self._call_fc(fc_name='mapPartitions', func=func, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def mapPartitions2(self, func, **kwargs):
        rtn_fcs = self._call_fc(fc_name='mapPartitions2', func=func, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def applyPartitions(self, func, **kwargs):
        rtn_fcs = self._call_fc(fc_name='applyPartitions', func=func, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def mapReducePartitions(self, map_func, reduce_func, **kwargs):

        def _dict_reduce(a: dict, b: dict):
            for k, v in b.items():
                if k not in a:
                    a[k] = v
                else:
                    a[k] = reduce_func(a[k], v)
            return a

        def _local_map_reduce(it):
            ret = {}
            for _k, _v in map_func(it):
                if _k not in ret:
                    ret[_k] = _v
                else:
                    ret[_k] = reduce_func(ret[_k], _v)
            return ret

        fc_source = self.applyPartitions(_local_map_reduce)
        rtn_fcs = fc_source._call_fc(fc_name=self.COLLECT_REDUCE_FC_NAME, func=_dict_reduce, key_func=None,
                                     flow_name='singleJob_main', unfold_result=True)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def reduce(self, func, key_func=None):
        rtn_fcs = self._call_fc(fc_name='reduce', func=func, key_func=key_func)
        fc_source = self._tmp_source_from_fcs(rtn_fcs)
        rtn_fcs = fc_source._call_fc(fc_name=self.COLLECT_REDUCE_FC_NAME, func=func, key_func=key_func,
                                     flow_name='singleJob_main')
        return rtn_fcs.first()[1]

    @log_elapsed
    def join(self, other, func=None, **kwargs):
        rtn_fcs = self._call_fc(fc_name='join', func=func, others=other, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def glom(self, **kwargs):
        rtn_fcs = self._call_fc(fc_name='glom', **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def sample(self, fraction, seed=None, **kwargs):
        rtn_fcs = self._call_fc(fc_name='sample', fraction=fraction, seed=seed, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def subtractByKey(self, other, **kwargs):
        rtn_fcs = self._call_fc(fc_name='subtractByKey', others=other, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def filter(self, func, **kwargs):
        rtn_fcs = self._call_fc(fc_name='filter', func=func, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def union(self, other, func=lambda v1, v2: v1, **kwargs):
        rtn_fcs = self._call_fc(fc_name='union', func=func, others=other, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def flatMap(self, func, **kwargs):
        rtn_fcs = self._call_fc(fc_name='flatMap', func=func, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def em(self, func=None, **kwargs):
        rtn_fcs = self._call_fc(fc_name='em', func=func, **kwargs)
        return self._tmp_source_from_fcs(rtn_fcs)

    @log_elapsed
    def collect(self, min_chunk_size=0, use_serialize=True, **kwargs):
        if self._dsource:
            return self._dsource.collect(min_chunk_size, use_serialize)
        else:
            return self.fcs().collect()

    """
    storage api
    """

    def put(self, k, v, use_serialize=True, maybe_large_value=False):
        if not maybe_large_value:
            rtn = self.dsource().put(k, v, use_serialize)
        else:
            rtn = split_put(k, v, use_serialize=use_serialize, put_call_back_func=self.dsource().put)
        self._fcs = None
        return rtn

    @log_elapsed
    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        rtn = self.dsource().put_all(kv_list, use_serialize, chunk_size)
        self._fcs = None
        return rtn

    def get(self, k, use_serialize=True, maybe_large_value=False):
        if not maybe_large_value:
            return self.dsource().get(k, use_serialize)
        else:
            return split_get(k=k, use_serialize=use_serialize, get_call_back_func=self.dsource().get)

    def delete(self, k, use_serialize=True):
        rtn = self.dsource().delete(k, use_serialize)
        self._fcs = None
        return rtn

    def destroy(self):
        if self._dsource:
            self._dsource.destroy()
        else:
            self._fcs = None
        return True

    def put_if_absent(self, k, v, use_serialize=True):
        rtn = self.dsource().put_if_absent(k, v, use_serialize)
        self._fcs = None
        return rtn

    # noinspection PyPep8Naming
    def take(self, n=1, keysOnly=False, use_serialize=True):
        if self._dsource:
            return self._dsource.take(n, keysOnly, use_serialize)
        else:
            rtn = self._fcs.take(n)
            if keysOnly:
                rtn = [pair[0] for pair in rtn]
            return rtn

    # noinspection PyPep8Naming
    def first(self, keysOnly=False, use_serialize=True):
        if self._dsource:
            return self._dsource.first(keysOnly=keysOnly, use_serialize=use_serialize)
        else:
            first = self._fcs.first()
            return first[0] if keysOnly and first else first

    def count(self, **kwargs):
        if self._dsource:
            return self._dsource.count()
        else:
            if self._fcs.get_storage_type() == consts.STORAGETYPE.OSS:
                return self._fcs.count()

            rtn_fcs = self._call_fc(fc_name='count')
            cnt = 0
            for k, v in rtn_fcs.collect():
                cnt += v
            return cnt

    @log_elapsed
    def save_as(self, name, namespace, partition=None, use_serialize=True, persistent=True, **kwargs) -> 'FCSource':
        if partition is None:
            partition = self._partitions
        partition = partition or self._partitions
        # from common.python import RuntimeInstance
        persistent_engine = RuntimeInstance.SESSION.get_persistent_engine()
        if self._dsource:
            _dtable = self._dsource.save_as(name, namespace, partition,
                                            use_serialize=use_serialize,
                                            persistent_engine=persistent_engine)
            return FCSource.from_dsource(session_id=self._session_id, dsource=_dtable)
        else:
            from common.python import session
            dup = session.table(name=name, namespace=namespace, partition=partition, persistent=persistent)
            dup.put_all(self.fcs().collect())
            return dup
