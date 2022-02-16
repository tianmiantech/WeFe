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

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Query input parameters of dataio component
 *
 * @author aaron.li
 **/
@Api(path = "project/flow/query/data_io_task_config", name = "Query input parameters of dataio component")
public class QueryDataIoTaskConfigApi extends AbstractApi<QueryDataIoTaskConfigApi.Input, JObject> {
    @Autowired
    private TaskService taskService;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException, IOException {
        return success(taskService.findDataIoTaskConfig(input));
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "job id", require = true)
        private String jobId;
        @Check(name = "role", require = true)
        private JobMemberRole role;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public JobMemberRole getRole() {
            return role;
        }

        public void setRole(JobMemberRole role) {
            this.role = role;
        }
    }
}
