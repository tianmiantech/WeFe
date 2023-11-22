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

package com.welab.wefe.board.service.api.data_output_info;

import com.welab.wefe.board.service.service.ServingService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "data_output_info/sync_model_to_serving", name = "sync model to serving service")
public class SyncModelToServingApi extends AbstractNoneOutputApi<SyncModelToServingApi.Input> {

    @Autowired
    private ServingService servingService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        servingService.syncModelToServing(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "taskId", require = true)
        private String taskId;

        @Check(name = "模型角色", require = true)
        private JobMemberRole role;

        //region getter/setter

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public JobMemberRole getRole() {
            return role;
        }

        public void setRole(JobMemberRole role) {
            this.role = role;
        }


        //endregion

    }
}
