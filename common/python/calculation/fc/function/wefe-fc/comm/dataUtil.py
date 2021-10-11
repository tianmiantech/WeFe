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

import datetime
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

from common.python.calculation.fc.fc_storage import FCStorage


def get_fc_storages_has_other(evt, context=None):
    """

    Parameters
    ----------
    evt: the data passed in when the user calls the function
    context: contains some information about the runtime of the function like:
            - requestId
            - function
            - credentials
            - service
            - region
            - accountId

    Returns
    -------
    source_fcs, others_fcs, dest_fcs: the source ,other ,destination fcStorage Object
    """

    # source storage args
    source_name = evt['source']['name']
    source_namespace = evt['source']['namespace']
    source_partitions = evt['source']['partitions']
    source_fcs = None
    if evt['source']['cloud_store_temp_auth'] is not None:
        cloud_store_temp_auth = evt['source']['cloud_store_temp_auth']
        source_fcs = FCStorage(source_namespace, source_name, partitions=source_partitions,
                               cloud_store_temp_auth=cloud_store_temp_auth)
    else:
        source_fcs = FCStorage(source_namespace, source_name, partitions=source_partitions)

    # destination storage args
    dest_name = evt['dest']['name']
    dest_namespace = evt['dest']['namespace']
    dest_partitions = evt['dest']['partitions']
    # others storage args
    others_name = evt['others']['name']
    others_namespace = evt['others']['namespace']
    others_partitions = evt['others']['partitions']
    others_fcs = None
    if evt['others']['cloud_store_temp_auth'] is not None:
        cloud_store_temp_auth = evt['others']['cloud_store_temp_auth']
        others_fcs = FCStorage(others_namespace, others_name, partitions=others_partitions,
                               cloud_store_temp_auth=cloud_store_temp_auth)
    else:
        others_fcs = FCStorage(others_namespace, others_name, partitions=others_partitions)

    dest_fcs = FCStorage(dest_namespace, dest_name, partitions=dest_partitions)

    partition = evt['partition']
    with ThreadPoolExecutor() as t:
        all_task = []
        all_task.append(t.submit(get_data, source_fcs, partition, True, context))
        all_task.append(t.submit(get_data, others_fcs, partition, False, context))
        result_list = list(as_completed(all_task))
        for result in result_list:
            if result.result()[1]:
                source_fcs_dict = result.result()[0]
            else:
                others_fcs_dict = result.result()[0]
    return source_fcs_dict, others_fcs_dict, dest_fcs


def get_data(fcs, partition, source=True, context=None):
    return dict(fcs.collect(partition=partition, debug_info=get_request_id(context))), source


def get_fc_storages(evt):
    """

    Parameters
    ----------
    evt: the data passed in when the user calls the function

    Returns
    -------
    source_fcs, dest_fcs: the source and destination fcStorage Object
    """

    # source storage args
    source_name = evt['source']['name']
    source_namespace = evt['source']['namespace']
    source_partitions = evt['source']['partitions']
    source_fcs = None
    if evt['source']['cloud_store_temp_auth'] is not None:
        cloud_store_temp_auth = evt['source']['cloud_store_temp_auth']
        source_fcs = FCStorage(source_namespace, source_name, partitions=source_partitions,
                               cloud_store_temp_auth=cloud_store_temp_auth)
    else:
        source_fcs = FCStorage(source_namespace, source_name, partitions=source_partitions)

    # destination storage args
    dest_name = evt['dest']['name']
    dest_namespace = evt['dest']['namespace']
    dest_partitions = evt['dest']['partitions']
    dest_fcs = FCStorage(dest_namespace, dest_name, partitions=dest_partitions)
    return source_fcs, dest_fcs


def get_execution_id(evt):
    """
    Parameters
    ----------
    evt: the data passed in when the user calls the function

    Returns
    -------
    execution name
    """
    return evt['execution_name'] if evt else ''


def get_request_id(cot):
    if cot:
        return cot.requestId


def fc_print(evt, info, cot=None):
    """

    Parameters
    ----------
    evt: the data passed in when the user calls the function
    info: function execute message like:
        - action_name
        - consume
        - perf_counter_consume
        - process_time
    cot: contains some information about the runtime of the function like:
        - requestId
        - function
        - credentials
        - service
        - region
        - accountId

    Returns
    -------

    """
    print(f'{get_execution_id(evt)},{datetime.datetime.now()},{info},req_id:{get_request_id(cot)}')


def fc_result(code=100, message=None, count=None, partition=None, req_id=None):
    return {
        'code': code,
        'message': message,
        'req_id': req_id,
        'count': count,
        'partition': partition
    }


class TimeConsume(object):
    def __init__(self, auto_reset=True, auto_print=True):
        self.auto_reset = auto_reset
        self.auto_print = auto_print
        self._start = time.time()
        self._perf_counter_start = time.perf_counter()
        self._process_time_start = time.process_time()
        self._end = None
        self._perf_counter_end = None
        self._process_time_end = None

    def start(self):
        self._start = time.time()
        self._perf_counter_start = time.perf_counter()
        self._process_time_start = time.process_time()

    def end(self, action_name, evt=None, cot=None):
        self._end = time.time()
        self._perf_counter_end = time.perf_counter()
        self._process_time_end = time.process_time()

        total_consume = self._end - self._start
        perf_counter_consume = self._perf_counter_end - self._perf_counter_start
        process_time = self._process_time_end - self._process_time_start

        if self.auto_print:
            fc_print(evt,
                     f'action_name:{action_name},consume:{total_consume},perf_counter_consume:{perf_counter_consume}'
                     f',process_time:{process_time}', cot=cot)
        if self.auto_reset:
            self._start = time.time()
            self._perf_counter_start = time.perf_counter()
            self._process_time_start = time.process_time()
        return total_consume
