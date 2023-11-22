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
import multiprocessing
import os
import time
from ctypes import *
from typing import Iterable

from tablestore import *

# from tablestore.retry import WriteRetryPolicy, NoRetryPolicy
from common.python import RuntimeInstance
from common.python.common import consts
from common.python.common.exception.custom_exception import OTSError
from common.python.storage.fc_storage import FCStorage
from common.python.utils import log_utils, conf_utils, core_utils, file_utils, network_utils
from common.python.utils.core_utils import deserialize
from common.python.utils.profile_util import log_elapsed

# from multiprocessing import Pool

LOGGER = log_utils.get_logger("PROFILING")


class OTS(FCStorage):
    """
    table store: the storage for function computing
    """

    JOB_TAG = "job_tag"
    NAME = 'name'
    PARTITION = 'partition'
    KEY = 'k'
    VALUE = 'v'
    SPLIT_INDEX_NAME = 'split_index'

    KEY_INDEX = 2
    SPLIT_INDEX_INDEX = 3
    VALUE_INDEX = 0

    # byte, split size
    SPLIT_EACH_SIZE = 1024 * 1024
    SPLIT_MAX_FREFIX = 'MAX_'

    def __init__(self, namespace, name, partitions=1, cloud_store_temp_auth=None):
        self._namespace = namespace
        self._name = name
        self._partitions = partitions
        self.schema = {}
        self._cloud_store_temp_auth = cloud_store_temp_auth
        self._client = self.get_client()
        self.ots_table_name = self._namespace
        self._max_workers = multiprocessing.cpu_count()

        self._todo_merge_data = {}

    @staticmethod
    def get_optimal_endpoint_from_temp_auth(temp_auth):

        """

        get a suitable endpoint from template auth

        Parameters
        ----------
        temp_auth: template auth:
            - temp_access_key_id
            - temp_access_key_secret
            - instance_name
            - sts_token
            - temp_auth_end_point
            - temp_auth_internal_end_point

        Returns
        -------

        end_point: an endpoint that can connect the other side with internal network

        """

        internal_end_point = temp_auth.get("temp_auth_internal_end_point")

        # determine whether it is in the same region
        if RuntimeInstance.BACKEND is not None and RuntimeInstance.BACKEND.is_fc():
            region = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_REGION)
            if region in internal_end_point:
                return internal_end_point
        else:
            # try to connect
            if network_utils.check_endpoint_is_connected(internal_end_point):
                return internal_end_point

        end_point = temp_auth.get("temp_auth_end_point")
        return end_point

    def get_client(self):
        """

        via the config or template auth, new an OTS client

        Returns
        -------
        OTS client

        """

        end_point = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OTS_INTERNAL_END_POINT)
        access_key_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID)
        key_secret = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_KEY_SECRET)
        instance_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OTS_INSTANCE_NAME)

        if self._cloud_store_temp_auth is None or self._cloud_store_temp_auth == "":
            OTS.OTS_CLIENT = OTSClient(end_point,
                                       access_key_id,
                                       key_secret,
                                       instance_name,
                                       logger_name='fc-log',
                                       retry_policy=DefaultRetryPolicy(), max_connection=200)
        else:
            temp_auth = self._cloud_store_temp_auth
            OTS.OTS_CLIENT = OTSClient(self.get_optimal_endpoint_from_temp_auth(temp_auth),
                                       temp_auth.get("temp_access_key_id"),
                                       temp_auth.get("temp_access_key_secret"),
                                       temp_auth.get("instance_name"),
                                       sts_token=temp_auth.get("sts_token"),
                                       logger_name='fc-log',
                                       retry_policy=DefaultRetryPolicy(), max_connection=200)
        print('init ots client')
        return OTS.OTS_CLIENT

    def get_pk_name(self, partition_index, name=None):

        """

        pk_name = (hashcode(name + + partition)) % 10007 + name

        Parameters
        ----------
        partition_index: the partition index
        name: table name

        Returns
        -------

        """

        name = name or self._name
        mod = (abs(core_utils.hash_code(str(partition_index) + name))) % 10007
        pk_name = f'{mod}_{name}'
        return pk_name

    def get_primary_key(self, name: str, partition, k_bytes, split_index=''):
        return [(self.NAME, name), (self.PARTITION, partition),
                (self.KEY, bytearray(k_bytes) if type(k_bytes) is bytes else k_bytes),
                (self.SPLIT_INDEX_NAME, split_index)]

    def get_value(self, v_bytes: bytes):
        return [(self.VALUE, bytearray(v_bytes))]

    def init_tb(self):

        """
        init OTS table, and the data expiration time is one day
        Returns
        -------

        """

        schema_of_primary_key = [(self.NAME, 'STRING'), (self.PARTITION, 'INTEGER'),
                                 (self.KEY, 'BINARY'), (self.SPLIT_INDEX_NAME, 'STRING')]
        defined_columns = [(self.VALUE, 'BINARY')]
        table_meta = TableMeta(self.ots_table_name, schema_of_primary_key, defined_columns)
        table_option = TableOptions(time_to_live=86400)
        reserved_throughput = ReservedThroughput(CapacityUnit(0, 0))
        self.get_client().create_table(table_meta, table_option, reserved_throughput)

    def _split_value(self, v_bytes: bytes):
        return [v_bytes[i:i + self.SPLIT_EACH_SIZE] for i in range(0, len(v_bytes), self.SPLIT_EACH_SIZE)]

    def put(self, k, v, use_serialize=True):
        self.put_all([(k, v)], use_serialize=use_serialize)
        return True

    @log_elapsed
    def _each_batch_put(self, put_row_items):
        # batch write
        show_data_size = None
        request = BatchWriteRowRequest()
        request.add(TableInBatchWriteRowItem(self.ots_table_name, put_row_items))
        start = time.time()
        end = time.time()
        print(
            f'_each_batch_put,data_count:{len(put_row_items)},start: {start}, end: {end} ,consume:{end - start},'
            f' data_size:{show_data_size}')

    @log_elapsed
    def put_all(self, kv_list: Iterable, use_serialize=True, chunk_size=100000, debug_info=None):

        """
        put OTS data in multi-process call
        Parameters
        ----------
        kv_list: list
        use_serialize: boolean
            whether serialize the data
        chunk_size: int
            Batch size when parallelizing data into Table.
        debug_info

        Returns
        -------

        """

        each_batch_data_list = self.generate_each_batch_data_param_list(kv_list, batch=10000)

        for each_batch in each_batch_data_list:
            put_all_by_go_4poolmap(each_batch)

    @staticmethod
    def get_data_item_4ctypes(k: bytes, v: bytes, partition: int, split_index: str):
        return DataItem(k, len(k), v, len(v), partition, split_index.encode('utf-8'))

    @staticmethod
    def get_data_group_4ctypes(data_item):
        return DataGroup(pointer(data_item))

    def generate_put_all_data_4go(self, kv_list: Iterable, use_serialize=True, chunk_size=100000):
        """

        generate the data link list, use golang to put all these data into OTS's table

        Parameters
        ----------
        kv_list: list
        use_serialize: boolean
            whether serialize the data
        chunk_size: int
            Batch size when parallelizing data into Table.
        Returns
        -------
        put_row_group_link: a group of data link lists

        """
        data_size = 0
        each_batch_max_size = 3800000
        put_row_items_link = None
        put_row_items_link_current = None
        put_row_group_link = None
        put_row_group_link_current = None
        put_row_items_len = 0

        for k, v in kv_list:
            k_bytes, v_bytes = self.kv_to_bytes(k=k, v=v, use_serialize=use_serialize)
            # get partition by hash key(k_bytes)
            p = self.hash_key_to_partition(k_bytes, self._partitions)
            split_value_list = self._split_value(v_bytes)
            length_split_size = len(split_value_list)

            for i in range(length_split_size):
                split_index = ''
                if length_split_size > 1:
                    split_index = f'{self.SPLIT_MAX_FREFIX + str(i)}' if i == length_split_size - 1 else str(i)

                split_v_bytes = split_value_list[i]
                data_row = self.get_data_item_4ctypes(k_bytes, split_v_bytes, p, split_index)

                # upload data to OTS when data_size > each_batch_max_size or put_row_items_len == 200
                if data_size + len(k_bytes) + len(split_v_bytes) > each_batch_max_size or put_row_items_len == 200:
                    # add to data group
                    new_group = self.get_data_group_4ctypes(put_row_items_link)
                    if put_row_group_link is None:
                        put_row_group_link = new_group
                        put_row_group_link_current = new_group
                    else:
                        put_row_group_link_current.next = pointer(new_group)
                        put_row_group_link_current = new_group
                    put_row_items_link = None
                    data_size = 0
                    put_row_items_len = 0

                # add data item to link list
                if put_row_items_link is None:
                    put_row_items_link = data_row
                    put_row_items_link_current = data_row
                else:
                    put_row_items_link_current.next = pointer(data_row)
                    put_row_items_link_current = data_row
                put_row_items_len += 1
                data_size += len(k_bytes)
                data_size += len(split_v_bytes)

        if put_row_items_len > 0:
            # add to data group
            new_group = self.get_data_group_4ctypes(put_row_items_link)
            if put_row_group_link is None:
                put_row_group_link = new_group
                put_row_group_link_current = new_group
            else:
                put_row_group_link_current.next = pointer(new_group)
                put_row_group_link_current = new_group

        return put_row_group_link

    def generate_each_batch_data_param_list(self, kv_list: Iterable, batch=5000):
        data = []
        count = 0
        for k, v in kv_list:
            data.append((k, v))
            count += 1
            if count == batch:
                yield data, self._namespace, self._name, self._partitions
                count = 0
                data = []
        if len(data) > 0:
            yield data, self._namespace, self._name, self._partitions

    def put_all_by_go(self, kv_list: Iterable, namespace, name, partitions):
        go_util = CDLL(os.path.join(file_utils.get_project_base_directory(), 'business/go/pkg/utils.so'))
        group_link = self.generate_put_all_data_4go(kv_list)
        if group_link:
            end_point = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OTS_INTERNAL_END_POINT)
            access_key_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID)
            key_secret = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_KEY_SECRET)
            instance_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OTS_INSTANCE_NAME)

            go_util.putAllBatchGroup.restype = c_char_p

            res = go_util.putAllBatchGroup(c_char_p(end_point.encode('utf-8')), c_char_p(instance_name.encode('utf-8')),
                                           c_char_p(access_key_id.encode('utf-8')),
                                           c_char_p(key_secret.encode('utf-8')),
                                           c_char_p(namespace.encode('utf-8')), c_char_p(name.encode('utf-8')),
                                           group_link)

            print(res)

    def put_if_absent(self, k, v, use_serialize=True):
        self.put(k, v, use_serialize)

    def _get_row(self, primary_key, columns_to_get, use_serialize):
        try:
            consumed, return_row, next_token = self._client.get_row(self.ots_table_name, primary_key, columns_to_get)
            if len(return_row.attribute_columns) > 0:
                key = return_row.primary_key[self.KEY_INDEX][1]
                value = return_row.attribute_columns[self.VALUE_INDEX][1]
                if use_serialize:
                    return deserialize(key), deserialize(value)
                else:
                    return key, value
        except OTSClientError as e:
            print("get row failed, http_status:%d, error_message:%s" % (e.get_http_status(), e.get_error_message()))
        except OTSServiceError as e:
            print("get row failed, http_status:%d, error_code:%s, error_message:%s, request_id:%s" % (
                e.get_http_status(), e.get_error_code(), e.get_error_message(), e.get_request_id()))

    def get(self, k, use_serialize=True, maybe_large_value=False):
        k_bytes = self.kv_to_bytes(k=k, use_serialize=use_serialize)
        p = self.hash_key_to_partition(k_bytes, self._partitions)
        has_next = self.HasNext(None, None, p, use_serialize, k_bytes)
        for item in self._collect_by_partition(has_next, rtn_next=False):
            return item

    class HasNext(object):
        """
            an object use for determine whether there's data to be queried in OTS table
        """

        def __init__(self, start_primary_key, end_primary_key, partition, use_serialize=True, assign_key=None):
            """

            Parameters
            ----------
            start_primary_key: string
                start primary key for get range function
            end_primary_key: string
                end primary key for get range function
            partition: int
                set the partition when primary key is None, meaning that range query
            use_serialize: boolean
                whether serialize the data
            assign_key: string
                set the specific key when primary key is None, usually use for inquiring single data
            """
            self.start_primary_key = start_primary_key
            self.end_primary_key = end_primary_key
            self.partition = partition
            self.use_serialize = use_serialize
            self.assign_key = assign_key

    def _merge_value(self, values: list):
        new_values = sorted(values, key=lambda x: int(x[0].replace(self.SPLIT_MAX_FREFIX, '')))
        value = b''
        for item in new_values:
            value += item[1]
        return value

    def _merge_action(self, key: bytes, value: bytes, splie_index):
        data_key = f'{self._name}_{key.hex()}'
        if data_key in self._todo_merge_data:
            self._todo_merge_data[data_key].append((splie_index, value))

            # determine whether the data is completely merged
            split_list = self._todo_merge_data[data_key]
            merged = False
            merged_value = None
            for splie_index, _ in split_list:
                if splie_index.startswith(self.SPLIT_MAX_FREFIX):
                    if int(splie_index.replace(self.SPLIT_MAX_FREFIX, '')) == len(split_list) - 1:
                        merged_value = self._merge_value(split_list)
                        merged = True
                        break
            if merged:
                del self._todo_merge_data[data_key]
                return key, merged_value
        else:
            self._todo_merge_data[data_key] = [(splie_index, value)]

    def _deal_collect_row_list(self, row_list, use_serialize=True):
        for row in row_list:
            key = row.primary_key[self.KEY_INDEX][1]
            split_index = row.primary_key[self.SPLIT_INDEX_INDEX][1]
            value = row.attribute_columns[self.VALUE_INDEX][1]
            if split_index == '':
                if use_serialize:
                    yield deserialize(key), deserialize(value)
                else:
                    yield key, value
            else:
                merged_result = self._merge_action(key, value, split_index)
                if merged_result:
                    if use_serialize:
                        yield deserialize(merged_result[0]), deserialize(merged_result[1])
                    else:
                        yield merged_result[0], merged_result[1]

    def _collect_by_partition(self, has_next: HasNext, rtn_next=True, page_limit=None, debug_info=None):

        """

        Parameters
        ----------
        has_next: HasNext
            - start_primary_key
            - end_primary_key
            - partition
            - use_serialize
            - assign_key
        rtn_next: boolean
            if there is next page, whether return HasNext object
        page_limit
        debug_info

        Returns
        -------

        """
        partition = has_next.partition
        use_serialize = has_next.use_serialize
        assign_key = has_next.assign_key
        inclusive_start_primary_key = has_next.start_primary_key or \
                                      self.get_primary_key(self.get_pk_name(partition),
                                                           INF_MIN if partition is None else partition,
                                                           INF_MIN if assign_key is None else assign_key,
                                                           INF_MIN)
        exclusive_end_primary_key = has_next.end_primary_key or \
                                    self.get_primary_key(self.get_pk_name(partition),
                                                         INF_MAX if partition is None else partition,
                                                         INF_MAX if assign_key is None else assign_key,
                                                         INF_MAX)
        columns_to_get = []
        limit = page_limit
        try:
            start_time = time.time()
            print(
                f'debug_info: {debug_info}, start to get range of first page:'
                f'{datetime.datetime.now(tz=datetime.timezone(datetime.timedelta(hours=+8)))}')
            # call get_range api
            consumed, next_start_primary_key, row_list, next_token = self._client.get_range(
                self.ots_table_name, Direction.FORWARD,
                inclusive_start_primary_key, exclusive_end_primary_key,
                columns_to_get,
                limit
            )
            print(
                f'debug_info: {debug_info}, end to get range of first page: {datetime.datetime.now()}, '
                f'consume_time: {time.time() - start_time}, getting partition {partition} data, '
                f'with next：{next_start_primary_key is not None}')
            for item in self._deal_collect_row_list(row_list, use_serialize=use_serialize):
                yield item

            # getting range of next page
            if next_start_primary_key is not None:
                if rtn_next:
                    # return HasNext object, use for multi-process call
                    yield self.HasNext(next_start_primary_key, exclusive_end_primary_key, partition, use_serialize)
                else:
                    next_time = time.time()
                    print(f'debug_info: {debug_info}, start to get range of next page:{next_time}')

                    while next_start_primary_key is not None:
                        inclusive_start_primary_key = next_start_primary_key
                        consumed, next_start_primary_key, row_list, next_token = self._client.get_range(
                            self.ots_table_name, Direction.FORWARD,
                            inclusive_start_primary_key, exclusive_end_primary_key,
                            columns_to_get,
                            limit
                        )
                        for item in self._deal_collect_row_list(row_list):
                            yield item
                    print(f'debug_info: {debug_info}, end to get range of next page:{time.time() - next_time}')

        except OTSClientError as e:
            print("get row failed, http_status:%d, error_message:%s" % (e.get_http_status(), e.get_error_message()))
            raise OTSError(
                f"OTSClientError,get row failed, http_status:{e.get_http_status()}, "
                f"error_message:{e.get_error_message()}")
        except OTSServiceError as e:
            print("get row failed, http_status:%d, error_code:%s, error_message:%s, request_id:%s" % (
                e.get_http_status(), e.get_error_code(), e.get_error_message(), e.get_request_id()))
            raise OTSError(
                f"OTSServiceError,get row failed, http_status:"
                f"{e.get_http_status()}, error_message:{e.get_error_message()}")

    @staticmethod
    def _fill_empty_flag(item_list: list, max_length, fill_flag):
        length = len(item_list)
        if length < max_length:
            for i in range(max_length - length):
                item_list.append(fill_flag)

    def _fill_list_with_same_length(self, data_list: list, fill_flag=None):
        max_length = 0
        for item in data_list:
            if max_length < len(item):
                max_length = len(item)

        for item in data_list:
            self._fill_empty_flag(item, max_length, fill_flag)

        return max_length

    @staticmethod
    def _get_ctypes_values_4charp(value: str):
        return value if value is None else value.encode("utf-8")

    def get_ots_primary_key_4ctypes(self, name: str, partition: int, key: bytes, split_index: str):
        return CotsPK(self.get_pk_name(partition, name).encode("utf-8"), partition,
                      cast(key, POINTER(c_char)) if key is not None else key,
                      0 if key is None else len(key),
                      self._get_ctypes_values_4charp(split_index))

    def _get_range_by_go(self, get_next, go_util=None, page_limit=None):
        go_util = go_util or CDLL(os.path.join(file_utils.get_project_base_directory(), 'business/go/pkg/utils.so'))

        if self._cloud_store_temp_auth:
            temp_auth = self._cloud_store_temp_auth
            end_point = self.get_optimal_endpoint_from_temp_auth(temp_auth)
            access_key_id = temp_auth.get("temp_access_key_id")
            key_secret = temp_auth.get("temp_access_key_secret")
            instance_name = temp_auth.get("instance_name")
            sts_token = temp_auth.get("sts_token")
        else:
            end_point = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OTS_INTERNAL_END_POINT)
            access_key_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID)
            key_secret = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_KEY_SECRET)
            instance_name = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_OTS_INSTANCE_NAME)
            sts_token = ''

        go_util.getPartitionsOnePage.restype = POINTER(CotsPartitionsOnePage)

        res = go_util.getPartitionsOnePage(
            pointer(get_next),
            c_char_p(end_point.encode('utf-8')),
            c_char_p(instance_name.encode('utf-8')),
            c_char_p(access_key_id.encode('utf-8')),
            c_char_p(key_secret.encode('utf-8')),
            c_char_p(self._namespace.encode('utf-8')),
            c_int32(page_limit or 0),
            c_char_p(sts_token.encode('utf-8'))
        )
        return res

    @staticmethod
    def _get_attr_by_pointer(data_pointer, attr):
        if data_pointer:
            return getattr(getattr(data_pointer, 'contents'), attr)
        return None

    def _copy_c_ots_pk(self, pk_pointer):
        k_pointer = self._get_attr_by_pointer(pk_pointer, "k")
        k_size = self._get_attr_by_pointer(pk_pointer, "ksize")

        ots_pk = CotsPK()
        ots_pk.name = self._get_attr_by_pointer(pk_pointer, "name")
        ots_pk.partition = self._get_attr_by_pointer(pk_pointer, "partition")
        ots_pk.k = cast(c_char_p(string_at(k_pointer, k_size)), POINTER(c_char)) if k_pointer else None
        ots_pk.ksize = k_size
        ots_pk.splitIndex = self._get_attr_by_pointer(pk_pointer, "splitIndex")

        return ots_pk

    def _copy_c_next(self, page_get_next):
        return CgetNext(pointer(self._copy_c_ots_pk(self._get_attr_by_pointer(page_get_next, "startPK"))),
                        pointer(self._copy_c_ots_pk(self._get_attr_by_pointer(page_get_next, "endPK"))), None)

    def _get_next_param_4go(self, page_result=None):
        if page_result:

            head = None
            tail = None
            current_page_result = page_result

            while True:
                page_get_next = self._get_attr_by_pointer(current_page_result, "getNext")
                if page_get_next:
                    copy_next = self._copy_c_next(page_get_next)
                    if tail:
                        tail.next = pointer(copy_next)
                        tail = copy_next
                    else:
                        head = copy_next
                        tail = copy_next

                if not self._get_attr_by_pointer(current_page_result, "next"):
                    break
                else:
                    current_page_result = self._get_attr_by_pointer(current_page_result, "next")
            return head

        else:
            head = CgetNext()
            tail = head
            for partition in range(self._partitions):
                ots_start_pk = self.get_ots_primary_key_4ctypes(self._name, partition, None, None)
                ots_end_pk = self.get_ots_primary_key_4ctypes(self._name, partition, None, None)
                if partition == 0:
                    tail.startPK = pointer(ots_start_pk)
                    tail.endPK = pointer(ots_end_pk)
                else:
                    new_get_next = CgetNext()
                    new_get_next.startPK = pointer(ots_start_pk)
                    new_get_next.endPK = pointer(ots_end_pk)
                    tail.next = pointer(new_get_next)
                    tail = new_get_next
            return head

    def _get_data_list_by_go_page_result(self, page_result):
        data_list = []

        page_tail = page_result

        # handle page link list
        while True:
            if page_tail:
                # handle ots item
                data_tail = self._get_attr_by_pointer(page_tail, "data")
                item_data = []
                while True:
                    if data_tail:
                        k_pointer = self._get_attr_by_pointer(data_tail, "k")
                        v_pointer = self._get_attr_by_pointer(data_tail, "v")
                        ksize = self._get_attr_by_pointer(data_tail, "ksize")
                        vsize = self._get_attr_by_pointer(data_tail, "vsize")

                        k = string_at(k_pointer, ksize)
                        v = string_at(v_pointer, vsize)

                        split_index = self._get_attr_by_pointer(data_tail, "splitIndex").decode()
                        if split_index == "":
                            item_data.append((k, v))
                        else:
                            merge_result = self._merge_action(k, v, split_index)
                            if merge_result:
                                item_data.append(merge_result)

                        data_tail = self._get_attr_by_pointer(data_tail, "next")
                    else:
                        if item_data:
                            data_list.append(item_data)
                        break

                page_tail = self._get_attr_by_pointer(page_tail, "next")
            else:
                break
        return data_list

    @log_elapsed
    def collect(self, min_chunk_size=0, use_serialize=True, partition=None, dispersal=True, page_limit=None,
                debug_info=None, only_key=False) -> list:
        """
            get all data
        Parameters
        ----------
        min_chunk_size
        use_serialize: boolean
            whether serialize the data
        partition
        dispersal: boolean
            distribute the data in each partition
        page_limit: int
            page size limit
        debug_info:
        only_key:

        Returns
        -------

        data list

        """

        if partition is None:
            LOGGER.debug(
                f'to collect by go: namespace:{self._namespace},name:{self._name},partitions:{self._partitions}')

            page_result = None
            go_util = CDLL(os.path.join(file_utils.get_project_base_directory(), 'business/go/pkg/utils.so'))
            get_next = self._get_next_param_4go()
            while True:
                if get_next:
                    page_result = self._get_range_by_go(get_next, go_util, page_limit)
                    data_list = self._get_data_list_by_go_page_result(page_result)
                    max_length = self._fill_list_with_same_length(data_list)
                    for j in range(max_length):
                        for i in range(len(data_list)):
                            d = data_list[i][j]
                            if d is not None:
                                yield deserialize(d[0]), deserialize(d[1])
                    get_next = self._get_next_param_4go(page_result)
                    # free memory
                    go_util.freeCotsPartitionPage(page_result)
                else:
                    break
        else:
            for item in self._collect_by_partition(self.HasNext(None, None, partition, use_serialize), rtn_next=False,
                                                   page_limit=page_limit, debug_info=debug_info):
                yield item

    def delete(self, k, use_serialize=True):
        pass

    def destroy(self):
        pass

    @log_elapsed
    def count(self, partition=None):
        count = 0
        for k, v in self.collect(partition=partition):
            count += 1
        return count

    @log_elapsed
    def take(self, n=1, keysOnly=False, use_serialize=True, partition=None):
        if n <= 0:
            n = 1
        page_limit = None
        if n < 200:
            page_limit = n
        it = self.collect(use_serialize=use_serialize, partition=partition, page_limit=page_limit)
        rtn = list()
        i = 0
        for item in it:
            if keysOnly:
                rtn.append(item[0])
            else:
                rtn.append(item)
            i += 1
            if i == n:
                break
        return rtn

    @log_elapsed
    def first(self, keysOnly=False, use_serialize=True, partition=None):
        data_list = self.take(n=1)
        if data_list:
            return data_list[0]
        return None

    def save_as(self, name, namespace, partition=None, use_serialize=True, **kwargs):
        save_ots = OTS(namespace, name, partition)
        save_ots.put_all(self.collect(), use_serialize=use_serialize)
        return save_ots


