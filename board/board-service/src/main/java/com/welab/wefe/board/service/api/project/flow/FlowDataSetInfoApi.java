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

import com.welab.wefe.board.service.dto.kernel.JobDataSet;
import com.welab.wefe.board.service.service.ProjectFlowNodeService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
/**
 * @author zane.luo
 */
@Api(path = "flow/dataset/info", name = "Get information about the flow data set")
public class FlowDataSetInfoApi
        extends AbstractApi<FlowDataSetInfoApi.Input, FlowDataSetInfoApi.Output> {

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        return success(new Output(projectFlowNodeService.findFlowDataSetInfo(input.getFlowId())));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "流程id", require = true)
        private String flowId;

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

    }

    public static class Output {
        private List<JobDataSet> flowDataSetFeatures;

        public Output(List<JobDataSet> findFlowDataSetFeature) {
            this.flowDataSetFeatures = findFlowDataSetFeature;
        }

        public List<JobDataSet> getFlowDataSetFeatures() {
            return flowDataSetFeatures;
        }

        public void setFlowDataSetFeatures(List<JobDataSet> flowDataSetFeatures) {
            this.flowDataSetFeatures = flowDataSetFeatures;
        }

    }

}
