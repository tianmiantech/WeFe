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

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/realname/auth", name = "member_realname_auth", allowAccessWithSign = true)
public class RealnameAuthApi extends AbstractApi<RealnameAuthApi.Input, AbstractApiOutput> {
    @Autowired
    private MemberService memberService;


    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        LOG.info("RealNameAuthApi handle..");
        memberService.realNameAuth(input);
        return success();
    }


    public static class Input extends BaseInput {
        @Check(require = true)
        private String principalName;
        @Check(require = true)
        private String authType;
        private String description;
        @Check(require = true)
        private List<String> fileIdList;
        @Check(require = true)
        private String certRequestContent;
        @Check(require = true)
        private String certRequestId;


        public String getPrincipalName() {
            return principalName;
        }

        public void setPrincipalName(String principalName) {
            this.principalName = principalName;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getFileIdList() {
            return fileIdList;
        }

        public void setFileIdList(List<String> fileIdList) {
            this.fileIdList = fileIdList;
        }

        public String getCertRequestContent() {
            return certRequestContent;
        }

        public void setCertRequestContent(String certRequestContent) {
            this.certRequestContent = certRequestContent;
        }

        public String getCertRequestId() {
            return certRequestId;
        }

        public void setCertRequestId(String certRequestId) {
            this.certRequestId = certRequestId;
        }
    }
}
