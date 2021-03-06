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

package com.welab.wefe.board.service.api.dataset_task;

import com.welab.wefe.board.service.database.entity.data_set.DataSetTaskMysqlModel;
import com.welab.wefe.board.service.service.dataset.DataSetTaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lonnie
 */
@Api(path = "data_set_task/detail", name = "get a data set upload task info")
public class DetailApi extends AbstractApi<DetailApi.Input, DataSetTaskMysqlModel> {

    @Autowired
    private DataSetTaskService dataSetTaskService;

    @Override
    protected ApiResult<DataSetTaskMysqlModel> handle(Input input) throws StatusCodeWithException {
        return success(dataSetTaskService.findById(input.getId()));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "id唯一标识", require = true)
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
