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

package com.welab.wefe.common.data.storage;

import com.welab.wefe.common.data.storage.config.DruidConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author yuxin.zhang
 */
public class StorageManager {
    private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

    private AnnotationConfigApplicationContext context;

    private static class SingletonClassInstance {
        private static final StorageManager INSTANCE = new StorageManager();
    }

    public static StorageManager getInstance() {
        return StorageManager.SingletonClassInstance.INSTANCE;
    }

    private StorageManager() {
    }

    public void init() {
        LOG.info("start init storage...");
        context = new AnnotationConfigApplicationContext(DruidConfig.class);
        context.start();
        LOG.info("end init storage...");
    }

    public <T> T getRepo(Class<T> t) {
        return context.getBean(t);
    }

    public <T> T getRepo(String beanName) {
        return (T) context.getBean(beanName);
    }

    public <T> T getBean(String beanName) {
        return getRepo(beanName);
    }

    public static <T> T getRepository(Class<T> t) {
        return getInstance().getRepo(t);
    }

}
