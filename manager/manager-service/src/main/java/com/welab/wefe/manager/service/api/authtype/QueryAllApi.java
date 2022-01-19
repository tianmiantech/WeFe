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

package com.welab.wefe.manager.service.api.authtype;

import com.welab.wefe.common.data.mongodb.repo.MemberAuthTypeMongoRepo;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.common.data.mongodb.dto.member.MemberAuthQueryOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query the default tag of the dataset
 *
 * @author yuxin.zhang
 */
@Api(path = "member/authtype/query", name = "member_authtype_query")
public class QueryAllApi extends AbstractApi<BaseInput, JObject> {

    @Autowired
    protected MemberAuthTypeMongoRepo memberAuthTypeMongoRepo;

    @Override
    protected ApiResult<JObject> handle(BaseInput input) {
        List<MemberAuthQueryOutput> list = memberAuthTypeMongoRepo.findList().stream().map(memberAuthType -> {
            MemberAuthQueryOutput memberAuthQueryOutput = new MemberAuthQueryOutput();
            memberAuthQueryOutput.setTypeId(memberAuthType.getTypeId());
            memberAuthQueryOutput.setTypeName(memberAuthType.getTypeName());
            memberAuthQueryOutput.setStatus(memberAuthType.getStatus());
            return memberAuthQueryOutput;
        }).collect(Collectors.toList());
        return success(JObject.create("list", JObject.toJSON(list)));
    }


}
