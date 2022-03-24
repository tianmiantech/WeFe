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
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.config.CommonConfig;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author zane
 * @date 2022/3/23
 */
@Api(path = "log_file/find_exception", name = "搜索日志文件中的异常堆栈块")
public class FindLogFileException extends AbstractApi<FindLogFileException.Input, FindLogFileException.Output> {

    @Autowired
    private CommonConfig commonConfig;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        if (!CurrentAccount.isAdmin()) {
            StatusCode.PERMISSION_DENIED.throwException("普通用户无法进行此操作。");
        }

        File file = new File(commonConfig.getLoggingFilePath());
        if (!file.exists()) {
            StatusCode
                    .FILE_DOES_NOT_EXIST
                    .throwException("日志文件不存在：" + file.getAbsolutePath());
        }
        String command = "cat '" + file.getAbsolutePath() +
                "'| grep -i '^[[:space:]]*at ' -B 5 -A 5 | cut -c 1-300 | tail -" +
                input.tailCount;

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.lineSeparator());
        }

        return success(new Output(builder.toString()));
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "最大日志行返回数")
        public int tailCount = 100;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (tailCount <= 0) {
                tailCount = 100;
            }
            if (tailCount > 1000) {
                tailCount = 1000;
            }
        }
    }

    public static class Output {
        public String log;

        public Output() {
        }

        public Output(String log) {
            this.log = log;
        }
    }
}
