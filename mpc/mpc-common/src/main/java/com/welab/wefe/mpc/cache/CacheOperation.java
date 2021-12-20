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

package com.welab.wefe.mpc.cache;

/**
 * @Author eval
 * @Date 2021/12/14
 **/
public interface CacheOperation {
    /**
     * 存储数据
     *
     * @param key   任务ID
     * @param name  数据name
     * @param value 值
     */
    void put(String key, String name, String value);

    /**
     * 获取数据
     *
     * @param key  任务ID
     * @param name 数据name
     * @return
     */
    String get(String key, String name);

    /**
     * 删除 key值
     *
     * @param key
     */
    void delete(String key);
}
