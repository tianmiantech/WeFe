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

import os
import typing
import uuid
from typing import Iterable

# noinspection PyProtectedMember
from common.python import WorkMode, Backend, RuntimeInstance, _STORAGE_VERSION
from common.python.common import consts
from common.python.p_session.session import WefeSession
from common.python.table import Table
from common.python.utils import log_utils, conf_utils
from common.python.utils.log_utils import LoggerFactory
from common.python.utils.profile_util import log_elapsed
from common.python.utils.store_type import StoreTypes, DBTypes
from kernel.utils import consts


def init(job_id=None,
         mode: typing.Union[int, WorkMode] = WorkMode.STANDALONE,
         backend: typing.Union[str, Backend] = Backend.LOCAL,
         persistent_engine: str = StoreTypes.STORE_TYPE_PERSISTENCE,
         storage_version=None,
         set_log_dir=True,
         db_type=None,
         options: dict = None):
    """
    Initializes session, should be called before all.

    Parameters
    ---------
    db_type
    set_log_dir
    storage_version
    persistent_engine
    job_id : string
      job id and default table namespace of this runtime.
    mode : WorkMode
      set work mode,

        - standalone: `WorkMode.STANDALONE` or 0
        - cluster: `WorkMode.CLUSTER` or 1
    backend : Backend
      set computing backend,
        
        - local: `Backend.LOCAL` or 0
        - spark: `Backend.SAPRK` or 1
    options : None or dict
      additional options

    Returns
    -------
    None
      nothing returns

    Examples
    --------
    >>> from common.python import session, WorkMode, Backend
    >>> session.init("a_job_id", WorkMode.Standalone, Backend.LOCAL)
    """
    if RuntimeInstance.SESSION:
        return

    if isinstance(mode, int):
        mode = WorkMode(mode)
    if isinstance(backend, str):
        backend = Backend(backend)
    if job_id is None:
        job_id = str(uuid.uuid1())
        if True:
            LoggerFactory.set_directory()
    else:
        if set_log_dir:
            LoggerFactory.set_directory(os.path.join(log_utils.get_log_root_path(), job_id))
    if storage_version is None:
        storage_version = _STORAGE_VERSION

    if not db_type:
        # Priority reads the data.type node of the public configuration
        db_type = conf_utils.get_comm_config(consts.COMM_CONF_KEY_DATA_TYPE)

    print(f"db_type:{db_type}")
    if db_type not in [DBTypes.CLICKHOUSE, DBTypes.LOCAL_FS, DBTypes.LMDB]:
        raise ValueError(f"{db_type} not supported")

    if backend.is_local():
        if db_type not in [DBTypes.LOCAL_FS, DBTypes.LMDB]:
            raise ValueError(f"{db_type} not supported in Local backend")

        from common.python.p_session.base_impl import build
        builder = build.Builder(session_id=job_id, work_mode=mode, persistent_engine=persistent_engine, db_type=db_type)

    elif backend.is_spark():
        from common.python.calculation.spark import build
        import findspark
        findspark.init()
        builder = build.Builder(session_id=job_id, work_mode=mode, persistent_engine=persistent_engine, db_type=db_type)

    elif backend.is_fc():
        from common.python.calculation.fc import build
        builder = build.Builder(session_id=job_id, work_mode=mode, persistent_engine=persistent_engine, db_type=db_type)

    else:
        raise ValueError(f"backend: ${backend} unknown")

    RuntimeInstance.MODE = mode
    RuntimeInstance.BACKEND = backend
    RuntimeInstance.BUILDER = builder
    RuntimeInstance.SESSION = builder.build_session()
    RuntimeInstance.OPTIONS = options


