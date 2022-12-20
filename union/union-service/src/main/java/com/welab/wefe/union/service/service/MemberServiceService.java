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

package com.welab.wefe.union.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.member.MemberServiceQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.MemberService;
import com.welab.wefe.common.data.mongodb.repo.MemberServiceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.union.service.api.service.PutApi;
import com.welab.wefe.union.service.api.service.QueryApi;
import com.welab.wefe.union.service.dto.member.ApiMemberServiceQueryOutput;
import com.welab.wefe.union.service.service.contract.MemberServiceContractService;
import com.welab.wefe.union.service.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberServiceService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberServiceService.class);
    @Autowired
    private MemberServiceContractService memberServiceContractService;
    @Autowired
    private MemberServiceMongoReop memberServiceMongoReop;

    public void add(PutApi.Input input) throws StatusCodeWithException {
        try {
            memberServiceContractService.save(ModelMapper.map(input, MemberService.class));
        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public PageOutput<ApiMemberServiceQueryOutput> query(QueryApi.Input input) throws StatusCodeWithException {
        try {
            PageOutput<MemberServiceQueryOutput> page = memberServiceMongoReop.find(
                    input.getPageIndex(),
                    input.getPageSize(),
                    input.getServiceId(),
                    input.getMemberId(),
                    input.getMemberName(),
                    input.getServiceName(),
                    input.getServiceType()
            );

            List<ApiMemberServiceQueryOutput> list = page.getList().stream()
                    .map(MapperUtil::transferToMemberServiceQueryOutput)
                    .collect(Collectors.toList());

            return new PageOutput<>(page.getPageIndex(), page.getTotal(), page.getPageSize(), page.getTotalPage(), list
            );
        } catch (Exception e) {
            LOG.error("Failed to query member information in pagination:", e);
            throw StatusCodeWithException.of(StatusCode.SYSTEM_ERROR, "Failed to query member information in pagination");
        }
    }
}
