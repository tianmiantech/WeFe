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
         * 包含图片的zip文件
         */
        public static File getModelFile(String taskId) {
            return getBaseDir(UseType.CallDeepLearningModel).resolve("model").resolve(taskId + ".zip").toFile();
        }

        /**
         * 包含图片的zip文件
         */
        public static File getZipFile(String taskId) {
            return getBaseDir(UseType.CallDeepLearningModel).resolve(taskId + ".zip").toFile();
        }

        /**
         * zip 文件解压目录
         */
        public static Path getZipFileUnzipDir(String taskId) {
            return getBaseDir(UseType.CallDeepLearningModel).resolve(taskId);
        }

        public static File singleImageToZip(String filename, String taskId) throws StatusCodeWithException {
            File rawFile = getBaseDir(UseType.CallDeepLearningModel).resolve(filename).toFile();
            // 检查文件是否是图片
            if (!FileUtil.isImage(rawFile)) {
                if (rawFile.exists()) {
                    rawFile.delete();
                }
                StatusCode.PARAMETER_VALUE_INVALID.throwException("文件不是图片");
            }

            // 创建文件夹
            Path dir = getBaseDir(UseType.CallDeepLearningModel).resolve(taskId);
            // 将图片移动到文件夹
            FileUtil.moveFile(rawFile, dir.toString());
            // 压缩文件夹
            Zip zip = new Zip();
            File zipFile = null;
            try {
                zipFile = zip.compression(
                        getBaseDir(UseType.CallDeepLearningModel).resolve(taskId).toString()
                );
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                StatusCode.FILE_IO_ERROR.throwException(e);
            }
            return zipFile;
        }

        /**
         * 将上传的文件重命名为以 taskId 命名的文件
         *
         * @param filename 原始文件名
         */
        public static File renameZipFile(String filename, String taskId) throws StatusCodeWithException {
            File rawFile = getBaseDir(UseType.CallDeepLearningModel).resolve(filename).toFile();
            File renamedFile = getBaseDir(UseType.CallDeepLearningModel).resolve(taskId + ".zip").toFile();

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
    }
}
