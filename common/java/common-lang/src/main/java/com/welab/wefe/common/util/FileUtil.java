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

package com.welab.wefe.common.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author zane.luo
 */
public class FileUtil {

    public static boolean isImage(File file) {
        switch (getFileSuffix(file).toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "webp":
            case "bmp":
            case "tif":
            case "gif":
                return true;
            default:
                return false;
        }
    }

    /**
     * get file suffix
     */
    public static String getFileSuffix(File file) {
        return StringUtil.substringAfterLast(file.getName(), ".");
    }

    /**
     * get file name without suffix
     */
    public static String getFileNameWithoutSuffix(File file) {
        return StringUtil.substringBeforeLast(file.getName(), ".");
    }

    /**
     * Create a directory
     *
     * @param dirPath Directory path
     */
    public static void createDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    public static String readAllText(String path) throws IOException {
        return readAllText(new File(path), StandardCharsets.UTF_8);
    }

    public static String readAllText(File file) throws IOException {
        return readAllText(file, StandardCharsets.UTF_8);
    }


    public static String readAllText(File file, Charset charset) throws IOException {
        StringBuilder content = new StringBuilder(512);
        BufferedReader in = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, charset);
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                content
                        .append(line)
                        .append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw e;
        } finally {
            in.close();
        }
        return content.toString();
    }

    /**
     * Delete files or folders
     *
     * @param file File or folder
     */
    public static void deleteFileOrDir(File file) {
        if (null == file) {
            return;
        }
        if (!file.isDirectory()) {
            file.delete();
            return;
        }
        File[] subFile = file.listFiles();
        for (File f : subFile) {
            deleteFileOrDir(f);
        }
        file.delete();
    }

    public static void deleteFileOrDir(String filePath) {
        deleteFileOrDir(new File(filePath));
    }


}
