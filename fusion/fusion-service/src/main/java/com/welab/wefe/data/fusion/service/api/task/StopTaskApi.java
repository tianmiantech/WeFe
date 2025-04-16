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

package com.welab.wefe.data.fusion.service.api.task;

import static com.welab.wefe.common.StatusCode.DATA_NOT_FOUND;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.actuator.rsapsi.AbstractPsiActuator;
import com.welab.wefe.data.fusion.service.actuator.rsapsi.PsiClientActuator;
import com.welab.wefe.data.fusion.service.actuator.rsapsi.PsiServerActuator;
import com.welab.wefe.data.fusion.service.database.entity.TaskMySqlModel;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorRole;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;
import com.welab.wefe.data.fusion.service.enums.TaskStatus;
import com.welab.wefe.data.fusion.service.manager.ActuatorManager;
import com.welab.wefe.data.fusion.service.service.TaskService;

/**
 * @author hunter.zhao
 */
@Api(path = "task/stop", name = "暂停任务", desc = "暂停任务", login = true)
public class StopTaskApi extends AbstractApi<StopTaskApi.Input, EnumSet<TaskStatus>> {
    @Autowired
    private TaskService taskService;

    @Override
    protected ApiResult<EnumSet<TaskStatus>> handle(Input input) throws Exception {
        TaskMySqlModel task = taskService.findByBusinessId(input.getBusinessId());
        if (task == null) {
            throw new StatusCodeWithException(DATA_NOT_FOUND, "任务不存在！");
        }
        if (PSIActuatorRole.client.equals(task.getPsiActuatorRole())) {
            PsiClientActuator act = (PsiClientActuator)ActuatorManager.get(input.getBusinessId()).actuator;
            act.status = PSIActuatorStatus.exception;
            LOG.info("change client actuator.status = exception");
        }
        else {
            PsiServerActuator act = (PsiServerActuator)ActuatorManager.get(input.getBusinessId()).actuator;
            act.status = PSIActuatorStatus.exception;
            LOG.info("change server actuator.status = exception");
        }
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "任务Id", require = true)
        private String businessId;

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

    }
}
