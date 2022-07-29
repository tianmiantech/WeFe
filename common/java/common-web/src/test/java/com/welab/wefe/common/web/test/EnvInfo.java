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

import com.welab.wefe.common.InformationSize;

import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zane
 * @date 2022/4/21
 */
public class EnvInfo {
    public static void main(String[] args) {

        TreeMap<String, String> systemProperties = new TreeMap<>();
        for (String name : System.getProperties().stringPropertyNames()) {
            if (name.startsWith("java") || name.startsWith("os")) {
                systemProperties.put(name, System.getProperty(name));
            }

        }

        LinkedHashMap<String, String> runtimeProperties = new LinkedHashMap<>();
        Runtime runtime = Runtime.getRuntime();
        runtimeProperties.put("thread_count", ManagementFactory.getThreadMXBean().getThreadCount() + "");
        runtimeProperties.put("jvm_max_memory", InformationSize.fromByte(runtime.maxMemory()).toString());
        runtimeProperties.put("jvm_use_memory", InformationSize.fromByte(runtime.totalMemory() - runtime.freeMemory()).toString());
        runtimeProperties.put("jvm_total_memory", InformationSize.fromByte(runtime.totalMemory()).toString());
        runtimeProperties.put("jvm_free_memory", InformationSize.fromByte(runtime.freeMemory()).toString());
        runtimeProperties.put("available_processors", String.valueOf(runtime.availableProcessors()));

        LinkedHashMap<String, String> envProperties = new LinkedHashMap<>();
        String[] keys = {"PWD", "USER"};
        Map<String, String> envMap = System.getenv();

        for (String key : keys) {
            envProperties.put("system_" + key.toLowerCase(), envMap.get(key));
        }

        envProperties.forEach((k, v) -> System.out.println(k + "=" + v));
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        runtimeProperties.forEach((k, v) -> System.out.println(k + "=" + v));
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        systemProperties.forEach((k, v) -> System.out.println(k + "=" + v));
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

    }
}
