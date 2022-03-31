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

package com.welab.wefe.mpc.cache.intermediate.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.welab.wefe.mpc.cache.intermediate.CacheOperation;

import java.util.concurrent.TimeUnit;

/**
 * @author eval
 */
public class LocalIntermediateCache implements CacheOperation {

    private static final Cache<String, Cache<String, Object>> caches = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    @Override
    public synchronized void save(String key, String name, Object value) {
        Cache cache = caches.getIfPresent(key);
        if (cache == null) {
            cache = CacheBuilder.newBuilder().build();
        }
        cache.put(name, value);
        caches.put(key, cache);
    }

    @Override
    public synchronized Object get(String key, String name) {
        Cache cache = caches.getIfPresent(key);
        if (cache != null) {
            return cache.getIfPresent(name);
        }
        return null;
    }

    @Override
    public void delete(String key) {
    }
}
