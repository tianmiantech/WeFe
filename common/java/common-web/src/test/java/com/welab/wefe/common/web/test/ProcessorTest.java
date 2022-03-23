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
package com.welab.wefe.common.web.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author zane
 * @date 2022/3/23
 */
public class ProcessorTest {
    public static void main(String[] args) throws IOException {
        //ProcessBuilder processBuilder = new ProcessBuilder("cat /Users/zane/test.log| grep -i '^[[:space:]]*at ' -B 5 -A 5 | cut -c 1-300 | tail -100");
        ProcessBuilder processBuilder = new ProcessBuilder()
                .command("sh", "-c", "cat /Users/zane/test.log| grep -i '^[[:space:]]*at ' -B 5 -A 5 | cut -c 1-300 | tail -100");

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.lineSeparator());
        }
        String result = builder.toString();
        System.out.println("----------------------------------------");
        System.out.println(result);
        System.out.println("----------------------------------------");
    }
}
