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

package com.welab.wefe.serving.service.service;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.service.api.model.EnableApi;
import com.welab.wefe.serving.service.api.model.QueryApi;
import com.welab.wefe.serving.service.api.model.SaveModelApi;
import com.welab.wefe.serving.service.api.service.RouteApi;
import com.welab.wefe.serving.service.api.serviceorder.SaveApi;
import com.welab.wefe.serving.service.database.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.entity.ServiceCallLogMysqlModel;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.database.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.dto.ModelStatusOutput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.dto.ServiceResultOutput;
import com.welab.wefe.serving.service.enums.MemberModelStatusEnum;
import com.welab.wefe.serving.service.enums.ServiceCallStatusEnum;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.service_processor.ModelServiceProcessor;
import com.welab.wefe.serving.service.utils.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * model Service
 *
 * @author hunter.zhao
 */
@Service
public class ModelService {

    Logger LOG = LoggerFactory.getLogger(getClass());

    private final String API_PREFIX = "/predict/";
    @Autowired
    private TableModelRepository modelRepository;

    @Autowired
    private ModelMemberService modelMemberService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private ClientServiceService clientServiceService;

    @Autowired
    private ModelMemberRepository modelMemberRepository;

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private ServiceCallLogService serviceCallLogService;


    @Transactional(rollbackFor = Exception.class)
    public String save(SaveModelApi.Input input) {

        saveModelMembers(input);

        addPartners(input);

        //open or activate
        openService(input);

        //save model
        return upsertModel(input);
    }

    private void addPartners(SaveModelApi.Input input) {
        partnerService.save(input.getMemberParams());
    }

    private void saveModelMembers(SaveModelApi.Input input) {
        if (JobMemberRole.provider.equals(input.getMyRole())) {
            modelMemberService.save(input.getServiceId(), CacheObjects.getMemberId(), input.getMyRole());
        } else {
            modelMemberService.save(input.getServiceId(), input.getMemberParams());
        }
    }

    private String upsertModel(SaveModelApi.Input input) {
        TableModelMySqlModel model = findOne(input.getServiceId());
        if (model == null) {
            model = new TableModelMySqlModel();
            model.setCreatedBy(CurrentAccount.get() == null ? "board推送" : CurrentAccount.get().getNickname());
        }

        convertTo(input, model);

        model.setUpdatedTime(new Date());
        model.setUpdatedBy(CurrentAccount.get() == null ? "board推送" : CurrentAccount.get().getNickname());
        modelRepository.save(model);
        return model.getId();
    }

    private TableModelMySqlModel convertTo(SaveModelApi.Input input, TableModelMySqlModel model) {
        BeanUtils.copyProperties(input, model);
        model.setUrl(setModelServiceUrl(input.getServiceId()));
        model.setServiceType(ServiceTypeEnum.MachineLearning.getCode());
        return model;
    }

    private void openService(SaveModelApi.Input input) {
        if (JobMemberRole.provider.equals(input.getMyRole())) {
            openPartnerService(input.getServiceId(), input.getMemberParams());
        } else {
            activatePartnerService(input.getServiceId(), input.getName(), input.getMemberParams());
        }
    }

    /**
     * promoter：Activate model service
     *
     * @param serviceId
     * @param memberParams
     */
    private void activatePartnerService(String serviceId, String modelName, List<MemberParams> memberParams) {
        memberParams.stream()
                .filter(x -> JobMemberRole.provider.equals(x.getRole()))
                .forEach(x -> activate(serviceId, modelName, x));
    }

    private void activate(String serviceId, String name, MemberParams x) {
        try {
            clientServiceService.activateService(
                    serviceId,
                    name,
                    x.getMemberId(),
                    CacheObjects.getRsaPrivateKey(),
                    CacheObjects.getRsaPublicKey(),
                    setModelServiceUrl(serviceId),
                    ServiceTypeEnum.MachineLearning
            );
        } catch (StatusCodeWithException e) {
            LOG.error("模型服务激活失败: {}", e.getMessage());
        }
    }

    private String setModelServiceUrl(String serviceId) {
        return API_PREFIX + serviceId;
    }

    /**
     * provider: add opening record
     *
     * @param modelId
     * @param memberParams
     */
    private void openPartnerService(String modelId, List<MemberParams> memberParams) {
        memberParams.stream()
                .filter(x -> JobMemberRole.promoter.equals(x.getRole()))
                .forEach(x -> openService(modelId, x));
    }

