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
package com.welab.wefe.common.file.decompression;

import com.welab.wefe.common.file.decompression.dto.DecompressionResult;
import com.welab.wefe.common.file.decompression.impl.SevenZip;
import com.welab.wefe.common.file.decompression.impl.Tgz;
import com.welab.wefe.common.file.decompression.impl.Zip;
import com.welab.wefe.common.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 超级解压，解压一切类型的压缩文件。
 *
 * @author zane
 * @date 2021/11/29
 */
public class SuperDecompressor {
    private static final Map<String, Class<? extends AbstractDecompression>> MAP = new HashMap<>();

    static {
        MAP.put(".zip", Zip.class);
        MAP.put(".gz", Tgz.class);
        MAP.put(".tar.gz", Tgz.class);
        MAP.put(".tgz", Tgz.class);
        MAP.put(".7z", SevenZip.class);
    }

    /**
     * 解压文件
     * 输出目录为压缩包所在的目录
     *
     * @param file 压缩包文件
     * @return 解压后的文件与目录列表
     */
    public static DecompressionResult decompression(File file) throws Exception {
        return decompression(file, null, false);
    }

    /**
     * 解压文件
     * 输出目录为压缩包所在的目录
     *
     * @param file      压缩包文件
     * @param recursive 是否递归解压
     * @return 解压后的文件与目录列表
     */
    public static DecompressionResult decompression(File file, boolean recursive) throws Exception {
        return decompression(file, null, recursive);
    }

    /**
     * 解压文件
     *
     * @param srcFile     压缩包文件
     * @param destDirPath 解压输出目录
     * @param recursive   是否递归解压
     * @return 解压后的文件与目录列表
     */
    public static DecompressionResult decompression(File srcFile, String destDirPath, boolean recursive) throws Exception {
        DecompressionResult result = new DecompressionResult(srcFile, destDirPath);

        decompression(result, srcFile, destDirPath);
        while (recursive) {
            File file = result.files.stream()
                    .filter(x -> FileUtil.isArchive(x))
                    .findFirst()
                    .orElse(null);

            if (file == null) {
                break;
            }
            decompression(result, file, null);
            FileUtil.deleteFileOrDir(file);
            result.files.remove(file);
        }
        return result;
    }

    /**
     * 递归解压
     */
    private static DecompressionResult decompression(DecompressionResult result, File srcFile, String destDirPath) throws InstantiationException, IllegalAccessException, IOException {
        Class<? extends AbstractDecompression> clazz = null;
        for (Map.Entry<String, Class<? extends AbstractDecompression>> entry : MAP.entrySet()) {
            String key = entry.getKey();
            Class<? extends AbstractDecompression> value = entry.getValue();
            if (srcFile.getName().toLowerCase().endsWith(key)) {
                clazz = value;
                break;
            }
        }

        if (clazz == null) {
            return result;
        }
        AbstractDecompression instance = clazz.newInstance();
        DecompressionResult decompressionResult = instance.decompression(srcFile, destDirPath);
        result.addDir(decompressionResult.baseDir);
        for (File dir : decompressionResult.dirs) {
            result.addDir(dir);
        }
        for (File file : decompressionResult.files) {
            result.addFile(file);
        }
        return result;
    }
}
