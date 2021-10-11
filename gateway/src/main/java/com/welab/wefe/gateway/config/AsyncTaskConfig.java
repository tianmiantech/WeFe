/**
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

package com.welab.wefe.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Spring boot asynchronous task thread pool configuration
 *
 * @author aaron.li
 **/
@Configuration
public class AsyncTaskConfig {
    @Autowired
    private ConfigProperties configProperties;

    @Bean
    public AsyncTaskExecutor transferMetaDataAsyncExecutor() {
        return buildExecutor("transferMetaDataAsyncExecutor", configProperties.getDataSinkCorePoolSize(), configProperties.getDataSinkCorePoolSize() * 100);
    }


    private AsyncTaskExecutor buildExecutor(String prefix, int coreSize, int poolSize) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(prefix + "-Thread-");
        taskExecutor.setCorePoolSize(coreSize);
        taskExecutor.setMaxPoolSize(poolSize);
        taskExecutor.setKeepAliveSeconds(120);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return taskExecutor;
    }
}
