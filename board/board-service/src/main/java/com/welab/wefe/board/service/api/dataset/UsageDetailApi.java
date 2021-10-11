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

package com.welab.wefe.board.service.api.dataset;

import com.welab.wefe.board.service.dto.entity.project.ProjectUsageDetailOutputModel;
import com.welab.wefe.board.service.service.DataSetService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * @author Jacky.jiang
 */
@Api(path = "data_set/usage_detail", name = "list usage_detail")
public class UsageDetailApi extends AbstractApi<UsageDetailApi.Input, List<ProjectUsageDetailOutputModel>> {
    @Autowired
    private DataSetService dataSetService;

    @Override
    protected ApiResult<List<ProjectUsageDetailOutputModel>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(dataSetService.queryUsageInProject(input.getDataSetId()));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "数据集ID", require = true)
        private String dataSetId;

        //region getter/setter

        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
        }

        //endregion
    }
}
