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

package com.welab.wefe.board.service.api.dataset;

import com.welab.wefe.board.service.database.entity.data_set.DataSetTaskMysqlModel;
import com.welab.wefe.board.service.dto.vo.DataSetAddInputModel;
import com.welab.wefe.board.service.service.dataset.DataSetTaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author Zane
 */
@Api(path = "data_set/add", name = "add data set")
public class AddApi extends AbstractApi<DataSetAddInputModel, DataSetTaskMysqlModel> {

    @Autowired
    private DataSetTaskService dataSetTaskService;

    @Override
    protected ApiResult<DataSetTaskMysqlModel> handle(DataSetAddInputModel input) throws StatusCodeWithException, IOException {
        DataSetTaskMysqlModel dataSetTaskMysqlModel = dataSetTaskService.add(input);
        return success(dataSetTaskMysqlModel);
    }

}
