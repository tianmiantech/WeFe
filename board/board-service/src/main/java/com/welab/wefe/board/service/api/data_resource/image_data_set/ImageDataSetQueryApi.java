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

package com.welab.wefe.board.service.api.data_resource.image_data_set;

import com.welab.wefe.board.service.api.data_resource.DataResourceQueryApi;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_resource.output.ImageDataSetOutputModel;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetService;
import com.welab.wefe.common.enums.DataResourceType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "image_data_set/query", name = "query image data set")
public class ImageDataSetQueryApi extends AbstractApi<DataResourceQueryApi.Input, PagingOutput<ImageDataSetOutputModel>> {

    @Autowired
    private ImageDataSetService imageDataSetService;

    @Override
    protected ApiResult<PagingOutput<ImageDataSetOutputModel>> handle(DataResourceQueryApi.Input input) throws StatusCodeWithException {
        input.setDataResourceType(DataResourceType.ImageDataSet);
        return success((PagingOutput<ImageDataSetOutputModel>) imageDataSetService.query(input));
    }

}
