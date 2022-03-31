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
package com.welab.wefe.common.file.compression;

import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件压缩工具类的抽象类
 *
 * @author zane
 * @date 2021/11/29
 */
public abstract class AbstractCompression {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 压缩文件夹至指定目录
     */
    protected abstract void doCompression(Path srcDir, String destFileName) throws IOException;

    protected abstract CompressionType getCompressionType();

    /**
     * 压缩文件夹至指定目录
     */
    public File compression(String srcDir) throws IOException {
        return compression(srcDir, null);
    }

    /**
     * 压缩文件夹至指定目录
     */
    public File compression(String srcDir, String destFileName) throws IOException {
        Path srcPath = Paths.get(srcDir);
        File srcFile = srcPath.toFile();

        // 判断源文件夹是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "文件夹不存在");
        }

        if (StringUtil.isEmpty(destFileName)) {
            destFileName = Paths.get(
                    srcFile.getParent(),
                    srcFile.getName() + getCompressionType().getSuffix()
            ).toString();
        }

        // 如果压缩文件已存在，删除。
        File destFile = new File(destFileName);
        if (destFile.exists()) {
            destFile.delete();
        }


        // 执行压缩
        doCompression(srcPath, destFileName);

        return new File(destFileName);
    }
}
