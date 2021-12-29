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

package com.welab.wefe.mpc.cache.result.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.welab.wefe.mpc.cache.result.QueryDataResult;

import java.util.concurrent.TimeUnit;

/**
 * @author eval
 */
public class QueryDataResultImpl implements QueryDataResult {

    private final static Cache<String, Object> resultCache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .build();

    @Override
    public Object query(String key) {
        Object value = null;
        while (value == null) {
            value = resultCache.getIfPresent(key);
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    @Override
    public void save(String key, Object value) {
        if (value != null) {
            resultCache.put(key, value);
        }
    }
}
