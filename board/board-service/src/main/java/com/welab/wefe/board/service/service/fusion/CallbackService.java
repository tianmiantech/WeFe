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

import com.welab.wefe.board.service.api.fusion.actuator.CallbackApi;
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
import com.welab.wefe.fusion.core.actuator.AbstractActuator;
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
    private BloomFilterService bloomfilterService;
    @Autowired
    private TableDataSetService tableDataSetService;

    /**
     * rsa-callback
     */
    @Transactional(rollbackFor = Exception.class)
    public void callback(CallbackApi.Input input) throws StatusCodeWithException {
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
//            case falsify:
//                //Alignment data check invalid, shut down task
//                AbstractActuator job = ActuatorManager.get(input.getBusinessId());
//                job.finish();
//                break;
//            case success:
//                //Mission completed. Destroy task
//                AbstractActuator successTask = ActuatorManager.get(input.getBusinessId());
//                successTask.finish();

//                break;
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
//        if (ActuatorManager.get(businessId) != null) {
//            return;
//        }

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

        AbstractActuator actuator = ActuatorManager.get(businessId);
        actuator.run();
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

        task.setStatus(FusionTaskStatus.Ready);
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
        BloomFilterMysqlModel bf = bloomfilterService.findOne(task.getDataResourceId());
        if (bf == null) {
            throw new StatusCodeWithException("Bloom filter not found", StatusCode.PARAMETER_VALUE_INVALID);
        }

//        BloomFilterMysqlModel bf = new BloomFilterMysqlModel();
//        BigInteger N = new BigInteger("146167375152084793681454802679848639178224348966309619052798488909082307110902445595724341286608959925801829756525526243684536115856528805020439965613516355067753856475629524304268915399502745195831856710907661535868988721331189916736238540712398051680091965455756603260140826492895494853907634504720747245633");
//        BigInteger e = new BigInteger("65537");
//        BigInteger d = new BigInteger("19889843166551599707817170915649025194796904711560632661135799992236385779254894331792265065443622756890012020212927705588884036211735720023380435682764524449631974370220019402021038164175570368177776959055309765000696946731304849785712081220896277458221633983822452333249197209907929579769680795368625751585");
//
//        BloomFilters bf1 = new BloomFilters(0.001, 1000);
//
//        for (int i = 1; i <= 1000; i++) {
//            BigInteger h = PSIUtils.stringToBigInteger(String.valueOf(i));
//            BigInteger z = h.modPow(d, N);
//
//            bf1.add(z);
//        }
//
//        bf.setRsaD(d.toString());
//        bf.setRsaN(N.toString());
//        bf.setRsaE(e.toString());
//        bf.setSourcePath(bf);


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
