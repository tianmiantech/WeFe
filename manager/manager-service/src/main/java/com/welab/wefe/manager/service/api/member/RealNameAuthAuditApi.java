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

package com.welab.wefe.manager.service.api.member;

import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.RealNameAuthInput;
import com.welab.wefe.manager.service.service.MemberContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "member/realname/auth/audit", name = "member_realname_auth_audit")
public class RealNameAuthAuditApi extends AbstractApi<RealNameAuthInput, AbstractApiOutput> {
    @Autowired
    protected MemberContractService memberContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(RealNameAuthInput input) throws StatusCodeWithException {
        MemberExtJSON memberExtJSON = new MemberExtJSON();
        memberExtJSON.setRealNameAuthStatus(input.getRealNameAuthStatus());
        memberExtJSON.setAuditComment(input.getAuditComment());
        if(input.getRealNameAuthStatus() == 2) {
            memberExtJSON.setRealNameAuthTime(System.currentTimeMillis());
        }
        memberContractService.updateExtJson(input.getId(), memberExtJSON);
        return success();
    }

}