    private void openService(String modelId, MemberParams x) {
        try {
            clientServiceService.openService(
                    modelId,
                    x.getMemberId(),
                    x.getPublicKey(),
                    ServiceTypeEnum.MachineLearning
            );
        } catch (StatusCodeWithException e) {
            LOG.error("开通模型服务失败：{}", e.getMessage());
        }
    }

    public TableModelMySqlModel findOne(String serviceId) {
        return modelRepository.findOne("serviceId", serviceId, TableModelMySqlModel.class);
    }

    public List<ModelMemberMySqlModel> findByModelIdAndMemberId(String modelId, String memberId) {
        return modelMemberRepository.findByModelIdAndMemberId(modelId, memberId);
    }

    public ModelMemberMySqlModel findByModelIdAndMemberIdAndRole(String modelId, String memberId, JobMemberRole myRole) {
        return modelMemberRepository.findByModelIdAndMemberIdAndRole(modelId, memberId, myRole);
    }


    /**
     * query
     *
     * @param input
     * @return PagingOutput<QueryApi.Output>
     */
    public PagingOutput<QueryApi.Output> query(QueryApi.Input input) {

        PagingOutput<TableModelMySqlModel> page = queryModels(input);

        PagingOutput<ModelMemberMySqlModel> memberPage = queryModelMembers(input);

        List<QueryApi.Output> list = bulidOutputs(page, memberPage);

        return PagingOutput.of(
                page.getTotal(),
                list
        );
    }

    private List<QueryApi.Output> bulidOutputs(PagingOutput<TableModelMySqlModel> page, PagingOutput<ModelMemberMySqlModel> memberPage) {
        List<QueryApi.Output> list = page
                .getList()
                .stream()
                .map(x -> setRole(memberPage, x))
                .collect(Collectors.toList());
        return list;
    }

    private QueryApi.Output setRole(PagingOutput<ModelMemberMySqlModel> memberPage, TableModelMySqlModel TableModelMySqlModel) {
        QueryApi.Output output = ModelMapper.map(TableModelMySqlModel, QueryApi.Output.class);

        memberPage.getList()
                .stream()
                .filter(model -> model.getModelId().equals(TableModelMySqlModel.getServiceId()))
                .forEach(x -> output.setMyRole(x.getRole()));

        return output;
    }

    private PagingOutput<ModelMemberMySqlModel> queryModelMembers(QueryApi.Input input) {
        Specification<ModelMemberMySqlModel> where = buildQueryMemberParam();
        PagingOutput<ModelMemberMySqlModel> memberPage = modelMemberRepository.paging(where, input);
        return memberPage;
    }

    private Specification<ModelMemberMySqlModel> buildQueryMemberParam() {
        Specification<ModelMemberMySqlModel> where = Where
                .create()
                .contains("memberId", CacheObjects.getMemberId())
                .build(ModelMemberMySqlModel.class);
        return where;
    }

    private PagingOutput<TableModelMySqlModel> queryModels(QueryApi.Input input) {
        Specification<TableModelMySqlModel> jobWhere = buildQueryModelParam(input);
        PagingOutput<TableModelMySqlModel> page = modelRepository.paging(jobWhere, input);
        return page;
    }

    private Specification<TableModelMySqlModel> buildQueryModelParam(QueryApi.Input input) {
        Specification<TableModelMySqlModel> jobWhere = Where
                .create()
                .contains("modelId", input.getModelId())
                .contains("name", input.getName())
                .equal("algorithm", input.getAlgorithm())
                .equal("flType", input.getFlType())
                .equal("createdBy", input.getCreator())
                .build(TableModelMySqlModel.class);
        return jobWhere;
    }

    /**
     * Update model enable field
     */
    public void enable(EnableApi.Input input) {

        modelRepository.updateById(input.getId(), "enable", input.isEnable(), TableModelMySqlModel.class);

        TableModelMySqlModel TableModelMySqlModel = modelRepository.findOne("id", input.getId(), TableModelMySqlModel.class);

        ModelManager.refreshModelEnable(TableModelMySqlModel.getServiceId(), input.isEnable());
    }


    public ModelStatusOutput checkAvailable(String modelId) {
        return ModelStatusOutput.of(
                CacheObjects.getMemberId(),
                CacheObjects.getMemberName(),
                getAvailableStatus(modelId)
        );
    }

