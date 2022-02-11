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

package com.welab.wefe.union.service.api.member;

import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.member.MemberQueryOutput;
import com.welab.wefe.union.service.mapper.MemberMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aaron.li
 **/
@Api(path = "member/query_all", name = "member_query_all", rsaVerify = true, login = false)
public class QueryAllApi extends AbstractApi<QueryAllApi.Input, JObject> {
    @Autowired
    private MemberMongoReop memberMongoReop;

    protected MemberMapper mMapper = Mappers.getMapper(MemberMapper.class);

    @Override
    protected ApiResult<JObject> handle(QueryAllApi.Input input) {
        List<Member> memberList = memberMongoReop.find(input.id);

        List<MemberQueryOutput> memberQueryOutputList = memberList.stream()
                .map(member -> {
                    // does not contain logo
                    if (!input.includeLogo) {
                        member.setLogo(null);
                    }
                    return mMapper.transfer(member);
                })
                .collect(Collectors.toList());

        return success(JObject.create("list", JObject.toJSON(memberQueryOutputList)));
    }

    public static class Input extends AbstractApiInput {
        /**
         * Primary key ID, if it is empty, query all
         */
        private String id;

        /**
         * Whether to include logo
         */
        private Boolean includeLogo = true;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Boolean getIncludeLogo() {
            return includeLogo;
        }

        public void setIncludeLogo(Boolean includeLogo) {
            this.includeLogo = includeLogo;
        }
    }
}
