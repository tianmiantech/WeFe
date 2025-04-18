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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.service.api.model.EnableApi;
import com.welab.wefe.serving.service.api.model.QueryApi;
import com.welab.wefe.serving.service.api.model.SaveModelApi;
import com.welab.wefe.serving.service.database.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.database.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.dto.ModelStatusOutput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.MemberModelStatusEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.manager.ModelManager;
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

    private final String API_PREFIX_2 = "predict/";

    private final String API_PREFIX_1 = "/api/";
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
        partnerService.upsert(input.getMemberParams());
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
            model.setCreatedBy(CurrentAccountUtil.get() == null ? "board推送" : CurrentAccountUtil.get().getId());
        }

        convertTo(input, model);

        model.setUpdatedTime(new Date());
        model.setUpdatedBy(CurrentAccountUtil.get() == null ? "board推送" : CurrentAccountUtil.get().getId());
        modelRepository.save(model);

        CacheObjects.refreshServiceMap();

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
            openPartnerService(input.getServiceId(), input.getName(), input.getMemberParams());
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
                    CacheObjects.getSecretKeyType(),
                    API_PREFIX_1 + setModelServiceUrl(serviceId),
                    ServiceTypeEnum.MachineLearning
            );
        } catch (StatusCodeWithException e) {
            LOG.error("模型服务激活失败: {}", e.getMessage());
        }
    }

    private String setModelServiceUrl(String serviceId) {
        return API_PREFIX_2 + serviceId;
    }

    /**
     * provider: add opening record
     *
     * @param modelId
     * @param memberParams
     */
    private void openPartnerService(String modelId, String modelName, List<MemberParams> memberParams) {
        memberParams.stream()
                .filter(x -> JobMemberRole.promoter.equals(x.getRole()))
                .forEach(x -> openService(modelId, modelName, x));
    }

    private void openService(String modelId, String name, MemberParams x) {
        try {
            clientServiceService.openService(
                    modelId,
                    name,
                    API_PREFIX_1 + setModelServiceUrl(modelId),
                    x.getMemberId(),
                    x.getPublicKey(),
                    ServiceTypeEnum.MachineLearning,
                    x.getSecretKeyType()
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
                             String serviceName,
                             PredictFeatureDataSource featureSource,
                             String dataSourceId,
                             String sqlScript,
                             String sqlConditionField) throws StatusCodeWithException {

        TableModelMySqlModel model = findOne(serviceId);
        if (model == null) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "未查找到模型！" + serviceId);
        }

        if (featureSource.equals(PredictFeatureDataSource.sql)) {
            model.setName(serviceName);
            model.setFeatureSource(featureSource);
            model.setSqlScript(sqlScript);
            model.setSqlConditionField(sqlConditionField);
            model.setDataSourceId(dataSourceId);
            model.setUpdatedBy(CurrentAccountUtil.get().getId());
            model.setUpdatedTime(new Date());
            modelRepository.save(model);
        } else {
            model.setName(serviceName);
            model.setFeatureSource(featureSource);
            model.setDataSourceId(null);
            model.setSqlScript("");
            model.setSqlConditionField("");
            model.setUpdatedBy(CurrentAccountUtil.get().getId());
            model.setUpdatedTime(new Date());
            modelRepository.save(model);
        }
        clientServiceService.updateAllByServiceId(model.getServiceId(), model.getName(),
                ServiceService.SERVICE_PRE_URL + model.getUrl(), model.getServiceType());
    }
}
