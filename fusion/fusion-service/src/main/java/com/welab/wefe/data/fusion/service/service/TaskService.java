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
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.data.fusion.service.actuator.rsapsi.PsiServerActuator;
import com.welab.wefe.data.fusion.service.api.task.*;
import com.welab.wefe.data.fusion.service.database.entity.BloomFilterMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.PartnerMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.TaskMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.database.repository.PartnerRepository;
import com.welab.wefe.data.fusion.service.database.repository.TaskRepository;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import com.welab.wefe.data.fusion.service.dto.entity.PartnerOutputModel;
import com.welab.wefe.data.fusion.service.dto.entity.TaskOutput;
import com.welab.wefe.data.fusion.service.dto.entity.TaskOverviewOutput;
import com.welab.wefe.data.fusion.service.dto.entity.bloomfilter.BloomfilterOutputModel;
import com.welab.wefe.data.fusion.service.dto.entity.dataset.DataSetOutputModel;
import com.welab.wefe.data.fusion.service.enums.*;
import com.welab.wefe.data.fusion.service.manager.ActuatorManager;
import com.welab.wefe.data.fusion.service.service.bloomfilter.BloomFilterService;
import com.welab.wefe.data.fusion.service.task.AbstractTask;
import com.welab.wefe.data.fusion.service.task.PsiServerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.welab.wefe.common.StatusCode.DATA_NOT_FOUND;
import static com.welab.wefe.common.StatusCode.PARAMETER_VALUE_INVALID;

/**
 * @author hunter.zhao
 */
