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

package com.welab.wefe.board.service.api.project.modeling;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.modeling_config.ModelingInfoOutputModel;
import com.welab.wefe.board.service.service.ProjectFlowService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "project/modeling/query", name = "query modeling by pagination")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<ModelingInfoOutputModel>> {

    @Autowired
    private ProjectFlowService projectFlowService;

    @Override
    protected ApiResult<PagingOutput<ModelingInfoOutputModel>> handle(Input input) throws StatusCodeWithException {

        return success(projectFlowService.queryModelingInfo(input));
    }

    public static class Input extends PagingInput {

        public Input() {
        }

        public Input(String projectId) {
            this.projectId = projectId;
        }

        @Check(name = "项目id", require = true)
        private String projectId;

        @Check(name = "任务id")
        private String jobId;

        @Check(name = "流程id")
        private String flowId;

        @Check(name = "组件类型")
        private ComponentType componentType;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public ComponentType getComponentType() {
            return componentType;
        }

        public void setComponentType(ComponentType componentType) {
            this.componentType = componentType;
        }
    }
}
