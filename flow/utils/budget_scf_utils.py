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

from tencentcloud.common import credential
from tencentcloud.common.profile.http_profile import HttpProfile
from tencentcloud.common.profile.client_profile import ClientProfile
from tencentcloud.scf.v20180416 import scf_client
from tencentcloud.scf.v20180416 import models as scf_models
from tencentcloud.monitor.v20180724 import monitor_client
from tencentcloud.monitor.v20180724 import models as monitor_models

from common.python.utils import conf_utils
from common.python.common import consts
import json
from datetime import datetime

# The unit price of the number of function calls: 0.0133/10,000 times
FC_CALL_UNIT_PRICE = 0.0133
# Function instance resource unit price 0.00011108 yuan/GB-second
SCF_RESOURCE_UNIT_PRICE = 0.00011108
# The number of function calls is free of charge of 1000,000/month
SCF_FREE_CALL = 100 * 10000
# The function resource is free of charge of 1000,000/month
SCF_FREE_RESOURCE = 100 * 10000


class BudgetScfUtils(object):
    """
        Used for estimation function calculations
    """

    def __init__(self):
        self.cred = self.get_cred()
        self.functions = self.get_functions(self.cred)

    @staticmethod
    def get_cred():
        access_key_id = conf_utils.get_comm_config(consts.COMM_CONF_KEY_SCF_ACCESS_KEY_ID)
        access_key_secret = conf_utils.get_comm_config(consts.COMM_CONF_KEY_SCF_KEY_SECRET)
        cred = credential.Credential(access_key_id, access_key_secret)
        return cred

    @staticmethod
    def get_functions(cred):
        httpProfile = HttpProfile()
        httpProfile.endpoint = "scf.tencentcloudapi.com"
        clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        client = scf_client.ScfClient(cred, "ap-guangzhou", clientProfile)
        req = scf_models.GetAccountRequest()
        params = {
        }
        req.from_json_string(json.dumps(params))
        resp = client.GetAccount(req)
        b = json.loads(resp.to_json_string())
        functions = b['AccountUsage']['Namespace'][0]['Functions']
        return functions

    def get_month_cost(self):
        """
            Assuming that the free credit for the month is not used up, the cost is 0.
        Returns
        -------
            function computing cost in current month.
        """
        resource_cost = self._get_resource_cost()
        total_fc_call = self.get_total_scf_call()
        if resource_cost >= SCF_FREE_RESOURCE:
            month_resource_cost = (resource_cost - SCF_FREE_RESOURCE) * SCF_RESOURCE_UNIT_PRICE
        else:
            month_resource_cost = 0
        if total_fc_call >= SCF_FREE_CALL:
            month_call_cost = int((total_fc_call - SCF_FREE_CALL) / 10000) * FC_CALL_UNIT_PRICE
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

        return self._get_resource_cost(is_month=False) * SCF_RESOURCE_UNIT_PRICE + self.get_total_scf_call(
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

        if is_month:
            start_time = self.get_month_first_daytime()
        else:
            start_time = self.get_current_daytime()
        cred = self.cred
        httpProfile = HttpProfile()
        httpProfile.endpoint = "monitor.tencentcloudapi.com"
        clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        client = monitor_client.MonitorClient(cred, "ap-guangzhou", clientProfile)
        resource_cost = 0
        for function in self.functions:
            req = monitor_models.GetMonitorDataRequest()
            params = {
                "Namespace": "QCE/SCF_V2",
                "MetricName": "MemDuration",
                "Period": 86400,
                "StartTime": start_time,
                "Instances": [
                    {
                        "Dimensions": [
                            {
                                "Name": "functionName",
                                "Value": function
                            },
                            {
                                "Name": "namespace",
                                "Value": "default"
                            }
                        ]
                    }
                ]
            }
            req.from_json_string(json.dumps(params))
            resp = client.GetMonitorData(req)
            b = json.loads(resp.to_json_string())
            print(resp.to_json_string())

            values = b['DataPoints'][0]['Values']
            for value in values:
                resource_cost = resource_cost + value
        return int(resource_cost / 1024 / 1000)

    def get_total_scf_call(self, is_month=True):
        """
            get the total number of function computing calls
        Parameters
        ----------
        is_month: boolean

        Returns
        -------
            function computing calls , Unit 10,000
        """

        if is_month:
            start_time = self.get_month_first_daytime()
        else:
            start_time = self.get_current_daytime()
        cred = self.cred
        httpProfile = HttpProfile()
        httpProfile.endpoint = "monitor.tencentcloudapi.com"
        clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        client = monitor_client.MonitorClient(cred, "ap-guangzhou", clientProfile)
        total_fc_call = 0
        for function in self.functions:
            req = monitor_models.GetMonitorDataRequest()
            params = {
                "Namespace": "QCE/SCF_V2",
                "MetricName": "Invocation",
                "Period": 86400,
                "StartTime": start_time,
                "Instances": [
                    {
                        "Dimensions": [
                            {
                                "Name": "functionName",
                                "Value": function
                            },
                            {
                                "Name": "namespace",
                                "Value": "default"
                            }
                        ]
                    }
                ]
            }
            req.from_json_string(json.dumps(params))
            resp = client.GetMonitorData(req)
            b = json.loads(resp.to_json_string())
            print(resp.to_json_string())

            values = b['DataPoints'][0]['Values']
            for value in values:
                total_fc_call = total_fc_call + value
        return total_fc_call

    @staticmethod
    def get_month_first_daytime():
        return datetime.now().strftime('%Y-%m-01 00:00:00')

    @staticmethod
    def get_current_daytime():
        return datetime.now().strftime('%Y-%m-%d 00:00:00')

    @staticmethod
    def get_now_time():
        return datetime.now()
