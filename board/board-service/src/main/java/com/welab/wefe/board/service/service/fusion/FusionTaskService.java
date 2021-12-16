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

import com.welab.wefe.board.service.api.fusion.task.*;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.database.repository.fusion.FusionTaskRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.fusion.FusionTaskOutput;
import com.welab.wefe.board.service.fusion.actuator.ClientActuator;
import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.fusion.core.enums.AlgorithmType;
import com.welab.wefe.fusion.core.enums.DataResourceType;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import com.welab.wefe.fusion.core.enums.PSIActuatorRole;
import com.welab.wefe.fusion.core.utils.PSIUtils;
import com.welab.wefe.fusion.core.utils.bf.BloomFilterUtils;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.welab.wefe.common.StatusCode.DATA_NOT_FOUND;

/**
 * @author hunter.zhao
 */
@Service
public class FusionTaskService extends AbstractService {

    @Autowired
    FusionTaskRepository fusionTaskRepository;

    @Autowired
    private TableDataSetService tableDataSetService;

    @Autowired
    ThirdPartyService thirdPartyService;

    @Autowired
    BloomFilterService bloomFilterService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    FieldInfoService fieldInfoService;

    public FusionTaskMySqlModel find(String taskId) throws StatusCodeWithException {
        return fusionTaskRepository.findOne("id", taskId, FusionTaskMySqlModel.class);
    }

    public FusionTaskMySqlModel findByBusinessId(String businessId) throws StatusCodeWithException {
        return fusionTaskRepository.findOne("businessId", businessId, FusionTaskMySqlModel.class);
    }

    public FusionTaskMySqlModel findByBusinessIdAndStatus(String businessId, FusionTaskStatus status) throws StatusCodeWithException {
        Specification<FusionTaskMySqlModel> where = Where.create()
                .equal("businessId", businessId)
                .equal("status", status).build(FusionTaskMySqlModel.class);
        return fusionTaskRepository.findOne(where).isPresent() ? fusionTaskRepository.findOne(where).get() : null;
    }

    public void updateByBusinessId(String businessId, FusionTaskStatus status, Integer count, long spend) throws StatusCodeWithException {
        FusionTaskMySqlModel model = findByBusinessId(businessId);
        if (model == null) {
            throw new StatusCodeWithException("task does not exist，businessId：" + businessId, StatusCode.DATA_NOT_FOUND);
        }
        model.setStatus(status);
        model.setUpdatedTime(new Date());
        model.setFusionCount(count);
        model.setSpend(spend);
        fusionTaskRepository.save(model);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(AddApi.Input input) throws StatusCodeWithException {
        //If a task is being executed, add it after the task is completed
        if (ActuatorManager.size() > 0) {
            throw new StatusCodeWithException("If a task is being executed, add it after the task is completed", StatusCode.SYSTEM_BUSY);
        }

        String businessId = UUID.randomUUID().toString().replaceAll("-", "");

        //Add fieldinfo
        fieldInfoService.saveAll(businessId, input.getFieldInfoList());

        //Add tasks
        FusionTaskMySqlModel task = ModelMapper.map(input, FusionTaskMySqlModel.class);
        task.setBusinessId(businessId);
        task.setStatus(FusionTaskStatus.Await);

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm()) && DataResourceType.BloomFilter.equals(input.getDataResourceType())) {
            task.setPsiActuatorRole(PSIActuatorRole.server);
            fusionTaskRepository.save(task);

            thirdPartyService.alignApply(task);
            return;
        }


