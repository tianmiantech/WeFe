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

package com.welab.wefe.common.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zane.luo
 */
public class FileUtil {

    public static boolean isImage(File file) {
        if (file.isDirectory()) {
            return false;
        }
        return isImage(file.getName());
    }

    public static boolean isImage(String filename) {
        switch (getFileSuffix(filename).toLowerCase()) {
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
     * 是否是压缩包文件
     */
    public static boolean isArchive(File file) {
        switch (getFileSuffix(file).toLowerCase()) {
            case "zip":
            case "tar":
            case "gz":
            case "tgz":
            case "7z":
            case "rar":
                return true;
            default:
                return false;
        }
    }

    public static String getFileSuffix(File file) {
        if (file.isDirectory()) {
            return null;
        }
        return getFileSuffix(file.getName());
    }

    /**
     * get file suffix
     */
    public static String getFileSuffix(String filename) {
        return StringUtil.substringAfterLast(filename, ".");
    }

    /**
     * get file name without suffix
     */
    public static String getFileNameWithoutSuffix(File file) {
        if (file.isDirectory()) {
            return "";
        }
        return getFileNameWithoutSuffix(file.getName());
    }

    public static String getFileNameWithoutSuffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        return StringUtil.substringBeforeLast(fileName, ".");
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

    /**
     * 将文本写入到文件（utf-8编码）
     *
     * @param text   要写入的文本内容
     * @param path   文件路径
     * @param append 是否追加，如果不追加，会覆盖已有文件。
     */
    public static void writeTextToFile(String text, Path path, boolean append) throws IOException {
        createDir(path.getParent().toString());
        if (!append) {
            File file = path.toFile();
            if (file.exists()) {
                file.delete();
            }
        }
        Files.write(
                path,
                text.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE
        );
    }

    public static void copy(Path source, Path target, CopyOption... options) throws IOException {
        createDir(target.getParent().toString());
        Files.copy(source, target, options);
    }

    public static void main(String[] args) throws IOException {
        String from = "/Users/zane/data/wefe_file_upload_dir/hello.zip";
        String to = "/Users/zane/data/wefe_file_upload_dir/";
        moveFile(new File(from), to);
    }

    public static void moveFile(File file, String distDir) {
        moveFile(file, Paths.get(distDir));
    }

    public static void moveFile(File file, Path distDir) {
        // 文件已经在目标目录，不用移动。
        if (file.getParentFile().toPath().equals(distDir)) {
            return;
        }

        String fileName = file.getName();

        distDir.toFile().mkdirs();

        File distFile = distDir.resolve(fileName).toFile();
        if (distFile.exists()) {
            distFile.delete();
        }
        file.renameTo(distFile);
    }

    /**
     * Reading file contents
     *
     * @return String
     */
    public static List<String> readAllForLine(String path, String encoding) throws IOException {
        List<String> lineList = new ArrayList<>();
        BufferedReader in = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, encoding);
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                lineList.add(line);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (null != in) {
                in.close();
            }

        }
        return lineList;
    }
}
