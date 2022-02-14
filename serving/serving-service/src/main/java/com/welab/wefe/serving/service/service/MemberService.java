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
import com.welab.wefe.serving.service.api.member.QueryApi;
import com.welab.wefe.serving.service.database.serving.entity.MemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.MemberRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Transactional(rollbackFor = Exception.class)
    public void save(String memberId, String name, String baseUrl, String publicKey) {

        MemberMySqlModel model = memberRepository.findOne("memberId", memberId, MemberMySqlModel.class);

        if (model == null) {
            model = new MemberMySqlModel();
        }

        model.setMemberId(memberId);
        model.setName(name);
        model.setApi(baseUrl);
        model.setPublicKey(publicKey);

        memberRepository.save(model);
    }

    public MemberMySqlModel findOne(String memberId) {
        return memberRepository.findOne("memberId", memberId, MemberMySqlModel.class);
    }

    /**
     * Paging query
     */
    public PagingOutput<QueryApi.Output> query(QueryApi.Input input) {

        Specification<MemberMySqlModel> where = Where
                .create()
                .equal("memberId", input.getMemberId())
                .contains("name", input.getName())
                .build(MemberMySqlModel.class);

        PagingOutput<MemberMySqlModel> page = memberRepository.paging(where, input);

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
