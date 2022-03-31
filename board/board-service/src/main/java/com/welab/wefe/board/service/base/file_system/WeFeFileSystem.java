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
package com.welab.wefe.board.service.base.file_system;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.file.compression.impl.Zip;
import com.welab.wefe.common.file.decompression.SuperDecompressor;
import com.welab.wefe.common.file.decompression.dto.DecompressionResult;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 统一管理所有的文件，合理分配各种文件的目录，及时清理不需要的过期文件。
 *
 * @author zane
 * @date 2022/2/15
 */
public class WeFeFileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(WeFeFileSystem.class);
    private static final Path ROOT_DIR = Paths.get(Launcher.getBean(Config.class).getFileUploadDir());

    static {
        File dir = ROOT_DIR.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static Path getRootDir() {
        return ROOT_DIR;
    }

    /**
     * 文件用途
     */
    public enum UseType {
        /**
         * 临时目录，文件不会长时间保存。
         * todo：zane 此目录的文件会被自动回收。
         */
        Temp,
        /**
         * 添加数据资源
         */
        AddTableDataSet,
        AddImageDataSet,
        AddBloomFilter,
        /**
         * 调用深度学习模型
         */
        CallDeepLearningModel,
        /**
         * 下载深度学习模型
         */
        DownloadDeepLearningModel
    }

    /**
     * 获取文件上传路径
     */
    public static Path getBaseDir(UseType type) {
        String childDir = StringUtil.stringToUnderLineLowerCase(type.name());
        return WeFeFileSystem.getRootDir().resolve(childDir);
    }

    public static Path getFilePath(UseType type, String filename) {
        return getBaseDir(type).resolve(filename);
    }

    /**
     * 获取资源上传完成后的完整路径
     */
    public static Path getFilePath(DataResourceType dataResourceType, String filename) {
        return getFileDir(dataResourceType).resolve(filename);
    }

    /**
     * 获取资源上传完成后的所在目录
     */
    public static Path getFileDir(DataResourceType dataResourceType) {
        switch (dataResourceType) {
            case TableDataSet:
                return getBaseDir(UseType.AddTableDataSet);
            case ImageDataSet:
                return getBaseDir(UseType.AddImageDataSet);
            case BloomFilter:
                return getBaseDir(UseType.AddBloomFilter);
            default:
                return WeFeFileSystem.getRootDir();
        }

    }

    public static class DownloadDeepLearningModel {
        /**
         * 获取下载中的模型文件
         */
        public static File getDownloadingModelFile(String taskId) {
            return getBaseDir(UseType.DownloadDeepLearningModel).resolve(taskId + ".downloading").toFile();
        }

        /**
         * 当模型下载完毕后，执行此操作。
         */
        public static File modelFileDownloadCompleted(String taskId) {
            File downloadingModelFile = getDownloadingModelFile(taskId);
            File modelFile = getModelFile(taskId);

            downloadingModelFile.renameTo(modelFile);
            return modelFile;
        }

        /**
         * 获取下载完毕的模型文件
         */
        public static File getModelFile(String taskId) {
            return getBaseDir(UseType.DownloadDeepLearningModel).resolve(taskId + ".model").toFile();
        }
    }

    public static class CallDeepLearningModel {

        /**
         * 获取上传的原始文件
         */
        public static File getRawFile(String filename) {
            return getBaseDir(UseType.CallDeepLearningModel).resolve(filename).toFile();
        }

        /**
         * 包含模型的zip文件
         */
        public static File getModelFile(String taskId) {
            return getBaseDir(UseType.CallDeepLearningModel).resolve("model").resolve(taskId + ".zip").toFile();
        }

        /**
         * 包含图片的zip文件
         */
        public static File getZipFile(String taskId, String sessionId) {
            return getBaseDir(UseType.CallDeepLearningModel).resolve(taskId).resolve(sessionId + ".zip").toFile();
        }


        /**
         * 图片样本所在的目录： /CallDeepLearningModel/{taskId}/{sessionId}
         */
        public static Path getImageSimpleDir(String taskId, String sessionId) {
            return getBaseDir(UseType.CallDeepLearningModel).resolve(taskId).resolve(sessionId);

        }

        /**
         * 将图片所在的文件夹压缩为 zip，供飞桨下载。
         */
        public static File zipImageSimpleDir(String taskId, String sessionId) throws StatusCodeWithException {
            File zipFile = getZipFile(taskId, sessionId);
            if (zipFile.exists()) {
                zipFile.delete();
            }

            // 压缩文件夹
            try {
                new Zip().compression(getImageSimpleDir(taskId, sessionId), zipFile);
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                StatusCode.FILE_IO_ERROR.throwException(e);
            }
            return zipFile;
        }

        /**
         * 将单张图片移到预定目录
         */
        public static void moveSingleImageToSessionDir(File rawFile, String taskId, String sessionId) throws StatusCodeWithException {
            // 检查文件是否是图片
            if (!FileUtil.isImage(rawFile)) {
                if (rawFile.exists()) {
                    rawFile.delete();
                }
                StatusCode.PARAMETER_VALUE_INVALID.throwException("文件不是图片");
            }

            Path distDir = getImageSimpleDir(taskId, sessionId);
            FileUtil.moveFile(rawFile, distDir.toString());

        }

        /**
         * 将上传的文件解压后移动到预定目录
         */
        public static int moveZipFileToSessionDir(File zipFile, String taskId, String sessionId) throws StatusCodeWithException {
            if (!zipFile.exists()) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("未找到文件：" + zipFile.getAbsolutePath());
            }

            String suffix = FileUtil.getFileSuffix(zipFile);
            if (!"zip".equalsIgnoreCase(suffix)) {
                FileUtil.deleteFileOrDir(zipFile);
                StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的文件类型：" + suffix);
            }

            Path distDir = getImageSimpleDir(taskId, sessionId);
            DecompressionResult result = null;
            try {
                result = SuperDecompressor.decompression(zipFile, distDir.toString(), true);
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                StatusCode.FILE_IO_ERROR.throwException(e);
            }

            // 安全起见，把非图片文件删除掉。
            int imageCount = 0;
            for (File file : result.files) {
                if (FileUtil.isImage(file)) {
                    // 将文件移动到解压目录的根目录，避免zip包内有子文件导致路径不好管理。
                    FileUtil.moveFile(file, distDir);
                    imageCount++;
                } else {
                    file.delete();
                }
            }

            // 移除解压后的子目录
            for (File file : result.dirs) {
                file.delete();
            }

            // 移除原始文件
            zipFile.delete();

            if (imageCount == 0) {
                FileUtil.deleteFileOrDir(distDir.toFile());
                StatusCode.PARAMETER_VALUE_INVALID.throwException("压缩包中没有图片文件！");
            }
            return imageCount;
        }
    }
}
