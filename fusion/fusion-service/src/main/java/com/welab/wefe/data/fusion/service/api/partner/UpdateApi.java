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

package com.welab.wefe.data.fusion.service.api.partner;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jacky.jiang
 */
@Api(path = "partner/update", name = "添加合作伙伴", desc = "添加合作伙伴")
public class UpdateApi extends AbstractNoneOutputApi<UpdateApi.Input> {

    @Autowired
    PartnerService partnerService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        partnerService.update(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "id", require = true)
        String id;

        @Check(name = "联邦成员ID", require = true)
        String memberId;

        @Check(name = "联邦成员名称", require = true)
        String memberName;

        @Check(name = "联帮成员fusion系统公钥", require = true)
        String rsaPublicKey;

        @Check(name = "请求路径", require = true)
        String baseUrl;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getRsaPublicKey() {
            return rsaPublicKey;
        }

        public void setRsaPublicKey(String rsaPublicKey) {
            this.rsaPublicKey = rsaPublicKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}