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
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.authtype.MemberAuthTypeDeleteInput;
import com.welab.wefe.manager.service.service.MemberAuthTypeContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "member/authtype/delete", name = "member_authtype_delete")
public class DeleteApi extends AbstractApi<MemberAuthTypeDeleteInput, AbstractApiOutput> {

    @Autowired
    private MemberAuthTypeContractService memberAuthTypeContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(MemberAuthTypeDeleteInput input) throws StatusCodeWithException {
        LOG.info("DeleteApi handle..");
        try {

            memberAuthTypeContractService.deleteByTypeId(input.getTypeId());
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
        }

        return success();
    }

}
