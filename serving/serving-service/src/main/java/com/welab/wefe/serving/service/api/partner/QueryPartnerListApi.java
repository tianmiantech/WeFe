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

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.PartnerService;

@Api(path = "partner/query-list", name = "get partner list")
public class QueryPartnerListApi
        extends AbstractApi<QueryPartnerListApi.Input, PagingOutput<QueryPartnerListApi.Output>> {

    @Autowired
    private PartnerService partnerService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) throws Exception {
        return success(partnerService.queryList(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "合作者名称")
        private String partnerName;

        @Check(name = "开始时间")
        private Long startTime;

        @Check(name = "结束时间")
        private Long endTime;

        public String getPartnerName() {
            return partnerName;
        }

        public void setPartnerName(String partnerName) {
            this.partnerName = partnerName;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }
    }

    public static class Output extends AbstractApiOutput {

        @Check(name = "合作者id")
        private String id;

        @Check(name = "合作者名称")
        private String name;

        @Check(name = "合作者邮箱")
        private String email;

        @Check(name = "合作者 code")
        private String code;

        @Check(name = "是否是联邦成员")
        private boolean isUnionMember;

        @Check(name = "Serving服务地址")
        private String servingBaseUrl;

        @Check(name = "备注")
        private String remark;

        @Check(name = "创建时间")
        private Date createdTime;

        @Check(name = "创建人")
        private String createdBy;

        private String updatedBy;

        @Check(name = "合作者状态")
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isUnionMember() {
            return isUnionMember;
        }

        public void setUnionMember(boolean isUnionMember) {
            this.isUnionMember = isUnionMember;
        }

        public String getServingBaseUrl() {
            return servingBaseUrl;
        }

        public void setServingBaseUrl(String servingBaseUrl) {
            this.servingBaseUrl = servingBaseUrl;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

    }
}
