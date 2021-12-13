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

package com.welab.wefe.board.service.api.data_resource.image_data_set.sample;

import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.database.repository.ImageDataSetSampleRepository;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;

/**
 * @author Zane
 */
@Api(path = "image_data_set_sample/download", name = "download image data set sample")
public class ImageDataSetSampleDownloadApi extends AbstractApi<ImageDataSetSampleDownloadApi.Input, ResponseEntity<?>> {

    @Autowired
    private ImageDataSetSampleRepository imageDataSetSampleRepository;

    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws StatusCodeWithException, IOException {
        ImageDataSetSampleMysqlModel sample = imageDataSetSampleRepository.findById(input.id).orElse(null);
        File file = new File(sample.getFilePath());

        return file(file);
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String id;
    }
}
