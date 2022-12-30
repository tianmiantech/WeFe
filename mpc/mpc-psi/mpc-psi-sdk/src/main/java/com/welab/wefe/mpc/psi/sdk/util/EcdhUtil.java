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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public class EcdhUtil {

    public static Map<Long, String> convert2Map(List<String> clientIds) {
        List<Set<String>> clientDatasetPartitions = PartitionUtil.partitionList(clientIds, 4);
        Map<Long, String> result = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(clientDatasetPartitions.size());
        for (Set<String> partition : clientDatasetPartitions) {
            executorService.submit(() -> {
                for (String value : partition) {
                    if (StringUtils.isNotBlank(value) && value.contains("#")) {
                        result.put(Long.valueOf(value.split("#")[0]), value.split("#")[1]);
                    }
                }
            });
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(10 * 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            executorService.shutdown();
        }
        return result;
    }

    public static List<String> convert2List(Map<Long, String> inputMap) {
        List<String> result = new CopyOnWriteArrayList<>();

        List<Map<Long, String>> partitionList = PartitionUtil.partitionMap(inputMap, 4);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());

        for (Map<Long, String> partition : partitionList) {
            executorService.submit(() -> {
                for (Map.Entry<Long, String> entry : partition.entrySet()) {
                    result.add(entry.getKey() + "#" + entry.getValue());
                }
            });
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(10 * 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            executorService.shutdown();
        }
        return result;
    }
}
