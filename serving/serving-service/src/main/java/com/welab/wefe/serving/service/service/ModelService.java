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
import com.welab.wefe.serving.service.database.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.database.entity.ServiceCallLogMysqlModel;
import com.welab.wefe.serving.service.database.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.database.repository.ModelRepository;
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

    private final String API_PREFIX = "predict/";
    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelMemberService modelMemberService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private ClientServiceService clientServiceService;

    @Autowired
    private ModelMemberRepository modelMemberRepository;

    @Autowired
    private ModelSqlConfigService modelSqlConfigService;

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
        ModelMySqlModel model = upsertModel(input);
        return model.getId();
        //TODO 考虑模型是否放到service表
    }

    private void addPartners(SaveModelApi.Input input) {
        partnerService.save(input.getMemberParams());
    }

    private void saveModelMembers(SaveModelApi.Input input) {
        if (JobMemberRole.provider.equals(input.getMyRole())) {
            modelMemberService.save(input.getModelId(), CacheObjects.getMemberId(), input.getMyRole());
        } else {
            modelMemberService.save(input.getModelId(), input.getMemberParams());
        }
    }

    private ModelMySqlModel upsertModel(SaveModelApi.Input input) {
        ModelMySqlModel model = findOne(input.getModelId());
        if (model == null) {
            model = new ModelMySqlModel();
        }

        convertTo(input, model);

        model.setUpdatedTime(new Date());
        modelRepository.save(model);
        return model;
    }

    private ModelMySqlModel convertTo(SaveModelApi.Input input, ModelMySqlModel model) {
        BeanUtils.copyProperties(input, model);
        model.setUrl(setModelServiceUrl(model.getModelId()));
        model.setServiceType(ServiceTypeEnum.MachineLearning.getCode());
        return model;
    }

    private void openService(SaveModelApi.Input input) {
        if (JobMemberRole.provider.equals(input.getMyRole())) {
            openPartnerService(input.getModelId(), input.getMemberParams());
        } else {
            activatePartnerService(input.getModelId(), input.getName(), input.getMemberParams());
        }
    }

    /**
     * promoter：Activate model service
     *
     * @param modelId
     * @param memberParams
     */
    private void activatePartnerService(String modelId, String modelName, List<MemberParams> memberParams) {
        memberParams.stream()
                .filter(x -> JobMemberRole.provider.equals(x.getRole()))
                .forEach(x -> activate(modelId, modelName, x));
    }

    private void activate(String modelId, String name, MemberParams x) {
        try {
            clientServiceService.activateService(
                    modelId,
                    name,
                    x.getMemberId(),
                    CacheObjects.getRsaPrivateKey(),
                    CacheObjects.getRsaPublicKey(),
                    setModelServiceUrl(modelId),
                    ServiceTypeEnum.MachineLearning
            );
        } catch (StatusCodeWithException e) {
            LOG.error("模型服务激活失败: {}", e.getMessage());
        }
    }

    private String setModelServiceUrl(String modelId) {
        return API_PREFIX + modelId;
    }

    private String extractServiceName(String modelId) {
        return API_PREFIX + modelId;
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

    public ModelMySqlModel findOne(String modelId) {
        return modelRepository.findOne("modelId", modelId, ModelMySqlModel.class);
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

        PagingOutput<ModelMySqlModel> page = queryModels(input);

        PagingOutput<ModelMemberMySqlModel> memberPage = queryModelMembers(input);

        List<QueryApi.Output> list = bulidOutputs(page, memberPage);

        return PagingOutput.of(
                page.getTotal(),
                list
        );
    }

    private List<QueryApi.Output> bulidOutputs(PagingOutput<ModelMySqlModel> page, PagingOutput<ModelMemberMySqlModel> memberPage) {
        List<QueryApi.Output> list = page
                .getList()
                .stream()
                .map(x -> setRole(memberPage, x))
                .collect(Collectors.toList());
        return list;
    }

    private QueryApi.Output setRole(PagingOutput<ModelMemberMySqlModel> memberPage, ModelMySqlModel modelMySqlModel) {
        QueryApi.Output output = ModelMapper.map(modelMySqlModel, QueryApi.Output.class);

        memberPage.getList()
                .stream()
                .filter(model -> model.getModelId().equals(modelMySqlModel.getModelId()))
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

    private PagingOutput<ModelMySqlModel> queryModels(QueryApi.Input input) {
        Specification<ModelMySqlModel> jobWhere = buildQueryModelParam(input);
        PagingOutput<ModelMySqlModel> page = modelRepository.paging(jobWhere, input);
        return page;
    }

    private Specification<ModelMySqlModel> buildQueryModelParam(QueryApi.Input input) {
        Specification<ModelMySqlModel> jobWhere = Where
                .create()
                .contains("modelId", input.getModelId())
                .contains("name", input.getName())
                .equal("algorithm", input.getAlgorithm())
                .equal("flType", input.getFlType())
                .equal("createdBy", input.getCreator())
                .build(ModelMySqlModel.class);
        return jobWhere;
    }

    /**
     * Update model enable field
     */
    public void enable(EnableApi.Input input) {

        modelRepository.updateById(input.getId(), "enable", input.isEnable(), ModelMySqlModel.class);

        ModelMySqlModel modelMySqlModel = modelRepository.findOne("id", input.getId(), ModelMySqlModel.class);

        ModelManager.refreshModelEnable(modelMySqlModel.getModelId(), input.isEnable());
    }


    public ModelStatusOutput checkAvailable(String modelId) {
        try {
            if (ModelManager.getModelEnable(modelId)) {
                return ModelStatusOutput.of(
                        CacheObjects.getMemberId(),
                        CacheObjects.getMemberName(),
                        MemberModelStatusEnum.available
                );
            }

            return ModelStatusOutput.of(
                    CacheObjects.getMemberId(),
                    CacheObjects.getMemberName(),
                    MemberModelStatusEnum.unavailable
            );
        } catch (StatusCodeWithException e) {
            return ModelStatusOutput.of(
                    CacheObjects.getMemberId(),
                    CacheObjects.getMemberName(),
                    MemberModelStatusEnum.unavailable
            );
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String modelId,
                             PredictFeatureDataSource featureSource,
                             String dataSourceId,
                             String sqlScript,
                             String sqlConditionField) throws StatusCodeWithException {

        ModelMySqlModel model = findOne(modelId);
        if (model == null) {
            throw new StatusCodeWithException("未查找到模型！" + modelId, StatusCode.PARAMETER_VALUE_INVALID);
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

        SaveApi.Input order = createOrder(input);

        String responseId = ServiceResultOutput.buildId();
        PredictResult result = null;
        Integer responseCode = 0;
        try {
            JObject data = JObject.create(input.getData())
                    .append("requestId", input.getRequestId())
                    .append("memberId", input.getCustomerId());
            result = forward(data);

            //更新订单信息
            order.setStatus(ServiceOrderEnum.SUCCESS.getValue());
            serviceOrderService.save(order);

            return ServiceResultOutput.of(input.getRequestId(), responseId, result);
        } catch (StatusCodeWithException e) {
            //更新订单信息
            order.setStatus(ServiceOrderEnum.FAILED.getValue());
            serviceOrderService.save(order);
            responseCode = StatusCode.SYSTEM_ERROR.getCode();
            throw e;
        } finally {
            callLog(input, order.getId(), responseId, result, responseCode);
        }
    }

    private void callLog(RouteApi.Input input, String orderId, String responseId, PredictResult result, Integer responseCode) {
        ServiceCallLogMysqlModel callLog = new ServiceCallLogMysqlModel();
        callLog.setServiceType(ServiceTypeEnum.MachineLearning.name());
        callLog.setOrderId(orderId);
        callLog.setServiceId(input.getServiceId());
        callLog.setServiceName(getModelName(input.getServiceId()));
        callLog.setRequestData(input.getData());
        callLog.setRequestPartnerId(input.getCustomerId());
        callLog.setRequestPartnerName(CacheObjects.getPartnerName(input.getCustomerId()));
        callLog.setRequestId(input.getRequestId());
        callLog.setRequestIp(ServiceUtil.getIpAddr(input.request));
        callLog.setResponseCode(responseCode);
        callLog.setResponseId(responseId);
        callLog.setResponsePartnerId(CacheObjects.getMemberId());
        callLog.setResponsePartnerName(CacheObjects.getMemberName());
        callLog.setResponseData(JSON.toJSONString(result));
        callLog.setCallByMe(0);
        callLog.setResponseStatus(getResponseStatus(result));
        serviceCallLogService.save(callLog);
    }

    private String getResponseStatus(PredictResult result) {
        return result == null ? ServiceCallStatusEnum.RESPONSE_ERROR.name() : ServiceCallStatusEnum.SUCCESS.name();
    }

    private SaveApi.Input createOrder(RouteApi.Input input) {
        SaveApi.Input order = new SaveApi.Input();
        order.setServiceId(input.getServiceId());
        order.setServiceName(getModelName(input.getServiceId()));
        order.setServiceType(ServiceTypeEnum.MachineLearning.name());
        order.setRequestPartnerId(input.getCustomerId());
        order.setRequestPartnerName(CacheObjects.getPartnerName(input.getCustomerId()));
        order.setResponsePartnerId(CacheObjects.getMemberId());
        order.setResponsePartnerName(CacheObjects.getMemberName());
        //是否自己发起的订单
        order.setOrderType(0);
        serviceOrderService.save(order);
        return order;
    }

    private String getModelName(String serviceId) {
        //TODO 需要修改
        return "测试";
    }

    private PredictResult forward(JObject data) throws StatusCodeWithException {
        ModelServiceProcessor processor = new ModelServiceProcessor();
        return processor.process(data, new ModelMySqlModel());
    }
}
