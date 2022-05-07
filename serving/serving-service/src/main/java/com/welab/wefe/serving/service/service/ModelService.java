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

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.service.api.model.EnableApi;
import com.welab.wefe.serving.service.api.model.QueryApi;
import com.welab.wefe.serving.service.api.model.SaveModelApi;
import com.welab.wefe.serving.service.database.serving.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.database.serving.repository.ModelRepository;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceStatusEnum;
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
    private ServiceService serviceService;

    @Transactional(rollbackFor = Exception.class)
    public void save(SaveModelApi.Input input) {

        modelMemberService.save(input.getModelId(), input.getMemberParams());

        //Add partner
        partnerService.save(input.getMemberParams());

        //open or activate
        openService(input);

        //save model
        upsert(input);

        //TODO 考虑模型是否放到service表
    }

    private void upsert(SaveModelApi.Input input) {
        ModelMySqlModel model = findOne(input.getModelId());
        if (model == null) {
            model = new ModelMySqlModel();
        }

        convertTo(input, model);

        model.setUpdatedTime(new Date());
        modelRepository.save(model);
    }

    private ModelMySqlModel convertTo(SaveModelApi.Input input, ModelMySqlModel model) {
        BeanUtils.copyProperties(input, model);
        return model;
    }

    private void openService(SaveModelApi.Input input) {
        if (JobMemberRole.provider.equals(input.getMyRole())) {
            openPartnerService(input.getModelId(), input.getMemberParams());
        } else {
            activatePartnerService(input.getModelId(), input.getMemberParams());
        }
    }

    /**
     * promoter：Activate model service
     *
     * @param modelId
     * @param memberParams
     */
    private void activatePartnerService(String modelId, List<MemberParams> memberParams) {
        memberParams.forEach(
                x -> {
                    if (JobMemberRole.provider.equals(x.getRole())) {
                        try {
                            clientServiceService.save(
                                    modelId,
                                    x.getMemberId(),
                                    x.getPublicKey(),
                                    ServiceClientTypeEnum.ACTIVATE,
                                    ServiceStatusEnum.UNUSED
                            );
                        } catch (StatusCodeWithException e) {
                            LOG.error("模型服务激活失败: {]", e.getMessage());
                        }
                    }
                }
        );
    }

    /**
     * provider: add opening record
     *
     * @param modelId
     * @param memberParams
     */
    private void openPartnerService(String modelId, List<MemberParams> memberParams) {
        memberParams.forEach(
                x -> {
                    if (JobMemberRole.promoter.equals(x.getRole())) {
                        try {
                            clientServiceService.save(
                                    modelId,
                                    x.getMemberId(),
                                    x.getPublicKey(),
                                    ServiceClientTypeEnum.OPEN,
                                    ServiceStatusEnum.UNUSED
                            );
                        } catch (StatusCodeWithException e) {
                            LOG.error("开通模型服务失败：{}", e.getMessage());
                        }
                    }
                }
        );
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
        /**
         * Restrict queries to models that are initiators only
         */
        Specification<ModelMySqlModel> jobWhere = Where
                .create()
                .contains("modelId", input.getModelId())
                .contains("name", input.getName())
                .equal("algorithm", input.getAlgorithm())
                .equal("flType", input.getFlType())
                .equal("createdBy", input.getCreator())
                .build(ModelMySqlModel.class);

        PagingOutput<ModelMySqlModel> page = modelRepository.paging(jobWhere, input);

        Specification<ModelMemberMySqlModel> where = Where
                .create()
                .contains("memberId", CacheObjects.getMemberId())
                //.equal("role", JobMemberRole.promoterPredictByHorz)
                .build(ModelMemberMySqlModel.class);

        PagingOutput<ModelMemberMySqlModel> memberPage = modelMemberRepository.paging(where, input);

        List<QueryApi.Output> list = page
                .getList()
                .stream()
                // .filter(x -> member.contains(x.getModelId()))
                .map(x -> ModelMapper.map(x, QueryApi.Output.class))
                .collect(Collectors.toList());

        list.forEach(x -> {
            for (ModelMemberMySqlModel model : memberPage.getList()) {
                if (model.getModelId().equals(x.getModelId())) {
                    x.setMyRole(model.getRole());
                }
            }
        });


        return PagingOutput.of(
                page.getTotal(),
                list
        );
    }

    /**
     * Update model enable field
     */
    public void enable(EnableApi.Input input) {

        modelRepository.updateById(input.getId(), "enable", input.isEnable(), ModelMySqlModel.class);

        ModelMySqlModel modelMySqlModel = modelRepository.findOne("id", input.getId(), ModelMySqlModel.class);

        ModelManager.refreshModelEnable(modelMySqlModel.getModelId(), input.isEnable());
    }
}