class DataItem(Structure):
    """
        ctypes structure link list
    """
    pass


DataItem._fields_ = [
    ("k", c_char_p),
    ("ksize", c_int),
    ("v", c_char_p),
    ("vsize", c_int),
    ("p", c_int),
    ("si", c_char_p),  # splitIndex
    ("next", POINTER(DataItem))
]


class DataGroup(Structure):
    """
        ctypes structure link list
    """
    pass


DataGroup._fields_ = [
    ("data", POINTER(DataItem)),
    ("next", POINTER(DataGroup))
]


class CotsPK(Structure):
    """
        primary key of the OTS table: name,partition,k,split_index
    """
    _fields_ = [
        ("name", c_char_p),
        ("partition", c_int),
        ("k", POINTER(c_char)),
        ("ksize", c_int),
        ("splitIndex", c_char_p)
    ]


class CgetNext(Structure):
    """
    ctypes structure GetNext
    """
    pass


CgetNext._fields_ = [
    ("startPK", POINTER(CotsPK)),
    ("endPK", POINTER(CotsPK)),
    ("next", POINTER(CgetNext))
]


class CotsItem(Structure):
    """
        the return structure of the getRange function
    """
    pass


# When the data is of type byte, you must explicitly use the POINTER(c_char) type.
# If you use c_char_p, the data will be lost during implicit conversion.
CotsItem._fields_ = [
    ("k", POINTER(c_char)),
    ("ksize", c_int),
    ("v", POINTER(c_char)),
    ("vsize", c_int),
    ("splitIndex", c_char_p),
    ("next", POINTER(CotsItem))
]


