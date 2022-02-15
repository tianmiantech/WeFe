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
import com.welab.wefe.common.web.Launcher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zane
 * @date 2022/2/15
 */
public class WeFeFileSystem {
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
}
