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
public class TaskManager {
    public static final Logger LOG = LoggerFactory.getLogger(TaskManager.class);

    /**
     * taskId : task
     */
    private static final ConcurrentHashMap<String, AbstractTask> TASKS = new ConcurrentHashMap<>();

    static TaskService taskService;

    static PartnerService partnerService;

    public static AbstractTask get(String businessId) {
        return TASKS.get(businessId);
    }

    public static void set(AbstractTask task) {

        String businessId = task.getBusinessId();
        if (TASKS.containsKey(businessId)) {
            throw new RuntimeException(businessId + "该 job 已存在，快看下代码是哪里搞错了！");
        }

        TASKS.put(businessId, task);

    }

    public synchronized static void remove(String businessId) {

        TASKS.remove(businessId);
    }

    public synchronized static List<JObject> dashboard() {

        List<JObject> list = new ArrayList<>();
        Object[] keys = TASKS.keySet().toArray();

        for (Object key : keys) {

            JObject info = getTaskInfo(key.toString());

            if (info != null) {
                list.add(info);
            }
        }

        return list;
    }

    public static JObject getTaskInfo(String businessId) {
        AbstractTask task = TASKS.get(businessId);
        if (task == null) {
            return null;
        }

        JObject info = JObject
                .create()
                .append("business_id", businessId)
                .append("fusion_count", task.getFusionCount())
                .append("processed_count", task.getProcessedCount())
                .append("data_count", task.getDataCount())
                .append("spend", task.getSpend())
                .append("stimated_spend", task.getEstimatedSpend())
                .append("progress", task.progress());

        return info;
    }

    /**
     * Remove a stalled task
     */
//    public synchronized static void removeCrashedTasks() {
//
//        Object[] keys = TASKS.keySet().toArray();
//
//        for (Object key : keys) {
//
//            String taskId = key.toString();
//            AbstractTask<?> task = get(taskId);
//
//            if (task == null) {
//                continue;
//            }
//
//
//            if (task.userLost()) {
//                try {
//                    task.close(TaskStatus.discard, "user lost, finish by client.");
//                    TASKS.remove(taskId);
//                } catch (Exception e) {
//                    LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
//                }
//            } else if (task.crashed()) {
//
//                try {
//                    task.close(TaskStatus.crashed, "task crashed, finish by client.");
//                    TASKS.remove(taskId);
//                } catch (Exception e) {
//                    LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
//                }
//            }
//
//
//        }
//
//    }

    /**
     * Number of tasks
     * @return Number of tasks
     */
    public static int size() {
        return TASKS.size();
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
