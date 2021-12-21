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

package com.welab.wefe.manager.service.api.member;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberUpdateInput;
import com.welab.wefe.manager.service.service.MemberContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/update", name = "member_update")
public class UpdateApi extends AbstractApi<MemberUpdateInput, AbstractApiOutput> {


    @Autowired
    private MemberContractService memberContractService;

    @Autowired
    private MemberMongoReop memberMongoReop;

    @Override
    protected ApiResult<AbstractApiOutput> handle(MemberUpdateInput input) throws StatusCodeWithException {
        try {
            Member member = memberMongoReop.findMemberId(input.getId());
            if (member == null) {
                throw new StatusCodeWithException("data does not exist ", StatusCode.SYSTEM_ERROR);
            }

            if (input.getFreezed() != null) {
                member.setFreezed(String.valueOf(input.getFreezed() ? 1 : 0));
            }
            if (input.getLostContact() != null) {
                member.setLostContact(String.valueOf(input.getLostContact() ? 1 : 0));
            }

            memberContractService.update(member);
        } catch (Exception e) {
            LOG.error("Failed to update member: ", e);
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}
