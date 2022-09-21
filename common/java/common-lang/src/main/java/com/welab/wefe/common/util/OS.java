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
package com.welab.wefe.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author zane.luo
 * @date 2022/9/21
 */
public enum OS {

    mac,
    windows,
    linux,
    unknown;

    private static final Logger LOG = LoggerFactory.getLogger(OS.class);
    private static OS os = unknown;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            os = linux;
        } else if (osName.contains("mac") && osName.indexOf("os") > 0) {
            os = mac;
        } else if (osName.contains("windows")) {
            os = windows;
        }
    }

    public static OS get() {
        return os;
    }

    public static String execute(String command) {
        try {
            return get() == windows
                    ? executeCmdCommand(command)
                    : executeLinuxCommand(command);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
        return "";
    }

    public static String executeLinuxCommand(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }


    public static String executeCmdCommand(String command) throws Exception {
        Runtime runtime = Runtime.getRuntime();  //获取Runtime实例
        String[] commandArray = {"powershell", "/c", command};
        Process process = runtime.exec(commandArray);
        // 标准输入流（必须写在 waitFor 之前）
        String inStr = consumeCmdInputStream(process.getInputStream());
        // 标准错误流（必须写在 waitFor 之前）
        String errStr = consumeCmdInputStream(process.getErrorStream()); //若有错误信息则输出
        int proc = process.waitFor();
        if (proc == 0) {
            return inStr;
        } else {
            return errStr;
        }

    }

    /**
     * 读取 cmd 的输出
     */
    private static String consumeCmdInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = br.readLine()) != null) {
            stringBuilder
                    .append(line)
                    .append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }
}
