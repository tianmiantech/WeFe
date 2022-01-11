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
package com.welab.wefe.common.file.decompression.impl;

import com.welab.wefe.common.file.decompression.AbstractDecompression;
import com.welab.wefe.common.file.decompression.dto.DecompressionResult;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 解压tar.gz文件
 *
 * @author zane
 * @date 2021/11/29
 */
public class Tgz extends AbstractDecompression {
    @Override
    protected DecompressionResult doDecompression(File file, String destDir) throws IOException {

        DecompressionResult result = new DecompressionResult(file, destDir);

        Path targetPath = Paths.get(destDir);
        try (InputStream fi = Files.newInputStream(file.toPath());
             BufferedInputStream bi = new BufferedInputStream(fi);
             GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
             TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {

                //获取解压文件目录，并判断文件是否损坏
                Path newPath = zipSlipProtect(entry, targetPath);

                if (entry.isDirectory()) {
                    //创建解压文件目录
                    Files.createDirectories(newPath);
                    result.addDir(newPath);
                } else {
                    //再次校验解压文件目录是否存在
                    Path parent = newPath.getParent();
                    if (parent != null) {
                        if (Files.notExists(parent)) {
                            Files.createDirectories(parent);
                        }
                    }
                    // 将解压文件输入到TarArchiveInputStream，输出到磁盘newPath目录
                    Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING);
                    result.addFile(newPath.toFile());
                }
            }
        }

        return result;
    }

    /**
     * 判断压缩文件是否被损坏，并返回该文件的解压目录
     */
    private Path zipSlipProtect(ArchiveEntry entry, Path targetDir)
            throws IOException {

        Path targetDirResolved = targetDir.resolve(entry.getName());
        Path normalizePath = targetDirResolved.normalize();

        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("压缩文件已被损坏: " + entry.getName());
        }

        return normalizePath;
    }

    public static void main(String[] args) throws IOException {
        Tgz tgz = new Tgz();
        File file = new File("/Users/zane/data/wefe_file_upload_dir/flowers/image.tgz");
        DecompressionResult decompression = tgz.decompression(file);
        System.out.println(decompression);
    }
}
