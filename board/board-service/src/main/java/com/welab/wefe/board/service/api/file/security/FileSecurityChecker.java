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
package com.welab.wefe.board.service.api.file.security;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author zane
 * @date 2021/12/31
 */
public abstract class FileSecurityChecker {
    protected final static Logger LOG = LoggerFactory.getLogger(FileSecurityChecker.class);
    protected static final String[] keywords = {"<", ">", "\\"};

    protected abstract void doCheck(File file) throws IOException;

    public static void check(File file) throws Exception {
        // 为检查上传的文件是否安全
        String suffix = StringUtil.substringAfterLast(file.getName(), ".");

        try {
            switch (suffix) {
                case "xls":
                case "xlsx":
                    new ExcelSecurityChecker().doCheck(file);
                    break;
                case "csv":
                    new CsvSecurityChecker().doCheck(file);
                    break;
                case "zip":
                case "gz":
                case "tgz":
                case "7z":
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
}
