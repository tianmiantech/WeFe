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

package com.welab.wefe.board.service.api.project.node;

import com.welab.wefe.board.service.dto.entity.job.ProjectFlowNodeOutputModel;
import com.welab.wefe.board.service.service.ProjectFlowNodeService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "project/flow/node/update", name = "update node info")
public class UpdateApi extends AbstractApi<UpdateApi.Input, UpdateApi.Output> {

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        return success(new Output(projectFlowNodeService.updateFlowNode(input)));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "流程id", require = true)
        private String flowId;

        @Check(name = "节点ID", require = true)
        private String nodeId;

        @Check(name = "组件类型", require = true)
        private ComponentType componentType;

        @Check(name = "组件参数", require = true)
        private String params;


        //region getter/setter


        public ComponentType getComponentType() {
            return componentType;
        }

        public void setComponentType(ComponentType componentType) {
            this.componentType = componentType;
        }

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getParams() {
            return params;
        }

        public void setParams(String params) {
            this.params = params;
        }


        //endregion
    }

    public static class Output {

        public Output(List<ProjectFlowNodeOutputModel> paramsIsNullFlowNodes) {
            this.paramsIsNullFlowNodes = paramsIsNullFlowNodes;
        }

        private List<ProjectFlowNodeOutputModel> paramsIsNullFlowNodes;

        public List<ProjectFlowNodeOutputModel> getParamsIsNullFlowNodes() {
            return paramsIsNullFlowNodes;
        }

        public void setParamsIsNullFlowNodes(List<ProjectFlowNodeOutputModel> paramsIsNullFlowNodes) {
            this.paramsIsNullFlowNodes = paramsIsNullFlowNodes;
        }
    }


}
