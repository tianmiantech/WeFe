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
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 对 7zip 文件进行解压
 *
 * @author zane
 * @date 2021/11/29
 */
public class SevenZip extends AbstractDecompression {
    @Override
    protected DecompressionResult doDecompression(File file, String outputDir) throws IOException {
        DecompressionResult result = new DecompressionResult(file, outputDir);
        // 循环解压
        SevenZFile sevenZFile = new SevenZFile(file);
        SevenZArchiveEntry entry = null;
        while ((entry = sevenZFile.getNextEntry()) != null) {
            String newFilePath = outputDir + File.separator + entry.getName();
            File newFile = new File(newFilePath);

            // 处理目录
            if (entry.isDirectory()) {
                if (!newFile.exists()) {
                    boolean mkdirs = newFile.mkdirs();
                    if (!mkdirs) {
                        throw new RuntimeException("Fail mkdir:" + newFilePath);
                    }
                }

                result.addDir(newFilePath);
                continue;
            }

            // 解压文件
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(newFile);
                int length = 0;
                byte[] buffer = new byte[2048];
                while ((length = sevenZFile.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                result.addFile(newFile);
            } catch (Exception e) {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        SevenZip sevenZip = new SevenZip();
        File file = new File("/Users/zane/data/wefe_file_upload_dir/flower.7z");
        String outputDir = "/Users/zane/data/wefe_file_upload_dir/temp";
        DecompressionResult decompression = sevenZip.decompression(file, outputDir);
        System.out.println(decompression);
    }

}
