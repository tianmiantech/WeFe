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

from common.python.common import consts
from common.python.storage.impl import ots_storage
from common.python.utils import conf_utils


def init_ots_table():
    """
    init OTS data table
    :return:
    """
    fc_storage_type = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_STORAGE_TYPE)
    if fc_storage_type != consts.STORAGETYPE.OTS:
        return

    tables = [consts.NAMESPACE.PROCESS, consts.NAMESPACE.TRANSFER]
    for table in tables:
        try:
            ots = ots_storage.OTS(table, "test")
            ots.init_tb()
        except Exception as e:
            print(e)


if __name__ == '__main__':
    init_ots_table()
