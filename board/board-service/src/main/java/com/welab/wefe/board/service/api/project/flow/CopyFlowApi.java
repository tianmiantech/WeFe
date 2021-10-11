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

import com.welab.wefe.board.service.service.ProjectFlowService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lonnie.ren
 */
@Api(path = "project/flow/copy", name = "copy flow")
public class CopyFlowApi extends AbstractNoneOutputApi<CopyFlowApi.Input> {

    @Autowired
    private ProjectFlowService projectFlowService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        projectFlowService.copy(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "被复制的流程Id", require = true)
        private String sourceFlowId;

        @Check(name = "目标项目Id", require = true)
        private String targetProjectId;

        @Check(name = "给复制的流程重命名")
        private String flowRename;

        @Check(name = "新的流程id")
        private String newFlowId;
        //region getter/setter

        public String getSourceFlowId() {
            return sourceFlowId;
        }

        public void setSourceFlowId(String sourceFlowId) {
            this.sourceFlowId = sourceFlowId;
        }

        public String getTargetProjectId() {
            return targetProjectId;
        }

        public void setTargetProjectId(String targetProjectId) {
            this.targetProjectId = targetProjectId;
        }

        public String getFlowRename() {
            return flowRename;
        }

        public void setFlowRename(String flowRename) {
            this.flowRename = flowRename;
        }

        public String getNewFlowId() {
            return newFlowId;
        }

        public void setNewFlowId(String newFlowId) {
            this.newFlowId = newFlowId;
        }

        //endregion

    }

}
