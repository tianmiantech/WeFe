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

import com.welab.wefe.board.service.api.project.fusion.task.*;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.fusion.FusionTaskRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.fusion.FusionMemberInfo;
import com.welab.wefe.board.service.dto.fusion.FusionTaskOutput;
import com.welab.wefe.board.service.fusion.actuator.ClientActuator;
import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.board.service.util.primarykey.PrimaryKeyUtils;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.fusion.core.enums.AlgorithmType;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import com.welab.wefe.fusion.core.enums.PSIActuatorRole;
import com.welab.wefe.fusion.core.utils.bf.BloomFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private DataResourceService dataResourceService;

    @Autowired
    ThirdPartyService thirdPartyService;

    @Autowired
    BloomFilterService bloomFilterService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    FieldInfoService fieldInfoService;

    @Autowired
    ProjectService projectService;

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

    public void updateByBusinessId(String businessId, FusionTaskStatus status, Long dataCount, Long fusionCount, Long processedCount, long spend) throws StatusCodeWithException {
        FusionTaskMySqlModel model = findByBusinessId(businessId);
        if (model == null) {
            throw new StatusCodeWithException("task does not exist，businessId：" + businessId, StatusCode.DATA_NOT_FOUND);
        }
        model.setStatus(status);
        model.setUpdatedTime(new Date());
        model.setFusionCount(fusionCount);
        model.setDataCount(dataCount);
        model.setProcessedCount(processedCount);
        model.setSpend(spend);
        fusionTaskRepository.save(model);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(AddApi.Input input) throws StatusCodeWithException {
        //A non promoter cannot create a task
        ProjectMySqlModel project = projectService.findByProjectId(input.getProjectId());
        if (!JobMemberRole.promoter.equals(project.getMyRole())) {
            throw new StatusCodeWithException("A non promoter cannot create a task", StatusCode.UNSUPPORTED_HANDLE);
        }

        //If a task is being executed, add it after the task is completed
//        if (ActuatorManager.size() > 0) {
//            throw new StatusCodeWithException("If a task is being executed, add it after the task is completed", StatusCode.SYSTEM_BUSY);
//        }

        String businessId = UUID.randomUUID().toString().replaceAll("-", "");

        //Add fieldInfo
        fieldInfoService.saveAll(businessId, input.getFieldInfoList());

        //Add tasks
        FusionTaskMySqlModel task = ModelMapper.map(input, FusionTaskMySqlModel.class);
        task.setBusinessId(businessId);
        task.setStatus(FusionTaskStatus.Await);
        task.setMyRole(JobMemberRole.promoter);

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm()) && DataResourceType.BloomFilter.equals(input.getDataResourceType())) {
            task.setPsiActuatorRole(PSIActuatorRole.server);
            task.setHashFunction(
                    bloomFilterService.findOne(
                            input.getDataResourceId()) == null ?
                            "" : bloomFilterService.findOne(input.getDataResourceId()).getHashFunction()
            );
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

        task.setHashFunction(
                PrimaryKeyUtils.hashFunction(input.getFieldInfoList())
        );
        fusionTaskRepository.save(task);

        dataResourceService.usageCountInJobIncrement(input.getDataResourceId());

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


    public void handle(AuditApi.Input input) throws StatusCodeWithException {
        FusionTaskMySqlModel task = findByBusinessIdAndStatus(input.getBusinessId(), FusionTaskStatus.Pending);
        if (task == null) {
            throw new StatusCodeWithException("businessId error:" + input.getBusinessId(), DATA_NOT_FOUND);
        }

        if (!input.getAuditStatus().equals(AuditStatus.agree)) {
            task.setStatus(FusionTaskStatus.Refuse);
            task.setComment(input.getAuditComment());

            //callback
            thirdPartyService.callback(task.getDstMemberId(), task.getBusinessId(), input.getAuditStatus(), input.getAuditComment());

            return;
        }

//        if (ActuatorManager.size() > 0) {
//            throw new StatusCodeWithException("If a task is being executed, add it after the task is completed", StatusCode.SYSTEM_BUSY);
//        }

        switch (task.getAlgorithm()) {
            case RSA_PSI:
                psi(input, task);
                break;
            default:
                throw new RuntimeException("Unexpected enumeration values");
        }

        //callback
        thirdPartyService.callback(
                task.getDstMemberId(),
                task.getBusinessId(),
                input.getAuditStatus(),
                input.getAuditComment(),
                PrimaryKeyUtils.hashFunction(input.getFieldInfoList())
        );
    }


    @Transactional(rollbackFor = Exception.class)
    public void restart(AuditApi.Input input) throws StatusCodeWithException {
        FusionTaskMySqlModel task = findByBusinessId(input.getBusinessId());
        if (task == null) {
            throw new StatusCodeWithException("businessId error:" + input.getBusinessId(), DATA_NOT_FOUND);
        }

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

        //Add fieldInfo
        fieldInfoService.saveAll(task.getBusinessId(), input.getFieldInfoList());

        task.setStatus(FusionTaskStatus.Running);
        task.setUpdatedTime(new Date());
        task.setTrace(input.getTrace());
        task.setTraceColumn(input.getTraceColumn());
        task.setHashFunction(PrimaryKeyUtils.hashFunction(input.getFieldInfoList()));

        fusionTaskRepository.save(task);

        ClientActuator client = new ClientActuator(
                task.getBusinessId(),
                task.getDataResourceId(),
                input.getTrace(),
                input.getTraceColumn(),
                task.getDstMemberId(),
                DataResourceType.TableDataSet.equals(task.getDataResourceType()) ?
                        task.getRowCount() : task.getPartnerRowCount()
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
                new BigInteger(bf.getRsaD()),
                DataResourceType.TableDataSet.equals(task.getDataResourceType()) ?
                        task.getRowCount() : task.getPartnerRowCount()
        );

        ActuatorManager.set(server);

        server.run();
    }

    /**
     * Receive alignment request
     */
    @Transactional(rollbackFor = Exception.class)
    public void alignByPartner(ReceiveApi.Input input) throws StatusCodeWithException {
        //A non promoter cannot create a task
        ProjectMySqlModel project = projectService.findByProjectId(input.getProjectId());
        if (!JobMemberRole.provider.equals(project.getMyRole())) {
            throw new StatusCodeWithException(StatusCode.UNSUPPORTED_HANDLE, "Non providers do not receive creation requests");
        }

        //Add tasks
        FusionTaskMySqlModel model = ModelMapper.map(input, FusionTaskMySqlModel.class);
        model.setStatus(FusionTaskStatus.Pending);
        model.setMyRole(JobMemberRole.provider);

        //getDataResource row count
        if (DataResourceType.TableDataSet.equals(input.getDataResourceType())) {
            TableDataSetMysqlModel tableDataSet = tableDataSetService.findOneById(model.getDataResourceId());
            if (tableDataSet == null) {
                throw new StatusCodeWithException("Input parameter value invalid", StatusCode.PARAMETER_VALUE_INVALID);
            }
            model.setRowCount(tableDataSet.getTotalDataCount());
        } else {
            BloomFilterMysqlModel bloomFilter = bloomFilterService.findOne(model.getDataResourceId());
            if (bloomFilter == null) {
                throw new StatusCodeWithException("Input parameter value invalid", StatusCode.PARAMETER_VALUE_INVALID);
            }
            model.setRowCount(bloomFilter.getTotalDataCount());
        }

        fusionTaskRepository.save(model);

        dataResourceService.usageCountInJobIncrement(input.getDataResourceId());
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

        return PagingOutput.of(page.getTotal(), list);
    }

    /**
     * Search according to taskId
     */
    public FusionTaskOutput detail(String taskId) throws StatusCodeWithException {
        FusionTaskMySqlModel model = fusionTaskRepository.findOne("id", taskId, FusionTaskMySqlModel.class);
        if (model == null) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "id", taskId);
        }
        FusionTaskOutput output = ModelMapper.map(model, FusionTaskOutput.class);

        setMemberInfo(output);

        return output;
    }

    private void setMemberInfo(FusionTaskOutput model) throws StatusCodeWithException {

        FusionMemberInfo myMemberInfo = ModelMapper.map(model, FusionMemberInfo.class);
        myMemberInfo.setMemberId(CacheObjects.getMemberId());
        myMemberInfo.setMemberName(CacheObjects.getMemberName());
        myMemberInfo.setRole(model.getMyRole());
        myMemberInfo.setHashFunction(model.getHashFunction());
        if (DataResourceType.TableDataSet.equals(myMemberInfo.getDataResourceType())) {
            TableDataSetMysqlModel tableDataSet = tableDataSetService.findOneById(myMemberInfo.getDataResourceId());
            myMemberInfo.setColumnNameList(tableDataSet.getColumnNameList());

            myMemberInfo.setFieldInfoList(fieldInfoService.fieldInfoList(model.getBusinessId()));
        }else {
            myMemberInfo.setFieldInfoList(fieldInfoService.fieldInfoList(model.getDataResourceId()));
        }
//        myMemberInfo.setDataResourceName(CacheObjects.getMemberName());


        FusionMemberInfo memberInfo = new FusionMemberInfo();
        memberInfo.setDataResourceId(model.getPartnerDataResourceId());
//        memberInfo.setDataResourceName(model.getPartnerDataResourceName());
        memberInfo.setDataResourceType(model.getPartnerDataResourceType());
        memberInfo.setRowCount(model.getPartnerRowCount());
        memberInfo.setMemberId(model.getDstMemberId());
        memberInfo.setMemberName(CacheObjects.getMemberName(model.getDstMemberId()));
        memberInfo.setHashFunction(model.getPartnerHashFunction());
        memberInfo.setRole(
                model.getMyRole().equals(JobMemberRole.promoter) ?
                        JobMemberRole.provider :
                        JobMemberRole.promoter
        );

        if (JobMemberRole.promoter.equals(model.getMyRole())) {
            model.setPromoter(myMemberInfo);
            model.setProvider(memberInfo);
        } else {
            model.setPromoter(memberInfo);
            model.setProvider(myMemberInfo);
        }
    }

    /**
     * Delete the data
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) throws StatusCodeWithException {

        //Judge task status
        fusionTaskRepository.deleteById(id);
    }

}
