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

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.dto.entity.job.ProjectFlowNodeOutputModel;
import com.welab.wefe.board.service.service.ProjectFlowService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * @author aaron.li
 **/
@Api(path = "project/flow_node/query", name = "query flow node list by flow id", login = false)
public class QueryFlowNodeListApi extends AbstractApi<QueryFlowNodeListApi.Input, QueryFlowNodeListApi.Output> {

    @Autowired
    private ProjectFlowService projectFlowService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        List<ProjectFlowNodeOutputModel> projectFlowNodeOutputModelList = projectFlowService.getFlowNodes(input.flowId);
        Output output = new Output();
        output.setList(projectFlowNodeOutputModelList);
        return success(output);
    }

    public static class Input extends AbstractApiInput {
        public Input(String flowId) {
            this.flowId = flowId;
        }

        @Check(name = "flow id", require = true)
        private String flowId;

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

    }


    public static class Output extends AbstractApiOutput {

        private List<ProjectFlowNodeOutputModel> list;

        public List<ProjectFlowNodeOutputModel> getList() {
            return list;
        }

        public void setList(List<ProjectFlowNodeOutputModel> list) {
            this.list = list;
        }
    }
}
