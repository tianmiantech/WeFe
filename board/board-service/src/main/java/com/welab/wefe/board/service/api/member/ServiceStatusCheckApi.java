/**
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

package com.welab.wefe.board.service.api.member;

import com.welab.wefe.board.service.dto.vo.MemberServiceStatusOutput;
import com.welab.wefe.board.service.service.ServiceCheckService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author lonnie
 */
@Api(path = "/member/service_status_check", name = "Check whether the member’s system services are normal", login = false)
public class ServiceStatusCheckApi extends AbstractApi<ServiceStatusCheckApi.Input, ServiceStatusCheckApi.Output> {

    @Autowired
    private ServiceCheckService serviceCheckService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        Output output = serviceCheckService.checkMemberServiceStatus(input);

        return success(output);
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "成员id", require = true)
        private String memberId;

        private MemberService service;

        public Input() {
        }

        public Input(String memberId) {
            this.memberId = memberId;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public MemberService getService() {
            return service;
        }

        public void setService(MemberService service) {
            this.service = service;
        }
    }

    public static class Output {
        private boolean allStatusIsSuccess;
        private Map<MemberService, MemberServiceStatusOutput> status;

        public Output() {
        }

        public Output(Map<MemberService, MemberServiceStatusOutput> status) {
            this.allStatusIsSuccess = status
                    .values()
                    .stream()
                    .allMatch(x -> x.isSuccess());

            this.status = status;
        }


        public boolean isAllStatusIsSuccess() {
            return allStatusIsSuccess;
        }

        public void setAllStatusIsSuccess(boolean allStatusIsSuccess) {
            this.allStatusIsSuccess = allStatusIsSuccess;
        }

        public Map<MemberService, MemberServiceStatusOutput> getStatus() {
            return status;
        }

        public void setStatus(Map<MemberService, MemberServiceStatusOutput> status) {
            this.status = status;
        }
    }
}
