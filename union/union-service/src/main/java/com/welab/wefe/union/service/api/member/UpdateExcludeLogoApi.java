/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.union.service.api.member;

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
 * Update member information (not including logo)
 *
 * @author aaron.li
 **/
@Api(path = "member/update_exclude_logo", name = "member_update_exclude_logo", rsaVerify = true, login = false)
public class UpdateExcludeLogoApi extends AbstractApi<UpdateExcludeLogoApi.Input, MemberOutput> {
    @Autowired
    private MemberContractService memberContractService;

    @Override
    protected ApiResult<MemberOutput> handle(Input input) throws StatusCodeWithException {
        memberContractService.updateExcludeLogo(input);
        return success();
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private String id;
        private String name;
        private String mobile;
        private String email;
        private Boolean allowOpenDataSet;
        private Boolean hidden;
        private Boolean freezed;
        private Boolean lostContact;
        @Check(require = true)
        private String publicKey;
        private String gatewayUri;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Boolean getAllowOpenDataSet() {
            return allowOpenDataSet;
        }

        public void setAllowOpenDataSet(Boolean allowOpenDataSet) {
            this.allowOpenDataSet = allowOpenDataSet;
        }

        public Boolean getHidden() {
            return hidden;
        }

        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }

        public Boolean getFreezed() {
            return freezed;
        }

        public void setFreezed(Boolean freezed) {
            this.freezed = freezed;
        }

        public Boolean getLostContact() {
            return lostContact;
        }

        public void setLostContact(Boolean lostContact) {
            this.lostContact = lostContact;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getGatewayUri() {
            return gatewayUri;
        }

        public void setGatewayUri(String gatewayUri) {
            this.gatewayUri = gatewayUri;
        }
    }
}
