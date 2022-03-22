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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.MemberAuthType;
import com.welab.wefe.common.data.mongodb.repo.MemberAuthTypeMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.authtype.MemberAuthTypeAddInput;
import com.welab.wefe.manager.service.service.MemberAuthTypeContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/authtype/add", name = "member_authtype_add")
public class AddApi extends AbstractApi<MemberAuthTypeAddInput, AbstractApiOutput> {

    @Autowired
    private MemberAuthTypeContractService memberAuthTypeContractService;

    @Autowired
    private MemberAuthTypeMongoRepo memberAuthTypeMongoRepo;

    @Override
    protected ApiResult<AbstractApiOutput> handle(MemberAuthTypeAddInput input) throws StatusCodeWithException {
        LOG.info("AddApi handle..");
        try {
            boolean isExist = memberAuthTypeMongoRepo.exists(input.getTypeName());
            if (isExist) {
                throw new StatusCodeWithException("该类型已存在",StatusCode.DATA_EXISTED);
            }
            MemberAuthType memberAuthType = new MemberAuthType();
            memberAuthType.setTypeName(input.getTypeName());
            memberAuthTypeContractService.add(memberAuthType);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}
