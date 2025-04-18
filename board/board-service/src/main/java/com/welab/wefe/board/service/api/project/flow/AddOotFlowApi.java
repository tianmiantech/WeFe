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

import com.welab.wefe.board.service.service.ProjectFlowService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Add OOT process
 * <p>
 * Call this interface in [scoring verification] in the model list
 * </p>
 *
 * @author aaron.li
 **/
@Api(path = "project/flow/add_oot", name = "Add OOT process")
public class AddOotFlowApi extends AbstractApi<AddOotFlowApi.Input, AddOotFlowApi.Output> {
    @Autowired
    ProjectFlowService projectFlowService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        return success(projectFlowService.addOotFlow(input));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "The ID of the task being OOT", require = true)
        private String ootJobId;
        @Check(name = "ID of the model being OOT", require = true)
        private String ootModelFlowNodeId;
        @Check(name = "ID of the model name")
        private String ootModelName;

        public String getOotJobId() {
            return ootJobId;
        }

        public void setOotJobId(String ootJobId) {
            this.ootJobId = ootJobId;
        }

        public String getOotModelFlowNodeId() {
            return ootModelFlowNodeId;
        }

        public void setOotModelFlowNodeId(String ootModelFlowNodeId) {
            this.ootModelFlowNodeId = ootModelFlowNodeId;
        }

        public String getOotModelName() {
            return ootModelName;
        }

        public void setOotModelName(String ootModelName) {
            this.ootModelName = ootModelName;
        }
    }

    public static class Output {
        private String flowId;

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }
    }
}
