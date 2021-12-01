/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.service.fusion;

import com.welab.wefe.board.service.api.fusion.CallbackApi;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.database.repository.fusion.FusionTaskRepository;
import com.welab.wefe.board.service.fusion.actuator.ClientActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.fusion.core.actuator.AbstractActuator;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.welab.wefe.common.StatusCode.DATA_NOT_FOUND;

/**
 * @author hunter.zhao
 */
@Service
public class CallbackService {
    @Autowired
    private FusionTaskService fusionTaskService;

    @Autowired
    private FusionTaskRepository fusionTaskRepository;


    /**
     * rsa-callback
     */
    public void callback(CallbackApi.Input input) throws StatusCodeWithException {
        switch (input.getType()) {
            case running:
                running(input.getBusinessId(), input.getSocketIp(), input.getSocketPort());
                break;
            case init:
                /**
                 *   The peer is a client. Change the task status and wait for confirmation
                 */
                FusionTaskMySqlModel task = fusionTaskService.findByBusinessId(input.getBusinessId());
               // task.setDataCount(input.getDataCount());
                task.setStatus(FusionTaskStatus.Pending);
                fusionTaskRepository.save(task);

                break;
            case falsify:
                //Alignment data check invalid, shut down task
                AbstractActuator job = ActuatorManager.get(input.getBusinessId());
                job.finish();
                break;
            case success:
                //Mission completed. Destroy task
                AbstractActuator successTask = ActuatorManager.get(input.getBusinessId());
                successTask.finish();

                break;
            default:
                throw new RuntimeException("Unexpected enumeration：" + input.getType());
        }
    }

    /**
     * The other party's server-socket is ready, we start client
     *
     * @param businessId
     * @throws StatusCodeWithException
     */
    private void running(String businessId, String ip, int port) throws StatusCodeWithException {
        if (ActuatorManager.get(businessId) != null) {
            return;
        }

        FusionTaskMySqlModel task = fusionTaskService.findByBusinessId(businessId);
        if (task == null) {
            throw new StatusCodeWithException("businessId error:" + businessId, DATA_NOT_FOUND);
        }
        task.setStatus(FusionTaskStatus.Running);
        fusionTaskRepository.save(task);

        /*
         * The other side is ready, we modify the task status and start client
         */
        ClientActuator client = new ClientActuator(
                businessId,
                task.getDataResourceId(),
                task.isTrace(),
                task.getTraceColumn()
        );

//        ActuatorManager.set(client);

        client.run();
    }
}
