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
package com.welab.wefe.board.service.api.model.deep_learning;

import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * @author zane
 * @date 2022/2/14
 */
@Api(path = "model/deep_learning/call/download/image", name = "下载需要批量推理的zip文件")
public class DownloadDataSetImageApi extends AbstractApi<DownloadDataSetImageApi.Input, ResponseEntity<?>> {

    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws Exception {
        File file = WeFeFileSystem.CallDeepLearningModel
                .getZipFileUnzipDir(input.taskId)
                .resolve(input.filename)
                .toFile();

        if (!file.exists()) {
            StatusCode.FILE_DOES_NOT_EXIST.throwException("文件不存在");
        }

        return file(file);
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String taskId;
        @Check(require = true)
        public String filename;
    }
}