@log_elapsed
def table(name, namespace=None, partition=48, persistent=True, create_if_missing=True, error_if_exist=False,
          in_place_computing=False, **kwargs) -> Table:
    """
    Loads an existing Table.

    Parameters
    ---------
    name : string
      Table name of result Table.
    namespace : string
      Table namespace of result Table.
    partition : int
      Number of partitions when creating new Table.
    create_if_missing : boolean
      Not implemented. Table will always be created if not exists.
    error_if_exist : boolean
      Not implemented. No error will be thrown if already exists.
    persistent : boolean
      Where to load the Table, `True` from persistent storage and `False` from temporary storage.
    in_place_computing : boolean
      Whether in-place computing is enabled.

    Returns
    -------
    Table
      A Table consisting data loaded.

    Examples
    --------
    >>> from common.python import session
    >>> a = session.table('foo', 'bar', persistent=True)
    """
    namespace = namespace or get_session_id()
    return RuntimeInstance.SESSION.table(name=name,
                                         namespace=namespace,
                                         partition=partition,
                                         persistent=persistent,
                                         in_place_computing=in_place_computing,
                                         create_if_missing=create_if_missing,
                                         error_if_exist=error_if_exist,
                                         **kwargs)


@log_elapsed
def parallelize(data: Iterable, include_key=False, name=None, partition=None, namespace=None, persistent=False,
                create_if_missing=True, error_if_exist=False, chunk_size=100000, in_place_computing=False,
                need_send=False) -> Table:
    """
    Transforms an existing iterable data into a Table.

    Parameters
    ---------
    data : Iterable
      Data to be put.
    include_key : boolean
      Whether to include key when parallelizing data into table.
    name : string
      Table name of result Table. A default table name will be generated when `None` is used
    partition : int
      Number of partitions when parallelizing data.
    namespace : string
      Table namespace of result Table. job_id will be used when `None` is used.
    create_if_missing : boolean
      Not implemented. Table will always be created.
    error_if_exist : boolean
      Not implemented. No error will be thrown if already exists.
    chunk_size : int
      Batch size when parallelizing data into Table.
    in_place_computing : boolean
      Whether in-place computing is enabled.

    Returns
    -------
    Table
      A Table consisting of parallelized data.

    Examples
    --------
    >>> from common.python import session
    >>> table = session.parallelize(range(10), in_place_computing=True)
    """
    if partition is None:
        raise ValueError("partition should be manual set in this version")
    return RuntimeInstance.SESSION.parallelize(data=data, include_key=include_key, name=name, partition=partition,
                                               namespace=namespace,
                                               persistent=persistent,
                                               chunk_size=chunk_size,
                                               in_place_computing=in_place_computing,
                                               create_if_missing=create_if_missing,
                                               error_if_exist=error_if_exist,
                                               need_send=need_send)


def cleanup(name, namespace, persistent=False):
    """
    Destroys Table(s). Wildcard can be used in `name` parameter.

    Parameters
    ---------
    name : string
      Table name to be cleanup. Wildcard can be used here.
    namespace : string
      Table namespace to be cleanup. This needs to be a exact match.
    persistent : boolean
      Where to delete the Tables, `True` from persistent storage and `False` from temporary storage.

    Returns
    -------
    None

    Examples
    --------
    >>> from common.python import session
    >>> session.cleanup('foo*', 'bar', persistent=True)
    """
    return RuntimeInstance.SESSION.cleanup(name=name, namespace=namespace, persistent=persistent)


# noinspection PyPep8Naming
def generateUniqueId():
    """
    Generates a unique ID each time it is invoked.

    Returns
    -------
    string
      uniqueId

    Examples
    --------
    >>> from common.python import session
    >>> session.generateUniqueId()
    """
    return RuntimeInstance.SESSION.generateUniqueId()


def get_session_id():
    """
    Returns session id.

    Returns
    -------
    string
      session id

    Examples
    --------
    >>> from common.python import session
    >>> session.get_session_id()
    """
    return RuntimeInstance.SESSION.get_session_id()


def get_data_table(name, namespace, partition=1):
    """
    return data table instance by table name and table name space

    Parameters
    ---------
    name : string
      table name of data table
    namespace : string
      table name space of data table
    partition : int
      if talbe not exist then create
    
    returns
    -------
    DTable
      data table instance

    Examples
    --------
    >>> from common.python import session
    >>> session.get_data_table(name, namespace)
    """
    return RuntimeInstance.SESSION.get_data_table(name=name, namespace=namespace, partition=partition)


