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

import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.member.MemberOutput;
import com.welab.wefe.union.service.service.MemberContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Jervis
 **/
@Api(path = "member/update_serving_base_url", name = "member_update_serving_base_url", rsaVerify = true, login = false)
public class UpdateServingBaseUrlApi extends AbstractApi<UpdateServingBaseUrlApi.Input, MemberOutput> {

    @Autowired
    private MemberContractService memberContractService;

    @Override
    protected ApiResult<MemberOutput> handle(Input input) throws StatusCodeWithException {
        try {
            MemberExtJSON extJSON = new MemberExtJSON();
            extJSON.setServingBaseUrl(input.servingBaseUrl);
            memberContractService.updateExtJson(input.curMemberId, extJSON);
        } catch (StatusCodeWithException e) {
            throw e;
        }

        return success();
    }


    public static class Input extends BaseInput {
        @Check(require = true)
        private String servingBaseUrl;

        public String getServingBaseUrl() {
            return servingBaseUrl;
        }

        public void setServingBaseUrl(String servingBaseUrl) {
            this.servingBaseUrl = servingBaseUrl;
        }
    }

}
