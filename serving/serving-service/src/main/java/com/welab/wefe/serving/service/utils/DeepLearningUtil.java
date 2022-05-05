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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hunter.zhao
 * @date 2022/3/23
 */
public class DeepLearningUtil {
//    public static void main(String[] args) throws IOException {
//        StringBuilder stringBuilder = new StringBuilder();
//        BufferedReader bufferedReader = null;
//
//        try {
//
//
//            String shellCommand = "python3 test_client.py";
//
//            stringBuilder.append("准备执行Shell命令 ").append(shellCommand).append(" /r/n");
//
//            String[] cmd = {"/bin/sh", "-c", shellCommand};
//            Process pid = Runtime.getRuntime().exec(cmd);
//
//            if (pid != null) {
//                stringBuilder.append("进程号：").append(pid.toString()).append("/r/n");
//                //bufferedReader用于读取Shell的输出内容
//                bufferedReader = new BufferedReader(new InputStreamReader(pid.getInputStream()), 1024);
//                pid.waitFor();
//            } else {
//                stringBuilder.append("没有pid/r/n");
//            }
//            stringBuilder.append("Shell命令执行完毕/r/n执行结果为：/r/n");
//            String line = null;
//            //读取Shell的输出内容，并添加到stringBuffer中
//            while (bufferedReader != null &&
//                    (line = bufferedReader.readLine()) != null) {
//                stringBuilder.append(line).append("/r/n");
//            }
//        } catch (Exception ioe) {
//            stringBuilder.append("执行Shell命令时发生异常：/r/n").append(ioe.getMessage()).append("/r/n");
//        } finally {
//            if (bufferedReader != null) {
////                OutputStreamWriter outputStreamWriter = null;
//                try {
//                    bufferedReader.close();
//                    //将Shell的执行情况输出到日志文件中
////                    OutputStream outputStream = new FileOutputStream(executeShellLogFile);
////                    outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
////                    outputStreamWriter.write(stringBuilder.toString());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
////                    outputStreamWriter.close();
//                }
//            }
//
//            System.out.println(stringBuilder);
//        }
//    }


    public static String callPaddleServing(String lableListPath, String imagePath, String output) throws IOException {
        // 创建命令集合
        List<String> commandList = new ArrayList<String>();

        commandList.add("python3");
        commandList.add("test_client.py");
        commandList.add(lableListPath);
        commandList.add(imagePath);
        commandList.add(output);
        // ProcessBuilder是一个用于创建操作系统进程的类，它的start()方法用于启动一个进行
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        // 启动进程
        Process process = processBuilder.start();
        // 解析输出
        return convertStreamToStr(process.getInputStream());
    }

    public void callShellCommand(){

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String cmd = "python3 -m paddle_serving_server.serve --model serving_server --port 9393";

        // 创建命令集合
        List<String> commandList = new ArrayList<String>();
//        commandList.add("/bin/sh");
//        commandList.add("-c");  // 执行结束后关闭
//        commandList.add("echo");
//        commandList.add("hello");
//        commandList.add("cmd");

        commandList.add("python3");
        commandList.add("test_client.py");
        // ProcessBuilder是一个用于创建操作系统进程的类，它的start()方法用于启动一个进行
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        // 启动进程
        Process process = processBuilder.start();
        process.waitFor();

        // 解析输出
        String result1 = convertStreamToStr(process.getInputStream());
        System.out.println(result1);
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

