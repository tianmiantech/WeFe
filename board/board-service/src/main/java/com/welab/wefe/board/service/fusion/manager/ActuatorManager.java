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

package com.welab.wefe.board.service.fusion.manager;

import com.google.common.collect.Lists;
import com.welab.wefe.board.service.database.entity.fusion.FusionActuatorInfoMySqlModel;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.database.repository.fusion.FusionActuatorInfoRepository;
import com.welab.wefe.board.service.fusion.actuator.ClientActuator;
import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.fusion.core.actuator.AbstractActuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final FusionActuatorInfoRepository fusionActuatorInfoRepository;
    private static final FusionTaskService fusionTaskService;

    static {
        fusionActuatorInfoRepository = Launcher.CONTEXT.getBean(FusionActuatorInfoRepository.class);
        fusionTaskService = Launcher.CONTEXT.getBean(FusionTaskService.class);
    }

    public static void set(AbstractActuator task) {

        String businessId = task.getBusinessId();
        if (ACTUATORS.containsKey(businessId)) {
            throw new RuntimeException(businessId + " This actuator already exists");
        }

        LOG.info("Set actuator successfully, businessId is {}", businessId);
        ACTUATORS.put(businessId, task);

    }

    public synchronized static void remove(String businessId) {

        ACTUATORS.remove(businessId);
    }

    public synchronized static List<JObject> dashboard() throws StatusCodeWithException {

        List<JObject> list = Lists.newArrayList();
        Object[] keys = ACTUATORS.keySet().toArray();

        for (Object key : keys) {

            JObject info = getTaskInfo(key.toString());

            if (info != null) {
                list.add(info);
            }
        }

        return list;
    }

    public static JObject getTaskInfo(String businessId) throws StatusCodeWithException {
        AbstractActuator actuator = ACTUATORS.get(businessId);
        if (actuator != null) {
            return JObject
                    .create()
                    .append("business_id", businessId)
                    .append("fusion_count", actuator.getFusionCount())
                    .append("processed_count", actuator.getProcessedCount())
                    .append("data_count", actuator.getDataCount())
                    .append("spend", actuator.getSpend())
                    .append("status", "Running")
                    .append("estimated_spend", actuator.getEstimatedSpend())
                    .append("progress", actuator.progress());
        }

        FusionTaskMySqlModel model = fusionTaskService.findByBusinessId(businessId);
        if (model != null) {
            return JObject
                    .create()
                    .append("business_id", businessId)
                    .append("fusion_count", model.getFusionCount())
                    .append("processed_count", model.getProcessedCount())
                    .append("data_count", model.getDataCount())
                    .append("spend", model.getSpend())
                    .append("status", model.getStatus())
                    .append("progress", model.getProcessedCount().doubleValue() / model.getDataCount().doubleValue());
        }

        return null;
    }

    /**
     * Number of tasks
     *
     * @return Number of tasks
     */
    public static int size() {
        return ACTUATORS.size();
    }

    public static void refresh(AbstractActuator actuator) {
        if (actuator instanceof ClientActuator) {
            FusionActuatorInfoMySqlModel info = new FusionActuatorInfoMySqlModel();
            info.setType(actuator.getClass().getSimpleName());
            info.setBusinessId(actuator.getBusinessId());
            info.setProgress(((ClientActuator) actuator).currentIndex);
            fusionActuatorInfoRepository.save(info);
        } else if (actuator instanceof ServerActuator) {
            FusionActuatorInfoMySqlModel info = new FusionActuatorInfoMySqlModel();
            info.setType(actuator.getClass().getSimpleName());
            info.setBusinessId(actuator.getBusinessId());
            fusionActuatorInfoRepository.save(info);
        }
    }

    public static void main(String[] args) {
        AbstractActuator actuator = new AbstractActuator("1") {
            @Override
            public void close() throws Exception {

            }

            @Override
            public boolean isFinish() {
                return false;
            }

            @Override
            public void init() throws StatusCodeWithException {

            }

            @Override
            public void fusion() throws StatusCodeWithException {

            }

            @Override
            public void dump(List<JObject> fruit) {

            }
        };


    }
}
