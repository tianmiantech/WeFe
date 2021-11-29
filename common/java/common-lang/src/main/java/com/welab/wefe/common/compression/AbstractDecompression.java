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
package com.welab.wefe.common.compression;

import com.welab.wefe.common.compression.dto.DecompressionResult;
import com.welab.wefe.common.util.StringUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * 解压工具类的抽象类
 *
 * @author zane
 * @date 2021/11/29
 */
public abstract class AbstractDecompression {
    protected final Logger LOG = Logger.getLogger(this.getClass());

    /**
     * 解压文件至指定目录
     */
    protected abstract DecompressionResult doDecompression(File file, String destDir) throws IOException;

    /**
     * 解压文件至指定目录
     */
    public DecompressionResult decompression(File srcFile) throws IOException {
        return decompression(srcFile, null);
    }

    /**
     * 解压文件至指定目录
     */
    public DecompressionResult decompression(File srcFile, String destDirPath) throws IOException {
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
        }

        if (StringUtil.isEmpty(destDirPath)) {
            destDirPath = srcFile.getParent();
        }

        return doDecompression(srcFile, destDirPath);
    }
}
