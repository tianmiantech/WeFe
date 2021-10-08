# Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import threading
import traceback

from common.python.common.consts import JobStatus
from common.python.db.db_models import *
from common.python.db.global_config_dao import GlobalConfigDao
from common.python.utils.log_utils import schedule_logger
from flow.utils.job_utils import mail


class JobErrorMailWarnScheduler(threading.Thread):
    """
    When the task status is abnormal, send a notification email to the job creator.
    """

    def __init__(self, job):
        threading.Thread.__init__(self)
        self.job = job

    def run(self):
        try:
            if self.job.status != JobStatus.ERROR_ON_RUNNING and self.job.status != JobStatus.TIMEOUT:
                return

            if self.job.my_role != 'promoter' or len(self.job.created_by) == 0:
                return

            # get mail server info
            mail_server = GlobalConfigDao.getMailServerConfig()
            if mail_server is None:
                return

            if len(mail_server.mail_host) == 0\
                    or mail_server.mail_port is None \
                    or mail_server.mail_port == 0 \
                    or len(mail_server.mail_username) == 0 \
                    or len(mail_server.mail_password) == 0:
                return

            # get email address
            account_list = Account.select().where(Account.id == self.job.created_by)
            if account_list.exists() is False:
                return
            account = account_list[0]
            if len(account.email) == 0:
                return

            # send email
            subject = '任务[' + self.job.name + ']失败'
            content = '任务失败，id:' + self.job.job_id + '，原因：' + self.job.message + '请登录系统查看'
            mail(host=mail_server.mail_host, port=mail_server.mail_port, username=mail_server.mail_username,
                 password=mail_server.mail_password, sender=mail_server.mail_username, receivers=account.email,
                 subject=subject, content=content)
            schedule_logger().info("发送任务失败通知邮件成功.")
        except Exception as e:
            traceback.print_exc()
            schedule_logger().exception("发送任务失败通知邮件异常:%s", e)
