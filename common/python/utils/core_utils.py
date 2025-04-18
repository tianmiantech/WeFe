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

import base64
import hashlib
import json
import os
import pickle
import socket
import time
import uuid
import platform

import numpy as np


def fate_uuid():
    return uuid.uuid1().hex


def get_commit_id():
    # the model may be larger, SHA1 is not used
    return fate_uuid()


def string_to_bytes(string):
    return string if isinstance(string, bytes) else string.encode(encoding="utf-8")


def bytes_to_string(byte):
    return byte.decode(encoding="utf-8")


def json_dumps(src, byte=False):
    if byte:
        return string_to_bytes(json.dumps(src))
    else:
        return json.dumps(src)


def json_loads(src):
    if isinstance(src, bytes):
        return json.loads(bytes_to_string(src))
    else:
        return json.loads(src)


def current_timestamp():
    return int(time.time() * 1000)


def current_datetime():
    return time.localtime(time.time())


def get_delta_seconds(a, b):
    second = 0
    pre = a
    now = b

    if b < a:
        pre = b
        now = a

    delta = now - pre

    day = delta.days
    if day > 0:
        second = second + day * 24 * 60 * 60

    return second + delta.seconds


def timestamp_to_date(timestamp=current_timestamp(), format_string="%Y-%m-%d %H:%M:%S"):
    timestamp = int(timestamp) / 1000
    time_array = time.localtime(timestamp)
    str_date = time.strftime(format_string, time_array)
    return str_date


def base64_encode(src):
    return bytes_to_string(base64.b64encode(src.encode("utf-8")))


def base64_decode(src):
    return bytes_to_string(base64.b64decode(src))


def serialize_b64(src, to_str=False):
    dest = base64.b64encode(pickle.dumps(src))
    if not to_str:
        return dest
    else:
        return bytes_to_string(dest)


def deserialize_b64(src):
    return pickle.loads(base64.b64decode(string_to_bytes(src) if isinstance(src, str) else src))


def get_lan_ip():
    if os.name != "nt":
        import fcntl
        import struct

        def get_interface_ip(ifname):
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            return socket.inet_ntoa(
                fcntl.ioctl(s.fileno(), 0x8915, struct.pack('256s', string_to_bytes(ifname[:15])))[20:24])

    ip = socket.gethostbyname(socket.getfqdn())
    if ip.startswith("127.") and os.name != "nt":
        interfaces = [
            "bond1",
            "eth0",
            "eth1",
            "eth2",
            "wlan0",
            "wlan1",
            "wifi0",
            "ath0",
            "ath1",
            "ppp0",
        ]
        for if_name in interfaces:
            try:
                ip = get_interface_ip(if_name)
                break
            except IOError:
                pass
    return ip or ''


def serialize(src):
    """
    The default serialization method

    In non-special cases, this method is used, protocol=2

    Parameters
    ----------
    src: object
        data to serialize

    Returns
    -------

    """
    return pickle.dumps(src, protocol=2)


def deserialize(src):
    """
    The default deserialization method,
    can automatically read the version number, so no need to specify

    Parameters
    ----------
    src

    Returns
    -------

    """
    return pickle.loads(src)


def md5(src):
    return hashlib.md5(src.encode('utf-8')).hexdigest()


def hash_code(s):
    """
    Calculate the hashcode value, same to Java

    Parameters
    ----------
    s

    Returns
    -------

    """
    h = 0
    if len(s) > 0:
        for item in s:
            h = np.int32(31) * h + np.int32(ord(item))
        return np.int32(h)
    else:
        return 0


def is_windows_sys():
    return platform.system().lower() == "windows"
