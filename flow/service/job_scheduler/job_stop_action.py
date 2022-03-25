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

import errno
import os

import psutil

from common.python.common.consts import JobStatus
from common.python.db.db_models import Task, Job
from common.python.db.flow_dao import ProjectFlowDao
from common.python.db.job_dao import JobDao
from common.python.db.task_dao import TaskDao
from common.python.db.task_progress_dao import TaskProgressDao
from common.python.utils.log_utils import schedule_logger
from flow.alert_service.job_error_mail_warn_scheduler import JobErrorMailWarnScheduler
from flow.service.board.board_service import BoardService
from flow.service.job_scheduler.job_service import JobService
from flow.settings import MemberInfo
from flow.service.visualfl.visualfl_service import VisualFLService
from common.python.db.db_models import GlobalSetting

class JobStopAction:
    job: Job

    def __init__(self, job_id, my_role) -> None:
        super().__init__()
        self.job = JobDao.find_one_by_job_id(job_id, my_role)

    def do(self, job_status, job_type, message):

        # If the task has stopped, jump out.
        if JobStatus.is_finished(self.job.status):
            return

        self.log_job_info("start stop job")
        if 'visualfl' == job_type:
            params = {
                'job_id': self.job.job_id
            }
            VisualFLService.request('stop', params)
        tasks = TaskDao.list_by_job(self.job)
        for task in tasks:
            kill_success = False
            try:
                # kill process
                kill_success = self.kill_task(task)
                # update task status
                TaskDao.stop(task, message)
                TaskProgressDao.after_close_task(task)
            except Exception as e:
                self.log_job_info(repr(e), exception=e)
            finally:
                self.log_job_info(
                    "component {} process {} kill {}".format(
                        task.name,
                        task.pid,
                        "success" if kill_success else "failed"
                    )
                )

        JobDao.stop(self.job, job_status, message)
        # update job progress
        JobService.update_progress(self.job)
        # update flow status
        ProjectFlowDao.update_status(self.job.flow_id, job_status)
        # notice board service
        BoardService.on_job_finished(self.job.job_id)
        # Email when task failed
        JobErrorMailWarnScheduler(self.job).start()

    def kill_task(self, task: Task):
        """"
        Close the process corresponding to the task
        """
        try:
            pid = task.pid
            if not pid:
                return False

            self.log_job_info("try to kill task {}({}) process pid:{}".format(task.name, task.task_id, pid))

            if not self.kill_process(pid):
                return True

            p = psutil.Process(pid)
            for child in p.children(recursive=True):
                if self.kill_process(child.pid):
                    child.kill()

            if self.kill_process(p.pid):
                p.kill()

            return True
        except Exception as e:
            raise e

    def kill_process(self, pid):
        """
        kill a process

        Parameters
        ----------
        pid: int
            The id of process

        """
        if pid < 0:
            return False
        if pid == 0:
            raise ValueError('invalid PID 0')
        try:
            os.kill(pid, 0)
        except OSError as err:
            if err.errno == errno.ESRCH:
                # ESRCH == No such process
                return False
            elif err.errno == errno.EPERM:
                # EPERM clearly means there's a process to deny access to
                return True
            else:
                # According to "man 2 kill" possible error values are
                # (EINVAL, EPERM, ESRCH)
                raise
        else:
            return True

    def log_job_info(self, message, exception=None):
        running_job = self.job.job_id + '_' + self.job.my_role
        logger = schedule_logger(running_job)
        message = '{} {} on kill job {} |{}'.format(self.job.my_role, MemberInfo.MEMBER_ID, self.job.job_id, message)
        if exception:
            logger.exception(message, exception)
        else:
            logger.info(message)


if __name__ == '__main__':
    JobStopAction("418387743a3a481597b4ba79bf6f68b1", "promoter").do(JobStatus.ERROR_ON_RUNNING, "zane close")
