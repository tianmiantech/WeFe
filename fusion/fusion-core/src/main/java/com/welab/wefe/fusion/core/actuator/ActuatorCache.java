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
package com.welab.wefe.fusion.core.actuator;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hunter.zhao
 */
public class ActuatorCache {
    protected final static ConcurrentHashMap<String, AbstractActuator> ACTUATOR_CACHE = new ConcurrentHashMap<>();

    public static AbstractActuator get(String businessId) {
        return ACTUATOR_CACHE.get(businessId);
    }


    /**
     * taskId : task
     */

    public static void set(AbstractActuator task) {

        String businessId = task.getBusinessId();
        if (ACTUATOR_CACHE.containsKey(businessId)) {
            throw new RuntimeException(businessId + " This actuator already exists");
        }

        ACTUATOR_CACHE.put(businessId, task);

    }

    public synchronized static void remove(String businessId) {
        ACTUATOR_CACHE.remove(businessId);
    }


    /**
     * Number of tasks
     *
     * @return Number of tasks
     */
    public static int size() {
        return ACTUATOR_CACHE.size();
    }

    public static boolean isReady(String businessId) {
        return get(businessId) != null;
    }
}
