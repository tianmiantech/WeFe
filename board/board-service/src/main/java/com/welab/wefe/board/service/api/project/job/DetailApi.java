/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.project.job;

import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowNodeMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.dto.entity.job.JobOutputModel;
import com.welab.wefe.board.service.dto.entity.job.TaskOutputView;
import com.welab.wefe.board.service.dto.entity.job.TaskResultOutputModel;
import com.welab.wefe.board.service.service.FlowJobService;
import com.welab.wefe.board.service.service.ProjectFlowNodeService;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Api(path = "flow/job/detail", name = "get job detail")
public class DetailApi extends AbstractApi<DetailApi.Input, DetailApi.Output> {

    @Autowired
    private FlowJobService flowJobService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        JobOutputModel job = flowJobService.detail(input.flowId, input.jobId, input.memberRole);
        if (job == null) {
            return success();
        }

        List<TaskMySqlModel> tasks = taskService.listByJobId(job.getJobId(), job.getMyRole());
        List<ProjectFlowNodeMySqlModel> nodes = projectFlowNodeService.findNodesByFlowId(job.getFlowId());

        List<TaskOutputView> output = tasks
                .parallelStream()
                .map(task -> {

                    if (input.needResult && task != null) {

                        List<TaskResultOutputModel> results = Components
                                .get(task.getTaskType())
                                .getTaskAllResult(task.getTaskId());

                        return new TaskOutputView(task, results);
                    } else {
                        return new TaskOutputView(task);
                    }


                })
                .collect(Collectors.toList());

        return success(new Output(job, output));
    }

    public static class Output {
        private JobOutputModel job;
        private List<TaskOutputView> taskViews;

        public Output(JobOutputModel job, List<TaskOutputView> taskViews) {
            this.job = job;
            this.taskViews = taskViews;
        }


        //region getter/setter

        public JobOutputModel getJob() {
            return job;
        }

        public void setJob(JobOutputModel job) {
            this.job = job;
        }

        public List<TaskOutputView> getTaskViews() {
            return taskViews;
        }

        public void setTaskViews(List<TaskOutputView> taskViews) {
            this.taskViews = taskViews;
        }


        //endregion
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "流程ID")
        private String flowId;

        @Check(name = "任务ID", desc = "允许为空，为空时自动获取最后一个 job。")
        private String jobId;

        @Check(name = "角色")
        private JobMemberRole memberRole;

        @Check(
                name = "是否需要返回 task 执行结果",
                require = true,
                desc = "task 执行结果可能会体积较大，仅在需要一次性展示所有 task 执行结果时给 true。"
        )
        private boolean needResult;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (StringUtil.isEmpty(flowId) && StringUtil.isEmpty(jobId)) {
                throw new StatusCodeWithException("flowId 和 jobId 不能同时为空", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (StringUtil.isNotEmpty(jobId) && memberRole == null) {
                throw new StatusCodeWithException("指定 jobId 时需要指定 memberRole", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }

        //region getter/setter


        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }

        public boolean isNeedResult() {
            return needResult;
        }

        public void setNeedResult(boolean needResult) {
            this.needResult = needResult;
        }

        //endregion
    }

}
