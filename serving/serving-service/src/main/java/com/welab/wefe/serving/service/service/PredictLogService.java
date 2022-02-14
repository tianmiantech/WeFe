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
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.service.api.logger.QueryApi;
import com.welab.wefe.serving.service.database.serving.entity.PredictLogMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.PredictLogRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Forecast log
 *
 * @author hunter.zhao
 */
@Service
public class PredictLogService {

    @Autowired
    private PredictLogRepository predictLogRepository;

    @Transactional(rollbackFor = Exception.class)
    public void save(String seqNo, String modelId, String memberId, Algorithm algorithm, FederatedLearningType flType, JobMemberRole myRole, String request, String response, long spend, boolean result) {

        PredictLogMySqlModel log = new PredictLogMySqlModel();
        log.setSeqNo(seqNo);
        log.setMemberId(memberId);
        log.setModelId(modelId);
        log.setAlgorithm(algorithm);
        log.setFlType(flType);
        log.setMyRole(myRole);
        log.setRequest(request);
        log.setResponse(response);
        log.setSpend(spend);
        log.setResult(result);

        predictLogRepository.save(log);
    }


    /**
     * paging query
     */
    public PagingOutput<QueryApi.Output> query(QueryApi.Input input) {

        Specification<PredictLogMySqlModel> where = Where
                .create()
                .equal("seqNo", input.getSeqNo())
                .equal("memberId", input.getMemberId())
                .equal("modelId", input.getModelId())
                .equal("algorithm", input.getAlgorithm())
                .equal("flType", input.getFlType())
                .equal("myRole", input.getMyRole())
                .build(PredictLogMySqlModel.class);

        PagingOutput<PredictLogMySqlModel> page = predictLogRepository.paging(where, input);

        List<QueryApi.Output> list = page
                .getList()
                .stream()
                // .filter(x -> member.contains(x.getModelId()))
                .map(x -> ModelMapper.map(x, QueryApi.Output.class))
                .collect(Collectors.toList());

        return PagingOutput.of(
                page.getTotal(),
                list
        );
    }
}
