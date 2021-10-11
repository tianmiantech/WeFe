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
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "project/flow/update/base_info", name = "update flow base info")
public class UpdateFlowBaseInfoApi extends AbstractNoneOutputApi<UpdateFlowBaseInfoApi.Input> {

    @Autowired
    ProjectFlowService projectFlowService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        projectFlowService.updateFlowBaseInfo(input);
        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "流程ID")
        private String flowId;

        @Check(name = "联邦类型（横向/纵向）", require = true)
        private FederatedLearningType federatedLearningType;

        @Check(name = "流程名", require = true)
        private String name;

        @Check(name = "流程描述")
        private String desc;

        //region getter/setter


        public FederatedLearningType getFederatedLearningType() {
            return federatedLearningType;
        }

        public void setFederatedLearningType(FederatedLearningType federatedLearningType) {
            this.federatedLearningType = federatedLearningType;
        }

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }


        //endregion
    }
}
