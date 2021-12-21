/*
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

import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/map", name = "member_map", rsaVerify = true, login = false)
public class QueryMemberNameApi extends AbstractApi<BaseInput, JObject> {
    @Autowired
    protected MemberMongoReop memberMongoReop;

    private static JObject apply(Member member) {
        return JObject.create()
                .put("name", member.getName())
                .put("hidden", Integer.parseInt(member.getHidden()))
                .put("freezed", Integer.parseInt(member.getFreezed()))
                .put("lostContact", Integer.parseInt(member.getLostContact()));
    }

    @Override
    protected ApiResult<JObject> handle(BaseInput input) throws StatusCodeWithException {
        List<Member> memberList = memberMongoReop.find(null);
        Map<String, JObject> collect = memberList.stream().collect(
                Collectors.toMap(
                        Member::getMemberId,
                        QueryMemberNameApi::apply
                ));
        return success(JObject.create(collect));
    }

}
