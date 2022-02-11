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

import com.welab.wefe.board.service.service.ProjectFlowNodeService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Check whether there are evaluation components in the process
 * <p>
 * Scenario: when the [scoring verification] component is dragged out in non OOT mode, if there is no [model evaluation] component before it,
 * the input parameters of the [scoring verification] component must include the input parameters of [model evaluation], otherwise it is not used; In the OOT mode, if the original process does not have a [model evaluation] component, the input parameters of the [scoring verification] component should include the input parameters of [model evaluation], otherwise it is not used. Since it is difficult for the front end to judge whether the [model evaluation] component exists in the front node of the [scoring verification] component,
 * it can only be placed on the back end, so this interface is generated
 * <p>
 * PS：OOT mode: click scoring verification in the model list to enter the canvas scene, that is,
 * OOT mode (there are only two components: start and scoring verification).
 * </p>
 *
 * @author aaron.li
 **/
@Api(path = "project/flow/node/check_exist_evaluation_component", name = "Check whether there are evaluation components in the process")
public class CheckExistEvaluationComponentApi extends AbstractApi<CheckExistEvaluationComponentApi.Input, CheckExistEvaluationComponentApi.Output> {
    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;


    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        Output out = new Output();
        out.setCheckResult(projectFlowNodeService.checkExistEvaluationComponent(input));
        return success(out);
    }

    public static class Input extends AbstractApiInput {
        @Check(desc = "This parameter is used in non OOT mode")
        private String flowId;

        @Check(desc = "The OOT component ID on the canvas (mainly used to find the front node and the OOT node on the canvas. This parameter is used in non OOT mode)")
        private String nodeId;
        @Check(desc = "Original model job ID (this parameter is used in OOT mode)")
        private String jobId;
        @Check(desc = "Original model node ID (this parameter is used in OOT mode)")
        private String modelNodeId;

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

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getModelNodeId() {
            return modelNodeId;
        }

        public void setModelNodeId(String modelNodeId) {
            this.modelNodeId = modelNodeId;
        }
    }

    public static class Output extends AbstractApiOutput {
        @Check(name = "check result")
        private boolean checkResult;

        public boolean isCheckResult() {
            return checkResult;
        }

        public void setCheckResult(boolean checkResult) {
            this.checkResult = checkResult;
        }
    }
}
