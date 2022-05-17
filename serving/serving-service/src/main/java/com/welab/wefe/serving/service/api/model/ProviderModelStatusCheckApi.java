/*
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
package com.welab.wefe.serving.service.api.model;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.MemberModelStatusEnum;
import com.welab.wefe.serving.service.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 * @date 2022/5/17
 */
@Api(
        path = "model/provider/status/check",
        name = "检查模型状态（协作方提供接口）",
        login = false
//        ,
//        rsaVerify = true
)
public class ProviderModelStatusCheckApi extends AbstractApi<ProviderModelStatusCheckApi.Input, ProviderModelStatusCheckApi.Output> {

    @Autowired
    ModelService modelService;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        return success(modelService.checkAvailable(input.getModelId()));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "模型id", require = true)
        private String modelId;

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }
    }


    public static class Output extends AbstractApiOutput {

        @Check(name = "modelId")
        private String modelId;

        @Check(name = "status")
        private MemberModelStatusEnum status;

        public static Output of(String modelId, MemberModelStatusEnum status) {
            Output output = new Output();
            output.modelId = modelId;
            output.status = status;
            return output;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public MemberModelStatusEnum getStatus() {
            return status;
        }

        public void setStatus(MemberModelStatusEnum status) {
            this.status = status;
        }
    }
}
