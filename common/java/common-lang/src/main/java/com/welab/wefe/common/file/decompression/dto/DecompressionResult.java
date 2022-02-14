/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.file.decompression.dto;

import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文件解压结果
 *
 * @author zane
 * @date 2021/11/26
 */
public class DecompressionResult {
    public final String baseDir;
    /**
     * 解压后的目录列表
     */
    public final Set<File> dirs = new HashSet<>();

    /**
     * 解压后的文件列表
     */
    public final Set<File> files = new HashSet<>();

    public DecompressionResult(File srcFile, String destDirPath) {
        baseDir = Paths.get(destDirPath, FileUtil.getFileNameWithoutSuffix(srcFile)).toAbsolutePath().toString();
    }

    public void addDir(File file) {
        dirs.add(file);
    }

    public void addDir(String dir) {
        dirs.add(new File(dir));
    }

    public void addDir(Path dir) {
        dirs.add(dir.toFile());
    }

    public void addFile(File file) {
        files.add(file);
    }

    /**
     * delete all unzip resource
     */
    public void deleteAllDirAndFiles() {

        FileUtil.deleteFileOrDir(baseDir);

        for (File dir : dirs) {
            FileUtil.deleteFileOrDir(dir);
        }

        for (File file : files) {
            FileUtil.deleteFileOrDir(file);
        }
    }

    @Override
    public String toString() {
        List<String> filesList = files
                .stream()
                .map(x -> x.getAbsolutePath())
                .collect(Collectors.toList());

        List<String> dirsList = dirs
                .stream()
                .map(x -> x.getAbsolutePath())
                .collect(Collectors.toList());


        return "Result{" + System.lineSeparator()
                + "files=" + System.lineSeparator()
                + StringUtil.join(filesList, System.lineSeparator())
                + System.lineSeparator()
                + System.lineSeparator()
                + "dirs=" + System.lineSeparator()
                + StringUtil.join(dirsList, System.lineSeparator())
                + System.lineSeparator()
                + System.lineSeparator()
                + "base_dir=" + System.lineSeparator()
                + baseDir
                + '}';
    }
}
