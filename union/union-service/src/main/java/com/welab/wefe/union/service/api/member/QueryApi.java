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

package com.welab.wefe.union.service.api.member;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.member.MemberQueryOutput;
import com.welab.wefe.union.service.mapper.MemberMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jervis
 **/
@Api(path = "member/query", name = "member_query", rsaVerify = true, login = false)
public class QueryApi extends AbstractApi<QueryApi.Input, PageOutput<MemberQueryOutput>> {
    @Autowired
    protected MemberMongoReop memberMongoReop;

    protected MemberMapper mMapper = Mappers.getMapper(MemberMapper.class);

    @Override
    protected ApiResult<PageOutput<MemberQueryOutput>> handle(QueryApi.Input input) throws StatusCodeWithException {
        try {
            PageOutput<Member> page = memberMongoReop.query(
                    input.getPageIndex(),
                    input.getPageSize(),
                    input.getId(),
                    input.getName()
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

    public static class Input extends BaseInput{
        private String id;
        private String name;


        private Integer pageIndex = 0;
        private Integer pageSize = 10;

        //region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(Integer pageIndex) {
            this.pageIndex = pageIndex;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
//endregion
    }

}