@Service
public class TaskService extends AbstractService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    DataSetRepository dataSetRepository;

    @Autowired
    PartnerRepository partnerRepository;

    @Autowired
    PartnerService partnerService;

    @Autowired
    ThirdPartyService thirdPartyService;

    @Autowired
    BloomFilterService bloomFilterService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    FieldInfoService fieldInfoService;

    public TaskMySqlModel find(String taskId) throws StatusCodeWithException {
        return taskRepository.findOne("id", taskId, TaskMySqlModel.class);
    }

    public TaskMySqlModel findByBusinessId(String businessId) throws StatusCodeWithException {
        return taskRepository.findOne("businessId", businessId, TaskMySqlModel.class);
    }

    public void updateByBusinessId(String businessId, TaskStatus status, Integer count, long spend) throws StatusCodeWithException {
        TaskMySqlModel model = findByBusinessId(businessId);
        if (model == null) {
            throw new StatusCodeWithException("任务不存在，检查参数businessId：" + businessId, StatusCode.DATA_NOT_FOUND);
        }
        model.setStatus(status);
        model.setUpdatedTime(new Date());
        model.setFusionCount(count);
        model.setSpend(spend);
        taskRepository.save(model);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(AddApi.Input input) throws StatusCodeWithException {
        //If a task is being executed, add it after the task is completed
        if (ActuatorManager.size() > 0) {
            throw new StatusCodeWithException("有正在运行的任务, 请等待任务完成后再添加", StatusCode.SYSTEM_BUSY);
        }

        String businessId = UUID.randomUUID().toString().replaceAll("-", "");

        //Add fieldinfo
        fieldInfoService.saveAll(businessId, input.getFieldInfoList());

        //Add tasks
        TaskMySqlModel task = new TaskMySqlModel();
        task.setBusinessId(businessId);
        task.setName(input.getName());
        task.setDataResourceId(input.getDataResourceId());
        task.setPartnerMemberId(input.getPartnerMemberId());
        task.setAlgorithm(input.getAlgorithm());
        task.setStatus(TaskStatus.Await);
        task.setDataResourceType(input.getDataResourceType());
        task.setRowCount(input.getRowCount());
        task.setDescription(input.getDescription());
        task.setTrace(input.getTrace());
        task.setTraceColumn(input.getTraceColumn());

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm()) && DataResourceType.BloomFilter.equals(input.getDataResourceType())) {
            task.setPsiActuatorRole(PSIActuatorRole.server);
            taskRepository.save(task);

            thirdPartyService.alignApply(task);
            return;
        }

        DataSetMySqlModel dataSet = dataSetRepository.findOne("id", input.getDataResourceId(), DataSetMySqlModel.class);
        if (dataSet == null) {
            throw new StatusCodeWithException(DATA_NOT_FOUND);
        }

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm())) {
            task.setPsiActuatorRole(PSIActuatorRole.client);
        }

        task.setRowCount(dataSet.getRowCount());
        task.setDataCount(dataSet.getRowCount());
        taskRepository.save(task);

        thirdPartyService.alignApply(task);
    }


    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateApi.Input input) throws StatusCodeWithException {

        TaskMySqlModel task = taskRepository.findOne("id", input.getId(), TaskMySqlModel.class);

        if (task == null) {
            throw new StatusCodeWithException("任务不存在！", DATA_NOT_FOUND);
        }

        //The update task
        task.setName(input.getName());
        task.setDataResourceId(input.getDataResourceId());
        task.setDataResourceType(input.getDataResourceType());
        task.setPartnerMemberId(input.getPartnerMemberId());

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm()) && DataResourceType.BloomFilter.equals(input.getDataResourceType())) {
            task.setPsiActuatorRole(PSIActuatorRole.server);
            taskRepository.save(task);
            return;
        }

        DataSetMySqlModel dataSet = dataSetRepository.findOne("id", input.getDataResourceId(), DataSetMySqlModel.class);

        if (dataSet == null) {
            throw new StatusCodeWithException(DATA_NOT_FOUND);
        }

        if (AlgorithmType.RSA_PSI.equals(input.getAlgorithm())) {
            task.setPsiActuatorRole(PSIActuatorRole.client);
        }
        task.setRowCount(dataSet.getRowCount());
        task.setDataCount(dataSet.getRowCount());

        taskRepository.save(task);
    }


    @Transactional(rollbackFor = Exception.class)
    public void handle(HandleApi.Input input) throws StatusCodeWithException {
        if (ActuatorManager.size() > 0) {
            throw new StatusCodeWithException("有正在运行的任务, 请等待任务完成后再添加", StatusCode.SYSTEM_BUSY);
        }

        TaskMySqlModel task = find(input.getId());
        if (task == null) {
            throw new StatusCodeWithException("任务不存在！taskId:" + input.getId(), DATA_NOT_FOUND);
        }

        //Find partner information
        PartnerMySqlModel partner = partnerService.findByPartnerId(task.getPartnerMemberId());
        if (partner == null) {
            throw new StatusCodeWithException("未找到合作方！", StatusCode.PARAMETER_VALUE_INVALID);
        }


        switch (task.getAlgorithm()) {
            case RSA_PSI:
                psi(input, task, partner);
                break;
            default:
                throw new RuntimeException("意料之外的枚举值，type: " + task.getAlgorithm());
        }
    }


    /**
     * RSA-psi Algorithm to deal with
     */
    private void psi(HandleApi.Input input, TaskMySqlModel task, PartnerMySqlModel partner) throws StatusCodeWithException {
        switch (task.getPsiActuatorRole()) {
            case server:
                psiServer(input, task, partner);
                break;
            case client:
                psiClient(input, task, partner);
                break;
            default:
                break;
        }
    }

    /**
     * psi-client
     */
    private void psiClient(HandleApi.Input input, TaskMySqlModel task, PartnerMySqlModel partner) throws StatusCodeWithException {
        if (StringUtil.isEmpty(input.getDataResourceId())) {
            throw new StatusCodeWithException(input.getDataResourceId(), PARAMETER_VALUE_INVALID);
        }

        DataSetMySqlModel dataSet = dataSetRepository.findOne("id", input.getDataResourceId(), DataSetMySqlModel.class);
        if (dataSet == null) {
            throw new StatusCodeWithException("未查找到数据集", DATA_NOT_FOUND);
        }

        //Add fieldinfo
        fieldInfoService.saveAll(task.getBusinessId(), input.getFieldInfoList());

        task.setStatus(TaskStatus.Ready);
        task.setDataResourceId(input.getDataResourceId());
        task.setDataResourceType(input.getDataResourceType());
        task.setRowCount(dataSet.getRowCount());
        task.setDataCount(dataSet.getRowCount());
        task.setUpdatedTime(new Date());
        task.setTrace(input.getTrace());
        task.setTraceColumn(input.getTraceColumn());

        taskRepository.save(task);

        //The callback
        thirdPartyService.callback(partner.getBaseUrl(), task.getBusinessId(), CallbackType.init, dataSet.getRowCount());
    }


    /**
     * psi-server
     *
     * @param input
     * @param task
     * @throws StatusCodeWithException
     */
    private void psiServer(HandleApi.Input input, TaskMySqlModel task, PartnerMySqlModel partner) throws StatusCodeWithException {

        task.setStatus(TaskStatus.Running);
        task.setDataResourceId(input.getDataResourceId());
        task.setDataResourceType(input.getDataResourceType());
        task.setRowCount(input.getRowCount());
        task.setUpdatedTime(new Date());
        taskRepository.save(task);

        if (ActuatorManager.get(task.getBusinessId()) != null) {
            return;
        }

        /**
         * Find your party by task ID
         */
        BloomFilterMySqlModel bf = bloomFilterService.findById(input.getDataResourceId());
        if (bf == null) {
            throw new StatusCodeWithException("未查找到布隆过滤器", StatusCode.PARAMETER_VALUE_INVALID);
        }

        /**
         * Generate the corresponding task handler
         */
        AbstractTask server = new PsiServerTask(
                task.getBusinessId(),
                bf.getSrc(),
                new PsiServerActuator(task.getBusinessId(),
                        task.getDataCount(),
                        "localhost",
                        CacheObjects.getOpenSocketPort(),
                        new BigInteger(bf.getN()),
                        new BigInteger(bf.getE()),
                        new BigInteger(bf.getD())
                )
        );

        ActuatorManager.set(server);

        server.run();

        //The callback
        thirdPartyService.callback(
                partner.getBaseUrl(),
                task.getBusinessId(),
                CallbackType.running,
                ActuatorManager.ip(),
                CacheObjects.getOpenSocketPort()
        );
    }


    /**
     * Receive alignment request
     */
    @Transactional(rollbackFor = Exception.class)
    public void alignByPartner(ReceiveApi.Input input) throws StatusCodeWithException {

        if (PSIActuatorRole.server.equals(input.getPsiActuatorRole()) && input.getDataCount() <= 0) {
            throw new StatusCodeWithException("请求参数缺失", StatusCode.PARAMETER_VALUE_INVALID);
        }

        //Add tasks
        TaskMySqlModel model = new TaskMySqlModel();
        model.setBusinessId(input.getBusinessId());
        model.setPartnerMemberId(input.getMemberId());
        model.setName(input.getName());
        model.setStatus(TaskStatus.Pending);
        model.setDataCount(input.getDataCount());
        model.setPsiActuatorRole(input.getPsiActuatorRole());
        model.setAlgorithm(input.getAlgorithm());
        model.setDescription(input.getDescription());

        taskRepository.save(model);
    }

    /**
     * Pages to find
     */
    public PagingOutput<TaskOutput> paging(PagingApi.Input input) {
        Specification<TaskMySqlModel> where = Where.create()
                .equal("businessId", input.getBusinessId())
                .equal("status", input.getStatus())
                .equal("myRole", input.getMyRole())
                .build(TaskMySqlModel.class);

        PagingOutput<TaskMySqlModel> page = taskRepository.paging(where, input);

        List<TaskOutput> list = page
                .getList()
                .stream()
                .map(x -> ModelMapper.map(x, TaskOutput.class))
                .collect(Collectors.toList());

        list.forEach(x -> {
            try {
                setName(x);
//                setDataResouceList(x);
            } catch (StatusCodeWithException e) {
                LOG.error("设置名称出错", e);
            }
        });

        return PagingOutput.of(page.getTotal(), list);
    }

    /**
     * Search according to taskId
     */
    public TaskOutput detail(String taskId) throws StatusCodeWithException {
        TaskMySqlModel model = taskRepository.findOne("id", taskId, TaskMySqlModel.class);

        TaskOutput output = ModelMapper.map(model, TaskOutput.class);

        setName(output);
        setDataResouceList(output);
        setPartnerList(output);

        return output;
    }

    private void setName(TaskOutput model) throws StatusCodeWithException {
        model.setPartnerMemberName(CacheObjects.getPartnerName(model.getPartnerMemberId()));

        if (DataResourceType.BloomFilter.equals(model.getDataResourceType())) {
            model.setDataResourceName(CacheObjects.getBloomFilterName(model.getDataResourceId()));
        } else {
            model.setDataResourceName(CacheObjects.getDataSetName(model.getDataResourceId()));
        }
    }

    /**
     * Finding data resources
     *
     * @param model
     * @throws StatusCodeWithException
     */
    private void setDataResouceList(TaskOutput model) throws StatusCodeWithException {

        if (model.getDataResourceType() == null) {
            return;
        }

        if (DataResourceType.BloomFilter.equals(model.getDataResourceType())) {
            BloomfilterOutputModel bf = ModelMapper.map(
                    bloomFilterService.findById(model.getDataResourceId()),
                    BloomfilterOutputModel.class);

            model.setBloomFilterList(Arrays.asList(bf));
        } else {
            DataSetOutputModel dataSet = ModelMapper.map(
                    dataSetRepository.findOne("id", model.getDataResourceId(), DataSetMySqlModel.class),
                    DataSetOutputModel.class);

            model.setDataSetList(Arrays.asList(dataSet));
        }
    }

    /**
     * Find partners
     *
     * @param model
     * @throws StatusCodeWithException
     */
    private void setPartnerList(TaskOutput model) throws StatusCodeWithException {
        PartnerOutputModel partner = ModelMapper.map(partnerService.findByPartnerId(model.getPartnerMemberId()),
                PartnerOutputModel.class);

        model.setPartnerList(Arrays.asList(partner));
    }

    /**
     * Delete the data
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) throws StatusCodeWithException {

        //Judge task status
        taskRepository.deleteById(id);
    }

    /**
     * task overview
     */
    public TaskOverviewOutput overview() throws StatusCodeWithException {
        Long allCount = taskRepository.count();

        Long promoterCount = taskRepository.count("myRole", RoleType.promoter, TaskMySqlModel.class);

        Long providerCount = taskRepository.count("myRole", RoleType.provider, TaskMySqlModel.class);

        Long pendingCount = taskRepository.count("status", TaskStatus.Pending, TaskMySqlModel.class);

        Long runningCount = taskRepository.count("status", TaskStatus.Running, TaskMySqlModel.class);

        return TaskOverviewOutput.of(allCount, promoterCount, providerCount, pendingCount, runningCount);
    }
}
