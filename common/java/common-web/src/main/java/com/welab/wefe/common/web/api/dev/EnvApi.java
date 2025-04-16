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
package com.welab.wefe.common.web.api.dev;

import com.welab.wefe.common.InformationSize;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.config.CommonConfig;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.env.EnvBranch;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zane
 * @date 2022/4/7
 */
@Api(path = "env", name = "环境变量")
public class EnvApi extends AbstractNoneInputApi<EnvApi.Output> {
    @Autowired
    private CommonConfig config;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        Output output = new Output();

        for (String name : System.getProperties().stringPropertyNames()) {
            if (name.startsWith("java") || name.startsWith("os")) {
                output.systemProperties.put(name, System.getProperty(name));
            }

        }

        Runtime runtime = Runtime.getRuntime();
        output.runtimeProperties.put("thread_count", ManagementFactory.getThreadMXBean().getThreadCount() + "");
        output.runtimeProperties.put("jvm_max_memory", InformationSize.fromByte(runtime.maxMemory()).toString());
        output.runtimeProperties.put("jvm_use_memory", InformationSize.fromByte(runtime.totalMemory() - runtime.freeMemory()).toString());
        output.runtimeProperties.put("jvm_total_memory", InformationSize.fromByte(runtime.totalMemory()).toString());
        output.runtimeProperties.put("jvm_free_memory", InformationSize.fromByte(runtime.freeMemory()).toString());
        output.runtimeProperties.put("available_processors", String.valueOf(runtime.availableProcessors()));

        String[] keys = {"PWD", "USER"};
        Map<String, String> envMap = System.getenv();

        for (String key : keys) {
            output.envProperties.put("system_" + key.toLowerCase(), envMap.get(key));
        }

        output.envProperties.put("env_name", config.getEnvName().name());
        output.envProperties.put("env_branch", config.getEnvBranch().name());
        output.envProperties.put("is_demo", (config.getEnvBranch() == EnvBranch.online_demo) + "");

        return success(output);
    }

    public class Output {
        @Check(name = "环境信息")
        public LinkedHashMap<String, String> envProperties = new LinkedHashMap<>();
        @Check(name = "系统信息")
        public TreeMap<String, String> systemProperties = new TreeMap<>();
        @Check(name = "运行时信息")
        public LinkedHashMap<String, String> runtimeProperties = new LinkedHashMap<>();
    }
}