class CotsPartitionsOnePage(Structure):
    pass


CotsPartitionsOnePage._fields_ = [
    ("data", POINTER(CotsItem)),
    ("getNext", POINTER(CgetNext)),
    ("next", POINTER(CotsPartitionsOnePage))
]


def put_all_by_go_4poolmap(param_list):
    ins = OTS(param_list[1], param_list[2], param_list[3])
    return ins.put_all_by_go(*param_list)


def get_put_data(data_list, rows):
    kv_lists = []
    kv_list = []
    for k, v in data_list:
        kv_list.append((k, v))
        if len(kv_list) == rows:
            kv_lists.append(kv_list)
            kv_list = []
    if len(kv_list) > 0:
        kv_lists.append(kv_list)

    return kv_lists


def error_callback(e):
    print(e)
    raise Exception(e)


def collect_each_partition_4poolmap(param_list):
    ins = OTS(param_list[0], param_list[1], param_list[2])
    return list(ins._collect_by_partition(param_list[3]))


async def _each_batch_put_aio(put_row_items, namespace, name, partition, show_data_size=None):
    # batch write
    ots = OTS(namespace, name, partition)
    request = BatchWriteRowRequest()
    request.add(TableInBatchWriteRowItem(ots.ots_table_name, put_row_items))
    result = await ots._client.batch_write_row_aio(request)
    return result


async def _each_batch_put_aio_main(item_param):
    return await _each_batch_put_aio(*item_param)


def free_test():
    go_util = CDLL(os.path.join(file_utils.get_project_base_directory(), 'business/go/pkg/utils.so'))
    go_util.memoryFreeTest.restype = POINTER(DataItem)
    _res = go_util.memoryFreeTest()

    try:
        go_util.freePoint(_res)
    except Exception as ex:
        print(ex)


def go_get_pk_name(name, partition):
    """
        test
    Parameters
    ----------
    name
    partition

    Returns
    -------

    """
    go_util = CDLL(os.path.join(file_utils.get_project_base_directory(), 'business/go/pkg/utils.so'))
    go_util.getPKNameGo.restype = c_char_p
    res = go_util.getPKNameGo(c_char_p(name.encode('utf-8')), c_int32(partition))
    result = bytes.decode(res, encoding='utf-8')
    print("go result", result)
    return result
