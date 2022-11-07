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

package com.welab.wefe.board.service.fusion.manager;

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
import com.welab.wefe.fusion.core.actuator.ActuatorCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hunter
 */
public class ActuatorManager extends ActuatorCache {
    public static final Logger LOG = LoggerFactory.getLogger(ActuatorManager.class);


    private static final FusionActuatorInfoRepository fusionActuatorInfoRepository;
    private static final FusionTaskService fusionTaskService;

    static {
        fusionActuatorInfoRepository = Launcher.CONTEXT.getBean(FusionActuatorInfoRepository.class);
        fusionTaskService = Launcher.CONTEXT.getBean(FusionTaskService.class);
    }


    public static JObject getTaskInfo(String businessId) throws StatusCodeWithException {
        AbstractActuator actuator = ACTUATOR_CACHE.get(businessId);
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
                    .append("progress",
                            Double.valueOf(
                                    model.getProcessedCount().doubleValue() / model.getDataCount() * 100
                            ).intValue()
                    );
        }

        return null;
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
}
