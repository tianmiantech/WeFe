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

package com.welab.wefe.board.service.api.data_resource.image_data_set;


import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * @author Zane
 */
@Api(path = "image_data_set/download", name = "delete data set", login = false)
public class ImageDataSetDownloadApi extends AbstractApi<ImageDataSetDownloadApi.Input, ResponseEntity<?>> {

    @Autowired
    private ImageDataSetService imageDataSetService;

    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws StatusCodeWithException {
        File file = imageDataSetService.download(input.dataSetId, input.jobId);
        return file(file);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "数据集 Id", require = true)
        public String dataSetId;
        public String jobId;

    }
}
