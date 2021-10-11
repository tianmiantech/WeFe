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

import json

import netifaces
import requests


class NetworkInfo:
    # Gateway
    routing_gateway: str
    # Nic Name
    routing_nic_name: str
    # Nic Mac Address
    routing_nic_mac_addr: str
    # IP Address
    routing_ip_addr: str
    # Net Mask
    routing_ip_netmask: str


def get_local_default_network_info() -> NetworkInfo:
    """
    Use the netifaces tool to get the information of the default network card
    This tool performs well in a multi-NIC environment, and will not get a virtual network card.

    https://pypi.org/project/netifaces/
    """
    info = NetworkInfo()

    info.routing_gateway = netifaces.gateways()['default'][netifaces.AF_INET][0]
    info.routing_nic_name = netifaces.gateways()['default'][netifaces.AF_INET][1]

    for interface in netifaces.interfaces():
        if interface == info.routing_nic_name:

            info.routing_nic_mac_addr = netifaces.ifaddresses(interface)[netifaces.AF_LINK][0]['addr']
            try:

                info.routing_ip_addr = netifaces.ifaddresses(interface)[netifaces.AF_INET][0]['addr']
                # TODO(Guodong Ding) Note: On Windows, netmask maybe give a wrong result in 'netifaces' module.
                info.routing_ip_netmask = netifaces.ifaddresses(interface)[netifaces.AF_INET][0]['netmask']
            except KeyError:
                pass

    return info


def get_local_ip():
    """
    Get local

    Returns
    -------

    """
    return get_local_default_network_info().routing_ip_addr


def get_internet_ip():
    """

    Get  external IP address

    Returns
    -------

    """
    net_ip = _get_net_ip_from_sohu()
    if is_ip_addr(net_ip):
        return net_ip

    net_ip = _get_net_ip_from_if_config_co()
    if is_ip_addr(net_ip):
        return net_ip
    return ''


def is_ip_addr(str):
    """
    Simply determine whether the string is in the IP address format

    Parameters
    ----------
    str

    Returns
    -------

    """
    if len(str) == 0:
        return False
    return str.find('.') != -1


def _get_net_ip_from_sohu():
    """
    Get IP address from Sohu service

    Address response format：var returnCitySN = {"cip": "183.3.218.18", "cid": "440113", "cname": "广东省广州市番禺区"};

    Returns
    -------

    """
    try:
        url = 'http://pv.sohu.com/cityjson?ie=utf-8'
        text = requests.get(url).text
        if len(text) == 0:
            return ''
        data = text.split('=')[1].replace(';', '')
        return json.loads(data)['cip'].lstrip()
    except KeyError:
        return ''


def _get_net_ip_from_if_config_co():
    """
    Get IP address from IfconfigCo service

    Returns
    -------

    """
    try:
        url = 'https://ifconfig.co/ip'
        text = requests.get(url).text
        if len(text) == 0:
            return ''
        return text.lstrip()
    except KeyError:
        return ''


_endpoint_check_result = {}


def check_endpoint_is_connected(endpoint):
    """
    Determine whether the endpoint can be connected

    :param endpoint:
    :return:
    """
    if endpoint in _endpoint_check_result:
        return _endpoint_check_result[endpoint]
    try:
        _endpoint_check_result[endpoint] = True
        print(f'check_endpoint_is_connected：{_endpoint_check_result[endpoint]},endpoint:{endpoint}')
        return True
    except KeyError:
        _endpoint_check_result[endpoint] = False
        print(f'check_endpoint_is_connected：{_endpoint_check_result[endpoint]},endpoint:{endpoint}')
        return False
