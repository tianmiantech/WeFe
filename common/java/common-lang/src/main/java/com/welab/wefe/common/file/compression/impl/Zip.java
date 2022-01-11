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
package com.welab.wefe.common.file.compression.impl;

import com.welab.wefe.common.file.compression.AbstractCompression;
import com.welab.wefe.common.file.compression.CompressionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zane
 * @date 2021/12/8
 */
public class Zip extends AbstractCompression {
    @Override
    protected void doCompression(Path srcDir, String destFileName) throws IOException {

        try (
                FileOutputStream fileOutputStream = new FileOutputStream(destFileName);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        ) {
            // 递归压缩文件夹
            File srcFile = srcDir.toFile();
            zipFile(srcFile, srcFile.getName(), zipOutputStream);
        }

    }

    /**
     * 使用递归遍历，将文件或文件夹压缩到zip文件中
     */
    private static void zipFile(File srcFile, String srcRelativePath, ZipOutputStream zipOutputStream) throws IOException {
        if (srcFile.isDirectory()) {
            String entryName = srcRelativePath;
            if (!entryName.endsWith(File.separatorChar + "")) {
                entryName += File.separatorChar;
            }
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            zipOutputStream.closeEntry();
            //遍历文件夹子目录，进行递归的zipFile
            File[] children = srcFile.listFiles();
            for (File childFile : children) {
                zipFile(childFile, entryName + childFile.getName(), zipOutputStream);
            }
        } else {
            FileInputStream fis = new FileInputStream(srcFile);
            ZipEntry zipEntry = new ZipEntry(srcRelativePath);
            zipOutputStream.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }
            fis.close();
        }
    }

    @Override
    protected CompressionType getCompressionType() {
        return CompressionType.Zip;
    }

    public static void main(String[] args) throws IOException {
        Zip zip = new Zip();
        File file = zip.compression(
                "/Users/zane/data/wefe_file_upload_dir/flowers 2"
        );
        System.out.println(file.getAbsolutePath());
        System.out.println((file.length() / 1024) + "MB");
    }
}
