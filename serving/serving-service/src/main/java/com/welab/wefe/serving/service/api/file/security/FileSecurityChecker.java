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
package com.welab.wefe.serving.service.api.file.security;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author zane
 * @date 2021/12/31
 */
public abstract class FileSecurityChecker {
    protected final static Logger LOG = LoggerFactory.getLogger(FileSecurityChecker.class);
    protected static final String[] keywords = {"<", ">", "\\"};
    /**
     * 允许的文件类型
     */
    private static final List<String> ALLOW_FILE_TYPES = Arrays.asList(
            "json", "zip"
    );

    protected abstract void doCheck(File file) throws IOException;

    public static void check(File file) throws Exception {

        // 为检查上传的文件是否安全
        String suffix = StringUtil.substringAfterLast(file.getName(), ".");
        try {
            checkIsAllowFileType(file.getName());

            switch (suffix) {
                case "json":
                case "zip":
                    break;
                default:
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的文件类型：" + suffix);
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            FileUtil.deleteFileOrDir(file);
            throw e;
        }
    }

    public static void checkIsAllowFileType(String filename) throws StatusCodeWithException {
        if (StringUtil.isEmpty(filename)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("文件名不允许为空");
        }

        String suffix = StringUtil.substringAfterLast(filename, ".");
        if (StringUtil.isEmpty(suffix)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("不支上传无文件后缀的文件");
        }

        if (!ALLOW_FILE_TYPES.contains(suffix)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的文件类型：" + suffix);
        }
    }
}