    private MemberModelStatusEnum getAvailableStatus(String modelId) {
        try {
            return ModelManager.getModelEnable(modelId) ?
                    MemberModelStatusEnum.available : MemberModelStatusEnum.unavailable;
        } catch (StatusCodeWithException e) {
            return MemberModelStatusEnum.unavailable;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String serviceId,
                             PredictFeatureDataSource featureSource,
                             String dataSourceId,
                             String sqlScript,
                             String sqlConditionField) throws StatusCodeWithException {

        TableModelMySqlModel model = findOne(serviceId);
        if (model == null) {
            throw new StatusCodeWithException("未查找到模型！" + serviceId, StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (featureSource.equals(PredictFeatureDataSource.sql)) {
            model.setFeatureSource(featureSource);
            model.setSqlScript(sqlScript);
            model.setSqlConditionField(sqlConditionField);
            model.setDataSourceId(dataSourceId);
            model.setUpdatedBy(CurrentAccount.id());
            model.setUpdatedTime(new Date());
            modelRepository.save(model);
        } else {
            model.setFeatureSource(featureSource);
            model.setDataSourceId(null);
            model.setSqlScript("");
            model.setSqlConditionField("");
            modelRepository.save(model);
        }
    }

    public ServiceResultOutput predict(RouteApi.Input input) throws StatusCodeWithException {

        String responseId = ServiceResultOutput.buildId();
        PredictResult result = null;
        ServiceOrderEnum status = ServiceOrderEnum.SUCCESS;
        Integer responseCode = 0;
        try {
            JObject data = initParam(input);
            result = forward(data);

            return ServiceResultOutput.of(input.getRequestId(), responseId, result);
        } catch (StatusCodeWithException e) {
            status = ServiceOrderEnum.FAILED;
            responseCode = StatusCode.SYSTEM_ERROR.getCode();
            throw e;
        } finally {
            String orderId = createOrder(input, status);
            callLog(input, orderId, responseId, result, responseCode);
        }
    }

    private JObject initParam(RouteApi.Input input) {
        return JObject.create(input.getData())
                .append("requestId", input.getRequestId())
                .append("partnerCode", input.getPartnerCode());
    }

    private void callLog(RouteApi.Input input, String orderId, String responseId, PredictResult result, Integer responseCode) {
        ServiceCallLogMysqlModel callLog = new ServiceCallLogMysqlModel();
        callLog.setServiceType(ServiceTypeEnum.MachineLearning.name());
        callLog.setOrderId(orderId);
        callLog.setServiceId(input.getServiceId());
        callLog.setServiceName(CacheObjects.getServiceName(input.getServiceId()));
        callLog.setRequestData(input.getData());
        callLog.setRequestPartnerId(input.getPartnerCode());
        callLog.setRequestPartnerName(CacheObjects.getPartnerName(input.getPartnerCode()));
        callLog.setRequestId(input.getRequestId());
        callLog.setRequestIp(ServiceUtil.getIpAddr(input.request));
        callLog.setResponseCode(responseCode);
        callLog.setResponseId(responseId);
        callLog.setResponsePartnerId(CacheObjects.getMemberId());
        callLog.setResponsePartnerName(CacheObjects.getMemberName());
        callLog.setResponseData(JSON.toJSONString(result));
        //是否自己发起的请求 0-否 1-是
        callLog.setCallByMe(0);
        callLog.setResponseStatus(getResponseStatus(result));
        serviceCallLogService.save(callLog);
    }

    private String getResponseStatus(PredictResult result) {
        return result == null ? ServiceCallStatusEnum.RESPONSE_ERROR.name() : ServiceCallStatusEnum.SUCCESS.name();
    }

    private String createOrder(RouteApi.Input input, ServiceOrderEnum status) {
        SaveApi.Input order = new SaveApi.Input();
        order.setServiceId(input.getServiceId());
        order.setServiceName(CacheObjects.getServiceName(input.getServiceId()));
        order.setServiceType(ServiceTypeEnum.MachineLearning.name());
        order.setRequestPartnerId(input.getPartnerCode());
        order.setRequestPartnerName(CacheObjects.getPartnerName(input.getPartnerCode()));
        order.setResponsePartnerId(CacheObjects.getMemberId());
        order.setResponsePartnerName(CacheObjects.getMemberName());
        //是否自己发起的订单
        order.setOrderType(0);
        order.setStatus(status.getValue());
        serviceOrderService.save(order);
        return order.getId();
    }

    private PredictResult forward(JObject data) throws StatusCodeWithException {
        ModelServiceProcessor processor = new ModelServiceProcessor();
        return processor.process(data, new TableModelMySqlModel());
    }
}
