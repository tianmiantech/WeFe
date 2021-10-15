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

import sys
import time

from werkzeug import run_simple

from common.python.common.consts import JobStatus
from common.python.db.db_models import DB, Job
from common.python.utils.log_utils import LoggerFactory, schedule_logger
from flow.cycle_actions.flow_action_queue.flow_action_queue_consumer import FlowActionQueueConsumer
from flow.cycle_actions.guard.job_guard import JobGuard
from flow.service.gateway.gateway_service import GatewayService
from flow.service.job_scheduler.clear_job_middle_data_scheduler import ClearJobMiddleDataScheduler
from flow.service.job_scheduler.job_stop_action import JobStopAction
from flow.web.app import app
from flow.web.util.const import ServiceMeta


class AppLauncher:
    """
    Application start entry class
    """
    logger = LoggerFactory.get_logger("AppLauncher")

    @staticmethod
    def start_app():
        AppLauncher.logger.info("AppLauncher start ")

        # Update gateway IP whitelist
        AppLauncher.logger.info("repost ip to white list")
        GatewayService.report_ip_to_white_list()
        AppLauncher.logger.info("---done---")

        # When flow restarts, close the previously unfinished job.
        AppLauncher.close_not_finished_job_when_flow_restart()

        # Listening flow_action_queue task queue
        AppLauncher.logger.info("flow queue listener")
        FlowActionQueueConsumer().start()
        AppLauncher.logger.info("---done---")

        # Observe the unfinished job
        AppLauncher.logger.info("start job guard")
        JobGuard().start()
        AppLauncher.logger.info("---done---")

        # Clean up intermediate data
        AppLauncher.logger.info("clean job middle data scheduler")
        ClearJobMiddleDataScheduler().start()
        AppLauncher.logger.info("---done---")

        # start web service
        AppLauncher.logger.info("start web server")
        run_simple(
            ServiceMeta.HOST,
            int(ServiceMeta.PORT),
            app,
            threaded=True
        )
        try:
            while True:
                time.sleep(60 * 60 * 24)
        except KeyboardInterrupt:
            sys.exit(0)

    @staticmethod
    def close_not_finished_job_when_flow_restart():
        """
        When flow restarts, close the previously unfinished job.
        """
        schedule_logger().info('--- 正在关闭未 finished 的 job ---')

        with DB.connection_context():
            jobs = Job.select().where(
                (
                        Job.status == JobStatus.WAIT_RUN
                ) | (
                        Job.status == JobStatus.WAIT_STOP
                ) | (
                        Job.status == JobStatus.RUNNING
                )
            )

            if len(jobs) == 0:
                schedule_logger().info('--- 未发现未 finished 的 job ---')
                return

            for job in jobs:
                schedule_logger().info('--- 未 finished 的 job 已被关闭:{},{}'.format(job.job_id, job.name))
                JobStopAction(job.job_id, job.my_role) \
                    .do(JobStatus.ERROR_ON_RUNNING, "由于 flow 服务重启导致该任务被中断，请重新启动任务")


if __name__ == '__main__':
    AppLauncher.start_app()
