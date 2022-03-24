/*
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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
package com.welab.wefe.serving.service.utils;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.file.compression.impl.Zip;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.service.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author hunter.zhao
 * @date 2022/3/9
 */
public class ServingFileUtil {
    private static final Path ROOT_DIR = Paths.get(Launcher.getBean(Config.class).getFileUploadDir());

    public static Path getRootDir(){
        return ROOT_DIR;
    }

    /**
     * 获取文件上传路径
     */
    public static Path getBaseDir(FileType type) {
        String childDir = StringUtil.stringToUnderLineLowerCase(type.name());
        return getRootDir().resolve(childDir);
    }


    public enum FileType {

        /**
         * 临时目录，文件不会长时间保存。
         */
        Temp,

        /**
         * 机器学习模型文件
         */
        MachineLearningModelFile,

        /**
         * 深度学习模型文件
         */
        DeepLearningModelFile
    }


    public static class DeepLearningModelFile {

        /**
         * 包含图片的zip文件
         */
        public static File getModelFile(String ModelId) {
            return getBaseDir(FileType.DeepLearningModelFile).resolve("model").resolve(ModelId + ".zip").toFile();
        }

        /**
         * 包含图片的zip文件
         */
        public static File getZipFile(String ModelId) {
            return getBaseDir(FileType.DeepLearningModelFile).resolve(ModelId + ".zip").toFile();
        }

        /**
         * zip 文件解压目录
         */
        public static Path getZipFileUnzipDir(String ModelId) {
            return getBaseDir(FileType.DeepLearningModelFile).resolve(ModelId);
        }

        public static File singleImageToZip(String filename, String ModelId) throws StatusCodeWithException {
            File rawFile = getBaseDir(FileType.DeepLearningModelFile).resolve(filename).toFile();
            // 检查文件是否是图片
            if (!FileUtil.isImage(rawFile)) {
                if (rawFile.exists()) {
                    rawFile.delete();
                }
                StatusCode.PARAMETER_VALUE_INVALID.throwException("文件不是图片");
            }

            // 创建文件夹
            Path dir = getBaseDir(FileType.DeepLearningModelFile).resolve(ModelId);
            // 将图片移动到文件夹
            FileUtil.moveFile(rawFile, dir.toString());
            // 压缩文件夹
            Zip zip = new Zip();
            File zipFile = null;
            try {
                zipFile = zip.compression(
                        getBaseDir(FileType.DeepLearningModelFile).resolve(ModelId).toString()
                );
            } catch (IOException e) {
                return null;
            }
            return zipFile;
        }

        /**
         * 将上传的文件重命名为以 ModelId 命名的文件
         *
         * @param filename 原始文件名
         */
        public static File renameZipFile(String filename, String ModelId) throws StatusCodeWithException {
            File rawFile = getBaseDir(FileType.DeepLearningModelFile).resolve(filename).toFile();
            File renamedFile = getBaseDir(FileType.DeepLearningModelFile).resolve(ModelId + ".zip").toFile();

            // 在重命名之前先检查是否需要重命名
            if (rawFile.getAbsolutePath().equals(renamedFile.getAbsolutePath())) {
                return renamedFile;
            }

            if (!rawFile.exists()) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("未找到文件：" + filename);
            }

            String suffix = FileUtil.getFileSuffix(filename);
            if (!"zip".equalsIgnoreCase(suffix)) {
                FileUtil.deleteFileOrDir(rawFile);
                StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的文件类型：" + suffix);
            }

            // 重命名之前新文件先删除之前重命名的文件
            if (renamedFile.exists()) {
                renamedFile.delete();
            }

            rawFile.renameTo(renamedFile);

            return renamedFile;
        }


        /**
         * 预测结果输出地址
         */
        public static Path getPredictOutputPath(String modelId) {
            return getBaseDir(FileType.DeepLearningModelFile).resolve(modelId);
        }

    }
}
