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

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.DataResourceType;

import java.nio.file.Path;

/**
 * @author zane
 * @date 2022/2/15
 */
public class UploadFile {
    /**
     * 文件用途
     */
    public enum UseType {
        /**
         * 添加数据资源
         */
        AddTableDataSet,
        AddImageDataSet,
        AddBloomFilter,
        /**
         * 调用深度学习模型
         */
        CallDeepLearningModel
    }

    /**
     * 获取文件上传路径
     */
    public static Path getBaseDir(UseType type) {
        String childDir = StringUtil.stringToUnderLineLowerCase(type.name());
        return WeFeFileSystem.getRootDir().resolve(childDir);
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
}
