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

import com.welab.wefe.mpc.cache.result.QueryDataResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author: eval
 * @Date: 2021-12-28
 **/
public class SecondDataResultCache implements QueryDataResult {
    private static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object query(String key) {
        System.out.println(key);
        Object value = cache.get(key);
        int tryCount = 0;
        while (value == null && tryCount < 30) {
            value = cache.get(key);
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tryCount++;
        }
        return value;
    }

    @Override
    public void save(String key, Object value) {
        cache.put(key, value);
    }
}
