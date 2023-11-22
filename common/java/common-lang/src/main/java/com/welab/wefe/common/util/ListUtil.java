/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.util;

import com.welab.wefe.common.function.ConsumerWithException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * @author Zane
 */
public class ListUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ListUtil.class);

    /**
     * 使用并发的方式遍历列表
     */
    public static <T> Exception parallelEach(Collection<T> list, ConsumerWithException<T> consumer) {
        AtomicReference<Exception> error = new AtomicReference<>();
        list
                .parallelStream()
                .forEach(x -> {
                    try {
                        consumer.accept(x);
                    } catch (Exception e) {
                        error.set(e);
                        log(e);
                    }
                });
        return error.get();
    }

    public static <T> long sumLong(Collection<T> list, ToLongFunction<T> toLongFunction) {
        if (list == null) {
            return 0L;
        }
        return list.stream().mapToLong(toLongFunction).sum();
    }

    public static <T> int sumInt(Collection<T> list, ToIntFunction<T> toIntFunction) {
        if (list == null) {
            return 0;
        }
        return list.stream().mapToInt(toIntFunction).sum();
    }

    /**
     * Moves the position of an element in a List
     */
    public static <T> void moveElement(List<T> list, int from, int to) {
        if (list == null || list.isEmpty()) {
            return;
        }

        if (from == to) {
            return;
        }

        T element = list.get(from);
        list.remove(from);
        list.add(to, element);
    }

    private static void log(Exception e) {
        LOG.error(e.getClass() + " " + e.getMessage(), e);
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5));

        moveElement(list, 3, 1);
        System.out.println(list);
        moveElement(list, 2, 0);
        System.out.println(list);
    }
}
