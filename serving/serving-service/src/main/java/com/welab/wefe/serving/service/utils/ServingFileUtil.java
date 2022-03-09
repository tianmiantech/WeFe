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

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.enums.file.FileTypeEnum;

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
    public static Path getBaseDir(FileTypeEnum type) {
        String childDir = StringUtil.stringToUnderLineLowerCase(type.name());
        return getRootDir().resolve(childDir);
    }
}
