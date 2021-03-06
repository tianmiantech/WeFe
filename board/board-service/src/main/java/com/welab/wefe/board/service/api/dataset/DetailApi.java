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

import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.DataSetRepository;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetOutputModel;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "data_set/detail", name = "get data set detail")
public class DetailApi extends AbstractApi<DetailApi.Input, DataSetOutputModel> {

    @Autowired
    DataSetRepository dataSetRepository;

    @Override
    protected ApiResult<DataSetOutputModel> handle(Input input) throws StatusCodeWithException {

        DataSetMysqlModel model = dataSetRepository.findById(input.id).orElse(null);

        if (model == null) {
            return success();
        }

        DataSetOutputModel output = ModelMapper.map(model, DataSetOutputModel.class);

        return success(output);

    }

    public static class Input extends AbstractApiInput {
        private String id;

        //region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }


        //endregion
    }
}
