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
import com.welab.wefe.common.data.mongodb.repo.MemberServiceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.member.MemberOutput;
import com.welab.wefe.union.service.entity.Member;
import com.welab.wefe.union.service.service.MemberContractService;
import com.welab.wefe.union.service.service.MemberServiceContractService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/service/update", name = "member_service_update", rsaVerify = true, login = false)
public class UpdateServiceStatusApi extends AbstractApi<UpdateServiceStatusApi.Input, AbstractApiOutput> {


    @Autowired
    private MemberServiceContractService memberServiceContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        try {
            memberServiceContractService.updateServiceStatus(input.serviceId, input.serviceStatus);
        } catch (Exception e) {
            LOG.error("Failed to update member: ", e);
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }


    public static class Input extends BaseInput {
        @Check(require = true)
        private String serviceId;
        @Check(require = true)
        private String memberId;
        @Check(require = true)
        private int serviceStatus;

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

        public int getServiceStatus() {
            return serviceStatus;
        }

        public void setServiceStatus(int serviceStatus) {
            this.serviceStatus = serviceStatus;
        }
    }

}
