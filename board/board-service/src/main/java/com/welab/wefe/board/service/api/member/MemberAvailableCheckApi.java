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

package com.welab.wefe.board.service.api.member;

import com.welab.wefe.board.service.service.ServiceCheckService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.checkpoint.dto.MemberAvailableCheckOutput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lonnie
 */
@Api(path = "/member/available", name = "Check whether the member’s system services are available")
public class MemberAvailableCheckApi extends AbstractApi<MemberAvailableCheckApi.Input, MemberAvailableCheckOutput> {

    @Autowired
    private ServiceCheckService serviceCheckService;

    @Override
    protected ApiResult<MemberAvailableCheckOutput> handle(Input input) throws StatusCodeWithException {
        MemberAvailableCheckOutput output = serviceCheckService.getMemberAvailableInfo(input.memberId);
        return success(output);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "成员id", require = true)
        public String memberId;

        public Input() {
        }

        public Input(String memberId) {
            this.memberId = memberId;
        }
    }

}
