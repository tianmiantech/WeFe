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

import threading
import time

from common.python.common.consts import JobStatus, TaskStatus
from common.python.db.job_dao import JobDao
from common.python.db.task_dao import TaskDao
from common.python.utils.log_utils import LoggerFactory
from flow.alert_service.job_error_mail_warn_scheduler import Job
from flow.service.board.board_service import BoardService
from flow.service.gateway.gateway_service import GatewayService
from flow.service.job_scheduler.job_service import JobService
from flow.service.job_scheduler.job_stop_action import JobStopAction
from flow.service.job_scheduler.job_success_action import JobSuccessAction
from flow.service.job_scheduler.job_wait_success_action import JobWaitSuccessAction


class JobGuard(threading.Thread):
    logger = LoggerFactory.get_logger("JobGuard")
    """
    The job guard observes the execution status of running tasks, and closes tasks that need to be closed.

    1.  Observe our gateway, if the gateway fails, shut yourself down.
    2. Observe our job status, all tasks are executed successfully, or some tasks fail, shut yourself down.
    3. Observe the status of the other party's job, 
       if you can't observe it, or observe that the job has stopped, shut yourself down.
    """

    def run(self):

        while True:
            job_list = JobDao.list_all_not_finished_job()
            if len(job_list) == 0:
                time.sleep(5)
                continue

            if not GatewayService.alive()[0]:
                for job in job_list:
                    JobStopAction(job.job_id, job.my_role) \
                        .do(JobStatus.ERROR_ON_RUNNING, "检测到 Gateway 服务状态异常，任务停止运行")

            for job in job_list:
                try:
                    self.observe(job)

                except Exception as e:
                    # When an exception is encountered, close the job.
                    message = "observe job error:" + repr(e)
                    self.logger.error(message)
                    self.logger.error(e)
                    JobStopAction(job.job_id, job.my_role).do(JobStatus.ERROR_ON_RUNNING, message)

            time.sleep(5)

    def observe(self, job):
        """
        Observe the job and close the tasks that need to be closed.
        """
        # Observe the status of the other party, if something goes wrong, stop.
        if self.observe_other_member_maybe_errored(job):
            return

        if job.status == JobStatus.RUNNING:
            self.observe_myself_running_job(job)
        elif job.status == JobStatus.WAIT_SUCCESS:
            JobSuccessAction(job.job_id, job.my_role).do("success")

    def observe_myself_running_job(self, job):
        """
        Observe the job whose status is running
        """
        task_list = TaskDao.list_by_job_id(job.job_id, job.my_role)

        # Observe whether the job is successful
        if self.assert_job_success(job, task_list):
            JobWaitSuccessAction(job.job_id, job.my_role).do("all task success!")

        # If the task is not running, no longer observe.
        if not JobService.job_is_running(job):
            return

        # Observe whether the job fails
        failed, message = self.assert_job_fail(job, task_list)
        if failed:
            JobStopAction(job.job_id, job.my_role) \
                .do(JobStatus.ERROR_ON_RUNNING, message)

        if not JobService.job_is_running(job):
            return

        # Neither succeeded nor failed, indicating that it is still running, update the progress.
        JobService.update_progress(job)

    def assert_job_success(self, job: Job, task_list) -> bool:
        """
        Observe whether the job is successful

        Condition: All tasks status is success

        Parameters
        ----------
        job: Job
        task_list: List[Task]

        Returns
        -------
        assert result of job success
        """
        for task in task_list:
            if task.status != TaskStatus.SUCCESS:
                return False

        return True

    def assert_job_fail(self, job: Job, task_list) -> (bool, str):
        """
        Observe whether the job has failed
        Condition: task_list contains error or timeout task

        Parameters
        ----------
        job: Job
        task_list: List[Task]

        Returns
        -------
        assert result of job fail
        """
        for task in task_list:
            if task.status == TaskStatus.ERROR or task.status == TaskStatus.TIMEOUT:
                return True, task.message

        return False, None

    def observe_other_member_maybe_errored(self, job) -> bool:
        """
        Observe the task status of other members, and stop the task when other members have failed.

        Parameters
        ----------
        job: Job

        Returns
        -------
        Whether an error occurred
        """
        progress_list = BoardService.get_job_progress(job.job_id)

        if not progress_list:
            return False

        # Check result: Do you want to stop
        need_stop = False
        # Check result: Reason for stopping
        stop_cause_message = ""

        # Start checking
        for progress in progress_list:
            if progress.get_progress_success is False:
                need_stop = True
                stop_cause_message = "检测到成员 {} 获取不到任务进度，停止任务".format(
                    progress.member_name
                )

            # The job status of one party is ERROR_ON_RUNNING/STOP_ON_RUNNING,
            # indicating that someone else has stopped by an error, so stop yourself.
            elif progress.job_status == JobStatus.ERROR_ON_RUNNING or progress.job_status == JobStatus.STOP_ON_RUNNING:
                need_stop = True
                stop_cause_message = "检测到成员 {} 任务状态为{}，我方跟随其停止任务".format(
                    progress.member_name, progress.job_status
                )

            if need_stop:
                break

        if need_stop:
            JobStopAction(job.job_id, job.my_role) \
                .do(JobStatus.ERROR_ON_RUNNING, stop_cause_message)

        return need_stop


if __name__ == '__main__':
    JobGuard().start()
