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

from common.python.storage.impl.clickhouse_storage import ClickHouseStorage
from common.python.utils.log_utils import schedule_logger
from flow.cycle_actions.flow_action_queue.worker.base_flow_action_worker import BaseFlowActionWorker


class SaveOutputDataWorker(BaseFlowActionWorker):
    """
    The processor when the save_output_data signal is received
    """

    def work(self, params):
        name = params.get('name')
        namespace = params.get('namespace')
        # fcs
        fcs_name = params.get('fcs_info').get('name')
        fcs_namespace = params.get('fcs_info').get('namespace')
        partitions = params.get('partitions')

        schedule_logger().info('schedule save data {}'.format(params))

        self.save_to_ck(name, namespace, fcs_name, fcs_namespace, partitions)

    @staticmethod
    def save_to_ck(name, namespace, fcs_name, fcs_namespace, partitions):
        """
            save fc data into clickHouse table
        Parameters
        ----------
        name: clickHouse table name
        namespace: clickHouse namespace
        fcs_name: fc table name
        fcs_namespace: fc namespace
        partitions: data is divided into several partitions on fc table

        Returns
        -------

        """
        from common.python.calculation.fc.fc_storage import FCStorage
        # get fcs object
        fcs = FCStorage(namespace=fcs_namespace, name=fcs_name, partitions=partitions)
        data = fcs.collect()
        ck_storage = ClickHouseStorage(_type=None, namespace=namespace, name=name)
        ck_storage.put_all(data)
