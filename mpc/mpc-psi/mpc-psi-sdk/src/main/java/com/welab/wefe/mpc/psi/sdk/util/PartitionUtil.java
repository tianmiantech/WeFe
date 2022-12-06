/*
 * Copyright 2022 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.mpc.psi.sdk.util;

import java.util.*;

public class PartitionUtil {

    private PartitionUtil() {
    }

    /**
     * 分片
     */
    public static <T, K> List<Map<T, K>> partitionMap(Map<T, K> map, int numPartitions) {
        if (map == null) {
            throw new NullPointerException("The map must not be null");
        }

        if (numPartitions <= 0)
            throw new IllegalArgumentException("'numPartitions' must be greater than 0");

        List<T> list = new ArrayList<>(map.keySet());
        List<Map<T, K>> partitions = new ArrayList<>(numPartitions);
        int size = list.size();

        int partitionSize = (int) Math.ceil((double) size / numPartitions);
        for (int i = 0; i < numPartitions; i++) {
            int from = Math.min(i * partitionSize, size);
            int to = Math.min((i + 1) * partitionSize, size);
            Map<T, K> tmpMap = new HashMap<>();
            for (T elem : list.subList(from, to))
                tmpMap.put(elem, map.get(elem));
            partitions.add(tmpMap);
        }
        return partitions;
    }

    /**
     * 分片
     */
    public static <T> List<Set<T>> partitionSet(Set<T> set, int numPartitions) {
        if (set == null) {
            throw new NullPointerException("The set must not be null");
        }

        List<Set<T>> partitions = new ArrayList<>(numPartitions);
        for (int i = 0; i < numPartitions; i++)
            partitions.add(i, new HashSet<>());

        int size = set.size();
        int partitionSize = (int) Math.ceil((double) size / numPartitions);
        if (numPartitions <= 0)
            throw new IllegalArgumentException("'numPartitions' must be greater than 0");

        Iterator<T> iterator = set.iterator();
        int partitionToWrite = 0;
        int cont = 0;
        while (iterator.hasNext()) {
            partitions.get(partitionToWrite).add(iterator.next());
            cont++;
            if (cont >= partitionSize) {
                partitionToWrite++;
                cont = 0;
            }
        }
        return partitions;
    }
    
    /**
     * 分片
     */
    public static <T> List<Set<T>> partitionList(List<T> list, int numPartitions) {
        if (list == null) {
            throw new NullPointerException("The set must not be null");
        }

        List<Set<T>> partitions = new ArrayList<>(numPartitions);
        for (int i = 0; i < numPartitions; i++)
            partitions.add(i, new HashSet<>());

        int size = list.size();
        int partitionSize = (int) Math.ceil((double) size / numPartitions);
        if (numPartitions <= 0)
            throw new IllegalArgumentException("'numPartitions' must be greater than 0");

        Iterator<T> iterator = list.iterator();
        int partitionToWrite = 0;
        int cont = 0;
        while (iterator.hasNext()) {
            partitions.get(partitionToWrite).add(iterator.next());
            cont++;
            if (cont >= partitionSize) {
                partitionToWrite++;
                cont = 0;
            }
        }
        return partitions;
    }
}
