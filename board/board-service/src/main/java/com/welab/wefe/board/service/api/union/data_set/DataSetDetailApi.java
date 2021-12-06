/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.union.data_set;


import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "union/table_data_set/detail", name = "Get data set details from union")
public class DataSetDetailApi extends AbstractApi<DataSetDetailApi.Input, TableDataSetOutputModel> {

    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<TableDataSetOutputModel> handle(DataSetDetailApi.Input input) throws StatusCodeWithException {
        return success(unionService.getDataResourceDetail(input.getDataSetId(), TableDataSetOutputModel.class));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "数据集 Id", require = true)
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
