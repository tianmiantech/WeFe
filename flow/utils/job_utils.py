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

import os
import smtplib
import subprocess
from email.header import Header
from email.mime.text import MIMEText

from common.python.protobuf.pyproto import gateway_meta_pb2
from common.python.protobuf.pyproto.gateway_meta_pb2 import TransferMeta
from common.python.utils import log_utils, file_utils
from common.python.utils.core_utils import get_commit_id
from common.python.utils.log_utils import schedule_logger
from flow.settings import stat_logger, JOB_GRPC


def run_subprocess(config_dir, process_cmd, log_dir=None):
    stat_logger.info('Starting process command: {}'.format(process_cmd))
    # stat_logger.info(' '.join(process_cmd))

    os.makedirs(config_dir, exist_ok=True)
    if log_dir:
        os.makedirs(log_dir, exist_ok=True)
    std_log = open(os.path.join(log_dir if log_dir else config_dir, 'std.log'), 'w')
    pid_path = os.path.join(config_dir, 'pid')

    if os.name == 'nt':
        startupinfo = subprocess.STARTUPINFO()
        startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
        startupinfo.wShowWindow = subprocess.SW_HIDE
    else:
        startupinfo = None
    p = subprocess.Popen(process_cmd,
                         stdout=std_log,
                         stderr=std_log,
                         startupinfo=startupinfo
                         )
    with open(pid_path, 'w') as f:
        f.truncate()
        f.write(str(p.pid) + "\n")
        f.flush()
    return p


def get_job_directory(job_id):
    return os.path.join(file_utils.get_project_base_directory(), 'jobs', job_id)


def get_job_log_directory(job_id):
    return os.path.join(log_utils.get_log_root_path(), job_id)


def send(dst_member_id, processor=None, action=None, content_str=""):
    """
    Send message to gateway service.

    Obsolete, please use GatewayService.send()
    """

    dst = gateway_meta_pb2.Member(memberId=dst_member_id)
    content = gateway_meta_pb2.Content(objectData=content_str)
    transfer_meta = TransferMeta(sessionId=get_commit_id(), dst=dst, content=content,
                                 action=action,
                                 taggedVariableName=None,
                                 processor=processor)
    result = JOB_GRPC.send(transfer_meta)
    schedule_logger().debug("[REMOTE] send result:%s", result.message)

    return result


def mail(host, port=25, username=None, password=None, sender=None, sender_alias='WEFE', receivers=None, subject=None,
         content=None):
    message = MIMEText(content, 'plain', 'utf-8')
    message['From'] = Header(sender_alias, 'utf-8')
    message['Subject'] = Header(subject, 'utf-8')
    smtp = smtplib.SMTP()
    smtp.connect(host, port)
    smtp.login(username, password)
    smtp.sendmail(sender, receivers, message.as_string())
