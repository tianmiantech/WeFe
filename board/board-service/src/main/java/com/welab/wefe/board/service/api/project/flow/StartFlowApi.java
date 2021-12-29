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

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.service.ProjectFlowJobService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "flow/start", name = "start flow, create a job and execute it.")
public class StartFlowApi extends AbstractApi<StartFlowApi.Input, StartFlowApi.Output> {

    @Autowired
    ProjectFlowJobService projectFlowJobService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        String jobId = projectFlowJobService.startFlow(input);
        return success(new Output(jobId));
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "流程id", require = true)
        private String flowId;
        @Check(name = "是否使用缓存", require = true)
        private boolean useCache;
        @Check(name = "终止节点", desc = "为空时表示执行全流程")
        private String endNodeId;

        @Check(name = "jobId", donotShow = true)
        private String jobId;

        @Check(name = "arbiterMemberId", desc = "arbiter成员id")
        private String arbiterMemberId;

        private String ootJobId;

        //region getter/setter

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public boolean isUseCache() {
            return useCache;
        }

        public void setUseCache(boolean useCache) {
            this.useCache = useCache;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getEndNodeId() {
            return endNodeId;
        }

        public void setEndNodeId(String endNodeId) {
            this.endNodeId = endNodeId;
        }

        public String getArbiterMemberId() {
            return arbiterMemberId;
        }

        public void setArbiterMemberId(String arbiterMemberId) {
            this.arbiterMemberId = arbiterMemberId;
        }

        public String getOotJobId() {
            return ootJobId;
        }

        public void setOotJobId(String ootJobId) {
            this.ootJobId = ootJobId;
        }
        //endregion
    }

    public static class Output {
        private String jobId;

        public Output(String jobId) {
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }
    }
}
