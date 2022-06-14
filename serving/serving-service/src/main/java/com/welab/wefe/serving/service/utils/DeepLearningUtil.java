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
package com.welab.wefe.serving.service.utils;

import org.apache.commons.compress.utils.Lists;

import java.io.*;
import java.util.List;

/**
 * @author hunter.zhao
 * @date 2022/3/23
 */
public class DeepLearningUtil {

    public static String callPaddleServing(String imagePath, String labelListPath, String output, String wordDir) throws IOException {
        start(wordDir);

        execute(imagePath, labelListPath, output, wordDir);

        kill(wordDir);

        return "";
    }

    private static void kill(String wordDir) throws IOException {
        executeShell(wordDir, "sh kill.sh");
    }

    private static void executeShell(String wordDir, String commandShell) throws IOException {

        List<String> commandList = Lists.newArrayList();
        System.out.println("执行命令：" + commandShell);
        commandList.add("/bin/sh");
        commandList.add("-c");
        commandList.add(commandShell);

        // ProcessBuilder是一个用于创建操作系统进程的类，它的start()方法用于启动一个进行
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);

        processBuilder.directory(new File(wordDir));
        // 启动进程
        Process process3 = processBuilder.start();
        // 解析输出
        convertStreamToStr(process3.getErrorStream());

        convertStreamToStr(process3.getInputStream());
    }

    private static void execute(String imagePath, String labelListPath, String output, String wordDir) throws IOException {
        String exe = "sh execute.sh test_client.py " + imagePath + " " + labelListPath + " " + output;
        executeShell(wordDir, exe);
    }

    private static void start(String wordDir) throws IOException {
        executeShell(wordDir, "sh start_server.sh");
    }

    public static String convertStreamToStr(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
}

