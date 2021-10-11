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

import com.welab.wefe.board.service.service.SystemInitializeService;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "member/update", name = "update member info")
public class UpdateMemberInfoApi extends AbstractNoneOutputApi<UpdateMemberInfoApi.Input> {

    @Autowired
    private SystemInitializeService systemInitializeService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        systemInitializeService.updateMemberInfo(input);
        return success();
    }


    public static class Input extends InitializeApi.Input {

        @Check(name = "成员logo")
        private String memberLogo;

        @Check(name = "成员隐身状态")
        private Boolean memberHidden;

        @Check(name = "网关通信地址", require = true, messageOnEmpty = "网关通信地址不能为空")
        private String memberGatewayUri;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (getMemberGatewayUri().contains(":")) {
                String portStr = getMemberGatewayUri().split(":")[1];
                Integer port = Convert.toInt(portStr);
                if (port == null || port < 1 || port > 65535) {
                    throw new StatusCodeWithException("Gateway Uri端口有误，端口范围：1~65535", StatusCode.PARAMETER_VALUE_INVALID);
                }
            }

        }

        //region getter/setter

        public String getMemberLogo() {
            return memberLogo;
        }

        public void setMemberLogo(String memberLogo) {
            this.memberLogo = memberLogo;
        }

        public Boolean getMemberHidden() {
            return memberHidden;
        }

        public void setMemberHidden(Boolean memberHidden) {
            this.memberHidden = memberHidden;
        }


        public String getMemberGatewayUri() {
            return memberGatewayUri;
        }

        public void setMemberGatewayUri(String memberGatewayUri) {
            this.memberGatewayUri = memberGatewayUri;
        }

        //endregion
    }


}
