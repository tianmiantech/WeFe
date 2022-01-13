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

package com.welab.wefe.union.service.api.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.member.MemberServiceQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.MemberServiceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.member.ApiMemberServiceQueryOutput;
import com.welab.wefe.union.service.mapper.MemberServiceMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/service/query", name = "member_service_query", rsaVerify = true, login = false)
public class QueryApi extends AbstractApi<QueryApi.Input, PageOutput<ApiMemberServiceQueryOutput>> {
    @Autowired
    private MemberServiceMongoReop memberServiceMongoReop;
    private MemberServiceMapper mMapper = Mappers.getMapper(MemberServiceMapper.class);

    @Override
    protected ApiResult<PageOutput<ApiMemberServiceQueryOutput>> handle(Input input) throws StatusCodeWithException {
        try {
            PageOutput<MemberServiceQueryOutput> page = memberServiceMongoReop.find(
                    input.pageIndex,
                    input.pageSize,
                    input.serviceId,
                    input.memberId,
                    input.memberName,
                    input.serviceName,
                    input.serviceType
            );

            List<ApiMemberServiceQueryOutput> list = page.getList().stream()
                    .map(mMapper::transfer)
                    .collect(Collectors.toList());

            return success(new PageOutput<>(
                    page.getPageIndex(),
                    page.getTotal(),
                    page.getPageSize(),
                    page.getTotalPage(),
                    list
            ));
        } catch (Exception e) {
            LOG.error("Failed to query member information in pagination:", e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "Failed to query member information in pagination");
        }

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
