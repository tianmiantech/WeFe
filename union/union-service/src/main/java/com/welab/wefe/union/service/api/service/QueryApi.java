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

package com.welab.wefe.union.service.api.service;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.member.ApiMemberServiceQueryOutput;
import com.welab.wefe.union.service.service.MemberServiceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/service/query", name = "member_service_query", allowAccessWithSign = true)
public class QueryApi extends AbstractApi<QueryApi.Input, PageOutput<ApiMemberServiceQueryOutput>> {
    @Autowired
    private MemberServiceService memberServiceService;

    @Override
    protected ApiResult<PageOutput<ApiMemberServiceQueryOutput>> handle(Input input) throws StatusCodeWithException {
        return success(memberServiceService.query(input));
    }

    public static class Input extends BaseInput {
        private String serviceId;
        private String memberId;
        private String serviceName;
        private String memberName;
        private Integer serviceType;

        private Integer pageIndex = 0;
        private Integer pageSize = 10;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public Integer getServiceType() {
            return serviceType;
        }

        public void setServiceType(Integer serviceType) {
            this.serviceType = serviceType;
        }

        public Integer getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(Integer pageIndex) {
            this.pageIndex = pageIndex;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
    }

}
