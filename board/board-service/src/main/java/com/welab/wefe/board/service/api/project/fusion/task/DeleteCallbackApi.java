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
package com.welab.wefe.board.service.api.project.fusion.task;

import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 * @date 2022/4/19
 */
@Api(path = "task/delete_callback", name = "接收删除请求", desc = "接收删除请求", login = false, rsaVerify = true)
public class DeleteCallbackApi extends AbstractNoneOutputApi<DeleteCallbackApi.Input> {

    @Autowired
    FusionTaskService fusionTaskService;

    @Override
    protected ApiResult handler(DeleteCallbackApi.Input input) throws StatusCodeWithException {
        fusionTaskService.deleteCallback(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "指定操作的businessId", require = true)
        private String businessId;

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }
    }
}
