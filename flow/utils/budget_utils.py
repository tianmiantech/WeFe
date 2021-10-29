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

from aliyunsdkcore.client import AcsClient
from aliyunsdkcms.request.v20190101.DescribeMetricListRequest import DescribeMetricListRequest
from common.python.utils import conf_utils
from common.python.common import consts
import json
from datetime import datetime

# The unit price of the number of function calls: 0.0133/10,000 times
FC_CALL_UNIT_PRICE = 0.0133
# Function instance resource unit price 0.000110592 yuan/GB-second
FC_RESOURCE_UNIT_PRICE = 0.000110592
# The number of function calls is free of charge of 1 million/month
FC_FREE_CALL = 100
# The function resource is free of charge of 400,000/month
FC_FREE_RESOURCE = 40 * 10000


class BudgetUtils(object):
    """
        Used for estimation function calculations
    """

    def __init__(self):
        self.client = self.get_client()

    @staticmethod
    def get_client():
        access_key_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCESS_KEY_ID)
        access_key_secret = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_KEY_SECRET)
        fc_region = conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_REGION)
        return AcsClient(access_key_id, access_key_secret, fc_region)

    def get_month_cost(self):
        """
            Assuming that the free credit for the month is not used up, the cost is 0.
        Returns
        -------
            function computing cost in current month.
        """
        resource_cost = self._get_resource_cost()
        total_fc_call = self.get_total_fc_call()
        if resource_cost >= FC_FREE_RESOURCE:
            month_resource_cost = (resource_cost - FC_FREE_RESOURCE) * FC_RESOURCE_UNIT_PRICE
        else:
            month_resource_cost = 0
        if total_fc_call >= FC_FREE_CALL:
            month_call_cost = (total_fc_call - FC_FREE_CALL) * FC_CALL_UNIT_PRICE
        else:
            month_call_cost = 0
        return month_call_cost + month_resource_cost

    def get_day_cost(self):
        """
            check function computing day cost when the month's free credit is used
        Returns
        -------
            function computing day cost
        """

        return self._get_resource_cost(is_month=False) * FC_RESOURCE_UNIT_PRICE + self._get_resource_cost(
            is_month=False) * FC_CALL_UNIT_PRICE

    def _get_resource_cost(self, is_month=True):
        """
            Use for getting function computing cost in the current month/day
        Parameters
        ----------
        is_month: boolean

        Returns
        -------
            function computing month/day cost
        """

        request = DescribeMetricListRequest()
        request.set_accept_format('json')
        if is_month:
            start_time = self.get_month_first_daytime()
        else:
            start_time = self.get_current_daytime()
        request.set_StartTime(start_time)
        request.set_Namespace("acs_fc")
        request.set_action_name("DescribeMetricList")
        request.set_MetricName("FunctionOnDemandUsage")
        request.set_Dimensions({
            "userId": conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCOUNT_ID),
            "region": conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_REGION),
            "serviceName": conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_SERVICE_NAME),
            "functionName": 'index'
        })
        response = self.client.do_action_with_exception(request)
        b = json.loads(str(response, encoding='utf-8'))
        data_points = json.loads(b['Datapoints'])
        total_value = 0

        for data_point in data_points:
            total_value += data_point['Value']

        return total_value / (1024 * 10)

    def get_total_fc_call(self, is_month=True):
        """
            get the total number of function computing calls
        Parameters
        ----------
        is_month: boolean

        Returns
        -------
            function computing calls , Unit 10,000
        """

        request = DescribeMetricListRequest()
        request.set_accept_format('json')
        if is_month:
            start_time = self.get_month_first_daytime()
        else:
            start_time = self.get_current_daytime()

        request.set_StartTime(start_time)
        request.set_Namespace("acs_fc")
        request.set_MetricName("FunctionTotalInvocations")
        request.set_Dimensions({
            "userId": conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_ACCOUNT_ID),
            "region": conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_REGION),
            "serviceName": conf_utils.get_comm_config(consts.COMM_CONF_KEY_FC_SERVICE_NAME),
            "functionName": 'index'
        })

        response = self.client.do_action_with_exception(request)
        b = json.loads(str(response, encoding='utf-8'))
        data_points = json.loads(b['Datapoints'])
        total_value = 0

        for datapoint in data_points:
            total_value += datapoint['Value']
        return total_value / 10000

    @staticmethod
    def get_month_first_daytime():
        return datetime.now().strftime('%Y-%m-01 00:00:00')

    @staticmethod
    def get_current_daytime():
        return datetime.now().strftime('%Y-%m-%d 00:00:00')
