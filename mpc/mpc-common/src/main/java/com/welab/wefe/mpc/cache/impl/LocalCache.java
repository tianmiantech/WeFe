
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

package com.welab.wefe.mpc.cache.impl;

import com.welab.wefe.mpc.cache.CacheOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author eval
 * @Date 2021/12/15
 **/
public class LocalCache implements CacheOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalCache.class);

    private static Map<String, Map<String, String>> caches = new ConcurrentHashMap<>();

    @Override
    public void put(String key, String name, String value) {
        LOGGER.info("set key:{}, name:{}, value:{}", key, name, value);
        Map<String, String> keyValues = caches.getOrDefault(key, new ConcurrentHashMap<>());
        keyValues.put(name, value);
        caches.put(key, keyValues);
    }

    @Override
    public String get(String key, String name) {
        Map<String, String> keyValues = caches.getOrDefault(key, new ConcurrentHashMap<>());
        return keyValues.getOrDefault(name, null);
    }

    @Override
    public void delete(String key) {
        caches.remove(key);
    }
}
