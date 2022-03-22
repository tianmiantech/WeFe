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

package com.welab.wefe.board.service.api.project.job;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.job.JobListOutputModel;
import com.welab.wefe.board.service.service.FlowJobService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "flow/job/query", name = "Query flow execution record")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<JobListOutputModel>> {


    @Autowired
    FlowJobService flowJobService;

    @Override
    protected ApiResult<PagingOutput<JobListOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(flowJobService.query(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "流程ID", require = true)
        private String flowId;

        @Check(name = "任务ID")
        private String jobId;

        @Check(name = "任务名称")
        private String name;

        @Check(name = "任务状态")
        private JobStatus status;

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public JobStatus getStatus() {
            return status;
        }

        public void setStatus(JobStatus status) {
            this.status = status;
        }


        //endregion
    }

}
