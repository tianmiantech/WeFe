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
import com.welab.wefe.serving.service.dto.ModelStatusOutput;
import com.welab.wefe.serving.service.enums.MemberModelStatusEnum;
import com.welab.wefe.serving.service.service.ModelMemberService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hunter.zhao
 * @date 2022/5/17
 */
@Api(path = "model/status/check", name = "检查模型状态")
public class ModelStatusCheckApi extends AbstractApi<ModelStatusCheckApi.Input, List<ModelStatusOutput>> {

    @Autowired
    ModelMemberService modelMemberService;

    @Override
    protected ApiResult<List<ModelStatusOutput>> handle(Input input) throws Exception {
        return success(
                modelMemberService.checkAvailableByModelIdAndMemberId(input.getServiceId(), input.getMemberId())
        );
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "模型id", require = true)
        private String serviceId;


        @Check(name = "成员ID")
        private String memberId;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }
    }
}
