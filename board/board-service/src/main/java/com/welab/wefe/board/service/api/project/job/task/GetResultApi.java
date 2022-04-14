/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author zane.luo
 */
@Api(path = "flow/job/task/result", name = "get task result", desc = "Use taskId or flowId + nodeId to get the node execution result.")
public class GetResultApi extends AbstractApi<GetResultApi.Input, List<TaskResultOutputModel>> {

    @Autowired
    private TaskService taskService;

    @Override
    protected ApiResult<List<TaskResultOutputModel>> handle(Input input) throws StatusCodeWithException {

        List<TaskMySqlModel> tasks;
        if (StringUtil.isNotEmpty(input.getTaskId())) {
            TaskMySqlModel task = taskService.findOne(input.getTaskId());
            if (task == null) {
                StatusCode
                        .PARAMETER_VALUE_INVALID
                        .throwException("错误的 task id：" + input.getTaskId());
            }
            tasks = Arrays.asList(task);
        } else {
            tasks = taskService.findAll(input);
        }


        if (tasks == null || tasks.isEmpty()) {
            return success();
        }
        List<TaskResultOutputModel> results = new ArrayList<>();
        Set<String> temp = new HashSet<>();
        for (TaskMySqlModel task : tasks) {
            String taskConf = task.getTaskConf();
            JObject taskConfigJson = JObject.create(taskConf);
            TaskResultOutputModel result = Components.get(task.getTaskType()).getTaskResult(task.getTaskId(),
                    input.type);
            if (result == null) {
                result = new TaskResultOutputModel();
            }
            // put task info to TaskResultOutputModel
            result.setStatus(task.getStatus());
            result.setStartTime(task.getStartTime());
            result.setFinishTime(task.getFinishTime());
            result.setMessage(task.getMessage());
            result.setErrorCause(task.getErrorCause());
            result.setPosition(task.getPosition());
            result.setSpend(task.getSpend());
            JObject taskInfo = taskConfigJson.getJObject("task");
            if (taskInfo != null) {
                result.setMembers(taskConfigJson.getJObject("task").getJSONList("members"));
            }

            if (result.getResult() != null && !temp.add(result.getResult().toJSONString()) && task.getRole() == JobMemberRole.provider
                    && (task.getTaskType() == ComponentType.MixStatistic
                    || task.getTaskType() == ComponentType.MixBinning
                    || task.getTaskType() == ComponentType.FillMissingValue
                    || task.getTaskType() == ComponentType.MixLR)) {
                continue;
            }
            results.add(result);
        }

        return success(results);
    }

    public static class Input extends DetailApi.Input {

        @Check(name = "结果类型")
        private String type;

        // region getter/setter

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        // endregion
    }

}