        TableDataSetMysqlModel dataSet = tableDataSetService.findOneById(input.getDataResourceId());
        if (dataSet == null) {
            throw new StatusCodeWithException(DATA_NOT_FOUND);
        }

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm())) {
            task.setPsiActuatorRole(PSIActuatorRole.client);
        }

        task.setRowCount(dataSet.getTotalDataCount());
        fusionTaskRepository.save(task);

        thirdPartyService.alignApply(task);
    }


    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateApi.Input input) throws StatusCodeWithException {

        FusionTaskMySqlModel task = fusionTaskRepository.findOne("id", input.getId(), FusionTaskMySqlModel.class);

        if (task == null) {
            throw new StatusCodeWithException("The task to update does not exist", DATA_NOT_FOUND);
        }

        //The update task
        task.setName(input.getName());
        task.setDataResourceId(input.getDataResourceId());
        task.setDataResourceType(input.getDataResourceType());
        task.setDstMemberId(input.getDstMemberId());

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm()) && DataResourceType.BloomFilter.equals(input.getDataResourceType())) {
            task.setPsiActuatorRole(PSIActuatorRole.server);
            fusionTaskRepository.save(task);
            return;
        }

        TableDataSetMysqlModel dataSet = tableDataSetService.findOneById(input.getDataResourceId());

        if (dataSet == null) {
            throw new StatusCodeWithException(DATA_NOT_FOUND);
        }

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm())) {
            task.setPsiActuatorRole(PSIActuatorRole.client);
        }
        task.setRowCount(dataSet.getTotalDataCount());

        fusionTaskRepository.save(task);
    }


    @Transactional(rollbackFor = Exception.class)
    public void handle(AuditApi.Input input) throws StatusCodeWithException {
        FusionTaskMySqlModel task = findByBusinessId(input.getBusinessId());
//        FusionTaskMySqlModel task = findByBusinessIdAndStatus(input.getBusinessId(), FusionTaskStatus.Pending);
//        if (task == null) {
//            throw new StatusCodeWithException("businessId error:" + input.getBusinessId(), DATA_NOT_FOUND);
//        }

        if (!input.getAuditStatus().equals(AuditStatus.agree)) {
            task.setStatus(FusionTaskStatus.Refuse);
            task.setComment(input.getAuditComment());

            //callback
            thirdPartyService.callback(task.getDstMemberId(), task.getBusinessId(), input.getAuditStatus(), input.getAuditComment());

            return;
        }

        if (ActuatorManager.size() > 0) {
            throw new StatusCodeWithException("If a task is being executed, add it after the task is completed", StatusCode.SYSTEM_BUSY);
        }

        switch (task.getAlgorithm()) {
            case RSA_PSI:
                psi(input, task);
                break;
            default:
                throw new RuntimeException("Unexpected enumeration values");
        }

        //callback
        thirdPartyService.callback(task.getDstMemberId(), task.getBusinessId(), input.getAuditStatus(), input.getAuditComment());
    }


    /**
     * RSA-psi Algorithm to deal with
     */
    private void psi(AuditApi.Input input, FusionTaskMySqlModel task) throws StatusCodeWithException {
        switch (task.getPsiActuatorRole()) {
            case server:
                psiServer(task);
                break;
            case client:
                psiClient(input, task);
                break;
            default:
                break;
        }
    }

    /**
     * psi-client
     */
    private void psiClient(AuditApi.Input input, FusionTaskMySqlModel task) throws StatusCodeWithException {

//        TableDataSetMysqlModel dataSet = tableDataSetService.findOneById(task.getDataResourceId());
//        if (dataSet == null) {
//            throw new StatusCodeWithException("No corresponding dataset was found", DATA_NOT_FOUND);
//        }

        //Add fieldinfo
        fieldInfoService.saveAll(task.getBusinessId(), input.getFieldInfoList());

        task.setStatus(FusionTaskStatus.Running);
        task.setUpdatedTime(new Date());
        task.setTrace(input.getTrace());
        task.setTraceColumn(input.getTraceColumn());

        fusionTaskRepository.save(task);

        ClientActuator client = new ClientActuator(
                task.getBusinessId(),
                task.getDataResourceId(),
                input.getTrace(),
                input.getTraceColumn(),
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

        if (ActuatorManager.get(task.getBusinessId()) != null) {
            return;
        }

        /**
         * Find your party by task ID
         */
        BloomFilterMysqlModel bf = bloomFilterService.findOne(task.getDataResourceId());
        if (bf == null) {
            throw new StatusCodeWithException("Bloom filter not found", StatusCode.PARAMETER_VALUE_INVALID);
        }

//
//        BigInteger N = new BigInteger("146167375152084793681454802679848639178224348966309619052798488909082307110902445595724341286608959925801829756525526243684536115856528805020439965613516355067753856475629524304268915399502745195831856710907661535868988721331189916736238540712398051680091965455756603260140826492895494853907634504720747245633");
//        BigInteger e = new BigInteger("65537");
//        BigInteger d = new BigInteger("19889843166551599707817170915649025194796904711560632661135799992236385779254894331792265065443622756890012020212927705588884036211735720023380435682764524449631974370220019402021038164175570368177776959055309765000696946731304849785712081220896277458221633983822452333249197209907929579769680795368625751585");
//
//        File file = new File("/Users/hunter.zhao/Documents/tel.txt");
//        String[] s = new String[0];
//        try {
//            s = FileUtil.readAllText(file).split(System.lineSeparator());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//        BloomFilters bf = new BloomFilters(0.001, s.length);
//        for (int i = 0; i < s.length; i++){
//
//            BigInteger h = PSIUtils.stringToBigInteger(String.valueOf(s[i]));
//            BigInteger z = h.modPow(d, N);
//
//            bf.add(z);
//        }

        /**
         * Generate the corresponding task handler
         */
        ServerActuator server = new ServerActuator(
                task.getBusinessId(),
//bf ,
//N,
//e,
//d
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

    /**
     * Receive alignment request
     */
    @Transactional(rollbackFor = Exception.class)
    public void alignByPartner(ReceiveApi.Input input) throws StatusCodeWithException {

        if (PSIActuatorRole.server.equals(input.getPsiActuatorRole()) && input.getRowCount() <= 0) {
            throw new StatusCodeWithException("The required parameter is missing", StatusCode.PARAMETER_VALUE_INVALID);
        }

        //Add tasks
        FusionTaskMySqlModel model = ModelMapper.map(input, FusionTaskMySqlModel.class);
        model.setStatus(FusionTaskStatus.Pending);

        fusionTaskRepository.save(model);
    }

    /**
     * Pages to find
     */
    public PagingOutput<FusionTaskOutput> paging(PagingApi.Input input) {
        Specification<FusionTaskMySqlModel> where = Where.create()
                .equal("projectId", input.getProjectId())
                .equal("businessId", input.getBusinessId())
                .equal("status", input.getStatus())
                .build(FusionTaskMySqlModel.class);

        PagingOutput<FusionTaskMySqlModel> page = fusionTaskRepository.paging(where, input);

        List<FusionTaskOutput> list = page
                .getList()
                .stream()
                .map(x -> ModelMapper.map(x, FusionTaskOutput.class))
                .collect(Collectors.toList());

//        list.forEach(x -> {
//            try {
//          //      setName(x);
////                setDataResouceList(x);
//            } catch (StatusCodeWithException e) {
//                LOG.error("设置名称出错", e);
//            }
//        });

        return PagingOutput.of(page.getTotal(), list);
    }

    /**
     * Search according to taskId
     */
    public FusionTaskOutput detail(String taskId) throws StatusCodeWithException {
        FusionTaskMySqlModel model = fusionTaskRepository.findOne("id", taskId, FusionTaskMySqlModel.class);

        FusionTaskOutput output = ModelMapper.map(model, FusionTaskOutput.class);

//        setName(output);
//        setDataResouceList(output);
//        setPartnerList(output);

        return output;
    }

//    private void setName(TaskOutput model) throws StatusCodeWithException {
//        model.setPartnerName(CacheObjects.getPartnerName(model.getPartnerId()));
//
//        if (DataResourceType.BloomFilter.equals(model.getDataResourceType())) {
//            model.setDataResourceName(CacheObjects.getBloomFilterName(model.getDataResourceId()));
//        } else {
//            model.setDataResourceName(CacheObjects.getDataSetName(model.getDataResourceId()));
//        }
//    }

    /**
     * Finding data resources
     *
     * @param model
     * @throws StatusCodeWithException
     */
//    private void setDataResouceList(TaskOutput model) throws StatusCodeWithException {
//
//        if (model.getDataResourceType() == null) {
//            return;
//        }
//
//        if (DataResourceType.BloomFilter.equals(model.getDataResourceType())) {
//            BloomfilterOutputModel bf = ModelMapper.map(
//                    bloomFilterService.findById(model.getDataResourceId()),
//                    BloomfilterOutputModel.class);
//
//            model.setBloomFilterList(Arrays.asList(bf));
//        } else {
//            DataSetOutputModel dataSet = ModelMapper.map(
//                    dataSetRepository.findOne("id", model.getDataResourceId(), TableDataSetMysqlModel.class),
//                    DataSetOutputModel.class);
//
//            model.setDataSetList(Arrays.asList(dataSet));
//        }
//    }

    /**
     * Find partners
     *
     * @param model
     * @throws StatusCodeWithException
     */
//    private void setPartnerList(TaskOutput model) throws StatusCodeWithException {
//        PartnerOutputModel partner = ModelMapper.map(partnerService.findByPartnerId(model.getPartnerId()),
//                PartnerOutputModel.class);
//
//        model.setPartnerList(Arrays.asList(partner));
//    }

    /**
     * Delete the data
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) throws StatusCodeWithException {

        //Judge task status
        fusionTaskRepository.deleteById(id);
    }


    public static void main(String[] args) {

    }
}
