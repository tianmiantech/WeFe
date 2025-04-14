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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.data.fusion.service.actuator.rsapsi.PsiClientActuator;
import com.welab.wefe.data.fusion.service.api.thirdparty.CallbackApi;
import com.welab.wefe.data.fusion.service.database.entity.PartnerMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.TaskMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.TaskRepository;
import com.welab.wefe.data.fusion.service.dto.entity.PartnerOutputModel;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;
import com.welab.wefe.data.fusion.service.enums.TaskStatus;
import com.welab.wefe.data.fusion.service.manager.ActuatorManager;
import com.welab.wefe.data.fusion.service.task.AbstractTask;
import com.welab.wefe.data.fusion.service.task.PsiClientTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.welab.wefe.data.fusion.service.task.PsiServerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;

import static com.welab.wefe.common.StatusCode.DATA_NOT_FOUND;

/**
 * @author hunter.zhao
 */
@Service
public class CallbackService {
    protected final static Logger LOG = LoggerFactory.getLogger(CallbackService.class);

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
            case success:
                //Mission completed. Destroy task
                AbstractTask successTask = ActuatorManager.get(input.getBusinessId());
                successTask.finish();

                break;
            case stop:
                stop(input.getBusinessId());
                break;
            default:
                throw new RuntimeException("意料之外的枚举值：" + input.getType());
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

        TaskMySqlModel task = taskService.findByBusinessId(businessId);
        if (task == null) {
            throw new StatusCodeWithException(DATA_NOT_FOUND, "该任务不存在，请检查入参:" + businessId);
        }
        PartnerMySqlModel partnerMySqlModel = partnerService.findByPartnerId(task.getPartnerMemberId());
        if(null == partnerMySqlModel){
            LOG.info("Find by partner id is empty, partner member id: {}", task.getPartnerMemberId());
        }
        if (null != partnerMySqlModel && StringUtil.isNotEmpty(partnerMySqlModel.getBaseUrl())) {
            LOG.info("Find by partner info by id: {}, url: {}", task.getPartnerMemberId(), partnerMySqlModel.getBaseUrl());
            String host = getUrlHost(partnerMySqlModel.getBaseUrl());
            ip = StringUtil.isNotEmpty(host) ? host : ip;
        }

        task.setStatus(TaskStatus.Running);
        taskRepository.save(task);

        PartnerOutputModel partnerModel = null;
        PartnerMySqlModel partner = partnerService.findByPartnerId(task.getPartnerMemberId());
        if (partner != null) {
            partnerModel = ModelMapper.map(partner, PartnerOutputModel.class);
        }
        /*
         * The other side is ready, we modify the task status and start client
         */
        AbstractTask client = new PsiClientTask(businessId, new PsiClientActuator(businessId, task.getDataCount(), ip,
                port, task.getDataResourceId(), task.isTrace(), task.getTraceColumn(), partnerModel));

        ActuatorManager.set(client);

        client.run();
    }

    /**
     * The other party's server-socket is ready, we start client
     *
     * @param businessId
     * @throws StatusCodeWithException
     */
    private void stop(String businessId) throws StatusCodeWithException {
        PsiServerTask serverTask = (PsiServerTask)ActuatorManager.get(businessId);
        try {
            serverTask.actuator.status = PSIActuatorStatus.exception;
            serverTask.close();
        } catch (Exception e) {
            StatusCode.SYSTEM_ERROR.throwException(e);
        }
    }


    /**
     * 提取url中的域名
     */
    private String getUrlHost(String urlStr) {
        try {
            return new URL(urlStr).getHost();
        } catch (Exception e) {
            LOG.error("Get url host exception: ", e);
        }
        return null;
    }
}
