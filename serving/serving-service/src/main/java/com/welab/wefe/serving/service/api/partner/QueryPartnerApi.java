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

package com.welab.wefe.serving.service.api.partner;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.PartnerService;

@Api(path = "partner/query-one", name = "get partner")
public class QueryPartnerApi extends AbstractApi<QueryPartnerApi.Input, QueryPartnerApi.Output> {

    @Autowired
    private PartnerService partnerService;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        if (null != input.getId()) {
            return success(partnerService.queryById(input.getId()));
        } else if (null != input.getName()) {
            return success(partnerService.queryByName(input.getName()));
        }
        return null;
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "client id")
        private String id;

        @Check(name = "client name")
        private String name;

        @Check(name = "客户状态")
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

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
    }

    public static class Output {

        private String id;
        private String name;
        private String email;
        private String remark;
        private String code;
        private String partnerId;
        private boolean isUnionMember;
        private String servingBaseUrl;
        private String createdBy;
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(String partnerId) {
            this.partnerId = partnerId;
        }

        public boolean getIsUnionMember() {
            return isUnionMember;
        }

        public void setIsUnionMember(boolean isUnionMember) {
            this.isUnionMember = isUnionMember;
        }

        public String getServingBaseUrl() {
            return servingBaseUrl;
        }

        public void setServingBaseUrl(String servingBaseUrl) {
            this.servingBaseUrl = servingBaseUrl;
        }
    }

}
