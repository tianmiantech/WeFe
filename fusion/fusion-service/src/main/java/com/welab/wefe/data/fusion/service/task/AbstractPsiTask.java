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

package com.welab.wefe.data.fusion.service.task;

import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.actuator.rsapsi.AbstractPsiActuator;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;
import com.welab.wefe.data.fusion.service.enums.TaskStatus;
import com.welab.wefe.data.fusion.service.manager.ActuatorManager;
import com.welab.wefe.data.fusion.service.service.TaskService;

/**
 * @author hunter.zhao
 */
public abstract class AbstractPsiTask<T extends AbstractPsiActuator> extends AbstractTask<T> {

    public AbstractPsiTask(String businessId, T psi) {
        super(businessId, psi);
    }


    @Override
    protected PSIActuatorStatus status() {
        return actuator.status;
    }


    @Override
    public boolean isFinish() {
        if (PSIActuatorStatus.running != actuator.status
                && PSIActuatorStatus.uninitialized != actuator.status) {
            return true;
        } else if (System.currentTimeMillis() - actuator.lastLogTime > actuator.socketTimeout) {
            return true;
        }
        return false;
    }


    @Override
    public void close() throws Exception {
        TaskService taskService = Launcher.CONTEXT.getBean(TaskService.class);

        switch (actuator.status) {
            case success:
                taskService.updateByBusinessId(businessId, TaskStatus.Success, getFusionCount(), getSpend());
                break;
            case falsify:
            case running:
                taskService.updateByBusinessId(businessId, TaskStatus.Interrupt, getFusionCount(), getSpend());
                break;
            default:
                taskService.updateByBusinessId(businessId, TaskStatus.Failure, getFusionCount(), getSpend());
                break;
        }

        ActuatorManager.remove(businessId);
    }
}
