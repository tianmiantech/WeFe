/**
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

package com.welab.wefe.data.fusion.service.manager;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.data.fusion.service.service.PartnerService;
import com.welab.wefe.data.fusion.service.service.TaskService;
import com.welab.wefe.data.fusion.service.task.AbstractTask;
import com.welab.wefe.fusion.core.actuator.AbstractActuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hunter
 */
public class ActuatorManager {
    public static final Logger LOG = LoggerFactory.getLogger(ActuatorManager.class);

    /**
     * taskId : task
     */
    private static final ConcurrentHashMap<String, AbstractActuator> ACTUATORS = new ConcurrentHashMap<>();

    public static AbstractActuator get(String businessId) {
        return ACTUATORS.get(businessId);
    }

    public static void set(AbstractActuator task) {

        String businessId = task.getBusinessId();
        if (ACTUATORS.containsKey(businessId)) {
            throw new RuntimeException(businessId + " This actuator already exists");
        }

        ACTUATORS.put(businessId, task);

    }

    public synchronized static void remove(String businessId) {

        ACTUATORS.remove(businessId);
    }

    public synchronized static List<JObject> dashboard() {

        List<JObject> list = new ArrayList<>();
        Object[] keys = ACTUATORS.keySet().toArray();

        for (Object key : keys) {

            JObject info = getTaskInfo(key.toString());

            if (info != null) {
                list.add(info);
            }
        }

        return list;
    }

    public static JObject getTaskInfo(String businessId) {
        AbstractActuator actuator = ACTUATORS.get(businessId);
        if (actuator == null) {
            return null;
        }

        JObject info = JObject
                .create()
                .append("business_id", businessId)
//                .append("fusion_count", actuator.getFusionCount())
//                .append("processed_count", actuator.getProcessedCount())
//                .append("data_count", actuator.getDataCount())
                .append("spend", actuator.getSpend())
                .append("stimated_spend", actuator.getEstimatedSpend())
                .append("progress", actuator.progress());

        return info;
    }

    /**
     * Number of tasks
     * @return Number of tasks
     */
    public static int size() {
        return ACTUATORS.size();
    }

    public static String ip() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            LOG.error("get ip error : " + e.getMessage());
        }

        return addr.getHostAddress();
    }
}
