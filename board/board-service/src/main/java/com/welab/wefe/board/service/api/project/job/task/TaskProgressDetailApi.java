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

import com.welab.wefe.board.service.database.entity.job.TaskProgressMysqlModel;
import com.welab.wefe.board.service.dto.entity.job.TaskProgressOuputModel;
import com.welab.wefe.board.service.service.TaskProgressService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lonnie
 */
@Api(path = "task/progress/detail", name = "task progress details")
public class TaskProgressDetailApi extends AbstractApi<TaskProgressDetailApi.Input, TaskProgressOuputModel> {

    @Autowired
    private TaskProgressService taskProgressService;

    @Override
    protected ApiResult<TaskProgressOuputModel> handle(Input input) throws StatusCodeWithException {

        TaskProgressMysqlModel taskProgress = taskProgressService.findOne(input.getTaskId(), input.getMemberRole());
        TaskProgressOuputModel output = ModelMapper.map(taskProgress, TaskProgressOuputModel.class);
        return success(output);
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "任务id", require = true)
        private String taskId;

        @Check(name = "角色", require = true)
        private JobMemberRole memberRole;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }
    }
}
