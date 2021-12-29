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

package com.welab.wefe.board.service.service.fusion;

import com.welab.wefe.board.service.api.fusion.task.AuditCallbackApi;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.database.repository.fusion.FusionTaskRepository;
import com.welab.wefe.board.service.fusion.actuator.ClientActuator;
import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import com.welab.wefe.fusion.core.utils.bf.BloomFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Date;

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

    @Autowired
    private BloomFilterService bloomFilterService;
    @Autowired
    private TableDataSetService tableDataSetService;

    /**
     * rsa-callback
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(AuditCallbackApi.Input input) throws StatusCodeWithException {
        switch (input.getAuditStatus()) {
            case agree:
                running(input.getBusinessId());
                break;
            case disagree:
                /**
                 *   The peer is a client. Change the task status and wait for confirmation
                 */
                FusionTaskMySqlModel task = fusionTaskService.findByBusinessId(input.getBusinessId());
                task.setStatus(FusionTaskStatus.Refuse);
                task.setComment(input.getAuditComment());
                fusionTaskRepository.save(task);

                break;
            default:
                throw new RuntimeException("Unexpected enumeration：" + input.getAuditStatus());
        }
    }

    /**
     * The other party's server-socket is ready, we start client
     *
     * @param businessId
     * @throws StatusCodeWithException
     */
    private void running(String businessId) throws StatusCodeWithException {
        FusionTaskMySqlModel task = fusionTaskService.findByBusinessIdAndStatus(businessId, FusionTaskStatus.Await);
        if (task == null) {
            throw new StatusCodeWithException("businessId error:" + businessId, DATA_NOT_FOUND);
        }
        task.setStatus(FusionTaskStatus.Running);
        fusionTaskRepository.save(task);

        switch (task.getAlgorithm()) {
            case RSA_PSI:
                psi(task);
                break;
            default:
                throw new RuntimeException("Unexpected enumeration values");
        }
    }


    /**
     * RSA-psi Algorithm to deal with
     */
    private void psi(FusionTaskMySqlModel task) throws StatusCodeWithException {
        switch (task.getPsiActuatorRole()) {
            case server:
                psiServer(task);
                break;
            case client:
                psiClient(task);
                break;
            default:
                break;
        }
    }

    /**
     * psi-client
     */
    private void psiClient(FusionTaskMySqlModel task) throws StatusCodeWithException {

        TableDataSetMysqlModel dataSet = tableDataSetService.findOneById(task.getDataResourceId());
        if (dataSet == null) {
            throw new StatusCodeWithException("No corresponding dataset was found", DATA_NOT_FOUND);
        }

        task.setStatus(FusionTaskStatus.Running);
        task.setUpdatedTime(new Date());
        fusionTaskRepository.save(task);

        ClientActuator client = new ClientActuator(
                task.getBusinessId(),
                task.getDataResourceId(),
                task.isTrace(),
                task.getTraceColumn(),
                task.getDstMemberId()
        );

        ActuatorManager.set(client);

        client.run();
    }


    /**
     * psi-server
     *
     * @param task
     * @throws StatusCodeWithException
     */
    private void psiServer(FusionTaskMySqlModel task) throws StatusCodeWithException {

        task.setStatus(FusionTaskStatus.Running);
        task.setUpdatedTime(new Date());
        fusionTaskRepository.save(task);

        /**
         * Find your party by task ID
         */
        BloomFilterMysqlModel bf = bloomFilterService.findOne(task.getDataResourceId());
        if (bf == null) {
            throw new StatusCodeWithException("Bloom filter not found", StatusCode.PARAMETER_VALUE_INVALID);
        }

        /**
         * Generate the corresponding task handler
         */
        ServerActuator server = new ServerActuator(
                task.getBusinessId(),
                BloomFilterUtils.readFrom(
                        Paths.get(bf.getStorageNamespace(), bf.getStorageResourceName()).toString()
                ),
                new BigInteger(bf.getRsaN()),
                new BigInteger(bf.getRsaE()),
                new BigInteger(bf.getRsaD())
        );

        ActuatorManager.set(server);

        server.run();
    }
}
