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
package com.welab.wefe.common.web.api.dev;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.config.CommonConfig;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * @author zane
 * @date 2022/3/23
 */
@Api(path = "log_file/download", name = "下载日志文件")
public class DownloadLogFile extends AbstractNoneInputApi<ResponseEntity<?>> {

    @Autowired
    private CommonConfig commonConfig;

    @Override
    protected ApiResult<ResponseEntity<?>> handle() throws StatusCodeWithException {

        File file = new File(commonConfig.getLoggingFilePath());
        if (!file.exists()) {
            StatusCode.FILE_DOES_NOT_EXIST.throwException("日志文件不存在：" + file.getAbsolutePath());
        }
        return file(new File(commonConfig.getLoggingFilePath()));
    }

}
