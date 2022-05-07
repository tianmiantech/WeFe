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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.member.MemberOutput;
import com.welab.wefe.union.service.entity.Member;
import com.welab.wefe.union.service.service.MemberContractService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * @author Jervis
 **/
@Api(path = "member/update", name = "member_update", rsaVerify = true, login = false)
public class UpdateApi extends AbstractApi<UpdateApi.Input, MemberOutput> {


    @Autowired
    private MemberContractService memberContractService;

    @Override
    protected ApiResult<MemberOutput> handle(Input input) throws StatusCodeWithException {
        try {
            Member member = putUpdateField(input);
            memberContractService.upsert(member);
        } catch (Exception e) {
            LOG.error("Failed to update member: ", e);
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

    public Member putUpdateField(Input input) throws StatusCodeWithException {
        List<Member> memberList = memberContractService.queryAll(input.getId());
        Member member = memberList.get(0);

        member.setId(input.getId());
        if (StringUtil.isNotEmpty(input.getName())) {
            member.setName(input.getName());
        }
        if (StringUtil.isNotEmpty(input.getMobile())) {
            member.setMobile(input.getMobile());
        }
        if (null != input.getHidden()) {
            member.setHidden(input.getHidden() ? 1 : 0);
        }
        if (null != input.getFreezed()) {
            member.setFreezed(input.getFreezed() ? 1 : 0);
        }
        if (null != input.getLostContact()) {
            member.setLostContact(input.getLostContact() ? 1 : 0);
        }
        if (StringUtil.isNotEmpty(input.getEmail())) {
            member.setEmail(input.getEmail());
        }
        if (null != input.getAllowOpenDataSet()) {
            member.setAllowOpenDataSet(input.getAllowOpenDataSet() ? 1 : 0);
        }
        if (StringUtil.isNotEmpty(input.getGatewayUri())) {
            member.setGatewayUri(input.getGatewayUri());
        }
        if (StringUtil.isNotEmpty(input.getLogo())) {
            member.setLogo(input.getLogo());
        }

        if (StringUtil.isNotEmpty(input.getServingBaseUrl())) {
            MemberExtJSON memberExtJSON = JObject.parseObject(member.getExtJson(), MemberExtJSON.class);
            memberExtJSON.setServingBaseUrl(input.getServingBaseUrl());
            member.setExtJson(JObject.toJSONString(memberExtJSON));
        }

        member.setUpdatedTime(new Date());
        member.setLastActivityTime(System.currentTimeMillis());
        return member;
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
        private String logo;
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

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getServingBaseUrl() {
            return servingBaseUrl;
        }

        public void setServingBaseUrl(String servingBaseUrl) {
            this.servingBaseUrl = servingBaseUrl;
        }
    }

}
