package com.welab.wefe.board.service.api.project.fusion.result;

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


import com.welab.wefe.board.service.dto.fusion.FusionResultExportProgress;
import com.welab.wefe.board.service.fusion.manager.ExportManager;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/result/export_progress", name = "结果导出", desc = "结果导出", login = false)
public class ResultExportProgressApi extends AbstractApi<ResultExportProgressApi.Input, FusionResultExportProgress> {


    @Override
    protected ApiResult<FusionResultExportProgress> handle(Input input) throws Exception {
        return success(ExportManager.get(input.getBusinessId()));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "指定操作的businessId", require = true)
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
