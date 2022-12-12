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

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.member.MemberOutput;
import com.welab.wefe.union.service.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Jervis
 **/
@Api(path = "member/add", name = "member_add", allowAccessWithSign = false)
public class AddApi extends AbstractApi<AddApi.Input, MemberOutput> {
    @Autowired
    private MemberService memberService;

    @Override
    protected ApiResult<MemberOutput> handle(Input input) throws StatusCodeWithException {
        LOG.info("AddApi handle..");
        memberService.add(input);
        return success();
    }


    public static class Input extends BaseInput {
        @Check(require = true)
        private String id;
        private String name;
        private String mobile;
        private String email;
        private boolean allowOpenDataSet;
        private boolean hidden;
        private boolean freezed;
        private boolean lostContact;
        private String publicKey;
        private String gatewayUri;
        private String logo;
        private SecretKeyType secretKeyType = SecretKeyType.rsa;
        private String servingBaseUrl;

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

        public boolean isAllowOpenDataSet() {
            return allowOpenDataSet;
        }

        public void setAllowOpenDataSet(boolean allowOpenDataSet) {
            this.allowOpenDataSet = allowOpenDataSet;
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

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public boolean isFreezed() {
            return freezed;
        }

        public void setFreezed(boolean freezed) {
            this.freezed = freezed;
        }

        public boolean isLostContact() {
            return lostContact;
        }

        public void setLostContact(boolean lostContact) {
            this.lostContact = lostContact;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public SecretKeyType getSecretKeyType() {
            return secretKeyType;
        }

        public void setSecretKeyType(SecretKeyType secretKeyType) {
            this.secretKeyType = secretKeyType;
        }

        public String getServingBaseUrl() {
            return servingBaseUrl;
        }

        public void setServingBaseUrl(String servingBaseUrl) {
            this.servingBaseUrl = servingBaseUrl;
        }
    }
}