def save_data_table_meta(kv, data_table_name, data_table_namespace):
    """
    Saves metas(in kv) to meta table associated with the table named `data_table_name`
    and namespaced `data_table_namespace`.

    Parameters
    ---------
    kv : dict
      metas to save. v should be serialized by JSON
    data_table_name : string
      table name of this data table
    data_table_namespace : string
      table name of this data table

    Returns
    None

    Examples
    --------
    >>> from common.python import session
    >>> session.save_data_table_meta({"model_id": "a_id", "used_framework": "fate"}, "meta", "readme")
    """
    return RuntimeInstance.SESSION.save_data_table_meta(kv=kv,
                                                        data_table_name=data_table_name,
                                                        data_table_namespace=data_table_namespace)


def get_data_table_meta(key, data_table_name, data_table_namespace):
    """
    Gets meta keyed by `key` from meta table associated with table named `data_table_name`
    and namespaced `data_table_namespace`.

    Parameters
    ---------
    key : string
      associated key.
    data_table_name : string
      table name of this data table
    data_table_namespace : string
      table name of this data table
    
    Returns
    -------
    any
      object associated with `key` provieded

    Examples
    --------
    >>> from common.python import session
    >>> session.get_data_table_meta("model_id", "meta", "readme") # a_id
    """
    return RuntimeInstance.SESSION.get_data_table_meta(key=key,
                                                       data_table_name=data_table_name,
                                                       data_table_namespace=data_table_namespace)


def get_data_table_metas(data_table_name, data_table_namespace):
    """
    Gets metas from meta table associated with table named `data_table_name` and namespaced `data_table_namespace`.

    Parameters
    ---------
    data_table_name : string
      table name of this data table
    data_table_namespace : string
      table name of this data table

    Returns
    -------
    dict
      metas

    Examples
    --------
    >>> from common.python import session
    >>> session.get_data_table_metas("meta", "readme") # {'model_id': 'a_id', 'used_framework': 'fate'}
    """
    return RuntimeInstance.SESSION.get_data_table_metas(data_table_name=data_table_name,
                                                        data_table_namespace=data_table_namespace)


def clean_tables(namespace, regex_string='*'):
    RuntimeInstance.SESSION.clean_table(namespace=namespace, regex_string=regex_string)


def save_data(kv_data: Iterable,
              name,
              namespace,
              partition=1,
              persistent: bool = True,
              create_if_missing=True,
              error_if_exist=False,
              in_version: bool = False,
              version_log=None):
    """
    Saves data to table, optional add version.

    Parameters
    ---------
    kv_data : Iterable
      data to be saved 
    name : string
      table name
    namespace : string
      table namespace
    partition** :int
      Number of partitions when creating new Table.
    create_if_missing : boolean
      Not implemented. Table will always be created if not exists.
    error_if_exist : boolean
      Not implemented. No error will be thrown if already exists.
    persistent : boolean
      Where to load the Table, `True` from persistent storage and `False` from temporary storage.
    in_version : boolean
      add a version log or not
    version_log : string
      log to be added 

    Returns
    -------
    Table

    Examples
    --------
    >>> from common.python import session
    >>> session.save_data([("one", 1), ("two", 2)], "save_data", "readme", in_version=True, version_log="a version")
    """
    return RuntimeInstance.SESSION.save_data(kv_data=kv_data,
                                             name=name,
                                             namespace=namespace,
                                             partition=partition,
                                             persistent=persistent,
                                             create_if_missing=create_if_missing,
                                             error_if_exist=error_if_exist,
                                             in_version=in_version,
                                             version_log=version_log)


def stop():
    """
    Stops session, clean all tables associated with this session.

    Examples
    --------
    >>> from common.python import session
    >>> session.stop()
    """
    RuntimeInstance.SESSION.stop()


def kill():
    RuntimeInstance.SESSION.kill()


def exit():
    RuntimeInstance.SESSION = None
    WefeSession.exit()


def is_table(v):
    return isinstance(v, Table)
