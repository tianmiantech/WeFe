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

import com.welab.wefe.common.data.mongodb.dto.member.RealnameAuthInfoQueryOutput;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/realname/authInfo/query", name = "member/realname/authInfo/query", allowAccessWithSign = true)
public class RealnameAuthInfoQueryApi extends AbstractApi<RealnameAuthInfoQueryApi.Input, RealnameAuthInfoQueryOutput> {
    @Autowired
    private MemberService memberService;

    @Override
    protected ApiResult<RealnameAuthInfoQueryOutput> handle(RealnameAuthInfoQueryApi.Input input) throws StatusCodeWithException {
        return success(memberService.queryRealNameAuthInfo(input));
    }

    public static class Input extends BaseInput {

    }
}
