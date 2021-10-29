/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.manager.service.api.member;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberExtJsonInput;
import com.welab.wefe.manager.service.service.MemberContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "member/update_ext_json", name = "member_update_ext_json", login = false)
public class UpdateExtJsonApi extends AbstractApi<MemberExtJsonInput, AbstractApiOutput> {
    @Autowired
    protected MemberContractService memberContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(MemberExtJsonInput input) throws StatusCodeWithException {
        memberContractService.updateExtJson(input);
        return success();
    }

}
