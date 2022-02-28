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
import json
import threading
import time
import traceback

from common.python import session, RuntimeInstance, Backend, WorkMode
from common.python.common.consts import NAMESPACE, JobStatus
from common.python.db.db_models import *
from common.python.db.task_dao import TaskDao
from common.python.utils.log_utils import schedule_logger
from common.python.utils.store_type import DBTypes


class ClearJobMiddleDataScheduler(threading.Thread):
    """
    Regularly clean up intermediate data of completed job
    """

    def get_need_clear_job(self):
        with DB.connection_context():
            job_list = Job.select().where(
                Job.status == JobStatus.SUCCESS,
                Job.job_middle_data_is_clear == 0
            ).limit(1).execute()
            if job_list:
                return job_list[0]
            return None

    def run(self):
        schedule_logger().info('start clear job middle data')
        while True:
            try:
                job = self.get_need_clear_job()
                if job is None:
                    time.sleep(5)
                    continue

            except Exception as e:
                traceback.print_exc()
                schedule_logger().exception("获取未清理中间数据的job异常：%s", e)
                time.sleep(5)
                continue

            try:

                ClearJobMiddleDataScheduler.clean_job_middle_data(job)

            except Exception as e:
                # 打印异常信息
                traceback.print_exc()
                schedule_logger().exception("执行清理任务中间数据异常：%s", e)

        schedule_logger().info('end clear job middle data')

    @staticmethod
    def clean_job_middle_data(job, reset_is_clear=True):
        try:
            with DB.connection_context():
                task = TaskDao.get(Task.job_id == job.job_id)
                if not task:
                    return

            if task.task_type == 'PaddleClassify' or task.task_type == 'PaddleDetection':
                schedule_logger().info("not need clean_job_middle_data task_id=:%s", task.task_id)
                return
            # if backend is FC, unnecessary.
            backend = Backend.get_by_task_config(json.loads(task.task_conf))
            if backend.is_fc():
                return
            db_type = DBTypes.CLICKHOUSE
            RuntimeInstance.SESSION = None

            session.init(job_id=job.job_id, mode=WorkMode.CLUSTER,
                         backend=backend, db_type=db_type)
            clean_name_pattern = f"{session.get_session_id()}*"
            schedule_logger().info("clean_name_pattern:%s", clean_name_pattern)
            session.cleanup(clean_name_pattern, NAMESPACE.PROCESS)
            schedule_logger().info('jobId:%s 清理完成！', job.job_id)
        except Exception as e:
            traceback.print_exc()
            schedule_logger().exception("jobId:%s 清理中间数据异常：%s", job.job_id, e)
        finally:
            # update job
            if reset_is_clear:
                with DB.connection_context():
                    job.job_middle_data_is_clear = True
                    job.save()
