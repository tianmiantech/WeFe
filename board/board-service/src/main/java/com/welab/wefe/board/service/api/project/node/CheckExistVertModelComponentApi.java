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
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * Check whether there are vert model components in the process
 * <p>
 * Only when the vert modeling is used, the evaluation component and the oot component need to calculate the psi service
 * <p>
 * PS：OOT mode: click scoring verification in the model list to enter the canvas scene, that is,
 * OOT mode (there are only two components: start and scoring verification).
 * </p>
 *
 * @author aaron.li
 **/
@Api(path = "project/flow/node/check_exist_vert_model_component", name = "Check whether there are vert model components in the process")
public class CheckExistVertModelComponentApi extends AbstractApi<CheckExistVertModelComponentApi.Input, CheckExistVertModelComponentApi.Output> {
    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        Output out = new Output();
        out.setCheckResult(projectFlowNodeService.checkExistSpecificComponent(input, Arrays.asList(ComponentType.VertLR, ComponentType.VertSecureBoost)));
        return success(out);
    }

    public static class Input extends CheckExistEvaluationComponentApi.Input {
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
