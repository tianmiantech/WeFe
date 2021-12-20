
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

package com.welab.wefe.mpc.pir.server.data.impl;

import com.welab.wefe.mpc.pir.server.data.QueryResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author eval
 * @Date 2021/12/15
 **/
public class LocalResultCache implements QueryResult {

    private static Map<String, Map<String, String>> caches = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> query(String uuid) {
        Map<String, String> result = caches.getOrDefault(uuid, null);
        caches.remove(uuid);
        return result;
    }

    @Override
    public void put(String uuid, Map<String, String> result) {
        caches.put(uuid, result);
    }
}
