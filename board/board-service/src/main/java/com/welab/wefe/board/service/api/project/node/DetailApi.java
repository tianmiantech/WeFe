/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.project.node;

import com.welab.wefe.board.service.component.deep_learning.ImageDataIOComponent;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowNodeMySqlModel;
import com.welab.wefe.board.service.dto.entity.job.ProjectFlowNodeOutputModel;
import com.welab.wefe.board.service.service.ProjectFlowNodeService;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "project/flow/node/detail", name = "get node detail")
public class DetailApi extends AbstractApi<DetailApi.Input, ProjectFlowNodeOutputModel> {

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;

    @Override
    protected ApiResult<ProjectFlowNodeOutputModel> handle(Input input) throws StatusCodeWithException {
        ProjectFlowNodeMySqlModel one = projectFlowNodeService.findOne(input.flowId, input.nodeId);

        if (one == null) {
            return success();
        }

        ProjectFlowNodeOutputModel output = ModelMapper.map(one, ProjectFlowNodeOutputModel.class);
        output.setParams(one.getParams());

        // ImageDataIO 节点顺带输出数据集信息。
        if (one.getComponentType() == ComponentType.ImageDataIO) {
            if (output.getParams() != null) {
                ImageDataIOComponent.Params params = output.getParams().toJavaObject(ImageDataIOComponent.Params.class);
                params.fillDataSetDetail();
                output.setParams(JObject.create(params));
            }
        }

        return success(output);
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "流程id", require = true)
        private String flowId;

        @Check(name = "节点id", require = true)
        private String nodeId;

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

    }
}
