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

package com.welab.wefe.board.service.api.project.job.task;

import com.welab.wefe.board.service.component.Components;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.dto.entity.job.JobOutputModel;
import com.welab.wefe.board.service.dto.entity.job.TaskOutputView;
import com.welab.wefe.board.service.dto.entity.job.TaskResultOutputModel;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "flow/job/task/detail", name = "get task detail")
public class DetailApi extends AbstractApi<DetailApi.Input, DetailApi.Output> {

    @Autowired
    private TaskService taskService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        TaskMySqlModel task = taskService.findOne(input);
        if (task == null) {
            return success();
        }

        List<TaskResultOutputModel> results = null;

        if (input.needResult) {
            results = Components
                    .get(task.getTaskType())
                    .getTaskAllResult(task.getTaskId());

        }

        return success(
                new Output(
                        new TaskOutputView(
                                task,
                                results
                        )
                )
        );
    }

    public static class Output {
        private JobOutputModel job;
        private TaskOutputView taskView;

        public Output(TaskOutputView taskView) {
            this.taskView = taskView;
        }


        //region getter/setter

        public JobOutputModel getJob() {
            return job;
        }

        public void setJob(JobOutputModel job) {
            this.job = job;
        }

        public TaskOutputView getTaskView() {
            return taskView;
        }

        public void setTaskView(TaskOutputView taskView) {
            this.taskView = taskView;
        }

        //endregion
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "任务ID")
        private String jobId;

        @Check(name = "流程节点ID")
        private String flowNodeId;

        @Check(name = "子任务ID")
        private String taskId;

        @Check(name = "流程ID")
        private String flowId;

        @Check(name = "是否需要返回 task 执行结果", require = true, desc = "task 的执行结果体积较大，在不需要时，请指定为 false")
        private boolean needResult;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (StringUtil.isEmpty(taskId)) {
                if (StringUtil.isEmpty(jobId) && StringUtil.isEmpty(flowNodeId)) {
                    throw new StatusCodeWithException("请指定 taskId，或者指定 jobId 和 flowNodeId。", StatusCode.PARAMETER_VALUE_INVALID);
                }
            }
        }


        //region getter/setter


        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getFlowNodeId() {
            return flowNodeId;
        }

        public void setFlowNodeId(String flowNodeId) {
            this.flowNodeId = flowNodeId;
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
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
