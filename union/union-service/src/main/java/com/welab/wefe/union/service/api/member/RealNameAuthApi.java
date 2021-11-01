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

package com.welab.wefe.union.service.api.member;

import com.welab.wefe.common.data.mongodb.entity.union.ext.RealNameAuthFileInfo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.service.MemberContractService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Jervis
 **/
@Api(path = "member/realname/auth", name = "member_realname_auth", rsaVerify = true, login = false)
public class RealNameAuthApi extends AbstractApi<RealNameAuthApi.Input, AbstractApiOutput> {

    @Autowired
    private MemberContractService memberContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        LOG.info("RealNameAuthApi handle..");
        memberContractService.updateExtJson(input);
        return success();
    }


    public static class Input extends BaseInput {
        private String principalName;
        private String authType;
        private String description;
        private List<RealNameAuthFileInfo> realNameAuthFileInfoList;



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

        public List<RealNameAuthFileInfo> getRealNameAuthFileInfoList() {
            return realNameAuthFileInfoList;
        }

        public void setRealNameAuthFileInfoList(List<RealNameAuthFileInfo> realNameAuthFileInfoList) {
            this.realNameAuthFileInfoList = realNameAuthFileInfoList;
        }
    }
}
