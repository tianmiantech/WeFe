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
package com.welab.wefe.common.file.compression.impl;

import com.welab.wefe.common.file.compression.AbstractCompression;
import com.welab.wefe.common.file.compression.CompressionType;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author zane
 * @date 2021/12/8
 */
public class Tgz extends AbstractCompression {
    @Override
    protected void doCompression(Path srcDir, String destFileName) throws IOException {

        Path destPath = Paths.get(destFileName);
        try (
                OutputStream fOut = Files.newOutputStream(destPath);
                BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
                GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
                TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)
        ) {
            //遍历文件目录树
            Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {

                //当成功访问到一个文件
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attributes) throws IOException {

                    // 判断当前遍历文件是不是符号链接(快捷方式)，不做打包压缩处理
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    //获取当前遍历文件名称
                    Path targetFile = srcDir.relativize(file);

                    //将该文件打包压缩
                    TarArchiveEntry tarEntry = new TarArchiveEntry(
                            file.toFile(), targetFile.toString());
                    tOut.putArchiveEntry(tarEntry);
                    Files.copy(file, tOut);
                    tOut.closeArchiveEntry();
                    //继续下一个遍历文件处理
                    return FileVisitResult.CONTINUE;
                }

                //当前遍历文件访问失败
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOG.error("无法对该文件压缩打包为tar.gz：" + file.toString(), exc);
                    return FileVisitResult.CONTINUE;
                }

            });
            //for循环完成之后，finish-tar包输出流
            tOut.finish();
        }

    }

    @Override
    protected CompressionType getCompressionType() {
        return CompressionType.Tgz;
    }

    public static void main(String[] args) throws IOException {
        Tgz tgz = new Tgz();
        File file = tgz.compression("/Users/zane/data/wefe_file_upload_dir/flowers/jpg");
        System.out.println(file.getAbsolutePath());
        System.out.println((file.length() / 1024) + "MB");
    }
}
