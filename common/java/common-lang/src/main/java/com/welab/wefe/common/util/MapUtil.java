/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.util;

import java.util.Collection;
import java.util.Map;

/**
 * @author zane
 * @date 2021/11/15
 */
public class MapUtil {

    /**
     * 统计集合中各元素的数量
     */
    public static <T> void statistics(Map<T, Integer> map, Collection<T> collection) {
        collection.forEach(x -> {
            increment(map, x);
        });
    }

    public static <T> void increment(Map<T, Integer> map, T key) {
        Integer number = map.get(key);
        if (number == null) {
            number = 0;
        }
        number++;
        map.put(key, number);
    }

}
