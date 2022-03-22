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

package com.welab.wefe.board.service.api.data_resource.upload_task;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceUploadTaskOutputModel;
import com.welab.wefe.board.service.service.data_resource.DataResourceUploadTaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "data_resource/upload_task/query", name = "query data set upload task list")
public class DataResourceUploadTaskQueryApi extends AbstractApi<DataResourceUploadTaskQueryApi.Input, PagingOutput<DataResourceUploadTaskOutputModel>> {

    @Autowired
    private DataResourceUploadTaskService dataResourceUploadTaskService;

    @Override
    protected ApiResult<PagingOutput<DataResourceUploadTaskOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(dataResourceUploadTaskService.query(input));
    }

    public static class Input extends PagingInput {
    }

}
