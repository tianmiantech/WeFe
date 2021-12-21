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

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.fusion.FusionTaskOutput;
import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/task/paging", name = "任务列表", desc = "任务列表", login = true)
public class PagingApi extends AbstractApi<PagingApi.Input, PagingOutput<FusionTaskOutput>> {
    @Autowired
    FusionTaskService fusionTaskService;

    @Override
    protected ApiResult<PagingOutput<FusionTaskOutput>> handle(Input input) throws StatusCodeWithException {
        return success(fusionTaskService.paging(input));
    }


    public static class Input extends PagingInput {
        @Check(name = "projectId")
        private String projectId;

        @Check(name = "businessId")
        private String businessId;

        @Check(name = "任务状态")
        private FusionTaskStatus status;

        //region


        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public FusionTaskStatus getStatus() {
            return status;
        }

        public void setStatus(FusionTaskStatus status) {
            this.status = status;
        }

        //endregion
    }
}
