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

package com.welab.wefe.board.service.api.fusion.task;

import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/task/info", name = "查询任务进度", desc = "查询任务进度")
public class InfoApi extends AbstractApi<InfoApi.Input, JObject> {


    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException {
        return success(ActuatorManager.getTaskInfo(input.getBusinessId()));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "指定操作的taskId", require = true)
        private String businessId;

        //region


        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        //endregion
    }
}
