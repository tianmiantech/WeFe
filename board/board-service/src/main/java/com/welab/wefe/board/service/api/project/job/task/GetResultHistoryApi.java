/**
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
import com.welab.wefe.board.service.dto.entity.job.TaskResultOutputModel;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "flow/job/task/result_history", name = "Get all historical execution results of task")
public class GetResultHistoryApi extends AbstractApi<GetResultHistoryApi.Input, GetResultHistoryApi.Output> {

    @Autowired
    private TaskService taskService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        List<TaskMySqlModel> tasks = taskService.findTaskHistory(input.flowId, input.flowNodeId, input.role);

        List<TaskResultOutputModel> list = new ArrayList<>();

        for (TaskMySqlModel task : tasks) {
            TaskResultOutputModel result = Components
                    .get(task.getTaskType())
                    .getTaskResult(task.getTaskId(), input.type);

            if (result == null) {
                result = ModelMapper.map(task, TaskResultOutputModel.class);
                result.setComponentType(task.getTaskType());
            }

            // put task info to TaskResultOutputModel
            result.setJobId(task.getJobId());
            result.setStatus(task.getStatus());
            result.setStartTime(task.getStartTime());
            result.setFinishTime(task.getFinishTime());
            result.setMessage(task.getMessage());
            result.setErrorCause(task.getErrorCause());
            result.setPosition(task.getPosition());
            result.setSpend(task.getSpend());

            list.add(result);
        }

        return success(new Output(list));
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "流程Id", require = true)
        private String flowId;
        @Check(name = "节点Id", require = true)
        private String flowNodeId;
        @Check(name = "角色", require = true)
        private JobMemberRole role;
        @Check(name = "结果类型")
        private String type;

        //region getter/setter


        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public String getFlowNodeId() {
            return flowNodeId;
        }

        public void setFlowNodeId(String flowNodeId) {
            this.flowNodeId = flowNodeId;
        }

        public JobMemberRole getRole() {
            return role;
        }

        public void setRole(JobMemberRole role) {
            this.role = role;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        //endregion
    }

    public static class Output {
        public List<TaskResultOutputModel> list;

        public Output(List<TaskResultOutputModel> list) {
            this.list = list;
        }
    }
}
