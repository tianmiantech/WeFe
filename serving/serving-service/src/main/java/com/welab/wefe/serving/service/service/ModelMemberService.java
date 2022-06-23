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

import com.google.common.collect.Lists;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.sdk.dto.ProviderParams;
import com.welab.wefe.serving.service.database.entity.ModelMemberBaseModel;
import com.welab.wefe.serving.service.database.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.entity.PartnerMysqlModel;
import com.welab.wefe.serving.service.database.repository.ModelMemberBaseRepository;
import com.welab.wefe.serving.service.database.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.dto.ModelStatusOutput;
import com.welab.wefe.serving.service.enums.MemberModelStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Service
public class ModelMemberService {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private ModelMemberRepository modelMemberRepository;

    @Autowired
    private ModelMemberBaseRepository modelMemberBaseRepository;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private ServiceService serviceService;

    public List<ModelMemberBaseModel> findModelMemberBase(String modelId, String role) {
        return modelMemberBaseRepository.findAllByModelIdAndRole(modelId, role);
    }


    /**
     * Get partner information
     */
    public List<ProviderParams> findProviders(String modelId) {

        List<ModelMemberBaseModel> modelMember = findModelMemberBase(modelId, JobMemberRole.provider.name());

        return modelMember
                .stream()
                .map(x -> ModelMapper.map(x, ProviderParams.class))
                .collect(Collectors.toList());
    }

    /**
     * Save model members
     *
     * @param modelId
     * @param memberParams
     */
    public void save(String modelId, List<MemberParams> memberParams) {
        Specification<ModelMemberMySqlModel> where = Where.
                create()
                .equal("modelId", modelId)
                .build(ModelMemberMySqlModel.class);
        List<ModelMemberMySqlModel> modelList = modelMemberRepository.findAll(where);
        modelMemberRepository.deleteAll(modelList);

        memberParams
                .stream()
                .forEach(x -> save(modelId, x.getMemberId(), x.getRole()));
    }

    public void save(String modelId, String memberId, JobMemberRole role) {

        ModelMemberMySqlModel member = modelMemberRepository.findByModelIdAndMemberIdAndRole(modelId, memberId, role);
        if (member == null) {
            member = new ModelMemberMySqlModel();
        }
        member.setModelId(modelId);
        member.setMemberId(memberId);
        member.setRole(role);
        modelMemberRepository.save(member);
    }

    public List<ModelStatusOutput> checkAvailableByModelIdAndMemberId(String modelId, String memberId) {
        List<ModelMemberMySqlModel> list = findListByModelIdAndMemberId(modelId, memberId);

        return isProvider(list) ?
                Lists.newArrayList() : list.stream()
                .filter(x -> !CacheObjects.getMemberId().equals(x.getMemberId()))
                .map(x -> checkAvailable(modelId, x))
                .collect(Collectors.toList());
    }

    private boolean isProvider(List<ModelMemberMySqlModel> list) {
        return list.stream().anyMatch(
                member -> member.getMemberId().equals(CacheObjects.getMemberId())
                        && member.getRole().equals(JobMemberRole.provider)
        );
    }

    private ModelStatusOutput checkAvailable(String modelId, ModelMemberMySqlModel model) {

        ModelStatusOutput output = callProvider(modelId, model.getMemberId());
        output.setUrl(findPartnerUrl(model.getMemberId()));
        updateModelStatus(model, output.getStatus());

        return output;
    }

    private String findPartnerUrl(String partnerId) {
        PartnerMysqlModel partnerMysqlModel = partnerService.findOne(partnerId);
        return partnerMysqlModel == null ? "" : partnerMysqlModel.getServingBaseUrl();
    }


    private void updateModelStatus(ModelMemberMySqlModel model, MemberModelStatusEnum status) {
        model.setStatus(status);
        model.setUpdatedTime(new Date());
        modelMemberRepository.save(model);
    }

    private ModelStatusOutput callProvider(String modelId, String memberId) {
        String servingBaseUrl = findServingBaseUrl(memberId);

        TreeMap<String, Object> param = new TreeMap<>();
        param.put("modelId", modelId);
        try {
            return serviceService.callOtherPartnerServing(
                    servingBaseUrl,
                    "model/provider/status/check",
                    param,
                    ModelStatusOutput.class
            );
        } catch (StatusCodeWithException e) {
            LOG.error("合作方 {} 服务失联", CacheObjects.getPartnerName(memberId));
            return ModelStatusOutput.of(
                    memberId,
                    CacheObjects.getPartnerName(memberId),
                    MemberModelStatusEnum.offline
            );
        }
    }

    private String findServingBaseUrl(String partnerId) {
        PartnerMysqlModel partner = partnerService.findOne(partnerId);
        return partner.getServingBaseUrl();
    }

    public List<ModelMemberMySqlModel> findListByModelIdAndMemberId(String modelId, String memberId) {
        Specification<ModelMemberMySqlModel> where = Where.
                create()
                .equal("modelId", modelId)
                .equal("memberId", memberId)
                .build(ModelMemberMySqlModel.class);
        return modelMemberRepository.findAll(where);
    }

}
