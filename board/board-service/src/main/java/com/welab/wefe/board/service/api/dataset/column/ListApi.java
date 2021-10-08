/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.api.dataset.column;

import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnOutputModel;
import com.welab.wefe.board.service.service.DataSetColumnService;
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
@Api(path = "data_set/column/list", name = "list of data set fields")
public class ListApi extends AbstractApi<ListApi.Input, PagingOutput<DataSetColumnOutputModel>> {

    @Autowired
    private DataSetColumnService service;

    @Override
    protected ApiResult<PagingOutput<DataSetColumnOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(service.list(input.getDataSetId()));
    }


    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "数据集Id")
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
