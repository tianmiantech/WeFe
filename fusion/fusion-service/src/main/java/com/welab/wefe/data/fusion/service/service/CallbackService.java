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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.data.fusion.service.actuator.rsapsi.PsiClientActuator;
import com.welab.wefe.data.fusion.service.api.thirdparty.CallbackApi;
import com.welab.wefe.data.fusion.service.database.entity.TaskMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.TaskRepository;
import com.welab.wefe.data.fusion.service.enums.TaskStatus;
import com.welab.wefe.data.fusion.service.manager.TaskManager;
import com.welab.wefe.data.fusion.service.task.AbstractTask;
import com.welab.wefe.data.fusion.service.task.PsiClientTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.welab.wefe.common.StatusCode.DATA_NOT_FOUND;

/**
 * @author hunter.zhao
 */
@Service
public class CallbackService {
    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;


    @Autowired
    private PartnerService partnerService;


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
                TaskMySqlModel task = taskService.findByBusinessId(input.getBusinessId());
                task.setDataCount(input.getDataCount());
                task.setStatus(TaskStatus.Pending);
                taskRepository.save(task);

                break;
            case falsify:
                //Alignment data check invalid, shut down task
                AbstractTask job = TaskManager.get(input.getBusinessId());
                job.finish();
                break;
            case success:
                //Mission completed. Destroy task
                AbstractTask successTask = TaskManager.get(input.getBusinessId());
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
        if (TaskManager.get(businessId) != null) {
            return;
        }

        TaskMySqlModel task = taskService.findByBusinessId(businessId);
        if (task == null) {
            throw new StatusCodeWithException("businessId error:" + businessId, DATA_NOT_FOUND);
        }
        task.setStatus(TaskStatus.Running);
        taskRepository.save(task);

        /*
         * The other side is ready, we modify the task status and start client
         */
        AbstractTask client = new PsiClientTask(
                businessId,
                new PsiClientActuator(
                        businessId,
                        task.getDataCount(),
                        ip,
                        port,
                        task.getDataResourceId(),
                        task.isTrace(),
                        task.getTraceColumn()
                ));

        TaskManager.set(client);

        client.run();
    }
}
