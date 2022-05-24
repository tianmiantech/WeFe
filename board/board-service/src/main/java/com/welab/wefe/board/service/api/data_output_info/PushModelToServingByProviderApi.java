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
package com.welab.wefe.board.service.api.data_output_info;

import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.service.ServingService;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 * @date 2022/5/6
 */
@Api(
        path = "data_output_info/provider/push_model_to_serving",
        name = "push model to serving service",
        login = false,
        rsaVerify = true
)
public class PushModelToServingByProviderApi extends AbstractNoneOutputApi<PushModelToServingByProviderApi.Input> {

    @Autowired
    private ServingService servingService;

    @Autowired
    private TaskResultService taskResultService;


    @Override
    protected ApiResult handler(PushModelToServingByProviderApi.Input input) throws StatusCodeWithException {
        servingService.syncModelToServing(extractTaskId(input), input.getRole());
        return success();
    }

    private String extractTaskId(Input input) throws StatusCodeWithException {
        String[] taskStr = input.getModelId().split("_");

        TaskResultMySqlModel model = taskResultService.findOne(taskStr[0], taskStr[2], input.getRole(), TaskResultType.model_train.name());
        if (model == null) {
            StatusCode.DATA_NOT_FOUND.throwException("未查找到模型数据！");
        }
        return model.getTaskId();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "modelId", require = true)
        private String modelId;

        @Check(name = "模型角色", require = true)
        private JobMemberRole role;

        //region getter/setter


        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public JobMemberRole getRole() {
            return role;
        }

        public void setRole(JobMemberRole role) {
            this.role = role;
        }

        public static PushModelToServingByProviderApi.Input of(String modelId, JobMemberRole role) {
            PushModelToServingByProviderApi.Input input = new PushModelToServingByProviderApi.Input();
            input.modelId = modelId;
            input.role = role;
            return input;
        }

        //endregion

    }
}
