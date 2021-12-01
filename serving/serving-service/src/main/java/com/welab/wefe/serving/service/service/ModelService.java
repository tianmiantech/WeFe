/**
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

package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.Algorithm;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.model.EnableApi;
import com.welab.wefe.serving.service.api.model.QueryApi;
import com.welab.wefe.serving.service.database.serving.entity.MemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.MemberRepository;
import com.welab.wefe.serving.service.database.serving.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.database.serving.repository.ModelRepository;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.manager.ModelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelMemberRepository modelMemberRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Transactional(rollbackFor = Exception.class)
    public void save(String modelId, Algorithm algorithm, FederatedLearningType flType, String modelParam, List<MemberParams> memberParams) {

        ModelMySqlModel model = modelRepository.findOne("modelId", modelId, ModelMySqlModel.class);

        if (model == null) {
            model = new ModelMySqlModel();
        }

        Specification<ModelMemberMySqlModel> where = Where.
                create().equal("modelId", modelId)
                .build(ModelMemberMySqlModel.class);

        List<ModelMemberMySqlModel> modelList = modelMemberRepository.findAll(where);
        modelMemberRepository.deleteAll(modelList);


        /**
         * Model member information
         */
        List<ModelMemberMySqlModel> list = new ArrayList<>();
        memberParams.forEach(x -> {
            ModelMemberMySqlModel member = new ModelMemberMySqlModel();
            member.setModelId(modelId);
            member.setMemberId(x.getMemberId());
            member.setRole(x.getRole());
            list.add(member);
        });

        modelMemberRepository.saveAll(list);

        /**
         * Member basic information
         */
        List<MemberMySqlModel> members = new ArrayList<>();
        for (MemberParams param : memberParams) {

            MemberMySqlModel member = memberRepository.findOne("memberId", param.getMemberId(), MemberMySqlModel.class);
            if (member == null) {
                member = new MemberMySqlModel();
            }

            member.setMemberId(param.getMemberId());
            member.setName(param.getName());
            member.setPublicKey(param.getPublicKey());
            members.add(member);
        }

        memberRepository.saveAll(members);


        model.setModelId(modelId);
        model.setModelParam(modelParam);
        model.setAlgorithm(algorithm);
        model.setFlType(flType);
        model.setUpdatedTime(new Date());

        modelRepository.save(model);

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
