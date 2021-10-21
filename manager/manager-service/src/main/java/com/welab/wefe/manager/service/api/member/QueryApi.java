/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.manager.service.api.member;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberQueryInput;
import com.welab.wefe.manager.service.dto.member.MemberQueryOutput;
import com.welab.wefe.manager.service.mapper.MemberMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/query", name = "member_query", login = false)
public class QueryApi extends AbstractApi<MemberQueryInput, PageOutput<MemberQueryOutput>> {
    @Autowired
    protected MemberMongoReop memberMongoReop;

    protected MemberMapper mMapper = Mappers.getMapper(MemberMapper.class);

    @Override
    protected ApiResult<PageOutput<MemberQueryOutput>> handle(MemberQueryInput input) throws StatusCodeWithException {
        try {
            PageOutput<Member> page = memberMongoReop.query(
                    input.getPageIndex(),
                    input.getPageSize(),
                    input.getId(),
                    input.getName(),
                    input.getHidden(),
                    input.getFreezed(),
                    input.getLostContact(),
                    input.getStatus()
            );

            List<MemberQueryOutput> list = page.getList().stream()
                    .map(mMapper::transfer)
                    .collect(Collectors.toList());

            return success(new PageOutput<>(
                    page.getPageIndex(),
                    page.getTotal(),
                    page.getPageSize(),
                    page.getTotalPage(),
                    list
            ));
        } catch (Exception e) {
            LOG.error("Failed to query member information in pagination:", e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "Failed to query member information in pagination");
        }
    }
}
